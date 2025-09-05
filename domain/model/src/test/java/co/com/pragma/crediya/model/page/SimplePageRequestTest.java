package co.com.pragma.crediya.model.page;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimplePageRequestTest {

    @Test
    void fixQueryShouldHandleAsteriskAndRegular() {
        SimplePageRequest req = new SimplePageRequest();
        req.setQuery("*");
        assertEquals("%", req.fixQueryFL());

        req.setQuery("loan");
        assertEquals("%loan%", req.fixQueryFL());
    }
}
