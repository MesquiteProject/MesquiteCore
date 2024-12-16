/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.SelectByCodonPosition;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SelectByCodonPosition extends CharacterSelector {
	static int targetPosition = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		return true;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Codon Position to Select",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Select which codon position?");
		RadioButtons choices = dialog.addRadioButtons (new String[]{"N", "1", "2", "3"}, targetPosition);
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			targetPosition= choices.getValue();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	/** Called to select characters*/
	public void selectCharacters(CharacterData data){
		if (data!=null && data.getNumChars()>0){
			CodonPositionsSet posSet = (CodonPositionsSet)data.getCurrentSpecsSet(CodonPositionsSet.class);
			if (posSet != null) {
				boolean changed = false;
				for (int i=0; i<data.getNumChars(); i++) {
					int position = posSet.getInt(i);
					if (position == targetPosition) {
						changed = true;
						data.setSelected(i, true);
					}
					
				}
				if (changed)
					data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Select By Codon Position ";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Selects characters by codon position" ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}


