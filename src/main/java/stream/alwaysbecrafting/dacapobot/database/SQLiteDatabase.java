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

	PreparedStatement insertIntoChatlog;
	PreparedStatement getLastTrackInRequests;
	PreparedStatement insertIntoRequests;
	PreparedStatement insertIntoVeto;
	PreparedStatement fuzzySearchTrackByTitle;
	PreparedStatement nextRequestSql;
	PreparedStatement getRandomFromTracks;

	public SQLiteDatabase( Config config ) {
		this.config = config;
		connect();
		try {
			insertIntoChatlog = connection.prepareStatement( "INSERT INTO chat_log(timestamp,user,message) VALUES(?,?,?)" );
			getLastTrackInRequests = connection.prepareStatement( "SELECT t.path, t.title, t.artist, t.album, t.id " +
					"FROM tracks as t " +
					"INNER JOIN requests as r ON t.id = r.track_id " +
					"ORDER BY r.id DESC LIMIT 1" );
			insertIntoRequests = connection.prepareStatement( "INSERT INTO requests(timestamp, user, track_id) VALUES(?,?,?)" );
			insertIntoVeto = connection.prepareStatement( "INSERT INTO vetoes(timestamp, user, track_id) VALUES(?,?,?)" );
			fuzzySearchTrackByTitle = connection.prepareStatement( "SELECT * FROM tracks WHERE title LIKE ?" );
			nextRequestSql = connection.prepareStatement( "SELECT t.id, r.timestamp, t.title, t.album, t.artist, t.path " +
					"FROM tracks AS t " +
					"INNER JOIN requests AS r " +
					"ON t.id = r.track_id WHERE r.timestamp > ? limit 1" );
			getRandomFromTracks = connection.prepareStatement( "SELECT * FROM tracks WHERE id IN (SELECT id FROM tracks ORDER BY RANDOM() LIMIT 1)" );
		} catch( SQLException e ){
			throw new RuntimeException( e );
		}
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
		try {
			insertIntoChatlog.setLong( 1, currentTimeMillis() );
			insertIntoChatlog.setString( 2, user.toString() );
			insertIntoChatlog.setString( 3, message );
			insertIntoChatlog.executeUpdate();
		} catch ( SQLException e ) {
			e.printStackTrace();
		}
	}

	public TrackMetadata getFinalFromRequests() {
		try {
			ResultSet resultSet = getLastTrackInRequests.executeQuery();
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
			try {
				insertIntoRequests.setLong( 1, System.currentTimeMillis() );
				insertIntoRequests.setString( 2, user );
				insertIntoRequests.setInt( 3, matchingTracks.get( 0 ).id );
				insertIntoRequests.execute();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		return matchingTracks;
	}

	public List<TrackMetadata> addVeto( String user, final String request ) {
		List<TrackMetadata> matchingTracks = getMatchingTracks( request );
		if(matchingTracks.size() == 1) {
			try {
				insertIntoVeto.setLong( 1, System.currentTimeMillis() );
				insertIntoVeto.setString( 2, user );
				insertIntoVeto.setInt( 3, matchingTracks.get( 0 ).id );
				insertIntoVeto.execute();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		return matchingTracks;
	}

	List<TrackMetadata> getMatchingTracks( String request ) {
		List<TrackMetadata> tracks = new ArrayList<>();
		String formattedRequest = request.replaceAll( "[\\W_]+", "%" );
		try {
			fuzzySearchTrackByTitle.setString( 1, "%" + formattedRequest + "%" );
			ResultSet rs = fuzzySearchTrackByTitle.executeQuery();
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
		try {
			nextRequestSql.setLong( 1, timestamp );
			ResultSet trackData = nextRequestSql.executeQuery();
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
		try {
			ResultSet rs = getRandomFromTracks.executeQuery();
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
















