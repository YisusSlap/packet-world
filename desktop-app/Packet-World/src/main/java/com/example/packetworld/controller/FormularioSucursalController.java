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
 * Controlador para gestionar Sucursales (Tiendas físicas).
 * Incluye lógica de dirección en cascada (Estado > Municipio > CP > Colonia).
 */
public class FormularioSucursalController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;

    // Combos de Dirección
    @FXML private ComboBox<Estado> cbEstado;
    @FXML private ComboBox<Municipio> cbMunicipio;
    @FXML private ComboBox<String> cbCP;
    @FXML private ComboBox<Colonia> cbColonia;

    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;

    private boolean esEdicion = false; // Bandera de estado

    @FXML
    public void initialize() {
        cargarEstados();
        configurarListeners();

        // --- VALIDACIONES DE ENTRADA ---
        // Código Sucursal: Máximo 10 caracteres (ej. SUC-XAL-01)
        Validaciones.limitarLongitud(txtCodigo, 10);

        // Nombre corto: Máx 50
        Validaciones.limitarLongitud(txtNombre, 50);

        // Dirección
        Validaciones.limitarLongitud(txtCalle, 100);
        Validaciones.limitarLongitud(txtNumero, 10);
    }

    private void cargarEstados() {
        List<Estado> estados = ApiService.obtenerEstados();
        cbEstado.setItems(FXCollections.observableArrayList(estados));
    }

    /**
     * Configura los triggers para limpiar y recargar los combos dependientes.
     */
    private void configurarListeners() {
        // 1. Cuando cambia Estado -> Cargar Municipios
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Limpieza en cascada
            cbMunicipio.getItems().clear(); cbCP.getItems().clear(); cbColonia.getItems().clear();
            cbMunicipio.setDisable(true); cbCP.setDisable(true); cbColonia.setDisable(true);

            if (newVal != null) {
                List<Municipio> muns = ApiService.obtenerMunicipios(newVal.getIdEstado());
                cbMunicipio.setItems(FXCollections.observableArrayList(muns));
                cbMunicipio.setDisable(false);
            }
        });

        // 2. Cuando cambia Municipio -> Cargar CPs
        cbMunicipio.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbCP.getItems().clear(); cbColonia.getItems().clear();
            cbCP.setDisable(true); cbColonia.setDisable(true);

            if (newVal != null) {
                List<String> cps = ApiService.obtenerCPs(newVal.getIdMunicipio());
                cbCP.setItems(FXCollections.observableArrayList(cps));
                cbCP.setDisable(false);
            }
        });

        // 3. Cuando cambia CP -> Cargar Colonias
        cbCP.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbColonia.getItems().clear(); cbColonia.setDisable(true);

            if (newVal != null) {
                List<Colonia> cols = ApiService.obtenerColonias(newVal);
                cbColonia.setItems(FXCollections.observableArrayList(cols));
                cbColonia.setDisable(false);
            }
        });
    }

    /**
     * Prepara el formulario para EDICIÓN cargando datos existentes.
     * Reconstruye la selección de combos paso a paso.
     */
    public void setSucursal(Sucursal s) {
        if (s != null) {
            this.esEdicion = true;
            lblTitulo.setText("Editar Sucursal");

            // 1. Datos Simples
            txtCodigo.setText(s.getCodigoSucursal());
            txtCodigo.setDisable(true); // Código es llave primaria, no editable
            txtNombre.setText(s.getNombreCorto());
            txtCalle.setText(s.getCalle());
            txtNumero.setText(s.getNumero());

            // 2. RECONSTRUCCIÓN DE LA CASCADA 🪄

            // A. Estado
            for (Estado e : cbEstado.getItems()) {
                if (e.getNombre().equalsIgnoreCase(s.getEstado())) {
                    cbEstado.setValue(e); // Dispara listener -> Carga Municipios
                    break;
                }
            }

            // B. Municipio
            if (cbEstado.getValue() != null) {
                for (Municipio m : cbMunicipio.getItems()) {
                    if (m.getNombre().equalsIgnoreCase(s.getCiudad())) {
                        cbMunicipio.setValue(m); // Dispara listener -> Carga CPs
                        break;
                    }
                }
            }

            // C. Código Postal
            if (cbMunicipio.getValue() != null) {
                for (String cp : cbCP.getItems()) {
                    if (cp.equals(s.getCodigoPostal())) {
                        cbCP.setValue(cp); // Dispara listener -> Carga Colonias
                        break;
                    }
                }
            }

            // D. Colonia (Destino final)
            if (cbCP.getValue() != null) {
                for (Colonia col : cbColonia.getItems()) {
                    if (col.getIdColonia().equals(s.getIdColonia())) {
                        cbColonia.setValue(col);
                        break;
                    }
                }
            }
        }
    }

    @FXML public void guardar() {
        // Validaciones previas
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty()) {
            mostrarAlerta("Campos vacíos", "El código y el nombre son obligatorios.");
            return;
        }

        if (cbColonia.getValue() == null) {
            mostrarAlerta("Dirección incompleta", "Debes seleccionar hasta la Colonia.");
            return;
        }

        // Construir objeto
        Sucursal s = new Sucursal();
        s.setCodigoSucursal(txtCodigo.getText());
        s.setNombreCorto(txtNombre.getText());
        s.setEstatus("Activa");
        s.setCalle(txtCalle.getText());
        s.setNumero(txtNumero.getText());

        // Datos desde Combos
        Colonia col = cbColonia.getValue();
        s.setIdColonia(col.getIdColonia());
        s.setNombreColonia(col.getNombre());
        s.setCodigoPostal(col.getCodigoPostal());
        s.setCiudad(cbMunicipio.getValue().getNombre());
        s.setEstado(cbEstado.getValue().getNombre());

        // Enviar a API
        Respuesta r = esEdicion ? ApiService.editarSucursal(s) : ApiService.registrarSucursal(s);

        if (r != null && !r.getError()) {
            // ÉXITO: Toast Verde
            Notificacion.mostrar("Operación Exitosa", "La sucursal se guardó correctamente.", Notificacion.EXITO);
            cerrar();
        } else {
            // ERROR: Alerta Modal
            mostrarAlerta("Error", "No se pudo guardar: " + (r!=null ? r.getMensaje() : "Error de conexión"));
        }
    }

    @FXML public void cerrar() { ((Stage) txtCodigo.getScene().getWindow()).close(); }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setContentText(msg);
        a.showAndWait();
    }
}