package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.HistorialEstatus;
import com.example.packetworld.model.Paquete;
import com.example.packetworld.service.ApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.Printer;
import javafx.print.PrinterJob; // <--- Importante para imprimir
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
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

    private Envio envioActual;

    @FXML
    public void initialize() {
        // Configurar Tabla Paquetes
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPesoKg() + " kg"));
        colMedidas.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDimAltoCm() + "x" + c.getValue().getDimAnchoCm() + "x" + c.getValue().getDimProfundidadCm()
        ));

        // Configurar Tabla Historial
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaCambio()));

        // Usamos el nombre bonito si ya actualizaste el POJO, si no usa getIdEstatus()
        colEstatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreEstatus()));

        colComentario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComentario()));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroPersonalColaborador()));
    }

    public void setEnvio(Envio envioSimple) {
        if (envioSimple != null) {
            this.envioActual = ApiService.rastrearEnvio(envioSimple.getNumeroGuia());
            if (this.envioActual == null) this.envioActual = envioSimple;

            // Llenar Datos
            lblGuia.setText(envioActual.getNumeroGuia());
            lblEstatusHeader.setText(envioActual.getEstatusActual().toUpperCase());
            lblCliente.setText(envioActual.getNombreCliente());
            lblOrigen.setText(envioActual.getCodigoSucursalOrigen());
            lblDestinatario.setText(envioActual.getDestinatarioNombre() + " " + envioActual.getDestinatarioAp1());
            lblCosto.setText("$" + envioActual.getCostoTotal());

            String dir = String.format("%s #%s, Col. %s\nCP %s, %s, %s",
                    envioActual.getDestinoCalle(), envioActual.getDestinoNumero(),
                    envioActual.getNombreColonia(), envioActual.getCodigoPostal(),
                    envioActual.getCiudad(), envioActual.getEstado()
            );
            lblDireccionCompleta.setText(dir);

            String conductor = envioActual.getIdConductorAsignado();
            if (conductor == null || conductor.isEmpty()) {
                lblConductor.setText("🔴 PENDIENTE");
            } else {
                lblConductor.setText(conductor);
            }

            if (envioActual.getListaPaquetes() != null) {
                tblPaquetes.setItems(FXCollections.observableArrayList(envioActual.getListaPaquetes()));
            }
            if (envioActual.getHistorial() != null) {
                tblHistorial.setItems(FXCollections.observableArrayList(envioActual.getHistorial()));
            }
        }
    }

    // --- ACCIONES ---

    @FXML public void btnAsignar() { abrirModal("FormularioAsignacion.fxml", "Asignar Conductor"); }
    @FXML public void btnEstatus() { abrirModal("FormularioEstatus.fxml", "Cambiar Estatus"); }
    @FXML
    public void btnPaquete() {
        // --- VALIDACIÓN DE NEGOCIO (Corrección #1) ---
        String estatus = envioActual.getEstatusActual().toLowerCase();

        if (estatus.contains("tránsito") || estatus.contains("ruta") || estatus.contains("entregado") || estatus.contains("cancelado")) {
            mostrarAlerta("OPERACIÓN DENEGADA:\nNo se pueden agregar paquetes a un envío que ya salió de sucursal o finalizó.");
            return;
        }
        // ---------------------------------------------

        abrirModal("FormularioPaquete.fxml", "Agregar Paquete");
    }

    @FXML
    public void imprimirEtiqueta() {
        if (envioActual == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/Etiqueta.fxml"));
            Parent nodoEtiqueta = loader.load();

            // Llenar datos (Usando tu EtiquetaController que está perfecto)
            EtiquetaController ctrl = loader.getController();
            ctrl.setDatos(envioActual);

            // Buscar impresora PDF si no hay default
            Printer printer = Printer.getDefaultPrinter();
            if (printer == null) {
                for (Printer p : Printer.getAllPrinters()) {
                    if (p.getName().contains("PDF")) {
                        printer = p;
                        break;
                    }
                }
            }

            PrinterJob job = (printer != null) ? PrinterJob.createPrinterJob(printer) : PrinterJob.createPrinterJob();

            if (job != null) {
                // ✨ AQUÍ ESTÁ LA MAGIA ✨
                // Le decimos a la impresora: "El trabajo se llama así".
                // Microsoft Print to PDF usa esto como nombre de archivo sugerido.
                job.getJobSettings().setJobName("Guia_" + envioActual.getNumeroGuia());

                boolean proceder = job.showPrintDialog(lblGuia.getScene().getWindow());

                if (proceder) {
                    boolean impreso = job.printPage(nodoEtiqueta);
                    if (impreso) {
                        job.endJob();
                        mostrarInfo("Éxito", "Enviado a impresora: " + job.getPrinter().getName());
                    } else {
                        mostrarAlerta("Falló la impresión.");
                    }
                }
            } else {
                // Plan B: Guardar imagen
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Java no detectó impresoras activas.\n¿Deseas guardar la guía como imagen PNG?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        guardarComoImagen(nodoEtiqueta);
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error al generar etiqueta: " + e.getMessage());
        }
    }

    private void guardarComoImagen(Parent nodo) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Etiqueta");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen PNG", "*.png"));

        // ✨ AQUÍ ESTÁ LA OTRA MAGIA (Para el Plan B) ✨
        fileChooser.setInitialFileName("Guia_" + envioActual.getNumeroGuia() + ".png");

        File file = fileChooser.showSaveDialog(lblGuia.getScene().getWindow());

        if (file != null) {
            try {
                WritableImage snapshot = nodo.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                mostrarInfo("Guardado", "Etiqueta guardada exitosamente.");
            } catch (IOException ex) {
                mostrarAlerta("Error al guardar imagen: " + ex.getMessage());
            }
        }
    }

    private void abrirModal(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/packetworld/view/modulos/" + fxml));
            Parent root = loader.load();

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
            setEnvio(envioActual);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML public void cerrar() { ((Stage) lblGuia.getScene().getWindow()).close(); }

    // --- MÉTODOS QUE FALTABAN ---
    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Importar java.net.URLEncoder y java.nio.charset.StandardCharsets

    @FXML
    public void abrirGoogleMaps() {
        if (envioActual == null) return;

        try {
            // Construimos la dirección completa
            String direccion = String.format("%s %s, %s, %s, %s",
                    envioActual.getDestinoCalle(),
                    envioActual.getDestinoNumero(),
                    envioActual.getNombreColonia(),
                    envioActual.getCiudad(),
                    envioActual.getEstado()
            );

            // Codificamos para URL (espacios -> %20, etc.)
            String direccionEncoded = java.net.URLEncoder.encode(direccion, "UTF-8");

            // URL Mágica de Google Maps
            String url = "https://www.google.com/maps/search/?api=1&query=" + direccionEncoded;

            // Llamamos al método estático de App
            com.example.packetworld.App.abrirNavegador(url);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo abrir el mapa.");
        }
    }

}

