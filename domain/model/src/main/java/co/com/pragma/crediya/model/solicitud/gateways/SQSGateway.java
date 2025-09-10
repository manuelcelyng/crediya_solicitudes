package co.com.pragma.crediya.model.solicitud.gateways;

import co.com.pragma.crediya.model.solicitud.SQSMessage;
import reactor.core.publisher.Mono;

public interface SQSGateway {
    public Mono<String> send(SQSMessage message);
}
