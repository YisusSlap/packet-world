package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Sucursal;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Validaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*; // Importante para List, Set, HashSet

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

    // Conductor
    @FXML private Label lblLicencia;
    @FXML private TextField txtLicencia;
    @FXML private Label lblUnidad;
    @FXML private ComboBox<Unidad> cbUnidad;

    // FOTO
    @FXML private ImageView imgFoto;
    private byte[] fotoBytesNueva = null; // Para guardar foto NUEVA (subida desde PC)

    // Variables de control
    private boolean esEdicion = false;
    private Colaborador colaboradorOriginal;

    @FXML
    public void initialize() {
        cbRol.getItems().addAll("Administrador", "Ejecutivo de tienda", "Conductor");
        cargarSucursales();

        cbRol.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean esConductor = "Conductor".equalsIgnoreCase(newVal);
            mostrarCamposConductor(esConductor);
        });

        Validaciones.soloLetras(txtNombre);
        Validaciones.soloLetras(txtPaterno);
        Validaciones.soloLetras(txtMaterno);
        Validaciones.limitarLongitud(txtCurp, 18);
        Validaciones.limitarLongitud(txtLicencia, 20);
        Validaciones.limitarLongitud(txtCorreo, 100);
        Validaciones.limitarLongitud(txtPassword, 50);
    }

    // --- LÓGICA DE FOTO (SELECCIONAR DESDE PC) ---
    @FXML
    public void seleccionarFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Foto de Perfil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg"));

        File file = fileChooser.showOpenDialog(txtNombre.getScene().getWindow());

        if (file != null) {
            try {
                if (file.length() > 1024 * 1024) {
                    mostrarAlerta("Imagen muy pesada", "Por favor seleccione una imagen menor a 1MB.");
                    return;
                }

                this.fotoBytesNueva = Files.readAllBytes(file.toPath());
                Image image = new Image(new ByteArrayInputStream(this.fotoBytesNueva));
                imgFoto.setImage(image);

            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo leer la imagen.");
            }
        }
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

        Sucursal sucursalSeleccionada = cbSucursal.getValue();
        if (sucursalSeleccionada != null) {
            c.setIdCodigoSucursal(sucursalSeleccionada.getCodigoSucursal());
        } else {
            mostrarAlerta("Faltan datos", "Debes seleccionar una sucursal obligatoriamente.");
            return;
        }

        c.setRol(cbRol.getValue());
        c.setEstatus("Activo");

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

            List<Colaborador> conductoresExistentes = ApiService.buscarColaboradores(null, "Conductor", null);
            for (Colaborador existente : conductoresExistentes) {
                if (esEdicion && existente.getNumeroPersonal().equals(c.getNumeroPersonal())) continue;

                if (existente.getNumeroLicencia() != null &&
                        existente.getNumeroLicencia().equalsIgnoreCase(c.getNumeroLicencia())) {
                    mostrarAlerta("Licencia Duplicada", "Esa licencia ya está registrada a nombre de: " + existente.getNombre());
                    return;
                }
            }

            // --- LÓGICA DE UNIDAD Y SUPLENTES ---
            Unidad unidadSeleccionada = cbUnidad.getValue();
            String idUnidadNueva = null; // Por defecto es NULL (Suplente)

            // Si seleccionó algo y NO es la opción "falsa" de Sin Asignar
            if (unidadSeleccionada != null && !"SIN_ASIGNAR".equals(unidadSeleccionada.getVin())) {
                idUnidadNueva = unidadSeleccionada.getVin(); // Guardamos el VIN real
            }

            if (esEdicion) {
                String idUnidadOriginal = (colaboradorOriginal != null) ? colaboradorOriginal.getIdUnidadAsignada() : null;

                if (!Objects.equals(idUnidadOriginal, idUnidadNueva)) {
                    List<Envio> misEnvios = ApiService.obtenerEnviosPorConductor(c.getNumeroPersonal());
                    boolean tieneCargaActiva = misEnvios.stream().anyMatch(e ->
                            e.getEstatusActual().toLowerCase().contains("tránsito") ||
                                    e.getEstatusActual().toLowerCase().contains("ruta"));

                    if (tieneCargaActiva) {
                        mostrarAlerta("Operación Denegada", "El conductor tiene envíos EN TRÁNSITO.\nNo se puede cambiar o quitar su unidad hasta que finalice.");
                        return;
                    }
                }
            }
            c.setIdUnidadAsignada(idUnidadNueva);

        } else {
            c.setNumeroLicencia(null);
            c.setIdUnidadAsignada(null);
        }

        // --- GUARDAR ---
        Respuesta resp;
        if (esEdicion) {
            resp = ApiService.editarColaborador(c);
        } else {
            resp = ApiService.registrarColaborador(c);
        }

        if (resp != null && !resp.getError()) {
            if (fotoBytesNueva != null) {
                Respuesta respFoto = ApiService.subirFoto(c.getNumeroPersonal(), fotoBytesNueva);
                if (respFoto.getError()) {
                    mostrarAlerta("Advertencia", "Datos guardados, pero falló la foto: " + respFoto.getMensaje());
                } else {
                    mostrarAlerta("Éxito", "Colaborador y foto guardados correctamente.");
                }
            } else {
                mostrarAlerta("Éxito", "Colaborador guardado correctamente.");
            }
            cerrarVentana();
        } else {
            mostrarAlerta("Error", "No se pudo guardar: " + (resp != null ? resp.getMensaje() : "Error de conexión"));
        }
    }

    @FXML
    public void btnCancelar() { cerrarVentana(); }

    public void setColaborador(Colaborador colaborador) {
        if (colaborador != null) {
            this.esEdicion = true;
            this.colaboradorOriginal = colaborador;

            txtNumPersonal.setText(colaborador.getNumeroPersonal());
            txtNumPersonal.setDisable(true);
            txtNombre.setText(colaborador.getNombre());
            txtPaterno.setText(colaborador.getApellidoPaterno());
            txtMaterno.setText(colaborador.getApellidoMaterno());
            txtCurp.setText(colaborador.getCurp());
            txtCorreo.setText(colaborador.getCorreoElectronico());
            txtPassword.setText(colaborador.getContrasenia());

            String idSucursalGuardada = colaborador.getIdCodigoSucursal();
            if (idSucursalGuardada != null) {
                for (Sucursal s : cbSucursal.getItems()) {
                    if (s.getCodigoSucursal().equals(idSucursalGuardada)) {
                        cbSucursal.setValue(s);
                        break;
                    }
                }
            }

            cbRol.setValue(colaborador.getRol());

            if ("Conductor".equalsIgnoreCase(colaborador.getRol())) {
                mostrarCamposConductor(true);
                txtLicencia.setText(colaborador.getNumeroLicencia());

                String idUnidad = colaborador.getIdUnidadAsignada();
                if (idUnidad != null && !idUnidad.isEmpty()) {
                    // Si tiene unidad, la seleccionamos
                    for (Unidad u : cbUnidad.getItems()) {
                        if (u.getVin().equals(idUnidad)) {
                            cbUnidad.setValue(u);
                            break;
                        }
                    }
                } else {
                    // Si NO tiene unidad (es suplente), seleccionamos la opción 0 ("Sin Unidad")
                    if (!cbUnidad.getItems().isEmpty()) {
                        cbUnidad.getSelectionModel().select(0);
                    }
                }
            }

            // Descargar foto fresca
            try {
                Colaborador colFoto = ApiService.obtenerFoto(colaborador.getNumeroPersonal());

                if (colFoto != null && colFoto.getFotografia() != null && !colFoto.getFotografia().isEmpty()) {
                    String fotoBase64 = colFoto.getFotografia();
                    String base64Limpio = fotoBase64.replaceAll("\\s", "");
                    byte[] imgBytes = Base64.getDecoder().decode(base64Limpio);
                    Image img = new Image(new ByteArrayInputStream(imgBytes));

                    if (!img.isError()) {
                        imgFoto.setImage(img);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error al descargar foto del servidor: " + e.getMessage());
            }
        }
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
        lblLicencia.setVisible(mostrar);
        lblLicencia.setManaged(mostrar);
        txtLicencia.setVisible(mostrar);
        txtLicencia.setManaged(mostrar);

        lblUnidad.setVisible(mostrar);
        lblUnidad.setManaged(mostrar);
        cbUnidad.setVisible(mostrar);
        cbUnidad.setManaged(mostrar);

        if (mostrar && cbUnidad.getItems().isEmpty()) {
            cargarUnidades();
        }
    }

    private void cargarUnidades() {
        List<Unidad> todasLasUnidades = ApiService.obtenerUnidades();
        List<Colaborador> conductores = ApiService.buscarColaboradores(null, "Conductor", null);

        Set<String> vinsOcupados = new HashSet<>();
        for (Colaborador c : conductores) {
            if (c.getIdUnidadAsignada() != null && !c.getIdUnidadAsignada().isEmpty()) {
                vinsOcupados.add(c.getIdUnidadAsignada());
            }
        }

        List<Unidad> unidadesDisponibles = new ArrayList<>();

        // OPCIÓN "SIN UNIDAD"
        Unidad unidadNula = new Unidad();
        unidadNula.setVin("SIN_ASIGNAR");
        unidadNula.setMarca("--- SIN UNIDAD / SUPLENTE ---");
        unidadNula.setModelo("");
        unidadesDisponibles.add(unidadNula);

        String miUnidadActual = (esEdicion && colaboradorOriginal != null) ? colaboradorOriginal.getIdUnidadAsignada() : null;

        for (Unidad u : todasLasUnidades) {
            boolean estaLibre = !vinsOcupados.contains(u.getVin());
            boolean esLaMia = (miUnidadActual != null && miUnidadActual.equals(u.getVin()));

            if ("activo".equalsIgnoreCase(u.getEstatus()) && (estaLibre || esLaMia)) {
                unidadesDisponibles.add(u);
            }
        }

        cbUnidad.setItems(FXCollections.observableArrayList(unidadesDisponibles));

        cbUnidad.setCellFactory(param -> new ListCell<Unidad>() {
            @Override
            protected void updateItem(Unidad item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if ("SIN_ASIGNAR".equals(item.getVin())) {
                        setText(item.getMarca());
                    } else {
                        setText(item.getMarca() + " " + item.getModelo() + " (" + item.getVin() + ")");
                    }
                }
            }
        });
        cbUnidad.setButtonCell(cbUnidad.getCellFactory().call(null));
    }

    private void cargarSucursales() {
        List<Sucursal> lista = ApiService.obtenerSucursales();
        cbSucursal.setItems(FXCollections.observableArrayList(lista));
    }
}