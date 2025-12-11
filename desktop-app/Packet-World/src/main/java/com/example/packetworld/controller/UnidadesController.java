package com.example.packetworld.controller;

import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UnidadesController {

    @FXML private TableView<Unidad> tblUnidades;
    @FXML private TableColumn<Unidad, String> colNii;
    @FXML private TableColumn<Unidad, String> colMarca;
    @FXML private TableColumn<Unidad, String> colModelo;
    @FXML private TableColumn<Unidad, String> colAnio;
    @FXML private TableColumn<Unidad, String> colVin;
    @FXML private TableColumn<Unidad, String> colTipo;
    @FXML private TableColumn<Unidad, String> colEstatus;
    @FXML private TextField txtBuscar;

    private ObservableList<Unidad> listaMaster;
    private FilteredList<Unidad> listaFiltrada;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();

        // Filtro local (más rápido que ir a la API cada vez que escribes)
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            listaFiltrada.setPredicate(u -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                // Buscar por VIN, Marca o Modelo
                return u.getVin().toLowerCase().contains(lower)
                        || u.getMarca().toLowerCase().contains(lower)
                        || u.getModelo().toLowerCase().contains(lower);
            });
        });
    }

    private void configurarColumnas() {
        colNii.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNii()));
        colMarca.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMarca()));
        colModelo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModelo()));
        colAnio.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getAnio())));
        colVin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVin()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoUnidad()));
        colEstatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstatus()));
    }

    private void cargarDatos() {
        List<Unidad> datos = ApiService.obtenerUnidades(); // Usamos el método que ya tenías
        listaMaster = FXCollections.observableArrayList(datos);
        listaFiltrada = new FilteredList<>(listaMaster, p -> true);
        tblUnidades.setItems(listaFiltrada);
    }

    @FXML
    public void btnNueva() {
        abrirFormulario(null);
    }

    @FXML
    public void btnEditar() {
        Unidad seleccionada = tblUnidades.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            abrirFormulario(seleccionada);
        } else {
            mostrarAlerta("Selecciona una unidad para editar.");
        }
    }

    @FXML
    public void btnBaja() {
        Unidad seleccionada = tblUnidades.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            // Tu API pide un motivo para la baja, lo pedimos con un TextInputDialog
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Dar de Baja");
            dialog.setHeaderText("Baja de unidad: " + seleccionada.getMarca() + " " + seleccionada.getModelo());
            dialog.setContentText("Motivo de la baja:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                seleccionada.setMotivoBaja(result.get());

                Respuesta resp = ApiService.darBajaUnidad(seleccionada);
                if (resp != null && !resp.getError()) {
                    mostrarAlerta("Unidad dada de baja correctamente.");
                    cargarDatos();
                } else {
                    mostrarAlerta("Error al dar de baja.");
                }
            }
        } else {
            mostrarAlerta("Selecciona una unidad.");
        }
    }

    private void abrirFormulario(Unidad unidad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/FormularioUnidad.fxml"));
            Parent root = loader.load();

            // Pasamos la unidad al controlador del formulario (IMPLEMENTAREMOS ESTO EN EL SIGUIENTE PASO)
            FormularioUnidadController controller = loader.getController();
            controller.setUnidad(unidad);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(unidad == null ? "Nueva Unidad" : "Editar Unidad");
            stage.showAndWait();

            cargarDatos(); // Recargar al volver
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }
}