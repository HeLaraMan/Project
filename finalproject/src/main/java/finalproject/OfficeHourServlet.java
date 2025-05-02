package finalproject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.google.gson.Gson;
import java.util.Map;
import java.util.HashMap;



@WebServlet("/sessions")
public class OfficeHourServlet extends HttpServlet {

    // HashMap data structure for caching sessions
    private static final Map<Integer, Session> sessionCache = new HashMap<>();
    private static final long CACHE_TTL = 300000; // 5 minutes in milliseconds
    private static final Map<Integer, Long> cacheTimestamps = new HashMap<>();

    // Cache methods
    private Session getFromCache(int sessionId) {
        Long timestamp = cacheTimestamps.get(sessionId);
        if (timestamp != null && System.currentTimeMillis() - timestamp < CACHE_TTL) {
            return sessionCache.get(sessionId);
        }
        return null;
    }

    private void addToCache(Session session) {
        sessionCache.put(session.getId(), session);
        cacheTimestamps.put(session.getId(), System.currentTimeMillis());
    }

    private void removeFromCache(int sessionId) {
        sessionCache.remove(sessionId);
        cacheTimestamps.remove(sessionId);
    }
    private static final long serialVersionUID = 1L;

    //FOR MY TEAMMATES: CHANGE THE DATABASE CREDENTIALS HERE
    private static final String sqlusername= "root";
    private static final String sqlpassword = "AWang@SQL01!";

    @Override
    public void init() throws ServletException {
        super.init();
        //from piazza post @311
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject", sqlusername, sqlpassword);
    }

    private static final String GET_ALL_SESSIONS = 
            "SELECT s.SessionID, s.UserID, s.CourseID, s.StartTime, s.EndTime, " +
            "u.fName, c.CourseName " + 
            "FROM Sessions s " +
            "JOIN Users u ON s.UserID = u.UserID " +
            "JOIN Courses c ON s.CourseID = c.CourseID";
    
    private static final String INSERT_SESSION = 
            "INSERT INTO Sessions (UserID, CourseID, StartTime, EndTime) VALUES (?, ?, ?, ?)";
    
    private static final String DELETE_SESSION = 
            "DELETE FROM Sessions WHERE SessionID = ?";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        List<Session> sessions = new ArrayList<>();
        
        try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(GET_ALL_SESSIONS)) {
        
        while (rs.next()) {
            int sessionId = rs.getInt("SessionID");
            
            // Try getting from cache first
            Session cachedSession = getFromCache(sessionId);
            if (cachedSession != null) {
                sessions.add(cachedSession);
                continue;
            }
            
            // If not in cache, create new session from database
            Session session = new Session();
            session.setId(sessionId);
            session.setUserId(rs.getInt("UserID"));
            session.setCourseId(rs.getInt("CourseID"));
            session.setStart(rs.getString("StartTime"));
            session.setEnd(rs.getString("EndTime"));
            session.setInstructorName(rs.getString("fName"));
            session.setCourseName(rs.getString("CourseName"));
            
            sessions.add(session);
            
            // Add to cache
            addToCache(session);
        }            
            out.print(gson.toJson(sessions));
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(new ErrorResponse("Database error: " + e.getMessage())));
            e.printStackTrace();
        }
        
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        Session newSession = gson.fromJson(request.getReader(), Session.class);

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(INSERT_SESSION, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, newSession.getUserId());
            pstmt.setInt(2, newSession.getCourseId());
            pstmt.setString(3, newSession.getStart());
            pstmt.setString(4, newSession.getEnd());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print(gson.toJson(new ErrorResponse("Creating session failed, no rows affected.")));
                return;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newSession.setId(generatedKeys.getInt(1));
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().print(gson.toJson(newSession));
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().print(gson.toJson(new ErrorResponse("Creating session failed, no ID obtained.")));
                }
            }
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(gson.toJson(new ErrorResponse("Database error: " + e.getMessage())));
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo(); // e.g., /5
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            
            try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(DELETE_SESSION)) {
                
                pstmt.setInt(1, id);
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows == 0) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().print(new Gson().toJson(new ErrorResponse("Session with ID " + id + " not found.")));
                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
                
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print(new Gson().toJson(new ErrorResponse("Database error: " + e.getMessage())));
                e.printStackTrace();
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(new Gson().toJson(new ErrorResponse("Invalid session ID format.")));
        }
    }

private static class Session {
        private int id;
        private int userId;
        private int courseId;
        private String start;
        private String end;
        private String instructorName;    // From Users table (fName)
        private String courseName;       // From Courses table
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public int getCourseId() { return courseId; }
        public void setCourseId(int courseId) { this.courseId = courseId; }
        
        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
        
        public String getInstructorName() { return instructorName; }
        public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
        
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
    }

    // Error response class for sending error details to client
    private static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
