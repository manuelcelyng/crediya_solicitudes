package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.CreateSolicitudDTO;
import co.com.pragma.crediya.api.mapper.SolicitudDtoMapper;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.usecase.solicitud.SolicitudUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {

    private final SolicitudUseCase solicitudUseCase;
    private final SolicitudDtoMapper solicitudDtoMapper;
    private final Validator validator;


    public Mono<ServerResponse> listenSaveSolicitud(ServerRequest serverRequest) {

        // 1) Mono del principal (JWT)
        Mono<JwtAuthenticationToken> authMono = serverRequest.principal()
                .cast(JwtAuthenticationToken.class)
                .switchIfEmpty(Mono.error(new AccessDeniedException("No autenticado"))); // 401

        // 2) Mono del body + validación Bean Validation
        Mono<CreateSolicitudDTO> dtoMono = serverRequest.bodyToMono(CreateSolicitudDTO.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Request Body is Required")))
                .doOnSubscribe(sub -> log.info("[CREATE_SOLICITUD] Request received"))
                .flatMap(dto -> {
                    var violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        log.warn("[CREATE_SOLICITUD] Validation failed: {} violation(s)", violations.size());
                        return Mono.error(new ConstraintViolationException(violations));
                    }
                    return Mono.just(dto);
                });


        // useCase.logic();
        // 3) Combinar principal + body, aplicar reglas y persistir
        return Mono.zip(authMono, dtoMono)
                .flatMap(tuple -> {
                    JwtAuthenticationToken auth = tuple.getT1();
                    CreateSolicitudDTO dto     = tuple.getT2();


                    // Identidad desde el token
                    String userId = auth.getName(); // = sub trae el id del usuario
                    String email  = auth.getTokenAttributes().get("email") != null ? auth.getTokenAttributes().get("email").toString() : null;

                    // (opcional) cruzar identidad declarada vs token
                    if (dto.email() != null && email != null &&
                            !dto.email().equalsIgnoreCase(email)) {
                        return ServerResponse.status(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error","FORBIDDEN","message","Email no coincide con el token"));
                    }
                    if (dto.email() != null && !dto.documentoIdentidad().equals(userId)) {
                        return ServerResponse.status(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error","FORBIDDEN","message","Token no corresponde al Cliente"));
                    }

                    // 4) Forzar dueño desde el token (mejor que confiar en el body)
                    var model = solicitudDtoMapper.toModel(dto);


                    return solicitudUseCase.saveSolicitud(model)
                            .doOnSuccess(s -> log.info("[CREATE_SOLICITUD] Solicitud persisted id={}", s.getIdNumber()))
                            .flatMap(saved -> ServerResponse.status(HttpStatus.CREATED)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(solicitudDtoMapper.toResponse(saved)));
                })
                .doOnError(ex -> log.error("[CREATE_SOLICITUD] Error: {}", ex.toString()));

    }
}
