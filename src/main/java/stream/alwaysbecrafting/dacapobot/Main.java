package stream.alwaysbecrafting.dacapobot;

import stream.alwaysbecrafting.dacapobot.bot.BotListener;
import stream.alwaysbecrafting.dacapobot.bot.DaCapoBot;
import stream.alwaysbecrafting.dacapobot.database.Database;
import stream.alwaysbecrafting.dacapobot.database.SQLiteDatabase;
import stream.alwaysbecrafting.dacapobot.player.JfxPlayer;
import stream.alwaysbecrafting.dacapobot.player.Player;

public class Main{

	public static void main( String[] args ) throws Exception {
		Config config = new Config();
		Database database = new SQLiteDatabase( config );
		Player player = new JfxPlayer( config, database );
		BotListener botListener = new BotListener( config, database, player );
		database.addMP3s( new Config().getMusicDir() );
		player.setQueue();
		player.play();
		DaCapoBot bot = new DaCapoBot( config, botListener );
		bot.startBot();
	}
}
