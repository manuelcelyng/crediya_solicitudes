package co.com.pragma.crediya.sqs.listener.mappers;

import co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.receiveFromSQS.ResultadoValidacion;
import co.com.pragma.crediya.sqs.listener.dto.ResultadoValidacionDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SqsResponseMapper {
            ResultadoValidacion toModel(ResultadoValidacionDTO dto);
}
