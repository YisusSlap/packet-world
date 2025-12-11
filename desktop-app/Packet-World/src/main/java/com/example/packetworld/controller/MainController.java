package com.example.packetworld.controller;

import com.example.packetworld.service.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUsuario;

    @FXML
    public void initialize() {
        if (ApiService.usuarioLogueado != null) {
            lblUsuario.setText("Hola, " + ApiService.usuarioLogueado.getNombre());
        }
        // Cargar Colaboradores al inicio por defecto
        irColaboradores();
    }

    private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/" + fxml));
            Parent vista = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error cargando vista: " + fxml);
        }
    }

    @FXML public void irColaboradores() { cargarVista("ColaboradoresView.fxml"); }
    @FXML public void irEnvios() { /* Por implementar */ }
    @FXML public void irUnidades() { cargarVista("UnidadesView.fxml"); }
    @FXML public void irSucursales() { /* Por implementar */ }
    @FXML public void irClientes() { /* Por implementar */ }

    @FXML
    public void cerrarSesion() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/packetworld/view/Login.fxml"));
            stage.setScene(new Scene(root));
            ApiService.usuarioLogueado = null;
        } catch (IOException e) { e.printStackTrace(); }
    }
}