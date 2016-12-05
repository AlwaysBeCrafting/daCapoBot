package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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
	private boolean shuffle = false;
	private List<File> tracks;

	public void setShuffle( boolean shuffle ) {
		this.shuffle = shuffle;
	}


	public Playlist( String path ) {
		dir = new File( path );

		tracks = Arrays.asList( dir.listFiles() );
		System.out.println( tracks.toString() );

		sort();

		for ( int i = 0; i < tracks.size(); i++ ) {  //print current tracks
			System.out.format( "%d %s\n", i + 1, tracks.get( i ) );
		}

	}

	public void shuffle() {
		//shuffle playlist
	}

	public void sort() {
		tracks.sort( File::compareTo );

	}
}

//------------------------------------------------------------------------------






















