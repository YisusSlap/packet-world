package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class FormularioColaboradorController {

    @FXML private TextField txtNumPersonal;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPaterno;
    @FXML private TextField txtMaterno;
    @FXML private TextField txtCurp;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtSucursal;
    @FXML private ComboBox<String> cbRol;
    @FXML private Label lblLicencia;
    @FXML private TextField txtLicencia;
    @FXML private Label lblUnidad;
    @FXML private ComboBox<Unidad> cbUnidad;

    @FXML
    public void initialize() {
        // Llenar combo de roles
        cbRol.getItems().addAll("Administrador", "Ejecutivo", "Conductor");

        // LISTENER: Detectar cambio de Rol en tiempo real
        cbRol.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean esConductor = "Conductor".equalsIgnoreCase(newVal);
            mostrarCamposConductor(esConductor);
        });
    }

    @FXML
    public void btnGuardar() {
        Colaborador c = new Colaborador();
        c.setNumeroPersonal(txtNumPersonal.getText());
        c.setNombre(txtNombre.getText());
        c.setApellidoPaterno(txtPaterno.getText());
        c.setApellidoMaterno(txtMaterno.getText());
        c.setCurp(txtCurp.getText());
        c.setCorreoElectronico(txtCorreo.getText());
        c.setContrasenia(txtPassword.getText());
        c.setIdCodigoSucursal(txtSucursal.getText());
        c.setRol(cbRol.getValue());
        c.setEstatus("Activo");

        // VALIDACIONES BÁSICAS
        if (c.getNumeroPersonal().isEmpty() || c.getNombre().isEmpty() || c.getRol() == null) {
            mostrarAlerta("Campos vacíos", "Por favor llena los campos obligatorios.");
            return;
        }

        // LÓGICA DE CONDUCTOR
        if ("Conductor".equalsIgnoreCase(c.getRol())) {
            // Validar licencia obligatoria
            if (txtLicencia.getText().isEmpty()) {
                mostrarAlerta("Faltan datos", "El conductor requiere número de licencia.");
                return;
            }
            c.setNumeroLicencia(txtLicencia.getText());

            // Asignar Unidad (Si seleccionó una)
            Unidad unidadSeleccionada = cbUnidad.getValue();
            if (unidadSeleccionada != null) {
                // Usamos el NII como ID de asignación (o VIN, según tu base de datos)
                c.setIdUnidadAsignada(unidadSeleccionada.getVin());
            }
        } else {
            // Limpiar datos si cambió de rol antes de guardar
            c.setNumeroLicencia(null);
            c.setIdUnidadAsignada(null);
        }

        Respuesta resp;

        if (esEdicion) {
            resp = ApiService.editarColaborador(c); // PUT
        } else {
            resp = ApiService.registrarColaborador(c); // POST
        }

        if (resp != null && !resp.getError()) {
            mostrarAlerta("Éxito", "Colaborador guardado correctamente.");
            cerrarVentana();
        } else {
            mostrarAlerta("Error", "No se pudo guardar: " + (resp != null ? resp.getMensaje() : "Error de conexión"));
        }
    }

    @FXML
    public void btnCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNumPersonal.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarCamposConductor(boolean mostrar) {
        // Activamos/Desactivamos visibilidad y espacio
        lblLicencia.setVisible(mostrar);
        lblLicencia.setManaged(mostrar);
        txtLicencia.setVisible(mostrar);
        txtLicencia.setManaged(mostrar);

        lblUnidad.setVisible(mostrar);
        lblUnidad.setManaged(mostrar);
        cbUnidad.setVisible(mostrar);
        cbUnidad.setManaged(mostrar);

        // Si se muestra y la lista está vacía, cargamos las unidades de la API
        if (mostrar && cbUnidad.getItems().isEmpty()) {
            cargarUnidades();
        }
    }

    private void cargarUnidades() {
        List<Unidad> unidades = ApiService.obtenerUnidades();
        cbUnidad.setItems(FXCollections.observableArrayList(unidades));
    }

    // Variable para controlar si guardamos (POST) o editamos (PUT)
    private boolean esEdicion = false;

    /**
     * Este método es llamado desde la tabla principal.
     * Si colaborador es NULL -> Modo Registro.
     * Si colaborador trae datos -> Modo Edición (Llenamos los campos).
     */
    public void setColaborador(Colaborador colaborador) {
        if (colaborador != null) {
            this.esEdicion = true; // Cambiamos bandera a TRUE

            // Cambiar título de la ventana (opcional)
            // lblTitulo.setText("Editar Colaborador");

            // 1. Llenar campos básicos
            txtNumPersonal.setText(colaborador.getNumeroPersonal());
            txtNumPersonal.setDisable(true); // REGLA DE ORO: No se puede editar la llave primaria (ID)

            txtNombre.setText(colaborador.getNombre());
            txtPaterno.setText(colaborador.getApellidoPaterno());
            txtMaterno.setText(colaborador.getApellidoMaterno());
            txtCurp.setText(colaborador.getCurp());
            txtCorreo.setText(colaborador.getCorreoElectronico());
            txtPassword.setText(colaborador.getContrasenia());
            txtSucursal.setText(colaborador.getIdCodigoSucursal());

            // 2. Seleccionar Rol en el ComboBox
            cbRol.setValue(colaborador.getRol());

            // 3. Lógica Especial para Conductores
            // Si es conductor, forzamos que se muestren los campos ocultos y los llenamos
            if ("Conductor".equalsIgnoreCase(colaborador.getRol())) {
                mostrarCamposConductor(true); // Método que ya creaste antes
                txtLicencia.setText(colaborador.getNumeroLicencia());

                // Truco para seleccionar la Unidad correcta en el ComboBox:
                // El colaborador tiene un String (ID), pero el combo tiene Objetos Unidad.
                // Buscamos cuál coincide y lo seleccionamos.
                String idUnidad = colaborador.getIdUnidadAsignada();
                if (idUnidad != null && !idUnidad.isEmpty()) {
                    for (Unidad u : cbUnidad.getItems()) {
                        // Comparamos por NII o VIN (según qué guardes como ID)
                        if (u.getNii().equals(idUnidad)) {
                            cbUnidad.setValue(u);
                            break;
                        }
                    }
                }
            }
        }
    }
}