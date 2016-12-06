package stream.alwaysbecrafting.daCapoBot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

//==============================================================================
public class BotListener extends ListenerAdapter{
	//--------------------------------------------------------------------------


	@Override
	public void onGenericMessage(GenericMessageEvent event) {
		//When someone says ?helloworld respond with "Hello World"
		if (event.getMessage().startsWith("?helloworld"))
			event.respond("Hello world!");
	}

	//--------------------------------------------------------------------------
}
//------------------------------------------------------------------------------
