package stream.alwaysbecrafting.daCapoBot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

//==============================================================================
class Player {
	//--------------------------------------------------------------------------
	private MediaPlayer player;
	private boolean playerRunning = false;
	private Track currentTrack;
	private List<Track> trackQueue = new ArrayList<>();

	Player(){}

	void setQueue() {
		this.trackQueue.add(Database.DB_INSTANCE.getFirst());
		this.currentTrack = this.trackQueue.get( 0 );

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

	void nextTrack() {
		currentTrack = nextInList( currentTrack );
		play();
	}

	Track nextInList(Track currentTrack) {

		trackQueue.remove( 0 );

		if(trackQueue.isEmpty()){
			trackQueue.addAll(Database.DB_INSTANCE.getAfter(currentTrack,10));
		}
		System.out.println(trackQueue.size());
		return 	trackQueue.get( 0 );
	}

	public boolean veto( String user, String short_name ) {
		short_name.replace( "!veto ", "" );
		if(!Database.DB_INSTANCE.addToVeto(user, short_name.replace( "!veto ", "" ))){
			return false;
		}
		if( Database.DB_INSTANCE.getShortName( currentTrack.title ).contains( short_name.replace( "!veto ", "" ) )){
			nextTrack();
		}
		if( Database.DB_INSTANCE.getShortName( currentTrack.title ).equalsIgnoreCase( "can't find short name")){
			System.out.println("can't find short name");
			return false;
		}
		return true;
	}

	public void request( String s ) {}
}
//------------------------------------------------------------------------------
