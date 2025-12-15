package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.EstatusEnvio;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FormularioEstatusController {

    @FXML private Label lblGuia;
    @FXML private ComboBox<EstatusEnvio> cbEstatus;
    @FXML private TextArea txtComentario;

    private Envio envioActual;

    @FXML
    public void initialize() {
        cargarEstatus();
        txtComentario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 200) txtComentario.setText(oldVal);
        });
    }

    public void setEnvio(Envio envio) {
        this.envioActual = envio;
        if (envio != null) {
            lblGuia.setText(envio.getNumeroGuia());

            // --- NUEVO: PRE-SELECCIONAR ESTATUS ACTUAL ---
            Integer idEstatusActual = envio.getIdEstatus();

            if (idEstatusActual != null) {
                for (EstatusEnvio e : cbEstatus.getItems()) {
                    // Comparamos IDs numéricos
                    if (e.getId().equals(idEstatusActual)) {
                        cbEstatus.setValue(e);
                        break;
                    }
                }
            }
            // ---------------------------------------------
        }
    }

    private void cargarEstatus() {
        cbEstatus.setItems(FXCollections.observableArrayList(ApiService.obtenerEstatus()));
    }

    @FXML
    public void guardar() {
        if (cbEstatus.getValue() == null) {
            mostrarAlerta("Seleccione el nuevo estatus.");
            return;
        }

        EstatusEnvio estatusSeleccionado = cbEstatus.getValue();

        // --- NUEVA REGLA DE NEGOCIO ---
        // Verificamos si intentan poner "En Tránsito" (Ajusta el texto o ID según tu BD)
        // Suponiendo que el ID 3 es "En Tránsito" o buscando por nombre:
        boolean esEnTransito = estatusSeleccionado.getNombre().equalsIgnoreCase("En Tránsito")
                || estatusSeleccionado.getNombre().equalsIgnoreCase("En Ruta");

        if (esEnTransito) {
            // Verificamos si el envío tiene conductor asignado
            if (envioActual.getIdConductorAsignado() == null || envioActual.getIdConductorAsignado().isEmpty()) {
                mostrarAlerta("No se puede cambiar a 'En Tránsito' sin un conductor asignado.\n\nPor favor, asigne un conductor primero.");
                return; // Detenemos el proceso
            }
        }
        // -----------------------------

        if (txtComentario.getText().trim().isEmpty()) {
            mostrarAlerta("Por favor agregue un comentario justificando el cambio.");
            return;
        }

        if (txtComentario.getText().trim().isEmpty()) {
            mostrarAlerta("Por favor agregue un comentario justificando el cambio.");
            return;
        }

        String guia = envioActual.getNumeroGuia();

        // --- CORRECCIÓN: Usamos Integer directamente ---
        Integer estatusId = cbEstatus.getValue().getId();
        // ----------------------------------------------

        String comentario = txtComentario.getText();
        String idUsuario = (ApiService.usuarioLogueado != null) ? ApiService.usuarioLogueado.getNumeroPersonal() : "SISTEMA";

        Respuesta resp = ApiService.actualizarEstatus(guia, estatusId, comentario, idUsuario);

        if (resp != null && !resp.getError()) {
            mostrarInfo("Estatus Actualizado", resp.getMensaje());
            cerrar();
        } else {
            mostrarAlerta("Error: " + (resp != null ? resp.getMensaje() : "Conexión fallida"));
        }
    }

    @FXML public void cerrar() { ((Stage) lblGuia.getScene().getWindow()).close(); }
    private void mostrarAlerta(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
    private void mostrarInfo(String titulo, String msg) { new Alert(Alert.AlertType.INFORMATION, msg).show(); }
}