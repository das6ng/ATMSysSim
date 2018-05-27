package life.dashyeah.ATMSysSim.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConn {
	
	private static Connection connectDB() {
		Connection conn = null;
		try {
			//load driver
			Class.forName("com.mysql.jdbc.Driver");
			
		    conn = DriverManager.getConnection(
		    		Cfg.getDatabaseUrl(), 
		    		Cfg.getDatabaseUser(), 
		    		Cfg.getDatabasePass());
		    System.out.println("[MSG] DB connection got.");
		    
		    return conn;
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	public static Connection getConn() {
		return connectDB();
	}
}
