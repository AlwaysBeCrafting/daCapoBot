package stream.alwaysbecrafting.daCapoBot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import static stream.alwaysbecrafting.daCapoBot.Database.DB_INSTANCE;

//==============================================================================
class Player {
	//--------------------------------------------------------------------------
	private MediaPlayer player;
	private boolean playerRunning = false;
	private long timestamp = System.currentTimeMillis();
	private Track currentTrack;

	Player(){}

	void setQueue() {
		this.currentTrack = DB_INSTANCE.getFirst();

		initializePlayer();

	}

	void initializePlayer(){
		if ( playerRunning ) {
			player.stop();
		}
		if ( !playerRunning ) {
			JFXPanel jfxPanel = new JFXPanel();
		}

		player = new MediaPlayer( new Media( currentTrack.toURIString() ) );
		playerRunning = true;

	}

	void play() {
		initializePlayer();

		if(currentTrack.fetchTrackData()){
			System.out.println( "Now Playing: " + currentTrack.title );
		}

		try {
			PrintWriter writer = new PrintWriter( Config.CONFIG.props.getProperty( "live_track_file" ), "UTF-8" );
			writer.println( currentTrack.title );
			writer.close();
		}
		catch (FileNotFoundException | UnsupportedEncodingException e){
			e.printStackTrace();
		}

		this.player.play();
		player.setOnEndOfMedia(this::nextTrack);
}

	void nextTrack() {
		Track requestedTrack = Database.DB_INSTANCE.getNextRequested( timestamp );
		if ( requestedTrack != null ) {
			timestamp = requestedTrack.timestamp;
			this.currentTrack = requestedTrack;
		} else {
			timestamp = System.currentTimeMillis();
			this.currentTrack = Database.DB_INSTANCE.getAfter( currentTrack, 1 ).get( 0 );
		}
		play();
	}


	boolean veto( String user, String short_name ) {
		if(!DB_INSTANCE.addToVeto(user, short_name.replaceAll( "!veto\\s+", "" ))){

			return false;
		}
		if( currentTrack.title
				.toLowerCase()
				.replaceAll( "[^a-z0-9]+", "-" )
				.contains( (short_name.replace( "!veto ", "" )) )){

			nextTrack();
		}
		return true;
	}

	String request( String user, String short_name ) {
		return DB_INSTANCE.addRequest(currentTrack, user, short_name.replaceAll( "!request\\s+", "" ));
	}
}
//------------------------------------------------------------------------------
