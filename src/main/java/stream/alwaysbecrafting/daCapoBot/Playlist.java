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
	private File dir;
	private List<Track> tracks;

	public Playlist( String path ) {
		try{
		dir = new File( path );

			tracks = Arrays.stream( dir.listFiles() )
					.filter( track -> track.getName().endsWith( ".mp3" ) )
					.map( Track::new )
					.collect( Collectors.toList()
					);
		}
		catch(Exception e){
			e.printStackTrace();
		}

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

	public void sort() {
		tracks = tracks
				.parallelStream()
				.sorted( (t1, t2) -> t1.title.compareToIgnoreCase( t2.title ) )
				.collect( Collectors.toList());
		for ( Track item:tracks
		       ) {
			System.out.println("Sorted: " + item.title);

		}

	}

	public Track getTrack(int index){
		return tracks.get(index);
	}

	//--------------------------------------------------------------------------

	public Track nextInList(Track currentTrack) {

		tracks.remove( 0 );

		if(tracks.isEmpty()){
			tracks.addAll(Database.DB_INSTANCE.getAfter(currentTrack,10));
		}
		System.out.println(tracks.size());
			return 	tracks.get( 0 );
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

	public int size(){
		return tracks.size();
	}

	public void clear(){
		this.tracks.clear();
	}

	public void add( Track first ) {
		tracks.add( first );
	}
}

//------------------------------------------------------------------------------






















