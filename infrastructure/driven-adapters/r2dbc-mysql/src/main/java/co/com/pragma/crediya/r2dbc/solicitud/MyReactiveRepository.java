package co.com.pragma.crediya.r2dbc.solicitud;

import co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage;
import co.com.pragma.crediya.r2dbc.dto.SolicitudFieldsPageDto;
import co.com.pragma.crediya.r2dbc.entities.SolicitudEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MyReactiveRepository extends ReactiveCrudRepository<SolicitudEntity, Long>, ReactiveQueryByExampleExecutor<SolicitudEntity> {

    @Query("""
        SELECT
          s.monto,
          s.plazo,
          s.email,
          tp.nombre as tipo_prestamo,
          tp.tasa_interes as tasa_interes,
          e.nombre  as estado
        FROM solicitud s
        JOIN tipo_prestamo tp ON tp.id_tipo_prestamo = s.id_tipo_prestamo
        JOIN estados e         ON e.id_estado        = s.id_estado
        WHERE e.nombre IN (:estados)
          AND (:q IS NULL OR LOWER(tp.nombre) LIKE LOWER(:q))
        ORDER BY s.id_solicitud ASC
        LIMIT :size OFFSET :offset
""")
    Flux<SolicitudFieldsPageDto> solicitudPageASC(
            @Param("estados") List<String> estados,   // ej: "APROBADO" / null
            @Param("q") String q,                         // ej: "%consumo%" / "%" // para los tipos de prestamo por nombre :D
            @Param("size") int size,
            @Param("offset") long offset
    );




    @Query("""
        SELECT
          s.monto,
          s.plazo,
          s.email,
          tp.tasa_interes as tasa_interes,
          tp.nombre AS tipo_prestamo,
          e.nombre  AS estado
        FROM solicitud s
        JOIN tipo_prestamo tp ON tp.id_tipo_prestamo = s.id_tipo_prestamo
        JOIN estados e         ON e.id_estado        = s.id_estado
        WHERE e.nombre IN (:estados)
          AND (:q IS NULL OR LOWER(tp.nombre) LIKE LOWER(:q))
        ORDER BY s.id_solicitud DESC
        LIMIT :size OFFSET :offset
""")
    Flux<SolicitudFieldsPageDto> solicitudPageDESC(
            @Param("estados") List<String> estados,   // ej: "APROBADO" / null
            @Param("q") String q,                         // ej: "%consumo%" / "%" // para los tipos de prestamo por nombre :D
            @Param("size") int size,
            @Param("offset") long offset
    );

    // COUNT con los mismos filtros (importante para total y hasNext correctos)
    @Query("""
        SELECT COUNT(*)
        FROM solicitud s
        JOIN tipo_prestamo tp ON tp.id_tipo_prestamo = s.id_tipo_prestamo
        JOIN estados e         ON e.id_estado        = s.id_estado
        WHERE e.nombre IN (:estados)
          AND (:q IS NULL OR LOWER(tp.nombre) LIKE LOWER(:q))
    """)
    Mono<Long> countResumen(@Param("estados") List<String> estadose,
                            @Param("q") String q);


}
