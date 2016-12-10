package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//==============================================================================
public class Database {
	//--------------------------------------------------------------------------
	private Connection connection;
	private ResultSet tables;

	Database() {
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
		File db = new File("daCapoBot.db");
			return db.getCanonicalPath();
		}
		catch ( IOException e ){
			e.printStackTrace();
		}
		return "";
	}

	//--------------------------------------------------------------------------

	private void connect() {
		if( connection != null){
			try{
				connection.close();
			}catch(SQLException e){
				System.out.println(e.getMessage());
			}
		}

		connection = null;
		boolean tableExists = false;

		try {
			// db parameters
			String url = "jdbc:sqlite:" + getDBPath();
			// create a connection to the database
			connection = DriverManager.getConnection( url );
			System.out.println( "Connection to SQLite has been established." );

			System.out.println( "Checking if table 'Tracks' exists...");
			DatabaseMetaData md = connection.getMetaData();
			tables = md.getTables( null, null, "%", null);
			while(tables.next()){
				if(tables.getString(3).equals("Tracks")){
					System.out.println("Table Exists");
					tableExists = true;
				}
				else{

					tableExists = false;
				}
			}
			if(!tableExists){
				System.out.println("Table 'Tracks' does not exist, creating...");
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
				+ "	Title text COLLATE NOCASE,\n"
				+ " Path text NOT NULL,\n"
				+ " Artist text COLLATE NOCASE,\n"
				+ " Album text COLLATE NOCASE,\n"
				+ " Rating DECIMAL(3,2)\n"
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

	public void insertToTracks(Playlist playlist){
		connect();

		List<String> pathsFromDB = new ArrayList<>();

		String sql = "INSERT INTO Tracks(Title,Path,Artist,Album,Rating) VALUES(?,?,?,?,50)";

		try{
			 String select = "SELECT Path FROM Tracks";
		     Statement stmt  = connection.createStatement();
		     ResultSet rs    = stmt.executeQuery(select);

				// loop through the result set
				while ( rs.next() ) {
					pathsFromDB.add( rs.getString( "Path" ) );
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}

		List<Track> tracksToInsert = playlist
				.stream()
				.filter( track -> !pathsFromDB.contains( track.getCanonicalPath() ) )
				.collect( Collectors.toList() );

		try {
			connection.setAutoCommit( false );
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		tracksToInsert.forEach( track -> {
			try {
				PreparedStatement statement = connection.prepareStatement( sql );
				statement.setString( 1, track.title );
				statement.setString( 2, track.file.getCanonicalPath() );
				statement.setString( 3, track.artist );
				statement.setString( 4, track.album );
				statement.executeUpdate();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		} );

		try {
			connection.commit();
			System.out.println("Added " + tracksToInsert.size() + " tracks to the database.");
		}
		catch ( SQLException e ){
			e.printStackTrace();
		}
	}

	//--------------------------------------------------------------------------


}
//------------------------------------------------------------------------------
