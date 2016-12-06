package stream.alwaysbecrafting.daCapoBot;

import org.pircbotx.Configuration;

//==============================================================================
public class BotConfig {
	Configuration botConfiguration;
	//--------------------------------------------------------------------------
	public BotConfig(){
	//Configure what we want our bot to do
		this.botConfiguration = new Configuration.Builder()
			.setName("PircBotXUser") //Set the nick of the bot. CHANGE IN YOUR CODE
			.addServer("127.0.0.1") //Join the freenode network
			.addAutoJoinChannel("#test") //Join the official #pircbotx channel
			.addListener(new BotListener()) //Add our listener that will be called on Events
			.buildConfiguration();

	}

	public Configuration get() {
		return botConfiguration;
	}
	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
