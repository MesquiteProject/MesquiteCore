/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.NameParserOnTaxonName;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== */
public class NameParserOnTaxonName extends TaxonNameAlterer {
	NameParser nameParser = new NameParser(this, "taxon");
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		if (nameParser!=null){
			String s = nameParser.preparePreferencesForXML(); 
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if (nameParser!=null)
			nameParser.processSingleXMLPreference(tag,content);
	}
	/*.................................................................................................................*/
	public boolean getOptions(Taxa taxa, int firstSelected){
		if (MesquiteThread.isScripting())
			return true;

		if (taxa.getNumTaxa()==1)
			nameParser.setExamples(new String[]{taxa.getTaxonName(0)});
		else if (taxa.numberSelected()==1){
			nameParser.setExamples(new String[]{taxa.getTaxonName( taxa.firstSelected())});
		}
		else if (taxa.getNumTaxa()>=2){
			if (taxa.anySelected()) //must be at least two selected
				nameParser.setExamples(new String[]{taxa.getTaxonName(taxa.firstSelected()), taxa.getTaxonName(taxa.lastSelected())});
			else
				nameParser.setExamples(new String[]{taxa.getTaxonName(0), taxa.getTaxonName(taxa.getNumTaxa()-1)});
		}
		String helpString = null;

		boolean ok = nameParser.queryOptions("Options for trimming taxon names", "Taxon names will be trimmed by keeping or deleting parts of them", 
				"In trimming the taxon names,", helpString);
		if (ok)
			storePreferences();
		return ok;

	}


	/*.................................................................................................................*/
	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
	public boolean alterName(Taxa taxa, int it){
		boolean nameChanged = false;
		String name = taxa.getTaxonName(it);

		if (name!=null){
			String newName = nameParser.extractPart(taxa.getTaxonName(it));
			taxa.setTaxonName(it, newName, false);
			nameChanged = true;
		}
		return nameChanged;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		Debugg.println("@@@@@@@ " + nameParser);
		temp.addLine("getNameParser");
		temp.addLine("tell It");
		temp.incorporate(nameParser.getSnapshot(file), true);
		temp.addLine("endTell");
		return temp;
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	if (checker.compare(this.getClass(), "Returns the name parser object", "[]", commandName, "getNameParser")) {
	   	 		return nameParser;
     	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
   	 }
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Keep/Delete Parts of Names...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Keep/Delete Parts of Names";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Changes taxon names by applying a set of rules about keeping and deleting portions of the names.";
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}

}





