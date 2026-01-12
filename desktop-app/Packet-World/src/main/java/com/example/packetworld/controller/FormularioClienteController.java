package com.example.packetworld.controller;

import com.example.packetworld.model.*;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion;
import com.example.packetworld.util.Validaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;

public class FormularioClienteController {

    // IDs FXML
    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre, txtPaterno, txtMaterno;
    @FXML private TextField txtTelefono, txtCorreo;

    // Dirección
    @FXML private ComboBox<Estado> cbEstado;
    @FXML private ComboBox<Municipio> cbMunicipio;
    @FXML private ComboBox<String> cbCP;
    @FXML private ComboBox<Colonia> cbColonia;

    @FXML private TextField txtCalle, txtNumero;

    private Integer idClienteEdicion = null; // null = Nuevo, Valor = Edición

    @FXML
    public void initialize() {
        // 1. Cargar Estados
        cargarEstados();

        // 2. Configurar Cascada (Estado -> Municipio -> CP...)
        configurarListeners();

        // --- 3. VALIDACIONES DE ENTRADA (Lo que pediste) ---

        // Nombres: SOLO LETRAS (Bloquea números)
        Validaciones.soloLetrasLimitado(txtNombre,50);
        Validaciones.soloLetrasLimitado(txtPaterno,50);
        Validaciones.soloLetrasLimitado(txtMaterno,50);


        // Teléfono: SOLO NÚMEROS (Bloquea letras) y Máx 10
        Validaciones.soloNumerosLimitado(txtTelefono, 10);

        // Otros
        Validaciones.limitarLongitud(txtCorreo, 100);
        Validaciones.limitarLongitud(txtCalle, 100);
        Validaciones.soloNumerosLimitado(txtNumero,10);
    }

    private void cargarEstados() {
        List<Estado> lista = ApiService.obtenerEstados();
        cbEstado.setItems(FXCollections.observableArrayList(lista));
    }

    private void configurarListeners() {
        // Estado -> Municipio
        cbEstado.valueProperty().addListener((obs, oldVal, newVal) -> {
            limpiarCombosDesde(cbMunicipio);
            if (newVal != null) {
                cbMunicipio.setItems(FXCollections.observableArrayList(ApiService.obtenerMunicipios(newVal.getIdEstado())));
                cbMunicipio.setDisable(false);
            }
        });

        // Municipio -> CP
        cbMunicipio.valueProperty().addListener((obs, oldVal, newVal) -> {
            limpiarCombosDesde(cbCP);
            if (newVal != null) {
                cbCP.setItems(FXCollections.observableArrayList(ApiService.obtenerCPs(newVal.getIdMunicipio())));
                cbCP.setDisable(false);
            }
        });

        // CP -> Colonia
        cbCP.valueProperty().addListener((obs, oldVal, newVal) -> {
            limpiarCombosDesde(cbColonia);
            if (newVal != null) {
                cbColonia.setItems(FXCollections.observableArrayList(ApiService.obtenerColonias(newVal)));
                cbColonia.setDisable(false);
            }
        });
    }

    private void limpiarCombosDesde(Control control) {
        if (control == cbMunicipio) {
            cbMunicipio.getItems().clear(); cbMunicipio.setDisable(true);
            cbCP.getItems().clear(); cbCP.setDisable(true);
            cbColonia.getItems().clear(); cbColonia.setDisable(true);
        } else if (control == cbCP) {
            cbCP.getItems().clear(); cbCP.setDisable(true);
            cbColonia.getItems().clear(); cbColonia.setDisable(true);
        } else if (control == cbColonia) {
            cbColonia.getItems().clear(); cbColonia.setDisable(true);
        }
    }

    public void setCliente(Cliente c) {
        if (c != null) {
            this.idClienteEdicion = c.getIdCliente();
            lblTitulo.setText("Editar Cliente");

            txtNombre.setText(c.getNombre());
            txtPaterno.setText(c.getApellidoPaterno());
            txtMaterno.setText(c.getApellidoMaterno());
            txtTelefono.setText(c.getTelefono());
            txtCorreo.setText(c.getCorreoElectronico());
            txtCalle.setText(c.getCalle());
            txtNumero.setText(c.getNumero());

            // --- Reconstrucción inteligente de la cascada ---
            // 1. Estado
            seleccionarEnCombo(cbEstado, c.getEstado());

            // 2. Municipio (Forzamos carga manual para asegurar que existen los items)
            if (cbEstado.getValue() != null) {
                cbMunicipio.setItems(FXCollections.observableArrayList(ApiService.obtenerMunicipios(cbEstado.getValue().getIdEstado())));
                cbMunicipio.setDisable(false);
                seleccionarEnCombo(cbMunicipio, c.getCiudad());
            }

            // 3. CP
            if (cbMunicipio.getValue() != null) {
                cbCP.setItems(FXCollections.observableArrayList(ApiService.obtenerCPs(cbMunicipio.getValue().getIdMunicipio())));
                cbCP.setDisable(false);
                cbCP.setValue(c.getCodigoPostal());
            }

            // 4. Colonia (Buscamos por ID para exactitud)
            if (cbCP.getValue() != null) {
                cbColonia.setItems(FXCollections.observableArrayList(ApiService.obtenerColonias(cbCP.getValue())));
                cbColonia.setDisable(false);
                for (Colonia col : cbColonia.getItems()) {
                    if (col.getIdColonia().equals(c.getIdColonia())) {
                        cbColonia.setValue(col);
                        break;
                    }
                }
            }
        }
    }

    // Método genérico para seleccionar en combos por nombre
    private <T> void seleccionarEnCombo(ComboBox<T> combo, String texto) {
        for (T item : combo.getItems()) {
            // Comparamos el toString o propiedades específicas si fuera necesario
            if (item.toString().equalsIgnoreCase(texto) ||
                    (item instanceof Municipio && ((Municipio)item).getNombre().equalsIgnoreCase(texto)) ||
                    (item instanceof Estado && ((Estado)item).getNombre().equalsIgnoreCase(texto))) {
                combo.setValue(item);
                break;
            }
        }
    }

    @FXML
    public void guardar() {
        // --- 1. VALIDAR TODOS LOS CAMPOS VACÍOS ---
        if (txtNombre.getText().trim().isEmpty() ||
                txtPaterno.getText().trim().isEmpty() ||
                txtMaterno.getText().trim().isEmpty() ||
                txtCorreo.getText().trim().isEmpty() ||
                txtTelefono.getText().trim().isEmpty() ||
                txtCalle.getText().trim().isEmpty() ||
                txtNumero.getText().trim().isEmpty()) {

            mostrarAlerta("Campos vacíos", "Por favor completa TODOS los datos del cliente (incluyendo dirección y apellidos).");
            return;
        }

        // 2. Validar Dirección Completa
        if (cbColonia.getValue() == null) {
            mostrarAlerta("Dirección incompleta", "Por favor completa todos los campos de ubicación.");
            return;
        }

        // 3. Validar Formato de Correo
        String correo = txtCorreo.getText().trim();
        if (!Validaciones.esEmailValido(correo)) {
            mostrarAlerta("Correo Inválido", "El formato debe ser: ejemplo@dominio.com");
            return;
        }

        // 4. VALIDAR DUPLICADOS (La lógica nueva)
        String telefono = txtTelefono.getText().trim();
        if (existeDuplicado(correo, telefono)) {
            return; // Si encontró duplicado, sale del método
        }

        // --- Construcción del Objeto ---
        Cliente c = new Cliente();
        c.setIdCliente(idClienteEdicion); // Si es null crea, si tiene ID edita
        c.setNombre(txtNombre.getText().trim());
        c.setApellidoPaterno(txtPaterno.getText().trim());
        c.setApellidoMaterno(txtMaterno.getText().trim());
        c.setTelefono(telefono);
        c.setCorreoElectronico(correo);
        c.setCalle(txtCalle.getText().trim());
        c.setNumero(txtNumero.getText().trim());
        c.setEstatus("Activo");

        // Datos desde Combos
        Colonia col = cbColonia.getValue();
        c.setIdColonia(col.getIdColonia());
        c.setNombreColonia(col.getNombre());
        c.setCodigoPostal(col.getCodigoPostal());
        c.setCiudad(cbMunicipio.getValue().getNombre());
        c.setEstado(cbEstado.getValue().getNombre());

        // --- Enviar a API ---
        Respuesta r = (idClienteEdicion != null) ? ApiService.editarCliente(c) : ApiService.registrarCliente(c);

        if (r != null && !r.getError()) {
            Notificacion.mostrar("Éxito", "Cliente guardado correctamente.", Notificacion.EXITO);
            cerrar();
        } else {
            mostrarAlerta("Error", "No se pudo guardar: " + (r != null ? r.getMensaje() : "Error desconocido"));
        }
    }

    /**
     * Verifica si el correo o teléfono ya existen en OTRO cliente.
     */
    private boolean existeDuplicado(String correo, String telefono) {
        // A. Verificar Correo
        List<Cliente> clientesPorCorreo = ApiService.buscarClientes(null, null, correo);
        for (Cliente existente : clientesPorCorreo) {
            // Si el ID del encontrado es diferente al que edito (o estoy creando uno nuevo y encontré uno)...
            if (!Objects.equals(existente.getIdCliente(), this.idClienteEdicion)) {
                mostrarAlerta("Correo Duplicado", "El correo ya está registrado a nombre de: " + existente.getNombre());
                return true;
            }
        }

        // B. Verificar Teléfono
        List<Cliente> clientesPorTel = ApiService.buscarClientes(null, telefono, null);
        for (Cliente existente : clientesPorTel) {
            if (!Objects.equals(existente.getIdCliente(), this.idClienteEdicion)) {
                mostrarAlerta("Teléfono Duplicado", "El teléfono ya pertenece a: " + existente.getNombre());
                return true;
            }
        }

        return false; // Todo limpio
    }

    @FXML public void cerrar() { ((Stage) txtNombre.getScene().getWindow()).close(); }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}