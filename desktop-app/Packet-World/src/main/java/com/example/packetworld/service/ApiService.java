package com.example.packetworld.service;

import com.example.packetworld.model.*;
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

    // --- SUCURSALES ---

    // 1. GET (Obtener todas)
    public static List<Sucursal> obtenerSucursales() {
        try {
            HttpResponse<String> response = Unirest.get(BASE_URL + "sucursales/obtener")
                    .asString();
            if (response.getStatus() == 200) {
                Sucursal[] array = new Gson().fromJson(response.getBody(), Sucursal[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    // 2. POST (Registrar)
    public static Respuesta registrarSucursal(Sucursal s) {
        try {
            String json = new Gson().toJson(s);
            HttpResponse<String> response = Unirest.post(BASE_URL + "sucursales/registrar")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asString();
            return new Gson().fromJson(response.getBody(), Respuesta.class);
        } catch (Exception e) { return null; }
    }

    // 3. PUT (Editar)
    public static Respuesta editarSucursal(Sucursal s) {
        try {
            String json = new Gson().toJson(s);
            HttpResponse<String> response = Unirest.put(BASE_URL + "sucursales/editar")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asString();
            return new Gson().fromJson(response.getBody(), Respuesta.class);
        } catch (Exception e) { return null; }
    }

    // 4. DELETE (Eliminar por código)
    public static Respuesta eliminarSucursal(String codigo) {
        try {
            HttpResponse<String> response = Unirest.delete(BASE_URL + "sucursales/eliminar/" + codigo)
                    .asString();
            return new Gson().fromJson(response.getBody(), Respuesta.class);
        } catch (Exception e) { return null; }
    }

    // --- CATÁLOGO ---

    public static List<Estado> obtenerEstados() {
        try {
            HttpResponse<String> response = Unirest.get(BASE_URL + "catalogos/estados").asString();
            if (response.getStatus() == 200) {
                Estado[] array = new Gson().fromJson(response.getBody(), Estado[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public static List<Municipio> obtenerMunicipios(Integer idEstado) {
        try {
            HttpResponse<String> response = Unirest.get(BASE_URL + "catalogos/municipios/" + idEstado).asString();
            if (response.getStatus() == 200) {
                Municipio[] array = new Gson().fromJson(response.getBody(), Municipio[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public static List<String> obtenerCPs(Integer idMunicipio) {
        try {
            HttpResponse<String> response = Unirest.get(BASE_URL + "catalogos/codigosPostales/" + idMunicipio).asString();
            if (response.getStatus() == 200) {
                // Tu API devuelve una lista de Strings directo
                String[] array = new Gson().fromJson(response.getBody(), String[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public static List<Colonia> obtenerColonias(String cp) {
        try {
            HttpResponse<String> response = Unirest.get(BASE_URL + "catalogos/colonias/" + cp).asString();
            if (response.getStatus() == 200) {
                Colonia[] array = new Gson().fromJson(response.getBody(), Colonia[].class);
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

}