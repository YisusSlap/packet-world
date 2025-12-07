package ws;

import com.google.gson.Gson;
import dominio.CatalogoImp;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pojo.Colonia;
import pojo.Estado;
import pojo.EstatusEnvio;
import pojo.Municipio;

@Path("catalogos")
public class CatalogoWS {

    @Path("estados")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Estado> obtenerEstados() {
        return CatalogoImp.obtenerEstados();
    }

    @Path("municipios/{idEstado}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Municipio> obtenerMunicipios(@PathParam("idEstado") Integer idEstado) {
        if (idEstado == null || idEstado <= 0) {
            throw new BadRequestException("ID Estado requerido");
        }
        return CatalogoImp.obtenerMunicipios(idEstado);
    }

    @Path("codigosPostales/{idMunicipio}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerCPs(@PathParam("idMunicipio") Integer idMunicipio) {
        if (idMunicipio == null || idMunicipio <= 0) {
            throw new BadRequestException("ID Municipio requerido");
        }

        List<String> lista = CatalogoImp.obtenerCPs(idMunicipio);
        String json = new Gson().toJson(lista);

//        return CatalogoImp.obtenerCPs(idMunicipio);
        return Response.ok(json).build();
    }

    @Path("colonias/{codigoPostal}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Colonia> obtenerColonias(@PathParam("codigoPostal") String codigoPostal) {
        if (codigoPostal == null || codigoPostal.isEmpty()) {
            throw new BadRequestException("El código postal es obligatorio");
        }
        return CatalogoImp.obtenerColoniasPorCP(codigoPostal);
    }
    
    @Path("estatusEnvio")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<EstatusEnvio> obtenerEstatus() {
        return CatalogoImp.obtenerEstatusEnvio();
    }
}
