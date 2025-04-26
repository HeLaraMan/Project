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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	//FOR THE GRADERS: CHANGE THE DATABASE CREDENTIALS HERE
    String sqlusername= "root";
    String sqlpassword = "AWang@SQL01!";
    
	Gson gson = new Gson(); 

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    	//json parser to java object code borrowed from my Assignment 1
        LoginResponse res = new LoginResponse();
        
        String email = request.getParameter("emaillogin");
		String password = request.getParameter("password");
		
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
        	ps = conn.prepareStatement("SELECT * FROM Users WHERE Email = ? AND Password = ?");
            ps.setString(1, email);
            ps.setString(2, password);
            rs = ps.executeQuery();

            //if this email is already in the database, this person can actually log in
            if (rs.next()) {
            	res.success = true;
                res.message = "Successfully logged in.";
                
                //store login credentials via cookie
                //code borrowed from https://www.geeksforgeeks.org/servlet-login-and-logout-example-using-cookies/
                Cookie c = new Cookie("loginemail", email);
                c.setPath("/");
                response.addCookie(c);
            } 
            //else if the email is not already in the database, this person doesn't have an account
            else {
                res.success = false;
                res.message = "Password or email is incorrect.";
            }
        } catch (SQLException e) {
            res.success = false;
            res.message = "SQL Database Exception Occurred: " + e.getMessage();
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
				res.success = false;
				res.message = "SQL Exception while in finally: " + sqle.getMessage();
			}
        }

        if(!res.success) //if unsuccessful login, tell the user
        {
			//skeleton code for JSON output borrowed from CSCI 201 HTTP-Servlets Lecture
        	PrintWriter out = response.getWriter();
        	out.println(res.message);	       	
        }
    }
}

class LoginResponse {
	public boolean success;
	public String message;
}