package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.SkiRecords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SkiRecordsDao {
  private static SkiRecordsDao instance = null;
  private static Logger logger = LogManager.getLogger(SkiRecordsDao.class);

  public static SkiRecordsDao getInstance(){
    if(instance == null){
      instance = new SkiRecordsDao();
    }
    return instance;
  }

  private SkiRecordsDao(){
  }

  public static void create(List<SkiRecords> skiRecords, String URL, String requestType) throws SQLException {
    String insertRecord =
        "INSERT INTO skiRecords(recordId,skierId, resortId, season, dayId, skiTime,"
            + "LiftId, vertical) "
            + "VALUES(?,?,?,?,?,?,?,?);";
    // Update stats
    String select = "SELECT COUNT(*), AVG(mean), MAX(max) FROM statistics WHERE (url=?) and (requestType=?);";
    String delete = "DELETE FROM statistics WHERE (url=?) and (requestType=?);";
    String insert = "INSERT INTO statistics(url, requestType, mean, max) VALUES(?,?,?,?);";
    long start = System.currentTimeMillis();
    Connection connection = DBConnection.getConnection();

    ResultSet results = null;
    PreparedStatement insertStmt = null;

    try {
      insertStmt = connection.prepareStatement(insertRecord);
      for (SkiRecords skiRecord : skiRecords) {
        insertStmt.setString(1, skiRecord.getRecordId());
        insertStmt.setInt(2, skiRecord.getSkierId());
        insertStmt.setInt(3, skiRecord.getResortId());
        insertStmt.setString(4, skiRecord.getSeasonId());
        insertStmt.setString(5, skiRecord.getDayId());
        insertStmt.setInt(6, skiRecord.getSkiTime());
        insertStmt.setInt(7, skiRecord.getLiftId());
        insertStmt.setInt(8, skiRecord.getVertical());
        insertStmt.addBatch();
      }
      insertStmt.executeBatch();
      long latency = System.currentTimeMillis() - start;
      // Update stats
      long count = 0L;
      long curMean = 0L;
      long curMax = 0L;
      insertStmt = connection.prepareStatement(select);
      insertStmt.setString(1, URL);
      insertStmt.setString(2, requestType);
      results = insertStmt.executeQuery();
      if (results.next()) {
        count = results.getLong(1);
        curMean = results.getLong(2);
        curMax = results.getLong(3);
      }
      if (count > 1000) {
        insertStmt = connection.prepareStatement(delete);
        insertStmt.setString(1, URL);
        insertStmt.setString(2, requestType);
        insertStmt.executeUpdate();
      }
      curMean = (curMean * count + latency) / (count + 1L);
      curMax = Math.max(curMax, latency);
      insertStmt = connection.prepareStatement(insert);
      insertStmt.setString(1, URL);
      insertStmt.setString(2, requestType);
      insertStmt.setLong(3, curMean);
      insertStmt.setLong(4, curMax);
      insertStmt.executeUpdate();

    } catch (SQLException e) {
      logger.error(e.getMessage());
    } finally {
      if (connection != null) {
        connection.close();
      }
      if (insertStmt != null) {
        insertStmt.close();
      }
    }
  }

  public static int getTotalVertical(int skierId, String dayId) throws SQLException{
    String getVerticalSum = "SELECT SUM(vertical) FROM skiRecords WHERE "
        + "skiRecords.skierId = ? AND skiRecords.dayId = ?;";
    Connection connection = DBConnection.getConnection();
    //Connection connection = HikariConnectionPool.getConnection();
    PreparedStatement selectStmt = null;
    ResultSet results = null;
    int verticalSum = 0;
    try{
      selectStmt = connection.prepareStatement(getVerticalSum);
      selectStmt.setInt(1, skierId);
      selectStmt.setString(2, dayId);
      results = selectStmt.executeQuery();
      if(results.next()){
        verticalSum = results.getInt(1);
      }
      return verticalSum;
    } catch (SQLException e){
      logger.error(e.getMessage());
    } finally {
      if(connection != null) {
        connection.close();
      }
      if(selectStmt != null) {
        selectStmt.close();
      }
      if(results != null) {
        results.close();
      }
    }
    return 0;
  }
}
