package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//==============================================================================
public class Database {
	//--------------------------------------------------------------------------
	private Connection connection;
	private ResultSet tables;

	public void test() {
		connect();
		try {
			connection.close();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

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

	private void connect() {
		connection = null;
		boolean tableExists = false;

		try {
			// db parameters
			String url = "jdbc:sqlite:" + getDBPath();
			System.out.println("test1");
			// create a connection to the database
			connection = DriverManager.getConnection( url );
			System.out.println( "Connection to SQLite has been established." );

			System.out.println( "Checking if table 'Tracks' exists...");
			DatabaseMetaData md = connection.getMetaData();
			tables = md.getTables( null, null, "%", null);
			while(tables.next()){
				if(tables.getString(3).equals("Tracks")){
					System.out.println("Tracks Exists");
					tableExists = true;
				}
				else{

					tableExists = false;
				}
			}
			if(!tableExists){
				System.out.println("Tracks does not exist, creating...");
				createTable();
			}

		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	//--------------------------------------------------------------------------

	private void createTable(){

		String sql = "CREATE TABLE IF NOT EXISTS Tracks (\n"
				+ "	id integer PRIMARY KEY,\n"
				+ "	Title text NOT NULL,\n"
				+ " Path text NOT NULL,\n"
				+ " Artist text NOT NULL,\n"
				+ " Album text NOT NULL,\n"
				+ " Rating DECIMAL(3,2) NOT NULL\n"
				+ ");";

		try{
		Statement stmt = connection.createStatement();
			// create a new table
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}
	//--------------------------------------------------------------------------


}
//------------------------------------------------------------------------------
