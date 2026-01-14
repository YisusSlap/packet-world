package com.example.packetworld.util;

import com.example.packetworld.model.Envio;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Exportador {

    public static void exportarEnviosExcel(List<Envio> listaEnvios, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Envíos");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV (Excel)", "*.csv"));
        fileChooser.setInitialFileName("Reporte_Envios.csv");

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
                writer.write("\uFEFF");

                // Encabezados (9 columnas)
                writer.write("Guía,Cliente,Origen,Destino,Estatus,Conductor,Costo,Fecha Reporte,Paquetes\n");

                // Formateador de fecha para el reporte
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String fechaHoy = java.time.LocalDate.now().format(dtf);

                for (Envio e : listaEnvios) {
                    String guia = escaparCSV(e.getNumeroGuia());
                    String cliente = escaparCSV(e.getNombreCliente());
                    String origen = escaparCSV(e.getCodigoSucursalOrigen());
                    String destino = escaparCSV(e.getCiudad() + ", " + e.getEstado());
                    String estatus = escaparCSV(e.getEstatusActual());
                    String conductor = (e.getIdConductorAsignado() != null) ? escaparCSV(e.getIdConductorAsignado()) : "SIN ASIGNAR";
                    String costo = String.format("$%.2f", e.getCostoTotal());
                    String numPaquetes = (e.getListaPaquetes() != null) ? String.valueOf(e.getListaPaquetes().size()) : "0";

                    // CORRECCIÓN AQUÍ: Agregamos 'fechaHoy' en su lugar correspondiente
                    String linea = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            guia, cliente, origen, destino, estatus, conductor, costo, fechaHoy, numPaquetes);

                    writer.write(linea);
                }

                writer.close();
                Notificacion.mostrar("Exportación Exitosa", "Archivo guardado correctamente.", Notificacion.EXITO);

            } catch (Exception ex) {
                ex.printStackTrace();
                Notificacion.mostrar("Error", "No se pudo guardar el archivo.", Notificacion.ERROR);
            }
        }
    }

    // Método auxiliar para evitar que una coma en el nombre rompa el CSV
    private static String escaparCSV(String data) {
        if (data == null) return "";
        // Si el texto tiene comas, lo envolvemos en comillas
        if (data.contains(",")) {
            return "\"" + data + "\"";
        }
        return data;
    }
}