package co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.receiveFromSQS;

import java.util.List;

public record ResultadoValidacion(
        Long idSolicitud,
        String email,
        Boolean aprobada,
        List<Cuota> plan,
        String mensaje
) {
}
