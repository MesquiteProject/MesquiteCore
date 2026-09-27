/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SelectCharsInvert;
/*~~  */

import java.util.Random;

import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharacterSelector;

/* ======================================================================== */
public class SelectCharsInvert extends CharacterSelector {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
   	/** Called to select characters*/
   	public void selectCharacters(CharacterData data){
   		if (data!=null && data.getNumChars()>0){
   			for (int i=0; i<data.getNumChars(); i++) {
   				data.setSelected(i, !data.isSelected(i));
   			}
   			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
   		}
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Invert Characters Selected";
   	 }
	/*.................................................................................................................*/
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Inverts characters selected." ;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return NEXTRELEASE;  
 	}

}


