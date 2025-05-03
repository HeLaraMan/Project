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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.google.gson.Gson;

@WebServlet("/sessions")
public class OfficeHourServlet extends HttpServlet {
    
	// HashMap data structure for caching sessions
    private static final Map<Integer, Session> sessionCache = new HashMap<>();
    private static final Map<Integer, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_TTL = 300000; // 5 minutes in milliseconds

    // Cache methods
    private Session getFromCache(int sessionId) {
        Long timestamp = cacheTimestamps.get(sessionId);
        if (timestamp != null && System.currentTimeMillis() - timestamp < CACHE_TTL) {
            return sessionCache.get(sessionId);
        }
        return null;
    }

    private void addToCache(Session session) {
        sessionCache.put(session.getSessionId(), session);
        cacheTimestamps.put(session.getSessionId(), System.currentTimeMillis());
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
                session.setSessionId(rs.getInt("SessionID"));
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

        //System.out.println("DEBUG: UserID = " + newSession.getUserId());
        //System.out.println("DEBUG: CourseName = " + newSession.getCourseName());

        try (Connection conn = getConnection()) {

            // 1, resolve CourseID from CourseName (insert if it doesn't exist)
            int courseId = -1;

            PreparedStatement checkCourse = conn.prepareStatement("SELECT CourseID FROM Courses WHERE CourseName = ?");
            checkCourse.setString(1, newSession.getCourseName());
            ResultSet rs = checkCourse.executeQuery();

            if (rs.next()) {
                courseId = rs.getInt("CourseID");
            } else {
                PreparedStatement insertCourse = conn.prepareStatement(
                    "INSERT INTO Courses (CourseName) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                insertCourse.setString(1, newSession.getCourseName());
                insertCourse.executeUpdate();

                ResultSet generated = insertCourse.getGeneratedKeys();
                if (generated.next()) {
                    courseId = generated.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated CourseID");
                }
            }

            // 2, insert the new session using the resolved CourseID
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Sessions (UserID, CourseID, StartTime, EndTime) VALUES (?, ?, ?, ?)");

            String email = newSession.getEmail();  // add this field to Session.java
            int userId = -1;

            PreparedStatement findUser = conn.prepareStatement("SELECT UserID FROM Users WHERE Email = ?");
            findUser.setString(1, email);
            ResultSet rsUser = findUser.executeQuery();

            if (rsUser.next()) {
                userId = rsUser.getInt("UserID");
            } else {
                throw new SQLException("No user found for email: " + email);
            }

            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            ps.setString(3, newSession.getStart());
            ps.setString(4, newSession.getEnd());

            ps.executeUpdate();

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().println("Session created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Failed to create session: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int sessionId = Integer.parseInt(request.getParameter("sessionId"));
        String email = request.getParameter("email");

        try (Connection conn = getConnection()) {

            // look up user ID from the cookies email
            int userId = -1;
            PreparedStatement findUser = conn.prepareStatement("SELECT UserID FROM Users WHERE Email = ?");
            findUser.setString(1, email);
            ResultSet rsUser = findUser.executeQuery();
            if (rsUser.next()) {
                userId = rsUser.getInt("UserID");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println("User not found.");
                return;
            }

            // check if session belongs to user
            PreparedStatement verify = conn.prepareStatement("SELECT * FROM Sessions WHERE SessionID = ? AND UserID = ?");
            verify.setInt(1, sessionId);
            verify.setInt(2, userId);
            ResultSet rsVerify = verify.executeQuery();

            if (!rsVerify.next()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().println("You are not authorized to delete this session.");
                return;
            }

            // 3. Delete the session
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Sessions WHERE SessionID = ?");
            ps.setInt(1, sessionId);
            ps.executeUpdate();

            sessionCache.remove(sessionId);
            cacheTimestamps.remove(sessionId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Session deleted successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Failed to delete session: " + e.getMessage());
        }
    }

private static class Session {
        private int sessionId;
        private int userId;
        private int courseId;
        private String start;
        private String end;
        private String instructorName;    // From Users table (fName)
        private String courseName;       // From Courses table
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public int getSessionId() { return sessionId; }
        public void setSessionId(int id) { this.sessionId = id; }
        
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
