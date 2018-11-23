package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class connection {
	public  Connection newConnection(Properties props) {
		try {
			Class.forName(props.getProperty("driver"));			
			return DriverManager.getConnection(props.getProperty("url"), props.getProperty("user"), props.getProperty("password"));
		} 
			
		catch(SQLException | ClassNotFoundException e) { e.printStackTrace(); }
		return null;
	}
}