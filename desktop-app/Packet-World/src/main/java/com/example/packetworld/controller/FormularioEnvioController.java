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

public class FormularioEnvioController {

    // --- ELEMENTOS DE LA INTERFAZ ---
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private TextField txtSucursalOrigen;
    @FXML private TextField txtDestNombre, txtDestAp1, txtDestAp2;

    @FXML private ComboBox<Estado> cbEstado;
    @FXML private ComboBox<Municipio> cbMunicipio;
    @FXML private ComboBox<String> cbCP;
    @FXML private ComboBox<Colonia> cbColonia;
    @FXML private TextField txtCalle, txtNumero;

    @FXML private TextField txtPaqDesc, txtPaqPeso, txtPaqAlto, txtPaqAncho, txtPaqProf;
    @FXML private TableView<Paquete> tblPaquetes;
    @FXML private TableColumn<Paquete, String> colPaqDesc;
    @FXML private TableColumn<Paquete, String> colPaqPeso;
    @FXML private TableColumn<Paquete, String> colPaqDimensiones;

    @FXML private Button btnGuardar;
    @FXML private Label lblCostoTotal;

    // --- VARIABLES DE ESTADO ---
    private ObservableList<Paquete> listaPaquetesLocal = FXCollections.observableArrayList();

    //  Lista para recordar qué paquetes borrar de la BD al guardar
    private List<Paquete> paquetesParaEliminar = new ArrayList<>();

    private boolean esEdicion = false;
    private String numeroGuiaEdicion = null;
    private Integer idEnvioEdicion = null; // Guardamos el ID del envío

    private boolean bloqueoCascada = false;

    @FXML
    public void initialize() {
        cargarCatalogosIniciales();
        configurarCascadaDireccion();
        configurarTablaPaquetes();
        aplicarValidacionesInputs();
    }

    private void cargarCatalogosIniciales() {
        cbCliente.setItems(FXCollections.observableArrayList(ApiService.obtenerClientes()));
        cbEstado.setItems(FXCollections.observableArrayList(ApiService.obtenerEstados()));
        if (ApiService.usuarioLogueado != null) {
            txtSucursalOrigen.setText(ApiService.usuarioLogueado.getIdCodigoSucursal());
        }
    }

    private void configurarTablaPaquetes() {
        colPaqDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPaqPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPesoKg() + " kg"));
        colPaqDimensiones.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDimAltoCm() + "x" + c.getValue().getDimAnchoCm() + "x" + c.getValue().getDimProfundidadCm()
        ));
        tblPaquetes.setItems(listaPaquetesLocal);
        configurarMenuContextualTabla();
    }

    private void aplicarValidacionesInputs() {
        Validaciones.soloLetras(txtDestNombre);
        Validaciones.soloLetras(txtDestAp1);
        Validaciones.soloLetras(txtDestAp2);
        Validaciones.soloDecimales(txtPaqPeso);
        Validaciones.soloDecimales(txtPaqAlto);
        Validaciones.soloDecimales(txtPaqAncho);
        Validaciones.soloDecimales(txtPaqProf);
    }

    // --- LOGICA DE EDICIÓN ---

    public void setEnvio(Envio envioSimple) {
        if (envioSimple != null) {
            this.esEdicion = true;
            this.numeroGuiaEdicion = envioSimple.getNumeroGuia();

            // Descargar completo
            Envio envio = ApiService.rastrearEnvio(envioSimple.getNumeroGuia());
            if (envio == null) envio = envioSimple;

            this.idEnvioEdicion = envio.getIdEnvio(); // Guardamos el ID real

            if (btnGuardar != null) btnGuardar.setText("Actualizar Envío");

            // Llenar Campos
            for (Cliente c : cbCliente.getItems()) {
                if (c.getIdCliente() != null && c.getIdCliente().equals(envio.getIdCliente())) {
                    cbCliente.setValue(c); break;
                }
            }
            txtDestNombre.setText(envio.getDestinatarioNombre());
            txtDestAp1.setText(envio.getDestinatarioAp1());
            txtDestAp2.setText(envio.getDestinatarioAp2());
            txtCalle.setText(envio.getDestinoCalle());
            txtNumero.setText(envio.getDestinoNumero());

            reconstruirDireccion(envio);

            // Paquetes
            if (envio.getListaPaquetes() != null) {
                listaPaquetesLocal.setAll(envio.getListaPaquetes());
            }
            // Actualizar precio inicial
            cotizar();
        }
    }

    private void reconstruirDireccion(Envio envio) {
        bloqueoCascada = true;
        try {
            if (envio.getEstado() != null) {
                for (Estado e : cbEstado.getItems()) {
                    if (e.getNombre().equalsIgnoreCase(envio.getEstado())) {
                        cbEstado.setValue(e); break;
                    }
                }
            }
            if (cbEstado.getValue() != null && cbMunicipio.getItems().isEmpty()) {
                cbMunicipio.setItems(FXCollections.observableArrayList(ApiService.obtenerMunicipios(cbEstado.getValue().getIdEstado())));
                cbMunicipio.setDisable(false);
            }
            if (envio.getCiudad() != null) {
                for (Municipio m : cbMunicipio.getItems()) {
                    if (m.getNombre().equalsIgnoreCase(envio.getCiudad())) {
                        cbMunicipio.setValue(m); break;
                    }
                }
            }
            if (cbMunicipio.getValue() != null && cbCP.getItems().isEmpty()) {
                cbCP.setItems(FXCollections.observableArrayList(ApiService.obtenerCPs(cbMunicipio.getValue().getIdMunicipio())));
                cbCP.setDisable(false);
            }
            if (envio.getCodigoPostal() != null) {
                for (String cp : cbCP.getItems()) {
                    if (cp.equals(envio.getCodigoPostal())) {
                        cbCP.setValue(cp); break;
                    }
                }
            }
            if (cbCP.getValue() != null && cbColonia.getItems().isEmpty()) {
                cbColonia.setItems(FXCollections.observableArrayList(ApiService.obtenerColonias(cbCP.getValue())));
                cbColonia.setDisable(false);
            }
            if (envio.getIdColoniaDestino() != null) {
                for (Colonia col : cbColonia.getItems()) {
                    if (col.getIdColonia().equals(envio.getIdColoniaDestino())) {
                        cbColonia.setValue(col); return;
                    }
                }
            }
        } catch (Exception e) { System.out.println("Error reconstruyendo dirección: " + e.getMessage()); }
        finally { bloqueoCascada = false; }
    }

    // --- CASCADA ---
    private void configurarCascadaDireccion() {
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (bloqueoCascada) return;
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

    @SafeVarargs
    private void limpiarCombos(ComboBox<?>... combos) {
        for (ComboBox<?> combo : combos) {
            combo.getItems().clear(); combo.setDisable(true);
        }
    }

    // --- ACCIONES ---

    @FXML
    public void agregarPaquete() {
        try {
            String desc = txtPaqDesc.getText();
            // VALIDAR CAMPOS VACÍOS (Todos son obligatorios para poder cotizar)
            if (txtPaqDesc.getText().trim().isEmpty() ||
                    txtPaqPeso.getText().trim().isEmpty() ||
                    txtPaqAlto.getText().trim().isEmpty() ||
                    txtPaqAncho.getText().trim().isEmpty() ||
                    txtPaqProf.getText().trim().isEmpty()) {

                mostrarAlerta("Datos incompletos", "Descripción, Peso y TODAS las dimensiones son obligatorias.");
                return;
            }

            Double peso = Double.parseDouble(txtPaqPeso.getText());
            Double alto = parseDoubleSeguro(txtPaqAlto.getText());
            Double ancho = parseDoubleSeguro(txtPaqAncho.getText());
            Double prof = parseDoubleSeguro(txtPaqProf.getText());

            // VALIDACIÓN LÓGICA (Lo que pediste: No ceros, no negativos)
            if (peso <= 0 || alto <= 0 || ancho <= 0 || prof <= 0) {
                mostrarAlerta("Valores Inválidos", "El peso y las dimensiones deben ser mayores a 0.");
                return;
            }

            //creacion del objeto

            Paquete p = new Paquete(desc, peso, alto, ancho, prof);
            // Si estamos editando un envío existente, este paquete nuevo
            // necesitará el ID del envío para guardarse.
            if(esEdicion && idEnvioEdicion != null) {
                p.setIdEnvio(idEnvioEdicion);
            }

            listaPaquetesLocal.add(p);

            if (cbColonia.getValue() != null) cotizar();

            txtPaqDesc.clear(); txtPaqPeso.clear();
            txtPaqAlto.clear(); txtPaqAncho.clear(); txtPaqProf.clear();
            txtPaqDesc.requestFocus();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Datos numéricos inválidos.");
        }
    }

    /**
     * Menú Contextual: Aquí capturamos lo que se elimina para procesarlo al guardar.
     */
    private void configurarMenuContextualTabla() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemEditar = new MenuItem("✏️ Editar (Mover a formulario)");
        itemEditar.setOnAction(e -> {
            Paquete p = tblPaquetes.getSelectionModel().getSelectedItem();
            if (p != null) {
                txtPaqDesc.setText(p.getDescripcion());
                txtPaqPeso.setText(String.valueOf(p.getPesoKg()));
                txtPaqAlto.setText(String.valueOf(p.getDimAltoCm()));
                txtPaqAncho.setText(String.valueOf(p.getDimAnchoCm()));
                txtPaqProf.setText(String.valueOf(p.getDimProfundidadCm()));

                // Si el paquete ya existía en BD (tiene ID), lo marcamos para eliminar
                // porque al darle "Agregar" crearemos uno nuevo.
                if (p.getIdPaquete() != null) {
                    paquetesParaEliminar.add(p);
                }

                listaPaquetesLocal.remove(p);
            }
        });

        MenuItem itemEliminar = new MenuItem("🗑️ Eliminar");
        itemEliminar.setOnAction(e -> {
            Paquete p = tblPaquetes.getSelectionModel().getSelectedItem();
            if (p != null) {
                // Si viene de BD, a la lista negra
                if (p.getIdPaquete() != null) {
                    paquetesParaEliminar.add(p);
                }
                listaPaquetesLocal.remove(p);
                if (cbColonia.getValue() != null) cotizar();
            }
        });

        contextMenu.getItems().addAll(itemEditar, itemEliminar);
        tblPaquetes.setContextMenu(contextMenu);
    }

    @FXML
    public void guardarEnvio() {
        if (cbCliente.getValue() == null || cbColonia.getValue() == null || listaPaquetesLocal.isEmpty()) {
            mostrarAlerta("Faltan datos", "Verifique Cliente, Dirección y agregue paquetes.");
            return;
        }

        // 2. VALIDACIÓN DE CAMPOS DE TEXTO (Nombre y Calle son vitales)
        if (txtDestNombre.getText().trim().isEmpty() ||
                txtDestAp1.getText().trim().isEmpty() ||
                txtDestAp2.getText().trim().isEmpty() ||
                txtCalle.getText().trim().isEmpty() ||
                txtNumero.getText().trim().isEmpty()) {

            mostrarAlerta("Datos del Destinatario", "El Nombre, Apellido, Calle y Número son obligatorios.");
            return;
        }

        Envio envio = new Envio();
        if (esEdicion) {
            envio.setNumeroGuia(numeroGuiaEdicion);
            envio.setIdEnvio(idEnvioEdicion);
        } else {
            envio.setIdEstatus(1);
        }

        envio.setIdCliente(cbCliente.getValue().getIdCliente());
        if (ApiService.usuarioLogueado != null) {
            envio.setCodigoSucursalOrigen(ApiService.usuarioLogueado.getIdCodigoSucursal());
            envio.setNumeroPersonalUsuario(ApiService.usuarioLogueado.getNumeroPersonal());
        }
        envio.setDestinatarioNombre(txtDestNombre.getText());
        envio.setDestinatarioAp1(txtDestAp1.getText());
        envio.setDestinatarioAp2(txtDestAp2.getText());

        Colonia col = cbColonia.getValue();
        envio.setIdColoniaDestino(col.getIdColonia());
        envio.setNombreColonia(col.getNombre());
        envio.setCodigoPostal(col.getCodigoPostal());
        envio.setCiudad(cbMunicipio.getValue().getNombre());
        envio.setEstado(cbEstado.getValue().getNombre());
        envio.setDestinoCalle(txtCalle.getText());
        envio.setDestinoNumero(txtNumero.getText());

        // Para registro nuevo, mandamos la lista completa.
        envio.setListaPaquetes(new ArrayList<>(listaPaquetesLocal));

        Respuesta resp;

        // --- LOGICA DE GUARDADO ---
        if (esEdicion) {
            // 1. Actualizar Encabezado
            resp = ApiService.editarEnvio(envio);

            if (resp != null && !resp.getError()) {
                // 2. Procesar Eliminaciones (Los que sacamos de la tabla)
                for (Paquete p : paquetesParaEliminar) {
                    ApiService.eliminarPaquete(p.getIdPaquete());
                }

                // 3. Procesar Nuevos (Los que agregamos a la tabla)
                for (Paquete p : listaPaquetesLocal) {
                    // Si el paquete NO tiene ID, es nuevo -> Registrarlo
                    if (p.getIdPaquete() == null) {
                        p.setIdEnvio(envio.getIdEnvio()); // Asegurar ID del padre
                        ApiService.registrarPaquete(p);
                    }
                    // Si ya tiene ID, técnicamente podríamos llamar editarPaquete,
                    // pero en este flujo normalmente no se tocan si no se borran.
                }

                // Forzamos un recalculo de costo al final por si acaso
                ApiService.editarEnvio(envio);
            }
        } else {
            // Registro Nuevo (Todo va junto)
            resp = ApiService.registrarEnvio(envio);
        }

        if (resp != null && !resp.getError()) {
            String msg = esEdicion ? "Envío actualizado." : ("Guía: " + resp.getMensaje());
            Notificacion.mostrar("Éxito", msg, Notificacion.EXITO);
            cerrar();
        } else {
            mostrarAlerta("Error", "Falló: " + (resp != null ? resp.getMensaje() : "Red"));
        }
    }

    @FXML
    public void cotizar() {
        if (cbColonia.getValue() == null) return;

        Envio envioCotizacion = new Envio();
        if (ApiService.usuarioLogueado != null) {
            envioCotizacion.setCodigoSucursalOrigen(ApiService.usuarioLogueado.getIdCodigoSucursal());
        }
        envioCotizacion.setIdColoniaDestino(cbColonia.getValue().getIdColonia());
        envioCotizacion.setListaPaquetes(new ArrayList<>(listaPaquetesLocal));

        Double costo = ApiService.cotizarEnvio(envioCotizacion);
        if (costo != null) {
            lblCostoTotal.setText(String.format("$%.2f", costo));
        } else {
            lblCostoTotal.setText("$0.00");
        }
    }

    @FXML public void cerrar() { ((Stage) txtDestNombre.getScene().getWindow()).close(); }

    private Double parseDoubleSeguro(String val) {
        return (val == null || val.isEmpty()) ? 0.0 : Double.parseDouble(val);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo); a.setContentText(msg); a.show();
    }
}