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

public class DashboardController {

    @FXML private Label lblFecha;
    @FXML private Label lblEnTransito, lblPorAsignar, lblEntregados, lblUnidades;
    @FXML private ListView<String> listPendientes;

    // Elementos del Mapa
    @FXML private WebView webViewMapa;
    private WebEngine webEngine;
    private boolean mapaListo = false;

    @FXML
    public void initialize() {
        lblFecha.setText("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));

        // INICIALIZAR EL MAPA
        if (webViewMapa != null) {
            webEngine = webViewMapa.getEngine();
            // Cargar archivo HTML local
            URL url = getClass().getResource("/com/example/packetworld/html/mapa.html");
            if (url != null) {
                webEngine.load(url.toExternalForm());
            } else {
                System.out.println("⚠️ ERROR: No se encontró /html/mapa.html");
            }

            // Esperar a que cargue el HTML para inyectar datos
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    mapaListo = true;
                    cargarDatos(); // Cargamos datos cuando el mapa esté listo
                }
            });
        } else {
            // Si por alguna razón falla el FXML, cargamos al menos los datos numéricos
            cargarDatos();
        }
    }

    @FXML
    public void cargarDatos() {
        List<Envio> todosEnvios = ApiService.obtenerTodosEnvios();
        List<Unidad> todasUnidades = ApiService.obtenerUnidades();

        // --- CÁLCULO DE KPIs ---
        int transitCount = 0;
        int unassignedCount = 0;
        int deliveredCount = 0;

        for (Envio e : todosEnvios) {
            String estatus = e.getEstatusActual().toLowerCase();

            if (estatus.contains("tránsito") || estatus.contains("ruta")) transitCount++;
            else if (estatus.contains("entregado")) deliveredCount++;

            boolean esActivo = !estatus.contains("entregado") && !estatus.contains("cancelado");
            if (esActivo && (e.getIdConductorAsignado() == null || e.getIdConductorAsignado().isEmpty())) {
                unassignedCount++;
            }
        }

        long unidadesLibres = todasUnidades.stream().filter(u -> "activo".equalsIgnoreCase(u.getEstatus())).count();

        lblEnTransito.setText(String.valueOf(transitCount));
        lblPorAsignar.setText(String.valueOf(unassignedCount));
        lblEntregados.setText(String.valueOf(deliveredCount));
        lblUnidades.setText(String.valueOf(unidadesLibres));

        // --- LISTA DE PENDIENTES ---
        ObservableList<String> itemsPendientes = FXCollections.observableArrayList();
        for (Envio e : todosEnvios) {
            if (e.getIdConductorAsignado() == null || e.getIdConductorAsignado().isEmpty()) {
                if (itemsPendientes.size() < 10) {
                    itemsPendientes.add(e.getNumeroGuia() + " - " + e.getCiudad());
                }
            }
        }
        if (itemsPendientes.isEmpty()) itemsPendientes.add("¡Todo al día! 🎉");
        listPendientes.setItems(itemsPendientes);

        // --- ACTUALIZAR EL MAPA ---
        if (mapaListo) {
            actualizarMapaCalor(todosEnvios);
        }
    }

    private void actualizarMapaCalor(List<Envio> envios) {
        System.out.println("--- INICIANDO MAPA DE CALOR ---");
        System.out.println("Total envíos recibidos: " + envios.size());

        Map<String, Integer> conteo = new HashMap<>();

        for (Envio e : envios) {
            String estado = e.getEstado();
            String guia = e.getNumeroGuia();

            // EL CHISMOSO 1: ¿Qué tiene el estado?
            System.out.println("Guía: " + guia + " | Estado RAW: '" + estado + "'");

            String iso = obtenerCodigoISO(estado);

            // EL CHISMOSO 2: ¿En qué se convirtió?
            System.out.println("   -> Código ISO: '" + iso + "'");

            if (!iso.isEmpty()) {
                conteo.put(iso, conteo.getOrDefault(iso, 0) + 1);
            }
        }

        // Construcción del JSON
        StringBuilder sb = new StringBuilder("[['Estado', 'Envíos']");
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            sb.append(",['").append(entry.getKey()).append("', ").append(entry.getValue()).append("]");
        }
        sb.append("]");

        // EL CHISMOSO 3: ¿Qué le mandamos al HTML?
        String jsonFinal = sb.toString();
        System.out.println("JSON ENVIADO AL MAPA: " + jsonFinal);
        System.out.println("---------------------------------");

        Platform.runLater(() -> {
            webEngine.executeScript("actualizarMapa(" + jsonFinal + ")");
        });
    }

    private String obtenerCodigoISO(String nombreBD) {
        if (nombreBD == null) return "";

        // 1. Normalizar: Todo a minúsculas para comparar fácil
        String nombre = nombreBD.toLowerCase().trim();

        // 2. Comparaciones (Usando minúsculas y palabras clave únicas)
        if (nombre.contains("aguascalientes")) return "MX-AGU";
        if (nombre.contains("baja california") && !nombre.contains("sur")) return "MX-BCN";
        if (nombre.contains("baja california sur")) return "MX-BCS";
        if (nombre.contains("campeche")) return "MX-CAM";
        if (nombre.contains("coahuila")) return "MX-COA";
        if (nombre.contains("colima")) return "MX-COL";
        if (nombre.contains("chiapas")) return "MX-CHP";
        if (nombre.contains("chihuahua")) return "MX-CHH";

        // CDMX tiene muchas variantes
        if (nombre.contains("ciudad de méxico") || nombre.contains("cdmx") || nombre.contains("distrito federal")) return "MX-DIF";

        if (nombre.contains("durango")) return "MX-DUR";

        // Estado de México vs Ciudad de México (Cuidado aquí)
        // Como ya filtramos CDMX arriba, si dice "mexico" o "méxico" suele ser el EdoMex
        if (nombre.contains("estado de méxico") || nombre.contains("estado de mexico") || nombre.equals("méxico") || nombre.equals("mexico")) return "MX-MEX";

        if (nombre.contains("guanajuato")) return "MX-GUA";
        if (nombre.contains("guerrero")) return "MX-GRO";
        if (nombre.contains("hidalgo")) return "MX-HID";
        if (nombre.contains("jalisco")) return "MX-JAL";
        if (nombre.contains("michoacán") || nombre.contains("michoacan")) return "MX-MIC";
        if (nombre.contains("morelos")) return "MX-MOR";
        if (nombre.contains("nayarit")) return "MX-NAY";
        if (nombre.contains("nuevo león") || nombre.contains("nuevo leon")) return "MX-NLE";
        if (nombre.contains("oaxaca")) return "MX-OAX";
        if (nombre.contains("puebla")) return "MX-PUE";
        if (nombre.contains("querétaro") || nombre.contains("queretaro")) return "MX-QUE";
        if (nombre.contains("quintana roo")) return "MX-ROO";
        if (nombre.contains("san luis potosí") || nombre.contains("san luis potosi")) return "MX-SLP";
        if (nombre.contains("sinaloa")) return "MX-SIN";
        if (nombre.contains("sonora")) return "MX-SON";
        if (nombre.contains("tabasco")) return "MX-TAB";
        if (nombre.contains("tamaulipas")) return "MX-TAM";
        if (nombre.contains("tlaxcala")) return "MX-TLA";
        if (nombre.contains("veracruz")) return "MX-VER";
        if (nombre.contains("yucatán") || nombre.contains("yucatan")) return "MX-YUC";
        if (nombre.contains("zacatecas")) return "MX-ZAC";

        // Debug para encontrar los rebeldes
        System.out.println("⚠️ Estado no reconocido para el mapa: " + nombreBD);
        return "";
    }

}