package co.com.pragma.crediya.r2dbc.mappers;

import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.r2dbc.dto.SolicitudFieldsPageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SolicitudPaginationMapper {

    SolicitudFieldsPage toModel(SolicitudFieldsPageDto dto);
}
