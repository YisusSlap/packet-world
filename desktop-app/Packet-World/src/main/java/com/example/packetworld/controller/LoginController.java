package com.example.packetworld.controller;

import com.example.packetworld.model.RSAutenticacionColaborador;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // UX Moderna
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controlador de Inicio de Sesión.
 * Maneja la autenticación contra la API y aplica reglas de seguridad por Rol.
 */
public class LoginController {

    @FXML private TextField txtNumPersonal;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError; // Label discreto para errores rápidos

    /**
     * Acción del botón "Ingresar".
     * Valida credenciales y redirige al Dashboard si es exitoso.
     */
    @FXML
    public void btnIngresarAction() {
        String num = txtNumPersonal.getText().trim(); // Trim para evitar espacios accidentales
        String pass = txtPassword.getText();

        // 1. Validación local
        if (num.isEmpty() || pass.isEmpty()) {
            mostrarMensaje("Por favor llene todos los campos.", true);
            return;
        }

        mostrarMensaje("Conectando...", false); // Feedback visual

        // 2. Llamada a la API (Síncrona)
        RSAutenticacionColaborador respuesta = ApiService.login(num, pass);

        if (respuesta != null && !respuesta.isError()) {

            // --- REGLA DE NEGOCIO: Bloqueo de Conductores ---
            // Los conductores no tienen permiso para usar la App de Escritorio.
            String rol = respuesta.getColaborador().getRol();

            if (rol != null && rol.equalsIgnoreCase("Conductor")) {
                mostrarMensaje("Acceso DENEGADO.\nConductores deben usar la Web App móvil.", true);
                return; // Detenemos el acceso
            }

            // 3. Éxito: Guardar sesión y cambiar de pantalla
            ApiService.usuarioLogueado = respuesta.getColaborador();

            // Notificación de bienvenida (Opcional, pero se ve bien)
            Notificacion.mostrar("Bienvenido", "Sesión iniciada correctamente.", Notificacion.EXITO);

            abrirDashboard();

        } else {
            // Error de credenciales o servidor
            String msg = (respuesta != null) ? respuesta.getMensaje() : "Error de conexión con el servidor.";
            mostrarMensaje(msg, true);
        }
    }

    /**
     * Muestra mensajes en el Label inferior del login.
     * @param msg Texto a mostrar
     * @param esError Si es true, se pinta rojo; si false, naranja/neutro.
     */
    private void mostrarMensaje(String msg, boolean esError) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setStyle(esError ? "-fx-text-fill: #FF5252;" : "-fx-text-fill: #FFA726;");
    }

    /**
     * Carga la vista principal (MainLayout) y cierra la ventana de Login.
     */
    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtNumPersonal.getScene().getWindow();

            // Reemplazamos la escena para cambiar de "Login" a "App Principal"
            Scene scene = new Scene(root);

            // Importante: Cargar CSS global aquí también por si acaso
            try {
                scene.getStylesheets().add(getClass().getResource("/com/example/packetworld/styles/estilos.css").toExternalForm());
            } catch (Exception e) {}

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Packet-World | Dashboard Operativo");

        } catch (IOException e) {
            e.printStackTrace();
            mostrarMensaje("Error crítico al cargar el Dashboard.", true);
        }
    }
}