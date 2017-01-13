package stream.alwaysbecrafting.dacapobot.database;

import java.io.File;
import java.util.List;

import stream.alwaysbecrafting.dacapobot.Track;

public interface Database {

	void logChat( String user, String message );
	void addMP3s( File dir );
	List<Track> addRequest( String user, final String request );
	Track getFinalFromRequests();
	List<Track> addVeto( String user, final String request );
	Track getRandomTrack();
	Track getNextRequested( long timestamp );
	void close();

}
