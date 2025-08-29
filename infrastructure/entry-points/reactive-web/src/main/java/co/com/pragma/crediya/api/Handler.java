package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.CreateSolicitudDTO;
import co.com.pragma.crediya.api.mapper.SolicitudDtoMapper;
import co.com.pragma.crediya.usecase.solicitud.SolicitudUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {

    private final SolicitudUseCase solicitudUseCase;
    private final SolicitudDtoMapper solicitudDtoMapper;
    private final Validator validator;


    public Mono<ServerResponse> listenSaveSolicitud(ServerRequest serverRequest) {
        // useCase.logic();
        return serverRequest
                .bodyToMono(CreateSolicitudDTO.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Request Body is Required")))
                .doOnSubscribe(sub -> log.info("[CREATE_SOLICITUD] Request received"))
                .flatMap(dto -> {
                    Set<ConstraintViolation<CreateSolicitudDTO>> violations =  validator.validate(dto);
                    if (!violations.isEmpty()) {
                        log.warn("[CREATE_SOLICITUD] Validation failed: {} violation(s)", violations.size());
                        return Mono.error(new ConstraintViolationException(violations));
                    }
                    return Mono.just(dto);
                })
                .map(solicitudDtoMapper::toModel)
                .flatMap(solicitudUseCase::saveSolicitud)
                .doOnSuccess(s -> log.info("[CREATE_SOLICITUD] Solicitud persisted with id={}", s.getIdNumber()))
                .flatMap(savedSolicitud -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(solicitudDtoMapper.toResponse(savedSolicitud)))
                .doOnError(ex -> log.error("[CREATE_SOLICITUD] Error: {}", ex.toString()));

    }
}
