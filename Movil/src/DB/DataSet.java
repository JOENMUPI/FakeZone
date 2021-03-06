package DB;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class DataSet implements Serializable{
	private static final long serialVersionUID = 1L;
	private List<Row> list = new ArrayList<Row>();
	private int fieldCount, current = 0;
	public Row[] row;
	
	public DataSet(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmt = rs.getMetaData();
		this.fieldCount = rsmt.getColumnCount();
		while (rs.next()) {
			Row row = new Row();
			for (int i = 1; i <= this.fieldCount; i++) { row.setField(rsmt.getColumnLabel(i), rs.getObject(i)); }
			this.list.add(row);
		}
		
		this.row = this.list.toArray(new Row[this.list.size()]);
	}
	
	public Row next() { 
		if (!hasNext()) { throw new NoSuchElementException(); } 
		else { return this.row[this.current++]; }
	}
	
	public boolean hasNext() { if (this.current < this.row.length) { return true; } else { return false; } }
}