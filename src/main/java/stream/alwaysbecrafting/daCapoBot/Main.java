package stream.alwaysbecrafting.daCapoBot;


import org.pircbotx.PircBotX;

//==============================================================================
public class Main{
	//--------------------------------------------------------------------------

	public static void main( String[] args ) throws Exception {
		BotConfig botconfig = new BotConfig();

		//Create our bot with the botConfiguration
		PircBotX bot = new PircBotX(botconfig.get());
		//Connect to the server
		bot.startBot();

		Playlist sideA = new Playlist( "/home/mh/Music/OC ReMix - 1 to 3000 [v20141015]" );
		sideA.shuffle();
		Player p1 = new Player();
		p1.setPlaylist( sideA );

		p1.play();

	}

	//--------------------------------------------------------------------------

}
//------------------------------------------------------------------------------
