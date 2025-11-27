package pojo;


public class HistorialEstatus {
    private Integer idHistorial;
    private String numeroGuia;
    private String estatus;
    private String fechaCambio;
    private String mensaje;
    private Integer idColaborador;

    public HistorialEstatus() {
    }

    public HistorialEstatus(Integer idHistorial, String numeroGuia, String estatus, String fechaCambio, String mensaje, Integer idColaborador) {
        this.idHistorial = idHistorial;
        this.numeroGuia = numeroGuia;
        this.estatus = estatus;
        this.fechaCambio = fechaCambio;
        this.mensaje = mensaje;
        this.idColaborador = idColaborador;
    }

    public Integer getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Integer idHistorial) {
        this.idHistorial = idHistorial;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Integer getIdColaborador() {
        return idColaborador;
    }

    public void setIdColaborador(Integer idColaborador) {
        this.idColaborador = idColaborador;
    }
    
        
}

