package co.com.pragma.crediya.r2dbc.mappers;

import co.com.pragma.crediya.model.solicitud.Email;
import co.com.pragma.crediya.model.solicitud.IdDocument;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.r2dbc.entities.SolicitudEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudEntityMapperTest {

    private final SolicitudEntityMapper mapper = new SolicitudEntityMapper() {};

    @Test
    void toEntity_shouldMapAllFields() {
        Solicitud domain = Solicitud.create(1L, 2L, new BigDecimal("500"), 12,
                new Email("user@example.com"), new IdDocument("1234567890"))
                .withIdNumber(99L);

        SolicitudEntity entity = mapper.toEntity(domain);
        assertEquals(99L, entity.getIdNumber());
        assertEquals(new BigDecimal("500"), entity.getMonto());
        assertEquals(2L, entity.getIdTipoPrestamo());
        assertEquals(1L, entity.getIdEstado());
        assertEquals(12, entity.getPlazo());
        assertEquals("user@example.com", entity.getEmail());
        assertEquals("1234567890", entity.getDocumentoIdentidad());
    }

    @Test
    void toDomain_shouldHandleNullsForEmailAndDocumento() {
        SolicitudEntity entity = SolicitudEntity.builder()
                .idNumber(10L)
                .idEstado(null)
                .idTipoPrestamo(2L)
                .monto(new BigDecimal("700"))
                .plazo(24)
                .email(null)
                .documentoIdentidad(null)
                .build();

        Solicitud domain = mapper.toDomain(entity);
        assertEquals(10L, domain.getIdNumber());
        assertNull(domain.getIdEstado());
        assertEquals(2L, domain.getIdTipoPrestamo());
        assertEquals(new BigDecimal("700"), domain.getMonto());
        assertEquals(24, domain.getPlazo());
        assertNull(domain.getEmail());
        assertNull(domain.getDocumentoIdentidad());
    }
}
