package dominio;

import java.util.ArrayList;
import java.util.List;
import modelo.mybatis.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import pojo.Colonia;
import pojo.Estado;
import pojo.EstatusEnvio;
import pojo.Municipio;

public class CatalogoImp {

    public static List<Estado> obtenerEstados() {
        List<Estado> lista = new ArrayList<>();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                lista = conexionBD.selectList("catalogos.obtenerEstados");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return lista;
    }

    public static List<Municipio> obtenerMunicipios(Integer idEstado) {
        List<Municipio> lista = new ArrayList<>();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                lista = conexionBD.selectList("catalogos.obtenerMunicipiosPorEstado", idEstado);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return lista;
    }
    
    public static List<String> obtenerCPs(Integer idMunicipio) {
        List<String> lista = new ArrayList<>();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                lista = conexionBD.selectList("catalogos.obtenerCPsPorMunicipio", idMunicipio);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return lista;
    }

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
    
   public static List<EstatusEnvio> obtenerEstatusEnvio(){
       List<EstatusEnvio> listaEstatus = new ArrayList<>();
        SqlSession conexionBD = MybatisUtil.getSession();
        if (conexionBD != null) {
            try {
                listaEstatus = conexionBD.selectList("catalogos.obtenerEstatusEnvio");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conexionBD.close();
            }
        }
        return listaEstatus;
   }
}