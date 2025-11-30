package pojo;


public class Unidad {
    private String vin;
    private String marca;
    private String modelo;
    private Integer anio;
    private String tipoUnidad;
    private String nii;
    private String estatus;
    private String motivoBaja;

    public Unidad() {
    }

    public Unidad(String vin, String marca, String modelo, Integer anio, String tipoUnidad, String nii, String estatus, String motivoBaja) {
        this.vin = vin;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.tipoUnidad = tipoUnidad;
        this.nii = nii;
        this.estatus = estatus;
        this.motivoBaja = motivoBaja;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getTipoUnidad() {
        return tipoUnidad;
    }

    public void setTipoUnidad(String tipoUnidad) {
        this.tipoUnidad = tipoUnidad;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getMotivoBaja() {
        return motivoBaja;
    }

    public void setMotivoBaja(String motivoBaja) {
        this.motivoBaja = motivoBaja;
    }
    
    

}
