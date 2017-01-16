package stream.alwaysbecrafting.dacapobot.database;

import java.io.File;
import java.util.List;

import stream.alwaysbecrafting.dacapobot.TrackData;

public interface Database {
	void logChat( String user, String message );
	void addMP3s( File dir ) throws Exception;
	List<TrackData> addRequest( String user, final String request );
	TrackData getFinalFromRequests();
	List<TrackData> addVeto( String user, final String request );
	TrackData getRandomTrack();
	TrackData getNextRequested( long timestamp );
	void close();
}
