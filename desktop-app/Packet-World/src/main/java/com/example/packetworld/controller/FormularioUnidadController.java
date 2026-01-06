package com.example.packetworld.controller;

import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion;
import com.example.packetworld.util.Validaciones;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;

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

        // Listeners para calcular NII en tiempo real
        txtAnio.textProperty().addListener((obs, oldVal, newVal) -> calcularNII());
        txtVin.textProperty().addListener((obs, oldVal, newVal) -> calcularNII());

        Validaciones.soloNumerosLimitado(txtAnio, 4);
        Validaciones.limitarLongitud(txtVin, 17);
        Validaciones.limitarLongitud(txtMarca, 50);
        Validaciones.limitarLongitud(txtModelo, 50);
    }

    private void calcularNII() {
        String anio = txtAnio.getText().trim();
        String vin = txtVin.getText().trim();

        if (!anio.isEmpty() && vin.length() >= 4) {
            String niiGenerado = anio + vin.substring(0, 4);
            txtNii.setText(niiGenerado.toUpperCase());
        } else {
            txtNii.setText("");
        }
    }

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

            // En edición, el VIN (ID) y el NII no deberían cambiar
            txtVin.setDisable(true);
            txtAnio.setDisable(true); // Bloqueamos año para no alterar el NII ya generado
        }
    }

    @FXML
    public void btnGuardar() {
        Unidad u = new Unidad();
        u.setMarca(txtMarca.getText());
        u.setModelo(txtModelo.getText());
        u.setTipoUnidad(cbTipo.getValue());
        u.setVin(txtVin.getText());
        u.setNii(txtNii.getText());
        u.setEstatus("activo");

        try {
            u.setAnio(Integer.parseInt(txtAnio.getText()));
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El año debe ser un número válido.");
            return;
        }

        if (u.getVin().isEmpty() || u.getMarca().isEmpty() || u.getModelo().isEmpty()) {
            mostrarAlerta("Faltan datos", "Por favor llena todos los campos.");
            return;
        }

        // --- VALIDACIÓN DE NII DUPLICADO (Solo si es nuevo) ---
        if (!esEdicion) {
            if (existeNiiEnBaseDeDatos(u.getNii())) {
                mostrarAlerta("Conflicto de NII",
                        "El NII generado (" + u.getNii() + ") ya existe en el sistema.\n\n" +
                                "Debido a la regla de negocio (Año + 4 dígitos VIN), esta unidad colisiona con otra.\n" +
                                "No es posible registrarla con este VIN/Año.");
                return; // Detenemos el guardado
            }
        }
        // -----------------------------------------------------

        Respuesta resp;
        if (esEdicion) {
            resp = ApiService.editarUnidad(u);
        } else {
            resp = ApiService.registrarUnidad(u);
        }

        if (resp != null && !resp.getError()) {
            Notificacion.mostrar("Éxito", "Unidad guardada correctamente.", Notificacion.EXITO);
            cerrarVentana();
        } else {
            // Si aun así falla (por ejemplo, duplicado de VIN completo), mostramos el error de la BD
            String mensajeError = resp != null ? resp.getMensaje() : "Error desconocido";

            // Si el mensaje de la BD dice "Duplicate entry", lo traducimos
            if (mensajeError.toLowerCase().contains("duplicate")) {
                mensajeError = "Ya existe una unidad con ese VIN o NII registrado.";
            }

            mostrarAlerta("Error al guardar", mensajeError);
        }
    }

    /**
     * Método auxiliar que descarga las unidades y verifica si el NII ya está ocupado.
     * Es una validación preventiva en el cliente (Frontend).
     */
    private boolean existeNiiEnBaseDeDatos(String niiGenerado) {
        // Descargamos la lista actual (no es lo más óptimo en Big Data, pero perfecto para este proyecto)
        List<Unidad> existentes = ApiService.obtenerUnidades();

        if (existentes != null) {
            for (Unidad unit : existentes) {
                if (unit.getNii() != null && unit.getNii().equalsIgnoreCase(niiGenerado)) {
                    return true; // Encontrado Es un duplicado
                }
            }
        }
        return false;
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
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}