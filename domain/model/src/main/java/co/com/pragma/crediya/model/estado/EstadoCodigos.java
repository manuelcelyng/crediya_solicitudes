package co.com.pragma.crediya.model.estado;

public enum EstadoCodigos {

    PENDIENTE( 1l, "Pendiente Revision"),
    RECHAZADA( 2l, "Rechazado"),
    APROBADA( 3l ,"Aprobado por chayanne");

    private final Long id;
    private final String descripcion;


    EstadoCodigos(Long id, String descripcion){
        this.id = id;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return this.id;
    }
    public String getDescripcion() {
        return this.descripcion;
    }

}
