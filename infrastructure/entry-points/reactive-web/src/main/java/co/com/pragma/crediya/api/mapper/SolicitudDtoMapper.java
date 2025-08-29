package co.com.pragma.crediya.api.mapper;

import co.com.pragma.crediya.api.dto.CreateSolicitudDTO;
import co.com.pragma.crediya.api.dto.ResponseSolicitudDTO;
import co.com.pragma.crediya.model.solicitud.Email;
import co.com.pragma.crediya.model.solicitud.IdDocument;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.shared.mappers.EmailMapper;
import co.com.pragma.crediya.shared.mappers.IdDocumentMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = "spring",
        uses = {
                EmailMapper.class,
                IdDocumentMapper.class
        },
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SolicitudDtoMapper {
    // Manual factory-based mapping due to domain factory method
    default Solicitud toModel(CreateSolicitudDTO dto) {
        if (dto == null) return null;
        return Solicitud.create(
                dto.idEstado(),
                dto.idTipoPrestamo(),
                dto.monto(),
                dto.plazo(),
                dto.email() == null ? new Email(null): new Email(dto.email()),
                dto.documentoIdentidad() == null ? new IdDocument(null): new IdDocument(dto.documentoIdentidad())
        );
    }

    @Mappings({
            @Mapping(target = "idNumber", source = "idNumber"),
            @Mapping(target = "documentoIdentidad", source = "documentoIdentidad", qualifiedByName = "fromIdDocument"),
            @Mapping(target = "email", source = "email", qualifiedByName = "fromEmail"),
    })
    ResponseSolicitudDTO toResponse(Solicitud solicitud);


}
