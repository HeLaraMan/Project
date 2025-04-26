package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/GetFavorites")
public class GetFavorites extends HttpServlet {
	private static final long serialVersionUID = 1L;

	//structure copied over from insert/remove favorites file
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sqlusername = "root";
		String sqlpassword = "AWang@SQL01!";

		PrintWriter out = response.getWriter();
		Gson gson = new Gson();
		List<String> favorites = new ArrayList<>();
		
		String email = request.getParameter("email");
		int userID = 0;
		
		//from piazza post @311
		try { 
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) { 
			e.printStackTrace(); 
		}
		
		//skeleton code for prepared statements borrowed from CSCI 201 JDBC lecture
        Connection conn = null;
        PreparedStatement ps = null;
		ResultSet rs = null;

		 try {
        	//establish a connection with the database and query it
        	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/assignment3", sqlusername, sqlpassword);
        	
        	//get UserID associated with this email
        	ps = conn.prepareStatement("SELECT UserID FROM Users WHERE Email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();
            
            if(rs.next())
            {
            	userID = rs.getInt("UserID");
            }
            
            //clear out memory before using again
            ps.close();
            rs.close();
        	
        	ps = conn.prepareStatement("SELECT ArtistID FROM Favorites WHERE UserID = ?");
			ps.setInt(1, userID);
			rs = ps.executeQuery();

			//add all favorites into a list
			while (rs.next()) {
				favorites.add(rs.getString("ArtistID"));
			}
	

        } catch (SQLException e) {
            e.printStackTrace();
        } finally { //code from JDBC lecture
        	try {
        		if(rs != null)
            	{
            		rs.close();
            	}
            	if(ps != null)
            	{
            		ps.close();
            	}
            	if(conn != null)
            	{
            		conn.close();
            	}
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
        }

		out.println(gson.toJson(favorites));
	}
}
