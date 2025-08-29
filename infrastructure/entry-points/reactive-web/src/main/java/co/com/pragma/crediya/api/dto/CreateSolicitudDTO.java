package co.com.pragma.crediya.api.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateSolicitudDTO(
        @NotNull
        BigDecimal monto,

        @NotNull
        Long idTipoPrestamo,


        Long idEstado, // SI ESTO VIENE NULL -> SE LE ASIGNA ESTADO "Pendiente de revision

        @NotNull
        Integer plazo, // Segun la H7 , se hace un calculo y el valor representa numero de mese

        @Email
        @NotBlank
        String  email,

        @NotBlank
        String documentoIdentidad // H

) {


}
