package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Paquete;
import com.example.packetworld.service.ApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class EnviosController {

    @FXML private TextField txtGuia;
    @FXML private Label lblCliente;
    @FXML private Label lblDestino;
    @FXML private Label lblEstatus;
    @FXML private Label lblCosto;

    @FXML private TableView<Paquete> tblPaquetes;
    @FXML private TableColumn<Paquete, String> colDescripcion;
    @FXML private TableColumn<Paquete, String> colPeso;
    @FXML private TableColumn<Paquete, String> colDimensiones;

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla de paquetes
        colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPesoKg() + " kg"));
        colDimensiones.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDimAltoCm() + "x" + c.getValue().getDimAnchoCm() + "x" + c.getValue().getDimProfundidadCm()
        ));
    }

    @FXML
    public void btnBuscar() {
        String guia = txtGuia.getText().trim();
        if (guia.isEmpty()) {
            mostrarAlerta("Ingrese un número de guía.");
            return;
        }

        Envio envio = ApiService.rastrearEnvio(guia);

        if (envio != null) {
            // Llenar datos
            lblCliente.setText(envio.getNombreCliente()); // Asegúrate que tu POJO Envio tenga este campo mapeado
            lblDestino.setText(envio.getCiudad() + ", " + envio.getEstado());
            lblEstatus.setText(envio.getEstatusActual());
            lblCosto.setText("$" + envio.getCostoTotal());

            // Llenar tabla de paquetes
            if (envio.getListaPaquetes() != null) {
                tblPaquetes.setItems(FXCollections.observableArrayList(envio.getListaPaquetes()));
            } else {
                tblPaquetes.getItems().clear();
            }
        } else {
            limpiar();
            mostrarAlerta("No se encontró ningún envío con esa guía.");
        }
    }

    @FXML
    public void btnNuevo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/FormularioEnvio.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nuevo Envío");
            stage.showAndWait();
            // Al cerrar no recargamos nada porque es búsqueda por guía
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void limpiar() {
        lblCliente.setText("---");
        lblDestino.setText("---");
        lblEstatus.setText("---");
        lblCosto.setText("---");
        tblPaquetes.getItems().clear();
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.show();
    }
}