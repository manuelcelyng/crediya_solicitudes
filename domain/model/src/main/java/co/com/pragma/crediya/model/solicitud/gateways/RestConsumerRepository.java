package co.com.pragma.crediya.model.solicitud.gateways;

import co.com.pragma.crediya.model.page.usuarios.SolicitudUsersFieldsPage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RestConsumerRepository {

    Mono<Boolean> getValid(String email, String documentoIdentidad);

    Flux<SolicitudUsersFieldsPage> getUsers(List<String> emails);


}
