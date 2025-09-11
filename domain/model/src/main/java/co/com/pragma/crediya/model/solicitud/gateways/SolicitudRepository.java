package co.com.pragma.crediya.model.solicitud.gateways;

import co.com.pragma.crediya.model.page.SimplePage;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.DeudaMensual;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SolicitudRepository {
    Mono<Solicitud> saveSolicitud(Solicitud solicitud);

    Mono<SimplePage<SolicitudFieldsPage>> page(SimplePageRequest pageRequest);

    Mono<Solicitud> findById(Long id);

    Mono<List<DeudaMensual>> getListDeudaMensualPrestamosAprobados(String email);
}
