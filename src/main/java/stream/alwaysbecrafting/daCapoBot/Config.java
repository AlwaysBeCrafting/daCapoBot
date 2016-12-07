package stream.alwaysbecrafting.daCapoBot;

import org.pircbotx.Configuration;

//==============================================================================
public class Config {
	public Configuration botConfiguration;
	public Playlist sideA;
	//--------------------------------------------------------------------------

	public Configuration getBotConfig(){
		if(botConfiguration == null) {
			//Configure what we want our bot to do
			this.botConfiguration = new Configuration.Builder()
					.setName( "daCapoBot" ) //Set the nick of the bot. CHANGE IN YOUR CODE
					.addServer( "127.0.0.1" ) //Join the freenode network
					.addAutoJoinChannel( "#test" ) //Join the official #pircbotx channel
					.addListener( new BotListener() ) //Add our listener that will be called on Events
					.buildConfiguration();
		}
		return botConfiguration;

	}
	//--------------------------------------------------------------------------

	public Playlist getPlaylist(){
		this.sideA = new Playlist( "/home/mh/Music/OC ReMix - 1 to 3000 [v20141015]" );
		return this.sideA;
	}
}
//------------------------------------------------------------------------------
