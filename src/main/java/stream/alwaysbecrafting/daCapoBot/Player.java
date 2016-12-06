package stream.alwaysbecrafting.daCapoBot;

import java.io.FileInputStream;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackListener;

import static javazoom.jl.player.FactoryRegistry.systemRegistry;

//==============================================================================
public class Player extends PlaybackListener implements Runnable{
	//--------------------------------------------------------------------------
	private AdvancedPlayer player;
	private Thread playerThread;


	public void play(Track track) {
		try {
			player = new AdvancedPlayer(
					new FileInputStream( track.file ),
					systemRegistry().createAudioDevice() );
			playerThread = new Thread( this, "AudioPlayerThread" );

			playerThread.start();

		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}

	public void run(){
		try {
			this.player.play();
		}
		catch ( javazoom.jl.decoder.JavaLayerException ex)
		{
			ex.printStackTrace();
		}
	}

	public void next() {

	}


	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
