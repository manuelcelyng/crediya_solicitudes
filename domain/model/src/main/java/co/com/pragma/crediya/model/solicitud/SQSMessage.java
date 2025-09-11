package co.com.pragma.crediya.model.solicitud;

import co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.receiveFromSQS.Cuota;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record SQSMessage(
        String idSolicitud,
        String estado,
        String correo,
        String documento,
        BigDecimal cantidad,// o String si prefieres
        String tipo,
        String mensaje,
        List<Cuota> plan
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