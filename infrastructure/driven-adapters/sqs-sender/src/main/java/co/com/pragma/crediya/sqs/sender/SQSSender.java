package co.com.pragma.crediya.sqs.sender;

import co.com.pragma.crediya.model.solicitud.SQSMessage;
import co.com.pragma.crediya.model.solicitud.gateways.SQSGateway;
import co.com.pragma.crediya.sqs.sender.config.SQSSenderProperties;
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
public class SQSSender implements SQSGateway {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    public Mono<String> send(SQSMessage message) { // acepta POJO o String
        return Mono.fromCallable(() -> toJson(message))
                .flatMap(json -> Mono.fromFuture(client.sendMessage(
                        SendMessageRequest.builder()
                                .queueUrl(properties.queueUrl())
                                .messageBody(json)
                                .build()))
                )
                .map(SendMessageResponse::messageId);
    }

    private String toJson(SQSMessage m) throws Exception {
        return objectMapper.writeValueAsString(m);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }
}
