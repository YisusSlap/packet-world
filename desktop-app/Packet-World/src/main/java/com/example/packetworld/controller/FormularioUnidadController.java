package com.example.packetworld.controller;

import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion;
import com.example.packetworld.util.Validaciones;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.Year;
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

        // --- BLINDAJE DE INPUTS ---
        Validaciones.soloNumerosLimitado(txtAnio, 4);
        Validaciones.limitarLongitud(txtVin, 17);
        // Permitimos letras y números en marca/modelo (ej. Mazda 3)
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

            // En edición, el VIN (ID) y el Año (base del NII) no deberían cambiar
            txtVin.setDisable(true);
            txtAnio.setDisable(true);
        }
    }

    @FXML
    public void btnGuardar() {
        // 1. Obtener datos limpios (sin espacios al inicio/final)
        String marca = txtMarca.getText().trim();
        String modelo = txtModelo.getText().trim();
        String anioStr = txtAnio.getText().trim();
        String vin = txtVin.getText().trim();
        String tipo = cbTipo.getValue();

        // 2. VALIDACIÓN DE CAMPOS VACÍOS (Aquí fallaba antes)
        if (marca.isEmpty() || modelo.isEmpty() || anioStr.isEmpty() || vin.isEmpty() || tipo == null) {
            mostrarAlerta("Campos incompletos", "Por favor, llene todos los campos (Marca, Modelo, Tipo, Año, VIN).");
            return;
        }

        // 3. VALIDACIÓN DE VIN (Longitud exacta)
        if (vin.length() != 17) {
            mostrarAlerta("VIN Inválido", "El VIN debe tener exactamente 17 caracteres.");
            return;
        }

        int anioInt;
        try {
            // 4. VALIDACIÓN DE AÑO LÓGICO
            anioInt = Integer.parseInt(anioStr);
            int anioActual = Year.now().getValue();

            // Rango razonable: Desde 1980 hasta el año actual + 1 (modelos del siguiente año)
            if (anioInt < 1980 || anioInt > (anioActual + 1)) {
                mostrarAlerta("Año Inválido", "El año del vehículo debe ser realista (1980 - " + (anioActual + 1) + ").");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El año debe ser un número válido.");
            return;
        }

        // --- Crear Objeto ---
        Unidad u = new Unidad();
        u.setMarca(marca);
        u.setModelo(modelo);
        u.setTipoUnidad(tipo);
        u.setVin(vin.toUpperCase()); // VIN siempre mayúsculas
        u.setNii(txtNii.getText());
        u.setAnio(anioInt);
        u.setEstatus("activo");

        // --- VALIDACIÓN DE NII DUPLICADO (Solo si es nuevo) ---
        if (!esEdicion) {
            if (existeNiiEnBaseDeDatos(u.getNii())) {
                mostrarAlerta("Conflicto de NII",
                        "El NII generado (" + u.getNii() + ") ya existe en el sistema.\n\n" +
                                "Debido a la regla de negocio (Año + 4 dígitos VIN), esta unidad colisiona con otra.\n" +
                                "No es posible registrarla.");
                return;
            }
        }

        // --- Enviar a API ---
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
            String mensajeError = resp != null ? resp.getMensaje() : "Error desconocido";
            if (mensajeError.toLowerCase().contains("duplicate")) {
                mensajeError = "Ya existe una unidad con ese VIN registrado.";
            }
            mostrarAlerta("Error al guardar", mensajeError);
        }
    }

    private boolean existeNiiEnBaseDeDatos(String niiGenerado) {
        List<Unidad> existentes = ApiService.obtenerUnidades();
        if (existentes != null) {
            for (Unidad unit : existentes) {
                if (unit.getNii() != null && unit.getNii().equalsIgnoreCase(niiGenerado)) {
                    return true;
                }
            }
        }
        return false;
    }

    @FXML public void btnCancelar() { cerrarVentana(); }

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