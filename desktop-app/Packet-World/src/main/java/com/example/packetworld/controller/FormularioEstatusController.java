package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.EstatusEnvio;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // UX Moderna
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controlador para la actualización del estatus de un envío.
 * Incluye reglas de negocio para impedir transiciones inválidas (ej. En Ruta sin Chofer).
 */
public class FormularioEstatusController {

    @FXML private Label lblGuia;
    @FXML private ComboBox<EstatusEnvio> cbEstatus;
    @FXML private TextArea txtComentario;

    private Envio envioActual; // El envío que se está modificando

    @FXML
    public void initialize() {
        // Cargar lista de estatus posibles
        cargarEstatus();

        // Limitar comentario a 200 caracteres para no saturar la BD
        txtComentario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 200) txtComentario.setText(oldVal);
        });
    }

    /**
     * Recibe el envío desde la ventana padre y pre-selecciona su estatus actual.
     */
    public void setEnvio(Envio envio) {
        this.envioActual = envio;
        if (envio != null) {
            lblGuia.setText(envio.getNumeroGuia());

            // --- PRE-SELECCIONAR ESTATUS ---
            Integer idEstatusActual = envio.getIdEstatus();

            if (idEstatusActual != null) {
                for (EstatusEnvio e : cbEstatus.getItems()) {
                    // Comparamos por ID numérico
                    if (e.getId().equals(idEstatusActual)) {
                        cbEstatus.setValue(e);
                        break;
                    }
                }
            }
        }
    }

    private void cargarEstatus() {
        cbEstatus.setItems(FXCollections.observableArrayList(ApiService.obtenerEstatus()));
    }

    @FXML
    public void guardar() {
        // 1. Validar selección
        if (cbEstatus.getValue() == null) {
            mostrarAlerta("Seleccione el nuevo estatus.");
            return;
        }

        EstatusEnvio estatusSeleccionado = cbEstatus.getValue();

        // 2. REGLA DE NEGOCIO: Validar "En Tránsito"
        // Si el usuario intenta poner "En Tránsito" o "En Ruta", debe haber Chofer.
        boolean esEnTransito = estatusSeleccionado.getNombre().equalsIgnoreCase("En Tránsito")
                || estatusSeleccionado.getNombre().equalsIgnoreCase("En Ruta");

        if (esEnTransito) {
            if (envioActual.getIdConductorAsignado() == null || envioActual.getIdConductorAsignado().isEmpty()) {
                mostrarAlerta("Operación Denegada:\nNo se puede cambiar a 'En Tránsito' sin un conductor asignado.\n\nPor favor, asigne un conductor primero.");
                return; // Detenemos el guardado
            }
        }

        // 3. Validar Comentario Obligatorio
        if (txtComentario.getText().trim().isEmpty()) {
            mostrarAlerta("Por favor agregue un comentario justificando el cambio.");
            return;
        }

        // 4. Preparar datos
        String guia = envioActual.getNumeroGuia();
        Integer estatusId = cbEstatus.getValue().getId();
        String comentario = txtComentario.getText();

        // Obtenemos quién hizo el cambio (Usuario logueado o SISTEMA por defecto)
        String idUsuario = (ApiService.usuarioLogueado != null) ? ApiService.usuarioLogueado.getNumeroPersonal() : "SISTEMA";

        // 5. Enviar a API
        Respuesta resp = ApiService.actualizarEstatus(guia, estatusId, comentario, idUsuario);

        if (resp != null && !resp.getError()) {
            // ÉXITO: Toast
            Notificacion.mostrar("Estatus Actualizado", resp.getMensaje(), Notificacion.EXITO);
            cerrar();
        } else {
            // ERROR: Alerta
            mostrarAlerta("Error: " + (resp != null ? resp.getMensaje() : "Conexión fallida"));
        }
    }

    @FXML public void cerrar() { ((Stage) lblGuia.getScene().getWindow()).close(); }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null); // Limpiamos header para que se vea más limpio
        alert.setContentText(msg);
        alert.showAndWait();
    }
}