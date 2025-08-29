package co.com.pragma.crediya.model.solicitud;

import java.util.regex.Pattern;

public record IdDocument (
    String documento
){
    private static final Pattern SIMPLE =
            Pattern.compile("^\\d{1,10}$");

    public IdDocument {
        if(documento==null || documento.isBlank()) throw new IllegalArgumentException("El documento no puede ser nulo ni vacio");

        if(!SIMPLE.matcher(documento).matches()) throw new IllegalArgumentException("El documento ingresado tiene un formato inválido");

        documento = documento.trim();
    }
}
