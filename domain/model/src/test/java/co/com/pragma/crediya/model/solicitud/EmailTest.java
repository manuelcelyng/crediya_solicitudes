package co.com.pragma.crediya.model.solicitud;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    void shouldNormalizeAndAcceptValidEmail() {
        Email email = new Email("  USER@Example.COM ");
        assertEquals("user@example.com", email.email());
    }

    @Test
    void shouldRejectNullOrBlank() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> new Email(null));
        assertTrue(ex1.getMessage().contains("no puede ser nulo"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> new Email("   "));
        assertTrue(ex2.getMessage().contains("no puede ser nulo"));
    }

    @Test
    void shouldRejectBadFormat() {
        assertThrows(IllegalArgumentException.class, () -> new Email("no-at-domain"));
        assertThrows(IllegalArgumentException.class, () -> new Email("a@b"));
        assertThrows(IllegalArgumentException.class, () -> new Email("a@b."));
        assertThrows(IllegalArgumentException.class, () -> new Email("@domain.com"));
    }
}
