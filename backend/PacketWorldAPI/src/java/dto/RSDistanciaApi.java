package dto;


public class RSDistanciaApi {
    private Double distanciaKM;
    private Boolean error;
    private String mensaje;

    public RSDistanciaApi() {
    }

    public RSDistanciaApi(Double distanciaKM, Boolean error, String mensaje) {
        this.distanciaKM = distanciaKM;
        this.error = error;
        this.mensaje = mensaje;
    }

    public Double getDistanciaKM() {
        return distanciaKM;
    }

    public void setDistanciaKM(Double distanciaKM) {
        this.distanciaKM = distanciaKM;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    
}
