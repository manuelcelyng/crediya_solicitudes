package co.com.pragma.crediya.sqs.listener.dto;

import java.math.BigDecimal;

public record CuotaDTO(
        Integer numeroCuota,
        BigDecimal cuota,
        BigDecimal abonoCapital,
        BigDecimal pagoIntereses,
        BigDecimal saldoRestante
) {
}
