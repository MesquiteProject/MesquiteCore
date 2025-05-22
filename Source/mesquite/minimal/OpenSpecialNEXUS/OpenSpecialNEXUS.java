/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.OpenSpecialNEXUS;
/*~~  */

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class OpenSpecialNEXUS extends GeneralFileMakerSingle  {
	public boolean loadModule(){
		return false;
	}
	public String getNameForMenuItem() {
		return "Special NEXUS...";
	}
	public String getName() {
		return "Open Special NEXUS";
	}
	public String getExplanation() {
		return "Opens a NEXUS file with a special importer";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** make a new    MesquiteProject.*/
	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments){
		MesquiteProject p = MesquiteTrunk.mesquiteTrunk.openOrImportFileHandler(null, arguments, NEXUSInterpreter.class);
		return p;
	}
}

