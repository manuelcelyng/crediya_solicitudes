package co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.sendtoSQS;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class SQSDataValidacionPrestamo{
    Long idSolicitud;
    String email;
    BigDecimal ingresoCliente;
    List<DeudaMensual> deudaMensualSolicitudesAprobadas;
    DeudaMensual deudaMensualSolicitudNueva;
}
