package stream.alwaysbecrafting.dacapobot.player;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import stream.alwaysbecrafting.dacapobot.Config;
import stream.alwaysbecrafting.dacapobot.TrackData;
import stream.alwaysbecrafting.dacapobot.database.Database;

public class JfxPlayer implements Player{
	private Config config;
	private Database database;
	private MediaPlayer player;
	private long timestamp = System.currentTimeMillis();
	private TrackData currentTrack;
	private Media media;

	public JfxPlayer( Config config, Database database ) {
		JFXPanel jfxPanel = new JFXPanel();
		this.config = config;
		this.database = database;
	}

	public void setQueue() {
		TrackData nextTrack = null;
		while(nextTrack == null || !nextTrack.exists()) {
			nextTrack = database.getRandomTrack();
			this.currentTrack = nextTrack;
		}
		storeTrackTitle( currentTrack.getTitle() );
	}

	public void play() {
		media = new Media( currentTrack.toURIString() );
		player = new MediaPlayer( media );
		player.setOnEndOfMedia( () -> nextTrack() );
		player.play();
		System.out.println( "Now Playing: " + currentTrack.getTitle() );
	}

	private void storeTrackTitle( String s ) {
		try {
			PrintWriter writer = new PrintWriter( config.getTrackFile(), "UTF-8" );
			writer.println( s );
			writer.close();
		} catch ( FileNotFoundException | UnsupportedEncodingException e ) {
			e.printStackTrace();
		}
	}

	public void nextTrack() {
		stop();
		storeTrackTitle( "" );
		TrackData nextTrack = null;
		while(nextTrack == null || !nextTrack.exists()) {
			TrackData requestedTrack = database.getNextRequested( timestamp );
			if ( requestedTrack != null ) {
				timestamp = requestedTrack.getTimestamp();
				nextTrack = requestedTrack;
			} else {
				timestamp = System.currentTimeMillis();
				nextTrack = database.getRandomTrack();
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
		storeTrackTitle( currentTrack.getTitle() );
		play();
	}

	public String getCurrentTitle() {
		return currentTrack.getTitle();
	}

	public void stop() {
		player.stop();
	}

	public void exit() {
		stop();
		storeTrackTitle( "" );
		Platform.exit();
	}
}
