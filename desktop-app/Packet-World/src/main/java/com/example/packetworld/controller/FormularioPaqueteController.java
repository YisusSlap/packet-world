package com.example.packetworld.controller;

import com.example.packetworld.model.Paquete;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Validaciones;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FormularioPaqueteController {

    @FXML private TextField txtDescripcion, txtPeso, txtAlto, txtAncho, txtProf;

    private Integer idEnvioPadre; // Aquí guardamos a qué envío pertenece

    @FXML
    public void initialize() {
        // Descripción: Texto normal limitado
        Validaciones.limitarLongitud(txtDescripcion, 100);

        // Peso y Dimensiones: SOLO DECIMALES (ej. 10.5) y longitud razonable
        Validaciones.soloDecimales(txtPeso);
        Validaciones.limitarLongitud(txtPeso, 10);

        Validaciones.soloDecimales(txtAlto);
        Validaciones.limitarLongitud(txtAlto, 10);

        Validaciones.soloDecimales(txtAncho);
        Validaciones.limitarLongitud(txtAncho, 10);

        Validaciones.soloDecimales(txtProf);
        Validaciones.limitarLongitud(txtProf, 10);
    }


    public void setIdEnvio(Integer idEnvio) {
        this.idEnvioPadre = idEnvio;
    }

    @FXML
    public void guardar() {
        try {
            Paquete p = new Paquete();
            p.setIdEnvio(idEnvioPadre); // ¡VITAL!
            p.setDescripcion(txtDescripcion.getText());
            p.setPesoKg(Double.parseDouble(txtPeso.getText()));
            p.setDimAltoCm(Double.parseDouble(txtAlto.getText()));
            p.setDimAnchoCm(Double.parseDouble(txtAncho.getText()));
            p.setDimProfundidadCm(Double.parseDouble(txtProf.getText()));

            if(p.getDescripcion().isEmpty()) {
                mostrarAlerta("Descripción requerida."); return;
            }

            Respuesta r = ApiService.registrarPaquete(p);

            if (r != null && !r.getError()) {
                cerrar();
            } else {
                mostrarAlerta("Error: " + (r != null ? r.getMensaje() : "Conexión"));
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Los valores numéricos no son válidos.");
        }
    }

    @FXML public void cerrar() { ((Stage) txtDescripcion.getScene().getWindow()).close(); }

    private void mostrarAlerta(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
}