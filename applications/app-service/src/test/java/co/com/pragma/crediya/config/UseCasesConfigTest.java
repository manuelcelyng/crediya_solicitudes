package co.com.pragma.crediya.config;

import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        // Stub repository to satisfy UseCase constructor dependencies discovered by @ComponentScan
        @Bean
        public SolicitudRepository userRepository() {
            return new SolicitudRepository() {
                @Override
                public Mono<Solicitud> saveSolicitud(Solicitud solicitud) { return Mono.just(solicitud); }

                @Override
                public Mono<co.com.pragma.crediya.model.page.SimplePage<co.com.pragma.crediya.model.page.solicitud.SolicitudFieldsPage>> page(co.com.pragma.crediya.model.page.SimplePageRequest pageRequest) {
                    return Mono.just(new co.com.pragma.crediya.model.page.SimplePage<>(java.util.List.of(), 0L, 0, 0));
                }

                @Override
                public Mono<Solicitud> findById(Long id) { return Mono.empty(); }
            };
        }

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}