package stream.alwaysbecrafting.dacapobot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static stream.alwaysbecrafting.dacapobot.Database.DB_INSTANCE;

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

						if ( PLAYER.getCurrentTitle()
								.toLowerCase()
								.replaceAll( "[^a-z0-9]+", "%" )
								.contains( ( matcher.group( 2 ).replaceAll( "[^a-z0-9]+", "%" ) ) ) ) {
							DB_INSTANCE.addVeto( nick, PLAYER.getCurrentTitle() );
							event.respondWith( PLAYER.getCurrentTitle() + " vetoed, thank you." );
							PLAYER.nextTrack();
						} else {
							List<Track> trackList = DB_INSTANCE.addVeto( nick, ( matcher.group( 2 ) ) );

							if ( trackList.isEmpty() ) {
								event.respondWith( "Private: Sorry, I couldn't find any tracks containing " + matcher.group( 2 ) );
							}
							if ( trackList.size() > 1 ) {
								String response = "";
								for ( int i = 0; i < Math.min( trackList.size(), 3 ); i++ ) {
									if ( "".equals( response ) ) {
										response = response + trackList.get( i ).title;
									} else {
										response = response + " ❙ " + trackList.get( i ).title;
									}
								}
								if ( trackList.size() > 3 ) {
									response = response + " ❙ +" + ( trackList.size() - 3 ) + " more";
								}
								event.respondWith( response );
							}
						}
					}
					break;

				case "!request":
					if ( nick != null && matcher.group( 2 ) != null ) {
						Track lastInRequest = DB_INSTANCE.getFinalFromRequests();
						List<Track> matchingTracks = DB_INSTANCE.addRequest( nick, matcher.group(2).toString() );

						if ( matchingTracks.isEmpty() ) {
							event.respondWith( "Sorry, I couldn't find any tracks containing " + matcher.group(2));
						}
						if ( matchingTracks.size() == 1 ){
							event.respondWith( matchingTracks.get( 0 ).title + " added to the queue." );
						}
						if ( matchingTracks.size() > 1 ) {
							String response = "";
							for ( int i = 0; i < Math.min( matchingTracks.size(), 3 ); i++ ) {
								if ( "".equals( response ) ) {
									response = response + matchingTracks.get( i ).title;
								} else {
									response = response + " ❙ " + matchingTracks.get( i ).title;
								}
							}
							if ( matchingTracks.size() > 3 ) {
								response = response + " ❙ +" + ( matchingTracks.size() - 3 ) + " more";
							}
							event.respondWith( response );
						}
						if ( lastInRequest != null && matchingTracks.get( 0 ).title.equalsIgnoreCase( lastInRequest.title ) ) {
							event.respondWith( matchingTracks.get( 0 ).title + " is the last song in the request list. Please choose a different track.");
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
