package ws;

import dominio.CatalogoImp;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import pojo.Colonia;

@Path("catalogos")
public class CatalogoWS {

    @Path("colonias/{codigoPostal}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Colonia> obtenerColonias(@PathParam("codigoPostal") String codigoPostal) {
        if (codigoPostal == null || codigoPostal.isEmpty()) {
            throw new BadRequestException("El código postal es obligatorio");
        }
        if (!codigoPostal.matches("\\d+")) {
             throw new BadRequestException("El código postal debe ser numérico");
        }
        
        return CatalogoImp.obtenerColoniasPorCP(codigoPostal);
    }
}