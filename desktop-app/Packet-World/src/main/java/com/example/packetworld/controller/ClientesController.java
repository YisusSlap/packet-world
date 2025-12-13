package com.example.packetworld.controller;

import com.example.packetworld.model.Cliente;
import com.example.packetworld.model.Respuesta;
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

public class ClientesController {

    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colDireccion;
    @FXML private TextField txtBuscar;

    private ObservableList<Cliente> listaMaster;
    private FilteredList<Cliente> listaFiltrada;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNombre() + " " + c.getValue().getApellidoPaterno() + " " + c.getValue().getApellidoMaterno()
        ));
        colCorreo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorreoElectronico()));
        colTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));

        colDireccion.setCellValueFactory(c -> {
            Cliente cli = c.getValue();
            String dir = cli.getCalle() + " #" + cli.getNumero() + ", Col. " + cli.getNombreColonia();
            return new SimpleStringProperty(dir);
        });

        cargarDatos();

        // Buscador
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (listaFiltrada == null) return;
            listaFiltrada.setPredicate(cli -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();

                return cli.getNombre().toLowerCase().contains(lower) ||
                        cli.getApellidoPaterno().toLowerCase().contains(lower) ||
                        cli.getCorreoElectronico().toLowerCase().contains(lower) ||
                        cli.getTelefono().contains(lower);
            });
        });
    }

    private void cargarDatos() {
        // Traemos todos los clientes (enviando nulls a los filtros de la API)
        listaMaster = FXCollections.observableArrayList(ApiService.buscarClientes(null, null, null));
        listaFiltrada = new FilteredList<>(listaMaster, p -> true);
        SortedList<Cliente> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tblClientes.comparatorProperty());
        tblClientes.setItems(sortedData);
    }

    @FXML public void btnNuevo() { abrirFormulario(null); }

    @FXML public void btnEditar() {
        Cliente c = tblClientes.getSelectionModel().getSelectedItem();
        if (c != null) abrirFormulario(c);
        else mostrarAlerta("Selecciona un cliente.");
    }

    @FXML public void btnEliminar() {
        Cliente c = tblClientes.getSelectionModel().getSelectedItem();
        if (c != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar a " + c.getNombre() + "?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                Respuesta r = ApiService.eliminarCliente(c.getIdCliente());
                if (r != null && !r.getError()) cargarDatos();
                else mostrarAlerta("Error al eliminar.");
            }
        }
    }

    private void abrirFormulario(Cliente c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/FormularioCliente.fxml"));
            Parent root = loader.load();
            FormularioClienteController ctrl = loader.getController();
            ctrl.setCliente(c);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).show(); }
}