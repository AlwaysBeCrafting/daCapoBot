package stream.alwaysbecrafting.daCapoBot;


//==============================================================================
public class Main {
	//--------------------------------------------------------------------------
	public static void main( String[] args ) {

		Playlist sideA = new Playlist( "/home/mh/Music/OC ReMix - 1 to 3000 [v20141015]" );
		Player p1 = new Player();
		sideA.shuffle();
		p1.play(sideA.getTrack(1));

	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
