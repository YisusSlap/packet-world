package com.example.packetworld.controller;

import com.example.packetworld.model.*;
import com.example.packetworld.service.ApiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;

public class FormularioClienteController {
    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre, txtPaterno, txtMaterno, txtTelefono, txtCorreo;
    @FXML private TextField txtCalle, txtNumero;

    // Combos Dirección
    @FXML private ComboBox<Estado> cbEstado;
    @FXML private ComboBox<Municipio> cbMunicipio;
    @FXML private ComboBox<String> cbCP;
    @FXML private ComboBox<Colonia> cbColonia;

    private Integer idClienteEdicion = null; // Para saber si editamos

    @FXML
    public void initialize() {
        cbEstado.setItems(FXCollections.observableArrayList(ApiService.obtenerEstados()));
        configurarListeners();
    }

    private void configurarListeners() {
        // Mismos listeners en cascada que en Sucursales
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbMunicipio.getItems().clear(); cbCP.getItems().clear(); cbColonia.getItems().clear();
            cbMunicipio.setDisable(true); cbCP.setDisable(true); cbColonia.setDisable(true);
            if (newVal != null) {
                cbMunicipio.setItems(FXCollections.observableArrayList(ApiService.obtenerMunicipios(newVal.getIdEstado())));
                cbMunicipio.setDisable(false);
            }
        });

        cbMunicipio.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbCP.getItems().clear(); cbColonia.getItems().clear();
            cbCP.setDisable(true); cbColonia.setDisable(true);
            if (newVal != null) {
                cbCP.setItems(FXCollections.observableArrayList(ApiService.obtenerCPs(newVal.getIdMunicipio())));
                cbCP.setDisable(false);
            }
        });

        cbCP.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbColonia.getItems().clear(); cbColonia.setDisable(true);
            if (newVal != null) {
                cbColonia.setItems(FXCollections.observableArrayList(ApiService.obtenerColonias(newVal)));
                cbColonia.setDisable(false);
            }
        });
    }

    public void setCliente(Cliente c) {
        if (c != null) {
            this.idClienteEdicion = c.getIdCliente();
            lblTitulo.setText("Editar Cliente");

            // 1. Llenar Datos Personales
            txtNombre.setText(c.getNombre());
            txtPaterno.setText(c.getApellidoPaterno());
            txtMaterno.setText(c.getApellidoMaterno());
            txtTelefono.setText(c.getTelefono());
            txtCorreo.setText(c.getCorreoElectronico());

            // 2. Llenar Dirección Manual
            txtCalle.setText(c.getCalle());
            txtNumero.setText(c.getNumero());

            // 3. RECONSTRUCCIÓN DE LA CASCADA (Automático) 🪄

            // PASO A: Seleccionar Estado
            // Buscamos en el combo el estado que coincida con el nombre guardado
            for (Estado e : cbEstado.getItems()) {
                if (e.getNombre().equalsIgnoreCase(c.getEstado())) {
                    cbEstado.setValue(e); // Al hacer esto, se dispara el Listener y carga Municipios
                    break;
                }
            }

            // PASO B: Seleccionar Municipio
            // Verificamos que se haya seleccionado un estado para buscar el municipio
            if (cbEstado.getValue() != null) {
                for (Municipio m : cbMunicipio.getItems()) {
                    // Nota: En Cliente se guarda como 'Ciudad', aquí lo comparamos
                    if (m.getNombre().equalsIgnoreCase(c.getCiudad())) {
                        cbMunicipio.setValue(m); // Dispara carga de CPs
                        break;
                    }
                }
            }

            // PASO C: Seleccionar Código Postal
            if (cbMunicipio.getValue() != null) {
                for (String cp : cbCP.getItems()) {
                    if (cp.equals(c.getCodigoPostal())) {
                        cbCP.setValue(cp); // Dispara carga de Colonias
                        break;
                    }
                }
            }

            // PASO D: Seleccionar Colonia
            // Aquí es más seguro comparar por ID de colonia
            if (cbCP.getValue() != null) {
                for (Colonia col : cbColonia.getItems()) {
                    if (col.getIdColonia().equals(c.getIdColonia())) {
                        cbColonia.setValue(col);
                        break;
                    }
                }
            }
        }
    }

    @FXML public void guardar() {
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

        if (cbColonia.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona la colonia").show();
            return;
        }

        // Datos de dirección
        Colonia col = cbColonia.getValue();
        c.setIdColonia(col.getIdColonia());
        c.setNombreColonia(col.getNombre());
        c.setCodigoPostal(col.getCodigoPostal());
        c.setCiudad(cbMunicipio.getValue().getNombre());
        c.setEstado(cbEstado.getValue().getNombre());

        Respuesta r = (idClienteEdicion != null) ? ApiService.editarCliente(c) : ApiService.registrarCliente(c);

        if (r != null && !r.getError()) cerrar();
        else new Alert(Alert.AlertType.ERROR, "Error al guardar").show();
    }

    @FXML public void cerrar() { ((Stage) txtNombre.getScene().getWindow()).close(); }
}