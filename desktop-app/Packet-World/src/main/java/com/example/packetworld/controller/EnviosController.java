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

/**
 * Controlador Principal de la Gestión de Envíos.
 * Lista todos los envíos y permite acceder a acciones como: Detalle, Asignación, Estatus y Edición.
 */
public class EnviosController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Envio> tblEnvios;
    @FXML private TableColumn<Envio, String> colGuia, colCliente, colDestino, colEstatus, colConductor, colCosto;

    private ObservableList<Envio> listaMaster;
    private FilteredList<Envio> listaFiltrada;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
        configurarBuscadorEnTiempoReal();
    }

    private void configurarColumnas() {
        colGuia.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroGuia()));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCliente()));
        colDestino.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCiudad()));
        colEstatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstatusActual()));
        colCosto.setCellValueFactory(c -> new SimpleStringProperty("$" + c.getValue().getCostoTotal()));

        // Formateo visual si no hay conductor
        colConductor.setCellValueFactory(c -> {
            String conductor = c.getValue().getIdConductorAsignado();
            return new SimpleStringProperty((conductor == null || conductor.isEmpty()) ? "🔴 POR ASIGNAR" : conductor);
        });
    }

    private void cargarDatos() {
        listaMaster = FXCollections.observableArrayList(ApiService.obtenerTodosEnvios());
        listaFiltrada = new FilteredList<>(listaMaster, p -> true);

        // SortedList permite ordenar las columnas al dar clic en el encabezado
        SortedList<Envio> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tblEnvios.comparatorProperty());

        tblEnvios.setItems(sortedData);
    }

    private void configurarBuscadorEnTiempoReal() {
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            listaFiltrada.setPredicate(envio -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                // Búsqueda por Guía, Cliente o Estatus
                return envio.getNumeroGuia().toLowerCase().contains(lower) ||
                        envio.getNombreCliente().toLowerCase().contains(lower) ||
                        envio.getEstatusActual().toLowerCase().contains(lower);
            });
        });
    }


    //                        BOTONES DE ACCIÓN


    @FXML public void btnNuevo() {
        abrirModal("FormularioEnvio.fxml", "Nuevo Envío", null);
    }

    @FXML public void btnEditar() {
        Envio e = obtenerSeleccion();
        if (e != null) {
            // Regla de Negocio: Solo editar si Estatus es 1 (Recibido) o 2 (Procesado)
            int estatus = e.getIdEstatus();
            if (estatus == 1 || estatus == 2) {
                abrirModal("FormularioEnvio.fxml", "Editar Envío", e);
            } else {
                mostrarAlerta("Operación Denegada", "Solo se pueden editar envíos que no han salido de sucursal.");
            }
        }
    }

    @FXML public void btnVerDetalle() {
        Envio e = obtenerSeleccion();
        if (e != null) abrirModal("DetalleEnvio.fxml", "Detalle de Envío", e);
    }

    @FXML public void btnAsignarConductor() {
        Envio e = obtenerSeleccion();
        if (e != null) abrirModal("FormularioAsignacion.fxml", "Asignar Conductor", e);
    }

    @FXML public void btnCambiarEstatus() {
        Envio e = obtenerSeleccion();
        if (e != null) abrirModal("FormularioEstatus.fxml", "Cambiar Estatus", e);
    }

    @FXML public void btnAgregarPaquete() {
        Envio e = obtenerSeleccion();
        if (e != null) {
            // Validar que el envío no haya salido
            String estatus = e.getEstatusActual().toLowerCase();
            if (estatus.contains("tránsito") || estatus.contains("ruta") || estatus.contains("entregado") || estatus.contains("cancelado")) {
                mostrarAlerta("Operación Denegada", "No se pueden agregar paquetes a envíos en tránsito o finalizados.");
            } else {
                abrirModal("FormularioPaquete.fxml", "Agregar Paquete Extra", e);
            }
        }
    }


    //                        MÉTODOS AUXILIARES

    private Envio obtenerSeleccion() {
        Envio e = tblEnvios.getSelectionModel().getSelectedItem();
        if (e == null) mostrarAlerta("Atención", "Selecciona un envío de la tabla primero.");
        return e;
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Gestor centralizado para abrir ventanas modales.
     * Inyecta el objeto 'envio' al controlador destino según corresponda.
     */
    private void abrirModal(String fxml, String titulo, Envio envioParaPasar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/" + fxml));
            Parent root = loader.load();

            Object controller = loader.getController();

            // Inyección de Dependencias Manual
            if (envioParaPasar != null) {
                if (controller instanceof DetalleEnvioController)
                    ((DetalleEnvioController) controller).setEnvio(envioParaPasar);
                else if (controller instanceof FormularioEnvioController)
                    ((FormularioEnvioController) controller).setEnvio(envioParaPasar);
                else if (controller instanceof FormularioAsignacionController)
                    ((FormularioAsignacionController) controller).setEnvio(envioParaPasar);
                else if (controller instanceof FormularioEstatusController)
                    ((FormularioEstatusController) controller).setEnvio(envioParaPasar);
                else if (controller instanceof FormularioPaqueteController)
                    ((FormularioPaqueteController) controller).setIdEnvio(envioParaPasar.getIdEnvio());
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(titulo);
            stage.showAndWait();

            cargarDatos(); // Recargar tabla al cerrar modal
        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error Crítico", "No se pudo cargar la ventana: " + fxml);
        }
    }
}