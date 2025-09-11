package co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.receiveFromSQS;

import java.math.BigDecimal;

public record Cuota(
        Integer numeroCuota,
        BigDecimal cuota,
        BigDecimal abonoCapital,
        BigDecimal pagoIntereses,
        BigDecimal saldoRestante
) {
}
