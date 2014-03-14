package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;


/* ======================================================================== */
/**Superclass of file interpreting modules (e.g., NEXUS file reader/writer).  Different subclasses are expected
to read different data file formats.  Example module: "Interpret NEXUS files" (class InterpretNexus).  Example of use:
see BasicFileCoordinator.*/

/** a subclass for determining whether two strings match*/
public abstract class MesquiteStringMatcher extends MesquiteModule  {

	/** returns true if the options are set and accepted.*/
	public  boolean queryOptions(){
		return true;
	}
	/** returns whether two strings are considered equal.*/
	public abstract boolean stringsMatch(String s1, String s2);
}


