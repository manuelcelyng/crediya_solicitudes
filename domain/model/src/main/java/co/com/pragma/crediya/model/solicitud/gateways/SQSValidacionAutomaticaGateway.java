package co.com.pragma.crediya.model.solicitud.gateways;

import co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.sendtoSQS.SQSDataValidacionPrestamo;
import reactor.core.publisher.Mono;

public interface SQSValidacionAutomaticaGateway {

    public Mono<String> sendSolicitudValidacionAutomatica(SQSDataValidacionPrestamo message);
}
