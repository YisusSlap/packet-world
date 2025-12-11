package com.example.packetworld.controller;

import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FormularioUnidadController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtMarca;
    @FXML private TextField txtModelo;
    @FXML private TextField txtAnio;
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtVin;
    @FXML private TextField txtNii;

    private boolean esEdicion = false;

    @FXML
    public void initialize() {
        cbTipo.getItems().addAll("Gasolina", "Diesel", "Hibrida", "Eléctrica");

        // LISTENERS PARA NII AUTOMÁTICO
        // Cada vez que escriban en Año o VIN, recalculamos
        txtAnio.textProperty().addListener((obs, oldVal, newVal) -> calcularNII());
        txtVin.textProperty().addListener((obs, oldVal, newVal) -> calcularNII());
    }

    private void calcularNII() {
        // Regla: Año + Primeros 4 del VIN
        String anio = txtAnio.getText().trim();
        String vin = txtVin.getText().trim();

        if (!anio.isEmpty() && vin.length() >= 4) {
            String niiGenerado = anio + vin.substring(0, 4);
            txtNii.setText(niiGenerado.toUpperCase());
        } else {
            txtNii.setText("");
        }
    }

    // Este método lo llama la ventana anterior si es EDICIÓN
    public void setUnidad(Unidad unidad) {
        if (unidad != null) {
            esEdicion = true;
            lblTitulo.setText("Editar Unidad");

            txtMarca.setText(unidad.getMarca());
            txtModelo.setText(unidad.getModelo());
            txtAnio.setText(String.valueOf(unidad.getAnio()));
            cbTipo.setValue(unidad.getTipoUnidad());
            txtVin.setText(unidad.getVin());
            txtNii.setText(unidad.getNii());

            // REGLA: En edición NO se puede cambiar el VIN
            txtVin.setDisable(true);
            // El NII tampoco debería cambiar si el VIN no cambia,
            // aunque si cambias el año, el NII cambiará (lo cual es correcto según la regla).
        }
    }

    @FXML
    public void btnGuardar() {
        // Recoger datos
        Unidad u = new Unidad();
        u.setMarca(txtMarca.getText());
        u.setModelo(txtModelo.getText());
        u.setTipoUnidad(cbTipo.getValue());
        u.setVin(txtVin.getText());
        u.setNii(txtNii.getText());
        u.setEstatus("Disponble"); // Por defecto al crear

        try {
            u.setAnio(Integer.parseInt(txtAnio.getText()));
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El año debe ser un número válido.");
            return;
        }

        // Validaciones
        if (u.getVin().isEmpty() || u.getMarca().isEmpty() || u.getModelo().isEmpty()) {
            mostrarAlerta("Faltan datos", "Por favor llena todos los campos.");
            return;
        }

        // Enviar a API
        Respuesta resp;
        if (esEdicion) {
            // En tu API editar requiere el objeto completo, el VIN es la llave
            resp = ApiService.editarUnidad(u);
        } else {
            resp = ApiService.registrarUnidad(u);
        }

        if (resp != null && !resp.getError()) {
            mostrarAlerta("Éxito", "Unidad guardada correctamente.");
            cerrarVentana();
        } else {
            mostrarAlerta("Error", "Error al guardar: " + (resp != null ? resp.getMensaje() : "Desconocido"));
        }
    }

    @FXML
    public void btnCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtMarca.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}