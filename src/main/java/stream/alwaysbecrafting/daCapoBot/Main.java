package stream.alwaysbecrafting.daCapoBot;


import org.pircbotx.PircBotX;

//==============================================================================
public class Main{
	//--------------------------------------------------------------------------

	public static void main( String[] args ) throws Exception {
		Config botconfig = new Config();

		//Create our bot with the botConfiguration
		PircBotX bot = new PircBotX(botconfig.getBotConfig());

		//Connect to the server
		bot.startBot();


	}

	//--------------------------------------------------------------------------

}
//------------------------------------------------------------------------------
