package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/InsertFavorite")
public class InsertFavorite extends HttpServlet{

	private static final long serialVersionUID = 1L;
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//FOR THE GRADERS: CHANGE THE DATABASE CREDENTIALS HERE
	    String sqlusername= "root";
	    String sqlpassword = "AWang@SQL01!";
		
		String email = request.getParameter("email");
		String artistID = request.getParameter("artistid");
		int userID = 0;
		
		PrintWriter out = response.getWriter();
		String message = "";
		
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
        	
        	PreparedStatement put = conn.prepareStatement("INSERT INTO Favorites (UserID, ArtistID) VALUES (?, ?)");
            put.setInt(1, userID);
            put.setString(2, artistID);
            put.executeUpdate();
            
            message = "Successfully added to favorites.";

        } catch (SQLException e) {
            message = "SQL Exception during query: " + e.getMessage();
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
				message = "SQL Exception during closing: " + sqle.getMessage();
			}
        }
        
        out.println(message);
	}
	
}
