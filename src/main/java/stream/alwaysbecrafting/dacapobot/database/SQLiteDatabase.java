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
import stream.alwaysbecrafting.dacapobot.TrackData;
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

	public void addMP3s( File dir ) throws Exception{
		System.out.println( "Checking for tracks to insert ...");
		Set<String> pathsFromDB = new HashSet<>();
		String tracksPathColumn = "SELECT path FROM tracks";
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery( tracksPathColumn );
		while( resultSet.next() ) {
			pathsFromDB.add( resultSet.getString( "path" ) );
		}
		statement.close();
		List<TrackData> tracksToInsert = Files.walk( dir.toPath(), FileVisitOption.FOLLOW_LINKS )
				.filter( path -> path.toString().endsWith( ".mp3" ))
				.filter( path -> !pathsFromDB.contains( path ) )
				.parallel()
				.collect(
						ArrayList::new,
						( list, path ) -> mp3Parser.tryParse( path ).ifPresent( value -> list.add( value ) ),
						( list1, list2 ) -> list1.addAll( list2 )
				);
		connection.setAutoCommit( false );
		String insertIntoTracks = "INSERT INTO tracks(title,path,artist,album) VALUES(?,?,?,?)";
		PreparedStatement preparedStatement = connection.prepareStatement( insertIntoTracks );
		tracksToInsert.forEach( track -> {
			try {
				preparedStatement.setString( 1, track.getTitle() );
				preparedStatement.setString( 2, track.getPath().toString() );
				preparedStatement.setString( 3, track.getArtist() );
				preparedStatement.setString( 4, track.getAlbum() );
				preparedStatement.executeUpdate();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		} );
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

	public TrackData getFinalFromRequests() {
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
				TrackData track = new TrackData(
						Paths.get( rs2.getString( "path" ) ),
						rs2.getString( "title" ),
						rs2.getString( "artist" ),
						rs2.getString( "album" ) );
				track.setId( rs2.getInt( "id" ) );
				return track;

			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public List<TrackData> addRequest( String user, final String request ) {
		List<TrackData> matchingTracks = getMatchingTracks( request );

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

	public List<TrackData> addVeto( String user, final String request ) {
		List<TrackData> matchingTracks = getMatchingTracks( request );

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

	List<TrackData> getMatchingTracks( String request ) {
		List<TrackData> tracks = new ArrayList<>();
		String formattedRequest = request.replaceAll( "[\\W_]+", "%" );
		try ( PreparedStatement statement = connection.prepareStatement(
				"SELECT * FROM tracks WHERE title LIKE ?") ) {
			statement.setString( 1, "%" + formattedRequest + "%" );
			ResultSet rs = statement.executeQuery();
			while ( rs.next() ) {
				TrackData track = new TrackData(
						Paths.get( rs.getString( "path" ) ),
						rs.getString( "title" ),
						rs.getString( "artist" ),
						rs.getString( "album" ) );
				if(!track.exists()) {
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

	public TrackData getNextRequested( long timestamp ) {
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
				TrackData track = new TrackData(
						Paths.get( trackData.getString( "path" ) ),
						trackData.getString( "title" ),
						trackData.getString( "artist" ),
						trackData.getString( "album" )  );
				track.setId( trackData.getInt( "id" ) );
				track.setTimestamp( requestSqlResults.getLong( "timestamp" ) );
				return track;
			}
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public TrackData getRandomTrack() {
		TrackData track = null;
		String sql = "SELECT * FROM tracks WHERE id IN (SELECT id FROM tracks ORDER BY RANDOM() LIMIT 1)";
		try ( PreparedStatement statement = connection.prepareStatement( sql ) ) {
			ResultSet rs = statement.executeQuery();
			track = new TrackData(
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
















