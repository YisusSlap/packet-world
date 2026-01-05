package com.example.packetworld;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Scene scene;

    // Variable para controlar el navegador
    private static HostServices hostServices;

    @Override
    public void start(Stage stage) throws IOException {
        // --- 1. CORRECCIÓN GOOGLE MAPS: Inicializar el servicio ---
        hostServices = getHostServices();
        // ----------------------------------------------------------

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("Login.fxml"));
        scene = new Scene(fxmlLoader.load());

        // --- 2. CORRECCIÓN CSS: Ruta Absoluta ---
        // Basado en tu estructura: resources/com/example/packetworld/styles/estilos.css
        String rutaCss = "/com/example/packetworld/styles/estilos.css";
        URL urlCss = App.class.getResource(rutaCss);

        if (urlCss != null) {
            scene.getStylesheets().add(urlCss.toExternalForm());
            System.out.println("✅ Estilo cargado correctamente.");
        } else {
            System.out.println("❌ ERROR CRÍTICO: No se encontró el CSS en: " + rutaCss);
            System.out.println("Asegúrate de haber hecho 'Reload Maven Project' o 'Rebuild'.");
        }
        // -----------------------------------------

        // Cargar Icono
        try {
            // Misma lógica: Ruta absoluta para asegurar
            stage.getIcons().add(new Image(App.class.getResourceAsStream("/com/example/packetworld/images/logo.png")));
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo cargar el icono (no es crítico).");
        }

        stage.setTitle("Packet World - Logística");
        stage.setScene(scene);
        stage.show();
    }

    // --- MÉTODO ESTÁTICO PARA ABRIR NAVEGADOR ---
    public static void abrirNavegador(String url) {
        if (hostServices != null) {
            hostServices.showDocument(url);
        } else {
            System.out.println("⚠️ Error: El servicio de HostServices no fue inicializado.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}