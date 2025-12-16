package com.example.packetworld.controller;

import com.example.packetworld.model.RSAutenticacionColaborador;
import com.example.packetworld.service.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField txtNumPersonal;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void btnIngresarAction() {
        String num = txtNumPersonal.getText();
        String pass = txtPassword.getText();

        if (num.isEmpty() || pass.isEmpty()) {
            mostrarMensaje("Por favor llene todos los campos.", true);
            return;
        }

        mostrarMensaje("Conectando...", false);

        // Llamada a la API
        RSAutenticacionColaborador respuesta = ApiService.login(num, pass);

        if (respuesta != null && !respuesta.isError()) {

            // --- VALIDACIÓN DE SEGURIDAD EXTRA ---
            String rol = respuesta.getColaborador().getRol();

            if (rol != null && rol.equalsIgnoreCase("Conductor")) {
                mostrarMensaje("Acceso DENEGADO.\nConductores deben usar la Web App.", true);
                return; // ¡ALTO! No entra al dashboard
            }
            // -------------------------------------

            // Guardamos el usuario en sesión
            ApiService.usuarioLogueado = respuesta.getColaborador();
            abrirDashboard();
        } else {
            String msg = (respuesta != null) ? respuesta.getMensaje() : "Error de conexión.";
            mostrarMensaje(msg, true);
        }
    }

    private void mostrarMensaje(String msg, boolean esError) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setStyle(esError ? "-fx-text-fill: red;" : "-fx-text-fill: orange;");
    }

    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtNumPersonal.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setTitle("Packet-World | Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarMensaje("Error al cargar la vista del Dashboard.", true);
        }
    }
}