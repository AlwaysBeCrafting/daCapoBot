package stream.alwaysbecrafting.dacapobot.player;

public interface Player {

	void setQueue();
	void play();
	void nextTrack();
	String getCurrentTitle();
	void exit();
}
