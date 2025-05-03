package finalproject;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.google.gson.Gson;

@WebServlet("/signup")
public class SignUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    String sqlusername= "root";
    String sqlpassword = "yourPassword";

    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // TO IMPLEMENT LATER: associate the user ID with the session
        // for now, just simulate successful signup
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // TO IMPLEMENT LATER: associate the user ID with the session
        // for now, just simulate successful signup
        SignUpInfo data = gson.fromJson(request.getReader(), SignUpInfo.class);
        Connection conn = null;
        PreparedStatement ps = null;
        int rowsUpdated = 0;

        try{
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject", sqlusername, sqlpassword);
            ps = conn.preparedstatement("INSERT INTO Favorites (UserID, SessionID) VALUES (?, ?)");
            ps.setInt(1,data.getUserId());
            ps.setInt(2,data.getSessionId());
            rowsUpdated = ps.executeUpdate();

            if(rowsUpdated <= 0){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to sign up");
            }else{
               response.setStatus(HttpServletResponse.SC_OK); 
            }

        } catch(SQLException sqe){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } finally{
            try{
                if(ps != null)
            	{
            		ps.close();
            	}
            	if(conn != null)
            	{
            		conn.close();
            	}
            }catch(SQLException swe){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database closing error");
            }
        }
        
        
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // TO IMPLEMENT LATER: Simulate successful un-signup
        String userIdStr = request.getParameter("userId");
        String sessionIdStr = request.getParameter("sessionId");

        int userId = Integer.parseInt(userIdStr); 
        int sessionId = Integer.parseInt(sessionIdStr);  

        Connection conn = null;
        PreparedStatement ps = null;
        int rowsUpdated = 0;

        try{
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject", sqlusername, sqlpassword);
            ps = conn.preparedstatement("DELETE FROM Favorites WHERE UserID = ? AND SessionID = ?");
            ps.setInt(1,data.getUserId());
            ps.setInt(2,data.getSessionId());
            rowsUpdated = ps.executeUpdate();

            if(rowsUpdated <= 0){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete sign up");
            }else{
               response.setStatus(HttpServletResponse.SC_OK); 
            }

        } catch(SQLException sqe){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } finally{
            try{
                if(ps != null)
            	{
            		ps.close();
            	}
            	if(conn != null)
            	{
            		conn.close();
            	}
            }catch(SQLException swe){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database closing error");
            }
        }


    }
}




class SignUpInfo{
    private int sessionId;
    private int userId;

    
    public int getSessionId() {
        return sessionId;
    }

    public int getUserId() {
        return userId;
    }

}

