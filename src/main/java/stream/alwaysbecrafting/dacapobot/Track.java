package stream.alwaysbecrafting.dacapobot;


import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.io.IOException;

public class Track {
	int id;
	long timestamp;
	String title;
	File file;
	String artist;
	String album;

	public Track( File track ) {
		this.file = track;
	}

	public String toURIString() {
		return this.file.toURI().toString();
	}

	public String getCanonicalPath() {
		try {
			return this.file.getCanonicalPath();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return "";
	}

	public boolean fetchTrackData() {
		try {
			Mp3File mp3file = new Mp3File( this.file );

			ID3v1 id3v1 = null;
			if ( mp3file.hasId3v1Tag() ) {
				id3v1 = mp3file.getId3v1Tag();
			}
			if ( mp3file.hasId3v2Tag() ) {
				id3v1 = mp3file.getId3v2Tag();
			}
			if ( id3v1 != null ) {
				this.title = id3v1.getTitle();
				this.artist = id3v1.getArtist();
				this.album = id3v1.getAlbum();
				return true;
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean exists() {
		return this.file.exists();
	}

	@Override
	public String toString(){
		return this.id
			+ " " + this.timestamp
			+ " " + this.title
			+ " " + this.file
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

	public File getFile() {
		return this.file;
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

	public void setFile( File file ) {
		this.file = file;
	}

	public void setTitle( String title ) {
		this.title = title;
	}

	public void setArtist( String artist ) {
		this.artist = artist;
	}

	public void setAlbum( String album ) {
		this.album = album;
	}
}

//------------------------------------------------------------------------------
