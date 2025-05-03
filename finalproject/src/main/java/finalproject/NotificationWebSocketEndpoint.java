package finalproject;

import java.time.Instant;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * WebSocket endpoint for browser connections
 */
@ServerEndpoint("/notifications/{userId}")
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
                int notificationId = jsonMessage.get("notificationId").getAsInt();
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