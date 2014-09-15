/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.dmanager.KeepTaxonPrefixSuffix;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== 
*new in 1.02* */
public class KeepTaxonPrefixSuffix extends TaxonNameAlterer {
	String searchText=".";
	MesquiteBoolean suffix = new MesquiteBoolean(true);
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	/*.................................................................................................................*/
   	public boolean getOptions(Taxa taxa, int firstSelected){
   		if (MesquiteThread.isScripting())
   			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Keep Only Prefix or Suffix",  buttonPressed);
		queryDialog.addLabel("Keep only a prefix or suffix of taxon name", Label.CENTER);
		SingleLineTextField searchField = queryDialog.addTextField("Delimiter:", searchText, 12, true);
		Checkbox suffixBox = queryDialog.addCheckBox("Keep Suffix (otherwise Prefix)", suffix.getValue());
		queryDialog.completeAndShowDialog(true);
			
		boolean ok = (queryDialog.query()==0);
		
		if (ok) {
			searchText = searchField.getText();
			suffix.setValue(suffixBox.getState());
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
			String s = "";
			if (suffix.getValue()){
				int delim = name.lastIndexOf(searchText);
				if (delim>=0){
					name = name.substring(delim+1, name.length());
					taxa.setTaxonName(it, name, false);
					nameChanged = true;
				}
			}
			else {
				int delim = name.indexOf(searchText);
				if (delim>=0){
					name = name.substring(0, delim);
					taxa.setTaxonName(it, name, false);
					nameChanged = true;
				}
			}
		}
		return nameChanged;
   	}
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Keep Prefix/Suffix of Taxon Name...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Keep Prefix/Suffix of Taxon Name";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Keeps only Prefix or Suffix of Taxon Name.";
   	 }
}


	


