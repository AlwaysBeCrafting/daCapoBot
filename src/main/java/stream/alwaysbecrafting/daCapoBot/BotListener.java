package stream.alwaysbecrafting.daCapoBot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;

//==============================================================================
public class BotListener extends ListenerAdapter{
	//--------------------------------------------------------------------------
	Player p1;
	Playlist sideA;
	Database db;

	@Override
	public void onMessage(MessageEvent event) throws Exception{
		if(event.getUser().getRealName() != null) {
			db.logChat( event.getUser().getRealName(), event.getMessage() );
		}
		if (event.getMessage().startsWith( "!veto" ))
			p1.nextTrack();
		if (event.getMessage().toLowerCase().startsWith( "!request" ))
			p1.request(event.getMessage().toLowerCase().toLowerCase());
		if (event.getMessage().startsWith( "!test" ))
			event.respondWith( "Slippy's not such a screw up after all" );

	}

	@Override
	public void onConnect( ConnectEvent event ){
		try {
			this.p1 = new Player();
			sideA = new Config().getPlaylist();
			p1.setPlaylist( sideA );
			p1.play();


			db = new Database();
			db.insertToTracks(sideA);

		}
		catch(Exception e){e.printStackTrace();}
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
