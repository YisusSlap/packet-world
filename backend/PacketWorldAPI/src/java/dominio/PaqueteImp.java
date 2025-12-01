package dominio;

import dto.Respuesta;
import java.util.HashMap;
import java.util.Map;
import modelo.mybatis.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import pojo.Paquete;
import utilidades.Constantes;

public class PaqueteImp {

    public static Respuesta agregarPaqueteExtra(Paquete paquete) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                int filas = conexionBD.insert("envios.registrarPaquete", paquete);
                
                if (filas > 0) {
                    // Recalcular Costo (+50 dummy)
                    Map<String, Object> params = new HashMap<>();
                    params.put("idEnvio", paquete.getIdEnvio());
                    params.put("monto", 50.0);
                    conexionBD.update("envios.sumarCostoDummy", params);
                    
                    conexionBD.commit();
                    respuesta.setError(false);
                    respuesta.setMensaje("Paquete agregado y costo actualizado.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo guardar el paquete.");
                }
            } catch (Exception e) {
                conexionBD.rollback();
                respuesta.setError(true);
                respuesta.setMensaje("Error: " + e.getMessage());
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

    public static Respuesta editarPaquete(Paquete paquete) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                int filas = conexionBD.update("envios.editarPaquete", paquete);
                conexionBD.commit();
                if (filas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Paquete actualizado.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se encontró el paquete.");
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

    public static Respuesta eliminarPaquete(Integer idPaquete) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                Integer idEnvio = conexionBD.selectOne("envios.obtenerIdEnvioPorPaquete", idPaquete);
                int filas = conexionBD.delete("envios.eliminarPaquete", idPaquete);
                
                if (filas > 0 && idEnvio != null) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("idEnvio", idEnvio);
                    params.put("monto", -50.0);
                    conexionBD.update("envios.sumarCostoDummy", params);
                    
                    conexionBD.commit();
                    respuesta.setError(false);
                    respuesta.setMensaje("Paquete eliminado y costo actualizado.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo eliminar el paquete.");
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
}