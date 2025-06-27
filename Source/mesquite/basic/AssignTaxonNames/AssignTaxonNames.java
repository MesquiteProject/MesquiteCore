/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.AssignTaxonNames;

import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.duties.TaxonNameAlterer;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== */
public class AssignTaxonNames extends TaxonNameAlterer {
	String prefix = "taxon ";
	int startingNumber = 0;
	int counter = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	public boolean getOptions(Taxa taxa, int firstSelected){
		counter=0;
		if (MesquiteThread.isScripting())
			return true;
		
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Assign Taxon Names",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Assign Taxon Names");
		
		SingleLineTextField baseName = dialog.addTextField("Base for taxon names: ", prefix, 25);
		IntegerField startingNumberField = dialog.addIntegerField("Starting number", firstSelected+1, 8, 0, 1000000);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			prefix = baseName.getText();
			startingNumber = startingNumberField.getValue();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
		}
	/*.................................................................................................................*/
   	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(Taxa taxa, int it){
   		int num = startingNumber + counter;
		taxa.setTaxonName(it, prefix + num, false);
		counter++;
		return true;
   	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Assign Taxon Names...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Assign taxon names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Assigns to each taxon name a string followed by a number.  For example, if the string is \"g\", then taxon 3 will be assigned the name g3.";
   	 }
}


	


