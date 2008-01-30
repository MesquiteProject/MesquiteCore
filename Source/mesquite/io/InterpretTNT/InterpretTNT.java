package mesquite.io.InterpretTNT;

import mesquite.io.lib.*;

public class InterpretTNT extends InterpretHennig86Base {


	/*.................................................................................................................*/
	public String getName() {
		return "TNT";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports and exports TNT files." ;
	}
	/*.................................................................................................................*/

	public String preferredDataFileExtension() {
		return "tnt";
	}

}




