package database;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DBConnection {
  private static final String CLOUD_SQL_CONNECTION_NAME = "ski-data-api-project:us-central1:ski-api-database";
  private static final String DB_USER = "sql_database_user";
  private static final String DB_PASS = "criminal51";
  private static final String DB_NAME = "sql_api_database";
  private static final String SOCKET_FACTORY = "com.google.cloud.sql.mysql.SocketFactory";
  private static final String PUBLIC_IP = "35.226.133.22";
  private static final String PORT = "3306";
  private static ComboPooledDataSource dataSource = new ComboPooledDataSource();

  private static Logger logger = LogManager.getLogger(DBConnection.class);

  private DBConnection(){

  }

  static{
    dataSource.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", PUBLIC_IP, PORT, DB_NAME));
    dataSource.setUser(DB_USER);
    dataSource.setPassword(DB_PASS);
    dataSource.setInitialPoolSize(10);
    dataSource.setMinPoolSize(10);
    dataSource.setMaxPoolSize(100);
    dataSource.setMaxStatementsPerConnection(500);
    try{
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e){
      logger.error(e.getMessage());
    }
  }

  public static Connection getConnection(){
   Connection connection = null;
   int count = 0;
   while(connection == null && count < 5){
     try{
       connection = dataSource.getConnection();
     } catch (SQLException e){
       count++;
       logger.error(e.getMessage());
     }
   }
   return connection;
  }
}
