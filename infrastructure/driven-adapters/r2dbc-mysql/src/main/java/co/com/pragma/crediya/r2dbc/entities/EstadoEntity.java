package co.com.pragma.crediya.r2dbc.entities;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table(name="estados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EstadoEntity {
    @Id
    @Column("id_estado")
    private Long idNumber;
    private String nombre;
    private String descripcion;
}
