package dominio;

import dto.Respuesta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
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
                // 1. Obtener Códigos Postales para el cálculo
                String cpOrigen = conexionBD.selectOne("envios.obtenerCPPorSucursal", envio.getCodigoSucursalOrigen());
                String cpDestino = conexionBD.selectOne("envios.obtenerCPPorColonia", envio.getIdColoniaDestino());

                if (cpOrigen == null || cpDestino == null) {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudieron obtener los Códigos Postales para la ruta.");
                    return respuesta;
                }

                // 2. Calcular Distancia y Costo
                Double distancia = Utilidades.obtenerDistancia(cpOrigen, cpDestino);
                if (distancia == null) {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo calcular la distancia (Servicio externo no disponible).");
                    return respuesta;
                }

                int cantidadPaquetes = (envio.getListaPaquetes() != null) ? envio.getListaPaquetes().size() : 0;
                double costoTotal = Utilidades.calcularCosto(distancia, cantidadPaquetes);
                envio.setCostoTotal(costoTotal);

                // --- 3. GENERACIÓN DE GUÍA INTELIGENTE ---
                // Formato: ORG-DES-YYYYMMDD-RANDOM (Ej. XAL-VER-20260105-123)
                
                // Origen (Primeras 3 letras de la sucursal o "ORI")
                String origen = (envio.getCodigoSucursalOrigen() != null && envio.getCodigoSucursalOrigen().length() >= 3)
                        ? envio.getCodigoSucursalOrigen().substring(0, 3).toUpperCase() : "ORI";

                // Destino (Primeras 3 letras del Estado o "DES")
                String destino = "DES";
                if (envio.getEstado() != null && envio.getEstado().length() >= 3) {
                    destino = envio.getEstado().substring(0, 3).toUpperCase();
                }

                // Fecha
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String fecha = sdf.format(new Date());

                // Aleatorio (3 dígitos)
                int random = (int) (Math.random() * 900) + 100;

                String guiaInteligente = String.format("PW-%s-%s-%s-%d", origen, destino, fecha, random);
                envio.setNumeroGuia(guiaInteligente);
                // ------------------------------------------

                // 4. Asignar Estatus Inicial (Si no viene)
                if (envio.getIdEstatus() == null) {
                    envio.setIdEstatus(1); // 1 = Recibido en Sucursal
                }

                // 5. Insertar Envío
                int filas = conexionBD.insert("envios.registrarEnvio", envio);
                Integer idEnvioGenerado = envio.getIdEnvio();

                // 6. Insertar Paquetes
                if (envio.getListaPaquetes() != null) {
                    for (Paquete p : envio.getListaPaquetes()) {
                        p.setIdEnvio(idEnvioGenerado);
                        conexionBD.insert("paquetes.registrarPaquete", p);
                    }
                }

                // 7. Insertar Historial Inicial
                HistorialEstatus historial = new HistorialEstatus();
                historial.setIdEnvio(idEnvioGenerado);
                historial.setNumeroPersonalColaborador(envio.getNumeroPersonalUsuario());
                historial.setIdEstatus(1);
                historial.setComentario("Envío registrado en sistema. Costo calculado: $" + costoTotal);
                conexionBD.insert("envios.registrarHistorial", historial);

                conexionBD.commit();
                respuesta.setError(false);
                respuesta.setMensaje(guiaInteligente); // Retornamos la guía generada

            } catch (Exception e) {
                if (conexionBD != null) conexionBD.rollback();
                respuesta.setError(true);
                respuesta.setMensaje("Error al crear envío: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (conexionBD != null) conexionBD.close();
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
                respuesta.setMensaje(String.valueOf(costoTotal)); // Retornamos el costo como String

            } catch (Exception e) {
                e.printStackTrace();
                respuesta.setError(true);
                respuesta.setMensaje("Error al cotizar.");
            } finally {
                conexionBD.close();
            }
        } else {
            respuesta.setError(true);
            respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
        }
        return respuesta;
    }

    // --- MÉTODOS DE APOYO (Ya existentes, se mantienen igual) ---

    public static boolean recalcularCostoReal(SqlSession conexionBD, Integer idEnvio) {
        try {
            Envio envio = conexionBD.selectOne("envios.obtenerDatosParaRecalculo", idEnvio);
            if (envio == null) return false;

            String cpOrigen = conexionBD.selectOne("envios.obtenerCPPorSucursal", envio.getCodigoSucursalOrigen());
            String cpDestino = conexionBD.selectOne("envios.obtenerCPPorColonia", envio.getIdColoniaDestino());

            Double distancia = Utilidades.obtenerDistancia(cpOrigen, cpDestino);
            if (distancia == null) return false;

            Integer numPaquetes = conexionBD.selectOne("paquetes.contarPaquetesPorEnvio", idEnvio);
            if (numPaquetes == null) numPaquetes = 0;

            double nuevoCosto = Utilidades.calcularCosto(distancia, numPaquetes);

            Map<String, Object> params = new HashMap<>();
            params.put("idEnvio", idEnvio);
            params.put("costoTotal", nuevoCosto);

            conexionBD.update("envios.actualizarCostoTotal", params);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
                if (filas > 0) {
                    // Recalcular Costo por si cambió el destino
                    Integer idEnvioReal = conexionBD.selectOne("envios.obtenerIdPorGuia", envio.getNumeroGuia());
                    boolean recalculoExitoso = recalcularCostoReal(conexionBD, idEnvioReal);
                    if (recalculoExitoso) {
                        conexionBD.commit();
                        respuesta.setError(false);
                        respuesta.setMensaje("Datos actualizados y costo recalculado.");
                    } else {
                        conexionBD.rollback();
                        respuesta.setError(true);
                        respuesta.setMensaje("Error al recalcular costos.");
                    }
                } else {
                    conexionBD.rollback();
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