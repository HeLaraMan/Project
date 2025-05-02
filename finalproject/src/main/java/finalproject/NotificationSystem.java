package finalproject;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

/**
 * Main class responsible for managing the notification system
 */
public class NotificationSystem {
    
    private final ScheduledExecutorService scheduler;
    private final ConnectionManager connectionManager;
    private final NotificationRepository notificationRepository;
    private final Map<String, Session> activeUserSessions;
    private static final int NOTIFICATION_CHECK_INTERVAL = 60000; // 1 minute
    private static final int NOTIFICATION_LEAD_TIME = 30; // 30 minutes before session
    private final Gson gson;
    
    public NotificationSystem() {
        // Initialize a thread pool for scheduling notifications
        this.scheduler = Executors.newScheduledThreadPool(5);
        this.connectionManager = new ConnectionManager();
        this.notificationRepository = new NotificationRepository();
        this.activeUserSessions = new ConcurrentHashMap<>();
        this.gson = new Gson();
        
        // Start the notification checking thread
        startNotificationChecker();
    }
    
    /**
     * Start a background thread that periodically checks for upcoming notifications
     */
    private void startNotificationChecker() {
        Thread notificationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    checkAndScheduleNotifications();
                    Thread.sleep(NOTIFICATION_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in notification thread: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        
        notificationThread.setName("NotificationCheckerThread");
        notificationThread.setDaemon(true);
        notificationThread.start();
    }
    
    /**
     * Check database for upcoming sessions and schedule notifications
     */
    private void checkAndScheduleNotifications() {
        try {
            // Get all sessions with start times within the next 60 minutes
            List<UserAppointment> upcomingSessions = notificationRepository.getUpcomingAppointments(60);
            
            for (UserAppointment session : upcomingSessions) {
                // Calculate delay until notification time (30 minutes before start)
                ZonedDateTime now = ZonedDateTime.now();
                ZonedDateTime notificationTime = session.getTargetTime().minusMinutes(NOTIFICATION_LEAD_TIME);
                
                // Skip if notification time has already passed
                if (notificationTime.isBefore(now)) {
                    continue;
                }
                
                // Calculate delay in milliseconds
                long delayMillis = Duration.between(now, notificationTime).toMillis();
                
                // Schedule the notification
                scheduler.schedule(() -> {
                    sendNotification(session.getUserId(), 
                                   "Upcoming Office Hours Session", 
                                   "You have an office hours session in " + NOTIFICATION_LEAD_TIME + 
                                   " minutes at " + session.getTargetTime().toLocalTime());
                }, delayMillis, TimeUnit.MILLISECONDS);
                
                // Mark this notification as scheduled in the database
                notificationRepository.markNotificationScheduled(session.getId());
            }
        } catch (Exception e) {
            System.err.println("Error checking for notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send a notification to a specific user
     */
    public void sendNotification(String userId, String title, String message) {
        try {
            // Create notification object
            JsonObject notification = new JsonObject();
            notification.addProperty("type", "notification");
            notification.addProperty("title", title);
            notification.addProperty("message", message);
            notification.addProperty("timestamp", Instant.now().toString());
            
            // Send to user if they have an active session
            Session userSession = activeUserSessions.get(userId);
            if (userSession != null && userSession.isOpen()) {
                userSession.getBasicRemote().sendText(gson.toJson(notification));
            }
            
            // Store notification in database for retrieval when user connects
            notificationRepository.saveNotification(userId, title, message);
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Register a user's WebSocket session
     */
    public void registerUserSession(String userId, Session session) {
        activeUserSessions.put(userId, session);
        
        // Send any pending notifications
        List<NotificationEntity> pendingNotifications = 
            notificationRepository.getPendingNotifications(userId);
        
        for (NotificationEntity notification : pendingNotifications) {
            try {
                JsonObject notificationJson = new JsonObject();
                notificationJson.addProperty("type", "notification");
                notificationJson.addProperty("title", notification.getTitle());
                notificationJson.addProperty("message", notification.getMessage());
                notificationJson.addProperty("timestamp", notification.getTimestamp().toString());
                
                session.getBasicRemote().sendText(gson.toJson(notificationJson));
                
                // Mark as delivered
                notificationRepository.markNotificationDelivered(notification.getId());
            } catch (Exception e) {
                System.err.println("Error sending pending notification: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Remove a user's session when they disconnect
     */
    public void removeUserSession(String userId) {
        activeUserSessions.remove(userId);
    }
    
    /**
     * Shutdown the notification system
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        connectionManager.closeConnection();
    }
}

/**
 * WebSocket endpoint for browser connections
 */
@ServerEndpoint(value = "/notifications/{userId}")
public class NotificationWebSocketEndpoint {
    
    private static NotificationSystem notificationSystem = new NotificationSystem();
    private String userId;
    private Session session;
    private final Gson gson = new Gson();
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.userId = userId;
        this.session = session;
        
        // Set session properties
        session.setMaxIdleTimeout(30 * 60 * 1000); // 30 minutes
        session.setMaxTextMessageBufferSize(8192); // 8KB buffer
        
        notificationSystem.registerUserSession(userId, session);
    }
    
    @OnClose
    public void onClose() {
        if (userId != null) {
            notificationSystem.removeUserSession(userId);
        }
    }
    
    @OnError
    public void onError(Throwable error) {
        System.err.println("WebSocket error for user " + userId + ": " + error.getMessage());
        error.printStackTrace();
        
        if (userId != null) {
            notificationSystem.removeUserSession(userId);
        }
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
            String type = jsonMessage.get("type").getAsString();
            
            if ("ack".equals(type)) {
                String notificationId = jsonMessage.get("notificationId").getAsString();
                // Process acknowledgment
                notificationSystem.markNotificationDelivered(notificationId);
            } else if ("ping".equals(type)) {
                // Handle ping message to keep connection alive
                JsonObject pong = new JsonObject();
                pong.addProperty("type", "pong");
                pong.addProperty("timestamp", Instant.now().toString());
                session.getBasicRemote().sendText(gson.toJson(pong));
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Entity class representing a user appointment
 */
class UserAppointment {
    private final String id;
    private final String userId;
    private final ZonedDateTime targetTime;
    private boolean notificationScheduled;
    
    public UserAppointment(String id, String userId, ZonedDateTime targetTime, boolean notificationScheduled) {
        this.id = id;
        this.userId = userId;
        this.targetTime = targetTime;
        this.notificationScheduled = notificationScheduled;
    }
    
    public String getId() {
        return id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public ZonedDateTime getTargetTime() {
        return targetTime;
    }
    
    public boolean isNotificationScheduled() {
        return notificationScheduled;
    }
}

/**
 * Entity class representing a stored notification
 */
class NotificationEntity {
    private final String id;
    private final String userId;
    private final String title;
    private final String message;
    private final Instant timestamp;
    private boolean delivered;
    
    public NotificationEntity(String id, String userId, String title, String message, Instant timestamp, boolean delivered) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.delivered = delivered;
    }
    
    public String getId() {
        return id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public boolean isDelivered() {
        return delivered;
    }
}

/**
 * Repository class for database operations
 */
class NotificationRepository {
    private final ConnectionManager connectionManager;
    
    public NotificationRepository() {
        this.connectionManager = new ConnectionManager();
    }
    
    /**
     * Get upcoming sessions within the specified minutes
     */
    public List<UserAppointment> getUpcomingAppointments(int minutes) {
        List<UserAppointment> sessions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = connectionManager.getConnection();
            
            // Query to find sessions in the next 'minutes' window that haven't been notified yet
            String sql = "SELECT s.SessionID, s.UserID, s.StartTime, s.EndTime, s.notification_scheduled " +
                         "FROM Sessions s " +
                         "WHERE s.StartTime BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? MINUTE) " +
                         "AND (s.notification_scheduled IS NULL OR s.notification_scheduled = false)";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, minutes);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("SessionID"));
                String userId = String.valueOf(rs.getInt("UserID"));
                ZonedDateTime targetTime = rs.getTimestamp("StartTime").toInstant().atZone(ZoneId.systemDefault());
                boolean notificationScheduled = rs.getBoolean("notification_scheduled");
                
                sessions.add(new UserAppointment(id, userId, targetTime, notificationScheduled));
            }
        } catch (SQLException e) {
            System.err.println("Database error getting upcoming sessions: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return sessions;
    }
    
    /**
     * Mark a notification as scheduled in the database
     */
    public void markNotificationScheduled(String sessionId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = connectionManager.getConnection();
            
            String sql = "UPDATE Sessions SET notification_scheduled = true WHERE SessionID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error marking notification scheduled: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    /**
     * Save a notification to the database
     */
    public void saveNotification(String userId, String title, String message) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = connectionManager.getConnection();
            
            String sql = "INSERT INTO Notifications (UserID, Title, Message, Timestamp, Delivered) " +
                         "VALUES (?, ?, ?, ?, false)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, title);
            stmt.setString(3, message);
            stmt.setTimestamp(4, Timestamp.from(Instant.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error saving notification: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    /**
     * Get pending notifications for a user
     */
    public List<NotificationEntity> getPendingNotifications(String userId) {
        List<NotificationEntity> notifications = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = connectionManager.getConnection();
            
            String sql = "SELECT NotificationID, UserID, Title, Message, Timestamp, Delivered " +
                         "FROM Notifications " +
                         "WHERE UserID = ? AND Delivered = false " +
                         "ORDER BY Timestamp DESC";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("NotificationID"));
                String title = rs.getString("Title");
                String message = rs.getString("Message");
                Instant timestamp = rs.getTimestamp("Timestamp").toInstant();
                boolean delivered = rs.getBoolean("Delivered");
                
                notifications.add(new NotificationEntity(id, userId, title, message, timestamp, delivered));
            }
        } catch (SQLException e) {
            System.err.println("Database error getting pending notifications: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return notifications;
    }
    
    /**
     * Mark a notification as delivered in the database
     */
    public void markNotificationDelivered(String notificationId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = connectionManager.getConnection();
            
            String sql = "UPDATE Notifications SET Delivered = true WHERE NotificationID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error marking notification delivered: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    /**
     * Helper method to close database resources
     */
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Class to manage database connections
 */
class ConnectionManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/finalproject";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "AWang@SQL01!";
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get a database connection
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Close the connection pool
     */
    public void closeConnection() {
        // If using a connection pool, close it here
    }
}