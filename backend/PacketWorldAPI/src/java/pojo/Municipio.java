package pojo;


public class Municipio {
    private Integer idMunicipio;
    private String nombre;
    private Integer idEstado;

    public Municipio() {
    }

    public Municipio(Integer idMunicipio, String nombre, Integer idEstado) {
        this.idMunicipio = idMunicipio;
        this.nombre = nombre;
        this.idEstado = idEstado;
    }

    public Integer getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(Integer idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(Integer idEstado) {
        this.idEstado = idEstado;
    }

    
    
}
