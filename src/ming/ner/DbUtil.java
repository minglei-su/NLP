package ming.ner;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.Properties;

public class DbUtil {
    private static String url="jdbc:oracle:thin:@192.168.1.203:1521:mn";
    private static String usr="yao";
    private static String passwd="12345678";
    
	private Connection connection;
    private PreparedStatement statement;
    private ResultSet set;
    
    private Properties prop ;
    private String size;
	public void open() {
	    try {
	    	prop = new Properties();
	    	prop.load(new FileReader("./dbutil.properties"));
	    	size = prop.getProperty("dbutil.limit");
	        Class.forName("oracle.jdbc.driver.OracleDriver");
	        connection = DriverManager.getConnection(url,usr,passwd);
	        //preparedStatement = connection.prepareStatement("select column_name from all_tab_ columns where table_name=? order by column_id");
	        statement  = connection.prepareStatement("select borrowernamecn from tb_enterpriseinf");
	        set = statement.executeQuery();
	    }catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public boolean getFile() {
		try {
			OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(
					"./models/cwsdbsource.txt"));
			for(int i=0; i<Integer.valueOf(size); i++) {
				w.write(getNext()+"\r\n");
			}
            w.flush();
            w.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
    public void close() throws SQLException {
        if(statement != null) {
            statement.close();
        }
        if(connection != null) {
                connection.close();

        }
    }
	public static void main(String args[]) {
        DbUtil db = new DbUtil();
        db.open();
        db.getFile();
        try {
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	public String getNext() {
		try {
			if(set.next()) {
				return set.getString(1);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
