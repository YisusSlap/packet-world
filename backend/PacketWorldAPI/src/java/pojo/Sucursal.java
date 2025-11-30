package pojo;


public class Sucursal {
    private String codigoSucursal;
    private String nombreCorto;
    private String estatus;

    private String calle;
    private String numero;

    private Integer idColonia;
    private String nombreColonia;
    private String codigoPostal;
    private String ciudad;
    private String estado;
    
    public Sucursal() {
    }

    public Sucursal(String codigoSucursal, String nombreCorto, String estatus, String calle, String numero, Integer idColonia, String nombreColonia, String codigoPostal, String ciudad, String estado) {
        this.codigoSucursal = codigoSucursal;
        this.nombreCorto = nombreCorto;
        this.estatus = estatus;
        this.calle = calle;
        this.numero = numero;
        this.idColonia = idColonia;
        this.nombreColonia = nombreColonia;
        this.codigoPostal = codigoPostal;
        this.ciudad = ciudad;
        this.estado = estado;
    }

    public String getCodigoSucursal() {
        return codigoSucursal;
    }

    public void setCodigoSucursal(String codigoSucursal) {
        this.codigoSucursal = codigoSucursal;
    }

    public String getNombreCorto() {
        return nombreCorto;
    }

    public void setNombreCorto(String nombreCorto) {
        this.nombreCorto = nombreCorto;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getIdColonia() {
        return idColonia;
    }

    public void setIdColonia(Integer idColonia) {
        this.idColonia = idColonia;
    }

    public String getNombreColonia() {
        return nombreColonia;
    }

    public void setNombreColonia(String nombreColonia) {
        this.nombreColonia = nombreColonia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    
    
}
