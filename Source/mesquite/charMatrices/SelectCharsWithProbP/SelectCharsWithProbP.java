/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SelectCharsWithProbP;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SelectCharsWithProbP extends CharacterSelector {
	Random rng = new Random();
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
   	/** Called to select characters*/
   	public void selectCharacters(CharacterData data){
   		if (data!=null && data.getNumChars()>0){
			double p = MesquiteDouble.queryDouble(containerOfModule(), "Selection probability", "Probability of selecting each character:", .5, 0, 1);
	   		if (!MesquiteDouble.isCombinable(p))
	   			return;
   			for (int i=0; i<data.getNumChars(); i++) {
   				if (rng.nextDouble()<p)
   					data.setSelected(i, true);
   			}
   			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
   		}
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "With probability p";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects characters randomly with probability p." ;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
   	 
}


