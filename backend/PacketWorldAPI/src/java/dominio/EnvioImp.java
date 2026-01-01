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
import utilidades.Utilidades;

public class EnvioImp {

    public static Respuesta registrarEnvio(Envio envio) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();

        if (conexionBD != null) {
            try {
                String cpOrigen = conexionBD.selectOne("envios.obtenerCPPorSucursal", envio.getCodigoSucursalOrigen());
                String cpDestino = conexionBD.selectOne("envios.obtenerCPPorColonia", envio.getIdColoniaDestino());

                if (cpOrigen == null || cpDestino == null) {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudieron obtener los Códigos Postales para calcular el envío.");
                    return respuesta;
                }

                Double distancia = Utilidades.obtenerDistancia(cpOrigen, cpDestino);
                if (distancia == null) {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo calcular la distancia con el servicio externo.");
                    return respuesta;
                }

                int cantidadPaquetes = (envio.getListaPaquetes() != null) ? envio.getListaPaquetes().size() : 0;
                double costoTotal = Utilidades.calcularCosto(distancia, cantidadPaquetes);

                envio.setCostoTotal(costoTotal);

                String guia = "PW-" + System.currentTimeMillis();
                envio.setNumeroGuia(guia);

                if (envio.getIdEstatus() == null) {
                    envio.setIdEstatus(1);
                }

                int filas = conexionBD.insert("envios.registrarEnvio", envio);
                Integer idEnvioGenerado = envio.getIdEnvio();

                if (envio.getListaPaquetes() != null) {
                    for (Paquete p : envio.getListaPaquetes()) {
                        p.setIdEnvio(idEnvioGenerado);
                        conexionBD.insert("paquetes.registrarPaquete", p);
                    }
                }

                HistorialEstatus historial = new HistorialEstatus();
                historial.setIdEnvio(idEnvioGenerado);
                historial.setNumeroPersonalColaborador(envio.getNumeroPersonalUsuario());
                historial.setIdEstatus(1);
                historial.setComentario("Envío registrado en sistema.");
                conexionBD.insert("envios.registrarHistorial", historial);
                conexionBD.commit();
                respuesta.setError(false);
                respuesta.setMensaje("Envío creado. Guía: " + guia + ". Costo total calculado: $" + costoTotal);
            } catch (Exception e) {
                if (conexionBD != null) {
                    conexionBD.rollback();
                }
                respuesta.setError(true);
                respuesta.setMensaje("Error al crear envío: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (conexionBD != null) {
                    conexionBD.close();
                }
            }
        } else {
            respuesta.setError(true);
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }

    public static Respuesta cotizarEnvio(Envio envio) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();

        if (conexionBD != null) {
            try {
                String cpOrigen = conexionBD.selectOne("envios.obtenerCPPorSucursal", envio.getCodigoSucursalOrigen());
                String cpDestino = conexionBD.selectOne("envios.obtenerCPPorColonia", envio.getIdColoniaDestino());

                if (cpOrigen == null || cpDestino == null) {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pueden obtener los CP para la ruta solicitada.");
                    return respuesta;
                }
                Double distancia = Utilidades.obtenerDistancia(cpOrigen, cpDestino);
                if (distancia == null) {
                    respuesta.setError(true);
                    respuesta.setMensaje("No hay cobertura o ruta disponible entre estos puntos.");
                    return respuesta;
                }

                int cantidadPaquetes = (envio.getListaPaquetes() != null) ? envio.getListaPaquetes().size() : 0;
                double costoTotal = Utilidades.calcularCosto(distancia, cantidadPaquetes);

                respuesta.setError(false);
                respuesta.setMensaje(String.valueOf(costoTotal));

            } catch (Exception e) {
                e.printStackTrace();
                respuesta.setError(true);
                respuesta.setMensaje("Error al cotizar.");
            } finally {
                conexionBD.close();
            }
        } else {
            respuesta.setError(true);
            respuesta.setMensaje("Error de conexión a BD");
        }
        return respuesta;
    }

    public static Envio obtenerPorGuia(String numeroGuia) {
        Envio envio = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                envio = conexionBD.selectOne("envios.obtenerPorGuia", numeroGuia);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return envio;
    }

    public static List<Envio> obtenerPorConductor(String numeroPersonal) {
        List<Envio> lista = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                lista = conexionBD.selectList("envios.obtenerPorConductor", numeroPersonal);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return lista;
    }

    public static Respuesta actualizarEstatus(String numeroGuia, Integer idEstatus, String comentario, String numeroPersonal) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("numeroGuia", numeroGuia);
                params.put("idEstatus", idEstatus);
                int filas = conexionBD.update("envios.actualizarEstatus", params);

                if (filas > 0) {
                    Integer idEnvio = conexionBD.selectOne("envios.obtenerIdPorGuia", numeroGuia);
                    HistorialEstatus historial = new HistorialEstatus();
                    historial.setIdEnvio(idEnvio);
                    historial.setNumeroPersonalColaborador(numeroPersonal);
                    historial.setIdEstatus(idEstatus);
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

    public static List<Envio> obtenerTodos() {
        List<Envio> listado = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                listado = conexionBD.selectList("envios.obtenerTodos");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return listado;
    }

}
