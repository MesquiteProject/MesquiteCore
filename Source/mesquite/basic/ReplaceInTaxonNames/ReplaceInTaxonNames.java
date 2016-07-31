/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.ReplaceInTaxonNames;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== 
*new in 1.02* */
public class ReplaceInTaxonNames extends TaxonNameAlterer {
	String searchText="";
	String replaceText = "";
//	MesquiteBoolean addToEnd = new MesquiteBoolean(true);
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
   	public boolean getOptions(Taxa taxa, int firstSelected){
   		if (MesquiteThread.isScripting())
   			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Replace in Taxon Names",  buttonPressed);
		queryDialog.addLabel("Replace in Taxon Name", Label.CENTER);
		SingleLineTextField searchField = queryDialog.addTextField("Search for:", searchText, 30, true);
		SingleLineTextField replaceField = queryDialog.addTextField("Replace with:", replaceText, 20, true);
	//	Checkbox addToEndBox = queryDialog.addCheckBox("add to end of names", addToEnd.getValue());
		queryDialog.completeAndShowDialog(true);
			
		boolean ok = (queryDialog.query()==0);
		
		if (ok) {
			searchText = searchField.getText();
			replaceText = replaceField.getText();
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
			String s=StringUtil.replace(name,searchText,replaceText);
			taxa.setTaxonName(it, s, false);
			nameChanged = !name.equals(s);
		}
		return nameChanged;
   	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Replaces text in taxon names", "[text]", commandName, "replaceText")) {
	   	 		if (taxa !=null){
	   	 			 searchText = parser.getFirstToken(arguments);
	   	 			 replaceText = parser.getFirstToken(arguments);
	   	 			alterTaxonNames(taxa,table);
	   	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Replace in Name...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Replaces text in taxon names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Replaces text in taxon names.";
   	 }
}


	


