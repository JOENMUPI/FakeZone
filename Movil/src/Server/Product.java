package Server;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import DB.DB;
import DB.DataSet;
import DB.Row;
import Utilities.Props;

@WebServlet("/Product")
public class Product extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DB db = new DB(Props.getPropertiesFile("config", "db"));
       
    public Product() { super(); }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject json = new JSONObject();
		
		switch(request.getParameter("methodName")) {
		case "MyProducts":
			json = this.getDataProduct(db.query("config", "queries", "myProduct", Integer.parseInt(request.getParameter("id"))));
			break;
		case "Petition":
			try {	
	    		if(request.getParameter("petition").length() > 0) {
	    			json = this.getDataProduct(db.query("config", "queries", "getProducts", "%" + request.getParameter("petition") + "%"));
	    		} else { json = this.getDataProduct(db.query("config", "queries", "lastProducts")); }
	    	} catch(Exception e) {
	    		System.out.println(e);
	    		json.put("status", 500).put("response", "internal Error, petition...");
	    	} 
			break;
		default:
			json.put("status", 400).put("response", "no atino...");
			break;
		}
		
		response.getWriter().print(json);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject reqBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		JSONObject json = new JSONObject();
		
    	if(db.doInsert("config", "queries", "createProduct", new Timestamp(Calendar.getInstance().getTimeInMillis()), reqBody.getString("tittle"), reqBody.getString("description"), reqBody.getInt("num"), reqBody.getFloat("price"), reqBody.getInt("userId")).hasNext()) {
	    	json.put("status", 200).put("response", "Product created");
    	} else { json.put("status", 500).put("response", "Internal Error"); }
    	response.getWriter().print(json);
	}
	
	protected void doPut( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {       
		this.setAccessControlHeaders(response);
		JSONObject reqBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		JSONObject json = new JSONObject();
    	
		try {
    		db.update("config", "queries", "editProduct", reqBody.getString("tittle"), reqBody.getString("description"), reqBody.getInt("num"), reqBody.getFloat("price"), reqBody.getInt("id"));
    		json.put("status", 200).put("response", "Product edited");
    	} catch(Exception e) {
    		System.out.println(e);
	    	json.put("status", 500).put("response", "product not found");
    	}
    	
    	response.getWriter().print(json);
	}
	
	protected void doDelete( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {       
		this.setAccessControlHeaders(response);
		JSONObject json = new JSONObject();
    	
		try {
    		db.update("config", "queries", "deleteProduct", Integer.parseInt(request.getParameter("id")));
    		json.put("status", 200).put("response", "Product removed");
    	} catch(Exception e) {
    		System.out.println(e);
	    	json.put("status", 500).put("response", "Internal Error: doDelete");
    	}
    	
    	response.getWriter().print(json);
	}
	
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setAccessControlHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    }
	
	private void setAccessControlHeaders(HttpServletResponse resp) {
        resp.setContentType("text/html;charset=UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers\", \"Content-Type, Accept, X-Requested-With, remember-me");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
    }
	
	private JSONObject getDataProduct(DataSet ds) {
    	JSONObject json = new JSONObject();
    	ArrayList<JSONObject> ar = new ArrayList<JSONObject>();
		
    	if(ds.hasNext()) {	
			while(ds.hasNext()) {
	    		JSONObject aux = new JSONObject();
	    		Row r = ds.next();
	    		aux.put("tittle", r.getField("product_nam").asString())
	    		.put("numComment", db.query("config", "queries", "numComment", r.getField("product_ide").asFloat()).next().getField("count").asInteger())
	    		.put("description", r.getField("product_des").asString())
	    		.put("num", r.getField("product_num").asFloat())
	    		.put("price", r.getField("product_pri").asFloat())
	    		.put("id", r.getField("product_ide").asFloat())
	    		.put("user", r.getField("user_ide").asFloat())
	    		.put("date", r.getField("product_dat").asString());
	    		ar.add(aux);
	    	}
			
			json.put("status", 200).put("response", ar);
		} else { json.put("status", 400).put("response", "not found produts"); }
		return json;
    }
}