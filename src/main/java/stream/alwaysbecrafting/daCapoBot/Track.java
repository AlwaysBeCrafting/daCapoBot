package stream.alwaysbecrafting.daCapoBot;


import java.io.File;

//==============================================================================
public class Track {
	//--------------------------------------------------------------------------
	public File file;

	//--------------------------------------------------------------------------

	Track(File track){
	this.file = track;
	}

	//--------------------------------------------------------------------------

	public String toURIString(){
		if(!this.file.exists()){
			System.out.println("Error: File does not Exist: " + this.file.toString());
			throw new RuntimeException(	);
		}
		return this.file.toURI().toString();
	}

	//--------------------------------------------------------------------------

	@Override
	public String toString(){
		return file.getName();

	}

	//--------------------------------------------------------------------------
}

//------------------------------------------------------------------------------
