package co.com.pragma.crediya.r2dbc.solicitud;

import co.com.pragma.crediya.model.page.SimplePage;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.r2dbc.dto.SolicitudFieldsPageDto;
import co.com.pragma.crediya.r2dbc.entities.SolicitudEntity;
import co.com.pragma.crediya.r2dbc.mappers.SolicitudEntityMapper;
import co.com.pragma.crediya.r2dbc.mappers.SolicitudPaginationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MyReactiveRepositoryAdapterTest {

    private MyReactiveRepository repository;
    private ObjectMapper objectMapper;
    private SolicitudEntityMapper entityMapper;
    private SolicitudPaginationMapper paginationMapper;

    private MyReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(MyReactiveRepository.class);
        objectMapper = mock(ObjectMapper.class);
        entityMapper = mock(SolicitudEntityMapper.class);
        paginationMapper = mock(SolicitudPaginationMapper.class);
        adapter = new MyReactiveRepositoryAdapter(repository, objectMapper, entityMapper, paginationMapper);
    }

    @Test
    void saveSolicitud_shouldMapAndReturnDomain() {
        Solicitud domainIn = mock(Solicitud.class);
        SolicitudEntity entityIn = new SolicitudEntity();
        SolicitudEntity entitySaved = new SolicitudEntity();
        Solicitud domainOut = mock(Solicitud.class);

        when(entityMapper.toEntity(domainIn)).thenReturn(entityIn);
        when(repository.save(entityIn)).thenReturn(Mono.just(entitySaved));
        when(entityMapper.toDomain(entitySaved)).thenReturn(domainOut);

        StepVerifier.create(adapter.saveSolicitud(domainIn))
                .expectNext(domainOut)
                .verifyComplete();

        verify(entityMapper).toEntity(domainIn);
        verify(repository).save(entityIn);
        verify(entityMapper).toDomain(entitySaved);
    }

    @Test
    void page_shouldBuildSimplePageAsc() {
        SimplePageRequest req = new SimplePageRequest();
        req.setPage(0);
        req.setSize(2);
        req.setSort("ASC");
        req.setQuery("*");
        req.setStatus(List.of("APROBADA"));

        SolicitudFieldsPageDto dto1 = SolicitudFieldsPageDto.builder()
                .monto(new BigDecimal("100"))
                .plazo(12)
                .email("a@b.com")
                .tipoPrestamo("Consumo")
                .estado("APROBADA")
                .build();

        SolicitudFieldsPage model1 = SolicitudFieldsPage.builder()
                .monto(new BigDecimal("100")).plazo(12).email("a@b.com").tipoPrestamo("Consumo").estado("APROBADA").build();

        when(repository.solicitudPageASC(anyList(), anyString(), eq(2), eq(0L)))
                .thenReturn(Flux.just(dto1));
        when(repository.countResumen(anyList(), anyString())).thenReturn(Mono.just(5L));
        when(paginationMapper.toModel(dto1)).thenReturn(model1);

        StepVerifier.create(adapter.page(req))
                .assertNext(page -> {
                    assertEquals(1, page.getData().size());
                    assertEquals(5L, page.getTotalRows());
                    assertEquals(2, page.getPageSize());
                    assertEquals(0, page.getPageNum());
                    assertEquals("id_solicitud ASC", page.getSort());
                    assertTrue(page.getHasNext()); // 1 of 5 with size 2 => hasNext
                })
                .verifyComplete();

        verify(repository).solicitudPageASC(eq(List.of("APROBADA")), eq("%"), eq(2), eq(0L));
        verify(repository).countResumen(eq(List.of("APROBADA")), eq("%"));
    }
}
