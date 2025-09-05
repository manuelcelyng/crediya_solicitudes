package co.com.pragma.crediya.r2dbc.mappers;

import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.r2dbc.dto.SolicitudFieldsPageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudPaginationMapper {

    // Aquí ignoro los campos , aunque esto por defecto lo hace , es mejor indicar que se está ignorando :D
    // Esos datos son del usuario y vienen el ms de autenticación
    @Mapping(target = "nombre", ignore = true)
    @Mapping(target = "salarioBase", ignore = true)
    @Mapping(target = "montoMensualSolicitud", ignore = true)
    SolicitudFieldsPage toModel(SolicitudFieldsPageDto dto);
}
