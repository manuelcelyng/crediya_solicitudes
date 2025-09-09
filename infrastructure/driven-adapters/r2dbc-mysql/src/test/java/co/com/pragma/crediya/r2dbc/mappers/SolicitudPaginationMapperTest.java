package co.com.pragma.crediya.r2dbc.mappers;

import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.r2dbc.dto.SolicitudFieldsPageDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudPaginationMapperTest {

    private final SolicitudPaginationMapper mapper = Mappers.getMapper(SolicitudPaginationMapper.class);

    @Test
    void toModel_shouldMapBasicFieldsAndIgnoreUserFields() {
        SolicitudFieldsPageDto dto = SolicitudFieldsPageDto.builder()
                .monto(new BigDecimal("123.45"))
                .plazo(24)
                .email("test@example.com")
                .tipoPrestamo("Vivienda")
                .estado("APROBADA")
                .build();

        SolicitudFieldsPage model = mapper.toModel(dto);

        assertNotNull(model);
        assertEquals(new BigDecimal("123.45"), model.getMonto());
        assertEquals(24, model.getPlazo());
        assertEquals("test@example.com", model.getEmail());
        assertEquals("Vivienda", model.getTipoPrestamo());
        assertEquals("APROBADA", model.getEstado());
        // Ignored user fields must remain null
        assertNull(model.getNombre());
        assertNull(model.getSalarioBase());
        assertNull(model.getMontoMensualSolicitud());
    }
}
