package co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.sendtoSQS;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DeudaMensual
{
    private BigDecimal monto;
    private Integer plazo;
    private BigDecimal tasaInteres;
}
