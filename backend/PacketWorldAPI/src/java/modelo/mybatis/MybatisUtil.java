package modelo.mybatis;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MybatisUtil {

    private static final String RESOURCE = "modelo/mybatis/mybatis-config.xml";
    private static final String ENVIROMENT = "desarrollo";

    public static SqlSession getSession() {
        SqlSession session = null;
        try {
            // Cargar variables de entorno
            Properties props = new Properties();
            props.setProperty("db.url", System.getenv("DB_URL") != null ? System.getenv("DB_URL")
                    : "jdbc:mysql://switchback.proxy.rlwy.net:42057/railway?serverTimezone=UTC");
            props.setProperty("db.username",
                    System.getenv("DB_USERNAME") != null ? System.getenv("DB_USERNAME") : "root");
            props.setProperty("db.password",
                    System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "LuWfiPxGVbdwSSPgqLAZFlpIjmpMFyVo");

            Reader reader = Resources.getResourceAsReader(RESOURCE);
            SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader, ENVIROMENT, props);
            session = sqlMapper.openSession();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return session;

    }
}
