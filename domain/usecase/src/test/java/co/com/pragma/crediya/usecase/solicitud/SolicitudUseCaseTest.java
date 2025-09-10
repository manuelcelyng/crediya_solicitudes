package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.model.estado.Estado;
import co.com.pragma.crediya.model.estado.EstadoCodigos;
import co.com.pragma.crediya.model.estado.gateways.EstadoRepository;
import co.com.pragma.crediya.model.page.SimplePage;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.model.solicitud.Email;
import co.com.pragma.crediya.model.solicitud.IdDocument;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.RestConsumerRepository;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.crediya.model.solicitud.gateways.SQSGateway;
import co.com.pragma.crediya.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.crediya.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.crediya.usecase.solicitud.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitudUseCaseTest {

    private SolicitudRepository solicitudRepository;
    private EstadoRepository estadoRepository;
    private TipoPrestamoRepository tipoPrestamoRepository;
    private RestConsumerRepository restConsumerRepository;
    private SQSGateway sqsGateway;

    private SolicitudUseCase useCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = mock(SolicitudRepository.class);
        estadoRepository = mock(EstadoRepository.class);
        tipoPrestamoRepository = mock(TipoPrestamoRepository.class);
        restConsumerRepository = mock(RestConsumerRepository.class);
        sqsGateway = mock(SQSGateway.class);
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
}
