package co.com.pragma.crediya.r2dbc.mappers;


import co.com.pragma.crediya.model.solicitud.Email;
import co.com.pragma.crediya.model.solicitud.IdDocument;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.r2dbc.entities.SolicitudEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SolicitudEntityMapper {

    default SolicitudEntity toEntity(Solicitud solicitud) {
        if (solicitud == null) return null;
        return SolicitudEntity.builder()
                .idNumber(solicitud.getIdNumber())
                .monto(solicitud.getMonto())
                .idTipoPrestamo(solicitud.getIdTipoPrestamo())
                .idEstado(solicitud.getIdEstado())
                .plazo(solicitud.getPlazo())
                .email(solicitud.getEmail().email())
                .documentoIdentidad(solicitud.getDocumentoIdentidad().documento())
                .build();
    }

    default Solicitud toDomain(SolicitudEntity entity) {
        if (entity == null) return null;
        Email email = entity.getEmail() == null ? null : new Email(entity.getEmail());
        IdDocument doc = entity.getDocumentoIdentidad() == null ? null : new IdDocument(entity.getDocumentoIdentidad());
        return Solicitud.create(
                entity.getIdEstado(),
                entity.getIdTipoPrestamo(),
                entity.getMonto(),
                entity.getPlazo(),
                email,
                doc
        ).withIdNumber(entity.getIdNumber());
    }
}
