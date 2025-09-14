package co.com.pragma.crediya.api.docs;

import co.com.pragma.crediya.api.Handler;
import co.com.pragma.crediya.api.dto.CreateSolicitudDTO;
import co.com.pragma.crediya.api.dto.ResponseSolicitudDTO;
import co.com.pragma.crediya.api.dto.UpdateEstadoInSolicidudDTO;
import co.com.pragma.crediya.model.page.SimplePage;
import co.com.pragma.crediya.model.page.SimplePageRequest;
import co.com.pragma.crediya.shared.errors.ErrorResponse;
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
            // Crear solicitud
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "listenSaveSolicitud",
                    operation = @Operation(
                            operationId = "createSolicitud",
                            summary = "Crear Solicitud",
                            description = "Crea una nueva solicitud",
                            requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateSolicitudDTO.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Solicitud creada",
                                            content = @Content(schema = @Schema(implementation = ResponseSolicitudDTO.class))),
                                    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "401", description = "No autenticado o token inválido",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Solicitud ya existe",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            ),
            // Listar solicitudes (GET)
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listenGetSolicitudes",
                    operation = @Operation(
                            operationId = "listSolicitudesGet",
                            summary = "Listar Solicitudes",
                            description = "Lista solicitudes paginadas. Si no se envía body se usan valores por defecto.",
                            requestBody = @RequestBody(required = false, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SimplePageRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Página de solicitudes",
                                            content = @Content(schema = @Schema(implementation = SimplePage.class))),
                                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            ),
            // Listar solicitudes (POST /search)
            @RouterOperation(
                    path = "/api/v1/solicitud/search",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "listenGetSolicitudes",
                    operation = @Operation(
                            operationId = "listSolicitudesSearch",
                            summary = "Buscar/Listar Solicitudes",
                            description = "Busca y lista solicitudes paginadas según los parámetros enviados.",
                            requestBody = @RequestBody(required = false, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SimplePageRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Página de solicitudes",
                                            content = @Content(schema = @Schema(implementation = SimplePage.class))),
                                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            ),
            // Actualizar estado de una solicitud
            @RouterOperation(
                    path = "/api/v1/solicitud/estado",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.PATCH,
                    beanClass = Handler.class,
                    beanMethod = "listenUpdateStateSolicitud",
                    operation = @Operation(
                            operationId = "updateEstadoSolicitud",
                            summary = "Actualizar estado de la Solicitud",
                            description = "Actualiza el estado de una solicitud existente. Si el estado enviado es el mismo, responde 204 (sin contenido).",
                            requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateEstadoInSolicidudDTO.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Solicitud actualizada",
                                            content = @Content(schema = @Schema(implementation = ResponseSolicitudDTO.class))),
                                    @ApiResponse(responseCode = "204", description = "Sin cambio: el estado enviado es el mismo"),
                                    @ApiResponse(responseCode = "400", description = "Solicitud o estado inválido",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            )
    })
    RouterFunction<ServerResponse> routerFunction(Handler handler);

}
