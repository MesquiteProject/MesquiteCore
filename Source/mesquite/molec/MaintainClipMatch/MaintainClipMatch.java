/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.MaintainClipMatch;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
/* ======================================================================== *

*new in 1. 06*

/* ======================================================================== */
public class MaintainClipMatch extends MaintainSequenceMatch {
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
   	public String getSequence() {
   		String sequence = null;
		 //  GET SEQUENCE FROM CLIPBOARD
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = clip.getContents(this);
		try {
			sequence = (String)t.getTransferData(DataFlavor.stringFlavor);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		if (sequence == null)
			return null;
		sequence = StringUtil.stripWhitespace(sequence);
		return sequence;
   	}
	public String getMessage(){
		if (getTaxonNumber()<0)
			return "No taxon has been selected for " + getName();
		if (getSearchSequence() == null)
			return "No text in clipboard";
		if (getSequenceFound())
			return "Showing this text from clipboard in \"" + getTaxonName() + "\": ";
		return "Text from clipboard not found in \"" + getTaxonName() + "\": ";
	}
   	 
	/*.................................................................................................................*/
    	 public String getName() {
		return "Maintain Clipboard Match";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Finds the first occurrence of the sequence in  the clipboard, within a designated taxon, and maintains that match as the clipboard changes." ;
   	 }
   	 
}


