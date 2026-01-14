package com.example.packetworld.util;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class Tema {

    // Variable estática para recordar si el usuario quiere modo oscuro
    private static boolean esOscuro = false;

    // Método para cambiar (Switch)
    public static void toggle(Scene scene) {
        esOscuro = !esOscuro;
        aplicar(scene);
    }

    // Método que "pinta" la escena
    public static void aplicar(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;

        Parent root = scene.getRoot();

        // Primero limpiamos por si acaso
        root.getStyleClass().remove("dark-mode");

        // Si está activado, agregamos la clase CSS
        if (esOscuro) {
            root.getStyleClass().add("dark-mode");
        }
    }

    public static boolean isEsOscuro() {
        return esOscuro;
    }
}