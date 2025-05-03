package finalproject;

import java.io.*;
import java.sql.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.*;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
	
    private static final long serialVersionUID = 1L;
    
    //FOR MY TEAMMATES: CHANGE THE DATABASE CREDENTIALS HERE
    private static final String sqlusername= "root";
    private static final String sqlpassword = "AWang@SQL01!";
    
    Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    	//json parser to java object code borrowed from my Assignment 1
        RegisterResponse res = new RegisterResponse();
        
        String name = request.getParameter("fullname");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String accountType = request.getParameter("account_type");
		if (accountType == null || accountType.isEmpty()) {
		    accountType = "Student"; // default fallback if somehow missing
		}
        
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
                res.success = false;
                res.message = "User with this email already exists.";
            } 
            //else if the email is not already in the database, put them into the database and send back a response saying registration was successful
            else {
            	//code structure from https://stackoverflow.com/a/11804918
            	PreparedStatement put = conn.prepareStatement("INSERT INTO Users (fName, Email, pwd, AccountType) VALUES (?, ?, ?, ?)");
            	put.setString(1, name);
            	put.setString(2, email);
            	put.setString(3, password);
            	put.setString(4, accountType);
            	put.executeUpdate();
            	put.close();
                res.success = true;
                res.message = "Successfully registered.";
                res.accountType = accountType;
                
                Cookie c = new Cookie("loginemail", email);
                c.setPath("/");
                response.addCookie(c);
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

        //skeleton code for JSON output borrowed from CSCI 201 HTTP-Servlets Lecture
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(res));
        out.flush();
    }
}

//structure of the following object classes are borrowed from Assignment 1 Datum to hold the post request to register

class RegisterResponse {
	public boolean success;
    public String message;
    public String accountType;
}