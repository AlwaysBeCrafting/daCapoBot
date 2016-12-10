package stream.alwaysbecrafting.daCapoBot;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//==============================================================================
public class Playlist {
	//--------------------------------------------------------------------------

//todo ability to request songs
//todo  play tracks from position
//todo 	eventually set it to use a sqllite db for upvote/downvote, skip count ...
	private File dir;
	private List<Track> tracks;

	public Playlist( String path ) {
		dir = new File( path );

		tracks =  Arrays.stream(dir.listFiles())
				.filter( track -> track.getName().endsWith( ".mp3" ) )
				.map( Track::new )
				.collect( Collectors.toList()
				);

	}

	//--------------------------------------------------------------------------

	public List<Track> getTrackList(){
		return this.tracks;
	}

	//--------------------------------------------------------------------------
	public void shuffle() {
		Collections.shuffle(tracks);

	}

	//--------------------------------------------------------------------------

	public void sort() {	}

	public Track getTrack(int index){
		return tracks.get(index);
	}

	//--------------------------------------------------------------------------

	public Track nextInList(Track currentTrack){
		return tracks.get( (tracks.indexOf( currentTrack ) + 1) % tracks.size() );
	}

	//--------------------------------------------------------------------------

	public Stream<Track> stream(){
		return tracks.stream();
	}

	//--------------------------------------------------------------------------

	public Stream<Track> parallelStream(){
		return tracks.parallelStream();
	}

	//--------------------------------------------------------------------------

}

//------------------------------------------------------------------------------






















