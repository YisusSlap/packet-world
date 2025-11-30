package ws;

import com.google.gson.Gson;
import dominio.UnidadImp;
import dto.Respuesta;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import pojo.Unidad;

@Path("unidades")
public class UnidadWS {
    
    @Path("obtener")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Unidad> obtenerUnidades() {
        return UnidadImp.obtenerUnidades();
    }
   
    @Path("registrar")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Respuesta registrar(String json) {
        Gson gson = new Gson();
        try {
            Unidad unidad = gson.fromJson(json, Unidad.class);
            
            if(unidad.getVin() == null || unidad.getMarca() == null || 
               unidad.getModelo() == null || unidad.getAnio() == null || 
               unidad.getTipoUnidad() == null) {
                
                throw new BadRequestException("Faltan datos obligatorios.");
            }
            
            return UnidadImp.registrarUnidad(unidad);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @Path("editar")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Respuesta editar(String json){
        Gson gson = new Gson();
        try{
            Unidad unidad = gson.fromJson(json, Unidad.class);
            
            if(unidad.getVin() == null || unidad.getMarca() == null || 
               unidad.getModelo() == null || unidad.getAnio() == null || 
               unidad.getTipoUnidad() == null) {
                
                throw new BadRequestException("Faltan datos obligatorios.");
            }
            
            
            return UnidadImp.editarUnidad(unidad);
        } catch(Exception e){
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @Path("baja")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Respuesta darBaja(String json){
        Gson gson = new Gson();
        try {
            Unidad unidad = gson.fromJson(json, Unidad.class);
            return UnidadImp.bajaUnidad(unidad);
        } catch (Exception e){
            throw new BadRequestException(e.getMessage());
        }
    }
    
}