package com.example.packetworld.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;

public class Validaciones {

    // 1. SOLO NÚMEROS (Ideal para IDs numéricos, años, cantidad de paquetes)
    public static void soloNumeros(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d*")) { // Regex: Solo dígitos (0-9)
                return change;
            }
            return null; // Rechaza el cambio si hay letras
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    // 2. SOLO NÚMEROS CON LÍMITE (Ideal para Teléfonos, CP)
    public static void soloNumerosLimitado(TextField field, int maxLen) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d*") && text.length() <= maxLen) {
                return change;
            }
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    // 3. SOLO LETRAS Y ESPACIOS (Ideal para Nombres, Apellidos, Ciudad)
    public static void soloLetras(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            // Permite letras (a-z, A-Z), espacios y caracteres acentuados comunes
            if (text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*")) {
                return change;
            }
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    // 4. LÍMITE DE CARACTERES (Para evitar error de SQL "Data too long")
    public static void limitarLongitud(TextField field, int maxLen) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (change.getControlNewText().length() <= maxLen) {
                return change;
            }
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    // 5. SOLO DECIMALES (Para precios, peso, dimensiones)
    public static void soloDecimales(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            // Regex: Dígitos opcionales, punto opcional, dígitos opcionales
            if (text.matches("\\d*(\\.\\d*)?")) {
                return change;
            }
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    // 6. VALIDAR FORMATO DE CORREO ELECTRÓNICO
    public static boolean esEmailValido(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Patrón estándar: texto + @ + texto + . + texto (2 a 4 letras)
        String patron = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(patron);
    }
}