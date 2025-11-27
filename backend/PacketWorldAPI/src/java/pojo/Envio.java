package pojo;

import java.util.List;



public class Envio {
    private String numeroGuia;
    private Integer idCliente;
    private Integer idSucursalOrigen;
    private Integer idSucursalDestino;
    private Integer idConductor;
    
    //Direccion Destino
    private String nombreDestinatario;
    private String calleDestino;
    private String coloniaDestino;
    private String codigoPostalDestino;
    private String ciudadDestino;
    private String estadoDestino;
    
    private String estatusActual;
    private Double costoTotal;
    
    private List<Paquete> listaPaquetes;
    private List<HistorialEstatus> historial;

    public Envio() {
    }

    public Envio(String numeroGuia, Integer idCliente, Integer idSucursalOrigen, Integer idSucursalDestino, Integer idConductor, String nombreDestinatario, String calleDestino, String coloniaDestino, String codigoPostalDestino, String ciudadDestino, String estadoDestino, String estatusActual, Double costoTotal, List<Paquete> listaPaquetes, List<HistorialEstatus> historial) {
        this.numeroGuia = numeroGuia;
        this.idCliente = idCliente;
        this.idSucursalOrigen = idSucursalOrigen;
        this.idSucursalDestino = idSucursalDestino;
        this.idConductor = idConductor;
        this.nombreDestinatario = nombreDestinatario;
        this.calleDestino = calleDestino;
        this.coloniaDestino = coloniaDestino;
        this.codigoPostalDestino = codigoPostalDestino;
        this.ciudadDestino = ciudadDestino;
        this.estadoDestino = estadoDestino;
        this.estatusActual = estatusActual;
        this.costoTotal = costoTotal;
        this.listaPaquetes = listaPaquetes;
        this.historial = historial;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdSucursalOrigen() {
        return idSucursalOrigen;
    }

    public void setIdSucursalOrigen(Integer idSucursalOrigen) {
        this.idSucursalOrigen = idSucursalOrigen;
    }

    public Integer getIdSucursalDestino() {
        return idSucursalDestino;
    }

    public void setIdSucursalDestino(Integer idSucursalDestino) {
        this.idSucursalDestino = idSucursalDestino;
    }

    public Integer getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(Integer idConductor) {
        this.idConductor = idConductor;
    }

    public String getNombreDestinatario() {
        return nombreDestinatario;
    }

    public void setNombreDestinatario(String nombreDestinatario) {
        this.nombreDestinatario = nombreDestinatario;
    }

    public String getCalleDestino() {
        return calleDestino;
    }

    public void setCalleDestino(String calleDestino) {
        this.calleDestino = calleDestino;
    }

    public String getColoniaDestino() {
        return coloniaDestino;
    }

    public void setColoniaDestino(String coloniaDestino) {
        this.coloniaDestino = coloniaDestino;
    }

    public String getCodigoPostalDestino() {
        return codigoPostalDestino;
    }

    public void setCodigoPostalDestino(String codigoPostalDestino) {
        this.codigoPostalDestino = codigoPostalDestino;
    }

    public String getCiudadDestino() {
        return ciudadDestino;
    }

    public void setCiudadDestino(String ciudadDestino) {
        this.ciudadDestino = ciudadDestino;
    }

    public String getEstadoDestino() {
        return estadoDestino;
    }

    public void setEstadoDestino(String estadoDestino) {
        this.estadoDestino = estadoDestino;
    }

    public String getEstatusActual() {
        return estatusActual;
    }

    public void setEstatusActual(String estatusActual) {
        this.estatusActual = estatusActual;
    }

    public Double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(Double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public List<Paquete> getListaPaquetes() {
        return listaPaquetes;
    }

    public void setListaPaquetes(List<Paquete> listaPaquetes) {
        this.listaPaquetes = listaPaquetes;
    }

    public List<HistorialEstatus> getHistorial() {
        return historial;
    }

    public void setHistorial(List<HistorialEstatus> historial) {
        this.historial = historial;
    }

    
    
}
