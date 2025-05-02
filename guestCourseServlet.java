package finalproject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

@WebServlet("/guestCourses")
public class guestCourseServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

    private static final String sqlusername= "root";
    private static final String sqlpassword = "BarcelonaEra25!";

    Gson gson = new Gson(); 

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        Map<String,Set<Session>> courseInfoMap = new HashMap<>();

        Connection conn = null;
        PreparedStatement ps = null;
		ResultSet rs = null;
        String res = "[]";
        boolean error = false;


        try {
            conn = Driver.getConnection("jdbc:mysql://localhost:3306/finalproject", sqlusername,sqlpassword);
            ps = conn.prepareStatement(
                 "SELECT Courses.CourseName, Sessions.SessionID, Sessions.StartTime, Sessions.EndTime " +
                 "FROM Courses " +
                 "LEFT JOIN Sessions ON Courses.CourseID = Sessions.CourseID");
            rs = ps.executeQuery();

            while(rs.next()){
                String courseName = rs.getString("CourseName");
                int sessionID = rs.getInt("SessionID");
                String startTime = rs.getString("StartTime");
                String endTime = rs.getString("EndTime");

                courseInfoMap.putIfAbsent(courseName, new TreeSet<>(Comparator.comparing(Session::getStartTime)));
                courseInfoMap.get(courseName).add(new Session(sessionID, startTime, endTime));
                
            }
        } catch (SQLException e){
            error = true;
        } finally{
            try{
                if(rs != null) { rs.close(); }
                if(ps != null) { ps.close(); }
                if(conn != null) { conn.close(); }  
            } catch(SQLException sqle){
                error = true;
            }
          
        }

        String json = new Gson().toJson(courseInfoMap);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        if(error){
            out.print(res); 
        } else{
            out.print(json);
        }
        out.flush();
    
}


private static class Session{
    private int sessionID;
    private String startTime;
    private String endTime;

    public Session(int sessionID, String startTime, String endTime){
        this.sessionID = sessionID;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getSessionID(){
        return sessionID;
    }

    public String getStartTime(){
        return startTime;
    }

    public String getEndTime(){
        return endTime;
    }

}