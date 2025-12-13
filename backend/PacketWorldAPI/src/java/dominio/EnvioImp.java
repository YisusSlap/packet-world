package dominio;

import dto.Respuesta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.mybatis.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import pojo.Envio;
import pojo.HistorialEstatus;
import pojo.Paquete;
import utilidades.Constantes;

public class EnvioImp {

    public static Respuesta registrarEnvio(Envio envio) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                // Generar Guía
                String guia = "PW-" + System.currentTimeMillis(); 
                envio.setNumeroGuia(guia);
                
                // Calcular Costo
                double costo = 150.0;
                if(envio.getListaPaquetes() != null){
                    costo += envio.getListaPaquetes().size() * 50;
                }
                envio.setCostoTotal(costo);

                // Insertar Envío
                int filas = conexionBD.insert("envios.registrarEnvio", envio);
                Integer idEnvioGenerado = envio.getIdEnvio();
                
                // Insertar Paquetes
                if (envio.getListaPaquetes() != null) {
                    for (Paquete p : envio.getListaPaquetes()) {
                        p.setIdEnvio(idEnvioGenerado);
                        conexionBD.insert("envios.registrarPaquete", p);
                    }
                }
                
                // Insertar Historial Inicial
                HistorialEstatus historial = new HistorialEstatus();
                historial.setIdEnvio(idEnvioGenerado);
                historial.setNumeroPersonalColaborador(envio.getNumeroPersonalUsuario()); 
                
                // CORRECCIÓN 1: Usar setIdEstatus (Integer)
                historial.setIdEstatus(1); 
                
                historial.setComentario("Envío registrado en sistema.");
                conexionBD.insert("envios.registrarHistorial", historial);

                conexionBD.commit();
                respuesta.setError(false);
                respuesta.setMensaje("Envío creado exitosamente. Guía: " + guia);
            } catch (Exception e) {
                conexionBD.rollback(); 
                respuesta.setError(true);
                respuesta.setMensaje("Error al crear envío: " + e.getMessage());
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        } else {
            respuesta.setError(true);
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }

    // ... (obtenerPorGuia y obtenerPorConductor quedan IGUAL) ...
    public static Envio obtenerPorGuia(String numeroGuia) {
        // ... tu código original ...
        Envio envio = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                envio = conexionBD.selectOne("envios.obtenerPorGuia", numeroGuia);
            } catch (Exception e) { e.printStackTrace(); } 
            finally { conexionBD.close(); }
        }
        return envio;
    }

    public static List<Envio> obtenerPorConductor(String numeroPersonal) {
        // ... tu código original ...
        List<Envio> lista = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                lista = conexionBD.selectList("envios.obtenerPorConductor", numeroPersonal);
            } catch (Exception e) { e.printStackTrace(); } 
            finally { conexionBD.close(); }
        }
        return lista;
    }


    public static Respuesta actualizarEstatus(String numeroGuia, String estatus, String comentario, String numeroPersonal) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("numeroGuia", numeroGuia);
                params.put("estatus", estatus);
                
                int filas = conexionBD.update("envios.actualizarEstatus", params);
                
                if (filas > 0) {
                    Integer idEnvio = conexionBD.selectOne("envios.obtenerIdPorGuia", numeroGuia);
                    
                    HistorialEstatus historial = new HistorialEstatus();
                    historial.setIdEnvio(idEnvio);
                    historial.setNumeroPersonalColaborador(numeroPersonal);
                    
                    // CORRECCIÓN 2: Parsear el String a Integer
                    try {
                        historial.setIdEstatus(Integer.parseInt(estatus));
                    } catch (NumberFormatException ex) {
                        historial.setIdEstatus(0); // O manejar error
                    }
                    
                    historial.setComentario(comentario);
                    
                    conexionBD.insert("envios.registrarHistorial", historial);
                    conexionBD.commit();
                    
                    respuesta.setError(false);
                    respuesta.setMensaje("Estatus actualizado correctamente.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se encontró el envío.");
                }
            } catch (Exception e) {
                conexionBD.rollback();
                respuesta.setError(true);
                respuesta.setMensaje("Error: " + e.getMessage());
            } finally {
                conexionBD.close();
            }
        } else {
            respuesta.setError(true);
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }

    // ... (asignarConductor y editarEnvio quedan IGUAL) ...
     public static Respuesta asignarConductor(String numeroGuia, String numeroPersonal) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("numeroGuia", numeroGuia);
                params.put("numeroPersonal", numeroPersonal);
                int filas = conexionBD.update("envios.asignarConductor", params);
                conexionBD.commit();
                if (filas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Conductor asignado correctamente al envío.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se encontró el envío con esa guía.");
                }
            } catch (Exception e) {
                respuesta.setError(true);
                respuesta.setMensaje("Error: " + e.getMessage());
            } finally {
                conexionBD.close();
            }
        } else {
            respuesta.setError(true);
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }

    public static Respuesta editarEnvio(Envio envio) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                int filas = conexionBD.update("envios.editarEnvio", envio);
                conexionBD.commit();
                if (filas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Datos del envío actualizados.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se encontró el envío.");
                }
            } catch (Exception e) {
                respuesta.setError(true);
                respuesta.setMensaje("Error: " + e.getMessage());
            } finally {
                conexionBD.close();
            }
        } else {
            respuesta.setError(true);
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }
}