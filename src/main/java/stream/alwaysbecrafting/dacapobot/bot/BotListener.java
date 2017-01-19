package stream.alwaysbecrafting.dacapobot.bot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import stream.alwaysbecrafting.dacapobot.Config;
import stream.alwaysbecrafting.dacapobot.TrackData.TrackMetadata;
import stream.alwaysbecrafting.dacapobot.database.Database;
import stream.alwaysbecrafting.dacapobot.player.Player;

public class BotListener extends ListenerAdapter {
	private final Random RANDOM = new Random();
	private static final Pattern pattern = Pattern.compile( "^(\\S+)(\\s+(.*))?" );
	private Config config;
	private Database database;
	private Player player;

	public BotListener( Config config, Database database, Player player ) {
		this.config = config;
		this.database = database;
		this.player = player;
	}

	@Override
	public void onDisconnect( DisconnectEvent event ) throws Exception{
		event.getBot().close();
	}

	@Override
	public void onMessage( MessageEvent event ) throws Exception {
		if ( event.getUser().getNick() != null ) {
			database.logChat( event.getUser().getNick(), event.getMessage() );
		}

		String nick = event.getUser().getNick();
		String eventMessage = event.getMessage().toLowerCase();
		Matcher matcher = pattern.matcher( eventMessage );
		if ( matcher.find() ) {
			List<String> matcherArgs = Collections.emptyList();
			if (matcher.group(3) != null) {
				matcherArgs = Arrays.stream( matcher.group( 3 ).split( "\\s+" ) )
						.filter( s -> s.length() > 2 )
						.collect( Collectors.toList() );
			}
			switch ( matcher.group( 1 ) ) {
				case "!shutdown":
					Optional isAdmin = config.getAdmins().stream().filter( s -> s.equals( nick ) ).findAny();
					if ( isAdmin.isPresent() ) {
						event.respondWith( "Clumsy Robot stopped moving!" );
						player.exit();
						database.close();
						event.getBot().send().quitServer();
					} else {
						event.respondWith( "I'm sorry " + nick + ", I'm afraid I can't do that." );
					}
					break;

				case "!veto":
					if ( nick != null && matcher.group( 3 ) != null ) {
						if ( matcherArgs.isEmpty() ){
							event.respondWith( "Sorry, " + matcher.group(3) + " is too vague. See '!help !veto'" +
									" for more info." );
						}
						else if (matcherArgs.get(0).toLowerCase().matches( "[remix]{1,5}" )){
							event.respondWith( "Illegal first argument " + matcherArgs.get(0) + " too similar to 'remix'" );
						}
						else if ( player.getCurrentTitle()
								.toLowerCase()
								.replaceAll( "[^a-z0-9]+", "%" )
								.contains( ( matcher.group(3).replaceAll( "[^a-z0-9]+", "%" ) ) ) ) {
							database.addVeto( nick, database.searchTracksByTitle( player.getCurrentTitle() ).get( 0 ));
							event.respondWith( player.getCurrentTitle() + " vetoed." );
							player.nextTrack();
						} else {
							List<TrackMetadata> trackList = database.searchTracksByTitle( matcherArgs.toString() );

							if ( trackList.isEmpty() ) {
								event.respondWith( "Sorry, I couldn't find any tracks containing " + matcher.group( 2 ) );
							}
							if ( trackList.size() == 1 ) {
								database.addVeto( nick, trackList.get( 0 ) );
								event.respondWith( trackList.get( 0 ).title + "vetoed." );
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
					if ( matcherArgs.isEmpty() ) {
						event.respondWith( "Sorry, " + matcher.group( 3 ) + " is too vague. See '!help !request'" +
								" for more info." );
						break;
					}
					else if (matcherArgs.get(0).toLowerCase().matches( "[remix]{1,5}" )){
						event.respondWith( "Illegal first argument " + matcherArgs.get(0) + " too similar to 'remix'" );
						break;
					}
					else if (matcherArgs.get(0).toLowerCase().matches( "--random" ) && matcherArgs.get(1).isEmpty()){
						event.respondWith( "--random requires a partial title." );
						break;
					}
					else if (matcherArgs.get(0).toLowerCase().matches( "--random" ) && !matcherArgs.get(1).isEmpty()){
						String randomRequest = "";
						for ( int i = 1; i < matcherArgs.size(); i++ ) {
							if("".equals( randomRequest )){
								randomRequest = matcherArgs.get(i);
							}else {
								randomRequest = randomRequest + " " + matcherArgs.get( i );
							}
						}

						List<TrackMetadata> matchingTracks = database.searchTracksByTitle( randomRequest );
						if ( matchingTracks.isEmpty() ) {
							event.respondWith( "Sorry, I couldn't find any tracks containing " + matcher.group(3).replaceAll( "--random ", "" ));
						}
						else if ( matchingTracks.size() == 1 ){
							database.addRequest( nick, matchingTracks.get( 0 ) );
							event.respondWith( matchingTracks.get( 0 ).title + " added to the queue." );
						}
						else if ( matchingTracks.size() > 1 ) {
							int randomTrack = RANDOM.nextInt(matchingTracks.size());
							database.addRequest( nick, matchingTracks.get(randomTrack) );
							event.respondWith( matchingTracks.get(randomTrack).title + " added to the queue.");
						}
					}
					else if ( nick != null && matcher.group( 3 ) != null ) {
						TrackMetadata lastInRequest = database.getFinalFromRequests();
						List<TrackMetadata> matchingTracks = database.searchTracksByTitle( matcher.group( 3 ).toString() );

						if ( matchingTracks.isEmpty() ) {
							event.respondWith( "Sorry, I couldn't find any tracks containing " + matcher.group(3));
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
						if ( matchingTracks.size() == 1 && !matchingTracks.get( 0 ).title.equalsIgnoreCase( lastInRequest.title )){
							database.addRequest( nick, matchingTracks.get( 0 ) );
							event.respondWith( matchingTracks.get( 0 ).title + " added to the queue." );
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
					if (matcherArgs.isEmpty()) {
						event.respondWith( "!help [Option] ... Options: !whoru, !help, !veto, !request, !suggestions" );
						break;
					}
					if (matcherArgs.get(0).equals( "!help" ) || matcherArgs.get(0).equals( "help" )){
						event.respondWith( "Need more help? Have a kitty. https://goo.gl/nRVwDf" );
						break;
					}
					if (matcherArgs.get(0).equals( "!whoru") || matcherArgs.get(0).equals( "whoru")){
						event.respondWith( "Error: Too many arguments." );
						break;
					}
					if (matcherArgs.get(0).equals( "!veto" ) || matcherArgs.get(0).equals( "veto" )){
						event.respondWith( "Usage: !veto {title}" );
						break;
					}
					if (matcherArgs.get(0).equals( "!request" ) || matcherArgs.get(0).equals( "request" )){
						event.respondWith( "Usage: !request {title} or !request --random {title}" );
						break;
					}
					if (matcherArgs.get(0).equals( "!suggestions" ) || matcherArgs.get(0).equals( "suggestions" )){
						event.respondWith( "Don't know how to submit a !suggestion? Here's more info than " +
								"you probably wanted: https://guides.github.com/features/issues/" );
						break;
					}
					else{
						event.respondWith( "!help [Option] ... Options: !whoru, !help, !veto, !request, !suggestions" );
						break;
					}
				default:
					break;
			}
		}
	}
}
