package finalproject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.google.gson.Gson;

@WebServlet("/signup")
public class SignUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Database connection info - matching other servlets
    private static final String sqlusername = "root";
    private static final String sqlpassword = "AWang@SQL01!";
    
    // SQL queries
    private static final String GET_ALL_SIGNUPS = 
            "SELECT UserID, SessionID FROM Signups";
    private static final String INSERT_SIGNUP = 
            "INSERT INTO Signups (UserID, SessionID, SignupTime) VALUES (?, ?, ?)";
    private static final String DELETE_SIGNUP = 
            "DELETE FROM Signups WHERE UserID = ? AND SessionID = ?";
    private static final String FIND_SIGNUP = 
            "SELECT COUNT(*) FROM Signups WHERE UserID = ? AND SessionID = ?";
    
    // Binary Search Tree for tracking signups
    private static class SignupNode {
        int userId;
        int sessionId;
        long timestamp;
        SignupNode left;
        SignupNode right;
        
        SignupNode(int userId, int sessionId, long timestamp) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.timestamp = timestamp;
            this.left = null;
            this.right = null;
        }
    }

    private SignupNode root = null;
    
    // Cache to quickly find all sessions a user is signed up for
    private Map<Integer, Map<Integer, Long>> userSessionMap = new HashMap<>();

    // Initialize BST from database on servlet startup
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            loadSignupsFromDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            getServletContext().log("Error initializing SignUpServlet", e);
        }
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject", sqlusername, sqlpassword);
    }
    
    // Load all existing signups from database into BST on startup
    private void loadSignupsFromDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(GET_ALL_SIGNUPS)) {
            
            while (rs.next()) {
                int userId = rs.getInt("UserID");
                int sessionId = rs.getInt("SessionID");
                long timestamp = System.currentTimeMillis(); // Use current time as fallback
                
                // Insert into BST
                insertSignup(userId, sessionId, timestamp);
                
                // Also update the lookup map
                userSessionMap.computeIfAbsent(userId, k -> new HashMap<>())
                        .put(sessionId, timestamp);
            }
            
            getServletContext().log("Successfully loaded signups from database into BST");
        }
    }

    // Insert into BST
    private void insertSignup(int userId, int sessionId, long timestamp) {
        root = insertRec(root, userId, sessionId, timestamp);
    }

    private SignupNode insertRec(SignupNode root, int userId, int sessionId, long timestamp) {
        if (root == null) {
            return new SignupNode(userId, sessionId, timestamp);
        }
        
        // We'll use userId as the key for the BST
        if (userId < root.userId) {
            root.left = insertRec(root.left, userId, sessionId, timestamp);
        } else if (userId > root.userId) {
            root.right = insertRec(root.right, userId, sessionId, timestamp);
        } else {
            // User already exists, update session
            root.sessionId = sessionId;
            root.timestamp = timestamp;
        }
        
        return root;
    }

    // Search in BST
    private SignupNode findUserSignup(int userId) {
        return searchRec(root, userId);
    }

    private SignupNode searchRec(SignupNode root, int userId) {
        if (root == null || root.userId == userId) {
            return root;
        }
        
        if (userId < root.userId) {
            return searchRec(root.left, userId);
        }
        
        return searchRec(root.right, userId);
    }
    
    // More efficient lookup using the HashMap
    private boolean isUserSignedUp(int userId, int sessionId) {
        Map<Integer, Long> userSessions = userSessionMap.get(userId);
        return userSessions != null && userSessions.containsKey(sessionId);
    }
    
    // Delete from BST
    private SignupNode deleteRec(SignupNode root, int userId) {
        if (root == null) {
            return null;
        }
        
        if (userId < root.userId) {
            root.left = deleteRec(root.left, userId);
        } else if (userId > root.userId) {
            root.right = deleteRec(root.right, userId);
        } else {
            // Node with only one child or no child
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }
            
            // Node with two children: Get the inorder successor
            root.userId = findMinValue(root.right);
            
            // Delete the inorder successor
            root.right = deleteRec(root.right, root.userId);
        }
        
        return root;
    }

    private int findMinValue(SignupNode root) {
        int minValue = root.userId;
        while (root.left != null) {
            minValue = root.left.userId;
            root = root.left;
        }
        return minValue;
    }

    private void removeSignup(int userId) {
        root = deleteRec(root, userId);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userIdParam = request.getParameter("userId");
        String sessionIdParam = request.getParameter("sessionId");
        
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        
        try {
            // If both parameters provided, check specific signup
            if (userIdParam != null && sessionIdParam != null) {
                int userId = Integer.parseInt(userIdParam);
                int sessionId = Integer.parseInt(sessionIdParam);
                
                boolean isSignedUp = isUserSignedUp(userId, sessionId);
                writer.print(new Gson().toJson(Map.of("isSignedUp", isSignedUp)));
            } 
            // If only userId provided, get all sessions for user
            else if (userIdParam != null) {
                int userId = Integer.parseInt(userIdParam);
                
                Map<Integer, Long> userSessions = userSessionMap.getOrDefault(userId, new HashMap<>());
                writer.print(new Gson().toJson(userSessions.keySet()));
            } 
            // If only sessionId provided, count signups for session
            else if (sessionIdParam != null) {
                int sessionId = Integer.parseInt(sessionIdParam);
                
                int count = 0;
                for (Map<Integer, Long> sessions : userSessionMap.values()) {
                    if (sessions.containsKey(sessionId)) {
                        count++;
                    }
                }
                
                writer.print(new Gson().toJson(Map.of("count", count)));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.print(new Gson().toJson(Map.of("error", "Missing required parameters")));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.print(new Gson().toJson(Map.of("error", "Invalid parameter format")));
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        SignupRequest signupRequest;
        
        try {
            signupRequest = gson.fromJson(request.getReader(), SignupRequest.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(gson.toJson(Map.of("error", "Invalid request format")));
            return;
        }
        
        if (signupRequest == null || signupRequest.getUserId() <= 0 || signupRequest.getSessionId() <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(gson.toJson(Map.of("error", "Missing or invalid parameters")));
            return;
        }
        
        // Check if already signed up (using lookup map for efficiency)
        if (isUserSignedUp(signupRequest.getUserId(), signupRequest.getSessionId())) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().print(gson.toJson(Map.of("error", "User already signed up for this session")));
            return;
        }
        
        long timestamp = System.currentTimeMillis();
        
        // First try to insert into database
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_SIGNUP)) {
            
            pstmt.setInt(1, signupRequest.getUserId());
            pstmt.setInt(2, signupRequest.getSessionId());
            pstmt.setLong(3, timestamp);
            
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                // Database update successful, now update in-memory structures
                
                // Update BST
                insertSignup(signupRequest.getUserId(), signupRequest.getSessionId(), timestamp);
                
                // Update lookup map
                userSessionMap.computeIfAbsent(signupRequest.getUserId(), k -> new HashMap<>())
                        .put(signupRequest.getSessionId(), timestamp);
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print(gson.toJson(Map.of("success", true)));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print(gson.toJson(Map.of("error", "Failed to insert signup")));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(gson.toJson(Map.of("error", "Database error: " + e.getMessage())));
            getServletContext().log("Database error in signup", e);
        }
    }
    
    private static class SignupRequest {
        private int userId;
        private int sessionId;
        
        public int getUserId() { return userId; }
        public int getSessionId() { return sessionId; }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(new Gson().toJson(Map.of("error", "Missing user ID or session ID")));
            return;
        }
        
        String[] params = pathInfo.substring(1).split("/");
        if (params.length != 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(new Gson().toJson(Map.of("error", "Expected format: /userId/sessionId")));
            return;
        }
        
        try {
            int userId = Integer.parseInt(params[0]);
            int sessionId = Integer.parseInt(params[1]);
            
            // First check if signup exists
            if (!isUserSignedUp(userId, sessionId)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print(new Gson().toJson(Map.of("error", "Signup not found")));
                return;
            }
            
            // Try to delete from database first
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(DELETE_SIGNUP)) {
                
                pstmt.setInt(1, userId);
                pstmt.setInt(2, sessionId);
                
                int affected = pstmt.executeUpdate();
                
                if (affected > 0) {
                    // Database deletion successful, now update in-memory structures
                    
                    // Update BST - for simplicity, we're just removing from the lookup map
                    // In a production environment, you'd want to properly maintain the BST as well
                    
                    // Update lookup map
                    Map<Integer, Long> userSessions = userSessionMap.get(userId);
                    if (userSessions != null) {
                        userSessions.remove(sessionId);
                        if (userSessions.isEmpty()) {
                            userSessionMap.remove(userId);
                            removeSignup(userId); // Remove from BST if no more sessions
                        }
                    }
                    
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().print(new Gson().toJson(Map.of("error", "Failed to delete signup")));
                }
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print(new Gson().toJson(Map.of("error", "Database error: " + e.getMessage())));
                getServletContext().log("Database error in signup deletion", e);
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(new Gson().toJson(Map.of("error", "Invalid ID format")));
        }
    }
}