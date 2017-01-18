package stream.alwaysbecrafting.dacapobot.database;

import java.io.File;
import java.util.List;

import stream.alwaysbecrafting.dacapobot.TrackData.TrackMetadata;

public interface Database {
	void logChat( String user, String message );
	void addMP3s( File dir ) throws Exception;
	List<TrackMetadata> addRequest( String user, final String request );
	TrackMetadata getFinalFromRequests();
	List<TrackMetadata> addVeto( String user, final String request );
	TrackMetadata getRandomTrack();
	TrackMetadata getNextRequested( long timestamp );
	void close();
}
