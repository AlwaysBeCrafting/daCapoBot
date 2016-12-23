package stream.alwaysbecrafting.dacapobot;

import org.pircbotx.Configuration;
import org.pircbotx.cap.EnableCapHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

//==============================================================================
class Config {
	public static final Config CONFIG = new Config();
	public final Properties props;
	private final List<String> propertiesList = Arrays.asList(
			"music_directory"
			,"bot_name"
			,"irc_channel"
			,"oauth"
			,"live_track_file");


	Config() {
		props = new Properties();
		try {
			File file = new File("dacapobot.properties");
			if ( !file.exists() ) {
				try ( OutputStream output = new FileOutputStream( file ) ) {
					props.setProperty( "music_directory", "$HOME/Music" );
					props.setProperty( "bot_name", "YOUR_BOT_NAME_HERE" );
					props.setProperty( "irc_server", "irc.twitch.tv" );
					props.setProperty( "irc_channel", "#test" );
					props.setProperty( "oauth", "BIGOAUTHTOKENHERE" );
					props.setProperty( "live_track_file", "location where the current track title stored." );
					props.store( output, null );
				}
				System.err.println( "Error: Properties file not exist " + file.getCanonicalPath() + "\n\t\tCreating, please edit before next run." );
				System.exit( 0 );
			} else {
				try ( InputStream input = new FileInputStream( file ) ) {
					props.load( input );
					if ( !props.stringPropertyNames().containsAll( propertiesList ) ) {
						System.err.println( "Error: Missing Property in " + file.getCanonicalPath() + "\n\t\tDelete to generate defaults." );
						System.exit( 0 );
					}
				}
			}
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}


	//--------------------------------------------------------------------------
	Configuration getBotConfig() {
		//Configure what we want our bot to do
		Configuration botConfiguration = new Configuration.Builder()

				.setAutoNickChange(false) //Twitch doesn't support multiple users
				.setOnJoinWhoEnabled(false) //Twitch doesn't support WHO command
				.setCapEnabled(true)
				.addCapHandler(new EnableCapHandler("twitch.tv/membership")) //Twitch by default doesn't send JOIN, PART, and NAMES unless you request it, see https://github.com/justintv/Twitch-API/blob/master/IRC.md#membership
				.setEncoding( StandardCharsets.UTF_8 )
				.addServer( props.getProperty( "irc_server" )) //Join the network
				.setName( props.getProperty( "bot_name" ) ) //Set the nick of the bot.
				.setServerPassword( props.getProperty( "oauth" )) //Your oauth password from http://twitchapps.com/tmi
				.addAutoJoinChannel( props.getProperty( "irc_channel" ) )
				.addListener( new BotListener() ) //Add our listener that will be called on Events
				.buildConfiguration();

		return botConfiguration;

	}
	//--------------------------------------------------------------------------

	File getMusicDir(){
		//set the directory where music will be located
		return new File( props.getProperty( "music_directory" ) );
	}




}
//------------------------------------------------------------------------------
