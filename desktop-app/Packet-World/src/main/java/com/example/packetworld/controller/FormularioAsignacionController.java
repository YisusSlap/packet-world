package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // Importante para la UX
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;

/**
 * Controlador para la ventana modal de Asignación de Conductores.
 * Permite vincular un Envío existente con un Conductor disponible.
 */
public class FormularioAsignacionController {

    @FXML private Label lblGuia;
    @FXML private ComboBox<Colaborador> cbConductores;

    private Envio envioActual; // El envío que estamos editando

    @FXML
    public void initialize() {
        // Al abrir, cargamos la lista de choferes disponibles
        cargarConductores();
    }

    /**
     * Recibe el envío seleccionado desde la pantalla anterior.
     * Muestra el número de guía y pre-selecciona el conductor actual si ya tiene uno.
     */
    public void setEnvio(Envio envio) {
        this.envioActual = envio;
        if (envio != null) {
            lblGuia.setText(envio.getNumeroGuia());

            // --- LÓGICA DE PRE-SELECCIÓN ---
            // Si el envío ya tiene conductor, buscamos su ID en el combo para dejarlo marcado.
            String idConductorActual = envio.getIdConductorAsignado();

            if (idConductorActual != null && !idConductorActual.isEmpty()) {
                for (Colaborador c : cbConductores.getItems()) {
                    // Comparamos por Número de Personal (ID único)
                    if (c.getNumeroPersonal().equals(idConductorActual)) {
                        cbConductores.setValue(c);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Carga solo los colaboradores con rol "Conductor" desde la API.
     */
    private void cargarConductores() {
        List<Colaborador> lista = ApiService.buscarColaboradores(null, "Conductor", null);
        cbConductores.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML
    public void guardar() {
        // Validación simple
        if (cbConductores.getValue() == null) {
            mostrarAlerta("Atención", "Debe seleccionar un conductor de la lista.");
            return;
        }

        String guia = envioActual.getNumeroGuia();
        String numPersonalConductor = cbConductores.getValue().getNumeroPersonal();

        // Enviar petición a la API
        Respuesta resp = ApiService.asignarConductor(guia, numPersonalConductor);

        if (resp != null && !resp.getError()) {
            // ÉXITO: Usamos Toast (No invasivo)
            Notificacion.mostrar("Asignación Exitosa", "Conductor asignado al envío.", Notificacion.EXITO);
            cerrar();
        } else {
            // ERROR: Usamos Alerta (El usuario debe saber qué falló)
            mostrarAlerta("Error", "No se pudo asignar: " + (resp != null ? resp.getMensaje() : "Error de conexión"));
        }
    }

    @FXML public void cerrar() {
        Stage stage = (Stage) lblGuia.getScene().getWindow();
        stage.close();
    }

    // Método auxiliar para alertas de error/warning
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}