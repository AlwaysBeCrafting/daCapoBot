package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

//==============================================================================
public class Playlist {
	//--------------------------------------------------------------------------
//todo new playlist
//todo 	set directory location? read from config file?
//todo 	read list of files from directory into array
//todo 	public readable map? no just an array, output array list with position + 1,
//todo 	call section with position - 1

//todo 	eventually set it to use a sqllite db for upvote/downvote, skip count ...
	private File              f;
	private boolean           shuffle  = false;
	private ArrayList<String> playlist;

	public void setShuffle( boolean shuffle ) {
		this.shuffle = shuffle;
	}


	public void Playlist( String path ) {
		this.f = new File(path);
		this.playlist = new ArrayList<String>( Arrays.asList(f.list()) );
		System.out.println(playlist.toString());
	}

	public void Play() {
		if ( shuffle == false ){
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------






















