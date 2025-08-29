package co.com.pragma.crediya.api.docs;

import co.com.pragma.crediya.api.Handler;
import co.com.pragma.crediya.api.dto.CreateSolicitudDTO;
import co.com.pragma.crediya.api.dto.ResponseSolicitudDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

public interface UserControllerDocs {

    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "listenSaveSolicitud",
                    operation = @Operation(
                            operationId = "createSolicitud",
                            summary = "Crear Solicitud",
                            description = "Crea un nuevo usuario",
                            requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CreateSolicitudDTO.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Solicitud Creada",
                                            content = @Content(schema = @Schema(implementation = ResponseSolicitudDTO.class))),
                                    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                                    @ApiResponse(responseCode = "409", description = "Solicitud ya existe")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler);

}
