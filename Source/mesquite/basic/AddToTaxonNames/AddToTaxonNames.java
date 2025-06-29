/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.AddToTaxonNames;

import java.awt.Label;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.duties.TaxonNameAlterer;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== */
public class AddToTaxonNames extends TaxonNameAlterer {
	String prefixToAdd="";
	String suffixToAdd="";
	//String textToAdd="";
	//MesquiteBoolean addToEnd = new MesquiteBoolean(true);

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	public boolean getOptions(Taxa taxa, int firstSelected){
		if (MesquiteThread.isScripting())
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Prefix/Suffix Taxon Names",  buttonPressed);
		queryDialog.addLabel("Add Prefix or Suffix Taxon Names", Label.CENTER);
		SingleLineTextField prefixField = queryDialog.addTextField("Prefix:", prefixToAdd, 12);
		SingleLineTextField suffixField = queryDialog.addTextField("Suffix:", suffixToAdd, 12);
		queryDialog.completeAndShowDialog(true);

		boolean ok = (queryDialog.query()==0);

		if (ok) {
			prefixToAdd = prefixField.getText();
			suffixToAdd = suffixField.getText();
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
			s =  prefixToAdd + name + suffixToAdd;
			taxa.setTaxonName(it, s, false);
			nameChanged = true;
		}
		return nameChanged;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Adds prefix/suffix to taxon names", "[text]", commandName, "addText")) {
			if (taxa !=null){
				String textToAdd = parser.getFirstToken(arguments);
				boolean toEnd = MesquiteBoolean.fromOffOnString(parser.getNextToken());
				prefixToAdd="";
				suffixToAdd="";
				if (toEnd)
					suffixToAdd=textToAdd;
				else
					prefixToAdd = textToAdd;
				alterTaxonNames(taxa,table);
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Add Prefix/Suffix to Names...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Add text to taxon names";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Adds prefix or suffix to taxon names.";
	}
}





