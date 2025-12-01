package ws;

import com.google.gson.Gson;
import dominio.ClienteImp;
import dto.Respuesta;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import pojo.Cliente;

@Path("clientes")
public class ClienteWS {

    @Path("buscar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Cliente> buscar(@QueryParam("nombre") String nombre,
            @QueryParam("telefono") String telefono,
            @QueryParam("correo") String correo) {
        return ClienteImp.buscarClientes(nombre, telefono, correo);
    }

    @Path("registrar")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Respuesta registrar(String json) {
        Gson gson = new Gson();
        try {
            Cliente cliente = gson.fromJson(json, Cliente.class);
            if (cliente.getNombre() == null || cliente.getTelefono() == null
                    || cliente.getIdColonia() == null || cliente.getCorreoElectronico() == null) {
                throw new BadRequestException("Faltán datos obligatorios (nombre, telefono, correo, colonia)");
            }
            return ClienteImp.registrarCliente(cliente);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Path("editar")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Respuesta editar(String json) {
        Gson gson = new Gson();
        try {
            Cliente cliente = gson.fromJson(json, Cliente.class);
            if (cliente.getIdCliente() == null) {
                throw new BadRequestException("ID de cliente requerido para editar");
            }
            return ClienteImp.editarCliente(cliente);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Path("eliminar/{idCliente}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Respuesta eliminar(@PathParam("idCliente") Integer idCliente) {
        if (idCliente == null || idCliente <= 0) {
            throw new BadRequestException("ID de cliente inválido");
        }
        return ClienteImp.eliminarCliente(idCliente);
    }
}
