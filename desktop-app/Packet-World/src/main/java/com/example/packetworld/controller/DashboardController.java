package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del Tablero Principal (Dashboard).
 * Muestra KPIs en tiempo real, lista de pendientes y un mapa de calor interactivo.
 */
public class DashboardController {

    @FXML private Label lblFecha;
    @FXML private Label lblEnTransito, lblPorAsignar, lblEntregados, lblUnidades;
    @FXML private ListView<String> listPendientes;

    // Componentes del Mapa Web (Google GeoChart)
    @FXML private WebView webViewMapa;
    private WebEngine webEngine;
    private boolean mapaListo = false; // Bandera para saber si el HTML ya cargó

    @FXML
    public void initialize() {
        // Fecha bonita en español
        lblFecha.setText("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));

        // --- INICIALIZACIÓN DEL MAPA ---
        if (webViewMapa != null) {
            webEngine = webViewMapa.getEngine();

            // Cargar el HTML local que contiene el JS de Google Charts
            URL url = getClass().getResource("/com/example/packetworld/html/mapa.html");
            if (url != null) {
                webEngine.load(url.toExternalForm());
            } else {
                System.out.println("⚠️ ERROR CRÍTICO: No se encontró el archivo /html/mapa.html");
            }

            // Listener: Esperar a que la página cargue completamente antes de mandarle datos
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    mapaListo = true;
                    cargarDatos(); // Ahora sí, inyectamos los datos al mapa
                }
            });
        } else {
            // Fallback: Si el WebView falla, al menos cargamos los números
            cargarDatos();
        }
    }

    /**
     * Descarga toda la información del servidor y calcula las métricas.
     */
    @FXML
    public void cargarDatos() {
        // Obtenemos datos frescos
        List<Envio> todosEnvios = ApiService.obtenerTodosEnvios();
        List<Unidad> todasUnidades = ApiService.obtenerUnidades();

        // --- CÁLCULO DE KPIs (Key Performance Indicators) ---
        int transitCount = 0;
        int unassignedCount = 0;
        int deliveredCount = 0;

        for (Envio e : todosEnvios) {
            String estatus = e.getEstatusActual().toLowerCase();

            if (estatus.contains("tránsito") || estatus.contains("ruta")) transitCount++;
            else if (estatus.contains("entregado")) deliveredCount++;

            // Lógica de "Por Asignar": Activo pero sin conductor
            boolean esActivo = !estatus.contains("entregado") && !estatus.contains("cancelado");
            if (esActivo && (e.getIdConductorAsignado() == null || e.getIdConductorAsignado().isEmpty())) {
                unassignedCount++;
            }
        }

        long unidadesLibres = todasUnidades.stream().filter(u -> "activo".equalsIgnoreCase(u.getEstatus())).count();

        // Actualizar etiquetas
        lblEnTransito.setText(String.valueOf(transitCount));
        lblPorAsignar.setText(String.valueOf(unassignedCount));
        lblEntregados.setText(String.valueOf(deliveredCount));
        lblUnidades.setText(String.valueOf(unidadesLibres));

        // --- LISTA DE PENDIENTES (Top 10 urgentes) ---
        ObservableList<String> itemsPendientes = FXCollections.observableArrayList();
        for (Envio e : todosEnvios) {
            // Priorizamos los que no tienen conductor
            if (e.getIdConductorAsignado() == null || e.getIdConductorAsignado().isEmpty()) {
                if (itemsPendientes.size() < 10) {
                    itemsPendientes.add("⚠️ Guía " + e.getNumeroGuia() + " -> " + e.getCiudad());
                }
            }
        }
        if (itemsPendientes.isEmpty()) itemsPendientes.add("¡Todo al día! 🎉 No hay pendientes.");
        listPendientes.setItems(itemsPendientes);

        // --- ENVIAR DATOS AL MAPA ---
        if (mapaListo) {
            actualizarMapaCalor(todosEnvios);
        }
    }

    /**
     * Procesa los envíos por estado geográfico y los envía al JavaScript del mapa.
     */
    private void actualizarMapaCalor(List<Envio> envios) {
        Map<String, Integer> conteo = new HashMap<>();

        for (Envio e : envios) {
            String estadoNombre = e.getEstado();
            String isoCode = obtenerCodigoISO(estadoNombre);

            if (!isoCode.isEmpty()) {
                conteo.put(isoCode, conteo.getOrDefault(isoCode, 0) + 1);
            }
        }

        // Construir JSON manualmente para Google Charts: [['Estado', 'Envíos'], ['MX-VER', 5], ...]
        StringBuilder sb = new StringBuilder("[['Estado', 'Envíos']");
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            sb.append(",['").append(entry.getKey()).append("', ").append(entry.getValue()).append("]");
        }
        sb.append("]");

        String jsonFinal = sb.toString();

        // Ejecutar JS en el hilo de JavaFX
        Platform.runLater(() -> {
            try {
                webEngine.executeScript("actualizarMapa(" + jsonFinal + ")");
            } catch (Exception ex) {
                System.out.println("Error comunicando con JS: " + ex.getMessage());
            }
        });
    }

    /**
     * Convierte nombres de estados (ej. "Nuevo León") a códigos ISO-3166-2 (ej. "MX-NLE").
     * Es vital para que el mapa entienda dónde pintar.
     */
    private String obtenerCodigoISO(String nombreBD) {
        if (nombreBD == null) return "";
        String nombre = nombreBD.toLowerCase().trim();

        // Mapeo manual de nombres comunes a códigos ISO
        if (nombre.contains("aguascalientes")) return "MX-AGU";
        if (nombre.contains("baja california") && !nombre.contains("sur")) return "MX-BCN";
        if (nombre.contains("baja california sur")) return "MX-BCS";
        if (nombre.contains("campeche")) return "MX-CAM";
        if (nombre.contains("coahuila")) return "MX-COA";
        if (nombre.contains("colima")) return "MX-COL";
        if (nombre.contains("chiapas")) return "MX-CHP";
        if (nombre.contains("chihuahua")) return "MX-CHH";
        if (nombre.contains("ciudad de méxico") || nombre.contains("cdmx") || nombre.contains("distrito")) return "MX-DIF";
        if (nombre.contains("durango")) return "MX-DUR";
        if (nombre.contains("guanajuato")) return "MX-GUA";
        if (nombre.contains("guerrero")) return "MX-GRO";
        if (nombre.contains("hidalgo")) return "MX-HID";
        if (nombre.contains("jalisco")) return "MX-JAL";
        if (nombre.contains("michoacán") || nombre.contains("michoacan")) return "MX-MIC";
        if (nombre.contains("morelos")) return "MX-MOR";
        // Nota: "México" a secas suele ser Estado de México
        if (nombre.equals("méxico") || nombre.equals("mexico") || nombre.contains("estado de")) return "MX-MEX";
        if (nombre.contains("nayarit")) return "MX-NAY";
        if (nombre.contains("nuevo león") || nombre.contains("nuevo leon")) return "MX-NLE";
        if (nombre.contains("oaxaca")) return "MX-OAX";
        if (nombre.contains("puebla")) return "MX-PUE";
        if (nombre.contains("querétaro") || nombre.contains("queretaro")) return "MX-QUE";
        if (nombre.contains("quintana")) return "MX-ROO";
        if (nombre.contains("san luis")) return "MX-SLP";
        if (nombre.contains("sinaloa")) return "MX-SIN";
        if (nombre.contains("sonora")) return "MX-SON";
        if (nombre.contains("tabasco")) return "MX-TAB";
        if (nombre.contains("tamaulipas")) return "MX-TAM";
        if (nombre.contains("tlaxcala")) return "MX-TLA";
        if (nombre.contains("veracruz")) return "MX-VER"; // ¡Tu estado! 🌴
        if (nombre.contains("yucatán") || nombre.contains("yucatan")) return "MX-YUC";
        if (nombre.contains("zacatecas")) return "MX-ZAC";

        return ""; // No encontrado
    }
}