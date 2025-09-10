package co.com.pragma.crediya.model.solicitud.gateways;

import co.com.pragma.crediya.model.page.SimplePage;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

public interface SolicitudRepository {
    Mono<Solicitud> saveSolicitud(Solicitud solicitud);

    Mono<SimplePage<SolicitudFieldsPage>> page(SimplePageRequest pageRequest);

    Mono<Solicitud> findById(Long id);

}
