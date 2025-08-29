package co.com.pragma.crediya.model.estado.gateways;

import co.com.pragma.crediya.model.estado.Estado;
import reactor.core.publisher.Mono;

public interface EstadoRepository {
    Mono<Estado> findById(Long id);
}
