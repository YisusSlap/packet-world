package ws;

import com.google.gson.Gson;
import dominio.EnvioImp;
import dto.Respuesta;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import pojo.Envio;

@Path("envios")
public class EnvioWS {

    @Path("registrar")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Respuesta registrar(String json) {
        Gson gson = new Gson();
        try {
            Envio envio = gson.fromJson(json, Envio.class);
            if (envio.getIdCliente() == null || envio.getIdColoniaDestino() == null || envio.getCodigoSucursalOrigen() == null) {
                throw new BadRequestException("Faltan datos del envío");
            }
            if (envio.getNumeroPersonalUsuario() == null || envio.getNumeroPersonalUsuario().isEmpty()) {
                throw new BadRequestException("Se requiere el numeroPersonalUsuario");
            }
            return EnvioImp.registrarEnvio(envio);
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
            Envio envio = gson.fromJson(json, Envio.class);
            if (envio.getNumeroGuia() == null) {
                throw new BadRequestException("Número de guía requerido");
            }
            return EnvioImp.editarEnvio(envio);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Path("rastrear/{guia}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Envio rastrear(@PathParam("guia") String guia) {
        if (guia == null || guia.isEmpty()) {
            throw new BadRequestException("Guía requerida");
        }
        return EnvioImp.obtenerPorGuia(guia);
    }

    @Path("conductor/{idConductor}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Envio> misEnvios(@PathParam("idConductor") String idConductor) {
        return EnvioImp.obtenerPorConductor(idConductor);
    }

    @Path("actualizarEstatus")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Respuesta actualizarEstatus(@FormParam("numeroGuia") String guia,
            @FormParam("estatus") Integer idEstatus,
            @FormParam("comentario") String comentario,
            @FormParam("idConductor") String idConductor) {
        if (guia == null || idEstatus == null || idConductor == null) {
            throw new BadRequestException("Datos incompletos");
        }
        return EnvioImp.actualizarEstatus(guia, idEstatus, comentario, idConductor);
    }

    @Path("asignarConductor")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Respuesta asignarConductor(@FormParam("numeroGuia") String numeroGuia,
            @FormParam("numeroPersonal") String numeroPersonal) {
        if (numeroGuia == null || numeroGuia.isEmpty() || numeroPersonal == null || numeroPersonal.isEmpty()) {
            throw new BadRequestException("Hacen falta datos");
        }
        return EnvioImp.asignarConductor(numeroGuia, numeroPersonal);
    }
    
    @Path("obtenerTodos")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Envio> obtenerTodos() {
        return EnvioImp.obtenerTodos();
    }
    
    @Path("cotizar")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Respuesta cotizar(String json) {
        Gson gson = new Gson();
        try {
            // Reutilizamos el objeto Envio para pasar los datos necesarios (Origen, Destino, Paquetes)
            Envio envio = gson.fromJson(json, Envio.class);
            
            // Validaciones mínimas para poder cotizar
            if (envio.getCodigoSucursalOrigen() == null) {
                throw new BadRequestException("Falta Sucursal de Origen");
            }
            if (envio.getIdColoniaDestino() == null) {
                throw new BadRequestException("Falta Colonia de Destino");
            }
            
            // Llamamos a la lógica de negocio (que ya tienes en EnvioImp)
            return EnvioImp.cotizarEnvio(envio);
            
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
}
