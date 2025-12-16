package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Paquete;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.LocalDate;

public class EtiquetaController {

    @FXML private Label lblGuia, lblOrigen, lblDestino, lblDireccion, lblFecha, lblPeso, lblPiezas;

    public void setDatos(Envio envio) {
        lblGuia.setText(envio.getNumeroGuia());
        lblOrigen.setText(envio.getCodigoSucursalOrigen()); // O Nombre Sucursal si lo tuviéramos
        lblDestino.setText(envio.getDestinatarioNombre() + " " + envio.getDestinatarioAp1());

        String dir = String.format("%s %s\n%s, %s CP %s",
                envio.getDestinoCalle(), envio.getDestinoNumero(),
                envio.getNombreColonia(), envio.getEstado(), envio.getCodigoPostal());
        lblDireccion.setText(dir.toUpperCase());

        lblFecha.setText(LocalDate.now().toString());

        // Calcular peso total y piezas
        int piezas = 0;
        double peso = 0;
        if(envio.getListaPaquetes() != null){
            piezas = envio.getListaPaquetes().size();
            for(Paquete p : envio.getListaPaquetes()){
                peso += p.getPesoKg();
            }
        }
        lblPiezas.setText(String.valueOf(piezas));
        lblPeso.setText(peso + " KG");
    }
}