package DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import Utilities.Props;

public class DB {
	private static Connection cn ;
	private PreparedStatement pstm = null;
	
	public DB(Properties props) {
		cn = new connection().newConnection(props);
	}
	
	public DataSet query(String path, String props,String query) {
		try {
			this.pstm = cn.prepareStatement(Props.getPropertiesFile(path, props).getProperty(query));
			return new DataSet(this.pstm.executeQuery());
		} 
		
		catch (SQLException e) { e.printStackTrace(); return null; } 
		finally { this.free(); }
	}
	
	public DataSet query(String path, String props, String query, Object... params) {
		try {
			this.pstm = setParams(cn.prepareStatement(Props.getPropertiesFile(path, props).getProperty(query)), params);
			return new DataSet(this.pstm.executeQuery());
		} 
	
		catch (SQLException e) { e.printStackTrace(); return null; } 
		finally { free(); }
	}
	
	public DataSet doInsert(String path, String props, String query, Object ...params) {
		try {
			this.pstm = setParams(cn.prepareStatement(Props.getPropertiesFile(path, props).getProperty(query), PreparedStatement.RETURN_GENERATED_KEYS), params);
			this.pstm.executeUpdate();
			return new DataSet(this.pstm.getGeneratedKeys());
		} 
		
		catch (SQLException e) { e.printStackTrace(); return null; } 
		finally { this.free(); }
	}
	
	public Integer update(String path, String props, String query, Object ...params) {
		try {
			this.pstm = setParams(cn.prepareStatement(Props.getPropertiesFile(path, props).getProperty(query)), params);
			return new Integer(this.pstm.executeUpdate());
		} 
		
		catch (SQLException e) { e.printStackTrace(); return null; } 
		finally { this.free(); }
	}
	
	private PreparedStatement setParams(PreparedStatement p, Object[] params) throws SQLException {
		for (int i = 0; i < params.length; i++) { p.setObject(i + 1, params[i]); }
		return p;
	}	
	
	private void free() { try { pstm.close(); } catch (SQLException e) { e.printStackTrace(); } }
}