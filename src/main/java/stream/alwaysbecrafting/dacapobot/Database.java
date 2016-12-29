package stream.alwaysbecrafting.dacapobot;

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
import java.util.Scanner;
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
			File db = new File( "dacapobot.db" );
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
		System.out.println( "Validating SQL schema..." );

		List<String> sqlList = new ArrayList<>();
		try(Scanner scanner = new Scanner( ClassLoader.getSystemResourceAsStream( "schema.sql" ))){
			String scannedLine;
			scanner.useDelimiter( ";" );

			while( scanner.hasNext() ){
				scannedLine = scanner.next();
				if(!scannedLine.matches( "\\s*" )) {
					sqlList.add( scannedLine + ";" );
				}
			}
		}

		try ( Statement stmt = connection.createStatement() ) {
			//noinspection SimplifyStreamApiCallChains
			sqlList.stream().forEachOrdered( sql -> {
				try {
					stmt.execute( sql );
				} catch ( SQLException e ) {
					e.printStackTrace();
				}
			});

		} catch ( SQLException e ) {
			e.printStackTrace();
		}

		System.out.println( "Done." );
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

		String select = "SELECT path FROM tracks";
		try ( Statement stmt = connection.createStatement() ) {
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
		String sql = "INSERT INTO tracks(title,path,artist,album) VALUES(?,?,?,?)";
		try ( PreparedStatement statement = connection.prepareStatement( sql ) ) {
			tracksToInsert.forEach( track -> {
				try {
					statement.setString( 1, track.title );
					statement.setString( 2, track.file.getCanonicalPath() );
					statement.setString( 3, track.artist );
					statement.setString( 4, track.album );
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
		try ( PreparedStatement statement = connection.prepareStatement( sql ); ) {
			statement.setLong( 1, currentTimeMillis() );
			statement.setString( 2, user.toString() );
			statement.setString( 3, message );
			statement.executeUpdate();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

	Track getFinalFromRequests() {
		String select = "SELECT * FROM requests ORDER BY id DESC LIMIT 1";
		try ( PreparedStatement statement = connection.prepareStatement( select ) ) {
			ResultSet rs1 = statement.executeQuery();
			if ( rs1.isClosed() ) {
				return null;
			}
			String select2 = "SELECT * FROM tracks WHERE id LIKE ?";
			try ( PreparedStatement statement2 = connection.prepareStatement( select2 ) ) {
				statement2.setInt( 1, rs1.getInt( "track_id" ) );
				ResultSet rs2 = statement2.executeQuery();
				Track temp = new Track( new File( rs2.getString( "path" ) ) );
				temp.id = rs2.getInt( "id" );
				temp.title = rs2.getString( "title" );
				temp.artist = rs2.getString( "artist" );
				temp.album = rs2.getString( "album" );
				return temp;

			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return null;
	}

	List<Track> addRequest( String user, final String request ) {
		List<Track> matchingTracks = getMatchingTracks( request );

		if (matchingTracks.size() == 1 && !matchingTracks.get( 0 ).title.equalsIgnoreCase( getFinalFromRequests().title ) ) {
			String insertRequest = "INSERT INTO requests(timestamp, user, track_id) VALUES(?,?,?)";
			try ( PreparedStatement statement = connection.prepareStatement( insertRequest ) ) {
				statement.setLong( 1, System.currentTimeMillis() );
				statement.setString( 2, user );
				statement.setInt( 3, matchingTracks.get( 0 ).id );
				statement.execute();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		return matchingTracks;
	}

	List<Track> addVeto( String user, final String request ) {
		List<Track> matchingTracks = getMatchingTracks( request );

		if(matchingTracks.size() == 1) {
			String insertRequest = "INSERT INTO vetoes(timestamp, user, track_id) VALUES(?,?,?)";
			try ( PreparedStatement statement = connection.prepareStatement( insertRequest ) ) {
				statement.setLong( 1, System.currentTimeMillis() );
				statement.setString( 2, user );
				statement.setInt( 3, matchingTracks.get( 0 ).id );
				statement.execute();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		return matchingTracks;
	}

	List<Track> getMatchingTracks( String request ) {
		List<Track> tracks = new ArrayList<>();
		String formattedRequest = request.replaceAll( "[\\W_]+", "%" );
		try ( PreparedStatement statement = connection.prepareStatement(
				"SELECT * FROM tracks WHERE title LIKE ?") ) {
			statement.setString( 1, "%" + formattedRequest + "%" );
			ResultSet rs = statement.executeQuery();
			while ( rs.next() ) {
				Track temp = new Track( new File( rs.getString( "path" ) ) );
				if(!temp.exists()) {
					continue;
				}
				temp.id = rs.getInt( "id" );
				temp.title = rs.getString( "title" );
				temp.artist = rs.getString( "artist" );
				temp.album = rs.getString( "album" );
				tracks.add( temp );
			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return tracks;
	}


	public Track getNextRequested( long timestamp ) {
		String nextRequestSql = "SELECT * FROM requests WHERE timestamp > ? limit 1";
		try ( PreparedStatement requestSql = connection.prepareStatement( nextRequestSql ) ) {
			requestSql.setLong( 1, timestamp );
			ResultSet requestSqlResults = requestSql.executeQuery();
			if ( requestSqlResults.isClosed() ) {
				return null;
			}
			String getTrackData = "SELECT * FROM tracks WHERE id = ?";
			try ( PreparedStatement trackSql = connection.prepareStatement( getTrackData ) ) {
				trackSql.setInt( 1, requestSqlResults.getInt( "track_id" ) );
				ResultSet trackData = trackSql.executeQuery();
				Track temp = new Track( new File( trackData.getString( "path" ) ) );
				temp.id = trackData.getInt( "id" );
				temp.timestamp = requestSqlResults.getLong( "timestamp" );
				temp.title = trackData.getString( "title" );
				temp.artist = trackData.getString( "artist" );
				temp.album = trackData.getString( "album" );
				return temp;
			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return null;
	}

//------------------------------------------------------------------------------

	Track getRandomTrack() {
		Track track = null;
		String sql = "SELECT * FROM tracks WHERE id IN (SELECT id FROM tracks ORDER BY RANDOM() LIMIT 1)";
		try ( PreparedStatement statement = connection.prepareStatement( sql ) ) {
			ResultSet rs = statement.executeQuery();
			track = new Track( new File( rs.getString( "path" ) ) );
			track.id = rs.getInt( "id" );
			track.title = rs.getString( "title" );
			track.artist = rs.getString( "artist" );
			track.album = rs.getString( "album" );
			return track;
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return track;
	}

	public void close() {
		try {
			connection.close();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}

	}
}
















