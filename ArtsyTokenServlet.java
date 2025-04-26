package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/ArtsyTokenServlet")
public class ArtsyTokenServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final String client_id = "89ba7516ed71a90c9104";
	private final String client_secret = "e99a3334b12d56f409d5b7d6d8e584ba";
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//skeleton code from the Working with URLs lecture
		String params = "?client_id=" + client_id + "&client_secret=" + client_secret;
		URL url = new URL("https://api.artsy.net/api/tokens/xapp_token" + params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        //JSON reading, code from Assignment 2
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String temp = "";
		String read = br.readLine();
		while(read != null)
		{
			temp += read;
			read = br.readLine();
		}
		//parse the file, code also from Assignment 2
		Gson gson = new Gson();
		Token token = gson.fromJson(temp, Token.class);

        //response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(token.token);
	}
}

//code generated from the JSON to Java Object website https://www.jsonschema2pojo.org/
class Token {
	public String type;
	public String token;
	public String expiresAt;
	public Links links;
}

class Links {
	
}