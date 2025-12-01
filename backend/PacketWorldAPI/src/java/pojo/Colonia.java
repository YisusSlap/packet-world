package pojo;

public class Colonia {
    private Integer idColonia;
    private String nombre;
    private String codigoPostal;

    public Colonia() {
    }

    public Colonia(Integer idColonia, String nombre, String codigoPostal) {
        this.idColonia = idColonia;
        this.nombre = nombre;
        this.codigoPostal = codigoPostal;
    }

    public Integer getIdColonia() {
        return idColonia;
    }

    public void setIdColonia(Integer idColonia) {
        this.idColonia = idColonia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
}