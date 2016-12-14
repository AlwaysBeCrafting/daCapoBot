package stream.alwaysbecrafting.daCapoBot;

import org.pircbotx.Configuration;

import java.io.File;

//==============================================================================
class Config {
	//--------------------------------------------------------------------------
	Configuration getBotConfig() {
		//Configure what we want our bot to do
		Configuration botConfiguration = new Configuration.Builder()
				.setName( "daCapoBot" ) //Set the nick of the bot.
				.addServer( "192.168.1.42" ) //Join the network
				.addAutoJoinChannel( "#test" )
				.addListener( new BotListener() ) //Add our listener that will be called on Events
				.buildConfiguration();
		return botConfiguration;

	}
	//--------------------------------------------------------------------------

	File getMusicDir(){
		//set the directory where music will be located
		return new File( "/home/mh/Music/" );
	}
}
//------------------------------------------------------------------------------
