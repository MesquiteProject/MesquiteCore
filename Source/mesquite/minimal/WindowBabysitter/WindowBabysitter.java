/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.7, August 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/
package mesquite.minimal.WindowBabysitter;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class WindowBabysitter extends WindowHolder {
	public String getName() {
		return "Window Holder";
	}
	public String getExplanation() {
		return "Helps other modules by holding their windows." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true; 
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (getModuleWindow() == null)
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("getWindow");
		temp.addLine("tell It");
		temp.incorporate(getModuleWindow().getSnapshot(file), true);
		temp.addLine("endTell");
		temp.addLine("showWindow");
		return temp;
	}


	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
}

