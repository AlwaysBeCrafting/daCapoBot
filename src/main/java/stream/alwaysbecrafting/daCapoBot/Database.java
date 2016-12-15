package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

//==============================================================================
class Database {
	//--------------------------------------------------------------------------
	/* TODO: 12/14/16 in your application, you should probably look at the query
		 plan for searching by title and short_name because those will be used very often*/
	static final Database DB_INSTANCE = new Database();
	private static Connection connection;

	private Database() {
		connect();
	}

	private static String getDBPath() {
		try {
			File db = new File( "daCapoBot.db" );
			return db.getCanonicalPath();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return "";
	}

	private void connect() {
		if ( connection != null ) {
			try {
				connection.close();
			} catch ( SQLException e ) {
				e.printStackTrace();
			}
		}

		connection = null;

		try {
			// db parameters
			String url = "jdbc:sqlite:" + getDBPath() + "?journal_mode=WAL&synchronous=NORMAL&foreign_keys=ON";
			// create a connection to the database
			connection = DriverManager.getConnection( url );
			System.out.println( "Connection to SQLite has been established." );
			checkIfTablesExist();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

	private void checkIfTablesExist() {
		System.out.println( "Creating tables..." );
		createTableTracks();
		createTableChatLog();
		createTableRequests();
		createTableVetoes();
		System.out.println( "Tables created." );
	}

	private void createTableChatLog() {
		System.out.println( "\tchat_log" );
		String sql = "CREATE TABLE IF NOT EXISTS chat_log ("
				+ "id          integer     PRIMARY KEY,"
				+ "timestamp   long        NOT NULL,"
				+ "user        text        COLLATE NOCASE,"
				+ "message     text        COLLATE NOCASE"
				+ ");";

		try(Statement stmt = connection.createStatement()){
			// create a new table
			stmt.execute( sql );
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

	private void createTableTracks() {
		System.out.println( "\ttracks" );

		String sql = "CREATE TABLE IF NOT EXISTS tracks ("
				+ "id          integer     PRIMARY KEY    ,"
				+ "title       text        COLLATE NOCASE ,"
				+ "short_name  text        COLLATE NOCASE ,"
				+ "path        text        NOT NULL       ,"
				+ "artist      text        COLLATE NOCASE ,"
				+ "album       text        COLLATE NOCASE  "
				+ ");                                       ";

		try(Statement stmt = connection.createStatement()){
			// create a new table
			stmt.execute( sql );
		} catch ( SQLException e ) {
			e.printStackTrace();
		}


	}

	private void createTableRequests() {
		System.out.println( "\trequest" );

		String sql =
				"CREATE TABLE IF NOT EXISTS requests ("
						+ "id          integer PRIMARY KEY                ,"
						+ "timestamp   long    NOT NULL                   ,"
						+ "user        text    COLLATE NOCASE             ,"
						+ "track_id    integer NOT NULL                   ,"
						+ "                                                "
						+ "FOREIGN KEY (track_id) REFERENCES tracks(id)    "
						+ ");";

		try(Statement stmt = connection.createStatement()){
			// create a new table
			stmt.execute( sql );
		} catch ( SQLException e ) {
			e.printStackTrace();
		}

	}

	private void createTableVetoes() {
		System.out.println( "\tvetoes" );

		String sql =
				"CREATE TABLE IF NOT EXISTS vetoes ("
						+ "id          integer PRIMARY KEY     ,"
						+ "timestamp   long    NOT NULL        ,"
						+ "user        text    COLLATE NOCASE  ,"
						+ "track_id    integer NOT NULL        ,"
						+ "                                     "
						+ "FOREIGN KEY (track_id) REFERENCES tracks(id)"
						+ ");";

		try(Statement stmt = connection.createStatement()){
			// create a new table
			stmt.execute( sql );
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

	void addMP3s( File dir ) {

		System.out.println( "Checking for tracks to insert..." );
		List<Track> tracks = Collections.emptyList();

		try {
			tracks = Files.walk( dir.toPath(), FileVisitOption.FOLLOW_LINKS )
					.filter( path -> path.toString().endsWith( ".mp3" ) )
					.map( path -> new Track( path.toFile() ) )
					.collect( Collectors.toList() );
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		List<String> pathsFromDB = new ArrayList<>();

		String sql = "INSERT INTO tracks(title,short_name,path,artist,album) VALUES(?,?,?,?,?)";

			String select = "SELECT path FROM tracks";
		try(Statement stmt = connection.createStatement()){
			ResultSet rs = stmt.executeQuery( select );

			// loop through the result set
			while ( rs.next() ) {
				pathsFromDB.add( rs.getString( "path" ) );
			}

		} catch ( Exception e ) {
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
		try(PreparedStatement statement = connection.prepareStatement( sql )){
			tracksToInsert.forEach( track -> {
				try {
					statement.setString( 1, track.title );
					statement.setString( 2, track.title.toLowerCase().replaceAll( "[^a-z0-9]+", "-" ) );
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
			System.out.println( "Added " + tracksToInsert.size() + " tracks to the database." );
			connection.setAutoCommit( true );
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

	void logChat( String user, String message ) {
		String sql = "INSERT INTO chat_log(timestamp,user,message) VALUES(?,?,?)";

		try(PreparedStatement statement = connection.prepareStatement( sql );){
			statement.setLong( 1, currentTimeMillis() );
			statement.setString( 2, user.toString() );
			statement.setString( 3, message );
			statement.executeUpdate();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

	Track getFirst() {
			String select = "SELECT path FROM tracks ORDER BY id ASC LIMIT 1";
		try(PreparedStatement statement = connection.prepareStatement( select )){
			ResultSet rs = statement.executeQuery();
			String s = rs.getString( "path" );

			return new Track( new File( s ) );
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	List<Track> getAfter( Track currentTrack, int numberToGet ) {
		int trackID;
		currentTrack.fetchTrackData();
		List<Track> trackList = new ArrayList<>();

		//get next track id from current track title
			String select = "SELECT id FROM tracks WHERE title = ? LIMIT 1";
			String select2 = "SELECT path FROM tracks WHERE id > ? LIMIT ?";
		try (PreparedStatement statement2 = connection.prepareStatement( select2 );PreparedStatement statement = connection.prepareStatement( select )){

			statement.setString( 1, currentTrack.title );

			ResultSet rs = statement.executeQuery();

			if ( rs.next() ) {
				trackID = rs.getInt( "id" );
				statement2.setInt( 1, trackID );
				statement2.setInt( 2, numberToGet);
				rs = statement2.executeQuery();
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

	boolean addToVeto( String user, String short_name ) {

			String select = "SELECT id, short_name FROM tracks WHERE short_name LIKE ?";
		try(PreparedStatement statement = connection.prepareStatement( select )){
			statement.setString( 1, "%" + short_name + "%");

			ResultSet rs = statement.executeQuery( );
			List<String> short_names = new ArrayList<>();
			List<Integer> track_ids   = new ArrayList<>();
			while (rs.next()){
				short_names.add( rs.getString( "short_name" ) );
				track_ids.add( rs.getInt( "id") );
			}

			if(short_names.isEmpty() || short_names.size() > 1){
				System.out.println("short_names was empty or size = " + short_names.size());

				return false;
			}
			else{
				System.out.println("inserting into vetoes");
				System.out.println(user);
				System.out.println(track_ids);
				String select2 = "INSERT INTO vetoes(timestamp,user,track_id) VALUES(?,?,?)";

				try(PreparedStatement statement2 = connection.prepareStatement( select2 )){
					statement2.setLong( 1, System.currentTimeMillis() );
					statement2.setString( 2, user );
					statement2.setInt( 3, track_ids.get( 0 ) );
					statement2.executeUpdate();
				} catch ( SQLException e ) {
					e.printStackTrace();
				}
				String query = "SELECT * FROM vetoes";
				try(PreparedStatement query1 = connection.prepareStatement( query ) ){
					ResultSet test = query1.executeQuery();
					while( test.next()){
						System.out.println(test.getInt( "id" ) + " " + test.getLong( "timestamp" ) + " " + test.getString( "user" ) + " " + test.getInt( "track_id" ) ) ;
					}
				}
				return true;

			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
			return false;
	}

	public String getShortName( String title ) {

			String select = "SELECT short_name FROM tracks WHERE title = ? LIMIT 1";
		try(PreparedStatement statement = connection.prepareStatement( select )){
			statement.setString( 1, title );
			ResultSet rs = statement.executeQuery();
			String s = rs.getString( "short_name" );
			return s;
		}
		catch ( SQLException e ){
			e.printStackTrace();
		}

		return "can't find short name";
	}
}
//------------------------------------------------------------------------------
