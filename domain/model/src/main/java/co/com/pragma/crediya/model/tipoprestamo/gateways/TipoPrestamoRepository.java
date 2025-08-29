package co.com.pragma.crediya.model.tipoprestamo.gateways;

import co.com.pragma.crediya.model.tipoprestamo.TipoPrestamo;
import reactor.core.publisher.Mono;

public interface TipoPrestamoRepository {
    Mono<TipoPrestamo> findById(Long id);
}
