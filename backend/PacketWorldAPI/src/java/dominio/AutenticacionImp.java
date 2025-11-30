package dominio;
import dto.RSAutenticacionColaborador;
import java.util.HashMap;
import java.util.LinkedHashMap;
import modelo.mybatis.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import pojo.Colaborador;
import utilidades.Constantes;


public class AutenticacionImp {
    public static RSAutenticacionColaborador autenticarColaborador(String numeroPersonal, String contrasenia) {
    RSAutenticacionColaborador respuesta = new RSAutenticacionColaborador();
    respuesta.setError(true);
    
    SqlSession conexionBD = MybatisUtil.getSession();
    
    if (conexionBD != null) {
        try {
            HashMap<String, String> parametros = new LinkedHashMap<>();
            parametros.put("numeroPersonal", numeroPersonal);
            parametros.put("contrasenia", contrasenia);
            
            Colaborador colaborador = conexionBD.selectOne("autenticacion.loginColaborador", parametros);
            
            if (colaborador != null) {
                respuesta.setError(false);
                respuesta.setMensaje("Credenciales correctas del colaborador " + colaborador.getNombre());
                respuesta.setColaborador(colaborador);
            } else {
                respuesta.setMensaje("Número de personal y/o contraseña son incorrectos, por favor verificar");
            }
        } catch (Exception e) {
            respuesta.setMensaje("Error: " + e.getMessage());
        } finally {
            conexionBD.close();
        }
    } else {
        respuesta.setMensaje(Constantes.MSJ_ERROR_BD);
    }
    
    return respuesta;
}
}
