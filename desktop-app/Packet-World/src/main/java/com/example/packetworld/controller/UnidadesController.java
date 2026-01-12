package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // UX Moderna
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

/**
 * Controlador para la Gestión de Flotilla (Unidades).
 * Realiza un cruce de información para mostrar qué conductor tiene asignada cada unidad.
 */
public class UnidadesController {

    @FXML private TableView<Unidad> tblUnidades;
    @FXML private TableColumn<Unidad, String> colNii;
    @FXML private TableColumn<Unidad, String> colMarca;
    @FXML private TableColumn<Unidad, String> colModelo;
    @FXML private TableColumn<Unidad, String> colAnio;
    @FXML private TableColumn<Unidad, String> colVin;
    @FXML private TableColumn<Unidad, String> colTipo;
    @FXML private TableColumn<Unidad, String> colEstatus;
    @FXML private TableColumn<Unidad, String> colConductor;
    @FXML private TextField txtBuscar;

    private ObservableList<Unidad> listaMaster;
    private FilteredList<Unidad> listaFiltrada;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();

        // Filtro Local (Búsqueda rápida)
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

        // Columna Especial: Conductor Asignado (Calculado)
        colConductor.setCellValueFactory(c -> {
            String conductor = c.getValue().getConductorAsignado();
            if (conductor == null || conductor.isEmpty()) return new SimpleStringProperty("--- Libres ---");
            return new SimpleStringProperty(conductor);
        });
    }

    /**
     * Carga las unidades y realiza el "Match" con los conductores para saber quién trae qué unidad.
     */
    private void cargarDatos() {
        // 1. Obtener Unidades
        List<Unidad> datosUnidades = ApiService.obtenerUnidades();

        // 2. Obtener Conductores
        List<Colaborador> listaConductores = ApiService.buscarColaboradores(null, "Conductor", null);

        // 3. Cruce de Datos (Algoritmo de búsqueda)
        if (listaConductores != null && datosUnidades != null) {
            for (Colaborador chofer : listaConductores) {
                String unidadDelChofer = chofer.getIdUnidadAsignada();

                if (unidadDelChofer != null && !unidadDelChofer.isEmpty()) {
                    // Buscar la unidad que coincida con el VIN
                    for (Unidad u : datosUnidades) {
                        if (u.getVin() != null && u.getVin().equalsIgnoreCase(unidadDelChofer)) {
                            // Asignar nombre del conductor a la unidad (solo visual)
                            u.setConductorAsignado(chofer.getNombre() + " " + chofer.getApellidoPaterno());
                            break;
                        }
                    }
                }
            }
        }

        listaMaster = FXCollections.observableArrayList(datosUnidades);
        listaFiltrada = new FilteredList<>(listaMaster, p -> true);
        tblUnidades.setItems(listaFiltrada);
    }

    @FXML
    public void btnNueva() { abrirFormulario(null); }

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

        // Validar selección
        if (seleccionada == null) {
            mostrarAlerta("Selecciona una unidad para dar de baja.");
            return;
        }

        // --- VALIDACIÓN NUEVA: UNIDAD OCUPADA ---
        // Usamos el dato que calculamos en cargarDatos().
        // Si tiene texto, significa que un chofer la tiene asignada.
        if (seleccionada.getConductorAsignado() != null && !seleccionada.getConductorAsignado().isEmpty()) {
            mostrarAlerta("Operación Denegada:\n" +
                    "La unidad está actualmente asignada al conductor:\n" +
                    "👤 " + seleccionada.getConductorAsignado() + "\n\n" +
                    "Primero debe retirar la unidad desde el módulo de Colaboradores.");
            return;
        }
        // ----------------------------------------

        // Dialogo para pedir motivo de baja
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Dar de Baja");
        dialog.setHeaderText("Baja de unidad: " + seleccionada.getMarca() + " " + seleccionada.getModelo());
        dialog.setContentText("Motivo de la baja:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            seleccionada.setMotivoBaja(result.get());

            Respuesta resp = ApiService.darBajaUnidad(seleccionada);

            if (resp != null && !resp.getError()) {
                Notificacion.mostrar("Baja Exitosa", "Unidad marcada como inactiva.", Notificacion.EXITO);
                cargarDatos();
            } else {
                mostrarAlerta("Error al dar de baja.");
            }
        }
    }

    private void abrirFormulario(Unidad unidad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/FormularioUnidad.fxml"));
            Parent root = loader.load();

            FormularioUnidadController controller = loader.getController();
            controller.setUnidad(unidad);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            com.example.packetworld.util.Tema.aplicar(scene); // Aplicamos el tema
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(unidad == null ? "Nueva Unidad" : "Editar Unidad");
            stage.showAndWait();

            cargarDatos();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Atención");
        a.setContentText(msg);
        a.show();
    }
}