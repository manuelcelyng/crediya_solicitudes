package co.com.pragma.crediya.sqs.sender;

import co.com.pragma.crediya.model.solicitud.gateways.SQSValidacionAutomaticaGateway;
import co.com.pragma.crediya.model.tipoprestamo.validacionautomatica.sendtoSQS.SQSDataValidacionPrestamo;
import co.com.pragma.crediya.sqs.sender.config.SQSValidacionAutomaticaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSValidacionAutomatica implements SQSValidacionAutomaticaGateway {

    private final SQSValidacionAutomaticaProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;


    @Override
    public Mono<String> sendSolicitudValidacionAutomatica(SQSDataValidacionPrestamo message) {
        return Mono.fromCallable(() -> toJson(message))
                .flatMap(json -> Mono.fromFuture(client.sendMessage(
                        SendMessageRequest.builder()
                                .queueUrl(properties.queueUrl())
                                .messageBody(json)
                                .build()))
                )
                .map(SendMessageResponse::messageId);
    }

    private String toJson(Object m) throws Exception {
        return objectMapper.writeValueAsString(m);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }


}
