package stream.alwaysbecrafting.daCapoBot;

import java.io.FileInputStream;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackListener;

import static javazoom.jl.player.FactoryRegistry.systemRegistry;

//==============================================================================
public class Player extends PlaybackListener{
	//--------------------------------------------------------------------------
	private AdvancedPlayer player;
	private Thread playerThread;
	private boolean playerRunning = false;

	public void play(Track track) {
		try {
			if( playerRunning ){
				player.close();
			}
			player = new AdvancedPlayer(
					new FileInputStream( track.file ), systemRegistry().createAudioDevice() );

			playerThread = new Thread(
					() -> {
							try {
								this.player.play();
							} catch ( javazoom.jl.decoder.JavaLayerException ex ) {
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

	public void next() {

	}


	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
