package co.com.pragma.crediya.usecase.solicitud.exceptions;

public class UserValidationException extends  BusinessException{

    public UserValidationException(String code, String message) {
        super(code, message);

    }


}