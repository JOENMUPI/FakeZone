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

@WebServlet("/Comment")
public class Comment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DB db = new DB(Props.getPropertiesFile("config", "db"));

	public Comment() { super(); }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject json = new JSONObject();
		try {
			switch(request.getParameter("methodName")) {
			case "GetComments":	
		    	json = this.getCommentData(false, db.query("config", "queries", "getComments", Integer.parseInt(request.getParameter("productId"))));
				break;
			case "MyComments":
				json = this.getCommentData(true, db.query("config", "queries", "myComments", Integer.parseInt(request.getParameter("id"))));
				break;
			default:
				json.put("status", 400).put("response", "no atino... comment");
				break;	
			}
		} catch(Exception e) {
			e.printStackTrace();
    		json.put("status", 500).put("response", "internal Error, comment");
    	} finally { response.getWriter().print(json); }
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject reqBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		JSONObject json = new JSONObject();
		
		try {
			DataSet ds = db.doInsert("config", "queries", "createComment", new Timestamp(Calendar.getInstance().getTimeInMillis()), reqBody.getString("comment"), reqBody.getInt("id"));
	    	
	    	if(ds.hasNext() && db.doInsert("config", "queries", "relationedComment", ds.next().getField("coment_ide").asInteger(), reqBody.getInt("idProduct")).hasNext()) {
	    		json.put("status", 200).put("response", "Comment created");
	    	} else { json.put("status", 500).put("response", "Interal Error, relation"); }
		} catch(Exception e) {
			System.out.println(e);
			json.put("status", 500).put("response", "Interal Error, existing comment");
		}
		
		response.getWriter().print(json);
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.setAccessControlHeaders(response);
		JSONObject json = new JSONObject();
		
		try {
    		db.delete("config", "queries", "deleteComment", Integer.parseInt(request.getParameter("productId")), Integer.parseInt(request.getParameter("commentId")));
    		json.put("status", 200).put("response", "comment removed of product");
    	} catch(Exception e) {
    		System.out.println(e);
	    	json.put("status", 500).put("response", "Internal Error: doDelete");
    	} finally { response.getWriter().print(json); }
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
	
	public JSONObject getCommentData(boolean b, DataSet ds) {
		JSONObject json = new JSONObject();
		
		if(ds.hasNext()) {
			ArrayList<JSONObject> ar = new ArrayList<JSONObject>();
			while(ds.hasNext()) {
				JSONObject aux = new JSONObject();
				Row r = ds.next();
				if(b) {	
					JSONObject aux2 = new JSONObject();
    			
					aux2.put("tittle", r.getField("product_nam").asString())
						.put("numComment", db.query("config", "queries", "numComment", r.getField("product_ide").asFloat()).next().getField("count").asInteger())
						.put("description", r.getField("product_des").asString())
			    		.put("num", r.getField("product_num").asFloat())
			    		.put("price", r.getField("product_pri").asFloat())
			    		.put("id", r.getField("product_ide").asFloat())
			    		.put("user", r.getField("user_ide").asFloat())
			    		.put("date", r.getField("product_dat").asString());
	    			aux.put("product", aux2); 
				} else { aux.put("user", r.getField("user_nam").asString()); }
	    		aux.put("req", r.getField("comnet_txt_req").asString())
		    		.put("res", r.getField("coment_txt_res").asString())
		    		.put("date", r.getField("comment_dat").asString())
		    		.put("id", r.getField("coment_ide").asInteger());
	    		ar.add(aux);
			}
			json.put("status", 200).put("response", ar);
		} else { json.put("status", 400).put("response", "dont have comment"); }
		return json;
	}
}