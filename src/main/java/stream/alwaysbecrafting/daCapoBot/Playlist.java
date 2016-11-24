package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static java.lang.String.format;

//==============================================================================
public class Playlist {
	//--------------------------------------------------------------------------
//todo 	public readable map? no just an array, output array list with position + 1,
//todo 	call section with position - 1
//todo  get position of playlist, output position + 1 and filename
//todo  play playlist from position
//todo 	eventually set it to use a sqllite db for upvote/downvote, skip count ...
	private File f;
	private boolean shuffle = false;
	private ArrayList<String> playlist;

	public void setShuffle( boolean shuffle ) {
		this.shuffle = shuffle;
	}


	public Playlist( String path ) {
		this.f = new File( path );
		this.playlist = new ArrayList<String>( Arrays.asList( f.list( )  ));
		Collections.sort(playlist, String.CASE_INSENSITIVE_ORDER);
		for ( Iterator<String> it = playlist.iterator(); it.hasNext();) {
			if (!it.next().contains(".mp3"))
				it.remove(); // NOTE: Iterator's remove method, not ArrayList's, is used.
		}
//		for ( File i:f.listFiles()
//		       ) {
//			System.out.println(i);
//
//		}


		for ( int i = 0; i < playlist.size(); i++ ) {
			System.out.format( "%d %s\n", i + 1, playlist.get( i ) );
		}
////		for ( String index : playlist
//				) {
//			System.out.println( (playlist.indexOf( index ) + 1) + " " + index );
//
//		}
		if ( !shuffle ) {
			new Player( format( "%s/%s", f.toURI(), playlist.get( 0 ) )).play();
		}
	}

	public void Play() {
		if ( shuffle == false ) {

		}
		//--------------------------------------------------------------------------
	}
}
//------------------------------------------------------------------------------






















