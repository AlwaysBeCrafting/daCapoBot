package stream.alwaysbecrafting.daCapoBot;


import java.io.File;

//==============================================================================
public class Track {
	//--------------------------------------------------------------------------
	public File file;

	Track(File track){
	this.file = track;
	}

	@Override
	public String toString(){
		return file.getName();

	}


	//--------------------------------------------------------------------------
}

//------------------------------------------------------------------------------
