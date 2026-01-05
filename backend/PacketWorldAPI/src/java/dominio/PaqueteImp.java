package dominio;

import dto.Respuesta;
import java.util.HashMap;
import java.util.List;
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
                int filas = conexionBD.insert("paquetes.registrarPaquete", paquete);
                
                if (filas > 0) {

                    boolean exito = EnvioImp.recalcularCostoReal(conexionBD, paquete.getIdEnvio());

                    if (exito) {
                        conexionBD.commit();
                        respuesta.setError(false);
                        respuesta.setMensaje("Paquete agregado y costo del envío actualizado correctamente.");
                    } else {
                        conexionBD.rollback();
                        respuesta.setError(true);
                        respuesta.setMensaje("Paquete guardado, pero error al recalcular costo (API).");
                    }
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
                int filas = conexionBD.update("paquetes.editarPaquete", paquete);
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
                Integer idEnvio = conexionBD.selectOne("paquetes.obtenerIdEnvioPorPaquete", idPaquete);
                int filas = conexionBD.delete("paquetes.eliminarPaquete", idPaquete);
                
                if (filas > 0 && idEnvio != null) {
                    boolean exito = EnvioImp.recalcularCostoReal(conexionBD, idEnvio);
                
                if(exito){
                        conexionBD.commit();
                        respuesta.setError(false);
                        respuesta.setMensaje("Paquete eliminado y costo actualizado.");
                    } else {
                        conexionBD.rollback();
                        respuesta.setError(true);
                        respuesta.setMensaje("Error al recalcular costo.");
                    }
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
    
    public static List<Paquete> obtenerTodos() {
        List<Paquete> listado = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                listado = conexionBD.selectList("paquetes.obtenerTodos");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return listado;
    }
}