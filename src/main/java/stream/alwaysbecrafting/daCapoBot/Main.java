package stream.alwaysbecrafting.daCapoBot;


import org.pircbotx.PircBotX;

//==============================================================================
public class Main{
	//--------------------------------------------------------------------------

	public static void main( String[] args ) {
		//Create our bot with the botConfiguration
		PircBotX bot = new PircBotX(Config.CONFIG.getBotConfig());

		//Connect to the server
		//See onConnect in BotListener for most program behavior
		try{
			bot.startBot();
		}
		catch (Exception e){
			e.printStackTrace();
		}


	}

	//--------------------------------------------------------------------------

}
//------------------------------------------------------------------------------
