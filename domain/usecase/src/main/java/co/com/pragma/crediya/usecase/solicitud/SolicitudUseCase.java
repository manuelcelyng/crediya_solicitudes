package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.model.estado.EstadoCodigos;
import co.com.pragma.crediya.model.estado.gateways.EstadoRepository;
import co.com.pragma.crediya.model.page.SimplePage;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.model.page.usuarios.SolicitudUsersFieldsPage;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.RestConsumerRepository;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;

import co.com.pragma.crediya.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.crediya.usecase.solicitud.exceptions.*;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Collections.emptyMap;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadosRepository;
    private final TipoPrestamoRepository tiposPrestamoRepository;
    private final RestConsumerRepository restConsumerRepository;



    public Mono<Solicitud> saveSolicitud(Solicitud solicitud) {
        return tiposPrestamoRepository.findById(solicitud.getIdTipoPrestamo())
                .switchIfEmpty(Mono.error(new TipoPrestamoNotFound(TypeErrors.TIPO_PRESTAMO_NOT_FOUND , "Tipo de Prestamo no encontrado")))
                .flatMap(prestamo ->
                        validarLimitesMonto(prestamo.getMontoMinimo(), prestamo.getMontoMaximo(), solicitud.getMonto())
                )
                .flatMap(valid -> valid ? Mono.just(true) :   Mono.error(new MontoOutRange(TypeErrors.MONTO_OUT_RANGE , "El monto está fuera de los limites del tipo de restamo")))// de Boolean -> error si es false
                .then(restConsumerRepository.getValid(solicitud.getEmail().email(), solicitud.getDocumentoIdentidad().documento()))
                .switchIfEmpty(Mono.error(new UserValidationException(TypeErrors.USER_VALIDATION_ERROR,"Los campos de la solicitud no coinciden con el usuario autenticado" )))
                .then(estadosRepository.findById(solicitud.getIdEstado() != null ? solicitud.getIdEstado() : EstadoCodigos.PENDIENTE.getId()))
                .switchIfEmpty(Mono.error(new EstadoNotFound(TypeErrors.ESTADO_NOT_FOUND , "Estado no encontrado")))
                // Si  lo anterior está melo, persisto la solicitud :D salu2
                .then(solicitudRepository.saveSolicitud(solicitud));
    }




    public Mono<Boolean> validarLimitesMonto(BigDecimal min, BigDecimal max, BigDecimal monto){
        return Mono.just(monto.compareTo(min)>=0 && monto.compareTo(max)<=0);
    }

    // En el entryPoint debo hacer un mapper
    public Mono<SimplePage<SolicitudFieldsPage>> page(SimplePageRequest pageRequest) {
        return solicitudRepository.page(pageRequest)
                .flatMap(
                        simplePage ->  {
                            // primero -> los emails ome
                            List<String> emails = simplePage.getData().stream().map(SolicitudFieldsPage::getEmail).toList();
                            // segundo -> llamar al consumer y construir el map por email izi pizi
                            return emails.isEmpty() ? Mono.just(Tuples.of(simplePage, emptyMap())) // Si no encuentra nada, siga el flujo :D
                                    : restConsumerRepository.getUsers(emails) // ESTO ES UN FLUXX !!!! PENDIENTEeeee :D salu2
                                    .collectMap(SolicitudUsersFieldsPage::correoElectronico, Function.identity())
                                    .map(usersByEmail -> Tuples.of(simplePage, usersByEmail));
                        }
                ).map(
                        tuple -> {
                            if(tuple.getT2().isEmpty()) return tuple.getT1(); // Si viene vacia de arriba es porque no encontró nada :D
                            SimplePage<SolicitudFieldsPage> original = tuple.getT1();
                            Map<String, SolicitudUsersFieldsPage> usersByEmail = (Map<String, SolicitudUsersFieldsPage>) tuple.getT2();

                            original.getData().forEach(item -> {

                                SolicitudUsersFieldsPage user = usersByEmail.get(item.getEmail());
                                if(user!=null){
                                    item.setNombre(user.nombre());
                                    item.setSalarioBase(user.salarioBase());
                                }
                            });
                            return original;

                        }
                );
    }


}
