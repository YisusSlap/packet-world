package dto;

import pojo.Colaborador;


public class RSAutenticacionColaborador {
    private Boolean error;
    private String mensaje;
    private Colaborador colaborador;
    private String token;

    public RSAutenticacionColaborador() {
    }
    
    public RSAutenticacionColaborador(Boolean error, String mensaje, Colaborador colaborador, String token) {
        this.error = error;
        this.mensaje = mensaje;
        this.colaborador = colaborador;
        this.token = token;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public void setColaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    

}
