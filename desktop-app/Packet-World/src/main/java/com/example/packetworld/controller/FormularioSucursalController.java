package com.example.packetworld.controller;

import com.example.packetworld.model.*;
import com.example.packetworld.service.ApiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

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

    private boolean esEdicion = false;

    @FXML
    public void initialize() {
        cargarEstados();
        configurarListeners();
    }

    private void cargarEstados() {
        List<Estado> estados = ApiService.obtenerEstados();
        cbEstado.setItems(FXCollections.observableArrayList(estados));
    }

    private void configurarListeners() {
        // 1. Cuando cambia Estado -> Cargar Municipios
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbMunicipio.getItems().clear();
            cbCP.getItems().clear();
            cbColonia.getItems().clear();
            cbMunicipio.setDisable(true);
            cbCP.setDisable(true);
            cbColonia.setDisable(true);

            if (newVal != null) {
                List<Municipio> muns = ApiService.obtenerMunicipios(newVal.getIdEstado());
                cbMunicipio.setItems(FXCollections.observableArrayList(muns));
                cbMunicipio.setDisable(false);
            }
        });

        // 2. Cuando cambia Municipio -> Cargar CPs
        cbMunicipio.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbCP.getItems().clear();
            cbColonia.getItems().clear();
            cbCP.setDisable(true);
            cbColonia.setDisable(true);

            if (newVal != null) {
                List<String> cps = ApiService.obtenerCPs(newVal.getIdMunicipio());
                cbCP.setItems(FXCollections.observableArrayList(cps));
                cbCP.setDisable(false);
            }
        });

        // 3. Cuando cambia CP -> Cargar Colonias
        cbCP.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbColonia.getItems().clear();
            cbColonia.setDisable(true);

            if (newVal != null) {
                List<Colonia> cols = ApiService.obtenerColonias(newVal);
                cbColonia.setItems(FXCollections.observableArrayList(cols));
                cbColonia.setDisable(false);
            }
        });
    }

    public void setSucursal(Sucursal s) {
        if (s != null) {
            this.esEdicion = true;
            lblTitulo.setText("Editar Sucursal");

            // 1. Datos Simples
            txtCodigo.setText(s.getCodigoSucursal());
            txtCodigo.setDisable(true);
            txtNombre.setText(s.getNombreCorto());
            txtCalle.setText(s.getCalle());
            txtNumero.setText(s.getNumero());

            // 2. RECONSTRUCCIÓN DE LA CASCADA (La parte mágica) 🪄

            // A. Seleccionar Estado
            // Recorremos los estados cargados en el combo
            for (Estado e : cbEstado.getItems()) {
                if (e.getNombre().equalsIgnoreCase(s.getEstado())) {
                    cbEstado.setValue(e); // Esto dispara el listener y carga Municipios
                    break;
                }
            }

            // B. Seleccionar Municipio
            // (Debemos esperar a que el listener de arriba termine, como es síncrono, podemos seguir)
            if (cbEstado.getValue() != null) {
                for (Municipio m : cbMunicipio.getItems()) {
                    if (m.getNombre().equalsIgnoreCase(s.getCiudad())) {
                        cbMunicipio.setValue(m); // Esto dispara carga de CPs
                        break;
                    }
                }
            }

            // C. Seleccionar Código Postal
            if (cbMunicipio.getValue() != null) {
                for (String cp : cbCP.getItems()) {
                    if (cp.equals(s.getCodigoPostal())) {
                        cbCP.setValue(cp); // Esto dispara carga de Colonias
                        break;
                    }
                }
            }

            // D. Seleccionar Colonia (Aquí comparamos por ID porque Sucursal guarda idColonia)
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
        Sucursal s = new Sucursal();
        s.setCodigoSucursal(txtCodigo.getText());
        s.setNombreCorto(txtNombre.getText());
        s.setEstatus("Activa");
        s.setCalle(txtCalle.getText());
        s.setNumero(txtNumero.getText());

        // Validar Selección de Colonia
        if (cbColonia.getValue() == null) {
            mostrarAlerta("Dirección incompleta", "Selecciona Estado, Municipio y Colonia.");
            return;
        }

        // Llenamos los datos desde los ComboBoxes
        Colonia col = cbColonia.getValue();
        s.setIdColonia(col.getIdColonia());
        s.setNombreColonia(col.getNombre());
        s.setCodigoPostal(col.getCodigoPostal());

        s.setCiudad(cbMunicipio.getValue().getNombre());
        s.setEstado(cbEstado.getValue().getNombre());

        Respuesta r = esEdicion ? ApiService.editarSucursal(s) : ApiService.registrarSucursal(s);

        if (r != null && !r.getError()) {
            cerrar();
        } else {
            mostrarAlerta("Error", "Error al guardar: " + (r!=null ? r.getMensaje() : "Conexión"));
        }
    }

    @FXML public void cerrar() { ((Stage) txtCodigo.getScene().getWindow()).close(); }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setContentText(msg);
        a.show();
    }
}