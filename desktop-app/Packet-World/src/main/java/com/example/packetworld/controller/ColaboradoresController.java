package com.example.packetworld.controller;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    // Si tienes un TextField en tu FXML para buscar, añádelo aquí:
    @FXML private TextField txtBuscar;

    // Para la unidad asignada
    @FXML private TableColumn<Colaborador, String> colUnidad; // Declárala arriba

    private ObservableList<Colaborador> listaColaboradores;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos(""); // Carga inicial sin filtros

        // Listener para búsqueda (opcional, si tienes el TextField)
         txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            cargarDatos(newValue); // Buscará mientras escribes
        });

        colUnidad.setCellValueFactory(c -> {
            String unidad = c.getValue().getIdUnidadAsignada();
            if (unidad == null || unidad.isEmpty()) {
                return new SimpleStringProperty("---");
            }
            return new SimpleStringProperty("🚛 " + unidad);
        });

    }

    private void configurarColumnas() {
        // Enlazar datos con columnas usando lambdas
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
    }

    private void cargarDatos(String filtroNombre) {
        // Llamamos al servicio 'buscar' enviando el filtro de nombre
        // Rol y Sucursal los mandamos null para que traiga todo
        List<Colaborador> resultados = ApiService.buscarColaboradores(filtroNombre, null, null);

        listaColaboradores = FXCollections.observableArrayList(resultados);
        tblColaboradores.setItems(listaColaboradores);
    }

    //Botones
    @FXML
    public void btnNuevo() {
        abrirFormulario(null); // Null significa crear nuevo
    }

    @FXML
    public void btnEditar() {
        Colaborador seleccionado = tblColaboradores.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            // AHORA SÍ: Pasamos el objeto seleccionado al método de apertura
            abrirFormulario(seleccionado);
        } else {
            mostrarAlerta("Atención", "Selecciona un colaborador para editar.");
        }
    }

    @FXML
    public void btnEliminar() {
        Colaborador seleccionado = tblColaboradores.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar eliminación");
            confirm.setContentText("¿Estás seguro de eliminar a " + seleccionado.getNombre() + "?");

            Optional<ButtonType> resultado = confirm.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                // Llamar a la API
                Respuesta resp = ApiService.eliminarColaborador(seleccionado.getNumeroPersonal());
                if (resp != null && !resp.getError()) {
                    mostrarAlerta("Éxito", "Colaborador eliminado.");
                    cargarDatos(""); // Recargar tabla
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar.");
                }
            }
        } else {
            mostrarAlerta("Atención", "Selecciona un colaborador para eliminar.");
        }
    }

    // Método auxiliar para abrir la ventana modal
    private void abrirFormulario(Colaborador colaboradorAEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/FormularioColaborador.fxml"));
            Parent root = loader.load();

            // --- LÓGICA DE INYECCIÓN DE DATOS ---
            // 1. Obtenemos el controlador de la ventana que acabamos de cargar
            FormularioColaboradorController controller = loader.getController();

            // 2. Le pasamos el colaborador (si es null, el formulario sabrá que es nuevo)
            controller.setColaborador(colaboradorAEditar);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(colaboradorAEditar == null ? "Nuevo Colaborador" : "Editar Colaborador");
            stage.showAndWait();

            cargarDatos(""); // Recargar tabla al cerrar

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana del formulario.");
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }


}