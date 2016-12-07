package stream.alwaysbecrafting.daCapoBot;

import java.io.IOException;
import java.io.PrintWriter;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

//==============================================================================
public class Player{
	//--------------------------------------------------------------------------
	//private AdvancedPlayer player;
	private MediaPlayer player;
	private Thread playerThread;
	private boolean playerRunning = false;
	private Track currentTrack;
	private Playlist currentPlaylist;

	//--------------------------------------------------------------------------
	public void setPlaylist(Playlist currentPlaylist){
		this.currentPlaylist = currentPlaylist;
		currentTrack = currentPlaylist.getTrack( 0 );
	}

	//--------------------------------------------------------------------------


	public void play() {
		try {
			if( playerRunning ){
				player.stop();
			}
			if( !playerRunning ){
				JFXPanel jfxPanel = new JFXPanel();
			}

			player = new MediaPlayer( new Media( currentTrack.toURIString()));

			playerThread = new Thread(
					() -> {
							try {

								System.out.println(currentTrack.toString());

								PrintWriter writer = new PrintWriter( "/home/mh/Current_Track", "UTF-8" );
								writer.println( currentTrack.toString() );
								writer.close();
								this.player.play();
								player.setOnEndOfMedia( new Runnable() {
									@Override public void run() {
										nextTrack();
									}
								} );
							} catch ( IOException e){
								e.printStackTrace();
							}
					}
					,"AudioPlayerThread" );

			playerThread.start();
			playerRunning = true;


		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}

	//--------------------------------------------------------------------------

	public void nextTrack() {
		currentTrack = currentPlaylist.nextInList( currentTrack );
		play();
	}
	//--------------------------------------------------------------------------

}
//------------------------------------------------------------------------------
