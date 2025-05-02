package finalproject;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.google.gson.Gson;

@WebServlet("/signup")
public class SignUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Binary Search Tree for tracking signups
    private static class SignupNode {
        int userId;
        int sessionId;
        long timestamp;
        SignupNode left;
        SignupNode right;
        
        SignupNode(int userId, int sessionId) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.timestamp = System.currentTimeMillis();
            this.left = null;
            this.right = null;
        }
    }

    private SignupNode root = null;

    // Insert into BST
    private void insertSignup(int userId, int sessionId) {
        root = insertRec(root, userId, sessionId);
    }

    private SignupNode insertRec(SignupNode root, int userId, int sessionId) {
        if (root == null) {
            return new SignupNode(userId, sessionId);
        }
        
        // We'll use userId as the key for the BST
        if (userId < root.userId) {
            root.left = insertRec(root.left, userId, sessionId);
        } else if (userId > root.userId) {
            root.right = insertRec(root.right, userId, sessionId);
        } else {
            // User already exists, update session
            root.sessionId = sessionId;
            root.timestamp = System.currentTimeMillis();
        }
        
        return root;
    }

    // Search in BST
    private boolean isUserSignedUp(int userId) {
        return searchRec(root, userId) != null;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check if user is signed up using BST
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null) {
            int userId = Integer.parseInt(userIdParam);
            boolean isSignedUp = isUserSignedUp(userId);
            
            response.setContentType("application/json");
            response.getWriter().print(new Gson().toJson(
                java.util.Map.of("isSignedUp", isSignedUp)
            ));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        SignupRequest signupRequest = gson.fromJson(request.getReader(), SignupRequest.class);
        
        // Insert into BST
        insertSignup(signupRequest.getUserId(), signupRequest.getSessionId());
        
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    private static class SignupRequest {
        private int userId;
        private int sessionId;
        
        public int getUserId() { return userId; }
        public int getSessionId() { return sessionId; }
    }

    // Add this method to the class:
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

    // And update doDelete to use it:
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            int userId = Integer.parseInt(pathInfo.substring(1));
            
            // Remove from BST
            removeSignup(userId);
            
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }


    
}