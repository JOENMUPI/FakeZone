package Server;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import DB.DB;
import DB.DataSet;
import Utilities.Props;

@WebServlet("/User")
public class User extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DB db = new DB(Props.getPropertiesFile("config", "db"));
	
    public User() { super(); }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject json = new JSONObject();
		json.put("status", 200).put("response", "You're logout");
		response.getWriter().print(json);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject reqBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		JSONObject json = new JSONObject();
    	
		switch(reqBody.getString("methodName")) { 
		case "Login":
			json = this.login(reqBody);
			break;
		case "Register":
			json = this.register(reqBody);
			break;
		default:
			json.put("status", 400).put("response", "No atino...");
			break;
		}
    	
		response.getWriter().println(json); 
	}
	
	protected void doDelete( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {       
		this.setAccessControlHeaders(response);
		JSONObject json = new JSONObject();
    	
		try {
	    	db.update("config", "queries", "shutdown", Integer.parseInt(request.getParameter("user")));
	    	json.put("status", 200).put("response", "your Account has been deleted, for more information... we don't have");
    	} catch(Exception e) {
    		json.put("status", 500).put("response", "exploto al borrar usuario");	
    		System.out.println(e); 
    	}
    	
    	response.getWriter().print(json);
	}
	
	protected void doPut( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {       
		this.setAccessControlHeaders(response);
		JSONObject reqBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		JSONObject json = new JSONObject();
		
		try {
			db.update("config", "queries", "updatePass", reqBody.getString("newPass"), reqBody.getInt("id"));
			json.put("status", 200).put("response", "your pass is updated");
		} catch(Exception e) { 
			System.out.println(e);
			json.put("response", "your pass is not updated, user not found").put("status", 400);
		}
			
		response.getWriter().println(json); 
	}
	
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.setAccessControlHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    }
	
	private void setAccessControlHeaders(HttpServletResponse resp) {
        resp.setContentType("text/html;charset=UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers\", \"Content-Type, Accept, X-Requested-With, remember-me");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
    }
	
	@SuppressWarnings("finally")
	private JSONObject login(JSONObject reqBody) {
		JSONObject json = new JSONObject();
    	
		try { 
    		DataSet ds = db.query("config", "queries", "loginUser", reqBody.getString("email"), reqBody.getString("password"));
    		
    		if(ds.hasNext()) {
	    		json.put("status", 200).put("response", ds.next().getField("user_ide").asInteger());
			} else { json.put("response", "Wrong email or password").put("status", 400); }
    	} catch(Exception e) { 
    		System.out.println(e);
    		json.put("response", "Internal Error...").put("status", 500);
    	} finally { return json; }
	}
	
	@SuppressWarnings("finally")
	private JSONObject register(JSONObject reqBody) {
		JSONObject json = new JSONObject();
		
		try {
			if(!db.query("config", "queries", "loginUser", reqBody.getString("email"), reqBody.getString("password")).hasNext()) {
				DataSet ds = db.doInsert("config", "queries", "createUser", reqBody.getString("email"), reqBody.getString("password"));
	    		json.put("status", 200).put("response", ds.next().getField("user_ide").asInteger());
			} else { json.put("status", 400).put("response", "email already used"); }
		} catch(Exception e) { 
			System.out.println(e);
			json.put("response", "Internal Error...").put("status", 500);
		} finally { return json; }
	}
}