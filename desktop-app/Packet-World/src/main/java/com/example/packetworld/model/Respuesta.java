package com.example.packetworld.model;

public class Respuesta {
    private Boolean error;
    private String mensaje;
    private Colaborador colaborador; // A veces devuelves el objeto afectado

    public Respuesta() {}

    public Boolean getError() { return error; }
    public void setError(Boolean error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Colaborador getColaborador() { return colaborador; }
}