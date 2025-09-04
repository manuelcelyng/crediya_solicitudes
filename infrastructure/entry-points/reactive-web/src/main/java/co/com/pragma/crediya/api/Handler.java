package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.CreateSolicitudDTO;
import co.com.pragma.crediya.api.mapper.SolicitudDtoMapper;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.usecase.solicitud.SolicitudUseCase;
import co.com.pragma.crediya.usecase.solicitud.exceptions.TypeErrors;
import co.com.pragma.crediya.usecase.solicitud.exceptions.UserValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

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
                    String userdocumentId = auth.getName(); // = sub trae el documento de identidad
                    String email  = auth.getTokenAttributes().get("email") != null ? auth.getTokenAttributes().get("email").toString() : null;

                    // (opcional) cruzar identidad declarada vs token
                    if (dto.email() != null && email != null &&
                            !dto.email().equalsIgnoreCase(email)) {
                        return Mono.error(new UserValidationException(TypeErrors.USER_VALIDATION_ERROR,
                                "Email de la solicitud no coincide con el token"));
                    }
                    if (dto.email() != null && !dto.documentoIdentidad().equals(userdocumentId)) {
                        return Mono.error(new UserValidationException(TypeErrors.USER_VALIDATION_ERROR,
                                "El documento del Token no corresponde al documento de la solicitud"));
                    }
                    var model = solicitudDtoMapper.toModel(dto);
                    return solicitudUseCase.saveSolicitud(model)
                            .doOnSuccess(s -> log.info("[CREATE_SOLICITUD] Solicitud persisted id={}", s.getIdNumber()))
                            .flatMap(saved -> ServerResponse.status(HttpStatus.CREATED)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(solicitudDtoMapper.toResponse(saved)));
                })
                .doOnError(ex -> log.error("[CREATE_SOLICITUD] Error: {}", ex.toString()));
    }


    public Mono<ServerResponse> listenGetSolicitudes(ServerRequest serverRequest) {
        // Ya no tomamos parámetros por query. Leemos un SimplePageRequest del cuerpo.
        return serverRequest.bodyToMono(SimplePageRequest.class)
                .switchIfEmpty(Mono.just(new SimplePageRequest()))
                .flatMap(pr -> solicitudUseCase.page(pr)
                        .flatMap(sp -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(sp))
                ).doOnError(ex -> log.error("[GET_SOLICITUDES] Error: {}", ex.toString()));
    }



}
