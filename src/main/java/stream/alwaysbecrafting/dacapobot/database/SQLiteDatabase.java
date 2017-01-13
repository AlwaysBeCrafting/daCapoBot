package stream.alwaysbecrafting.dacapobot.database;

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

import stream.alwaysbecrafting.dacapobot.Config;
import stream.alwaysbecrafting.dacapobot.Track;

import static java.lang.System.currentTimeMillis;

public class SQLiteDatabase implements Database {
	private static Connection connection;
	private static Config config;

	public SQLiteDatabase( Config config ) {
		this.config = config;
		connect();
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
			String url = "jdbc:sqlite:" + new File( config.getDB() ).getCanonicalPath() + "?journal_mode=WAL&synchronous=NORMAL&foreign_keys=ON";
			// create a connection to the database
			connection = DriverManager.getConnection( url );
			System.out.println( "Connection to SQLite has been established." );
			checkIfTablesExist();
		} catch ( SQLException | IOException e ) {
			throw new RuntimeException( e );
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

	public void addMP3s( File dir ) {

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
					statement.setString( 1, track.getTitle() );
					statement.setString( 2, track.getFile().getCanonicalPath() );
					statement.setString( 3, track.getArtist() );
					statement.setString( 4, track.getAlbum() );
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

	public void logChat( String user, String message ) {
		String sql = "INSERT INTO chat_log(timestamp,user,message) VALUES(?,?,?)";
		try ( PreparedStatement statement = connection.prepareStatement( sql ) ) {
			statement.setLong( 1, currentTimeMillis() );
			statement.setString( 2, user.toString() );
			statement.setString( 3, message );
			statement.executeUpdate();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

	public Track getFinalFromRequests() {
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
				Track track = new Track( new File( rs2.getString( "path" ) ) );
				track.setId( rs2.getInt( "id" ) );
				track.setTitle( rs2.getString( "title" ) );
				track.setArtist( rs2.getString( "artist" ) );
				track.setAlbum( rs2.getString( "album" ) );
				return track;

			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Track> addRequest( String user, final String request ) {
		List<Track> matchingTracks = getMatchingTracks( request );

		if (matchingTracks.size() == 1 && !matchingTracks.get( 0 ).getTitle().equalsIgnoreCase( getFinalFromRequests().getTitle() ) ) {
			String insertRequest = "INSERT INTO requests(timestamp, user, track_id) VALUES(?,?,?)";
			try ( PreparedStatement statement = connection.prepareStatement( insertRequest ) ) {
				statement.setLong( 1, System.currentTimeMillis() );
				statement.setString( 2, user );
				statement.setInt( 3, matchingTracks.get( 0 ).getId() );
				statement.execute();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		return matchingTracks;
	}

	public List<Track> addVeto( String user, final String request ) {
		List<Track> matchingTracks = getMatchingTracks( request );

		if(matchingTracks.size() == 1) {
			String insertRequest = "INSERT INTO vetoes(timestamp, user, track_id) VALUES(?,?,?)";
			try ( PreparedStatement statement = connection.prepareStatement( insertRequest ) ) {
				statement.setLong( 1, System.currentTimeMillis() );
				statement.setString( 2, user );
				statement.setInt( 3, matchingTracks.get( 0 ).getId() );
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
				Track track = new Track( new File( rs.getString( "path" ) ) );
				if(!track.exists()) {
					continue;
				}
				track.setId( rs.getInt( "id" ) );
				track.setTitle( rs.getString( "title" ) );
				track.setArtist( rs.getString( "artist" ) );
				track.setAlbum( rs.getString( "album" ) );
				tracks.add( track );
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
				Track track = new Track( new File( trackData.getString( "path" ) ) );
				track.setId( trackData.getInt( "id" ) );
				track.setTimestamp( requestSqlResults.getLong( "timestamp" ) );
				track.setTitle( trackData.getString( "title" ) );
				track.setArtist( trackData.getString( "artist" ) );
				track.setAlbum( trackData.getString( "album" ) );
				return track;
			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public Track getRandomTrack() {
		Track track = null;
		String sql = "SELECT * FROM tracks WHERE id IN (SELECT id FROM tracks ORDER BY RANDOM() LIMIT 1)";
		try ( PreparedStatement statement = connection.prepareStatement( sql ) ) {
			ResultSet rs = statement.executeQuery();
			track = new Track( new File( rs.getString( "path" ) ) );
			track.setId( rs.getInt( "id" ) );
			track.setTitle( rs.getString( "title" ) );
			track.setArtist( rs.getString( "artist" ) );
			track.setAlbum( rs.getString( "album" ) );
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
















