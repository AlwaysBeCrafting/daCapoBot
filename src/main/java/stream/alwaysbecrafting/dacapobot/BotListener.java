package stream.alwaysbecrafting.dacapobot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//==============================================================================
class BotListener extends ListenerAdapter {
	//--------------------------------------------------------------------------
	private final Player PLAYER = new Player();
	private static final Pattern pattern = Pattern.compile( "^(\\S+)\\s*(.+)?" );

	@Override
	public void onMessage( MessageEvent event ) throws Exception {
		if ( event.getUser().getNick() != null ) {
			Database.DB_INSTANCE.logChat( event.getUser().getNick(), event.getMessage() );
		}

		String nick = event.getUser().getNick();
		String eventMessage = event.getMessage().toLowerCase();

		Matcher matcher = pattern.matcher( eventMessage );

		while ( matcher.find() ) {

			switch ( matcher.group( 1 ) ) {

				case "!veto":
					if ( nick != null && matcher.group( 2 ) != null ) {
						String veto = PLAYER.veto( nick, matcher.group( 2 ) );
						if ( veto.startsWith( "Private:" ) ) {
							event.respondWith( veto.replaceAll( "Private:\\s*", "" ) );
						}
						if ( veto.startsWith( "Public:" ) ) {
							event.respondWith( veto.replaceAll( "Public:\\s+", "" ) );
						}
					}
					break;

				case "!request":
					if ( nick != null && matcher.group( 2 ) != null ) {
						String request = PLAYER.request( nick, matcher.group( 2 ) );
						if ( request.startsWith( "Private:" ) ) {
							event.respondWith( request.replaceAll( "Private:\\s*", "" ) );
						}
						if ( request.startsWith( "Public:" ) ) {
							event.respondWith( request.replaceAll( "Public:\\s*", "" ) );
						}
					}
					break;

				case "!whoru":
					event.respondWith( "I'm your friendly music bot. For a list of commands type !help. " +
							"For my source code visit https://github.com/AlwaysBeCrafting/dacapobot" );
					break;

				case "!test":
					event.respondWith( "Slippy's not such a screw up after all" );
					break;

				case "!suggestions":
					event.respondWith( "Feature requests are welcome, please submit to https://github.com/AlwaysBeCrafting/dacapobot/issues" );
					break;

				case "!help":
					event.respondWith( "!whoru, !help, !veto, !request, !suggestions" );
					break;

				default:
					break;
			}
		}

	}

	@Override
	public void onConnect( ConnectEvent event ) {
		try {
			Database.DB_INSTANCE.addMP3s( new Config().getMusicDir() );

			this.PLAYER.setQueue();
			this.PLAYER.play();
		} catch ( Exception e ) {e.printStackTrace();}
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
