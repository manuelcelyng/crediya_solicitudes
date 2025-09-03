package co.com.pragma.crediya.model.solicitud.gateways;

import reactor.core.publisher.Mono;

public interface RestConsumerRepository {

    Mono<Boolean> getValid(String email, String documentoIdentidad);
}
