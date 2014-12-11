/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SelectCharacterRanges;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SelectCharacterRanges extends CharacterSelector {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public void selectRange(CharacterData data, int icStart, int icEnd){
			for (int i=icStart; i<=icEnd; i++) {
					data.setSelected(i, true);
			}
   	}

	
   	/** Called to select characters*/
   	public void selectCharacters(CharacterData data){
   		if (data!=null && data.getNumChars()>0){
   			MesquiteString ranges = new MesquiteString();
   			
   			String help="Indicate ranges in the form \"[startOfRange1 endOfRange1] [startOfRange2 endOfRange2] [startOfRange3 endOfRange3]\"";
   			boolean success = QueryDialogs.queryString(containerOfModule(), "Character Ranges to Select", "Enter ranges to select", help, ranges, 5, true, false);
   			if (!success) 
   				return;
   			Parser parser = new Parser(ranges.getValue());
   			//parser.setPunctuationString("[]");
   			parser.setAllowComments(false);
   			String token = parser.getNextToken();
   			int icStart = -2;
   			int icEnd = -1;
   			
   			while (StringUtil.notEmpty(token)) {
   				if (token.equalsIgnoreCase("[")) {  // start of range
					icStart = MesquiteInteger.fromString(parser);
					if (MesquiteInteger.isCombinable(icStart)) {
						icEnd = MesquiteInteger.fromString(parser);
						if (MesquiteInteger.isCombinable(icEnd)) {
							selectRange(data,icStart-1,icEnd-1);
						} else
							selectRange(data,icStart-1,icStart-1);
					}
   				}
   				token = parser.getNextToken();
					if (StringUtil.notEmpty(token)) {
		   				if (token.equalsIgnoreCase("]")) 
		   	   				token = parser.getNextToken();
					}
   			}

   			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
   		}
   	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Character Ranges...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects characters that are within the ranges given in a text list of the form [start end] [start end]..." ;
   	 }
   	 
}


