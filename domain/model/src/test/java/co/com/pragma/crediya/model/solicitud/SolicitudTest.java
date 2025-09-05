package co.com.pragma.crediya.model.solicitud;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudTest {

    @Test
    void shouldCreateWithAllFieldsAndAllowNullEstado() {
        Email email = new Email("user@example.com");
        IdDocument doc = new IdDocument("1234567890");
        // idEstado null segun comentario y use case: debe ser permitido por el modelo
        Solicitud solicitud = Solicitud.create(null, 10L, new BigDecimal("1000"), 12, email, doc);
        assertNull(solicitud.getIdEstado());
        assertEquals(10L, solicitud.getIdTipoPrestamo());
        assertEquals(new BigDecimal("1000"), solicitud.getMonto());
        assertEquals(12, solicitud.getPlazo());
        assertEquals(email, solicitud.getEmail());
        assertEquals(doc, solicitud.getDocumentoIdentidad());
    }

    @Test
    void shouldFailWhenRequiredFieldsNullExceptEstado() {
        Email email = new Email("user@example.com");
        IdDocument doc = new IdDocument("1234567890");
        assertThrows(IllegalArgumentException.class, () -> Solicitud.create(1L, null, new BigDecimal("1000"), 12, email, doc));
        assertThrows(IllegalArgumentException.class, () -> Solicitud.create(1L, 2L, null, 12, email, doc));
        assertThrows(IllegalArgumentException.class, () -> Solicitud.create(1L, 2L, new BigDecimal("1000"), null, email, doc));
    }

    @Test
    void withersShouldCreateNewInstances() {
        Email email = new Email("user@example.com");
        IdDocument doc = new IdDocument("1234567890");
        Solicitud base = Solicitud.create(1L, 2L, new BigDecimal("1000"), 12, email, doc);

        Solicitud withId = base.withIdNumber(99L);
        assertNotSame(base, withId);
        assertEquals(99L, withId.getIdNumber());
        assertEquals(base.getIdTipoPrestamo(), withId.getIdTipoPrestamo());

        Solicitud withEstado = base.withIdEstado(5L);
        assertNotSame(base, withEstado);
        assertEquals(5L, withEstado.getIdEstado());
        assertEquals(base.getIdTipoPrestamo(), withEstado.getIdTipoPrestamo());
    }
}
