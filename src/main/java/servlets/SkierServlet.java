package servlets;

import com.google.gson.Gson;
import com.google.gson.annotations.Since;
import dao.SkiRecordsDao;
import dao.StatisticsDao;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.SkiRecords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
  private static Logger logger = LogManager.getLogger(SkiRecordsDao.class);
  Pipeline pipeline = new Pipeline();
  StatisticsDao statisticsDao = new StatisticsDao();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();

    Validate(response, out, urlPath);
    String[] urlParts = urlPath.split("/");

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      String jsonString = new Gson().toJson("invalid url");
      out.write(jsonString);
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      if (urlParts.length == 8) {

        try {
          long start = System.currentTimeMillis();
          int totalVertical = SkiRecordsDao.getTotalVertical(Integer.parseInt(urlParts[7]), urlParts[5]);
          if(totalVertical != 0){
            long latency = ((System.currentTimeMillis() - start) / 1000);
            statisticsDao.UpdateStatsTable("/{skierID}/vertical", "GET", latency, latency);
          }
          out.write(new Gson().toJson(totalVertical));
        } catch (Exception e) {
          logger.error(e.getMessage());
        }
      }
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();

    Validate(response, out, urlPath);
    String[] urlParts = urlPath.split("/");

    if (isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_OK);
      JSONObject newSkiLift = RpcHandler.readJSONObject(request);

      int liftID = ((Long)newSkiLift.get("liftID")).intValue();
      String recordId = getRecordId(UUID.randomUUID().toString());

      try {
        SkiRecords newRecord = new SkiRecords(recordId, Integer.parseInt(urlParts[7]),
            Integer.parseInt(urlParts[1]), urlParts[3], urlParts[5],
            ((Long)newSkiLift.get("time")).intValue(),
            liftID,liftID*10);
        pipeline.enqueue(newRecord);
      } catch (Exception e) {
        logger.error(e.getMessage());
      }

    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      String jsonString = new Gson().toJson("invalid url");
      out.write(jsonString);
    }
  }

  private String getRecordId(String uuidString) {
    return uuidString.substring(0, 8) + uuidString.substring(9, 13) +
        uuidString.substring(14, 18) + uuidString.substring(19, 23) +
        uuidString.substring(24);
  }

  private boolean isUrlValid(String[] urlPath) {
    if (urlPath.length == 8 && isInteger(urlPath[1]) && urlPath[2].equals("seasons")
        && isInteger(urlPath[3]) && urlPath[4].equals("days")
        && isInteger(urlPath[5]) && urlPath[6].equals("skiers")
        && isInteger(urlPath[7])) {
      return true;
    }
    return urlPath.length == 3 && isInteger(urlPath[1])
        && urlPath[2].equals("vertical");
  }

  private boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
    } catch(NumberFormatException e) {
      return false;
    } catch(NullPointerException e) {
      return false;
    }
    return true;
  }

  private void Validate(HttpServletResponse response, PrintWriter out, String urlPath) {
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      String jsonString = new Gson().toJson("missing paramterers");
      out.write(jsonString);
      return;
    }
  }
}
