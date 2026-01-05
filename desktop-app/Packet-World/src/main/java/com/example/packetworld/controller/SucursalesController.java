package com.example.packetworld.controller;

import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Sucursal;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // UX Moderna
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList; // Para ordenar al filtrar
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

/**
 * Controlador para el Catálogo de Sucursales.
 * Permite visualizar, filtrar, agregar, editar y eliminar sucursales.
 */
public class SucursalesController {

    @FXML private TableView<Sucursal> tblSucursales;
    @FXML private TableColumn<Sucursal, String> colCodigo;
    @FXML private TableColumn<Sucursal, String> colNombre;
    @FXML private TableColumn<Sucursal, String> colDireccion;
    @FXML private TableColumn<Sucursal, String> colCiudad;
    @FXML private TableColumn<Sucursal, String> colEstatus;

    @FXML private TextField txtBuscar;

    // Listas para el manejo eficiente de datos en memoria
    private ObservableList<Sucursal> listaMaster; // Lista original
    private FilteredList<Sucursal> listaFiltrada; // Lista dinámica según búsqueda

    @FXML
    public void initialize() {
        // Configuración de Columnas
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigoSucursal()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCorto()));

        // Columna calculada: Dirección Completa
        colDireccion.setCellValueFactory(c -> {
            Sucursal s = c.getValue();
            // Validación defensiva contra nulos
            String calle = s.getCalle() != null ? s.getCalle() : "";
            String num = s.getNumero() != null ? s.getNumero() : "";
            String col = s.getNombreColonia() != null ? s.getNombreColonia() : "";
            String cp = s.getCodigoPostal() != null ? s.getCodigoPostal() : "";

            return new SimpleStringProperty(calle + " #" + num + ", Col. " + col + ", CP: " + cp);
        });

        colCiudad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCiudad() + ", " + c.getValue().getEstado()));
        colEstatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstatus()));

        // Cargar datos al inicio
        cargarDatos();

        // CONFIGURACIÓN DEL BUSCADOR EN TIEMPO REAL
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (listaFiltrada == null) return;

            listaFiltrada.setPredicate(sucursal -> {
                // Si el buscador está vacío, mostramos todo
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String filtro = newValue.toLowerCase();

                // Buscamos coincidencias en Código, Nombre o Ciudad
                if (sucursal.getCodigoSucursal() != null && sucursal.getCodigoSucursal().toLowerCase().contains(filtro)) {
                    return true;
                } else if (sucursal.getNombreCorto() != null && sucursal.getNombreCorto().toLowerCase().contains(filtro)) {
                    return true;
                } else if (sucursal.getCiudad() != null && sucursal.getCiudad().toLowerCase().contains(filtro)) {
                    return true;
                }

                return false; // No coincide
            });
        });
    }

    private void cargarDatos() {
        // 1. Descargar datos de la API
        listaMaster = FXCollections.observableArrayList(ApiService.obtenerSucursales());

        // 2. Envolver en una lista filtrable
        listaFiltrada = new FilteredList<>(listaMaster, p -> true);

        // 3. Envolver en una lista ordenable (SortedList)
        // Esto permite que al dar clic en el encabezado de la columna, se ordene
        SortedList<Sucursal> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tblSucursales.comparatorProperty());

        // 4. Asignar a la tabla
        tblSucursales.setItems(sortedData);
    }

    // --- ACCIONES ---

    @FXML public void btnNueva() { abrirFormulario(null); }

    @FXML public void btnEditar() {
        Sucursal s = tblSucursales.getSelectionModel().getSelectedItem();
        if (s != null) abrirFormulario(s);
        else mostrarAlerta("Atención", "Selecciona una sucursal para editar.");
    }

    @FXML public void btnEliminar() {
        Sucursal s = tblSucursales.getSelectionModel().getSelectedItem();
        if (s != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de eliminar la sucursal " + s.getNombreCorto() + "?");
            confirm.setHeaderText(null); // Limpiar header para que se vea mejor

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Respuesta r = ApiService.eliminarSucursal(s.getCodigoSucursal());

                if (r != null && !r.getError()) {
                    Notificacion.mostrar("Eliminado", "La sucursal fue eliminada.", Notificacion.EXITO);
                    cargarDatos(); // Recargar tabla
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la sucursal.");
                }
            }
        } else {
            mostrarAlerta("Atención", "Selecciona una sucursal para eliminar.");
        }
    }

    private void abrirFormulario(Sucursal s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/FormularioSucursal.fxml"));
            Parent root = loader.load();

            FormularioSucursalController ctrl = loader.getController();
            ctrl.setSucursal(s);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquear ventana de atrás
            stage.setTitle(s == null ? "Nueva Sucursal" : "Editar Sucursal");
            stage.showAndWait();

            cargarDatos(); // Recargar al volver
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}