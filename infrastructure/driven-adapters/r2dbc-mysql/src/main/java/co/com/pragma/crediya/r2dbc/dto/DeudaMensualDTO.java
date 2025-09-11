package co.com.pragma.crediya.r2dbc.dto;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DeudaMensualDTO {

    @Column("monto")
    private BigDecimal monto;
    @Column("plazo")
    private Integer plazo;
    @Column("tasa_interes")
    private BigDecimal tasaInteres;
}
