package Server;

import javax.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

public class EmbeddedTomcat {
	static User user = new User();
	static Comment comment = new Comment();
	static Product product = new Product();
	static Cart cart = new Cart();
	static Bill bill = new Bill();
	
	
	public static void main(String[] args) throws LifecycleException, ServletException {
		Tomcat tomcat = new Tomcat();
		Context ctxt = null;
		Connector con = new Connector();
		con.setPort(8080);
		con.setMaxPostSize(10);
		tomcat.setConnector(con);
		ctxt = tomcat.addWebapp("/", System.getProperty("user.dir") + "\\WebContent");
		Tomcat.addServlet(ctxt, "User", user);
		ctxt.addServletMappingDecoded("/User", "User");
		Tomcat.addServlet(ctxt, "Product", product);
		ctxt.addServletMappingDecoded("/Product", "Product");
		Tomcat.addServlet(ctxt, "Comment", comment);
		ctxt.addServletMappingDecoded("/Comment", "Comment");
		Tomcat.addServlet(ctxt, "Cart", cart);
		ctxt.addServletMappingDecoded("/Cart", "Cart");
		Tomcat.addServlet(ctxt, "Bill", bill);
		ctxt.addServletMappingDecoded("/Bill", "Bill");
		ctxt.setAllowCasualMultipartParsing(true);	
		tomcat.start();
		tomcat.getServer().await();		
	}
}