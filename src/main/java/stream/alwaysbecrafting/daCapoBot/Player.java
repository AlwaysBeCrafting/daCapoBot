package stream.alwaysbecrafting.daCapoBot;

import java.io.FileInputStream;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackListener;

import static javazoom.jl.player.FactoryRegistry.systemRegistry;

//==============================================================================
public class Player extends PlaybackListener{
	//--------------------------------------------------------------------------
	private AdvancedPlayer player;
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
				player.close();
			}
			player = new AdvancedPlayer(
					new FileInputStream( currentTrack.file ), systemRegistry().createAudioDevice() );

			playerThread = new Thread(
					() -> {
							try {
								this.player.play();
								nextTrack();
							} catch ( JavaLayerException ex ) {
								ex.printStackTrace();
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
