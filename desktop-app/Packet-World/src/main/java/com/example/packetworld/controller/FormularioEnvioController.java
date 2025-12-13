package com.example.packetworld.controller;

import com.example.packetworld.model.*;
import com.example.packetworld.service.ApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class FormularioEnvioController {

    // Cliente y Origen
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private TextField txtSucursalOrigen;
    @FXML private TextField txtDestNombre, txtDestAp1, txtDestAp2;

    // Dirección Destino (Cascada)
    @FXML private ComboBox<Estado> cbEstado;
    @FXML private ComboBox<Municipio> cbMunicipio;
    @FXML private ComboBox<String> cbCP;
    @FXML private ComboBox<Colonia> cbColonia;
    @FXML private TextField txtCalle, txtNumero;

    // Paquetes
    @FXML private TextField txtPaqDesc, txtPaqPeso, txtPaqAlto, txtPaqAncho, txtPaqProf;
    @FXML private TableView<Paquete> tblPaquetes;
    @FXML private TableColumn<Paquete, String> colPaqDesc;
    @FXML private TableColumn<Paquete, String> colPaqPeso;
    @FXML private TableColumn<Paquete, String> colPaqDimensiones;

    // Lista temporal de paquetes
    private ObservableList<Paquete> listaPaquetesLocal = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Cargar Clientes
        cbCliente.setItems(FXCollections.observableArrayList(ApiService.obtenerClientes())); // Asumiendo que implementaste esto

        // 2. Cargar Estado Origen (Usuario Logueado)
        if (ApiService.usuarioLogueado != null) {
            txtSucursalOrigen.setText(ApiService.usuarioLogueado.getIdCodigoSucursal());
        }

        // 3. Configurar Cascada Dirección (Igual que Cliente/Sucursal)
        cbEstado.setItems(FXCollections.observableArrayList(ApiService.obtenerEstados()));
        configurarListenersDireccion();

        // 4. Configurar Tabla Paquetes
        colPaqDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPaqPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPesoKg() + " kg"));
        colPaqDimensiones.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDimAltoCm() + "x" + c.getValue().getDimAnchoCm() + "x" + c.getValue().getDimProfundidadCm()
        ));
        tblPaquetes.setItems(listaPaquetesLocal);
    }

    private void configurarListenersDireccion() {
        // 1. Cuando cambia el ESTADO -> Cargar MUNICIPIOS
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Limpiamos los combos dependientes
            cbMunicipio.getItems().clear();
            cbCP.getItems().clear();
            cbColonia.getItems().clear();

            // Deshabilitamos hasta que se seleccione algo
            cbMunicipio.setDisable(true);
            cbCP.setDisable(true);
            cbColonia.setDisable(true);

            if (newVal != null) {
                // Llamamos a la API usando el ID del Estado seleccionado
                List<Municipio> municipios = ApiService.obtenerMunicipios(newVal.getIdEstado());
                cbMunicipio.setItems(FXCollections.observableArrayList(municipios));
                cbMunicipio.setDisable(false); // Habilitamos el combo de Municipios
            }
        });

        // 2. Cuando cambia el MUNICIPIO -> Cargar CÓDIGOS POSTALES
        cbMunicipio.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbCP.getItems().clear();
            cbColonia.getItems().clear();

            cbCP.setDisable(true);
            cbColonia.setDisable(true);

            if (newVal != null) {
                // Llamamos a la API usando el ID del Municipio seleccionado
                List<String> cps = ApiService.obtenerCPs(newVal.getIdMunicipio());
                cbCP.setItems(FXCollections.observableArrayList(cps));
                cbCP.setDisable(false); // Habilitamos el combo de CPs
            }
        });

        // 3. Cuando cambia el CÓDIGO POSTAL -> Cargar COLONIAS
        cbCP.valueProperty().addListener((obs, oldVal, newVal) -> {
            cbColonia.getItems().clear();
            cbColonia.setDisable(true);

            if (newVal != null) {
                // Llamamos a la API usando el Código Postal (String)
                List<Colonia> colonias = ApiService.obtenerColonias(newVal);
                cbColonia.setItems(FXCollections.observableArrayList(colonias));
                cbColonia.setDisable(false); // Habilitamos el combo de Colonias
            }
        });
    }

    @FXML
    public void agregarPaquete() {
        try {
            String desc = txtPaqDesc.getText();
            Double peso = Double.parseDouble(txtPaqPeso.getText());
            Double alto = Double.parseDouble(txtPaqAlto.getText());
            Double ancho = Double.parseDouble(txtPaqAncho.getText());
            Double prof = Double.parseDouble(txtPaqProf.getText());

            if (desc.isEmpty()) {
                mostrarAlerta("Error", "Descripción requerida");
                return;
            }

            // Crear paquete local (sin ID aún)
            Paquete p = new Paquete(desc, peso, alto, ancho, prof);
            listaPaquetesLocal.add(p);

            // Limpiar campos
            txtPaqDesc.clear(); txtPaqPeso.clear();
            txtPaqAlto.clear(); txtPaqAncho.clear(); txtPaqProf.clear();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Peso y dimensiones deben ser números válidos.");
        }
    }

    @FXML
    public void guardarEnvio() {
        // Validaciones generales
        if (cbCliente.getValue() == null || cbColonia.getValue() == null || listaPaquetesLocal.isEmpty()) {
            mostrarAlerta("Faltan datos", "Verifique Cliente, Dirección y agregue al menos un paquete.");
            return;
        }

        Envio envio = new Envio();

        envio.setIdEstatus(1);

        // 1. Datos Cliente
        envio.setIdCliente(cbCliente.getValue().getIdCliente());

        // 2. Datos Origen (Del usuario logueado)
        if (ApiService.usuarioLogueado != null) {
            envio.setCodigoSucursalOrigen(ApiService.usuarioLogueado.getIdCodigoSucursal());
            envio.setNumeroPersonalUsuario(ApiService.usuarioLogueado.getNumeroPersonal());
        }

        // 3. Datos Destinatario
        envio.setDestinatarioNombre(txtDestNombre.getText());
        envio.setDestinatarioAp1(txtDestAp1.getText());
        envio.setDestinatarioAp2(txtDestAp2.getText());

        // 4. Dirección Destino
        Colonia col = cbColonia.getValue();
        envio.setIdColoniaDestino(col.getIdColonia());
        envio.setNombreColonia(col.getNombre());
        envio.setCodigoPostal(col.getCodigoPostal());
        envio.setCiudad(cbMunicipio.getValue().getNombre());
        envio.setEstado(cbEstado.getValue().getNombre());
        envio.setDestinoCalle(txtCalle.getText());
        envio.setDestinoNumero(txtNumero.getText());

        // 5. Los Paquetes (La lista local se pasa al objeto)
        envio.setListaPaquetes(new ArrayList<>(listaPaquetesLocal));

        // 6. Enviar
        Respuesta resp = ApiService.registrarEnvio(envio);

        if (resp != null && !resp.getError()) {
            mostrarAlerta("¡Envío Registrado!", "Guía generada: " + (resp.getMensaje())); // Asumiendo que el mensaje trae la guía
            cerrar();
        } else {
            mostrarAlerta("Error", "No se pudo registrar: " + (resp != null ? resp.getMensaje() : "Error red"));
        }
    }

    @FXML public void cerrar() { ((Stage) txtDestNombre.getScene().getWindow()).close(); }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setContentText(msg);
        a.show();
    }
}