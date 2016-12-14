package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//==============================================================================
class Database {
	//--------------------------------------------------------------------------
	static final Database DB_INSTANCE = new Database();
	private static Connection connection;

	private Database() {
		connect();
	}

	//--------------------------------------------------------------------------

	private static String getDBPath(){
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
		boolean tableTracksExists = false;
		boolean tableChatLogExists = false;

		try {
			// db parameters
			String url = "jdbc:sqlite:" + getDBPath() + "?journal_mode=WAL&synchronous=NORMAL&foreign_keys=ON";
			// create a connection to the database
			connection = DriverManager.getConnection( url );
			System.out.println( "Connection to SQLite has been established." );

			System.out.println( "Checking if tables 'tracks' & 'chat_log' exists...");
			DatabaseMetaData md = connection.getMetaData();
			ResultSet tables = md.getTables( null, null, "%", null);
			while(tables.next()){
				if(tables.getString(3).equals("tracks") || tableTracksExists == true){
					tableTracksExists = true;
				}
				else{
					tableTracksExists = false;
				}
				if(tables.getString(3).equals("chat_log") || tableChatLogExists == true){
					tableChatLogExists = true;
				}
				else{
					tableChatLogExists = false;
				}
			}
			if(!tableTracksExists){
				System.out.println("Table 'Tracks' does not exist, creating...");
				createTableTracks();
			}
			else{
					System.out.println("Table Tracks Exists");
			}
			if(!tableChatLogExists){
				System.out.println("Table 'ChatLog' does not exist, creating...");
				createTableChatLog();
			}
			else{
					System.out.println("Table ChatLog Exists");
			}
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	private void createTableChatLog() {

		String sql = "CREATE TABLE IF NOT EXISTS chat_log (\n"
				+ "	id integer PRIMARY KEY,\n"
				+ "	timestamp long NOT NULL,\n"
				+ " user text COLLATE NOCASE,\n"
				+ " message text COLLATE NOCASE\n"
				+ ");";

		try{
			Statement stmt = connection.createStatement();
			// create a new table
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	private void createTableTracks(){

		String sql = "CREATE TABLE IF NOT EXISTS tracks (\n"
				+ "	id integer PRIMARY KEY,\n"
				+ "	title text COLLATE NOCASE,\n"
				+ " short_name text COLLATE NOCASE,\n"
				+ " path text NOT NULL,\n"
				+ " artist text COLLATE NOCASE,\n"
				+ " album text COLLATE NOCASE,\n"
				+ " requests INTEGER,\n"
				+ " vetoes INTEGER\n"
				+ ");";

		try{
		Statement stmt = connection.createStatement();
			// create a new table
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	void insertIntoTracksTable( File dir){

		System.out.println("Checking for tracks to insert...");
		List<Track> tracks = Collections.emptyList();

		try{
			tracks = Files.walk( dir.toPath(), FileVisitOption.FOLLOW_LINKS  )
				        .filter( path -> path.toString().endsWith( ".mp3" ) )
						.map( path -> new Track( path.toFile() ))
					    .collect( Collectors.toList() );
		}
		catch(Exception e){
			e.printStackTrace();
		}

		List<String> pathsFromDB = new ArrayList<>();

		String sql = "INSERT INTO tracks(title,short_name,path,artist,album,requests,vetoes) VALUES(?,?,?,?,?,0,0)";

		try{
			 String select = "SELECT path FROM tracks";
		     Statement stmt  = connection.createStatement();
		     ResultSet rs    = stmt.executeQuery(select);

				// loop through the result set
				while ( rs.next() ) {
					pathsFromDB.add( rs.getString( "path" ) );
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}

		List<Track> tracksToInsert = tracks
				.stream()
				.filter( track -> !pathsFromDB.contains( track.getCanonicalPath() ) )
				.collect( Collectors.toList() );


		tracksToInsert.parallelStream().forEach( Track::fetchTrackData );

		try {
			connection.setAutoCommit( false );
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		try {
			PreparedStatement statement = connection.prepareStatement( sql );
			tracksToInsert.forEach( track -> {
				try {
					statement.setString( 1, track.title );
					statement.setString( 2, "" );
					statement.setString( 3, track.file.getCanonicalPath() );
					statement.setString( 4, track.artist );
					statement.setString( 5, track.album );
					statement.executeUpdate();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			} );
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		try {
			connection.commit();
			System.out.println("Added " + tracksToInsert.size() + " tracks to the database.");
		}
		catch ( SQLException e ){
			e.printStackTrace();
		}
	}

	void logChat( String user, String message ) {
		String sql = "INSERT INTO chat_log(timestamp,user,message) VALUES(?,?,?)";

		try {
			PreparedStatement statement = connection.prepareStatement( sql );
			statement.setLong( 1, System.currentTimeMillis() );
			statement.setString( 2, user.toString() );
			statement.setString( 3, message );
			statement.executeUpdate();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

	Track getFirst(){
		try {
			String select = "SELECT path FROM tracks ORDER BY id ASC LIMIT 1";

			PreparedStatement statement = connection.prepareStatement( select );
			ResultSet rs = statement.executeQuery();

			return new Track( new File( rs.getString( "path" ) ) );
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	List<Track> getAfter( Track currentTrack, int numberToGet ) {
		int trackID;
		currentTrack.fetchTrackData();
		List<Track> trackList = new ArrayList<>();

		//get next track id from current track title
		try {

			String select = "SELECT id FROM tracks WHERE title = ? LIMIT 1";
			PreparedStatement statement = connection.prepareStatement( select );
			statement.setString( 1, currentTrack.title );

			ResultSet rs = statement.executeQuery();
			if ( rs.next() ) {
				trackID = rs.getInt( "id" );

				select = "SELECT path FROM tracks WHERE id > ? LIMIT ?";

				statement = connection.prepareStatement( select );
				statement.setInt( 1, trackID );
				statement.setInt( 2, numberToGet);
				rs = statement.executeQuery();
				while(rs.next()){
				trackList.add( new Track( new File( rs.getString( "path" ))));
				}
			} else {
				trackList.add( getFirst());
			}
			return trackList;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	void request( String s ) {}

	void veto() {}
}
//------------------------------------------------------------------------------
