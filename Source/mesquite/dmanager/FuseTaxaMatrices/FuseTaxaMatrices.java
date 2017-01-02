/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.dmanager.FuseTaxaMatrices;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class FuseTaxaMatrices extends FileAssistantT {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		includeFuse();
		return true;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Includes a file and optionally fuses taxa/characters block", null, commandName, "fuse")) {
			includeFuse();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/

	private void includeFuse(){
		String message = "You are about to read in another file, and import the taxa and characters blocks found there, with or without fusing to taxa and character blocks in "
			+ "the current file.  This process will NOT incorporate trees, footnotes or most other auxiliary information associated "
			+ "with those taxa and character blocks. ";

		discreetAlert( message);
		MesquiteModule fCoord = getFileCoordinator();
		MesquiteCommand command = makeCommand("includeFileFuse", fCoord);
		command.doItMainThread(StringUtil.argumentMarker + "fuseTaxaCharBlocks", null, this);
		MesquiteWindow w = containerOfModule();
		MesquiteFrame f = w.getParentFrame();
		if (f.getResourcesClosedWhenMinimized())
				f.setResourcesState(false, false, -1);
		iQuit();

	}
	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return false;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Merge Taxa & Matrices from File...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Merge Taxa & Matrices from File";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Includes a file and concatenates its taxa to an existing taxa block, for instance to add seqences." ;  
	}

}


