package co.com.pragma.crediya.sqs.listener;

import co.com.pragma.crediya.sqs.listener.dto.ResultadoValidacionDTO;
import co.com.pragma.crediya.sqs.listener.mappers.SqsResponseMapper;
import co.com.pragma.crediya.usecase.solicitud.SolicitudUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    // private final MyUseCase myUseCase;
    private final ObjectMapper objectMapper;
    private final SolicitudUseCase myUseCase;
    private final SqsResponseMapper sqsResponseMapper;

    @Override
    public Mono<Void> apply(Message message) {
        ResultadoValidacionDTO resultadoValidacionDTO = null;
        try {
            resultadoValidacionDTO = objectMapper.readValue(message.body(), ResultadoValidacionDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return myUseCase.updateEstadoValidacionAutomatica(sqsResponseMapper.toModel(resultadoValidacionDTO));
    }
}
