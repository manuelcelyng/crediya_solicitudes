package co.com.pragma.crediya.usecase.solicitud.exceptions;

public class SolicitudNotFound extends  BusinessException{

    public SolicitudNotFound(String code, String message) {
        super(code, message);
    }
}
