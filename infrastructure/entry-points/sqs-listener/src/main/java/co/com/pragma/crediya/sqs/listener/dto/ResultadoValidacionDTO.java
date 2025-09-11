package co.com.pragma.crediya.sqs.listener.dto;

import java.util.List;


public record ResultadoValidacionDTO(
        Long idSolicitud,
        String email,
        Boolean aprobada,
        List<CuotaDTO> plan,
        String mensaje
) {}
