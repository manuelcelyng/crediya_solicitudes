package co.com.pragma.crediya.model.page.usuarios;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SolicitudUsersFieldsPage(
        String nombre,
        BigDecimal salarioBase,
        String correoElectronico
) {
}
