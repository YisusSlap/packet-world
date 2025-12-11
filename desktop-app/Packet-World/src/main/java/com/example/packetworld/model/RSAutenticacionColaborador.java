package com.example.packetworld.model;

public class RSAutenticacionColaborador {
    private boolean error;
    private String mensaje;
    private Colaborador colaborador;

    // Getters y Setters
    public boolean isError() { return error; } // O getError()
    public void setError(boolean error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Colaborador getColaborador() { return colaborador; }
    public void setColaborador(Colaborador colaborador) { this.colaborador = colaborador; }
}