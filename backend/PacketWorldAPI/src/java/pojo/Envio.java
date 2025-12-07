package pojo;

import java.util.List;



public class Envio {
    private Integer idEnvio;
    private String numeroGuia;
    private Integer idCliente;
    private String nombreCliente;
    private String destinatarioNombre;
    private String destinatarioAp1;
    private String destinatarioAp2;
    
    private String codigoSucursalOrigen;
    private String idConductorAsignado;    
    
    //Direccion Destino
    private String destinoCalle;
    private String destinoNumero;
    private Integer idColoniaDestino;
    private String nombreColonia;
    private String codigoPostal;
    private String ciudad;
    private String estado;
    
    private Integer idEstatus;
    private String estatusActual;
    private Double costoTotal;
    
    private List<Paquete> listaPaquetes;
    private List<HistorialEstatus> historial;
    
    private String numeroPersonalUsuario;

    public Envio() {
    }

    public Envio(Integer idEnvio, String numeroGuia, Integer idCliente, String nombreCliente, String destinatarioNombre, String destinatarioAp1, String destinatarioAp2, String codigoSucursalOrigen, String idConductorAsignado, String destinoCalle, String destinoNumero, Integer idColoniaDestino, String nombreColonia, String codigoPostal, String ciudad, String estado, Integer idEstatus, String estatusActual, Double costoTotal, List<Paquete> listaPaquetes, List<HistorialEstatus> historial, String numeroPersonalUsuario) {
        this.idEnvio = idEnvio;
        this.numeroGuia = numeroGuia;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.destinatarioNombre = destinatarioNombre;
        this.destinatarioAp1 = destinatarioAp1;
        this.destinatarioAp2 = destinatarioAp2;
        this.codigoSucursalOrigen = codigoSucursalOrigen;
        this.idConductorAsignado = idConductorAsignado;
        this.destinoCalle = destinoCalle;
        this.destinoNumero = destinoNumero;
        this.idColoniaDestino = idColoniaDestino;
        this.nombreColonia = nombreColonia;
        this.codigoPostal = codigoPostal;
        this.ciudad = ciudad;
        this.estado = estado;
        this.idEstatus = idEstatus;
        this.estatusActual = estatusActual;
        this.costoTotal = costoTotal;
        this.listaPaquetes = listaPaquetes;
        this.historial = historial;
        this.numeroPersonalUsuario = numeroPersonalUsuario;
    }

    public Integer getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(Integer idEnvio) {
        this.idEnvio = idEnvio;
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

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDestinatarioNombre() {
        return destinatarioNombre;
    }

    public void setDestinatarioNombre(String destinatarioNombre) {
        this.destinatarioNombre = destinatarioNombre;
    }

    public String getDestinatarioAp1() {
        return destinatarioAp1;
    }

    public void setDestinatarioAp1(String destinatarioAp1) {
        this.destinatarioAp1 = destinatarioAp1;
    }

    public String getDestinatarioAp2() {
        return destinatarioAp2;
    }

    public void setDestinatarioAp2(String destinatarioAp2) {
        this.destinatarioAp2 = destinatarioAp2;
    }

    public String getCodigoSucursalOrigen() {
        return codigoSucursalOrigen;
    }

    public void setCodigoSucursalOrigen(String codigoSucursalOrigen) {
        this.codigoSucursalOrigen = codigoSucursalOrigen;
    }

    public String getIdConductorAsignado() {
        return idConductorAsignado;
    }

    public void setIdConductorAsignado(String idConductorAsignado) {
        this.idConductorAsignado = idConductorAsignado;
    }

    public String getDestinoCalle() {
        return destinoCalle;
    }

    public void setDestinoCalle(String destinoCalle) {
        this.destinoCalle = destinoCalle;
    }

    public String getDestinoNumero() {
        return destinoNumero;
    }

    public void setDestinoNumero(String destinoNumero) {
        this.destinoNumero = destinoNumero;
    }

    public Integer getIdColoniaDestino() {
        return idColoniaDestino;
    }

    public void setIdColoniaDestino(Integer idColoniaDestino) {
        this.idColoniaDestino = idColoniaDestino;
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

    public String getNumeroPersonalUsuario() {
        return numeroPersonalUsuario;
    }

    public void setNumeroPersonalUsuario(String numeroPersonalUsuario) {
        this.numeroPersonalUsuario = numeroPersonalUsuario;
    }

    public Integer getIdEstatus() {
        return idEstatus;
    }

    public void setIdEstatus(Integer idEstatus) {
        this.idEstatus = idEstatus;
    }
    
    
        
}
