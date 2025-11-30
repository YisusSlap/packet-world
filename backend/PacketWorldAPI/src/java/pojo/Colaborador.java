package pojo;


public class Colaborador {
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String curp;
    private String correo;
    private String numeroPersonal;
    private String contrasenia;
    private String rol;
    private String numeroLicencia;
    private String fotografia;
    private String idCodigoSucursal;
    private String idUnidadAsignada; 

    public Colaborador() {
    }

    public Colaborador(String nombre, String apellidoPaterno, String apellidoMaterno, String curp, String correo, String numeroPersonal, String contrasenia, String rol, String numeroLicencia, String fotografia, String idCodigoSucursal, String idUnidadAsignada) {
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.curp = curp;
        this.correo = correo;
        this.numeroPersonal = numeroPersonal;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.numeroLicencia = numeroLicencia;
        this.fotografia = fotografia;
        this.idCodigoSucursal = idCodigoSucursal;
        this.idUnidadAsignada = idUnidadAsignada;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNumeroPersonal() {
        return numeroPersonal;
    }

    public void setNumeroPersonal(String numeroPersonal) {
        this.numeroPersonal = numeroPersonal;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public void setNumeroLicencia(String numeroLicencia) {
        this.numeroLicencia = numeroLicencia;
    }

    public String getFotografia() {
        return fotografia;
    }

    public void setFotografia(String fotografia) {
        this.fotografia = fotografia;
    }

    public String getIdCodigoSucursal() {
        return idCodigoSucursal;
    }

    public void setIdCodigoSucursal(String idCodigoSucursal) {
        this.idCodigoSucursal = idCodigoSucursal;
    }

    public String getIdUnidadAsignada() {
        return idUnidadAsignada;
    }

    public void setIdUnidadAsignada(String idUnidadAsignada) {
        this.idUnidadAsignada = idUnidadAsignada;
    }
    
    
}
