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
package mesquite.basic.AddToTaxonNames;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class AddToTaxonNames extends TaxonNameAlterer {
	String textToAdd="";
	MesquiteBoolean addToEnd = new MesquiteBoolean(true);
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
   	public boolean getOptions(Taxa taxa, int firstSelected){
   		if (MesquiteThread.isScripting())
   			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Add to Taxon Names",  buttonPressed);
		queryDialog.addLabel("Add to Taxon Names", Label.CENTER);
		SingleLineTextField prefixField = queryDialog.addTextField("Text:", textToAdd, 12);
		Checkbox addToEndBox = queryDialog.addCheckBox("add to end of names", addToEnd.getValue());
		queryDialog.completeAndShowDialog(true);
			
		boolean ok = (queryDialog.query()==0);
		
		if (ok) {
			textToAdd = prefixField.getText();
			addToEnd.setValue(addToEndBox.getState());
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
		if (name!=null){
			String s;
			if (addToEnd.getValue())
				s =  name + textToAdd;
			else
				s =  textToAdd + name;
			taxa.setTaxonName(it, s, false);
			nameChanged = true;
		}
		return nameChanged;
   	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Adds text to taxon names", "[text]", commandName, "addText")) {
	   	 		if (taxa !=null){
	   	 			 textToAdd = parser.getFirstToken(arguments);
    	 				addToEnd.toggleValue(parser.getNextToken());
	   	 			alterTaxonNames(taxa,table);
	   	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Add to Names...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Add text to taxon names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Adds text to taxon names.";
   	 }
}


	


