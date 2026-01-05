package com.example.packetworld.controller;

import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // Importar notificaciones bonitas
import com.example.packetworld.util.Validaciones;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controlador para la ventana de Registro/Edición de Unidades de Transporte.
 * Maneja la lógica de validación, generación de NII y comunicación con la API.
 */
public class FormularioUnidadController {

    // Referencias a los elementos visuales del FXML (Cajas de texto, Combos, etc.)
    @FXML private Label lblTitulo;
    @FXML private TextField txtMarca;
    @FXML private TextField txtModelo;
    @FXML private TextField txtAnio;
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtVin;
    @FXML private TextField txtNii; // Número de Identificación Interno (Automático)

    private boolean esEdicion = false; // Bandera para saber si estamos creando o editando

    /**
     * Método que se ejecuta automáticamente al abrir la ventana.
     * Aquí configuramos las listas desplegables y las reglas de validación.
     */
    @FXML
    public void initialize() {
        // 1. Llenar el combo de Tipos de Unidad
        cbTipo.getItems().addAll("Gasolina", "Diesel", "Hibrida", "Eléctrica");

        // 2. LOGICA DE NEGOCIO: Generación Automática de NII
        // El NII se forma combinando el Año + los primeros 4 dígitos del VIN.
        // Agregamos "escuchas" (listeners) para que se recalcule en tiempo real.
        txtAnio.textProperty().addListener((obs, oldVal, newVal) -> calcularNII());
        txtVin.textProperty().addListener((obs, oldVal, newVal) -> calcularNII());

        // 3. Validaciones de Entrada (Para evitar errores de usuario)
        Validaciones.soloNumerosLimitado(txtAnio, 4); // Año: máx 4 dígitos
        Validaciones.limitarLongitud(txtVin, 17);     // VIN: máx 17 caracteres
        Validaciones.limitarLongitud(txtMarca, 50);
        Validaciones.limitarLongitud(txtModelo, 50);
    }

    /**
     * Calcula el NII en base a las reglas de negocio.
     * Regla: Año + Primeros 4 caracteres del VIN.
     */
    private void calcularNII() {
        String anio = txtAnio.getText().trim();
        String vin = txtVin.getText().trim();

        // Solo generamos si hay año y al menos 4 caracteres de VIN
        if (!anio.isEmpty() && vin.length() >= 4) {
            String niiGenerado = anio + vin.substring(0, 4);
            txtNii.setText(niiGenerado.toUpperCase());
        } else {
            txtNii.setText(""); // Si faltan datos, limpiamos el campo
        }
    }

    /**
     * Método para recibir una unidad existente desde la pantalla anterior.
     * Si se llama a este método, el formulario entra en modo "Edición".
     */
    public void setUnidad(Unidad unidad) {
        if (unidad != null) {
            esEdicion = true;
            lblTitulo.setText("Editar Unidad"); // Cambiamos el título visual

            // Rellenamos los campos con los datos existentes
            txtMarca.setText(unidad.getMarca());
            txtModelo.setText(unidad.getModelo());
            txtAnio.setText(String.valueOf(unidad.getAnio()));
            cbTipo.setValue(unidad.getTipoUnidad());
            txtVin.setText(unidad.getVin());
            txtNii.setText(unidad.getNii());

            // REGLA DE NEGOCIO: El VIN es la llave primaria, no se puede editar una vez creado.
            txtVin.setDisable(true);
        }
    }

    /**
     * Acción del botón "Guardar". Recoge datos, valida y envía a la API.
     */
    @FXML
    public void btnGuardar() {
        // 1. Crear objeto Unidad y llenarlo con los datos del formulario
        Unidad u = new Unidad();
        u.setMarca(txtMarca.getText());
        u.setModelo(txtModelo.getText());
        u.setTipoUnidad(cbTipo.getValue());
        u.setVin(txtVin.getText());
        u.setNii(txtNii.getText());
        u.setEstatus("activo"); // Por defecto siempre nacen activas

        // Validación de Año (puede lanzar error si no es número)
        try {
            u.setAnio(Integer.parseInt(txtAnio.getText()));
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El año debe ser un número válido.");
            return;
        }

        // 2. Validar campos obligatorios
        if (u.getVin().isEmpty() || u.getMarca().isEmpty() || u.getModelo().isEmpty()) {
            mostrarAlerta("Faltan datos", "Por favor llena todos los campos.");
            return;
        }

        // 3. Enviar a la API (Backend)
        Respuesta resp;
        if (esEdicion) {
            resp = ApiService.editarUnidad(u);
        } else {
            resp = ApiService.registrarUnidad(u);
        }

        // 4. Manejar la respuesta del servidor
        if (resp != null && !resp.getError()) {
            // ÉXITO: Usamos Notificación Toast (No interrumpe)
            Notificacion.mostrar("¡Operación Exitosa!", "La unidad se guardó correctamente.", Notificacion.EXITO);
            cerrarVentana();
        } else {
            // ERROR: Usamos Alerta Modal (El usuario debe leer el error)
            mostrarAlerta("Error", "No se pudo guardar: " + (resp != null ? resp.getMensaje() : "Error de conexión"));
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
        Alert alert = new Alert(Alert.AlertType.WARNING); // Warning es mejor para errores de validación
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}