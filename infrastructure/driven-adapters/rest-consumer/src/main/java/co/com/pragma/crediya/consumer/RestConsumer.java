package co.com.pragma.crediya.consumer;

import co.com.pragma.crediya.model.page.usuarios.SolicitudUsersFieldsPage;
import co.com.pragma.crediya.model.solicitud.gateways.RestConsumerRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.zip.ZipEntry;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestConsumer implements RestConsumerRepository {
    private final WebClient client;
    private final WebClient webClient;


    // these methods are an example that illustrates the implementation of WebClient.
    // You should use the methods that you implement from the Gateway from the domain.



    @CircuitBreaker(name = "testGet" /*, fallbackMethod = "testGetOk"*/)
    public Mono<UserExistsResponse> testGet() {
        return client
                .get()
                .retrieve()
                .bodyToMono(UserExistsResponse.class);
    }


// Possible fallback method
//    public Mono<String> testGetOk(Exception ignored) {
//        return client
//                .get() // TODO: change for another endpoint or destination
//                .retrieve()
//                .bodyToMono(String.class);
//    }
            /*
    @CircuitBreaker(name = "testPost")
    public Mono<ObjectResponse> testPost() {
        ObjectRequest request = ObjectRequest.builder()
            .email("manuel.com")
                .documentoIdentidad("1003212312")
            .build();
        return client
                .post()
                .body(Mono.just(request), ObjectRequest.class)
                .retrieve()
                .bodyToMono(ObjectResponse.class);
    }
    */

    @Override
    public Mono<Boolean> getValid(String email, String documentoIdentidad) {
        return bearer().flatMap(tok->
                client.post()
                        .uri("/api/v1/usuarios/validar")
                        .headers(h -> h.setBearerAuth(tok))
                        .bodyValue(new UserExistsRequest( email, documentoIdentidad))
                        .retrieve()
                        .bodyToMono(UserExistsResponse.class)
                        .map(UserExistsResponse::isValid)
                );
    }

    @Override
    public Flux<SolicitudUsersFieldsPage> getUsers(List<String> emails) {

        return bearer().flatMapMany(tok ->
                 client.post()
                         .uri("/api/v1/usuarios/solicitud")
                        .headers(h -> h.setBearerAuth(tok))
                        .bodyValue(new UsersByEmailsRequest(emails))
                        .retrieve()
                        .bodyToMono(UsersByEmailsResponse.class)
                        .doOnNext(r -> log.warn("Request emails: " + emails))
                        .doOnNext(r -> log.warn("Response: " + r))
                        .doOnError(e -> log.error("Error: " + e.getMessage()))
                        .flatMapMany(r -> Flux.fromIterable(r.getResults()))
        );
    }

    private Mono<String> bearer(){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(a -> a instanceof JwtAuthenticationToken)
                .map(a -> ((JwtAuthenticationToken) a).getToken().getTokenValue());
    }

}
