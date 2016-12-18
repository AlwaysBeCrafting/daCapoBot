package stream.alwaysbecrafting.daCapoBot;

import org.pircbotx.Configuration;
import org.pircbotx.cap.EnableCapHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

//==============================================================================
class Config {
	private Properties props = new Properties();
	private static final List<String> propertiesList = Arrays.asList(
			"music_directory"
			,"bot_name"
			,"irc_channel"
			,"oauth");

	Config(){
		properties();
	}

	//--------------------------------------------------------------------------
	Configuration getBotConfig() {
		//Configure what we want our bot to do
		Configuration botConfiguration = new Configuration.Builder()

				.setAutoNickChange(false) //Twitch doesn't support multiple users
				.setOnJoinWhoEnabled(false) //Twitch doesn't support WHO command
				.setCapEnabled(true)
				.addCapHandler(new EnableCapHandler("twitch.tv/membership")) //Twitch by default doesn't send JOIN, PART, and NAMES unless you request it, see https://github.com/justintv/Twitch-API/blob/master/IRC.md#membership

				.addServer( props.getProperty( "irc_server" )) //Join the network
				.setName( props.getProperty( "bot_name" ) ) //Set the nick of the bot.
				.setServerPassword(props.getProperty( "oauth" )) //Your oauth password from http://twitchapps.com/tmi
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

	private void properties() {
		if ( !(new File( "dacapobot.properties" ).exists()) ) {
			try ( OutputStream output = new FileOutputStream( "dacapobot.properties" ) ) {
				props.setProperty( "music_directory", "$HOME/Music" );
				props.setProperty( "bot_name", "YOUR_BOT_NAME_HERE" );
				props.setProperty( "irc_server", "irc.twitch.tv" );
				props.setProperty( "irc_channel", "#test" );
				props.setProperty( "oauth", "BIGOATHTOKENHERE" );
				props.store(output, null);
			} catch ( IOException io ) {
				io.printStackTrace();
			}
			System.exit( 0 );
		}
		else{
			File file = new File( "dacapobot.properties" );
			try( InputStream input = new FileInputStream( file )){
				props.load(input);
				if(!props.stringPropertyNames().containsAll( propertiesList ) ){
					System.err.println("Error: Missing Property in " + file.getCanonicalPath() + "\n\t\tDelete to generate defaults.");
					System.exit( 0 );
				}
			}
			catch ( IOException io ){
				io.printStackTrace();
			}
		}
	}



}
//------------------------------------------------------------------------------
