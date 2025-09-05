package co.com.pragma.crediya.model.solicitud;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdDocumentTest {

    @Test
    void shouldAcceptDigitsUpTo10() {
        IdDocument id1 = new IdDocument("1");
        assertEquals("1", id1.documento());
        IdDocument id2 = new IdDocument("0000000001");
        assertEquals("0000000001", id2.documento());
        // Validación exacta de 10 dígitos
        assertEquals("1234567890", new IdDocument("1234567890").documento());
    }

    @Test
    void shouldRejectNullBlankAndNonDigitsOrTooLong() {
        assertThrows(IllegalArgumentException.class, () -> new IdDocument(null));
        assertThrows(IllegalArgumentException.class, () -> new IdDocument("   "));
        assertThrows(IllegalArgumentException.class, () -> new IdDocument("12345678901")); // 11 digits
        assertThrows(IllegalArgumentException.class, () -> new IdDocument("12A456"));
        assertThrows(IllegalArgumentException.class, () -> new IdDocument(" 12345 ")); // spaces fail
    }
}
