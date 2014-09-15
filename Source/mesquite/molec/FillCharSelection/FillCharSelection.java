/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.FillCharSelection;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
//new in 1. 06
//need to have UI to specify tolerance

/* ======================================================================== */
public class FillCharSelection extends CharacterSelector {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	int THRESHOLD = 1; //allowed space between selections to fuse

   	/** Called to select characters*/
   	public void selectCharacters(CharacterData data){
   		if (data!=null && data.getNumChars()>0){
   			if (!data.anySelected()) //nothing to interpolate
   				return;
   			int numChars = data.getNumChars();
   			int lastSel = -1;
   			boolean prevUnselected = false;
   			for (int i=0; i<numChars; i++) {
   				if (data.getSelected(i)) {
   					if (lastSel>=0 && i-lastSel>1 && i-lastSel<=THRESHOLD+1){ //hit selected after unselected; last selected near enough to interpolate
						for (int i2=lastSel+1; i2<i; i2++)
							data.setSelected(i2, true);
   					}
   					lastSel = i;
   				}
   			}
   			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
   		}
   	}
   	public boolean requestPrimaryChoice(){
   		return true;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Interpolate Character Selection";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false; 
   	 }
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects characters that are surrounded by characters selected." ;
   	 }
   	 
}


