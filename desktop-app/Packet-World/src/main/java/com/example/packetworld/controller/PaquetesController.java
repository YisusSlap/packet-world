package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Paquete;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para el Inventario Global de Paquetes.
 * Permite visualizar el contenido individual de todos los envíos y gestionarlos
 * si el estatus del envío padre lo permite.
 */
public class PaquetesController {

    @FXML private TableView<Paquete> tblPaquetes;
    @FXML private TableColumn<Paquete, String> colGuia;
    @FXML private TableColumn<Paquete, String> colDescripcion;
    @FXML private TableColumn<Paquete, String> colPeso;
    @FXML private TableColumn<Paquete, String> colDimensiones;
    @FXML private TableColumn<Paquete, String> colEstatus;
    @FXML private TextField txtBuscar;

    private ObservableList<Paquete> listaMaster;
    private FilteredList<Paquete> listaFiltrada;

    // Mapa Vital: Relaciona cada Paquete con su Envío Padre para poder validarlo y editarlo
    private Map<Paquete, Envio> mapaPadres = new HashMap<>();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
        configurarBuscador();
    }

    private void configurarColumnas() {
        colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPesoKg() + " kg"));
        colDimensiones.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDimAltoCm() + "x" + c.getValue().getDimAnchoCm() + "x" + c.getValue().getDimProfundidadCm()
        ));

        // Columnas calculadas usando el Mapa de Padres
        colGuia.setCellValueFactory(c -> {
            Envio padre = mapaPadres.get(c.getValue());
            return new SimpleStringProperty(padre != null ? padre.getNumeroGuia() : "---");
        });

        colEstatus.setCellValueFactory(c -> {
            Envio padre = mapaPadres.get(c.getValue());
            return new SimpleStringProperty(padre != null ? padre.getEstatusActual() : "---");
        });
    }

    private void cargarDatos() {
        List<Envio> envios = ApiService.obtenerTodosEnvios();
        listaMaster = FXCollections.observableArrayList();
        mapaPadres.clear();

        // APLANAR LA LISTA: Extraemos paquetes de los envíos
        for (Envio e : envios) {
            if (e.getListaPaquetes() != null) {
                for (Paquete p : e.getListaPaquetes()) {
                    mapaPadres.put(p, e); // Guardamos la referencia
                    listaMaster.add(p);
                }
            }
        }

        listaFiltrada = new FilteredList<>(listaMaster, p -> true);
        SortedList<Paquete> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tblPaquetes.comparatorProperty());
        tblPaquetes.setItems(sortedData);
    }

    private void configurarBuscador() {
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            listaFiltrada.setPredicate(p -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();

                boolean matchDesc = p.getDescripcion().toLowerCase().contains(lower);

                Envio padre = mapaPadres.get(p);
                boolean matchGuia = (padre != null) && padre.getNumeroGuia().toLowerCase().contains(lower);
                boolean matchEstatus = (padre != null) && padre.getEstatusActual().toLowerCase().contains(lower);

                return matchDesc || matchGuia || matchEstatus;
            });
        });
    }

    // --- ACCIONES ---

    @FXML
    public void btnEditar() {
        Paquete p = tblPaquetes.getSelectionModel().getSelectedItem();
        if (p == null) {
            mostrarAlerta("Selecciona un paquete.");
            return;
        }

        Envio padre = mapaPadres.get(p);
        if (padre != null) {
            // Regla: Solo IDs 1 y 2 son editables
            int estatus = padre.getIdEstatus();
            if (estatus > 2) {
                mostrarAlerta("Operación Denegada:\nEl envío ya salió de almacén (" + padre.getEstatusActual() + ").\nNo se puede modificar su contenido.");
                return;
            }

            // Abrir formulario de edición de paquete
            abrirFormularioPaquete(p, padre);
        }
    }

    @FXML
    public void btnEliminar() {
        Paquete p = tblPaquetes.getSelectionModel().getSelectedItem();
        if (p == null) {
            mostrarAlerta("Selecciona un paquete.");
            return;
        }

        Envio padre = mapaPadres.get(p);
        if (padre == null) return;

        // Regla: Solo IDs 1 y 2 son editables
        int estatus = padre.getIdEstatus();
        if (estatus > 2) {
            mostrarAlerta("Operación Denegada:\nEl envío ya salió de almacén (" + padre.getEstatusActual() + ").\nNo se puede eliminar contenido.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar paquete: " + p.getDescripcion() + "?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 1. Eliminar de la lista del padre
            padre.getListaPaquetes().remove(p);

            // 2. Actualizar Envío Completo
            Respuesta resp = ApiService.editarEnvio(padre);

            if (resp != null && !resp.getError()) {
                Notificacion.mostrar("Paquete Eliminado", "Inventario actualizado.", Notificacion.EXITO);
                cargarDatos();
            } else {
                mostrarAlerta("Error al actualizar: " + (resp != null ? resp.getMensaje() : "Red"));
            }
        }
    }

    private void abrirFormularioPaquete(Paquete p, Envio padre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/FormularioPaquete.fxml"));
            Parent root = loader.load();

            FormularioPaqueteController ctrl = loader.getController();

            // --- AQUÍ ESTÁ LA MAGIA ---
            // Le pasamos el paquete y el padre. El controlador sabrá qué hacer.
            ctrl.setPaquete(p, padre);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Editar Paquete - Guía " + padre.getNumeroGuia());
            stage.showAndWait();

            // Recargar datos al cerrar para ver los cambios
            cargarDatos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error al abrir el formulario.");
        }
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}