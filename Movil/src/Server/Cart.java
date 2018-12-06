package Server;

import java.io.IOException;
import java.util.ArrayList;
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

@WebServlet("/Cart")
public class Cart extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DB db = new DB(Props.getPropertiesFile("config", "db"));
	
    public Cart() { super(); }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject json = new JSONObject();
		
		try {	
			DataSet ds = db.query("config", "queries", "myCart", Integer.parseInt(request.getParameter("id")));	
			ArrayList<JSONObject> ar = new ArrayList<JSONObject>();
			
	    	if(ds.hasNext()) {	
				while(ds.hasNext()) {
		    		JSONObject aux = new JSONObject();
		    		Row r = ds.next();
		    		aux.put("tittle", r.getField("product_nam").asString())
		    		.put("description", r.getField("product_des").asString())
		    		.put("num", r.getField("product_num").asFloat())
		    		.put("price", r.getField("product_pri").asFloat())
		    		.put("id", r.getField("product_ide").asFloat())
		    		.put("user", r.getField("user_ide").asFloat())
		    		.put("date", r.getField("product_dat").asString());
		    		ar.add(aux);
		    	}
				
				json.put("status", 200).put("response", ar);
			} else { json.put("status", 400).put("response", "You haven't items in your cart"); }
    	} catch(Exception e) {
    		System.out.println(e);
    		json.put("status", 500).put("response", "internal Error, petition...");
    	} finally { response.getWriter().print(json); }
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject reqBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		JSONObject json = new JSONObject();
		
		try {
			db.doInsert("config", "queries", "addCart", reqBody.getInt("userId"), reqBody.getInt("productId"));
			json.put("status", 200).put("response", "added to cart");
		} catch(Exception e) {
			System.out.println(e);
			json.put("status", 500).put("response", "Internal Error, addCart...");
		} finally { response.getWriter().print(json); }
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject json = new JSONObject();
		
		try {
    		db.delete("config", "queries", "deleteCart", Integer.parseInt(request.getParameter("productId")), Integer.parseInt(request.getParameter("userId")));
    		json.put("status", 200).put("response", "Product removed of cart");
    	} catch(Exception e) {
    		System.out.println(e);
	    	json.put("status", 500).put("response", "Internal Error: doDelete");
    	} finally { response.getWriter().print(json); }
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
}