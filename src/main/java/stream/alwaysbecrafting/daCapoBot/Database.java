package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

//==============================================================================
public class Database {
	//--------------------------------------------------------------------------

	static String getDBPath(){
		try{
		File db = new File("db.db");
			return db.getCanonicalPath();
		}
		catch ( IOException e ){
			e.printStackTrace();
		}
		return "";
	}
	//--------------------------------------------------------------------------

	public static void createNewDatabase() {

		File db = new File("db.db");
		String url = "jdbc:sqlite:" + getDBPath();

		try (Connection conn = DriverManager.getConnection(url)) {
			if (conn != null) {
				DatabaseMetaData meta = conn.getMetaData();
				System.out.println("The driver name is " + meta.getDriverName());
				System.out.println("A new database has been created.");
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

//--------------------------------------------------------------------------

	public static void connect() {
		Connection conn = null;
		try {
			// db parameters
			String url = "jdbc:sqlite:" + getDBPath();
			// create a connection to the database
			conn = DriverManager.getConnection( url );

			System.out.println( "Connection to SQLite has been established." );
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
