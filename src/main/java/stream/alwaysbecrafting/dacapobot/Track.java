package stream.alwaysbecrafting.dacapobot;


import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.io.IOException;

//==============================================================================
class Track {
	//--------------------------------------------------------------------------
	int id;
	long timestamp;
	String title;
	String shortName;
	File file;
	String artist;
	String album;

	//--------------------------------------------------------------------------

	Track(File track){
	this.file = track;
	}

	//--------------------------------------------------------------------------

	String toURIString(){
		if(!this.file.exists()){
			System.out.println("Error: File does not Exist: " + this.file.toString());
			throw new RuntimeException(	);
		}
		return this.file.toURI().toString();

	}

	//--------------------------------------------------------------------------

	String getCanonicalPath(){
		try {
			return this.file.getCanonicalPath();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
			return "";
	}

	//--------------------------------------------------------------------------

	@Override
	public String toString(){
		return file.getName();

	}

	//--------------------------------------------------------------------------

	boolean fetchTrackData(){
		try {
			Mp3File mp3file = new Mp3File( this.file );

			ID3v1 id3v1 = null;
			if (mp3file.hasId3v1Tag()){
				id3v1 = mp3file.getId3v1Tag();
			}
			if (mp3file.hasId3v2Tag()){
				id3v1 = mp3file.getId3v2Tag();
			}
			if(id3v1 != null) {
				this.title  = id3v1.getTitle();
				this.artist = id3v1.getArtist();
				this.album  = id3v1.getAlbum();
				return true;
			}
		}
		catch ( Exception e ){
			e.printStackTrace();
		}
		return false;
	}

}

//------------------------------------------------------------------------------
