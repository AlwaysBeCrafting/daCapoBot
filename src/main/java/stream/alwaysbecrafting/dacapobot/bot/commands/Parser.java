package stream.alwaysbecrafting.dacapobot.bot.commands;

import java.nio.file.Path;
import java.util.Optional;

import stream.alwaysbecrafting.dacapobot.TrackData;

public interface Parser {
	Optional<TrackData> tryParse( Path path );
}
