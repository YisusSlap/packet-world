package modelo.mybatis;

import java.io.IOException;
import java.io.Reader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;


public class MybatisUtil {
    
    private static final String RESOURCE = "modelo/mybatis/mybatis-config.xml";
    private static final String ENVIROMENT = "desarrollo";
    
    public static SqlSession getSession(){
        SqlSession session = null;
        try{
            Reader reader = Resources.getResourceAsReader(RESOURCE);
            SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader,ENVIROMENT);
            session = sqlMapper.openSession();
            
        } catch(IOException e){
            e.printStackTrace();
        }
        
        return session;
        
    }
}
