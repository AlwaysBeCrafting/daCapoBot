package stream.alwaysbecrafting.daCapoBot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

//==============================================================================
public class Player {
	//--------------------------------------------------------------------------
	//private AdvancedPlayer player;
	private MediaPlayer player;
	private boolean playerRunning = false;
	private Track currentTrack;
	private Playlist currentPlaylist;

	//--------------------------------------------------------------------------
	public void setPlaylist( Playlist currentPlaylist ) {
		this.currentPlaylist = currentPlaylist;
		currentTrack = currentPlaylist.getTrack( 0 );
	}

	//--------------------------------------------------------------------------


	public void play() {
		if ( playerRunning ) {
			player.stop();
		}
		if ( !playerRunning ) {
			JFXPanel jfxPanel = new JFXPanel();
		}
		player = new MediaPlayer( new Media( currentTrack.toURIString() ) );
		System.out.println( "Now Playing: " + currentTrack.toString() );

		try {
			PrintWriter writer = new PrintWriter( "/home/mh/Current_Track", "UTF-8" );
			writer.println( currentTrack.toString() );
			writer.close();
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException f){
			f.printStackTrace();
		}

		this.player.play();
		player.setOnEndOfMedia(this::nextTrack);
		playerRunning = true;
}

	//--------------------------------------------------------------------------

	public void nextTrack() {
		currentTrack = currentPlaylist.nextInList( currentTrack );
		play();
	}
	//--------------------------------------------------------------------------

}
//------------------------------------------------------------------------------
