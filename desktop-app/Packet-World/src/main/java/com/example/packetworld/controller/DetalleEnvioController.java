package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.HistorialEstatus;
import com.example.packetworld.model.Paquete;
import com.example.packetworld.service.ApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class DetalleEnvioController {

    // Header y Datos Generales
    @FXML private Label lblGuia, lblEstatusHeader;
    @FXML private Label lblCliente, lblOrigen, lblConductor, lblDestinatario, lblDireccionCompleta, lblCosto;

    // Tabla Paquetes
    @FXML private TableView<Paquete> tblPaquetes;
    @FXML private TableColumn<Paquete, String> colDesc, colPeso, colMedidas;

    // Tabla Historial
    @FXML private TableView<HistorialEstatus> tblHistorial;
    @FXML private TableColumn<HistorialEstatus, String> colFecha, colEstatus, colComentario, colUsuario;

    private Envio envioActual; // Guardamos el objeto completo

    @FXML
    public void initialize() {
        // Configurar Tabla Paquetes
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPesoKg() + " kg"));
        colMedidas.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDimAltoCm() + "cm x" + c.getValue().getDimAnchoCm() + "cm x" + c.getValue().getDimProfundidadCm() + "cm"
        ));

        // Configurar Tabla Historial
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaCambio()));
        // Nota: HistorialEstatus ahora tiene idEstatus (Integer), necesitamos el nombre (String).
        // Si tu backend no manda el nombre en el historial, mostramos el ID o mapeamos manualmente.
        // Asumiendo que tu JSON sí trae "nombreEstatus" (lo vi en tu ejemplo):
        // Tendrías que agregar ese campo a tu POJO HistorialEstatus si no lo tienes.
        // Si no, mostramos el comentario que suele explicarlo.
        colEstatus.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdEstatus())));

        colComentario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComentario()));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroPersonalColaborador()));
    }

    public void setEnvio(Envio envioSimple) {
        if (envioSimple != null) {
            // Recargamos desde la API para asegurar tener Historial y Paquetes frescos
            this.envioActual = ApiService.rastrearEnvio(envioSimple.getNumeroGuia());
            if (this.envioActual == null) this.envioActual = envioSimple;

            // 1. Header
            lblGuia.setText(envioActual.getNumeroGuia());
            lblEstatusHeader.setText(envioActual.getEstatusActual().toUpperCase());

            // 2. Datos Generales
            lblCliente.setText(envioActual.getNombreCliente());
            lblOrigen.setText(envioActual.getCodigoSucursalOrigen());
            lblDestinatario.setText(envioActual.getDestinatarioNombre() + " " + envioActual.getDestinatarioAp1());
            lblCosto.setText("$" + envioActual.getCostoTotal());

            // Dirección Completa Concatenada
            String dir = String.format("%s #%s, Col. %s\nCP %s, %s, %s",
                    envioActual.getDestinoCalle(),
                    envioActual.getDestinoNumero(),
                    envioActual.getNombreColonia(),
                    envioActual.getCodigoPostal(),
                    envioActual.getCiudad(),
                    envioActual.getEstado()
            );
            lblDireccionCompleta.setText(dir);

            // Conductor
            String conductor = envioActual.getIdConductorAsignado();
            if (conductor == null || conductor.isEmpty()) {
                lblConductor.setText("🔴 PENDIENTE");
            } else {
                lblConductor.setText(conductor);
            }

            // 3. Tablas
            if (envioActual.getListaPaquetes() != null) {
                tblPaquetes.setItems(FXCollections.observableArrayList(envioActual.getListaPaquetes()));
            }
            if (envioActual.getHistorial() != null) {
                tblHistorial.setItems(FXCollections.observableArrayList(envioActual.getHistorial()));
            }
        }
    }

    // --- ACCIONES (Redundancia Útil) ---
    // Reutilizamos los formularios modales

    @FXML public void btnAsignar() { abrirModal("FormularioAsignacion.fxml", "Asignar Conductor"); }
    @FXML public void btnEstatus() { abrirModal("FormularioEstatus.fxml", "Cambiar Estatus"); }
    @FXML public void btnPaquete() { abrirModal("FormularioPaquete.fxml", "Agregar Paquete"); }

    private void abrirModal(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/" + fxml));
            Parent root = loader.load();

            // Pasamos el envío al controlador
            Object controller = loader.getController();
            if (controller instanceof FormularioAsignacionController)
                ((FormularioAsignacionController) controller).setEnvio(envioActual);
            else if (controller instanceof FormularioEstatusController)
                ((FormularioEstatusController) controller).setEnvio(envioActual);
            else if (controller instanceof FormularioPaqueteController)
                ((FormularioPaqueteController) controller).setIdEnvio(envioActual.getIdEnvio());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(titulo);
            stage.showAndWait();

            // IMPORTANTE: Recargar este mismo detalle para ver los cambios
            setEnvio(envioActual);

        } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML public void cerrar() { ((Stage) lblGuia.getScene().getWindow()).close(); }
}