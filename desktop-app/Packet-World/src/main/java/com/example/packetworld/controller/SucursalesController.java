package com.example.packetworld.controller;

import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Sucursal;
import com.example.packetworld.service.ApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList; // IMPORTANTE: Agrega este import
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class SucursalesController {

    @FXML private TableView<Sucursal> tblSucursales;
    @FXML private TableColumn<Sucursal, String> colCodigo;
    @FXML private TableColumn<Sucursal, String> colNombre;
    @FXML private TableColumn<Sucursal, String> colDireccion;
    @FXML private TableColumn<Sucursal, String> colCiudad;
    @FXML private TableColumn<Sucursal, String> colEstatus;

    @FXML private TextField txtBuscar;

    // Listas para el filtrado
    private ObservableList<Sucursal> listaMaster;
    private FilteredList<Sucursal> listaFiltrada;

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigoSucursal()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCorto()));

        colDireccion.setCellValueFactory(c -> {
            Sucursal s = c.getValue();
            // Validamos nulos para evitar errores si la dirección viene incompleta
            String calle = s.getCalle() != null ? s.getCalle() : "";
            String num = s.getNumero() != null ? s.getNumero() : "";
            String col = s.getNombreColonia() != null ? s.getNombreColonia() : "";
            String cp = s.getCodigoPostal() != null ? s.getCodigoPostal() : "";

            String dir = calle + " #" + num + ", Col. " + col + ", CP: " + cp;
            return new SimpleStringProperty(dir);
        });

        colCiudad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCiudad() + ", " + c.getValue().getEstado()));
        colEstatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstatus()));

        // Cargamos los datos iniciales
        cargarDatos();

        // LISTENER CORREGIDO: Usamos 'newValue' en ambos lados
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {

            // Si listaFiltrada no se inicializó, salimos para evitar crash
            if (listaFiltrada == null) return;

            listaFiltrada.setPredicate(sucursal -> {
                // Si el buscador está vacío, muestra todo
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Validaciones de nulos antes de usar .toLowerCase() para evitar crash
                if (sucursal.getCodigoSucursal() != null && sucursal.getCodigoSucursal().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (sucursal.getNombreCorto() != null && sucursal.getNombreCorto().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (sucursal.getCiudad() != null && sucursal.getCiudad().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false; // No coincide
            });
        });
    }

    private void cargarDatos() {
        // 1. Obtenemos datos de la API
        listaMaster = FXCollections.observableArrayList(ApiService.obtenerSucursales());

        // 2. Inicializamos la lista filtrada con los datos maestros
        listaFiltrada = new FilteredList<>(listaMaster, p -> true);

        // 3. (Opcional pero recomendado) Envolver en SortedList para que funcionen los clics en cabeceras
        SortedList<Sucursal> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tblSucursales.comparatorProperty());

        // 4. Seteamos la lista con capacidades de filtro a la tabla
        tblSucursales.setItems(sortedData);
    }

    @FXML public void btnNueva() { abrirFormulario(null); }

    @FXML public void btnEditar() {
        Sucursal s = tblSucursales.getSelectionModel().getSelectedItem();
        if (s != null) abrirFormulario(s);
        else mostrarAlerta("Selecciona una sucursal.");
    }

    @FXML public void btnEliminar() {
        Sucursal s = tblSucursales.getSelectionModel().getSelectedItem();
        if (s != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar sucursal " + s.getNombreCorto() + "?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                Respuesta r = ApiService.eliminarSucursal(s.getCodigoSucursal());
                if (r != null && !r.getError()) cargarDatos();
                else mostrarAlerta("Error al eliminar");
            }
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).show(); }
}