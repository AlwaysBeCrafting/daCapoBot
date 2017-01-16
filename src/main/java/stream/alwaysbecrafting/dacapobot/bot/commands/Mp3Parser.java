package stream.alwaysbecrafting.dacapobot.bot.commands;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import stream.alwaysbecrafting.dacapobot.TrackData;

public class Mp3Parser implements Parser{

	public Optional<TrackData> tryParse( Path path ){
		try {
			Mp3File mp3file = new Mp3File( path.toFile() );
			ID3v1 id3v1 = null;
			if ( mp3file.hasId3v1Tag() ) {
				id3v1 = mp3file.getId3v1Tag();
			}
			if ( mp3file.hasId3v2Tag() ) {
				id3v1 = mp3file.getId3v2Tag();
			}
			if ( id3v1 != null ) {
				String title = id3v1.getTitle();
				String artist = id3v1.getArtist();
				String album = id3v1.getAlbum();
				return Optional.of( new TrackData( path, title, artist, album ) );
			}
			return Optional.empty();

	} catch ( IOException | UnsupportedTagException | InvalidDataException e ) {
			e.printStackTrace();
			return Optional.empty();
			}
	}
}
