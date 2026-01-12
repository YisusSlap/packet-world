package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Paquete;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion;
import com.example.packetworld.util.Validaciones;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FormularioPaqueteController {

    @FXML private Label lblTitulo; // Para cambiar "Agregar" por "Editar"
    @FXML private TextField txtDescripcion, txtPeso, txtAlto, txtAncho, txtProf;
    @FXML private Button btnGuardar;

    // --- MODO CREACIÓN ---
    private Integer idEnvioPadre; // Solo ID (para agregar extra)

    // --- MODO EDICIÓN ---
    private Paquete paqueteEdicion;
    private Envio envioPadreCompleto;
    private boolean esEdicion = false;

    @FXML
    public void initialize() {
        // 1. Validaciones de Longitud
        Validaciones.limitarLongitud(txtDescripcion, 100);
        Validaciones.limitarLongitud(txtPeso, 10);
        Validaciones.limitarLongitud(txtAlto, 10);
        Validaciones.limitarLongitud(txtAncho, 10);
        Validaciones.limitarLongitud(txtProf, 10);

        // 2. Validaciones de Tipo
        Validaciones.soloDecimales(txtPeso);
        Validaciones.soloDecimales(txtAlto);
        Validaciones.soloDecimales(txtAncho);
        Validaciones.soloDecimales(txtProf);
    }

    /**
     * MODO 1: Agregar un paquete nuevo a un envío existente.
     */
    public void setIdEnvio(Integer idEnvio) {
        this.idEnvioPadre = idEnvio;
        this.esEdicion = false;
        if(lblTitulo != null) lblTitulo.setText("Agregar Paquete Extra");
    }

    /**
     * MODO 2: Editar un paquete existente.
     * Necesitamos el paquete (para modificarlo) y el envío padre (para guardarlo todo).
     */
    public void setPaquete(Paquete paquete, Envio envioPadre) {
        this.paqueteEdicion = paquete;
        this.envioPadreCompleto = envioPadre;
        this.esEdicion = true;

        // Ajustes visuales
        if(lblTitulo != null) lblTitulo.setText("Editar Contenido");
        if(btnGuardar != null) btnGuardar.setText("Actualizar");

        // Llenar campos
        txtDescripcion.setText(paquete.getDescripcion());
        txtPeso.setText(String.valueOf(paquete.getPesoKg()));
        txtAlto.setText(String.valueOf(paquete.getDimAltoCm()));
        txtAncho.setText(String.valueOf(paquete.getDimAnchoCm()));
        txtProf.setText(String.valueOf(paquete.getDimProfundidadCm()));
    }

    @FXML
    public void guardar() {
        try {
            // --- 1. VALIDACIÓN DE TODOS LOS CAMPOS VACÍOS ---
            if (txtDescripcion.getText().trim().isEmpty() ||
                    txtPeso.getText().trim().isEmpty() ||
                    txtAlto.getText().trim().isEmpty() ||
                    txtAncho.getText().trim().isEmpty() ||
                    txtProf.getText().trim().isEmpty()) {

                mostrarAlerta("Todos los campos (Descripción, Peso y Dimensiones) son obligatorios.");
                return;
            }

            // Parsear datos
            double peso = Double.parseDouble(txtPeso.getText());
            double alto = parseDouble(txtAlto.getText());
            double ancho = parseDouble(txtAncho.getText());
            double prof = parseDouble(txtProf.getText());

            // --- 2. VALIDACIÓN DE VALORES (No ceros ni negativos) ---
            if (peso <= 0 || alto <= 0 || ancho <= 0 || prof <= 0) {
                mostrarAlerta("Valores Inválidos, El peso y las dimensiones deben ser mayores a 0.");
                return;
            }

            Respuesta r;

            if (esEdicion) {
                // --- LÓGICA DE EDICIÓN (CORREGIDA) ---
                // 1. Actualizamos el objeto local con los datos del formulario
                paqueteEdicion.setDescripcion(txtDescripcion.getText());
                paqueteEdicion.setPesoKg(peso);
                paqueteEdicion.setDimAltoCm(alto);
                paqueteEdicion.setDimAnchoCm(ancho);
                paqueteEdicion.setDimProfundidadCm(prof);

                // 2. Llamamos al endpoint específico de PAQUETE
                // Antes llamabas a editarEnvio, ahora usamos editarPaquete
                r = ApiService.editarPaquete(paqueteEdicion);

            } else {
                // --- LÓGICA DE CREACIÓN  ---
                Paquete p = new Paquete();
                p.setIdEnvio(idEnvioPadre);
                p.setDescripcion(txtDescripcion.getText());
                p.setPesoKg(peso);
                p.setDimAltoCm(alto);
                p.setDimAnchoCm(ancho);
                p.setDimProfundidadCm(prof);

                r = ApiService.registrarPaquete(p);
            }

            // Respuesta
            if (r != null && !r.getError()) {
                Notificacion.mostrar("Éxito", esEdicion ? "Paquete actualizado." : "Paquete agregado.", Notificacion.EXITO);
                cerrar();
            } else {
                mostrarAlerta("Error: " + (r != null ? r.getMensaje() : "Conexión"));
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Verifique los valores numéricos.");
        }
    }

    @FXML public void cerrar() { ((Stage) txtDescripcion.getScene().getWindow()).close(); }

    // --- UTILIDADES ---
    private double parseDouble(String val) {
        return (val == null || val.isEmpty()) ? 0.0 : Double.parseDouble(val);
    }

    private void forzarDecimales(TextField campo) {
        campo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) campo.setText(oldValue);
        });
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setContentText(msg);
        a.show();
    }
}