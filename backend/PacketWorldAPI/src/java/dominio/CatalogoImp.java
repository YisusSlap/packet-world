package dominio;

import java.util.List;
import modelo.mybatis.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import pojo.Colonia;

public class CatalogoImp {

    public static List<Colonia> obtenerColoniasPorCP(String codigoPostal) {
        List<Colonia> colonias = null;
        SqlSession conexionBD = MybatisUtil.getSession();
        
        if (conexionBD != null) {
            try {
                colonias = conexionBD.selectList("catalogos.obtenerColoniasPorCP", codigoPostal);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return colonias;
    }
}