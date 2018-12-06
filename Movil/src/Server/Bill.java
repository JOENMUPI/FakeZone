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

@WebServlet("/Bill")
public class Bill extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DB db = new DB(Props.getPropertiesFile("config", "db"));
	
	public Bill() { super(); }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject json = new JSONObject();
		try {	
			DataSet ds = db.query("config", "queries", "myBills", Integer.parseInt(request.getParameter("id")));
			ArrayList<JSONObject> ar = new ArrayList<JSONObject>();
			
	    	if(ds.hasNext()) {	
				while(ds.hasNext()) {
		    		JSONObject aux = new JSONObject();
		    		Row r = ds.next();
		    		aux.put("num", r.getField("bill_num").asFloat())
		    		.put("total", r.getField("bill_mou").asFloat())
		    		.put("id", r.getField("bill_ide").asFloat())
		    		.put("product", r.getField("product_nam").asString())
		    		.put("date", r.getField("bill_date").asString());
		    		ar.add(aux);
		    	}
				
				json.put("status", 200).put("response", ar);
			} else { json.put("status", 400).put("response", "You haven't bills"); }
    	} catch(Exception e) {
    		System.out.println(e);
    		json.put("status", 500).put("response", "internal Error, Bill...");
    	} finally { response.getWriter().print(json); }
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject reqBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		JSONObject json = new JSONObject();
		
		try {
			db.doInsert("config", "queries", "createBill", new Timestamp(Calendar.getInstance().getTimeInMillis()), reqBody.getInt("num"), reqBody.getFloat("total"), reqBody.getInt("userId"), reqBody.getInt("productId"));
			db.update("config", "queries", "updateNum", reqBody.getInt("num"), reqBody.getInt("productId"));
			json.put("status", 200).put("response", "Purchase processed");
		} catch(Exception e) {
			System.out.println(e);
			json.put("status", 500).put("response", "Internal Error, Bill...");
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