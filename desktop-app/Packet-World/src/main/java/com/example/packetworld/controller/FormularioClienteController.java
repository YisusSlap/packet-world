package com.example.packetworld.controller;

import com.example.packetworld.model.*;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // UX Moderna
import com.example.packetworld.util.Validaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;

/**
 * Controlador para Registro y Edición de Clientes.
 * Incluye validaciones de formato y lógica de combos dependientes (Cascada de Dirección).
 */
public class FormularioClienteController {
    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre, txtPaterno, txtMaterno, txtTelefono, txtCorreo;
    @FXML private TextField txtCalle, txtNumero;

    // Combos de Dirección (Estado -> Municipio -> CP -> Colonia)
    @FXML private ComboBox<Estado> cbEstado;
    @FXML private ComboBox<Municipio> cbMunicipio;
    @FXML private ComboBox<String> cbCP;
    @FXML private ComboBox<Colonia> cbColonia;

    private Integer idClienteEdicion = null; // Si es null, es nuevo registro. Si tiene ID, es edición.

    @FXML
    public void initialize() {
        // Cargar lista inicial de Estados
        cbEstado.setItems(FXCollections.observableArrayList(ApiService.obtenerEstados()));

        // Configurar la lógica de cascada (que al elegir Estado cargue Municipios, etc.)
        configurarListeners();

        // --- VALIDACIONES DE ENTRADA (UX) ---
        // Evitamos que el usuario escriba números en el nombre o letras en el teléfono
        Validaciones.soloLetras(txtNombre);
        Validaciones.soloLetras(txtPaterno);
        Validaciones.soloLetras(txtMaterno);
        Validaciones.limitarLongitud(txtNombre, 50);
        Validaciones.limitarLongitud(txtPaterno, 50);
        Validaciones.limitarLongitud(txtMaterno, 50);

        Validaciones.soloNumerosLimitado(txtTelefono, 10); // Máx 10 dígitos

        Validaciones.limitarLongitud(txtCorreo, 100);
        Validaciones.limitarLongitud(txtCalle, 100);
        Validaciones.limitarLongitud(txtNumero, 10);
    }

    /**
     * Configura los triggers para limpiar y recargar los combos dependientes.
     */
    private void configurarListeners() {
        // 1. Cambio de Estado -> Cargar Municipios
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbMunicipio.getItems().clear(); cbCP.getItems().clear(); cbColonia.getItems().clear();
            cbMunicipio.setDisable(true); cbCP.setDisable(true); cbColonia.setDisable(true);

            if (newVal != null) {
                cbMunicipio.setItems(FXCollections.observableArrayList(ApiService.obtenerMunicipios(newVal.getIdEstado())));
                cbMunicipio.setDisable(false);
            }
        });

        // 2. Cambio de Municipio -> Cargar CPs
        cbMunicipio.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbCP.getItems().clear(); cbColonia.getItems().clear();
            cbCP.setDisable(true); cbColonia.setDisable(true);

            if (newVal != null) {
                cbCP.setItems(FXCollections.observableArrayList(ApiService.obtenerCPs(newVal.getIdMunicipio())));
                cbCP.setDisable(false);
            }
        });

        // 3. Cambio de CP -> Cargar Colonias
        cbCP.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbColonia.getItems().clear(); cbColonia.setDisable(true);

            if (newVal != null) {
                cbColonia.setItems(FXCollections.observableArrayList(ApiService.obtenerColonias(newVal)));
                cbColonia.setDisable(false);
            }
        });
    }

    /**
     * Prepara el formulario para EDICIÓN cargando los datos de un cliente existente.
     * Reconstruye la cascada de dirección paso a paso.
     */
    public void setCliente(Cliente c) {
        if (c != null) {
            this.idClienteEdicion = c.getIdCliente();
            lblTitulo.setText("Editar Cliente");

            // Llenar datos básicos
            txtNombre.setText(c.getNombre());
            txtPaterno.setText(c.getApellidoPaterno());
            txtMaterno.setText(c.getApellidoMaterno());
            txtTelefono.setText(c.getTelefono());
            txtCorreo.setText(c.getCorreoElectronico());
            txtCalle.setText(c.getCalle());
            txtNumero.setText(c.getNumero());

            // --- RECONSTRUCCIÓN DE CASCADA ---
            // Truco: Al setear el valor del combo, se disparan los listeners automáticamente.

            // 1. Estado
            for (Estado e : cbEstado.getItems()) {
                if (e.getNombre().equalsIgnoreCase(c.getEstado())) {
                    cbEstado.setValue(e); // Listener carga municipios
                    break;
                }
            }

            // 2. Municipio (Esperamos a que el listener anterior termine, en JavaFX es síncrono)
            if (cbEstado.getValue() != null) {
                for (Municipio m : cbMunicipio.getItems()) {
                    if (m.getNombre().equalsIgnoreCase(c.getCiudad())) { // 'Ciudad' en BD es Municipio
                        cbMunicipio.setValue(m); // Listener carga CPs
                        break;
                    }
                }
            }

            // 3. Código Postal
            if (cbMunicipio.getValue() != null) {
                for (String cp : cbCP.getItems()) {
                    if (cp.equals(c.getCodigoPostal())) {
                        cbCP.setValue(cp); // Listener carga Colonias
                        break;
                    }
                }
            }

            // 4. Colonia (Dato final)
            if (cbCP.getValue() != null) {
                for (Colonia col : cbColonia.getItems()) {
                    if (col.getIdColonia().equals(c.getIdColonia())) { // Comparamos por ID único
                        cbColonia.setValue(col);
                        break;
                    }
                }
            }
        }
    }

    @FXML public void guardar() {
        // Objeto a enviar
        Cliente c = new Cliente();
        c.setIdCliente(idClienteEdicion);
        c.setNombre(txtNombre.getText());
        c.setApellidoPaterno(txtPaterno.getText());
        c.setApellidoMaterno(txtMaterno.getText());
        c.setTelefono(txtTelefono.getText());
        c.setCorreoElectronico(txtCorreo.getText());
        c.setCalle(txtCalle.getText());
        c.setNumero(txtNumero.getText());
        c.setEstatus("Activo");

        // --- VALIDACIONES DE NEGOCIO ---

        // 1. Dirección completa
        if (cbColonia.getValue() == null) {
            mostrarAlerta("Faltan datos", "Por favor selecciona la colonia y completa la dirección.");
            return;
        }

        // 2. Campos obligatorios
        if (txtNombre.getText().isEmpty() || txtCorreo.getText().isEmpty()) {
            mostrarAlerta("Campos vacíos", "El Nombre y el Correo son obligatorios.");
            return;
        }

        // 3. Formato de Correo
        if (!Validaciones.esEmailValido(txtCorreo.getText())) {
            mostrarAlerta("Correo Inválido", "El formato del correo es incorrecto (ej: usuario@dominio.com).");
            return;
        }

        // Llenar datos de dirección desde los objetos seleccionados
        Colonia col = cbColonia.getValue();
        c.setIdColonia(col.getIdColonia());
        c.setNombreColonia(col.getNombre());
        c.setCodigoPostal(col.getCodigoPostal());
        c.setCiudad(cbMunicipio.getValue().getNombre());
        c.setEstado(cbEstado.getValue().getNombre());

        // Enviar a API (Crear o Editar)
        Respuesta r = (idClienteEdicion != null) ? ApiService.editarCliente(c) : ApiService.registrarCliente(c);

        if (r != null && !r.getError()) {
            // ÉXITO: Notificación bonita
            Notificacion.mostrar("Operación Exitosa", "Cliente guardado correctamente.", Notificacion.EXITO);
            cerrar();
        } else {
            // ERROR: Alerta fea
            mostrarAlerta("Error", "No se pudo guardar: " + (r != null ? r.getMensaje() : "Error desconocido"));
        }
    }

    @FXML public void cerrar() { ((Stage) txtNombre.getScene().getWindow()).close(); }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}