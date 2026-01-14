package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Paquete;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controlador visual para la Guía de Envío (Etiqueta).
 * No tiene lógica de negocio compleja, solo formatea datos para impresión.
 */
public class EtiquetaController {

    @FXML private Label lblGuia, lblOrigen, lblDestino, lblDireccion, lblFecha, lblPeso, lblPiezas;

    /**
     * Llena los campos de la etiqueta con la información del envío.
     * Aplica formatos de texto para que se vea profesional en papel.
     */
    public void setDatos(Envio envio) {
        if (envio == null) return;

        // 1. Datos Generales
        lblGuia.setText(envio.getNumeroGuia());

        // Si tienes el nombre de la sucursal, úsalo; si no, el código está bien.
        lblOrigen.setText(envio.getCodigoSucursalOrigen() != null ? envio.getCodigoSucursalOrigen() : "CENTRAL");

        lblDestino.setText(envio.getDestinatarioNombre() + " " + envio.getDestinatarioAp1());

        // 2. Formateo de Dirección (Validando nulos para evitar "null")
        String calle = envio.getDestinoCalle() != null ? envio.getDestinoCalle() : "";
        String num = envio.getDestinoNumero() != null ? envio.getDestinoNumero() : "";
        String col = envio.getNombreColonia() != null ? envio.getNombreColonia() : "";
        String estado = envio.getEstado() != null ? envio.getEstado() : "";
        String cp = envio.getCodigoPostal() != null ? envio.getCodigoPostal() : "";

        String dir = String.format("%s #%s\nCOL. %s, %s\nCP: %s", calle, num, col, estado, cp);
        lblDireccion.setText(dir.toUpperCase());

        // 3. Fecha con formato amigable (ej. 05-ENE-2026)
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd-MMM-yyyy", new Locale("es", "MX"));
        lblFecha.setText(LocalDate.now().format(formato).toUpperCase());

        // 4. Cálculos Matemáticos (Totales)
        int piezas = 0;
        double pesoTotal = 0;

        if (envio.getListaPaquetes() != null) {
            piezas = envio.getListaPaquetes().size();
            for (Paquete p : envio.getListaPaquetes()) {
                pesoTotal += p.getPesoKg();
            }
        }

        lblPiezas.setText(String.valueOf(piezas));

        // Formato de peso a 2 decimales (ej. 5.50 KG)
        lblPeso.setText(String.format("%.2f KG", pesoTotal));
    }
}