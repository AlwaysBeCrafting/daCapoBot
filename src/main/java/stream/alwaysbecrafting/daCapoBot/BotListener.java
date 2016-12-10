package stream.alwaysbecrafting.daCapoBot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

//==============================================================================
public class BotListener extends ListenerAdapter{
	//--------------------------------------------------------------------------
	Player p1;
	Playlist sideA;
	Database db;

	@Override
	public void onGenericMessage(GenericMessageEvent event) {
		//When someone says ?helloworld respond with "Hello World"
		if (event.getMessage().startsWith( "!next" ))
			p1.nextTrack();
		if (event.getMessage().startsWith( "!test" ))
			event.respondWith( "this was a test" );

	}

	@Override
	public void onConnect( ConnectEvent event ){
		try {
			this.p1 = new Player();
			sideA = new Config().getPlaylist();
			p1.setPlaylist( sideA );
			p1.play();

			sideA.populatePlaylistMetadata();

			db = new Database();
			db.insertToTracks(sideA);

		}
		catch(Exception e){e.printStackTrace();}
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
