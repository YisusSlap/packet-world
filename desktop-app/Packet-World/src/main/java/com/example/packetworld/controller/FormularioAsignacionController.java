package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;

public class FormularioAsignacionController {

    @FXML private Label lblGuia;
    @FXML private ComboBox<Colaborador> cbConductores;

    private Envio envioActual;

    @FXML
    public void initialize() {
        cargarConductores();
    }

    public void setEnvio(Envio envio) {
        this.envioActual = envio;
        if (envio != null) {
            lblGuia.setText(envio.getNumeroGuia());

            // --- NUEVO: PRE-SELECCIONAR CONDUCTOR ACTUAL ---
            String idConductorActual = envio.getIdConductorAsignado();

            if (idConductorActual != null && !idConductorActual.isEmpty()) {
                // Recorremos los items del ComboBox
                for (Colaborador c : cbConductores.getItems()) {
                    // Comparamos el ID del objeto con el del envío
                    if (c.getNumeroPersonal().equals(idConductorActual)) {
                        cbConductores.setValue(c);
                        break; // Ya lo encontramos, dejamos de buscar
                    }
                }
            }
            // -----------------------------------------------
        }
    }

    private void cargarConductores() {
        // Usamos el servicio de búsqueda filtrando por Rol = "Conductor"
        // (nombre=null, rol="Conductor", sucursal=null)
        List<Colaborador> lista = ApiService.buscarColaboradores(null, "Conductor", null);
        cbConductores.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML
    public void guardar() {
        if (cbConductores.getValue() == null) {
            mostrarAlerta("Debe seleccionar un conductor.");
            return;
        }

        String guia = envioActual.getNumeroGuia();
        // Obtenemos el ID (Numero Personal) del conductor seleccionado
        String numPersonalConductor = cbConductores.getValue().getNumeroPersonal();

        // Llamamos a la API
        Respuesta resp = ApiService.asignarConductor(guia, numPersonalConductor);

        if (resp != null && !resp.getError()) {
            mostrarInfo("Asignación Exitosa", resp.getMensaje());
            cerrar();
        } else {
            mostrarAlerta("Error: " + (resp != null ? resp.getMensaje() : "Error de conexión"));
        }
    }

    @FXML public void cerrar() {
        Stage stage = (Stage) lblGuia.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
    private void mostrarInfo(String titulo, String msg) { new Alert(Alert.AlertType.INFORMATION, msg).show(); }
}