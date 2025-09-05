package co.com.pragma.crediya.model.page.solicitud;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Setter
@Getter
public class SolicitudFieldsPage{
        private BigDecimal monto;
        private Integer plazo;
        private String email;
        private String tipoPrestamo;
        private BigDecimal tasaInteres;
        private String estado;
        private String nombre;
        private BigDecimal salarioBase;
        private BigDecimal montoMensualSolicitud;


}
