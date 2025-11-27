package pojo;


public class ActualizacionEnvio {
    private String numeroGuia;
    private String estatus;
    private String motivo;
    private Integer idConductor;

    public ActualizacionEnvio() {
    }

    public ActualizacionEnvio(String numeroGuia, String estatus, String motivo, Integer idConductor) {
        this.numeroGuia = numeroGuia;
        this.estatus = estatus;
        this.motivo = motivo;
        this.idConductor = idConductor;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Integer getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(Integer idConductor) {
        this.idConductor = idConductor;
    }
    
    
}
