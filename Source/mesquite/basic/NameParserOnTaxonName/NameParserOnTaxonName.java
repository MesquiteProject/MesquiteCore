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
		if (taxa.getNumTaxa()>=3)
			nameParser.setExamples(new String[]{taxa.getTaxonName(0), taxa.getTaxonName(taxa.getNumTaxa()/2), taxa.getTaxonName(taxa.getNumTaxa()-1)});
		else if (taxa.getNumTaxa()==2)
			nameParser.setExamples(new String[]{taxa.getTaxonName(0), taxa.getTaxonName(1)});
		else if (taxa.getNumTaxa()>0)
			nameParser.setExamples(new String[]{taxa.getTaxonName(0)});
			String helpString = "";
			/*This tool requires that the names of the containing taxa (e.g., populations) are formed as reduced versions of the taxon names of the "
					+ "other block of taxa (e.g., specimens).  In particular, the names of the populations must exactly match a portion of specimen names of the other block.  "
					+ "This tool finds the match by reducing the specimen names by including or excluding pieces according to the criteria you specify, and, if that reduced name"
					+ "matches the name of a population, then the specimen is associated with that population.";
			*/
			boolean ok = nameParser.queryOptions("Options for matching specimens to populations", "Populations will be matched to specimens by examining their names", "In choosing what parts of the specimen name to compare to the population names,", helpString);
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
	/*.................................................................................................................*
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
}


	


