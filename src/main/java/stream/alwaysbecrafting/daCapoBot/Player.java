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
	private MediaPlayer player;
	private boolean playerRunning = false;
	private Track currentTrack;
	private Playlist currentPlaylist;

	//--------------------------------------------------------------------------
	public void setPlaylist( Playlist currentPlaylist ) {
		this.currentPlaylist = currentPlaylist;
		currentTrack = currentPlaylist.getTrack( 0 );
		initializePlayer();

	}


	//--------------------------------------------------------------------------

	public void initializePlayer(){
		if ( playerRunning ) {
			player.stop();
		}
		if ( !playerRunning ) {
			JFXPanel jfxPanel = new JFXPanel();
		}

		player = new MediaPlayer( new Media( currentTrack.toURIString() ) );
		playerRunning = true;

	}

	public void play() {
		initializePlayer();

		if(currentTrack.fetchTrackData()){
			System.out.println("Title: " + currentTrack.title);
		}

		System.out.println( "Now Playing: " + currentTrack.title );
		try {
			PrintWriter writer = new PrintWriter( "/home/mh/Current_Track", "UTF-8" );
			writer.println( currentTrack.title );
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
}

	//--------------------------------------------------------------------------

	public void nextTrack() {
		currentTrack = currentPlaylist.nextInList( currentTrack );
		play();
	}
	//--------------------------------------------------------------------------

}
//------------------------------------------------------------------------------
