package ws;

import com.google.gson.Gson;
import dominio.ColaboradorImp;
import dto.Respuesta;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import pojo.Colaborador;

@Path("colaboradores")
public class ColaboradorWS {

    @Path("buscar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Colaborador> buscar(@QueryParam("nombre") String nombre,
            @QueryParam("rol") String rol,
            @QueryParam("idCodigoSucursal") String idCodigoSucursal) {
        return ColaboradorImp.buscarColaboradores(nombre, rol, idCodigoSucursal);
    }

    @Path("obtener/{numeroPersonal}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Colaborador obtenerPorId(@PathParam("numeroPersonal") String numeroPersonal) {
        if (numeroPersonal == null || numeroPersonal.isEmpty()) {
            throw new BadRequestException("Número de personal requerido");
        }
        return ColaboradorImp.obtenerColaborador(numeroPersonal);
    }

    @Path("registrar")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Respuesta registrar(String json) {
        Gson gson = new Gson();
        try {
            Colaborador colaborador = gson.fromJson(json, Colaborador.class);
            if (colaborador.getNumeroPersonal() == null || colaborador.getRol() == null
                    || colaborador.getNombre() == null || colaborador.getIdCodigoSucursal() == null) {
                throw new BadRequestException("Faltan datos obligatorios.");
            }
            return ColaboradorImp.registrarColaborador(colaborador);
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
            Colaborador colaborador = gson.fromJson(json, Colaborador.class);
            if (colaborador.getNumeroPersonal() == null) {
                throw new BadRequestException("Número de personal requerido para editar.");
            }
            return ColaboradorImp.editarColaborador(colaborador);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Path("eliminar/{numeroPersonal}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Respuesta eliminar(@PathParam("numeroPersonal") String numeroPersonal) {
        if (numeroPersonal == null || numeroPersonal.isEmpty()) {
            throw new BadRequestException("Número de personal requerido");
        }
        return ColaboradorImp.eliminarColaborador(numeroPersonal);
    }

    @Path("guardarFoto/{numeroPersonal}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Respuesta guardarFoto(@PathParam("numeroPersonal") String numeroPersonal, byte[] foto) {
        if (numeroPersonal != null && !numeroPersonal.isEmpty() && foto != null && foto.length > 0) {
            return ColaboradorImp.guardarFoto(numeroPersonal, foto);
        }
        throw new BadRequestException("Faltan datos (ID o Foto)");
    }

    @Path("obtenerFoto/{numeroPersonal}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Colaborador obtenerFoto(@PathParam("numeroPersonal") String numeroPersonal) {
        if (numeroPersonal == null || numeroPersonal.isEmpty()) {
            throw new BadRequestException("Número de personal requerido");
        }
        return ColaboradorImp.obtenerFoto(numeroPersonal);
    }
    
    @Path("cambiarContrasenia")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Respuesta cambiarContrasenia(
            @FormParam("numeroPersonal") String numeroPersonal,
            @FormParam("contraseniaActual") String contraseniaActual,
            @FormParam("contraseniaNueva") String contraseniaNueva) {
        
        if (numeroPersonal == null || numeroPersonal.isEmpty() || 
            contraseniaActual == null || contraseniaActual.isEmpty() || 
            contraseniaNueva == null || contraseniaNueva.isEmpty()) {
            
            throw new BadRequestException("Faltan datos (número personal, contraseña actual o nueva).");
        }
        
        return ColaboradorImp.cambiarContrasenia(numeroPersonal, contraseniaActual, contraseniaNueva);
    }
}
