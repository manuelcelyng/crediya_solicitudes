package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.model.estado.EstadoCodigos;
import co.com.pragma.crediya.model.estado.gateways.EstadoRepository;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;

import co.com.pragma.crediya.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.crediya.usecase.solicitud.exceptions.EstadoNotFound;

import co.com.pragma.crediya.usecase.solicitud.exceptions.MontoOutRange;
import co.com.pragma.crediya.usecase.solicitud.exceptions.TipoPrestamoNotFound;
import co.com.pragma.crediya.usecase.solicitud.exceptions.TypeErrors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadosRepository;
    private final TipoPrestamoRepository tiposPrestamoRepository;



    public Mono<Solicitud> saveSolicitud(Solicitud solicitud) {
        return tiposPrestamoRepository.findById(solicitud.getIdTipoPrestamo())
                .switchIfEmpty(Mono.error(new TipoPrestamoNotFound(TypeErrors.TIPO_PRESTAMO_NOT_FOUND , "Tipo de Prestamo no encontrado")))
                .flatMap(prestamo ->
                        validarLimitesMonto(prestamo.getMontoMinimo(), prestamo.getMontoMaximo(), solicitud.getMonto())
                )
                .filter(Boolean::booleanValue)// de Boolean -> error si es false
                .switchIfEmpty(
                        Mono.error(new MontoOutRange(TypeErrors.MONTO_OUT_RANGE , "El monto está fuera de los limites del tipo de restamo")))
                //Buscamos el estado
                .then(estadosRepository.findById(solicitud.getIdEstado() != null ? solicitud.getIdEstado() : EstadoCodigos.PENDIENTE.getId()))
                .switchIfEmpty(Mono.error(new EstadoNotFound(TypeErrors.ESTADO_NOT_FOUND , "Estado no encontrado")))
                // Si  lo anterior está melo, persisto la solicitud :D salu2
                .then(solicitudRepository.saveSolicitud(solicitud));
    }




    public Mono<Boolean> validarLimitesMonto(BigDecimal min, BigDecimal max, BigDecimal monto){
        return Mono.just(monto.compareTo(min)>=0 && monto.compareTo(max)<=0);
    }


}
