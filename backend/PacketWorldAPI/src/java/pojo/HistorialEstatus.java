package pojo;


public class HistorialEstatus {
    private Integer idHistorial;
    private Integer idEnvio;
    private Integer idEstatus;
    private String nombreEstatus;
    private String fechaCambio;
    private String comentario;
    private String numeroPersonalColaborador;

    public HistorialEstatus() {
    }

    public HistorialEstatus(Integer idHistorial, Integer idEnvio, Integer idEstatus, String nombreEstatus, String fechaCambio, String comentario, String numeroPersonalColaborador) {
        this.idHistorial = idHistorial;
        this.idEnvio = idEnvio;
        this.idEstatus = idEstatus;
        this.nombreEstatus = nombreEstatus;
        this.fechaCambio = fechaCambio;
        this.comentario = comentario;
        this.numeroPersonalColaborador = numeroPersonalColaborador;
    }

    public String getNombreEstatus() {
        return nombreEstatus;
    }

    public void setNombreEstatus(String nombreEstatus) {
        this.nombreEstatus = nombreEstatus;
    }

    public Integer getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Integer idHistorial) {
        this.idHistorial = idHistorial;
    }

    public Integer getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(Integer idEnvio) {
        this.idEnvio = idEnvio;
    }

    public Integer getIdEstatus() {
        return idEstatus;
    }

    public void setIdEstatus(Integer idEstatus) {
        this.idEstatus = idEstatus;
    }

    public String getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(String fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getNumeroPersonalColaborador() {
        return numeroPersonalColaborador;
    }

    public void setNumeroPersonalColaborador(String numeroPersonalColaborador) {
        this.numeroPersonalColaborador = numeroPersonalColaborador;
    }
        
}

