package co.com.pragma.crediya.model.solicitud;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
public class Solicitud {
    private Long idNumber;
    private BigDecimal monto;
    private Long idTipoPrestamo;
    private Long idEstado; // SI ESTO VIENE NULL -> SE LE ASIGNA ESTADO "Pendiente de revision"
    private Integer plazo; // Segun la H7 , se hace un calculo y el valor representa numero de meses
    private Email email;
    private IdDocument documentoIdentidad; // H2


    private Solicitud(
            Long idNumber,
            Long idEstado,
            Long idTipoPrestamo,
            BigDecimal monto,
            Integer plazo,
            Email email,
            IdDocument documentoIdentidad ) {
        // Invatiantes de negocio (No dependemos de anotaciones aqui :D !!! salu2 )
        if(idEstado==null) { throw new IllegalArgumentException("El idEstado no puede ser nulo");}
        if(idTipoPrestamo==null) { throw new IllegalArgumentException("El idTipoPrestamo no puede ser nulo");}
        if(monto==null) { throw new IllegalArgumentException("El monto no puede ser nulo");}
        if(plazo==null) { throw new IllegalArgumentException("El plazo no puede ser nulo");}


        this.idNumber = idNumber;
        this.idEstado = idEstado;
        this.idTipoPrestamo = idTipoPrestamo;
        this.monto = monto;
        this.plazo = plazo;
        this.email = email;
        this.documentoIdentidad = documentoIdentidad;
    }


    public static Solicitud create(
            Long idEstado,
            Long idTipoPrestamo,
            BigDecimal monto,
            Integer plazo,
            Email email,
            IdDocument documentoIdentidad
    ){
        return new Solicitud(null, idEstado, idTipoPrestamo, monto, plazo, email, documentoIdentidad);
    }

    public Solicitud withIdNumber(Long idNumber) {
        return new Solicitud(idNumber, idEstado, idTipoPrestamo, monto, plazo, email, documentoIdentidad);
    }

    public Solicitud withIdEstado(Long idEstado) {
        return new Solicitud(idNumber, idEstado, idTipoPrestamo, monto, plazo, email, documentoIdentidad);
    }


    public Long getIdNumber() {
        return idNumber;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Long getIdEstado() {
        return idEstado;
    }

    public Long getIdTipoPrestamo() {
        return idTipoPrestamo;
    }

    public Integer getPlazo() {
        return plazo;
    }

    public Email getEmail() {
        return email;
    }

    public IdDocument getDocumentoIdentidad() {
        return documentoIdentidad;
    }
}
