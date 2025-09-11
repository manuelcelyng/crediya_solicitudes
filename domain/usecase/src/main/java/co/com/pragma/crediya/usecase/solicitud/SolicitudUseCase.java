package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.model.estado.EstadoCodigos;
import co.com.pragma.crediya.model.estado.gateways.EstadoRepository;
import co.com.pragma.crediya.model.estado.Estado;
import co.com.pragma.crediya.model.solicitud.SQSMessage;
import co.com.pragma.crediya.model.solicitud.gateways.SQSCambioEstadoGateway;
import co.com.pragma.crediya.model.solicitud.gateways.SQSValidacionAutomaticaGateway;
import co.com.pragma.crediya.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.crediya.model.page.SimplePage;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.model.page.usuarios.SolicitudUsersFieldsPage;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.RestConsumerRepository;

import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;

import co.com.pragma.crediya.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.DeudaMensual;
import co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.SQSDataValidacionPrestamo;
import co.com.pragma.crediya.usecase.solicitud.exceptions.*;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.emptyMap;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadosRepository;
    private final TipoPrestamoRepository tiposPrestamoRepository;
    private final RestConsumerRepository restConsumerRepository;
    private final SQSCambioEstadoGateway sqsGateway;
    private final SQSValidacionAutomaticaGateway sqsValidacionAutomaticaGateway;



    public Mono<Solicitud> saveSolicitud(Solicitud solicitud) {
        return tiposPrestamoRepository.findById(solicitud.getIdTipoPrestamo())
                .switchIfEmpty(Mono.error(new TipoPrestamoNotFound(
                        TypeErrors.TIPO_PRESTAMO_NOT_FOUND, "Tipo de Prestamo no encontrado")))
                // Validar límites de monto y conservar 'prestamo'
                .flatMap(prestamo ->
                        validarLimitesMonto(prestamo.getMontoMinimo(), prestamo.getMontoMaximo(), solicitud.getMonto())
                                .flatMap(valid -> valid
                                        ? Mono.just(prestamo)
                                        : Mono.error(new MontoOutRange(
                                        TypeErrors.MONTO_OUT_RANGE,
                                        "El monto está fuera de los límites del tipo de préstamo, el rango es: "
                                                + prestamo.getMontoMinimo() + " - " + prestamo.getMontoMaximo()))
                                )
                )
                // Validación externa del usuario, pero NO perder 'prestamo'
                .flatMap(prestamo ->
                        restConsumerRepository.getValid(
                                        solicitud.getEmail().email(),
                                        solicitud.getDocumentoIdentidad().documento()
                                )
                                .switchIfEmpty(Mono.error(new UserValidationException(
                                        TypeErrors.USER_VALIDATION_ERROR,
                                        "Los campos de la solicitud no coinciden con el usuario autenticado")))
                                .thenReturn(prestamo) // <- conserva 'prestamo'
                )
                // Resolver estado en la solicitud y empaquetar (solicitud, prestamo)
                .flatMap(prestamo ->
                        estadosRepository.findById(
                                        (solicitud.getIdEstado() != null)
                                                ? solicitud.getIdEstado()
                                                : EstadoCodigos.PENDIENTE.getId()
                                )
                                .switchIfEmpty(Mono.error(new EstadoNotFound(
                                        TypeErrors.ESTADO_NOT_FOUND, "Estado no encontrado")))
                                .map(estado -> {
                                    Long resolvedId = Optional.ofNullable(estado.getIdNumber())
                                            .orElseGet(() -> Optional.ofNullable(solicitud.getIdEstado())
                                                    .orElse(EstadoCodigos.PENDIENTE.getId()));
                                    return solicitud.withIdEstado(resolvedId);
                                })
                                .map(solConEstado -> Tuples.of(solConEstado, prestamo)) // Tuple2<Solicitud, TipoPrestamo>
                )
                // validación automática con ambos valores y decisión final
                .flatMap((Tuple2<Solicitud, TipoPrestamo> t) ->
                        validacionAutomatica(t.getT1(), t.getT2())
                                .flatMap(ok -> ok
                                        ? solicitudRepository.saveSolicitud(t.getT1())
                                        : Mono.error(new ValidacionAutomaticaException(
                                        TypeErrors.VALIDACION_AUTOMATICA_FALLIDA,
                                        "El proceso de validación automática ha fallado, envie la solicitud nuevamente")))
                );
    }


    // SI
    public Mono<Boolean> validacionAutomatica(Solicitud solicitud, TipoPrestamo tipoPrestamo){

        //Traer el salario_base del cliente con el restConsumeR
        // Asumo que toda la validación del usuario ya se hizo antes de guardar la solicitud y de enviar esto
        return !tipoPrestamo.getValidacionAutomatica() ?  Mono.just(true) : restConsumerRepository.getUsers(List.of(solicitud.getEmail().email()))
                .collectMap(SolicitudUsersFieldsPage::correoElectronico, Function.identity())
                .flatMap( userInfo ->
                        solicitudRepository.getListDeudaMensualPrestamosAprobados(solicitud.getEmail().email())
                                .map(listaDeudasMensuales -> SQSDataValidacionPrestamo.builder()
                                        .idSolicitud(solicitud.getIdNumber())
                                        .email(solicitud.getEmail().email())
                                        .ingresoCliente(userInfo.get(solicitud.getEmail().email()).salarioBase())
                                        .deudaMensualSolicitudesAprobadas(listaDeudasMensuales)
                                        .deudaMensualSolicitudNueva(DeudaMensual.builder()
                                                .plazo(solicitud.getPlazo())
                                                .tasaInteres(tipoPrestamo.getTasaInteres())
                                                .monto(solicitud.getMonto())
                                                .build())
                                        .build())
                                .flatMap(sqsValidacionAutomaticaGateway::sendSolicitudValidacionAutomatica
                                ).thenReturn(true)

                );

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


    public Mono<Solicitud> updateEstadoInSolicitud(Long idEstado, Long idSolicitud){
        return Mono.zip(
                        estadosRepository.findById(idEstado)
                                .switchIfEmpty(Mono.error(new EstadoNotFound(TypeErrors.ESTADO_NOT_FOUND, "Estado no encontrado"))),
                        solicitudRepository.findById(idSolicitud)
                                .switchIfEmpty(Mono.error(new SolicitudNotFound(TypeErrors.SOLICITUD_NOT_FOUND, "Solicitud no encontrada")))
                )
                .flatMap(tuple -> {
                    Estado estado = tuple.getT1();
                    Solicitud solicitud = tuple.getT2();
                    if(Objects.equals(solicitud.getIdEstado(), estado.getIdNumber())){ // Me evito una operación a la BD
                        return Mono.empty(); // Retorna un empty -> Implica que no hubo cambio debido a que el estado es el mismo
                    } else {
                        solicitud.setIdEstado(estado.getIdNumber());
                        return solicitudRepository.saveSolicitud(solicitud)
                                .map(saved -> Tuples.of(saved, estado));
                    }
                })
                .flatMap(tuple2 -> tiposPrestamoRepository.findById(tuple2.getT1().getIdTipoPrestamo())
                        .switchIfEmpty(Mono.error(new TipoPrestamoNotFound(TypeErrors.TIPO_PRESTAMO_NOT_FOUND, "Tipo de Prestamo no encontrado")))
                        .map(tipoPrestamo -> Tuples.of(tuple2.getT1(), tuple2.getT2(), tipoPrestamo))
                )
                .flatMap(tuple3 -> {

                    Solicitud saved = (Solicitud) tuple3.getT1();
                    Estado estado = tuple3.getT2();
                    TipoPrestamo tipoPrestamo = tuple3.getT3();
                    String mensaje = ( estado.getNombre().toUpperCase().contains("APRO"))
                            ? "Su desembolso estará disponible en las próximas 24 horas."
                            : "Actualización de estado de su solicitud.";

                    SQSMessage messagetoSend =  SQSMessage.builder()
                            .idSolicitud("SOL-" + LocalDate.now() + "-" + saved.getIdNumber())
                            .estado(estado.getNombre())
                            .correo(saved.getEmail().email())
                            .documento(saved.getDocumentoIdentidad().documento())
                            .cantidad(saved.getMonto())
                            .tipo(tipoPrestamo.getNombre())
                            .mensaje(mensaje)
                            .build();

                    return sqsGateway.send(messagetoSend).thenReturn(saved);
                });

    }





}
