package stream.alwaysbecrafting.dacapobot.player;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import stream.alwaysbecrafting.dacapobot.Config;
import stream.alwaysbecrafting.dacapobot.TrackData.TrackMetadata;
import stream.alwaysbecrafting.dacapobot.database.Database;

public class JfxPlayer implements Player{
	private Config config;
	private Database database;
	private MediaPlayer player;
	private long timestamp = System.currentTimeMillis();
	private TrackMetadata currentTrack;
	private Media media;

	public JfxPlayer( Config config, Database database ) {
		JFXPanel jfxPanel = new JFXPanel();
		this.config = config;
		this.database = database;
	}

	public void setQueue() {
		TrackMetadata nextTrack = null;
		while(nextTrack == null || !Files.exists(nextTrack.path)) {
			nextTrack = database.getRandomTrack();
			this.currentTrack = nextTrack;
		}
		storeTrackTitle( currentTrack.title );
	}

	public void play() {
		media = new Media( currentTrack.path.toUri().toString() );
		player = new MediaPlayer( media );
		player.setOnEndOfMedia( () -> nextTrack() );
		player.play();
		System.out.println( "Now Playing: " + currentTrack.title );
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
		stop( () -> {
			storeTrackTitle( "" );
			TrackMetadata nextTrack = null;
			while(nextTrack == null || !Files.exists( nextTrack.path )) {
				TrackMetadata requestedTrack = database.getNextRequested( timestamp );
				if ( requestedTrack != null ) {
					timestamp = requestedTrack.timestamp;
					nextTrack = requestedTrack;
				} else {
					timestamp = System.currentTimeMillis();
					nextTrack = database.getRandomTrack();
				}
				if( nextTrack == null || !Files.exists( nextTrack.path ) ) {
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
		});
	}

	public String getCurrentTitle() {
		return currentTrack.title;
	}

	public void stop( Runnable runnable ) {
		player.setOnStopped( runnable );
		player.stop();
	}

	public void exit() {
		stop( () -> {
			storeTrackTitle( "" );
			Platform.exit();
		});
	}
}
