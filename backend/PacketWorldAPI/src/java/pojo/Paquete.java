package pojo;


public class Paquete {
    private Integer idPaquete;
    private String numeroGuia;
    private String descripcion;
    private Double peso;
    private Double alto;
    private Double ancho;
    private Double profundida;

    public Paquete() {
    }

    public Paquete(Integer idPaquete, String descripcion, Double peso, Double alto, Double ancho, Double profundida, String numeroGuia) {
        this.idPaquete = idPaquete;
        this.descripcion = descripcion;
        this.peso = peso;
        this.alto = alto;
        this.ancho = ancho;
        this.profundida = profundida;
        this.numeroGuia = numeroGuia;
    }

    public Integer getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(Integer idPaquete) {
        this.idPaquete = idPaquete;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getAlto() {
        return alto;
    }

    public void setAlto(Double alto) {
        this.alto = alto;
    }

    public Double getAncho() {
        return ancho;
    }

    public void setAncho(Double ancho) {
        this.ancho = ancho;
    }

    public Double getProfundida() {
        return profundida;
    }

    public void setProfundida(Double profundida) {
        this.profundida = profundida;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }
    
    
}
