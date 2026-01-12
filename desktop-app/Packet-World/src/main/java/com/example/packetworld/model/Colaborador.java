package com.example.packetworld.model;

public class Colaborador {
    private String numeroPersonal;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String curp;
    private String correoElectronico;
    private String contrasenia;
    private String rol;
    private String numeroLicencia;
    private String fotografia; // Base64 String
    private String idCodigoSucursal;
    private String idUnidadAsignada;
    private String estatus;
    private byte[] fotografiaBytes;

    public Colaborador() {}

    // Constructor completo (puedes copiar el tuyo o usar Lombok)
    public Colaborador(String numeroPersonal, String nombre, String apellidoPaterno, String curp, String correo, String rol, String idSucursal) {
        this.numeroPersonal = numeroPersonal;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.curp = curp;
        this.correoElectronico = correo;
        this.rol = rol;
        this.idCodigoSucursal = idSucursal;
    }

    // Getters y Setters (Es crucial que los nombres coincidan exactamente)


    public String getNumeroPersonal() {
        return numeroPersonal;
    }

    public void setNumeroPersonal(String numeroPersonal) {
        this.numeroPersonal = numeroPersonal;
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

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
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

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public byte[] getFotografiaBytes() {
        return fotografiaBytes;
    }

    public void setFotografiaBytes(byte[] fotografiaBytes) {
        this.fotografiaBytes = fotografiaBytes;
    }

    // Método auxiliar para mostrar nombre completo en la tabla
    @Override
    public String toString() {
        return nombre + " " + apellidoPaterno;
    }
}