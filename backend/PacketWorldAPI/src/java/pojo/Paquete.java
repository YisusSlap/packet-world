package pojo;


public class Paquete {
    private Integer idPaquete;
    private Integer idEnvio;
    private String descripcion;
    
    private Double pesoKg;
    private Double dimAltoCm;
    private Double dimAnchoCm;
    private Double dimProfundidadCm;

    public Paquete() {
    }

    public Paquete(Integer idPaquete, Integer idEnvio, String descripcion, Double pesoKg, Double dimAltoCm, Double dimAnchoCm, Double dimProfundidadCm) {
        this.idPaquete = idPaquete;
        this.idEnvio = idEnvio;
        this.descripcion = descripcion;
        this.pesoKg = pesoKg;
        this.dimAltoCm = dimAltoCm;
        this.dimAnchoCm = dimAnchoCm;
        this.dimProfundidadCm = dimProfundidadCm;
    }

    public Integer getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(Integer idPaquete) {
        this.idPaquete = idPaquete;
    }

    public Integer getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(Integer idEnvio) {
        this.idEnvio = idEnvio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Double getDimAltoCm() {
        return dimAltoCm;
    }

    public void setDimAltoCm(Double dimAltoCm) {
        this.dimAltoCm = dimAltoCm;
    }

    public Double getDimAnchoCm() {
        return dimAnchoCm;
    }

    public void setDimAnchoCm(Double dimAnchoCm) {
        this.dimAnchoCm = dimAnchoCm;
    }

    public Double getDimProfundidadCm() {
        return dimProfundidadCm;
    }

    public void setDimProfundidadCm(Double dimProfundidadCm) {
        this.dimProfundidadCm = dimProfundidadCm;
    }
    
    
    
}
