package com.example.packetworld;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // Import necesario para debug

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // CORRECCIÓN: Agregamos '/' al inicio
        String rutaFXML = "/com/example/packetworld/Login.fxml";

        // --- DEBUG DE SEGURIDAD ---
        // Esto imprimirá en la consola si realmente lo encontró antes de fallar
        URL url = getClass().getResource(rutaFXML);
        if (url == null) {
            System.out.println("❌ FATAL: No se encuentra el archivo en: " + rutaFXML);
            System.out.println("Asegurate de que Login.fxml esté en: src/main/resources/com/example/packetworld/");
            // Detenemos aquí para no tener el error gigante de excepción
            return;
        } else {
            System.out.println("✅ ÉXITO: Archivo encontrado en: " + url);
        }
        // --------------------------

        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();

        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Packet-World | Iniciar Sesión");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}