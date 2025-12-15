package com.example.packetworld.controller;

import com.example.packetworld.model.Envio;
import com.example.packetworld.model.Unidad;
import com.example.packetworld.service.ApiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label lblFecha;
    @FXML private Label lblEnTransito, lblPorAsignar, lblEntregados, lblUnidades;
    @FXML private PieChart pieChartEstatus;
    @FXML private ListView<String> listPendientes;

    @FXML
    public void initialize() {
        // Poner fecha actual
        lblFecha.setText("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));

        cargarDatos();
    }

    @FXML
    public void cargarDatos() {
        // 1. Obtener Datos de la API
        List<Envio> todosEnvios = ApiService.obtenerTodosEnvios();
        List<Unidad> todasUnidades = ApiService.obtenerUnidades();

        // 2. Calcular KPIs (Contadores)
        int transitCount = 0;
        int unassignedCount = 0;
        int deliveredCount = 0;
        int pendingCount = 0; // Recibido en sucursal

        // Recorremos los envíos UNA SOLA VEZ para contar todo (Eficiencia)
        for (Envio e : todosEnvios) {
            String estatus = e.getEstatusActual().toLowerCase();

            // Lógica de Estatus
            if (estatus.contains("tránsito") || estatus.contains("ruta")) {
                transitCount++;
            } else if (estatus.contains("entregado")) {
                deliveredCount++;
            } else if (estatus.contains("pendiente") || estatus.contains("recibido")) {
                pendingCount++;
            }

            // Lógica de Sin Conductor (Solo nos importa si NO está entregado ni cancelado)
            boolean esActivo = !estatus.contains("entregado") && !estatus.contains("cancelado");
            if (esActivo && (e.getIdConductorAsignado() == null || e.getIdConductorAsignado().isEmpty())) {
                unassignedCount++;
            }
        }

        // Contar Unidades Disponibles
        long unidadesLibres = todasUnidades.stream()
                .filter(u -> "Disponible".equalsIgnoreCase(u.getEstatus()))
                .count();

        // 3. Actualizar Etiquetas (UI)
        lblEnTransito.setText(String.valueOf(transitCount));
        lblPorAsignar.setText(String.valueOf(unassignedCount));
        lblEntregados.setText(String.valueOf(deliveredCount));
        lblUnidades.setText(String.valueOf(unidadesLibres));

        // 4. Llenar Gráfico de Pastel
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("En Tránsito", transitCount),
                new PieChart.Data("Entregados", deliveredCount),
                new PieChart.Data("Pendientes", pendingCount)
        );
        pieChartEstatus.setData(pieData);

        // 5. Llenar Lista Rápida (Solo mostramos las guías de los que faltan de chofer)
        ObservableList<String> itemsPendientes = FXCollections.observableArrayList();
        for (Envio e : todosEnvios) {
            if (e.getIdConductorAsignado() == null || e.getIdConductorAsignado().isEmpty()) {
                // Limitamos a mostrar solo los primeros 10 para no saturar
                if (itemsPendientes.size() < 10) {
                    itemsPendientes.add(e.getNumeroGuia() + " - " + e.getCiudad());
                }
            }
        }
        if (itemsPendientes.isEmpty()) itemsPendientes.add("¡Todo al día! 🎉");
        listPendientes.setItems(itemsPendientes);
    }
}