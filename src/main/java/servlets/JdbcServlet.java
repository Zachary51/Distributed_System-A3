package servlets;

import database.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name="jdbc")
public class JdbcServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/plain");

    try{
      Connection conn = DBConnection.getConnection();
      try{
        Statement st = conn.createStatement();
        st.execute("USE sql_api_database;");
        ResultSet rs = st.executeQuery("SELECT * FROM skiRecords;");
        while(rs.next()){
          res.getWriter().println(rs.getString(1));
        }
        res.getWriter().println("--done--");
      } finally {
        conn.close();
      }
    } catch (SQLException e){
      res.getWriter().println("SQLException: " + e.getMessage());
    }
  }
}
