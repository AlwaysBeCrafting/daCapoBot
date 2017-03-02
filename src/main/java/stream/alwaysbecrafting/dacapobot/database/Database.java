package stream.alwaysbecrafting.dacapobot.database;

import java.io.File;
import java.util.List;

import stream.alwaysbecrafting.dacapobot.TrackData.TrackMetadata;

public interface Database {
	void logChat( String user, String message );
	void addMP3s( File dir ) throws Exception;
	List<TrackMetadata> searchTracksByTitle( String request );
	void addRequest( String user, final TrackMetadata trackMetadata );
	TrackMetadata getFinalFromRequests();
	void addVeto( String user, final TrackMetadata trackMetadata );
	TrackMetadata getRandomTrack();
	TrackMetadata getNextRequested( long timestamp );

	void close();
}
