/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.RemoveFromTaxonNames;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class RemoveFromTaxonNames extends TaxonNameAlterer {
	int truncLength = 1;
	MesquiteBoolean removeFromEnd = new MesquiteBoolean(true);
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
   	public boolean getOptions(Taxa taxa, int firstSelected){
   		if (MesquiteThread.isScripting())
   			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Remove from names",  buttonPressed);
		queryDialog.addLabel("Remove text from taxon names", Label.CENTER);
		SingleLineTextField truncationLengthField = queryDialog.addTextField("Number of characters to remove:", ""+truncLength, 20);
		Checkbox removeFromEndBox = queryDialog.addCheckBox("remove from end of names", removeFromEnd.getValue());
		queryDialog.completeAndShowDialog(true);
			
		boolean ok = (queryDialog.query()==0);
		
		if (ok) {
			String s = truncationLengthField.getText();
			truncLength = MesquiteInteger.fromString(s);
			if (!MesquiteInteger.isCombinable(truncLength))
				ok = false;
			if (truncLength<0)
				truncLength=0;
			removeFromEnd.setValue(removeFromEndBox.getState());
		}
		
		queryDialog.dispose();

		return ok;
   	}
	/*.................................................................................................................*/
   	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(Taxa taxa, int it){
   		boolean nameChanged = false;
		String name = taxa.getTaxonName(it);
		if (name!=null && name.length()>truncLength){
			String trunced;
			if (removeFromEnd.getValue())
				trunced =  name.substring(0, name.length()-truncLength);
			else
				trunced =  name.substring(truncLength, name.length());
			taxa.setTaxonName(it, trunced, false);
			nameChanged = true;
		}
		return nameChanged;
   	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Removes text from taxon names", "[length]", commandName, "removeText")) {
	   	 		if (taxa !=null){
	   	 			 truncLength = MesquiteInteger.fromFirstToken(arguments, new MesquiteInteger(0));
    	 				removeFromEnd.toggleValue(parser.getNextToken());
	   	 			alterTaxonNames(taxa,table);
	   	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Remove from Names...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Remove from Taxon Names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Removes a specified number of characters from taxon names.";
   	 }
}


	


