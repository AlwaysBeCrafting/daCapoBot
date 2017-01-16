package stream.alwaysbecrafting.dacapobot;


import java.nio.file.Files;
import java.nio.file.Path;

public class TrackData {
	int id;
	long timestamp;
	final Path path;
	final String title;
	final String artist;
	final String album;

	public TrackData( Path path, String title, String artist, String album ) {
		this.path = path;
		this.title = title;
		this.artist = artist;
		this.album = album;
	}

	public String toURIString() {
		return this.path.toUri().toString();
	}

	public boolean exists() {
		return Files.exists( this.path );
	}

	@Override
	public String toString(){
		return this.id
			+ " " + this.timestamp
			+ " " + this.title
			+ " " + this.path
			+ " " + this.artist
			+ " " + this.album;

	}

	public int getId() {
		return this.id;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public String getTitle() {
		return this.title;
	}

	public Path getPath() {
		return this.path;
	}

	public String getArtist() {
		return this.artist;
	}

	public String getAlbum() {
		return this.album;
	}

	public void setId( int id ) {
		this.id = id;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}
}
