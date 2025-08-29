package co.com.pragma.crediya.model.estado;
import lombok.*;
//import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Estado {
    private Long idNumber;
    private String nombre;
    private String descripcion;
}
