package stream.alwaysbecrafting.dacapobot.bot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import stream.alwaysbecrafting.dacapobot.Config;

public class DaCapoBot {
	PircBotX bot;
	public DaCapoBot( Config config, BotListener botListener ) {
		bot = new PircBotX( new Configuration.Builder()
				.setAutoNickChange(false) //Twitch doesn't support multiple users
				.setOnJoinWhoEnabled(false) //Twitch doesn't support WHO command
				.setCapEnabled(true)
				.addCapHandler(new EnableCapHandler("twitch.tv/membership")) //Twitch by default doesn't send JOIN, PART, and NAMES unless you request it, see https://github.com/justintv/Twitch-API/blob/master/IRC.md#membership
				.setEncoding( StandardCharsets.UTF_8 )
				.addServer( config.getServer() ) //Join the network
				.setName( config.getName() ) //Set the nick of the bot.
				.setServerPassword( config.getOauth() ) //Your oauth password from http://twitchapps.com/tmi
				.addAutoJoinChannel( config.getChannel() )
				.addListener( botListener ) //Add our listener that will be called on Events
				.buildConfiguration() );

	}

	public void startBot() throws IOException, IrcException {
		bot.startBot();
	}
}
