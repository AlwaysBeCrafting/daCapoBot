package stream.alwaysbecrafting.dacapobot.bot.commands;

import java.nio.file.Path;
import java.util.Optional;

import stream.alwaysbecrafting.dacapobot.TrackData.TrackMetadata;

public interface Parser {
	Optional<TrackMetadata> tryParse( Path path );
}
