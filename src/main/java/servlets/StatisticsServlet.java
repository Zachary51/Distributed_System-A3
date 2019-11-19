package servlets;

import com.google.gson.Gson;
import dao.StatisticsDao;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Stats;

@WebServlet(name = "StatisticsServlet")
public class StatisticsServlet extends HttpServlet {
  StatisticsDao statisticsDao = new StatisticsDao();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      List<Stats> stats = new ArrayList<Stats>();
      try {
        stats = statisticsDao.getStats();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      String result = new Gson().toJson(stats);
      response.setStatus(HttpServletResponse.SC_OK);
      out.println(result);
      out.flush();
    }
  }

    @Override
    protected void doPost (HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
      super.doPost(req, resp);
    }
}
