package stream.alwaysbecrafting.dacapobot;

import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//==============================================================================
class BotListener extends ListenerAdapter{
	//--------------------------------------------------------------------------
	private final Player PLAYER = new Player();

	@Override
	public void onMessage(MessageEvent event) throws Exception{
		if(event.getUser().getRealName() != null) {
			Database.DB_INSTANCE.logChat( event.getUser().getRealName(), event.getMessage() );
		}

		User getUser = event.getUser();
		String eventMessage = event.getMessage().toLowerCase();

		Pattern requestRegex = Pattern.compile("!request\\s*(\\S+)");
		Matcher requestMatcher = requestRegex.matcher( eventMessage );

		Pattern vetoRegex = Pattern.compile("!veto\\s*(\\S+)");
		Matcher vetoMatcher = vetoRegex.matcher( eventMessage );

		if (event.getMessage().toLowerCase().startsWith( "!help" ))
			event.respondWith( "!whoru, !help, !veto, !request, !suggestions"  );

		while ( vetoMatcher.find() ) {
			System.out.println( "veto group 0: " + vetoMatcher.group( 1 ) );
			if ( getUser != null ) {
				if ( !PLAYER.veto( getUser.toString()
						, vetoMatcher.group( 1 ) ) ) {

					event.respondWith( "Sorry, I can't find "
							+ vetoMatcher.group(1)
							+ ". Could you be specific?" );
				}
			}
		}

		while ( requestMatcher.find() ) {
			System.out.println("request group: " + requestMatcher.group(1));
			if ( getUser != null ) {
				event.respondWith( PLAYER.request( getUser.toString()
						, requestMatcher.group( 1 ) ) );

			}
		}
		if (event.getMessage().toLowerCase().startsWith( "!whoru" ))
			event.respondWith( "I'm your friendly music bot. For a list of commands type !help. " +
					"for my source code visit https://github.com/AlwaysBeCrafting/dacapobot" );

		if (event.getMessage().toLowerCase().startsWith( "!test" ))
			event.respondWith( "Slippy's not such a screw up after all" );

		if (event.getMessage().toLowerCase().startsWith( "!suggestions" ))
			event.respondWith( "Feature requests are welcome, please submit to https://github.com/AlwaysBeCrafting/dacapobot/issues" );

	}

	@Override
	public void onConnect( ConnectEvent event ){
		try {
			Database.DB_INSTANCE.addMP3s(new Config().getMusicDir());

			this.PLAYER.setQueue();
			this.PLAYER.play();
		}
		catch(Exception e){e.printStackTrace();}
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
