package ws;

import dominio.AutenticacionImp;
import dto.RSAutenticacionColaborador;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("autenticacion")
public class AutenticacionWS {

    @POST
    @Path("movil") 
    @Produces(MediaType.APPLICATION_JSON)
    public RSAutenticacionColaborador loginMovil(@FormParam("numeroPersonal") String numeroPersonal, 
                                                 @FormParam("contrasenia") String contrasenia) {
        if (numeroPersonal == null || numeroPersonal.isEmpty() || contrasenia == null || contrasenia.isEmpty()) {
            throw new BadRequestException("Hacen falta parámetros (número de personal o contraseña");
        }
        
        
        RSAutenticacionColaborador respuesta = AutenticacionImp.autenticarColaborador(numeroPersonal, contrasenia);
        if (!respuesta.getError()) {
            String rol = respuesta.getColaborador().getRol();
            
            if (!rol.equalsIgnoreCase("Conductor")) {
                respuesta.setError(true);
                respuesta.setMensaje("Acceso denegado. Esta aplicación es solo para conductores.");
                respuesta.setColaborador(null);
            }
        }
        return respuesta;
    }
    
    @POST
    @Path("escritorio")
    public RSAutenticacionColaborador loginEscritorio(@FormParam("numeroPersonal") String numeroPersonal, 
                                                      @FormParam("contrasenia") String contrasenia) {
        
        if (numeroPersonal == null || numeroPersonal.isEmpty() || contrasenia == null || contrasenia.isEmpty()) {
            throw new BadRequestException("Hacen falta parámetros (número de personal o contraseña");
        }
        
        RSAutenticacionColaborador respuesta = AutenticacionImp.autenticarColaborador(numeroPersonal, contrasenia);

        
        if (!respuesta.getError() && respuesta.getColaborador().getRol().equals("Conductor")) {
             respuesta.setError(true);
             respuesta.setMensaje("Los conductores deben usar la App Móvil.");
             respuesta.setColaborador(null);
        }
        
        return respuesta;
    }
}