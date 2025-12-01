package dominio;

import dto.Respuesta;
import java.util.List;
import modelo.mybatis.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import pojo.Cliente;
import utilidades.Constantes;

public class ClienteImp {

    public static List<Cliente> buscarClientes(String nombre, String telefono, String correo) {
        List<Cliente> clientes = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                Cliente filtro = new Cliente();
                filtro.setNombre(nombre);
                filtro.setTelefono(telefono);
                filtro.setCorreoElectronico(correo);
                clientes = conexionBD.selectList("clientes.buscarClientes", filtro);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return clientes;
    }

    public static Respuesta registrarCliente(Cliente cliente) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                int filasAfectadas = conexionBD.insert("clientes.registrarCliente", cliente);
                conexionBD.commit();
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Cliente registrado exitosamente.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo registrar al cliente.");
                }
            } catch (Exception e) {
                respuesta.setError(true);
                if(e.getMessage().contains("Duplicate entry")){
                    respuesta.setMensaje("Error: El correo electrónico ya está registrado.");
                } else {
                    respuesta.setMensaje("Error: " + e.getMessage());
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

    public static Respuesta editarCliente(Cliente cliente) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                int filasAfectadas = conexionBD.update("clientes.editarCliente", cliente);
                conexionBD.commit();
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Cliente actualizado correctamente.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo actualizar el cliente.");
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

    public static Respuesta eliminarCliente(Integer idCliente) {
        Respuesta respuesta = new Respuesta();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                int filasAfectadas = conexionBD.update("clientes.eliminarCliente", idCliente);
                conexionBD.commit();
                if (filasAfectadas > 0) {
                    respuesta.setError(false);
                    respuesta.setMensaje("Cliente eliminado correctamente.");
                } else {
                    respuesta.setError(true);
                    respuesta.setMensaje("No se pudo eliminar al cliente.");
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