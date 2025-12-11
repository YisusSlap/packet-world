package com.example.packetworld.service;

import com.example.packetworld.model.Colaborador;
import com.example.packetworld.model.Respuesta;
import com.example.packetworld.model.RSAutenticacionColaborador;
import com.example.packetworld.model.Unidad;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiService {

    // URL BASE: Ajusta el puerto o nombre del war si cambia
    private static final String BASE_URL = "http://localhost:8084/PacketWorldAPI/api/";

    public static Colaborador usuarioLogueado;

    // --- AUTENTICACIÓN ---
    public static RSAutenticacionColaborador login(String numeroPersonal, String password) {
        try {
            HttpResponse<String> response = Unirest.post(BASE_URL + "autenticacion/escritorio")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .field("numeroPersonal", numeroPersonal)
                    .field("contrasenia", password)
                    .asString();

            return new Gson().fromJson(response.getBody(), RSAutenticacionColaborador.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- COLABORADORES ---

    // 1. BUSCAR (GET)
    public static List<Colaborador> buscarColaboradores(String nombre, String rol, String sucursal) {
        try {
            HttpResponse<String> response = Unirest.get(BASE_URL + "colaboradores/buscar")
                    .queryString("nombre", nombre != null ? nombre : "")
                    .queryString("rol", rol != null ? rol : "")
                    .queryString("idCodigoSucursal", sucursal != null ? sucursal : "")
                    .asString();

            if (response.getStatus() == 200) {
                Colaborador[] array = new Gson().fromJson(response.getBody(), Colaborador[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // 2. REGISTRAR (POST)
    public static Respuesta registrarColaborador(Colaborador colaborador) {
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(colaborador); // Convertimos a JSON string

            HttpResponse<String> response = Unirest.post(BASE_URL + "colaboradores/registrar")
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();

            return gson.fromJson(response.getBody(), Respuesta.class);
        } catch (Exception e) {
            e.printStackTrace();
            Respuesta error = new Respuesta();
            error.setError(true);
            error.setMensaje("Error de conexión: " + e.getMessage());
            return error;
        }
    }

    // 3. EDITAR (PUT)
    public static Respuesta editarColaborador(Colaborador colaborador) {
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(colaborador);

            HttpResponse<String> response = Unirest.put(BASE_URL + "colaboradores/editar")
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();

            return gson.fromJson(response.getBody(), Respuesta.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 4. ELIMINAR (DELETE)
    public static Respuesta eliminarColaborador(String numeroPersonal) {
        try {
            HttpResponse<String> response = Unirest.delete(BASE_URL + "colaboradores/eliminar/" + numeroPersonal)
                    .asString();

            return new Gson().fromJson(response.getBody(), Respuesta.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- UNIDADES ---

    // 1. OBTENER UNIDADES
    public static List<Unidad> obtenerUnidades() {
        try {
            // Nota: Tu servicio es "unidades/obtener"
            HttpResponse<String> response = Unirest.get(BASE_URL + "unidades/obtener")
                    .asString();

            if (response.getStatus() == 200) {
                Unidad[] array = new Gson().fromJson(response.getBody(), Unidad[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // 2. REGISTRAR UNIDAD
    public static Respuesta registrarUnidad(Unidad unidad) {
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(unidad);

            HttpResponse<String> response = Unirest.post(BASE_URL + "unidades/registrar")
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();

            return gson.fromJson(response.getBody(), Respuesta.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 3. EDITAR UNIDAD
    public static Respuesta editarUnidad(Unidad unidad) {
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(unidad);

            HttpResponse<String> response = Unirest.put(BASE_URL + "unidades/editar")
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();

            return gson.fromJson(response.getBody(), Respuesta.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 4. DAR DE BAJA (Tu API usa PUT a "baja" enviando el objeto Unidad)
    public static Respuesta darBajaUnidad(Unidad unidad) {
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(unidad);

            HttpResponse<String> response = Unirest.put(BASE_URL + "unidades/baja")
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();

            return gson.fromJson(response.getBody(), Respuesta.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}