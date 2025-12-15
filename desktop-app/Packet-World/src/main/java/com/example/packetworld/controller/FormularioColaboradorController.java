package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Sucursal;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Validaciones;
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
    @FXML private ComboBox<Sucursal> cbSucursal;
    @FXML private ComboBox<String> cbRol;
    @FXML private Label lblLicencia;
    @FXML private TextField txtLicencia;
    @FXML private Label lblUnidad;
    @FXML private ComboBox<Unidad> cbUnidad;

    @FXML
    public void initialize() {
        // Llenar combo de roles
        cbRol.getItems().addAll("Administrador", "Ejecutivo de tienda", "Conductor");
        cargarSucursales();

        // LISTENER: Detectar cambio de Rol en tiempo real
        cbRol.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean esConductor = "Conductor".equalsIgnoreCase(newVal);
            mostrarCamposConductor(esConductor);
        });

        // No. Personal: Solo números y máximo 20 caracteres (según tu BD VARCHAR(20))
        //Validaciones.soloNumerosLimitado(txtNumPersonal, 20);

        // Nombres: Solo letras
        Validaciones.soloLetras(txtNombre);
        Validaciones.soloLetras(txtPaterno);
        Validaciones.soloLetras(txtMaterno);

        // CURP: Limitar a 18 caracteres
        Validaciones.limitarLongitud(txtCurp, 18);

        // Licencia: Limitar a 20 caracteres (según tu BD)
        Validaciones.limitarLongitud(txtLicencia, 20);

        // Correo y Password: Solo limitar longitud para no desbordar BD
        Validaciones.limitarLongitud(txtCorreo, 100);
        Validaciones.limitarLongitud(txtPassword, 50);
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

        // --- CAMBIO AQUÍ: SUCURSAL ---
        Sucursal sucursalSeleccionada = cbSucursal.getValue();
        if (sucursalSeleccionada != null) {
            // Guardamos solo el ID (código) de la sucursal
            c.setIdCodigoSucursal(sucursalSeleccionada.getCodigoSucursal());
        } else {
            mostrarAlerta("Faltan datos", "Debes seleccionar una sucursal obligatoriamente.");
            return;
        }
        // -----------------------------

        c.setRol(cbRol.getValue());
        c.setEstatus("Activo");

        // VALIDACIONES BÁSICAS
        if (c.getNumeroPersonal().isEmpty() || c.getNombre().isEmpty() || c.getRol() == null) {
            mostrarAlerta("Campos vacíos", "Por favor llena los campos obligatorios.");
            return;
        }

        if (!txtCorreo.getText().isEmpty() && !Validaciones.esEmailValido(txtCorreo.getText())) {
            mostrarAlerta("Correo inválido", "Por favor ingrese un correo válido.");
            return;
        }

        // LÓGICA DE CONDUCTOR
        if ("Conductor".equalsIgnoreCase(c.getRol())) {
            if (txtLicencia.getText().isEmpty()) {
                mostrarAlerta("Faltan datos", "El conductor requiere número de licencia.");
                return;
            }
            c.setNumeroLicencia(txtLicencia.getText());

            Unidad unidadSeleccionada = cbUnidad.getValue();
            if (unidadSeleccionada != null) {
                c.setIdUnidadAsignada(unidadSeleccionada.getVin());
            }
        } else {
            c.setNumeroLicencia(null);
            c.setIdUnidadAsignada(null);
        }

        // ENVÍO A API
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
            this.esEdicion = true;

            // 1. Llenar campos básicos
            txtNumPersonal.setText(colaborador.getNumeroPersonal());
            txtNumPersonal.setDisable(true); // ID no editable

            txtNombre.setText(colaborador.getNombre());
            txtPaterno.setText(colaborador.getApellidoPaterno());
            txtMaterno.setText(colaborador.getApellidoMaterno());
            txtCurp.setText(colaborador.getCurp());
            txtCorreo.setText(colaborador.getCorreoElectronico());
            txtPassword.setText(colaborador.getContrasenia());

            // --- CAMBIO AQUÍ: SUCURSAL ---
            // Buscamos la sucursal en el combo que coincida con el ID guardado
            String idSucursalGuardada = colaborador.getIdCodigoSucursal();
            if (idSucursalGuardada != null) {
                for (Sucursal s : cbSucursal.getItems()) {
                    // Comparamos el código de la sucursal
                    if (s.getCodigoSucursal().equals(idSucursalGuardada)) {
                        cbSucursal.setValue(s);
                        break;
                    }
                }
            }
            // -----------------------------

            // 2. Seleccionar Rol
            cbRol.setValue(colaborador.getRol());

            // 3. Lógica Especial para Conductores
            if ("Conductor".equalsIgnoreCase(colaborador.getRol())) {
                mostrarCamposConductor(true);
                txtLicencia.setText(colaborador.getNumeroLicencia());

                // Seleccionar Unidad
                String idUnidad = colaborador.getIdUnidadAsignada();
                if (idUnidad != null && !idUnidad.isEmpty()) {
                    for (Unidad u : cbUnidad.getItems()) {
                        if (u.getVin().equals(idUnidad)) { // O u.getNii() según uses
                            cbUnidad.setValue(u);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void cargarSucursales() {
        List<Sucursal> lista = ApiService.obtenerSucursales();
        cbSucursal.setItems(FXCollections.observableArrayList(lista));
    }
}