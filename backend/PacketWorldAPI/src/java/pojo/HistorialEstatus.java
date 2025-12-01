package pojo;


public class HistorialEstatus {
    private Integer idHistorial;
    private Integer idEnvio;
    private String estatus;
    private String fechaCambio;
    private String comentario;
    private String numeroPersonalColaborador;

    public HistorialEstatus() {
    }

    public HistorialEstatus(Integer idHistorial, Integer idEnvio, String estatus, String fechaCambio, String comentario, String numeroPersonalColaborador) {
        this.idHistorial = idHistorial;
        this.idEnvio = idEnvio;
        this.estatus = estatus;
        this.fechaCambio = fechaCambio;
        this.comentario = comentario;
        this.numeroPersonalColaborador = numeroPersonalColaborador;
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

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
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

