package Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class Txt {
	private static File file;
	private static String path;
	private static int i = 0;
	
	public Txt() {
		path = System.getProperty("user.dir") + "\\" + Calendar.getInstance().get(Calendar.YEAR)+"-" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		file = new File(path + "_" + i + ".txt");
	}
	
	private void createtxt() {
		i = 0;
		do {
			i++;
			file = new File(path + "_" + i + ".txt"); 
		} while(file.exists());
		
		try { file.createNewFile(); } 
		catch (Exception e) { System.out.println(e); e.printStackTrace(); }
	}
	
	public void setLine(String line){
		if(file.length() >= 1000000) { this.createtxt(); }
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getName(), true));
			bw.newLine();
			bw.write(line);
	        bw.close();
		} 
		
		catch (IOException e) { System.out.println(e); e.printStackTrace(); }
	} 
}