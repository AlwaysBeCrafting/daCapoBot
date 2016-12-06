package stream.alwaysbecrafting.daCapoBot;


//==============================================================================
public class Main {
	//--------------------------------------------------------------------------
	public static void main( String[] args ) {

		Playlist sideA = new Playlist( "/home/mh/Music/OC ReMix - 1 to 3000 [v20141015]" );
		sideA.shuffle();
		Player p1 = new Player();
		p1.setPlaylist( sideA );
		p1.play();

		try{
			Thread.sleep( 10000 );
		}
		catch(Exception e){

		}
		sideA.shuffle();
		p1.play();

	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
