package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.service.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Controlador Principal (Layout).
 * Gestiona el menú lateral, la carga de vistas dinámicas y la cabecera con perfil.
 */
public class MainController {

    @FXML private StackPane contentArea; // Área donde se inyectan las vistas
    @FXML private Label lblUsuario;
    @FXML private ImageView imgPerfilUsuario;


    // Botones del Menú (para aplicar permisos)
    @FXML private Button btnInicio, btnEnvios, btnClientes, btnColaboradores, btnUnidades, btnSucursales, btnPaquetes;

    @FXML
    public void initialize() {
        // Verificar sesión activa
        if (ApiService.usuarioLogueado != null) {
            Colaborador user = ApiService.usuarioLogueado;
            lblUsuario.setText("Hola, " + user.getNombre());

            // 1. Aplicar Seguridad (RBAC - Role Based Access Control)
            configurarPermisos(user.getRol());

            // 2. Cargar Avatar
            cargarFotoPerfil(user);
        }

        // Cargar Dashboard por defecto al entrar
        irInicio();
    }

    /**
     * Oculta módulos según el rol del usuario para evitar accesos no autorizados.
     */
    private void configurarPermisos(String rol) {
        if (rol == null) return;
        rol = rol.toLowerCase().trim();

        // Admin tiene acceso total
        if (rol.contains("administrador")) return;

        // Ejecutivo solo ve operaciones, no administración
        if (rol.equals("ejecutivo de tienda") || rol.equals("ejecutivo")) {
            ocultarBoton(btnColaboradores);
            ocultarBoton(btnUnidades);
            ocultarBoton(btnSucursales);
            return;
        }

        // Rol desconocido: Bloquear todo sensible por seguridad
        ocultarBoton(btnClientes);
        ocultarBoton(btnColaboradores);
        ocultarBoton(btnUnidades);
        ocultarBoton(btnSucursales);
    }

    /**
     * Método auxiliar para esconder y colapsar el espacio de un botón.
     */
    private void ocultarBoton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false); // Importante: Libera el espacio en el VBox
        }
    }

    // --- NAVEGACIÓN ---
    @FXML public void irInicio() { cargarVista("DashboardView.fxml"); }
    @FXML public void irColaboradores() { cargarVista("ColaboradoresView.fxml"); }
    @FXML public void irEnvios() { cargarVista("EnviosView.fxml"); }
    @FXML public void irUnidades() { cargarVista("UnidadesView.fxml"); }
    @FXML public void irSucursales() { cargarVista("SucursalesView.fxml");}
    @FXML public void irClientes() { cargarVista("ClientesView.fxml"); }
    @FXML public void irPaquetes() { cargarVista("PaquetesView.fxml"); }

    /**
     * Carga un archivo FXML dentro del StackPane central.
     * Reemplaza la vista actual por la nueva.
     */
    private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/" + fxml));
            Parent vista = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(vista);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error crítico cargando vista: " + fxml);
        }
    }

    @FXML
    public void cerrarSesion() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/packetworld/Login.fxml"));

            // Volvemos a la escena de Login
            Scene scene = new Scene(root);
            // Cargar CSS también aquí para que el Login se vea bien al volver
            try {
                scene.getStylesheets().add(getClass().getResource("/com/example/packetworld/styles/estilos.css").toExternalForm());
            } catch (Exception e) {}

            stage.setScene(scene);
            stage.centerOnScreen();

            ApiService.usuarioLogueado = null; // Limpiar sesión

        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Decodifica y renderiza la foto de perfil en el círculo del header.
     */
    private void cargarFotoPerfil(Colaborador user) {
        try {
            byte[] fotoBytes = user.getFotografiaBytes();

            // Si no hay bytes directos, intentamos decodificar Base64 del String
            if ((fotoBytes == null || fotoBytes.length == 0) &&
                    user.getFotografia() != null && !user.getFotografia().isEmpty()) {

                String base64Limpio = user.getFotografia().replaceAll("\\s", "");
                try {
                    fotoBytes = Base64.getDecoder().decode(base64Limpio);
                } catch (IllegalArgumentException e) {
                    System.out.println("⚠️ Foto corrupta, usando default.");
                    return;
                }
            }

            // Renderizar
            if (fotoBytes != null && fotoBytes.length > 0) {
                ByteArrayInputStream bis = new ByteArrayInputStream(fotoBytes);
                Image img = new Image(bis);

                if (!img.isError()) {
                    imgPerfilUsuario.setImage(img);

                    // Efecto de Recorte Circular (Avatar)
                    Circle clip = new Circle(20, 20, 20); // Radio, Centro X, Centro Y
                    imgPerfilUsuario.setClip(clip);
                }
            }
        } catch (Exception e) {
            System.out.println("Error no crítico en foto de perfil.");
        }
    }
}