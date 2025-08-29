package co.com.pragma.crediya.r2dbc.solicitud;

import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.crediya.r2dbc.entities.SolicitudEntity;
import co.com.pragma.crediya.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.crediya.r2dbc.mappers.SolicitudEntityMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
    Long,
        MyReactiveRepository
> implements SolicitudRepository {

            private final SolicitudEntityMapper solicitudEntityMapper;

            public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, SolicitudEntityMapper solicitudEntityMapper) {
                super(repository, mapper, d -> mapper.map(d, Solicitud.class));
                this.solicitudEntityMapper = solicitudEntityMapper;
            }

    @Override
    public Mono<Solicitud> saveSolicitud(Solicitud solicitud) {

        return super.repository.save(solicitudEntityMapper.toEntity(solicitud))
                .map(solicitudEntityMapper::toDomain);
    }
}
