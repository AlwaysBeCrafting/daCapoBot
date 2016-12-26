package stream.alwaysbecrafting.dacapobot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import static stream.alwaysbecrafting.dacapobot.Database.DB_INSTANCE;

//==============================================================================
class Player {
	//--------------------------------------------------------------------------
	private MediaPlayer player;
	private long timestamp = System.currentTimeMillis();
	private Track currentTrack;

	Player() {}

	void setQueue() {
		JFXPanel jfxPanel = new JFXPanel();

		Track nextTrack = null;
		while(nextTrack == null || !nextTrack.exists()) {
			nextTrack = DB_INSTANCE.getRandomTrack();
			this.currentTrack = nextTrack;
		}

		System.out.println( "queue " + this.currentTrack );
	}

	void play() {

		if ( player != null && player.getStatus() == MediaPlayer.Status.PLAYING ) {
			player.stop();
		}
		player = new MediaPlayer( new Media( currentTrack.toURIString() ) );

		if ( currentTrack.fetchTrackData() ) {
			System.out.println( "Now Playing: " + currentTrack.title );
		}

		try {
			PrintWriter writer = new PrintWriter( Config.CONFIG.props.getProperty( "live_track_file" ), "UTF-8" );
			writer.println( currentTrack.title );
			writer.close();
		} catch ( FileNotFoundException | UnsupportedEncodingException e ) {
			e.printStackTrace();
		}

		this.player.play();
		player.setOnEndOfMedia( this::nextTrack );
	}

	void nextTrack() {
		Track nextTrack = null;
		while(nextTrack == null || !nextTrack.exists()) {

			Track requestedTrack = Database.DB_INSTANCE.getNextRequested( timestamp );
			if ( requestedTrack != null ) {
				timestamp = requestedTrack.timestamp;
				nextTrack = requestedTrack;
			} else {
				timestamp = System.currentTimeMillis();
				nextTrack = Database.DB_INSTANCE.getRandomTrack();
			}
		}
		this.currentTrack = nextTrack;
		play();
	}


	String request( String user, String request ) {
		return DB_INSTANCE.addRequest( user, ( request) );
	}

	public String getCurrentTitle() {
		return currentTrack.title;
	}
}
//------------------------------------------------------------------------------
