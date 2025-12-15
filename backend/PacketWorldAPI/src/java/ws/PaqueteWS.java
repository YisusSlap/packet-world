package ws;

import com.google.gson.Gson;
import dominio.PaqueteImp;
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
import javax.ws.rs.core.MediaType;
import pojo.Paquete;

@Path("paquetes")
public class PaqueteWS {

    @Path("registrar")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Respuesta registrar(String json) {
        Gson gson = new Gson();
        try {
            Paquete paquete = gson.fromJson(json, Paquete.class);
            if (paquete.getIdEnvio() == null) {
                throw new BadRequestException("Falta el idEnvio");
            }
            if (paquete.getPesoKg() == null || paquete.getDescripcion() == null) {
                 throw new BadRequestException("Datos del paquete incompletos");
            }
            return PaqueteImp.agregarPaqueteExtra(paquete);
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
            Paquete paquete = gson.fromJson(json, Paquete.class);
            if (paquete.getIdPaquete() == null) {
                throw new BadRequestException("ID Paquete requerido");
            }
            return PaqueteImp.editarPaquete(paquete);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Path("eliminar/{idPaquete}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Respuesta eliminar(@PathParam("idPaquete") Integer idPaquete) {
        if(idPaquete == null){
             throw new BadRequestException("ID requerido");
        }
        return PaqueteImp.eliminarPaquete(idPaquete);
    }
    
    @Path("obtenerTodos")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Paquete> obtenerTodos() {
        return PaqueteImp.obtenerTodos();
    }
}