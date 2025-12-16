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
import java.util.Base64; // <--- IMPORTANTE: PARA DECODIFICAR EL STRING

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUsuario;
    @FXML private ImageView imgPerfilUsuario;

    @FXML private Button btnInicio;
    @FXML private Button btnEnvios;
    @FXML private Button btnClientes;
    @FXML private Button btnColaboradores;
    @FXML private Button btnUnidades;
    @FXML private Button btnSucursales;

    @FXML
    public void initialize() {
        if (ApiService.usuarioLogueado != null) {
            Colaborador user = ApiService.usuarioLogueado;
            lblUsuario.setText("Hola, " + user.getNombre());

            // 1. Aplicar Seguridad
            configurarPermisos(user.getRol());

            // 2. Cargar Foto (Pasando el usuario logueado)
            cargarFotoPerfil(user);
        }

        // Cargar Dashboard al inicio
        irInicio();
    }

    private void configurarPermisos(String rol) {
        if (rol == null) return;
        rol = rol.toLowerCase().trim();

        if (rol.contains("administrador")) return;

        if (rol.equals("ejecutivo de tienda") || rol.equals("ejecutivo")) {
            ocultarBoton(btnColaboradores);
            ocultarBoton(btnUnidades);
            ocultarBoton(btnSucursales);
            return;
        }

        ocultarBoton(btnClientes);
        ocultarBoton(btnColaboradores);
        ocultarBoton(btnUnidades);
        ocultarBoton(btnSucursales);
    }

    private void ocultarBoton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }

    @FXML
    public void irInicio() { cargarVista("DashboardView.fxml"); }

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
    @FXML public void irEnvios() { cargarVista("EnviosView.fxml"); }
    @FXML public void irUnidades() { cargarVista("UnidadesView.fxml"); }
    @FXML public void irSucursales() { cargarVista("SucursalesView.fxml");}
    @FXML public void irClientes() { cargarVista("ClientesView.fxml"); }

    @FXML
    public void cerrarSesion() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/packetworld/Login.fxml"));
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            ApiService.usuarioLogueado = null;
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void cargarFotoPerfil(Colaborador user) {
        try {
            // 1. Intentar obtener bytes directos
            byte[] fotoBytes = user.getFotografiaBytes();

            // 2. Si no hay bytes, intentar decodificar el String Base64
            if ((fotoBytes == null || fotoBytes.length == 0) &&
                    user.getFotografia() != null && !user.getFotografia().isEmpty()) {

                // Limpiamos saltos de línea que a veces mete MySQL
                String base64Limpio = user.getFotografia().replaceAll("\\s", "");

                try {
                    fotoBytes = Base64.getDecoder().decode(base64Limpio);
                } catch (IllegalArgumentException e) {
                    System.out.println("⚠️ Foto corrupta en BD, se usará imagen por defecto.");
                    return; // Salimos sin hacer nada, se queda la imagen default
                }
            }

            // 3. Renderizar si obtuvimos bytes válidos
            if (fotoBytes != null && fotoBytes.length > 0) {
                ByteArrayInputStream bis = new ByteArrayInputStream(fotoBytes);
                Image img = new Image(bis);

                if (!img.isError()) { // Verificamos que sea una imagen real
                    imgPerfilUsuario.setImage(img);

                    // Recorte circular
                    Circle clip = new Circle(20, 20, 20);
                    imgPerfilUsuario.setClip(clip);
                }
            }

        } catch (Exception e) {
            System.out.println("Error al procesar foto de perfil: " + e.getMessage());
        }
    }
}