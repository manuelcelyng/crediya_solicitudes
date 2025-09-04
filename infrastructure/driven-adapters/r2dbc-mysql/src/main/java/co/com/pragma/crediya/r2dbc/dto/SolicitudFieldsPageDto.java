package co.com.pragma.crediya.r2dbc.dto;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SolicitudFieldsPageDto {

    @Column("monto")
    private BigDecimal monto;

    @Column("plazo")
    private Integer plazo;

    @Column("email")
    private String email;

    // Usa alias snake_case en el SQL y lo mapeas al camelCase del campo
    @Column("tipo_prestamo")
    private String tipoPrestamo;

    @Column("estado")
    private String estado;
}