package co.com.pragma.crediya.model.page.solicitud;

import lombok.*;

import java.math.BigDecimal;

@Builder
public record SolicitudFieldsPage(
        BigDecimal monto,
        Integer plazo,
        String email,
        String tipoPrestamo,
        String estado
) {
}
