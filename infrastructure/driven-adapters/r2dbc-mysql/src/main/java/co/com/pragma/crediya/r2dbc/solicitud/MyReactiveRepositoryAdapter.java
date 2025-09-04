package co.com.pragma.crediya.r2dbc.solicitud;

import co.com.pragma.crediya.model.page.SimplePage;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.crediya.r2dbc.dto.SolicitudFieldsPageDto;
import co.com.pragma.crediya.r2dbc.entities.SolicitudEntity;
import co.com.pragma.crediya.r2dbc.helper.ReactiveAdapterOperations;

import co.com.pragma.crediya.r2dbc.mappers.SolicitudEntityMapper;
import co.com.pragma.crediya.r2dbc.mappers.SolicitudPaginationMapper;
import org.reactivecommons.utils.ObjectMapper;
;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
    Long,
        MyReactiveRepository
> implements SolicitudRepository {

            private final SolicitudEntityMapper solicitudEntityMapper;
            private final SolicitudPaginationMapper solicitudPaginationMapper;

            public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, SolicitudEntityMapper solicitudEntityMapper, SolicitudPaginationMapper solicitudPaginationMapper) {
                super(repository, mapper, d -> mapper.map(d, Solicitud.class));
                this.solicitudEntityMapper = solicitudEntityMapper;
                this.solicitudPaginationMapper = solicitudPaginationMapper;
            }

    @Override
    public Mono<Solicitud> saveSolicitud(Solicitud solicitud) {

        return super.repository.save(solicitudEntityMapper.toEntity(solicitud))
                .map(solicitudEntityMapper::toDomain);
    }

    @Override
    public Mono<SimplePage<SolicitudFieldsPage>> page(SimplePageRequest req) {

        // 1) Sanea inputs
        int size  = Math.min(Math.max(1, req.getSize() == null ? 50 : req.getSize()), 200);
        int page  = Math.max(0, req.getPage() == null ? 0 : req.getPage());
        long offset = (long) page * size;

        // sort solo decide si llamamos a ASC o DESC (columna fija id_solicitud)
        boolean desc = "DESC".equalsIgnoreCase(req.getSort());
        String sortLabel = "id_solicitud " + (desc ? "DESC" : "ASC");

        // 2) Normaliza filtros
        // query: "*" => "%" , otro => "%texto%"
        String q = req.getQuery();
        q = (q == null || q.isBlank() || "*".equals(q)) ? "%" : "%" + q.trim() + "%";

        // estadoNombre: tomamos el primero si viene lista (o null si vacío)
        List<String> estados= null;
        if (req.getStatus() != null && !req.getStatus().isEmpty()) {
            estados = req.getStatus(); // TODO: si quieres IN, habrá que ajustar el SQL
        }

        // 3) Ejecuta la página + total
        Flux<SolicitudFieldsPageDto> pageFlux =
                desc
                        ? repository.solicitudPageDESC(estados, q, size, offset)
                        : repository.solicitudPageASC (estados, q, size, offset);

        Mono<List<SolicitudFieldsPage>> itemsMono = pageFlux.map(solicitudPaginationMapper::toModel).collectList();
        Mono<Long> totalMono = repository.countResumen(estados, q);

        // 4) Combina y arma SimplePage
        return Mono.zip(itemsMono, totalMono)
                .map(t -> {
                    var items = t.getT1();
                    var total = t.getT2();
                    boolean hasNext = (offset + items.size()) < total;

                    return new SimplePage<>(
                            items,
                            total,
                            size,
                            page,
                            hasNext,
                            sortLabel
                    );
                });
    }
}
