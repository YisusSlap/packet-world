package com.example.packetworld.controller;

import com.example.packetworld.model.Cliente;
import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.service.ApiService;
import com.example.packetworld.util.Notificacion; // UX Moderna
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
 * Controlador para el Catálogo de Clientes.
 * Permite buscar, filtrar y gestionar la base de datos de clientes.
 */
public class ClientesController {

    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colDireccion;
    @FXML private TextField txtBuscar;

    // Listas para el manejo eficiente de datos en memoria (Filtrado rápido)
    private ObservableList<Cliente> listaMaster;
    private FilteredList<Cliente> listaFiltrada;

    @FXML
    public void initialize() {
        // Configurar Columnas
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNombre() + " " + c.getValue().getApellidoPaterno() + " " + c.getValue().getApellidoMaterno()
        ));
        colCorreo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorreoElectronico()));
        colTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));

        // Dirección formateada en una sola celda
        colDireccion.setCellValueFactory(c -> {
            Cliente cli = c.getValue();
            String dir = cli.getCalle() + " #" + cli.getNumero() + ", Col. " + cli.getNombreColonia();
            return new SimpleStringProperty(dir);
        });

        // Carga inicial
        cargarDatos();

        // Configuración del Buscador Local (Filtra sin ir al servidor)
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (listaFiltrada == null) return;

            listaFiltrada.setPredicate(cli -> {
                // Si está vacío, muestra todo
                if (newVal == null || newVal.isEmpty()) return true;

                String lower = newVal.toLowerCase();

                // Busca coincidencias en Nombre, Correo o Teléfono
                return cli.getNombre().toLowerCase().contains(lower) ||
                        cli.getApellidoPaterno().toLowerCase().contains(lower) ||
                        cli.getCorreoElectronico().toLowerCase().contains(lower) ||
                        cli.getTelefono().contains(lower);
            });
        });
    }

    private void cargarDatos() {
        // Traemos todos los clientes de una sola vez
        listaMaster = FXCollections.observableArrayList(ApiService.buscarClientes(null, null, null));

        // Envolvemos en FilteredList para poder filtrar
        listaFiltrada = new FilteredList<>(listaMaster, p -> true);

        // Envolvemos en SortedList para poder ordenar por columna
        SortedList<Cliente> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tblClientes.comparatorProperty());

        tblClientes.setItems(sortedData);
    }

    // --- ACCIONES ---

    @FXML public void btnNuevo() { abrirFormulario(null); }

    @FXML public void btnEditar() {
        Cliente c = tblClientes.getSelectionModel().getSelectedItem();
        if (c != null) abrirFormulario(c);
        else mostrarAlerta("Atención", "Selecciona un cliente de la lista.");
    }

    @FXML public void btnEliminar() {
        Cliente c = tblClientes.getSelectionModel().getSelectedItem();

        // 1. Validar selección
        if (c == null) {
            mostrarAlerta("Atención", "Selecciona un cliente para eliminar.");
            return;
        }

        // --- VALIDACIÓN NUEVA: ENVÍOS PENDIENTES ---
        // Obtenemos todos los envíos (Idealmente tu API debería tener un "obtenerEnviosPorCliente",
        // pero filtrar la lista general funciona para este propósito).
        List<Envio> todosLosEnvios = ApiService.obtenerTodosEnvios();

        boolean tienePendientes = false;
        String guiaPendiente = "";

        for (Envio e : todosLosEnvios) {
            // Verificamos si el envío pertenece a este cliente
            if (e.getIdCliente() != null && e.getIdCliente().equals(c.getIdCliente())) {

                // Verificamos el estatus
                String estatus = e.getEstatusActual() != null ? e.getEstatusActual().toLowerCase() : "";

                // Si NO está entregado Y NO está cancelado, está activo/pendiente
                if (!estatus.contains("entregado") && !estatus.contains("cancelado")) {
                    tienePendientes = true;
                    guiaPendiente = e.getNumeroGuia();
                    break; // Con encontrar uno basta para bloquear
                }
            }
        }

        if (tienePendientes) {
            mostrarAlerta("Operación Denegada",
                    "No se puede eliminar al cliente " + c.getNombre() + ".\n\n" +
                            "Tiene envíos activos en curso (Ej. Guía: " + guiaPendiente + ").\n" +
                            "Debe esperar a que se entreguen o cancelarlos primero.");
            return;
        }
        // -------------------------------------------

        // Confirmación y Borrado
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de eliminar a " + c.getNombre() + "?\nEsta acción es irreversible.");
        confirm.setHeaderText(null);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Respuesta r = ApiService.eliminarCliente(c.getIdCliente());

            if (r != null && !r.getError()) {
                Notificacion.mostrar("Eliminado", "Cliente eliminado correctamente.", Notificacion.EXITO);
                cargarDatos();
            } else {
                // Aquí capturamos si la BD falla por alguna llave foránea histórica
                String msgError = r != null ? r.getMensaje() : "Error desconocido";
                if(msgError.contains("foreign key") || msgError.contains("constraint")) {
                    msgError = "El cliente tiene historial de envíos antiguos y no se puede borrar por seguridad.";
                }
                mostrarAlerta("Error", "No se pudo eliminar: " + msgError);
            }
        }
    }

    private void abrirFormulario(Cliente c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/FormularioCliente.fxml"));
            Parent root = loader.load();

            FormularioClienteController ctrl = loader.getController();
            ctrl.setCliente(c);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            com.example.packetworld.util.Tema.aplicar(scene); // Aplicamos el tema
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(c == null ? "Nuevo Cliente" : "Editar Cliente");
            stage.showAndWait();

            cargarDatos(); // Recargar al cerrar
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}