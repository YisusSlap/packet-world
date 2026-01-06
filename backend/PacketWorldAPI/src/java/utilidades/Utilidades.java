package utilidades;

import com.google.gson.Gson;
import dto.RSDistanciaApi;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utilidades {

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String API_DISTANCIA_URL = "http://sublimas.com.mx:8080/calculadora/api/envios/distancia/%s,%s";

    public static Double obtenerDistancia(String cpOrigen, String cpDestino) {
        try {
            String urlFormato = String.format(API_DISTANCIA_URL, cpOrigen, cpDestino);
            URL url = new URL(urlFormato);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            conn.disconnect();

            Gson gson = new Gson();
            RSDistanciaApi respuesta = gson.fromJson(sb.toString(), RSDistanciaApi.class);

            if (respuesta != null && !respuesta.getError()) {
                return respuesta.getDistanciaKM();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double calcularCosto(double distanciaKm, int numPaquetes) {
        double costoBase = 0.0;

        if (distanciaKm > 0 && distanciaKm <= 200) {
            costoBase += (distanciaKm * 4.00);
        } else if (distanciaKm <= 500) {
            costoBase += (distanciaKm * 3.00);
        } else if (distanciaKm <= 1000) {
            costoBase += (distanciaKm * 2.00);
        } else if (distanciaKm <= 2000) {
            costoBase += (distanciaKm * 1.00);
        } else {
            costoBase += (distanciaKm * 0.50);
        }

        double costoPaquetes = 0.0;
        if (numPaquetes > 1) {
            if (numPaquetes == 2) {
                costoPaquetes = 50.00;
            } else if (numPaquetes >= 3) {

                costoPaquetes = 100.00 + ((numPaquetes - 3) * 50.00);
            }
        }

        return costoBase + costoPaquetes;
    }

}
