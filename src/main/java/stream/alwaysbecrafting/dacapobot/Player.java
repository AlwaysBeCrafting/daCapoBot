package stream.alwaysbecrafting.dacapobot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import static stream.alwaysbecrafting.dacapobot.Database.DB_INSTANCE;

class Player {
	private MediaPlayer player;
	private long timestamp = System.currentTimeMillis();
	private Track currentTrack;
	private Media media;

	Player() {
		JFXPanel jfxPanel = new JFXPanel();
	}

	void setQueue() {
		Track nextTrack = null;
		while(nextTrack == null || !nextTrack.exists()) {
			nextTrack = DB_INSTANCE.getRandomTrack();
			this.currentTrack = nextTrack;
		}
		storeTrackTitle( currentTrack.title );
	}

	void play() {
		currentTrack.fetchTrackData();
		media = new Media( currentTrack.toURIString() );
		player = new MediaPlayer( media );
		player.setOnEndOfMedia( () -> nextTrack() );
		player.play();
		System.out.println( "Now Playing: " + currentTrack.title );
	}

	private void storeTrackTitle( String s ) {
		try {
			PrintWriter writer = new PrintWriter( Config.CONFIG.props.getProperty( "live_track_file" ), "UTF-8" );
			writer.println( s );
			writer.close();
		} catch ( FileNotFoundException | UnsupportedEncodingException e ) {
			e.printStackTrace();
		}
	}

	void nextTrack() {
		stop();
		storeTrackTitle( "" );
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
			if( nextTrack == null || !nextTrack.exists() ) {
				try {
					Thread.sleep( 200 );
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}
		}
		this.currentTrack = nextTrack;
		storeTrackTitle( currentTrack.title );
		play();
	}

	public String getCurrentTitle() {
		return currentTrack.title;
	}

	public void stop() {
		if ( player != null ) {
			player.stop();
		}
	}
}
