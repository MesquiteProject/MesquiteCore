/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.CondenseTaxonNames;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CondenseTaxonNames extends TaxonNameAlterer {
	int truncLength = 10;
	boolean cleanToken = true;

	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
   	/** A stub method for doing any necessary cleanup after taxon names have been altered.*/
   	public void cleanupAfterAlterTaxonNames(){
    	}
	/*.................................................................................................................*/
   	public boolean getOptions(Taxa taxa, int firstSelected){
   		if (MesquiteThread.isScripting())
   			return true;

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Condense names",  buttonPressed);
		queryDialog.addLabel("Condensation of taxon names", Label.CENTER);
		SingleLineTextField truncationLengthField = queryDialog.addTextField("Maximum length:", ""+truncLength, 20);
		Checkbox cleanTokenBox = queryDialog.addCheckBox("remove non-alphanumeric characters", cleanToken);
		queryDialog.addLargeOrSmallTextLabel("Hint: before doing this, you may want to archive current names using Archive Taxon Names under Taxon Utilities in the Character Matrix Editor or the List of Taxa window" );
		queryDialog.completeAndShowDialog(true);
			
		boolean ok = (queryDialog.query()==0);
		
		if (ok) {
			String s = truncationLengthField.getText();
			truncLength = MesquiteInteger.fromString(s);
			if (!MesquiteInteger.isCombinable(truncLength))
				ok = false;
			cleanToken = cleanTokenBox.getState();
		}
		
		queryDialog.dispose();

		return ok;
   	}
	/*.................................................................................................................*/
   	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(Taxa taxa, int it){
//		String s=StringUtil.deletePunctuationAndSpaces(name);
 		boolean nameChanged = false;
		String name = taxa.getTaxonName(it);
		if (name!=null && (cleanToken || name.length()>truncLength)){
			String suffix;
			if (cleanToken)
				suffix= ""+(it+1);   //it+1 as zero-based
			else
				suffix = "."+(it+1);
			int cutLength = truncLength-suffix.length();
			String trunced=name;
			if (cutLength>0  && name.length()>cutLength)
				trunced =  name.substring(0, cutLength)+suffix;
			if (cleanToken){
				trunced =  StringUtil.cleanseStringOfFancyChars(trunced,true,false);
			}
			taxa.setTaxonName(it, trunced, false);
			nameChanged = true;
		}
		return nameChanged;
   	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Consenses taxon names", "[length]", commandName, "condense")) {
	   	 		if (taxa !=null){
	   	 			 truncLength = MesquiteInteger.fromFirstToken(arguments, new MesquiteInteger(0));

	   	 			alterTaxonNames(taxa,table);
	   	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Condense Taxon Names...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Condense taxon names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Condenses taxon names, for example for use by programs that cannot handle long names or names with punctuation.";
   	 }
 	/*.................................................................................................................*/
  	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
  	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
  	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
     	public int getVersionOfFirstRelease(){
     		return 110;  
     	}
     	/*.................................................................................................................*/
     	public boolean isPrerelease(){
     		return false;
     	}

}


	


