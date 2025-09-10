package co.com.pragma.crediya.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitud"), handler::listenSaveSolicitud)
                .andRoute(GET("/api/v1/solicitud"), handler::listenGetSolicitudes)
                .andRoute(POST("/api/v1/solicitud/search"), handler::listenGetSolicitudes)
                .andRoute(PATCH("/api/v1/solicitud/estado"), handler::listenUpdateStateSolicitud);
    }
}