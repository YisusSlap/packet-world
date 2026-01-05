package com.example.packetworld.controller;

import com.example.packetworld.model.*;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion;
import com.example.packetworld.util.Validaciones;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador unificado para Registrar y Editar Envíos.
 * * CARACTERÍSTICAS PRINCIPALES:
 * 1. Manejo de cascada de direcciones (Estado -> Municipio -> CP -> Colonia).
 * 2. Tabla temporal de paquetes en memoria.
 * 3. Semáforo (bloqueoCascada) para evitar conflictos entre la carga automática y manual.
 */
public class FormularioEnvioController {

    // --- ELEMENTOS DE LA INTERFAZ (FXML) ---

    // Sección 1: Datos Generales
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private TextField txtSucursalOrigen;
    @FXML private TextField txtDestNombre, txtDestAp1, txtDestAp2;

    // Sección 2: Dirección Destino
    @FXML private ComboBox<Estado> cbEstado;
    @FXML private ComboBox<Municipio> cbMunicipio;
    @FXML private ComboBox<String> cbCP;
    @FXML private ComboBox<Colonia> cbColonia;
    @FXML private TextField txtCalle, txtNumero;

    // Sección 3: Paquetes
    @FXML private TextField txtPaqDesc, txtPaqPeso, txtPaqAlto, txtPaqAncho, txtPaqProf;
    @FXML private TableView<Paquete> tblPaquetes;
    @FXML private TableColumn<Paquete, String> colPaqDesc;
    @FXML private TableColumn<Paquete, String> colPaqPeso;
    @FXML private TableColumn<Paquete, String> colPaqDimensiones;

    // Botones
    @FXML private Button btnGuardar;

    // --- VARIABLES DE ESTADO ---
    private ObservableList<Paquete> listaPaquetesLocal = FXCollections.observableArrayList();
    private boolean esEdicion = false;
    private String numeroGuiaEdicion = null;

    /**
     * Semáforo para controlar los Listeners de los ComboBox.
     * true = Estamos editando por código (Listeners desactivados).
     * false = El usuario está interactuando (Listeners activos).
     */
    private boolean bloqueoCascada = false;

    @FXML
    public void initialize() {
        cargarCatalogosIniciales();
        configurarCascadaDireccion();
        configurarTablaPaquetes();
        aplicarValidacionesInputs();
    }

    /**
     * Carga clientes, estados y sucursal del usuario logueado.
     */
    private void cargarCatalogosIniciales() {
        cbCliente.setItems(FXCollections.observableArrayList(ApiService.obtenerClientes()));
        cbEstado.setItems(FXCollections.observableArrayList(ApiService.obtenerEstados()));

        if (ApiService.usuarioLogueado != null) {
            txtSucursalOrigen.setText(ApiService.usuarioLogueado.getIdCodigoSucursal());
        }
    }

    /**
     * Prepara la tabla para mostrar los paquetes que vamos agregando.
     */
    private void configurarTablaPaquetes() {
        colPaqDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPaqPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPesoKg() + " kg"));
        colPaqDimensiones.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDimAltoCm() + "x" + c.getValue().getDimAnchoCm() + "x" + c.getValue().getDimProfundidadCm()
        ));
        tblPaquetes.setItems(listaPaquetesLocal);
        configurarMenuContextualTabla(); // Click derecho para editar/eliminar
    }

    private void aplicarValidacionesInputs() {
        Validaciones.soloLetras(txtDestNombre);
        Validaciones.soloDecimales(txtPaqPeso);
        Validaciones.soloDecimales(txtPaqAlto);
        Validaciones.soloDecimales(txtPaqAncho);
        Validaciones.soloDecimales(txtPaqProf);
    }


    //                        LÓGICA DE EDICIÓN (CARGA)


    /**
     * Recibe un envío, descarga su información completa y rellena el formulario.
     */
    public void setEnvio(Envio envioSimple) {
        if (envioSimple != null) {
            this.esEdicion = true;
            this.numeroGuiaEdicion = envioSimple.getNumeroGuia();

            // 1. Descargar objeto completo (con paquetes y detalles)
            Envio envioFull = ApiService.rastrearEnvio(envioSimple.getNumeroGuia());
            Envio envio = (envioFull != null) ? envioFull : envioSimple;

            if (btnGuardar != null) btnGuardar.setText("Actualizar Envío");

            // 2. Llenar Cliente
            for (Cliente c : cbCliente.getItems()) {
                if (c.getIdCliente() != null && c.getIdCliente().equals(envio.getIdCliente())) {
                    cbCliente.setValue(c);
                    break;
                }
            }

            // 3. Llenar Textos
            txtDestNombre.setText(envio.getDestinatarioNombre());
            txtDestAp1.setText(envio.getDestinatarioAp1());
            txtDestAp2.setText(envio.getDestinatarioAp2());
            txtCalle.setText(envio.getDestinoCalle());
            txtNumero.setText(envio.getDestinoNumero());

            // 4. Llenar Dirección (Complejo)
            reconstruirDireccion(envio);

            // 5. Llenar Paquetes
            if (envio.getListaPaquetes() != null) {
                listaPaquetesLocal.setAll(envio.getListaPaquetes());
            }
        }
    }

    /**
     * Reconstruye los ComboBox de dirección forzando la carga de datos si están vacíos.
     * Usa el semáforo 'bloqueoCascada' para evitar que los listeners borren lo que ponemos.
     */
    private void reconstruirDireccion(Envio envio) {
        bloqueoCascada = true; // 🛑 ALTO: Listeners desactivados

        try {
            // A. Estado
            if (envio.getEstado() != null) {
                for (Estado e : cbEstado.getItems()) {
                    if (e.getNombre().equalsIgnoreCase(envio.getEstado())) {
                        cbEstado.setValue(e);
                        break;
                    }
                }
            }

            // B. Municipio (Carga Manual si es necesario)
            if (cbEstado.getValue() != null && cbMunicipio.getItems().isEmpty()) {
                cbMunicipio.setItems(FXCollections.observableArrayList(ApiService.obtenerMunicipios(cbEstado.getValue().getIdEstado())));
                cbMunicipio.setDisable(false);
            }
            if (envio.getCiudad() != null) {
                for (Municipio m : cbMunicipio.getItems()) {
                    if (m.getNombre().equalsIgnoreCase(envio.getCiudad())) {
                        cbMunicipio.setValue(m);
                        break;
                    }
                }
            }

            // C. CP
            if (cbMunicipio.getValue() != null && cbCP.getItems().isEmpty()) {
                cbCP.setItems(FXCollections.observableArrayList(ApiService.obtenerCPs(cbMunicipio.getValue().getIdMunicipio())));
                cbCP.setDisable(false);
            }
            if (envio.getCodigoPostal() != null) {
                for (String cp : cbCP.getItems()) {
                    if (cp.equals(envio.getCodigoPostal())) {
                        cbCP.setValue(cp);
                        break;
                    }
                }
            }

            // D. Colonia
            if (cbCP.getValue() != null && cbColonia.getItems().isEmpty()) {
                cbColonia.setItems(FXCollections.observableArrayList(ApiService.obtenerColonias(cbCP.getValue())));
                cbColonia.setDisable(false);
            }

            // Intentamos seleccionar por ID primero, luego por nombre
            boolean encontrada = false;
            if (envio.getIdColoniaDestino() != null) {
                for (Colonia col : cbColonia.getItems()) {
                    if (col.getIdColonia().equals(envio.getIdColoniaDestino())) {
                        cbColonia.setValue(col);
                        encontrada = true;
                        break;
                    }
                }
            }
            if (!encontrada && envio.getNombreColonia() != null) {
                for (Colonia col : cbColonia.getItems()) {
                    if (col.getNombre().equalsIgnoreCase(envio.getNombreColonia())) {
                        cbColonia.setValue(col);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error reconstruyendo dirección: " + e.getMessage());
        } finally {
            bloqueoCascada = false; // 🟢 SIGA: Listeners reactivados
        }
    }


    //                        LOGICA DE LISTENERS (CASCADA)


    private void configurarCascadaDireccion() {
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (bloqueoCascada) return; // Si estamos editando, ignorar

            limpiarCombos(cbMunicipio, cbCP, cbColonia);
            if(newVal != null) {
                cbMunicipio.setItems(FXCollections.observableArrayList(ApiService.obtenerMunicipios(newVal.getIdEstado())));
                cbMunicipio.setDisable(false);
            }
        });

        cbMunicipio.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (bloqueoCascada) return;

            limpiarCombos(cbCP, cbColonia);
            if(newVal != null) {
                cbCP.setItems(FXCollections.observableArrayList(ApiService.obtenerCPs(newVal.getIdMunicipio())));
                cbCP.setDisable(false);
            }
        });

        cbCP.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (bloqueoCascada) return;

            limpiarCombos(cbColonia);
            if(newVal != null) {
                cbColonia.setItems(FXCollections.observableArrayList(ApiService.obtenerColonias(newVal)));
                cbColonia.setDisable(false);
            }
        });
    }

    // Helper para limpiar combos en cascada
    @SafeVarargs
    private void limpiarCombos(ComboBox<?>... combos) {
        for (ComboBox<?> combo : combos) {
            combo.getItems().clear();
            combo.setDisable(true);
        }
    }


    //                        ACCIONES (BOTONES)


    @FXML
    public void agregarPaquete() {
        try {
            String desc = txtPaqDesc.getText();
            if (desc.isEmpty() || txtPaqPeso.getText().isEmpty()) {
                mostrarAlerta("Datos incompletos", "Descripción y Peso son obligatorios."); return;
            }

            Double peso = Double.parseDouble(txtPaqPeso.getText());
            Double alto = parseDoubleSeguro(txtPaqAlto.getText());
            Double ancho = parseDoubleSeguro(txtPaqAncho.getText());
            Double prof = parseDoubleSeguro(txtPaqProf.getText());

            Paquete p = new Paquete(desc, peso, alto, ancho, prof);
            listaPaquetesLocal.add(p);

            // Limpiar campos
            txtPaqDesc.clear(); txtPaqPeso.clear();
            txtPaqAlto.clear(); txtPaqAncho.clear(); txtPaqProf.clear();
            txtPaqDesc.requestFocus();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Datos numéricos inválidos.");
        }
    }

    @FXML
    public void guardarEnvio() {
        // Validaciones de Negocio
        if (cbCliente.getValue() == null || cbColonia.getValue() == null) {
            mostrarAlerta("Faltan datos", "Cliente y Dirección completa son obligatorios.");
            return;
        }
        if (listaPaquetesLocal.isEmpty()) {
            mostrarAlerta("Sin Paquetes", "Debe agregar al menos un paquete al envío.");
            return;
        }

        // Construcción del Objeto
        Envio envio = new Envio();

        if (esEdicion) {
            envio.setNumeroGuia(numeroGuiaEdicion);
        } else {
            envio.setIdEstatus(1); // Nuevo = Recibido
        }

        envio.setIdCliente(cbCliente.getValue().getIdCliente());
        if (ApiService.usuarioLogueado != null) {
            envio.setCodigoSucursalOrigen(ApiService.usuarioLogueado.getIdCodigoSucursal());
            envio.setNumeroPersonalUsuario(ApiService.usuarioLogueado.getNumeroPersonal());
        }

        // Datos Destino
        envio.setDestinatarioNombre(txtDestNombre.getText());
        envio.setDestinatarioAp1(txtDestAp1.getText());
        envio.setDestinatarioAp2(txtDestAp2.getText());
        envio.setDestinoCalle(txtCalle.getText());
        envio.setDestinoNumero(txtNumero.getText());

        // Datos Colonia (Obtenidos del objeto seleccionado)
        Colonia col = cbColonia.getValue();
        envio.setIdColoniaDestino(col.getIdColonia());
        envio.setNombreColonia(col.getNombre());
        envio.setCodigoPostal(col.getCodigoPostal());
        envio.setCiudad(cbMunicipio.getValue().getNombre());
        envio.setEstado(cbEstado.getValue().getNombre());

        envio.setListaPaquetes(new ArrayList<>(listaPaquetesLocal));

        // Enviar al Backend
        Respuesta resp = esEdicion ? ApiService.editarEnvio(envio) : ApiService.registrarEnvio(envio);

        if (resp != null && !resp.getError()) {
            String msg = esEdicion ? "Datos actualizados." : ("Guía: " + resp.getMensaje());
            Notificacion.mostrar("Operación Exitosa", msg, Notificacion.EXITO);
            cerrar();
        } else {
            mostrarAlerta("Error", "No se pudo guardar: " + (resp != null ? resp.getMensaje() : "Error de red"));
        }
    }

    @FXML public void cerrar() { ((Stage) txtDestNombre.getScene().getWindow()).close(); }

    // --- UTILIDADES ---

    private Double parseDoubleSeguro(String val) {
        return (val == null || val.isEmpty()) ? 0.0 : Double.parseDouble(val);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setContentText(msg);
        a.show();
    }

    private void configurarMenuContextualTabla() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemEditar = new MenuItem("✏️ Editar");
        itemEditar.setOnAction(e -> {
            Paquete p = tblPaquetes.getSelectionModel().getSelectedItem();
            if (p != null) {
                txtPaqDesc.setText(p.getDescripcion());
                txtPaqPeso.setText(String.valueOf(p.getPesoKg()));
                txtPaqAlto.setText(String.valueOf(p.getDimAltoCm()));
                txtPaqAncho.setText(String.valueOf(p.getDimAnchoCm()));
                txtPaqProf.setText(String.valueOf(p.getDimProfundidadCm()));
                listaPaquetesLocal.remove(p);
            }
        });

        MenuItem itemEliminar = new MenuItem("🗑️ Eliminar");
        itemEliminar.setOnAction(e -> listaPaquetesLocal.remove(tblPaquetes.getSelectionModel().getSelectedItem()));

        contextMenu.getItems().addAll(itemEditar, itemEliminar);
        tblPaquetes.setContextMenu(contextMenu);
    }
}