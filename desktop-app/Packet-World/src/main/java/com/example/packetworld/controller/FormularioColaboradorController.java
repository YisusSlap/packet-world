package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.Sucursal;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // UX Moderna
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
import java.util.*;

/**
 * Controlador para la Gestión de Colaboradores (Altas, Bajas, Edición).
 * Maneja lógica compleja como:
 * - Roles (Conductor vs Administrativo)
 * - Asignación inteligente de unidades
 * - Carga y previsualización de fotos de perfil
 */
public class FormularioColaboradorController {

    // Campos generales
    @FXML private TextField txtNumPersonal, txtNombre, txtPaterno, txtMaterno, txtCurp, txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Sucursal> cbSucursal;
    @FXML private ComboBox<String> cbRol;

    // Campos específicos para Conductores (se ocultan si no es conductor)
    @FXML private Label lblLicencia, lblUnidad;
    @FXML private TextField txtLicencia;
    @FXML private ComboBox<Unidad> cbUnidad;

    // Gestión de Foto
    @FXML private ImageView imgFoto;
    private byte[] fotoBytesNueva = null; // Almacena temporalmente la foto seleccionada del PC

    // Variables de estado
    private boolean esEdicion = false;
    private Colaborador colaboradorOriginal; // Para comparar cambios críticos

    @FXML
    public void initialize() {
        // 1. Cargar Catálogos
        cbRol.getItems().addAll("Administrador", "Ejecutivo de tienda", "Conductor");
        cargarSucursales();

        // 2. Listener de Rol: Si es conductor, mostramos campos extra
        cbRol.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean esConductor = "Conductor".equalsIgnoreCase(newVal);
            mostrarCamposConductor(esConductor);
        });

        // 3. Validaciones de entrada
        Validaciones.soloLetras(txtNombre);
        Validaciones.soloLetras(txtPaterno);
        Validaciones.soloLetras(txtMaterno);
        Validaciones.limitarLongitud(txtCurp, 18);
        Validaciones.limitarLongitud(txtLicencia, 20);
        Validaciones.limitarLongitud(txtCorreo, 100);
        Validaciones.limitarLongitud(txtPassword, 50);
    }

    /**
     * Abre un selector de archivos para elegir una foto de perfil.
     * Convierte la imagen a bytes y la muestra en el ImageView.
     */
    @FXML
    public void seleccionarFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Foto de Perfil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg"));

        File file = fileChooser.showOpenDialog(txtNombre.getScene().getWindow());

        if (file != null) {
            try {
                if (file.length() > 1024 * 1024) { // Límite de 1MB por rendimiento
                    mostrarAlerta("Imagen muy pesada", "Por favor seleccione una imagen menor a 1MB.");
                    return;
                }

                // Leemos bytes y mostramos previsualización
                this.fotoBytesNueva = Files.readAllBytes(file.toPath());
                Image image = new Image(new ByteArrayInputStream(this.fotoBytesNueva));
                imgFoto.setImage(image);

            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo leer la imagen seleccionada.");
            }
        }
    }

    /**
     * Proceso principal de guardado.
     * Valida datos, verifica reglas de negocio (licencias duplicadas, unidades ocupadas)
     * y envía la petición a la API.
     */
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
        c.setRol(cbRol.getValue());
        c.setEstatus("Activo");

        // Validación Sucursal
        Sucursal sucursalSeleccionada = cbSucursal.getValue();
        if (sucursalSeleccionada != null) {
            c.setIdCodigoSucursal(sucursalSeleccionada.getCodigoSucursal());
        } else {
            mostrarAlerta("Faltan datos", "La sucursal es obligatoria.");
            return;
        }

        // Validaciones Básicas
        if (c.getNumeroPersonal().isEmpty() || c.getNombre().isEmpty() || c.getRol() == null) {
            mostrarAlerta("Campos vacíos", "Por favor llena los campos obligatorios.");
            return;
        }

        if (!txtCorreo.getText().isEmpty() && !Validaciones.esEmailValido(txtCorreo.getText())) {
            mostrarAlerta("Correo inválido", "Formato incorrecto (ejemplo: usuario@dominio.com).");
            return;
        }

        // --- REGLAS DE NEGOCIO PARA CONDUCTORES ---
        if ("Conductor".equalsIgnoreCase(c.getRol())) {
            if (txtLicencia.getText().isEmpty()) {
                mostrarAlerta("Faltan datos", "El conductor requiere número de licencia.");
                return;
            }
            c.setNumeroLicencia(txtLicencia.getText());

            // Regla: No puede haber dos conductores con la misma licencia
            List<Colaborador> conductoresExistentes = ApiService.buscarColaboradores(null, "Conductor", null);
            for (Colaborador existente : conductoresExistentes) {
                // Si editamos, ignoramos al propio usuario
                if (esEdicion && existente.getNumeroPersonal().equals(c.getNumeroPersonal())) continue;

                if (existente.getNumeroLicencia() != null &&
                        existente.getNumeroLicencia().equalsIgnoreCase(c.getNumeroLicencia())) {
                    mostrarAlerta("Licencia Duplicada", "Esa licencia ya pertenece a: " + existente.getNombre());
                    return;
                }
            }

            // Regla: Asignación de Unidad
            Unidad unidadSeleccionada = cbUnidad.getValue();
            String idUnidadNueva = null; // Por defecto es Suplente (null)

            // Si seleccionó una unidad real (no la opción dummy "SIN_ASIGNAR")
            if (unidadSeleccionada != null && !"SIN_ASIGNAR".equals(unidadSeleccionada.getVin())) {
                idUnidadNueva = unidadSeleccionada.getVin();
            }

            // Regla: No quitar unidad si tiene carga activa (Solo en edición)
            if (esEdicion) {
                String idUnidadOriginal = (colaboradorOriginal != null) ? colaboradorOriginal.getIdUnidadAsignada() : null;

                if (!Objects.equals(idUnidadOriginal, idUnidadNueva)) { // Si cambió la unidad
                    List<Envio> misEnvios = ApiService.obtenerEnviosPorConductor(c.getNumeroPersonal());
                    boolean tieneCargaActiva = misEnvios.stream().anyMatch(e ->
                            e.getEstatusActual().toLowerCase().contains("tránsito") ||
                                    e.getEstatusActual().toLowerCase().contains("ruta"));

                    if (tieneCargaActiva) {
                        mostrarAlerta("Operación Denegada", "El conductor tiene envíos EN TRÁNSITO.\nNo se puede cambiar su unidad hasta que entregue la carga.");
                        return;
                    }
                }
            }
            c.setIdUnidadAsignada(idUnidadNueva);

        } else {
            // Si no es conductor, limpiamos estos campos por seguridad
            c.setNumeroLicencia(null);
            c.setIdUnidadAsignada(null);
        }

        // --- GUARDADO ---
        Respuesta resp;
        if (esEdicion) {
            resp = ApiService.editarColaborador(c);
        } else {
            resp = ApiService.registrarColaborador(c);
        }

        if (resp != null && !resp.getError()) {

            // Si hay foto nueva, la subimos en una segunda petición
            if (fotoBytesNueva != null) {
                Respuesta respFoto = ApiService.subirFoto(c.getNumeroPersonal(), fotoBytesNueva);
                if (respFoto.getError()) {
                    Notificacion.mostrar("Advertencia", "Datos guardados, pero falló la foto.", Notificacion.ERROR);
                } else {
                    Notificacion.mostrar("Éxito", "Colaborador y foto guardados.", Notificacion.EXITO);
                }
            } else {
                Notificacion.mostrar("Éxito", "Colaborador guardado correctamente.", Notificacion.EXITO);
            }
            cerrarVentana();
        } else {
            mostrarAlerta("Error", "No se pudo guardar: " + (resp != null ? resp.getMensaje() : "Error de conexión"));
        }
    }

    @FXML public void btnCancelar() { cerrarVentana(); }

    /**
     * Carga los datos de un colaborador existente para editarlo.
     * Incluye la descarga "en vivo" de la foto de perfil.
     */
    public void setColaborador(Colaborador colaborador) {
        if (colaborador != null) {
            this.esEdicion = true;
            this.colaboradorOriginal = colaborador;

            // Llenado de campos
            txtNumPersonal.setText(colaborador.getNumeroPersonal());
            txtNumPersonal.setDisable(true); // ID no editable
            txtNombre.setText(colaborador.getNombre());
            txtPaterno.setText(colaborador.getApellidoPaterno());
            txtMaterno.setText(colaborador.getApellidoMaterno());
            txtCurp.setText(colaborador.getCurp());
            txtCorreo.setText(colaborador.getCorreoElectronico());
            txtPassword.setText(colaborador.getContrasenia());

            // Seleccionar sucursal
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

            // Lógica Conductor (Mostrar campos y seleccionar unidad)
            if ("Conductor".equalsIgnoreCase(colaborador.getRol())) {
                mostrarCamposConductor(true);
                txtLicencia.setText(colaborador.getNumeroLicencia());

                String idUnidad = colaborador.getIdUnidadAsignada();
                if (idUnidad != null && !idUnidad.isEmpty()) {
                    for (Unidad u : cbUnidad.getItems()) {
                        if (u.getVin().equals(idUnidad)) {
                            cbUnidad.setValue(u);
                            break;
                        }
                    }
                } else {
                    // Si es conductor sin unidad (Suplente), seleccionamos la opción dummy
                    if (!cbUnidad.getItems().isEmpty()) {
                        cbUnidad.getSelectionModel().select(0);
                    }
                }
            }

            // --- DESCARGAR FOTO ---
            try {
                // Solicitamos la foto al servidor (GET) para tener la versión más reciente
                Colaborador colFoto = ApiService.obtenerFoto(colaborador.getNumeroPersonal());

                if (colFoto != null && colFoto.getFotografia() != null && !colFoto.getFotografia().isEmpty()) {
                    // Decodificamos Base64 -> Imagen
                    String base64Limpio = colFoto.getFotografia().replaceAll("\\s", "");
                    byte[] imgBytes = Base64.getDecoder().decode(base64Limpio);
                    Image img = new Image(new ByteArrayInputStream(imgBytes));

                    if (!img.isError()) {
                        imgFoto.setImage(img);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error al descargar foto: " + e.getMessage());
            }
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void cerrarVentana() {
        Stage stage = (Stage) txtNumPersonal.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarCamposConductor(boolean mostrar) {
        lblLicencia.setVisible(mostrar); lblLicencia.setManaged(mostrar);
        txtLicencia.setVisible(mostrar); txtLicencia.setManaged(mostrar);
        lblUnidad.setVisible(mostrar);   lblUnidad.setManaged(mostrar);
        cbUnidad.setVisible(mostrar);    cbUnidad.setManaged(mostrar);

        if (mostrar && cbUnidad.getItems().isEmpty()) {
            cargarUnidades();
        }
    }

    /**
     * Carga unidades disponibles filtrando las que ya están ocupadas por otros conductores.
     */
    private void cargarUnidades() {
        List<Unidad> todasLasUnidades = ApiService.obtenerUnidades();
        List<Colaborador> conductores = ApiService.buscarColaboradores(null, "Conductor", null);

        // Detectar VINs ocupados
        Set<String> vinsOcupados = new HashSet<>();
        for (Colaborador c : conductores) {
            if (c.getIdUnidadAsignada() != null && !c.getIdUnidadAsignada().isEmpty()) {
                vinsOcupados.add(c.getIdUnidadAsignada());
            }
        }

        List<Unidad> unidadesDisponibles = new ArrayList<>();

        // Opción 1: Suplente
        Unidad unidadNula = new Unidad();
        unidadNula.setVin("SIN_ASIGNAR");
        unidadNula.setMarca("--- SIN UNIDAD / SUPLENTE ---");
        unidadNula.setModelo("");
        unidadesDisponibles.add(unidadNula);

        String miUnidadActual = (esEdicion && colaboradorOriginal != null) ? colaboradorOriginal.getIdUnidadAsignada() : null;

        // Filtrar
        for (Unidad u : todasLasUnidades) {
            boolean estaLibre = !vinsOcupados.contains(u.getVin());
            boolean esLaMia = (miUnidadActual != null && miUnidadActual.equals(u.getVin()));

            if ("activo".equalsIgnoreCase(u.getEstatus()) && (estaLibre || esLaMia)) {
                unidadesDisponibles.add(u);
            }
        }

        cbUnidad.setItems(FXCollections.observableArrayList(unidadesDisponibles));

        // Decorador visual para el combo
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