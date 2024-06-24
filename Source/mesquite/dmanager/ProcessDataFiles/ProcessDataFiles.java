/* Mesquite source code.  Copyright 2016 and onward, W. Maddison and D. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.dmanager.ProcessDataFiles; 

import java.awt.event.KeyEvent;

import mesquite.dmanager.lib.ProcessDataFilesLib;


/* ======================================================================== */
public class ProcessDataFiles extends ProcessDataFilesLib { 
	/*.................................................................................................................*/

	/*.................................................................................................................*/
	public String getName() {
		return "Process Data Files";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Process Data Files...";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}

	/*.................................................................................................................*/
	/** Returns the shortcut used for the menu item for the module*/
	public int getShortcutForMenuItem(){
		return KeyEvent.VK_O;
	}
	/*.................................................................................................................*/
	/** Returns whether the shortcut needs shift*/
	public boolean getShortcutForMenuItemNeedsShift(){
		return true;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Processes a folder of data files.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
	}

}




