package stream.alwaysbecrafting.dacapobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Config {
	public final Properties props;
	private final List<String> propertiesList = Arrays.asList(
			"music_directory",
			"bot_name",
			"irc_channel",
			"irc_server",
			"oauth",
			"live_track_file",
			"admins",
			"db_path"
			);


	public Config() {
		props = new Properties();
		try {
			File file = new File("dacapobot.properties");
			if ( !file.exists() ) {
				try ( OutputStream output = new FileOutputStream( file ) ) {
					props.setProperty( "music_directory", "<yourHomePathHere>/Music" );
					props.setProperty( "bot_name", "YOUR_BOT_NAME_HERE" );
					props.setProperty( "irc_server", "irc.twitch.tv" );
					props.setProperty( "irc_channel", "#test" );
					props.setProperty( "oauth", "BIGOAUTHTOKENHERE" );
					props.setProperty( "live_track_file", "location where the current track title stored." );
					props.setProperty( "admins", "#,#" );
					props.setProperty( "db_path", "<yourHomePathHere>/dacapobot.db");
					props.store( output, null );
				}
				System.err.println( "Error: Properties path not exist " + file.getCanonicalPath() + "\n\t\tCreating, please edit before next run." );
				System.exit( 0 );
			} else {
				try ( InputStream input = new FileInputStream( file ) ) {
					props.load( input );
					if ( !props.stringPropertyNames().containsAll( propertiesList ) ) {
						System.err.println( "Error: Missing Property in " + file.getCanonicalPath() + "\n\t\tDelete to generate defaults." );
						System.exit( 0 );
					}
				}
			}
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}

	public File getMusicDir(){
		return new File( props.getProperty( "music_directory" ) );
	}

	public String getServer() {
		return props.getProperty( "irc_server" );
	}

	public String getName() {
		return props.getProperty( "bot_name" );
	}

	public String getOauth() {
		return props.getProperty( "oauth" );
	}

	public String getChannel() {
		return props.getProperty( "irc_channel" );
	}

	public String getDBPath() {
		return props.getProperty( "db_path" );
	}

	public List<String> getAdmins() {
		return Arrays.asList( props.getProperty( "admins" ).toString().split( "," ) );
	}

	public List<String> getTimeZones() {
		String[] tzones = TimeZone.getAvailableIDs();
		Arrays.sort(tzones);
		return Arrays.asList(tzones);
	}

	public String getTime() {

		ZoneId your_zone = ZoneId.of(props.getProperty( "timezone" ));
		LocalDateTime time_now = LocalDateTime.now(your_zone);
		return time_now.toString();
	}

	public File getTrackFile() {
		return new File( props.getProperty( "live_track_file" ) );
	}
}
