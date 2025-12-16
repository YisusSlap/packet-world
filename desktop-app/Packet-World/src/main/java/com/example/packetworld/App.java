package com.example.packetworld;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // Import necesario para debug

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // --- AGREGA ESTO ---
        // Cargar icono de la aplicación
        try {
            stage.getIcons().add(new Image(App.class.getResourceAsStream("images/logo.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono.");
        }
        // -------------------

        stage.setTitle("Packet World - Logística");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}