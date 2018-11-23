package Server;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;


import DB.DB;
import DB.DataSet;
import DB.Row;
import Utilities.Props;

@MultipartConfig
@WebServlet("/ServletMain")
public class ServletMain extends HttpServlet {
	private DB db = new DB(Props.getPropertiesFile("config", "db"));
	private static final long serialVersionUID = 1L;
    
	public ServletMain() { super(); }
	
	 protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {			
		 setAccessControlHeaders(response);
		 response.getWriter().print(new JSONObject().put("status", "200").put("response", "logged in").toString());
	}
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
    	setAccessControlHeaders(response);
    	HttpSession session = request.getSession();
		JSONObject reqBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		switch(reqBody.getString("methodName")) {
			case "Login":
				this.loginPost(session, reqBody, response);
				break;
			case "Register":
				this.registerPost(session, reqBody, response);
				break;	
			case "Logout":
				this.logoutPost(session, response);
				break;
			case "updatePass":
				this.changePass(session, reqBody, response);
				break;
			case "Shutdown":
				this.shutdownPost(session, reqBody, response);
				break;
			case "NewProduct":
				this.newProductPost(session, reqBody, response);
				break;
			case "MyProducts":
				this.myProductsPost(session, reqBody, response);
				break;
			default:
				System.out.println("no atino...");
				break;
		}
    }
    
    private void myProductsPost(HttpSession session, JSONObject reqBody, HttpServletResponse response) throws IOException {
    	JSONObject json = new JSONObject();
    	DataSet ds = db.query("config", "queries", "myProduct", reqBody.getInt("id"));
    	if(ds.hasNext()) {
    		String s = "";
	    	while(ds.hasNext()) {
	    		JSONObject aux = new JSONObject();
	    		Row r = ds.next();
	    		aux.put("tittle", r.getField("product_nam").asString())
	    		.put("description", r.getField("product_des").asString())
	    		.put("num", r.getField("product_num").asFloat())
	    		.put("price", r.getField("product_pri").asFloat())
	    		.put("id", r.getField("product_ide").asFloat());
	    		s += aux + ",,";
	    	}
	    	
    		System.out.println("products found..");
	    	json.put("status", 200).put("response", s);
    	} else {
    		System.out.println("exploto en buscar sus productor");
    		json.put("status", 400).put("response", "Internal Error");
    	}
    	
    	response.getWriter().print(json.toString());
    }
    
    private void newProductPost(HttpSession session, JSONObject reqBody, HttpServletResponse response) throws IOException {
    	JSONObject json = new JSONObject();
    	if(db.doInsert("config", "queries", "createProduct", reqBody.getString("tittle"), reqBody.getString("description"), reqBody.getInt("num"), reqBody.getInt("price"), reqBody.getInt("userId")).hasNext()) {
	    	System.out.println("product created..");
	    	json.put("status", 200).put("response", "Product created");
    	} else {
    		System.out.println("exploto en crear un producto");
    		json.put("status", 400).put("response", "Internal Error");
    	}
    	
    	response.getWriter().print(json.toString());
    }
    
    private void shutdownPost(HttpSession session, JSONObject reqBody, HttpServletResponse response) throws IOException {
    	JSONObject json = new JSONObject();
    	db.update("config", "queries", "shutdown", reqBody.getString("user"));
    	System.out.println("eliminado cuenta: "+reqBody.getString("user"));
    	json.put("status", 200).put("response", "acount eliminated");
    	response.getWriter().print(json.toString());
    }
    
    private void logoutPost(HttpSession session, HttpServletResponse response) throws IOException {
    	JSONObject json = new JSONObject();
    	//if(session.isNew()) { 
    	//	json.put("status", "401").put("response", "You're not logged in");
		//	System.out.println("Not logged --");
		//	session.invalidate();
		//} else {
			json.put("status", "200").put("response", "You're logout");
			System.out.println("Logout --");
			session.invalidate();
		//}
		
		response.getWriter().print(json.toString());
    }    
        
	private void registerPost(HttpSession session, JSONObject reqBody, HttpServletResponse response) throws IOException {
		JSONObject json = new JSONObject();
		
		if(!db.query("config", "queries", "loginUser",reqBody.getString("email"), reqBody.getString("password")).hasNext()) {
	    	db.doInsert("config", "queries", "createUser", reqBody.getString("email"), reqBody.getString("password"));
	    	json.put("status", "200").put("response", this.getId(reqBody.getString("email"), reqBody.getString("password")));
	    	System.out.println("Register --");
	    	this.storeValue(reqBody.getString("email"), reqBody.getString("password"), session);
	    	System.out.println("------------------------------------------------------------");
			System.out.println("User-> " + reqBody.getString("email"));
		} else {
			json.put("status", "400").put("response", "email already used");
	    	System.out.println("Fail register --");
			session.invalidate();
		}
		
		response.getWriter().println(json.toString()); 
	}
    
    private void loginPost(HttpSession session, JSONObject reqBody, HttpServletResponse response) throws IOException {
    	JSONObject json = new JSONObject();
    	//if(session.isNew()) {
			if(db.query("config", "queries", "loginUser", reqBody.getString("email"), reqBody.getString("password")).hasNext()) {
				//this.storeValue(reqBody.getString("email"), reqBody.getString("password"), session);
				json.put("status", "200").put("response", this.getId(reqBody.getString("email"), reqBody.getString("password")));
				System.out.println("User-> " + reqBody.getString("email"));
			} else {
				json.put("response", "Wrong email or password").put("status", "400");
				//session.invalidate();
				System.out.println("Wrong data --");
			}
		//} else {
		//	json.put("response", "you're logged in").put("status", "400");
		//	System.out.println("Already log --");
		//}

		response.getWriter().println(json.toString()); 
	}	
    
//    private String getTime() {
//    	return Calendar.getInstance().get(Calendar.YEAR)+"-" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+" "+Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND)+".00";
//    }
    
	private void storeValue(String email, String password, HttpSession session) {
		if(email == null) {
			session.setAttribute("email", "");
			session.setAttribute("password", "");
		} else {
			session.setAttribute("email", email);
			session.setAttribute("password", password);
		}
	}
	
	private int getId(String name, String pass) {
		DataSet ds = db.query("config", "queries", "loginUser", name, pass);
		while(ds.hasNext()) {
			Row r = ds.next();
			return r.getField("user_ide").asInteger();
		}
		
		return 999999999;
	}
	
	private void changePass(HttpSession session, JSONObject reqBody, HttpServletResponse response) throws IOException {
		JSONObject json = new JSONObject();
		//if(!session.isNew()) {	
			//if(session.getAttribute("password").equals(reqBody.getString("oldPass"))){
				if(db.query("config", "queries", "loginUser", reqBody.getString("user"), reqBody.getString("oldPass")).hasNext()) {
					db.update("config", "queries", "updatePass", reqBody.getString("newPass"), reqBody.getString("user"), reqBody.getString("oldPass"));
					json.put("status", "200").put("response", "your pass is updated");
					System.out.println("------------------------------------------------------------\nUser-> " + reqBody.getString("user"));
					//session.setAttribute("password", reqBody.getString("newPass"));
				} else {
					json.put("response", "your pass is not updated, internal error").put("status", "400");
					System.out.println("internal error");
				}
			//} else {
			//	json.put("response", "your pass is not updated, oldPass error").put("status", "400");
			//	System.out.println("error oldpass --");
			//}
    	//} else {
		//	json.put("response", "your pass is not updated, you're not login").put("status", "400");
		//	System.out.println("no login --");
		//}

		response.getWriter().println(json.toString()); 
    }
	
	private void setAccessControlHeaders(HttpServletResponse resp) {
        resp.setContentType("text/html;charset=UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers\", \"Content-Type, Accept, X-Requested-With, remember-me");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
    }
	
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("llego a options...");
		setAccessControlHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    }
}