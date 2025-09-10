package co.com.pragma.crediya.api.dto;

import lombok.Builder;

@Builder
public record UpdateEstadoInSolicidudDTO(
        Long idEstado,
        Long idSolicitud
) {
}
