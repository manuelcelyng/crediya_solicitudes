package co.com.pragma.crediya.r2dbc.entities;

import co.com.pragma.crediya.model.solicitud.Email;
import co.com.pragma.crediya.model.solicitud.IdDocument;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name="solicitud")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class SolicitudEntity {
    @Id
    @Column("id_solicitud")
    private Long idNumber;
    private BigDecimal monto;

    @Column("id_tipo_prestamo")
    private Long idTipoPrestamo;
    @Column("id_estado")
    private Long idEstado; // SI ESTO VIENE NULL -> SE LE ASIGNA ESTADO "Pendiente de revision"
    private Integer plazo; // Segun la H7 , se hace un calculo y el valor representa numero de meses
    private String email;
    @Column("documento_identidad")
    private String documentoIdentidad; // H2

}
