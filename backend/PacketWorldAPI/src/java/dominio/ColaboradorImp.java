package dominio;

import dto.Respuesta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.mybatis.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import pojo.Colaborador;
import utilidades.Constantes;
import utilidades.Utilidades;

public class ColaboradorImp {

    public static List<Colaborador> buscarColaboradores(String nombre, String rol, String idCodigoSucursal) {
        List<Colaborador> colaboradores = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                Colaborador filtro = new Colaborador();
                filtro.setNombre(nombre);
                filtro.setRol(rol);
                filtro.setIdCodigoSucursal(idCodigoSucursal);
                colaboradores = conexionBD.selectList("colaboradores.buscarColaboradores", filtro);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return colaboradores;
    }

    public static Colaborador obtenerColaborador(String numeroPersonal) {
        Colaborador colaborador = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                colaborador = conexionBD.selectOne("colaboradores.obtenerColaboradorPorNoPersonal", numeroPersonal);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return colaborador;
    }

    public static Respuesta registrarColaborador(Colaborador colaborador) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                if (colaborador.getContrasenia() != null && !colaborador.getContrasenia().isEmpty()) {
                    String passHash = Utilidades.hashPassword(colaborador.getContrasenia());
                    colaborador.setContrasenia(passHash);
                }
                int filasAfectadas = conexionBD.insert("colaboradores.registrarColaborador", colaborador);
                conexionBD.commit();
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Colaborador registrado exitosamente.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo registrar el colaborador.");
                }
            } catch (Exception e) {
                respuesta.setError(true);
                if (e.getMessage().contains("Duplicate entry")) {
                    respuesta.setMensaje("Error: El Número de Personal, CURP, Número de licencia, Unidad o Correo ya están registrados.");
                } else {
                    respuesta.setMensaje("Error al guardar: " + e.getMessage());
                }
            } finally {
                conexionBD.close();
            }
        } else {
            respuesta.setError(true);
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }

    public static Respuesta editarColaborador(Colaborador colaborador) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                int filasAfectadas = conexionBD.update("colaboradores.editarColaborador", colaborador);
                conexionBD.commit();
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Información del colaborador actualizada.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo actualizar.");
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
    
    public static Respuesta cambiarContrasenia(String numeroPersonal, String contraseniaActual, String contraseniaNueva) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                Colaborador colaboradorGuardado = conexionBD.selectOne("colaboradores.obtenerColaboradorPorNoPersonal", numeroPersonal);
                
                if (colaboradorGuardado != null) {
                    String hashActualEnviado = Utilidades.hashPassword(contraseniaActual);
                    
                    if (hashActualEnviado.equals(colaboradorGuardado.getContrasenia())) {
                        
                        String nuevoHash = Utilidades.hashPassword(contraseniaNueva);
                        
                        Map<String, String> params = new HashMap<>();
                        params.put("numeroPersonal", numeroPersonal);
                        params.put("nuevaContrasenia", nuevoHash);
                        
                        int filas = conexionBD.update("colaboradores.actualizarContrasenia", params);
                        conexionBD.commit();
                        
                        if(filas > 0){
                            respuesta.setError(false);
                            respuesta.setMensaje("Contraseña actualizada correctamente.");
                        } else {
                            respuesta.setError(true);
                            respuesta.setMensaje("No se pudo actualizar la contraseña.");
                        }
                    } else {
                        respuesta.setError(true);
                        respuesta.setMensaje("La contraseña actual es incorrecta.");
                    }
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("Usuario no encontrado.");
                }
            } catch (Exception e) {
                if(conexionBD!=null) conexionBD.rollback();
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

    public static Respuesta eliminarColaborador(String numeroPersonal) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                int filasAfectadas = conexionBD.update("colaboradores.eliminarColaborador", numeroPersonal);
                conexionBD.commit();
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Colaborador dado de baja correctamente.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo dar de baja.");
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
    
    public static Respuesta guardarFoto(String numeroPersonal, byte[] fotoBytes) {
        Respuesta respuesta = new Respuesta();
        respuesta.setError(true);
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                Colaborador colaborador = new Colaborador();
                colaborador.setNumeroPersonal(numeroPersonal);
                colaborador.setFotografiaBytes(fotoBytes);
                
                int filasAfectadas = conexionBD.update("colaboradores.guardarFoto", colaborador);
                conexionBD.commit();
                
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("La fotografía ha sido guardada correctamente.");
                } else {
                    respuesta.setMensaje("La fotografía no ha sido guardada.");
                }
                conexionBD.close();
            } catch (Exception e) {
                respuesta.setMensaje(e.getMessage());
            }
        } else {
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }

    public static Colaborador obtenerFoto(String numeroPersonal) {
        Colaborador colaborador = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                colaborador = conexionBD.selectOne("colaboradores.obtenerFoto", numeroPersonal);
                conexionBD.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return colaborador;
    }

}
