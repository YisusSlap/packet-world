package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // UX Moderna
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList; // Importante
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el Catálogo de Colaboradores (Empleados).
 * Muestra lista general con capacidad de filtrado y gestión CRUD.
 */
public class ColaboradoresController {

    @FXML private TableView<Colaborador> tblColaboradores;
    @FXML private TableColumn<Colaborador, String> colNumPersonal;
    @FXML private TableColumn<Colaborador, String> colNombre;
    @FXML private TableColumn<Colaborador, String> colRol;
    @FXML private TableColumn<Colaborador, String> colCorreo;
    @FXML private TableColumn<Colaborador, String> colSucursal;
    @FXML private TableColumn<Colaborador, String> colCurp;
    @FXML private TableColumn<Colaborador, String> colLicencia;
    @FXML private TableColumn<Colaborador, String> colEstatus;
    @FXML private TableColumn<Colaborador, String> colUnidad;

    @FXML private TextField txtBuscar;

    // Listas para filtrado local eficiente
    private ObservableList<Colaborador> listaMaster;
    private FilteredList<Colaborador> listaFiltrada;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();

        // Listener para búsqueda LOCAL (Sin llamar a la API a cada tecla)
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (listaFiltrada == null) return;

            listaFiltrada.setPredicate(col -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lower = newValue.toLowerCase();

                // Buscar por Nombre, Numero Personal o Rol
                return col.getNombre().toLowerCase().contains(lower) ||
                        col.getApellidoPaterno().toLowerCase().contains(lower) ||
                        col.getNumeroPersonal().toLowerCase().contains(lower) ||
                        col.getRol().toLowerCase().contains(lower);
            });
        });
    }

    private void configurarColumnas() {
        colNumPersonal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroPersonal()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNombre() + " " + c.getValue().getApellidoPaterno() + " " + c.getValue().getApellidoMaterno()
        ));
        colRol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRol()));
        colCorreo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorreoElectronico()));
        colSucursal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIdCodigoSucursal()));
        colCurp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCurp()));

        colLicencia.setCellValueFactory(c -> {
            String licencia = c.getValue().getNumeroLicencia();
            return new SimpleStringProperty(licencia != null ? licencia : "N/A");
        });

        colEstatus.setCellValueFactory(c -> {
            String estatus = c.getValue().getEstatus();
            return new SimpleStringProperty(estatus != null ? estatus.toUpperCase() : "INACTIVO");
        });

        colUnidad.setCellValueFactory(c -> {
            String unidad = c.getValue().getIdUnidadAsignada();
            if (unidad == null || unidad.isEmpty()) {
                return new SimpleStringProperty("---");
            }
            return new SimpleStringProperty("🚛 " + unidad);
        });
    }

    private void cargarDatos() {
        // Obtenemos TODOS los colaboradores de una vez
        List<Colaborador> resultados = ApiService.buscarColaboradores(null, null, null);

        listaMaster = FXCollections.observableArrayList(resultados);

        // Configuramos el filtro
        listaFiltrada = new FilteredList<>(listaMaster, p -> true);

        // Configuramos el ordenamiento
        SortedList<Colaborador> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tblColaboradores.comparatorProperty());

        tblColaboradores.setItems(sortedData);
    }

    // --- ACCIONES ---

    @FXML
    public void btnNuevo() {
        abrirFormulario(null);
    }

    @FXML
    public void btnEditar() {
        Colaborador seleccionado = tblColaboradores.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            abrirFormulario(seleccionado);
        } else {
            mostrarAlerta("Atención", "Selecciona un colaborador para editar.");
        }
    }

    @FXML
    public void btnEliminar() {
        Colaborador seleccionado = tblColaboradores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un colaborador para eliminar.");
            return;
        }

        // --- VALIDACIÓN 1: UNIDAD ASIGNADA ---
        // Verificamos si tiene el campo 'idUnidadAsignada' lleno
        if (seleccionado.getIdUnidadAsignada() != null && !seleccionado.getIdUnidadAsignada().isEmpty()) {
            mostrarAlerta("Operación Denegada",
                    "No se puede eliminar al colaborador porque tiene la UNIDAD " +
                            seleccionado.getIdUnidadAsignada() + " asignada.\n\n" +
                            "Por favor, edite el colaborador y retire la unidad primero.");
            return;
        }

        // --- VALIDACIÓN 2: ENVÍOS ACTIVOS (Solo si es conductor) ---
        if ("Conductor".equalsIgnoreCase(seleccionado.getRol())) {
            // Consultamos a la API los envíos de este conductor
            List<Envio> misEnvios = ApiService.obtenerEnviosPorConductor(seleccionado.getNumeroPersonal());

            // Buscamos si hay alguno que NO esté concluido
            boolean tieneActivos = false;
            String guiaActiva = "";

            for (Envio e : misEnvios) {
                String estatus = e.getEstatusActual() != null ? e.getEstatusActual().toLowerCase() : "";
                // Si no está entregado ni cancelado, sigue activo
                if (!estatus.contains("entregado") && !estatus.contains("cancelado")) {
                    tieneActivos = true;
                    guiaActiva = e.getNumeroGuia();
                    break;
                }
            }

            if (tieneActivos) {
                mostrarAlerta("Operación Denegada",
                        "El conductor tiene envíos ACTIVOS (Ej. Guía " + guiaActiva + ").\n\n" +
                                "Debe reasignar o concluir los envíos antes de eliminarlo.");
                return;
            }
        }

        // --- SI PASA LAS VALIDACIONES, CONFIRMAMOS ---
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Estás seguro de eliminar a " + seleccionado.getNombre() + "?\nEsta acción es irreversible.");

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            Respuesta resp = ApiService.eliminarColaborador(seleccionado.getNumeroPersonal());

            if (resp != null && !resp.getError()) {
                Notificacion.mostrar("Eliminado", "Colaborador dado de baja.", Notificacion.EXITO);
                cargarDatos();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar: " + (resp != null ? resp.getMensaje() : "Error de conexión"));
            }
        }
    }

    private void abrirFormulario(Colaborador colaboradorAEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/FormularioColaborador.fxml"));
            Parent root = loader.load();

            FormularioColaboradorController controller = loader.getController();
            controller.setColaborador(colaboradorAEditar);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            com.example.packetworld.util.Tema.aplicar(scene); // Aplicamos el tema
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(colaboradorAEditar == null ? "Nuevo Colaborador" : "Editar Colaborador");
            stage.showAndWait();

            cargarDatos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana del formulario.");
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}