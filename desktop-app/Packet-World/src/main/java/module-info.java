module com.example.packetworld {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires unirest.java;
    requires javafx.web;
    requires jdk.jsobject; // A veces necesario para la comunicación JS

    requires javafx.swing;

    // A veces necesario para dependencias internas de Unirest
    requires java.sql;

    // 1. Permite a JavaFX cargar la clase principal (App)
    opens com.example.packetworld to javafx.fxml;

    // 2. ¡ESTA FALTABA! Permite a JavaFX ver tus controladores
    opens com.example.packetworld.controller to javafx.fxml;

    // 3. ¡ESTA FALTABA! Permite a Gson rellenar tus datos (LoginResponse, Colaborador)
    opens com.example.packetworld.model to com.google.gson;




    exports com.example.packetworld;
}