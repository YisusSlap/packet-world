package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.service.ApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class EnviosController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Envio> tblEnvios;
    @FXML private TableColumn<Envio, String> colGuia;
    @FXML private TableColumn<Envio, String> colCliente;
    @FXML private TableColumn<Envio, String> colDestino;
    @FXML private TableColumn<Envio, String> colEstatus;
    @FXML private TableColumn<Envio, String> colConductor;
    @FXML private TableColumn<Envio, String> colCosto;

    private ObservableList<Envio> listaMaster;
    private FilteredList<Envio> listaFiltrada;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
        configurarBuscador();
    }

    private void configurarColumnas() {
        colGuia.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroGuia()));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCliente()));
        colDestino.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCiudad()));
        colEstatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstatusActual()));
        colCosto.setCellValueFactory(c -> new SimpleStringProperty("$" + c.getValue().getCostoTotal()));

        // Columna Conductor con lógica visual "Por Asignar"
        colConductor.setCellValueFactory(c -> {
            String conductor = c.getValue().getIdConductorAsignado();
            if (conductor == null || conductor.isEmpty()) return new SimpleStringProperty("🔴 POR ASIGNAR");
            return new SimpleStringProperty(conductor);
        });
    }

    private void cargarDatos() {
        listaMaster = FXCollections.observableArrayList(ApiService.obtenerTodosEnvios());
        listaFiltrada = new FilteredList<>(listaMaster, p -> true);
        SortedList<Envio> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tblEnvios.comparatorProperty());
        tblEnvios.setItems(sortedData);
    }

    private void configurarBuscador() {
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            listaFiltrada.setPredicate(envio -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();

                return envio.getNumeroGuia().toLowerCase().contains(lower) ||
                        envio.getNombreCliente().toLowerCase().contains(lower) ||
                        envio.getEstatusActual().toLowerCase().contains(lower);
            });
        });
    }

    // --- ACCIONES DE BOTONES (Ahora usan la selección de la tabla) ---

    private Envio obtenerEnvioSeleccionado() {
        Envio e = tblEnvios.getSelectionModel().getSelectedItem();
        if (e == null) mostrarAlerta("Selecciona un envío de la tabla primero.");
        return e;
    }

    @FXML public void btnVerDetalle() {
        Envio e = obtenerEnvioSeleccionado();
        if (e != null) abrirModal("DetalleEnvio.fxml", "Detalle de Envío", e);
    }

    @FXML public void btnAsignarConductor() {
        Envio e = obtenerEnvioSeleccionado();
        if (e != null) abrirModal("FormularioAsignacion.fxml", "Asignar Conductor", e);
    }

    @FXML public void btnCambiarEstatus() {
        Envio e = obtenerEnvioSeleccionado();
        if (e != null) abrirModal("FormularioEstatus.fxml", "Cambiar Estatus", e);
    }

    @FXML public void btnAgregarPaquete() {
        Envio e = obtenerEnvioSeleccionado();
        if (e != null) abrirModal("FormularioPaquete.fxml", "Agregar Paquete", e);
    }

    @FXML public void btnNuevo() {
        abrirModal("FormularioEnvio.fxml", "Nuevo Envío", null);
    }

    // Método genérico para abrir modales
    private void abrirModal(String fxml, String titulo, Envio envioParaPasar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/" + fxml));
            Parent root = loader.load();

            // Pasar el envío al controlador si es necesario
            Object controller = loader.getController();
            if (envioParaPasar != null) {
                if (controller instanceof DetalleEnvioController) ((DetalleEnvioController) controller).setEnvio(envioParaPasar);
                else if (controller instanceof FormularioAsignacionController) ((FormularioAsignacionController) controller).setEnvio(envioParaPasar);
                else if (controller instanceof FormularioEstatusController) ((FormularioEstatusController) controller).setEnvio(envioParaPasar);
                else if (controller instanceof FormularioPaqueteController) ((FormularioPaqueteController) controller).setIdEnvio(envioParaPasar.getIdEnvio());
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(titulo);
            stage.showAndWait();

            cargarDatos(); // Recargar tabla al cerrar modal
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void mostrarAlerta(String msg) { new Alert(Alert.AlertType.WARNING, msg).show(); }
}