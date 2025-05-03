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
public class GuestCourseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String sqlusername= "root";
    private static final String sqlpassword = "AWang@SQL01!";

    Gson gson = new Gson(); 

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,Set<Session>> courseInfoMap = new HashMap<>();

        //from piazza post @311
  		try { 
  			Class.forName("com.mysql.cj.jdbc.Driver");
  		} catch (ClassNotFoundException e) { 
  			e.printStackTrace(); 
  		}
        
        Connection conn = null;
        PreparedStatement ps = null;
		ResultSet rs = null;
        String res = "[]";
        boolean error = false;
        String message = "";
      
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject", sqlusername,sqlpassword);
            ps = conn.prepareStatement(
                 "SELECT s.SessionID, c.CourseName, s.StartTime, s.EndTime "
                 + "FROM Sessions s "
                 + "JOIN Courses c ON s.CourseID = c.CourseID;");
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
            message = e.getMessage();
        } finally{
            try{
                if(rs != null) { rs.close(); }
                if(ps != null) { ps.close(); }
                if(conn != null) { conn.close(); }  
            } catch(SQLException sqle){
                error = true;
                message = sqle.getMessage();
            }
          
        }

        String json = new Gson().toJson(courseInfoMap);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        if(error){
        	System.out.println("DEBUG: SQL EXCEPTION" + message);
            out.print(res); 
        } else{
        	System.out.println("DEBUG sessions: " + json);
            out.print(json);
        }
        out.flush();
    
    } 


	private static class Session {
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

}