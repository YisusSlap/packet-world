package com.example.packetworld.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import com.example.packetworld.App;

public class Notificacion {

    public static final boolean ERROR = true;
    public static final boolean EXITO = false;

    public static void mostrar(String titulo, String mensaje, boolean esError) {
        // 1. Crear ventana sin bordes (transparente)
        Stage toastStage = new Stage();
        toastStage.initStyle(StageStyle.TRANSPARENT);
        toastStage.setAlwaysOnTop(true);

        // 2. Crear etiqueta con el mensaje
        Label label = new Label(titulo + "\n" + mensaje);
        label.getStyleClass().add("toast"); // Clase base del CSS
        label.getStyleClass().add(esError ? "toast-error" : "toast-success"); // Variación de color

        label.setWrapText(true);
        label.setMaxWidth(350);
        label.setAlignment(Pos.CENTER_LEFT);

        // 3. Contenedor
        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT); // Fondo de la escena transparente

        // Cargar CSS Global para que tome los estilos .toast
        try {
            // Usamos la misma ruta que en App.java
            scene.getStylesheets().add(Notificacion.class.getResource("/com/example/packetworld/styles/estilos.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("⚠️ No se cargó estilo de notificación.");
        }

        toastStage.setScene(scene);

        // 4. Posición (Ajuste manual para esquina inferior derecha)
        // Puedes ajustar estos valores según tu resolución (luego lo haremos dinámico si quieres)
        toastStage.setX(1100);
        toastStage.setY(700);

        toastStage.show();

        // 5. Temporizador: Cerrar a los 3.5 segundos
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(3500), e -> {
            toastStage.close();
        }));
        timeline.play();
    }
}