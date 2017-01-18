package stream.alwaysbecrafting.dacapobot.TrackData;


import java.nio.file.Path;

public class TrackMetadata {
	public int id;
	public long timestamp;
	public final Path path;
	public final String title;
	public final String artist;
	public final String album;

	public TrackMetadata( Path path, String title, String artist, String album ) {
		this.path = path;
		this.title = title;
		this.artist = artist;
		this.album = album;
	}

	public void setId( int id ) {
		this.id = id;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}
}
