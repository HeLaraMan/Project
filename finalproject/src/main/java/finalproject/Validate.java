package finalproject;

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


/*skeleton code from AJAX lecture*/
@WebServlet("/Validate")
public class Validate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//FOR THE GRADERS: CHANGE THE DATABASE CREDENTIALS HERE
    String sqlusername= "root";
    String sqlpassword = "AWang@SQL01!";
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String fieldToValidate = request.getParameter("field");
		PrintWriter out = response.getWriter();
		
		if(fieldToValidate != null && fieldToValidate.equals("email")) {
			
			String email = request.getParameter("email");
			
			if(email == null || email.length() <= 0 || email.indexOf('@') == -1) {
				
				out.println("Email must be valid.");
			} else { //query the database to make sure this email doesn't already exist
				
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
		        	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject", sqlusername, sqlpassword);
		        	ps = conn.prepareStatement("SELECT * FROM Users WHERE Email = ?");
		            ps.setString(1, email);
		            rs = ps.executeQuery();

		            //if this email is already in the database, send back a response saying the email is already registered
		            if (rs.next()) {
						out.println("User with this email already exists.");
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
			}
		}
		if(fieldToValidate != null && fieldToValidate.equals("emaillogin")) {
			String emaillogin = request.getParameter("emaillogin");
			if(emaillogin == null || emaillogin.length() <= 0 || emaillogin.indexOf('@') == -1) {				
				out.println("Email must be valid");
			}
		}
		
		if(fieldToValidate != null && fieldToValidate.equals("password")) {
			String password = request.getParameter("password");
			if(password == null || password.length() <= 0) {				
				out.println("Password is required.");
			}
		}
		if(fieldToValidate != null && fieldToValidate.equals("fullname")) {
			String fullname = request.getParameter("fullname");
			if(fullname == null || fullname.length() <= 0) {			
				out.println("Full name is required.");
			}
		}
	}
}
