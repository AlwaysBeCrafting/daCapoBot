package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//==============================================================================
public class Playlist {
	//--------------------------------------------------------------------------

//todo ability to request songs

	//todo 	public readable map? no just an array, output array tracks with position + 1,
//todo 	call section with position - 1
//todo  get position of tracks, output position + 1 and filename
//todo  play tracks from position
//todo 	eventually set it to use a sqllite db for upvote/downvote, skip count ...
	private File dir;
	private List<Track> tracks;

	public Playlist( String path ) {
		dir = new File( path );

		tracks =  Arrays.stream(dir.listFiles())
				.filter( track -> track.getName().endsWith( ".mp3" ) )
				.map( (f) -> new Track(f) )
				.collect( Collectors.toList()
				);


		sort();

		tracks.stream()
				.forEach( track -> System.out.format("%s\n", track ) );

	}

	public void shuffle() {
		Collections.shuffle(tracks);
	}

	public void sort() {
		//tracks.sort( File::compareTo );

	}
}

//------------------------------------------------------------------------------






















