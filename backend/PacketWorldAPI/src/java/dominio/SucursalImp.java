package dominio;

import dto.Respuesta;
import java.util.List;
import modelo.mybatis.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import pojo.Sucursal;
import utilidades.Constantes;


public class SucursalImp {
    public static List<Sucursal> obtenerSucursales(){
        List<Sucursal> sucursales = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if(conexionBD != null){
            try{
                sucursales = conexionBD.selectList("sucursales.obtenerSucursales");
                conexionBD.close();
            } catch(Exception e){
                e.printStackTrace();
            } finally{
                conexionBD.close();
            }
        }
        return sucursales;
    }
    
    public static Respuesta registrarSucursal(Sucursal sucursal){
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if(conexionBD != null){
            try{                
                int filasAfectadas = conexionBD.insert("sucursales.registrarSucursal",sucursal);
                conexionBD.commit();
                if(filasAfectadas > 0){
                    respuesta.setError(false);
                    respuesta.setMensaje("Sucursal registrada exitosamente.");
                }else{
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo registrar la sucursal");
                }
            }catch(Exception e){
                respuesta.setError(true);
                respuesta.setMensaje("Error: "+ e.getMessage());
            }finally{
                conexionBD.close();

            }
        }else{
            respuesta.setError(true);
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }
    
    public static Respuesta editarSucursal(Sucursal sucursal){
        Respuesta respuesta = new Respuesta();
        respuesta.setError(true);        
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if(conexionBD != null){
            try{
                int filasAfectadas = conexionBD.update("sucursales.editarSucursal",sucursal);
                conexionBD.commit();
                if(filasAfectadas > 0){
                    respuesta.setError(false);
                    respuesta.setMensaje("Sucursal actualizada correctamente.");
                }else{
                    respuesta.setMensaje("No se pudo actualizacion la información");
                }
            }catch(Exception e){
                respuesta.setMensaje("Error: "+ e.getMessage());
            }finally{
                conexionBD.close();
            }
        }else{
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }
    
    public static Respuesta eliminarSucursal(String codigoSucursal){
        Respuesta respuesta = new Respuesta();
        respuesta.setError(true);
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if(conexionBD != null){
            try{
                int filasAfectadas = conexionBD.update("sucursales.eliminarSucursal",codigoSucursal);
                conexionBD.commit();
                if(filasAfectadas > 0){
                    respuesta.setError(false);
                    respuesta.setMensaje("Sucursal dada de baja correctamente.");
                }else{
                    respuesta.setMensaje("No se pudo dar de baja la sucursal");
                }
            }catch(Exception e){
                respuesta.setMensaje("Error: "+ e.getMessage());
            }finally{
                conexionBD.close();
            }
        }else{
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }
    
}
