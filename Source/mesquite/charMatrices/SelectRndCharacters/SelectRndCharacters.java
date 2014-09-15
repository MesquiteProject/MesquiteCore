/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SelectRndCharacters;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SelectRndCharacters extends CharacterSelector {
	RandomBetween rng = new RandomBetween();
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

   	/** Called to select characters*/
   	public void selectCharacters(CharacterData data){
   		if (data!=null && data.getNumChars()>0){
   			
   			int numColumns = data.getNumChars();
   			int[]  indices = new int[numColumns];
   			for (int i=0; i<indices.length; i++)
   				indices[i]=i;
			int numToSelect = MesquiteInteger.queryInteger(containerOfModule(), "Number to select", "Number of characters to select randomly:", 1, 1, numColumns-1);
	   		if (!MesquiteInteger.isCombinable(numToSelect))
	   			return;
   			for (int i=0; i<numToSelect; i++) {
   				int r = rng.randomIntBetween(0, numColumns-1-i);
   				int toComeForward = indices[numColumns-1-i];
   				indices[numColumns-1-i] = indices[r];
   				indices[r]=toComeForward;
   			}
   			data.deselectAll();
   			for (int i=numColumns-numToSelect; i<numColumns; i++) {
   				data.setSelected(indices[i], true);
   			}
   			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
   		}
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Random n characters";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects n characters randomly." ;
   	 }
   	 
}


