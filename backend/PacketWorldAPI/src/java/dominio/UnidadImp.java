package dominio;

import dto.Respuesta;
import java.util.List;
import modelo.mybatis.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import pojo.Unidad;
import utilidades.Constantes;


public class UnidadImp {
    public static List<Unidad> obtenerUnidades(){
        List<Unidad> unidades = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                unidades = conexionBD.selectList("unidades.obtenerUnidades",null);
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                conexionBD.close();
            }
        }
        return unidades;
    }
    
    public static Respuesta registrarUnidad(Unidad unidad){
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                //Año + primero 4 caracteres del VIN
                if (unidad.getVin() != null && unidad.getVin().length() >= 4 && unidad.getAnio() != null) {
                    String cuatroDigitos = unidad.getVin().substring(0,4);
                    String niiGenerado = unidad.getAnio().toString() + cuatroDigitos;
                    unidad.setNii(niiGenerado);
                }
                
                int filasAfectadas = conexionBD.insert("unidades.registrarUnidad",unidad);
                conexionBD.commit();
                
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Unidad registrada exitosamente");
                }else{
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo registrar la unidad.");
                }
            } catch (Exception e) {
                respuesta.setError(true);
                respuesta.setMensaje("Error: " + e.getMessage());
            }finally{
                conexionBD.close();
            }
        }else{
            respuesta.setError(true);
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }
    
    public static Respuesta editarUnidad(Unidad unidad) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                int filasAfectadas = conexionBD.update("unidades.editarUnidad", unidad);
                conexionBD.commit();
                
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Unidad actualizada correctamente.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo actualizar la unidad (Verifica el VIN).");
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
    
    public static Respuesta bajaUnidad(Unidad unidad) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                int filasAfectadas = conexionBD.update("unidades.bajaUnidad", unidad);
                conexionBD.commit();
                
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Unidad dada de baja correctamente.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo dar de baja la unidad.");
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
