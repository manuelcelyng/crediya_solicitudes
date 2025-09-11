package co.com.pragma.crediya.model.solicitud.gateways;

import co.com.pragma.crediya.model.solicitud.SQSMessage;
import co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.SQSDataValidacionPrestamo;
import reactor.core.publisher.Mono;

public interface SQSCambioEstadoGateway {
    public Mono<String> send(SQSMessage message);

}
