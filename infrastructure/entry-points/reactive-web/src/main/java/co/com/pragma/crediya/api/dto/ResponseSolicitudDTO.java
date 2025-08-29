package co.com.pragma.crediya.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ResponseSolicitudDTO(
        Long idNumber,
        BigDecimal monto,
        Long idTipoPrestamo,
        Long idEstado, // SI ESTO VIENE NULL -> SE LE ASIGNA ESTADO "Pendiente de revision
        Integer plazo, // Segun la H7 , se hace un calculo y el valor representa numero de mese
        String  email,
        String documentoIdentidad // H
) {

}
