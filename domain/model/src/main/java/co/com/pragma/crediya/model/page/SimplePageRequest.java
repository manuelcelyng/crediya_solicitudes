package co.com.pragma.crediya.model.page;

import lombok.Data;
import java.util.List;

@Data
public class SimplePageRequest {

    // Offset pagination (still supported)
    Integer page = 0;
    Integer size = 50;

    // Sorting and filtering placeholders
    String sort = "ASC";
    String columnSort = ""; // Organizar por Columna
    String query = "%";  // Para filtrar por tipo de prestamo.
    List<String> status;

    // Keyset (cursor) pagination

    // Crea un String query que ayuda a buscar segun el patron indicado al hacer la consulta
    public String fixQueryFL() {
        return ("*".equals(query)) ? "%" : "%" + query + "%";
    }
}
