package finalproject;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.google.gson.Gson;

@WebServlet("/signup")
public class SignUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // TO IMPLEMENT LATER: Simulate successful un-signup
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
