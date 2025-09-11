package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.model.estado.Estado;
import co.com.pragma.crediya.model.estado.EstadoCodigos;
import co.com.pragma.crediya.model.estado.gateways.EstadoRepository;
import co.com.pragma.crediya.model.page.SimplePage;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.model.page.usuarios.SolicitudUsersFieldsPage;
import co.com.pragma.crediya.model.solicitud.Email;
import co.com.pragma.crediya.model.solicitud.IdDocument;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.SQSMessage;
import co.com.pragma.crediya.model.solicitud.gateways.RestConsumerRepository;
import co.com.pragma.crediya.model.solicitud.gateways.SQSCambioEstadoGateway;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;

import co.com.pragma.crediya.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.crediya.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.crediya.usecase.solicitud.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitudUseCaseTest {

    private SolicitudRepository solicitudRepository;
    private EstadoRepository estadoRepository;
    private TipoPrestamoRepository tipoPrestamoRepository;
    private RestConsumerRepository restConsumerRepository;
    private SQSCambioEstadoGateway sqsGateway;

    private SolicitudUseCase useCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = mock(SolicitudRepository.class);
        estadoRepository = mock(EstadoRepository.class);
        tipoPrestamoRepository = mock(TipoPrestamoRepository.class);
        restConsumerRepository = mock(RestConsumerRepository.class);
        sqsGateway = mock(SQSCambioEstadoGateway.class);
        when(sqsGateway.send(any())).thenReturn(Mono.just("msg-id"));
        useCase = new SolicitudUseCase(solicitudRepository, estadoRepository, tipoPrestamoRepository, restConsumerRepository, sqsGateway);
    }

    private TipoPrestamo tipoPrestamo(BigDecimal min, BigDecimal max){
        return TipoPrestamo.builder().idNumber(2L).montoMinimo(min).montoMaximo(max).build();
    }

    private Solicitud solicitud(Long idEstado, BigDecimal monto){
        return Solicitud.create(idEstado, 2L, monto, 12, new Email("user@example.com"), new IdDocument("1234567890"));
    }

    @Test
    void validarLimitesMonto_ShouldReturnTrueWhenWithinRange() {
        StepVerifier.create(useCase.validarLimitesMonto(new BigDecimal("100"), new BigDecimal("1000"), new BigDecimal("500")))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void validarLimitesMonto_ShouldReturnFalseWhenOutOfRange() {
        StepVerifier.create(useCase.validarLimitesMonto(new BigDecimal("100"), new BigDecimal("1000"), new BigDecimal("50")))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void saveSolicitud_HappyPath_WithNullEstadoShouldUsePendiente() {
        when(tipoPrestamoRepository.findById(2L)).thenReturn(Mono.just(tipoPrestamo(new BigDecimal("100"), new BigDecimal("1000"))));
        when(restConsumerRepository.getValid(anyString(), anyString())).thenReturn(Mono.just(true));
        when(estadoRepository.findById(EstadoCodigos.PENDIENTE.getId())).thenReturn(Mono.just(new Estado()));
        when(solicitudRepository.saveSolicitud(any())).thenAnswer(inv -> Mono.just((Solicitud)inv.getArgument(0)));

        Solicitud req = solicitud(null, new BigDecimal("500"));
        StepVerifier.create(useCase.saveSolicitud(req))
                .assertNext(saved -> {
                    assertNotSame(req, saved);
                    assertEquals(req.getMonto(), saved.getMonto());
                    assertEquals(req.getIdTipoPrestamo(), saved.getIdTipoPrestamo());
                    assertEquals(EstadoCodigos.PENDIENTE.getId(), saved.getIdEstado());
                })
                .verifyComplete();

        // Verifica que consultó el estado por PENDIENTE y que guardó algo
        verify(estadoRepository).findById(EstadoCodigos.PENDIENTE.getId());
        verify(solicitudRepository).saveSolicitud(any());
    }

    @Test
    void saveSolicitud_ShouldErrorWhenTipoPrestamoNotFound() {
        when(tipoPrestamoRepository.findById(2L)).thenReturn(Mono.empty());
        Solicitud req = solicitud(1L, new BigDecimal("500"));
        StepVerifier.create(useCase.saveSolicitud(req))
                .expectErrorSatisfies(ex -> assertTrue(ex instanceof TipoPrestamoNotFound))
                .verify();
    }

    @Test
    void saveSolicitud_ShouldErrorWhenMontoOutOfRange() {
        when(tipoPrestamoRepository.findById(2L)).thenReturn(Mono.just(tipoPrestamo(new BigDecimal("100"), new BigDecimal("1000"))));
        Solicitud req = solicitud(1L, new BigDecimal("50"));
        StepVerifier.create(useCase.saveSolicitud(req))
                .expectErrorSatisfies(ex -> assertTrue(ex instanceof MontoOutRange))
                .verify();
    }

    @Test
    void saveSolicitud_ShouldErrorWhenUserValidationEmpty() {
        when(tipoPrestamoRepository.findById(2L)).thenReturn(Mono.just(tipoPrestamo(new BigDecimal("100"), new BigDecimal("1000"))));
        when(restConsumerRepository.getValid(anyString(), anyString())).thenReturn(Mono.empty());
        Solicitud req = solicitud(1L, new BigDecimal("500"));
        StepVerifier.create(useCase.saveSolicitud(req))
                .expectErrorSatisfies(ex -> assertTrue(ex instanceof UserValidationException))
                .verify();
    }

    @Test
    void saveSolicitud_ShouldErrorWhenEstadoNotFound() {
        when(tipoPrestamoRepository.findById(2L)).thenReturn(Mono.just(tipoPrestamo(new BigDecimal("100"), new BigDecimal("1000"))));
        when(restConsumerRepository.getValid(anyString(), anyString())).thenReturn(Mono.just(true));
        when(estadoRepository.findById(1L)).thenReturn(Mono.empty());
        Solicitud req = solicitud(1L, new BigDecimal("500"));
        StepVerifier.create(useCase.saveSolicitud(req))
                .expectErrorSatisfies(ex -> assertTrue(ex instanceof EstadoNotFound))
                .verify();
    }

    @Test
    void page_ShouldDelegateToRepository() {
        SimplePageRequest pr = new SimplePageRequest();
        SimplePage<SolicitudFieldsPage> page = new SimplePage<>();
        when(solicitudRepository.page(pr)).thenReturn(Mono.just(page));
        StepVerifier.create(useCase.page(pr))
                .expectNext(page)
                .verifyComplete();
    }

    @Test
    void page_ShouldEnrichItemsWithUserData() {
        // Given a page with two items (only one will be enriched)
        SimplePageRequest pr = new SimplePageRequest();
        SimplePage<SolicitudFieldsPage> page = new SimplePage<>();
        SolicitudFieldsPage a = SolicitudFieldsPage.builder().email("a@a.com").build();
        SolicitudFieldsPage b = SolicitudFieldsPage.builder().email("b@b.com").build();
        page.add(a);
        page.add(b);
        when(solicitudRepository.page(pr)).thenReturn(Mono.just(page));

        SolicitudUsersFieldsPage userA = SolicitudUsersFieldsPage.builder()
                .correoElectronico("a@a.com")
                .nombre("Alice")
                .salarioBase(new BigDecimal("1234.56"))
                .build();
        when(restConsumerRepository.getUsers(List.of("a@a.com", "b@b.com")))
                .thenReturn(Flux.just(userA));

        // When
        StepVerifier.create(useCase.page(pr))
                .assertNext(result -> {
                    // Same instance returned, but enriched in-place
                    assertSame(page, result);
                    assertEquals("Alice", a.getNombre());
                    assertEquals(new BigDecimal("1234.56"), a.getSalarioBase());
                    assertNull(b.getNombre());
                    assertNull(b.getSalarioBase());
                })
                .verifyComplete();

        verify(restConsumerRepository).getUsers(List.of("a@a.com", "b@b.com"));
    }

    @Test
    void page_WithEmptyData_ShouldNotCallRestConsumer() {
        SimplePageRequest pr = new SimplePageRequest();
        SimplePage<SolicitudFieldsPage> page = new SimplePage<>();
        when(solicitudRepository.page(pr)).thenReturn(Mono.just(page));

        StepVerifier.create(useCase.page(pr))
                .expectNext(page)
                .verifyComplete();

        verifyNoInteractions(restConsumerRepository);
    }

    @Test
    void updateEstadoInSolicitud_ShouldErrorWhenEstadoNotFound() {
        when(estadoRepository.findById(3L)).thenReturn(Mono.empty());
        when(solicitudRepository.findById(10L)).thenReturn(Mono.just(solicitud(1L, new BigDecimal("500"))));

        StepVerifier.create(useCase.updateEstadoInSolicitud(3L, 10L))
                .expectErrorSatisfies(ex -> assertTrue(ex instanceof EstadoNotFound))
                .verify();

        verify(estadoRepository).findById(3L);
        verify(solicitudRepository).findById(10L);
        verifyNoInteractions(tipoPrestamoRepository, sqsGateway);
    }

    @Test
    void updateEstadoInSolicitud_ShouldErrorWhenSolicitudNotFound() {
        when(estadoRepository.findById(3L)).thenReturn(Mono.just(Estado.builder().idNumber(3L).nombre("Aprobado").build()));
        when(solicitudRepository.findById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateEstadoInSolicitud(3L, 10L))
                .expectErrorSatisfies(ex -> assertTrue(ex instanceof SolicitudNotFound))
                .verify();

        verify(estadoRepository).findById(3L);
        verify(solicitudRepository).findById(10L);
        verifyNoInteractions(tipoPrestamoRepository, sqsGateway);
    }

    @Test
    void updateEstadoInSolicitud_WhenEstadoIsSame_ShouldCompleteEmptyAndAvoidSideEffects() {
        Estado estado = Estado.builder().idNumber(2L).nombre("Pendiente").build();
        Solicitud sol = solicitud(2L, new BigDecimal("500")).withIdNumber(10L);

        when(estadoRepository.findById(2L)).thenReturn(Mono.just(estado));
        when(solicitudRepository.findById(10L)).thenReturn(Mono.just(sol));

        StepVerifier.create(useCase.updateEstadoInSolicitud(2L, 10L))
                .verifyComplete();

        verify(estadoRepository).findById(2L);
        verify(solicitudRepository).findById(10L);
        verifyNoMoreInteractions(solicitudRepository);
        verifyNoInteractions(tipoPrestamoRepository, sqsGateway);
    }

    @Test
    void updateEstadoInSolicitud_HappyPath_ShouldUpdateAndSendSQS() {
        Long idEstadoNuevo = 3L;
        Long idSolicitud = 10L;
        Estado estado = Estado.builder().idNumber(idEstadoNuevo).nombre("Aprobado").build();
        Solicitud sol = solicitud(1L, new BigDecimal("750"))
                .withIdNumber(idSolicitud);
        when(estadoRepository.findById(idEstadoNuevo)).thenReturn(Mono.just(estado));
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(sol));
        when(solicitudRepository.saveSolicitud(any(Solicitud.class)))
                .thenAnswer(inv -> Mono.just((Solicitud) inv.getArgument(0)));
        TipoPrestamo tipo = TipoPrestamo.builder().idNumber(2L).nombre("Libre Inversión").build();
        when(tipoPrestamoRepository.findById(2L)).thenReturn(Mono.just(tipo));
        when(sqsGateway.send(any())).thenReturn(Mono.just("msg-123"));

        StepVerifier.create(useCase.updateEstadoInSolicitud(idEstadoNuevo, idSolicitud))
                .assertNext(updated -> {
                    assertEquals(idEstadoNuevo, updated.getIdEstado());
                    assertEquals(new BigDecimal("750"), updated.getMonto());
                })
                .verifyComplete();

        ArgumentCaptor<SQSMessage> msgCaptor = ArgumentCaptor.forClass(SQSMessage.class);
        verify(sqsGateway).send(msgCaptor.capture());
        SQSMessage msg = msgCaptor.getValue();
        assertEquals("Aprobado", msg.estado());
        assertEquals("user@example.com", msg.correo());
        assertEquals("1234567890", msg.documento());
        assertEquals(new BigDecimal("750"), msg.cantidad());
        assertEquals("Libre Inversión", msg.tipo());
        // idSolicitud dynamic part starts with today
        String today = LocalDate.now().toString();
        assertTrue(msg.idSolicitud().startsWith("SOL-" + today + "-"));
        assertEquals("Su desembolso estará disponible en las próximas 24 horas.", msg.mensaje());
    }

    @Test
    void saveSolicitud_WithExplicitEstado_ShouldUseEstadoIdNumberOverInput() {
        // tipo de préstamo válido
        when(tipoPrestamoRepository.findById(2L))
                .thenReturn(Mono.just(tipoPrestamo(new BigDecimal("100"), new BigDecimal("1000"))));
        // validación de usuario devuelve valor (false) pero no vacío
        when(restConsumerRepository.getValid(anyString(), anyString()))
                .thenReturn(Mono.just(true));
        // viene idEstado=99 pero el repositorio devuelve un estado con idNumber=5
        when(estadoRepository.findById(99L))
                .thenReturn(Mono.just(Estado.builder().idNumber(5L).nombre("Cualquiera").build()));
        when(solicitudRepository.saveSolicitud(any()))
                .thenAnswer(inv -> Mono.just((Solicitud) inv.getArgument(0)));

        Solicitud req = solicitud(99L, new BigDecimal("500"));
        StepVerifier.create(useCase.saveSolicitud(req))
                .assertNext(saved -> {
                    assertEquals(new BigDecimal("500"), saved.getMonto());
                    // Debe usar el id del Estado encontrado (5) y no el provisto (99)
                    assertEquals(5L, saved.getIdEstado());
                })
                .verifyComplete();

        verify(estadoRepository).findById(99L);
        verify(solicitudRepository).saveSolicitud(any());
    }

    @Test
    void saveSolicitud_ShouldProceedWhenUserValidationReturnsFalse() {
        when(tipoPrestamoRepository.findById(2L))
                .thenReturn(Mono.just(tipoPrestamo(new BigDecimal("100"), new BigDecimal("1000"))));
        // Devuelve false (no vacío) -> debe continuar el flujo
        when(restConsumerRepository.getValid(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(estadoRepository.findById(EstadoCodigos.PENDIENTE.getId()))
                .thenReturn(Mono.just(Estado.builder().idNumber(7L).nombre("Pendiente").build()));
        when(solicitudRepository.saveSolicitud(any()))
                .thenAnswer(inv -> Mono.just((Solicitud) inv.getArgument(0)));

        Solicitud req = solicitud(null, new BigDecimal("800"));
        StepVerifier.create(useCase.saveSolicitud(req))
                .assertNext(saved -> assertEquals(7L, saved.getIdEstado()))
                .verifyComplete();

        verify(restConsumerRepository).getValid(anyString(), anyString());
    }

    @Test
    void page_WithUsersEmpty_ShouldReturnOriginalAndCallRest() {
        SimplePageRequest pr = new SimplePageRequest();
        SimplePage<SolicitudFieldsPage> page = new SimplePage<>();
        SolicitudFieldsPage a = SolicitudFieldsPage.builder().email("x@y.com").build();
        page.add(a);
        when(solicitudRepository.page(pr)).thenReturn(Mono.just(page));
        when(restConsumerRepository.getUsers(List.of("x@y.com")))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.page(pr))
                .assertNext(result -> {
                    assertSame(page, result);
                    assertNull(a.getNombre());
                    assertNull(a.getSalarioBase());
                })
                .verifyComplete();

        verify(restConsumerRepository).getUsers(List.of("x@y.com"));
    }

    @Test
    void updateEstadoInSolicitud_NonApproval_ShouldUseGenericMessage() {
        Long idEstadoNuevo = 2L; // Rechazado
        Long idSolicitud = 20L;
        Estado estado = Estado.builder().idNumber(idEstadoNuevo).nombre("Rechazado").build();
        Solicitud sol = solicitud(1L, new BigDecimal("650")).withIdNumber(idSolicitud);

        when(estadoRepository.findById(idEstadoNuevo)).thenReturn(Mono.just(estado));
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(sol));
        when(solicitudRepository.saveSolicitud(any(Solicitud.class)))
                .thenAnswer(inv -> Mono.just((Solicitud) inv.getArgument(0)));
        when(tipoPrestamoRepository.findById(2L))
                .thenReturn(Mono.just(TipoPrestamo.builder().idNumber(2L).nombre("Consumo").build()));
        when(sqsGateway.send(any())).thenReturn(Mono.just("msg-ok"));

        StepVerifier.create(useCase.updateEstadoInSolicitud(idEstadoNuevo, idSolicitud))
                .assertNext(updated -> assertEquals(idEstadoNuevo, updated.getIdEstado()))
                .verifyComplete();

        ArgumentCaptor<SQSMessage> msgCaptor = ArgumentCaptor.forClass(SQSMessage.class);
        verify(sqsGateway).send(msgCaptor.capture());
        assertEquals("Actualización de estado de su solicitud.", msgCaptor.getValue().mensaje());
    }

    @Test
    void updateEstadoInSolicitud_ShouldErrorWhenTipoPrestamoNotFound() {
        Long idEstadoNuevo = 3L;
        Long idSolicitud = 30L;
        Estado estado = Estado.builder().idNumber(idEstadoNuevo).nombre("Aprobado").build();
        Solicitud sol = solicitud(1L, new BigDecimal("900")).withIdNumber(idSolicitud);

        when(estadoRepository.findById(idEstadoNuevo)).thenReturn(Mono.just(estado));
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(sol));
        when(solicitudRepository.saveSolicitud(any(Solicitud.class)))
                .thenAnswer(inv -> Mono.just((Solicitud) inv.getArgument(0)));
        // Tipo de préstamo no existe
        when(tipoPrestamoRepository.findById(2L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateEstadoInSolicitud(idEstadoNuevo, idSolicitud))
                .expectErrorSatisfies(ex -> assertTrue(ex instanceof TipoPrestamoNotFound))
                .verify();

        verifyNoInteractions(sqsGateway);
    }
}
