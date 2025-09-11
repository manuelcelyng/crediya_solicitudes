package co.com.pragma.crediya.r2dbc.mappers;

import co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.DeudaMensual;
import co.com.pragma.crediya.r2dbc.dto.DeudaMensualDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeudaMensuaEntityMapper {


    DeudaMensual toModel(DeudaMensualDTO dto);
}
