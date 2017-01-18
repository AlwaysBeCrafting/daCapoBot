package stream.alwaysbecrafting.dacapobot.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import stream.alwaysbecrafting.dacapobot.Config;
import stream.alwaysbecrafting.dacapobot.TrackData.TrackMetadata;
import stream.alwaysbecrafting.dacapobot.bot.commands.Mp3Parser;
import stream.alwaysbecrafting.dacapobot.bot.commands.Parser;

import static java.lang.System.currentTimeMillis;

public class SQLiteDatabase implements Database {
	private static Connection connection;
	private static Config config;
	private Parser mp3Parser = new Mp3Parser();

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
			String url = "jdbc:sqlite:" + new File( config.getDB() ).getCanonicalPath() + "?journal_mode=WAL&synchronous=NORMAL&foreign_keys=ON";
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

	public void addMP3s( File dir ) throws Exception{
		System.out.println( "Checking for tracks to insert ...");
		Set<String> pathsFromDB = new HashSet<>();
		String tracksPathColumn = "SELECT path FROM tracks";
		try( Statement statement = connection.createStatement() ) {
			ResultSet resultSet = statement.executeQuery( tracksPathColumn );
			while ( resultSet.next() ) {
				pathsFromDB.add( resultSet.getString( "path" ) );
			}
		}
		List<TrackMetadata> tracksToInsert = Files.walk( dir.toPath(), FileVisitOption.FOLLOW_LINKS )
				.filter( path -> path.toString().endsWith( ".mp3" ))
				.filter( path -> !pathsFromDB.contains( path.toString() ) )
				.parallel()
				.collect(
						ArrayList::new,
						( list, path ) -> mp3Parser.tryParse( path ).ifPresent( value -> list.add( value ) ),
						( list1, list2 ) -> list1.addAll( list2 )
				);
		connection.setAutoCommit( false );
		String insertIntoTracks = "INSERT INTO tracks(title,path,artist,album) VALUES(?,?,?,?)";
		try( PreparedStatement preparedStatement = connection.prepareStatement( insertIntoTracks ) ) {
			tracksToInsert.forEach( track -> {
				try {
					preparedStatement.setString( 1, track.title );
					preparedStatement.setString( 2, track.path.toString() );
					preparedStatement.setString( 3, track.artist );
					preparedStatement.setString( 4, track.album );
					preparedStatement.executeUpdate();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			} );
		}
		connection.commit();
		System.out.println( "Added " + tracksToInsert.size() + " tracks to the database." );
		connection.setAutoCommit( true );
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

	public TrackMetadata getFinalFromRequests() {
		String select = "SELECT t.path, t.title, t.artist, t.album, t.id " +
				"FROM tracks as t " +
				"INNER JOIN requests as r ON t.id = r.track_id " +
				"ORDER BY r.id DESC LIMIT 1";
		try ( PreparedStatement statement = connection.prepareStatement( select ) ) {
			ResultSet resultSet = statement.executeQuery();
			TrackMetadata track = new TrackMetadata(
					Paths.get( resultSet.getString( "path" ) ),
					resultSet.getString( "title" ),
					resultSet.getString( "artist" ),
					resultSet.getString( "album" ) );
			track.setId( resultSet.getInt( "id" ) );
			return track;
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public List<TrackMetadata> addRequest( String user, final String request ) {
		List<TrackMetadata> matchingTracks = getMatchingTracks( request );

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

	public List<TrackMetadata> addVeto( String user, final String request ) {
		List<TrackMetadata> matchingTracks = getMatchingTracks( request );

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

	List<TrackMetadata> getMatchingTracks( String request ) {
		List<TrackMetadata> tracks = new ArrayList<>();
		String formattedRequest = request.replaceAll( "[\\W_]+", "%" );
		try ( PreparedStatement statement = connection.prepareStatement(
				"SELECT * FROM tracks WHERE title LIKE ?") ) {
			statement.setString( 1, "%" + formattedRequest + "%" );
			ResultSet rs = statement.executeQuery();
			while ( rs.next() ) {
				TrackMetadata track = new TrackMetadata(
						Paths.get( rs.getString( "path" ) ),
						rs.getString( "title" ),
						rs.getString( "artist" ),
						rs.getString( "album" ) );
				if(!Files.exists( track.path )) {
					continue;
				}
				track.setId( rs.getInt( "id" ) );
				tracks.add( track );
			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return tracks;
	}

	public TrackMetadata getNextRequested( long timestamp ) {
		String nextRequestSql = "SELECT t.id, r.timestamp, t.title, t.album, t.artist, t.path " +
				"FROM tracks AS t " +
				"INNER JOIN requests AS r " +
				"ON t.id = r.track_id WHERE r.timestamp > ? limit 1";
		try ( PreparedStatement requestSql = connection.prepareStatement( nextRequestSql ) ) {
			requestSql.setLong( 1, timestamp );
			ResultSet trackData = requestSql.executeQuery();
			while( trackData.next() ) {
				TrackMetadata track = new TrackMetadata(
					Paths.get( trackData.getString( "t.path" ) ),
					trackData.getString( "t.title" ),
					trackData.getString( "t.artist" ),
					trackData.getString( "t.album" )  );
			track.setId( trackData.getInt( "t.id" ) );
			track.setTimestamp( trackData.getLong( "r.timestamp" ) );
			return track;
			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public TrackMetadata getRandomTrack() {
		TrackMetadata track = null;
		String sql = "SELECT * FROM tracks WHERE id IN (SELECT id FROM tracks ORDER BY RANDOM() LIMIT 1)";
		try ( PreparedStatement statement = connection.prepareStatement( sql ) ) {
			ResultSet rs = statement.executeQuery();
			track = new TrackMetadata(
					Paths.get( rs.getString( "path" ) ),
					rs.getString( "title" ),
					rs.getString( "artist" ),
					rs.getString( "album" ) );
			track.setId( rs.getInt( "id" ) );
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
















