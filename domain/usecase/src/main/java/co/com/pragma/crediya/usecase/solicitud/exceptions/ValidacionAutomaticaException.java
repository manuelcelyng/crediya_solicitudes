package co.com.pragma.crediya.usecase.solicitud.exceptions;


public class ValidacionAutomaticaException extends BusinessException {

    public ValidacionAutomaticaException(String code, String message) {
        super(code, message);
    }
}
