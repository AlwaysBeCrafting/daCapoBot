package stream.alwaysbecrafting.daCapoBot;

import org.pircbotx.Configuration;

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
			,"irc_server"
			,"irc_channel");

	Config(){
		properties();
	}

	//--------------------------------------------------------------------------
	Configuration getBotConfig() {
		//Configure what we want our bot to do
		Configuration botConfiguration = new Configuration.Builder()
				.setName( props.getProperty( "bot_name" ) ) //Set the nick of the bot.
				.addServer( props.getProperty( "irc_server" )) //Join the network
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
				props.setProperty( "irc_server", "127.0.0.1" );
				props.setProperty( "irc_channel", "#test" );
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
