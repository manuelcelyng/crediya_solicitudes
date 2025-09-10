package co.com.pragma.crediya.model.solicitud;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record SQSMessage(
        String idSolicitud,
        String estado,
        String correo,
        String documento,
        BigDecimal cantidad,// o String si prefieres
        String tipo,
        String mensaje
) {
}
/*
{
  "idSolicitud": "12345",
  "estado": "APROBADA",
  "correo": "usuario@dominio.com",
  "documento": "CC123456789",
  "cantidad": 1500.75,
  "tipo": "TRANSFERENCIA",
  "mensaje": "La solicitud fue procesada exitosamente"
}

 */