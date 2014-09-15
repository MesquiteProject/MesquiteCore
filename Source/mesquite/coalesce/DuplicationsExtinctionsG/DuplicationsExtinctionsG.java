/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.DuplicationsExtinctionsG;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;
import mesquite.assoc.lib.*;


/* ======================================================================== */
/** This evaluates a gene tree by calculating how much lineage sorting is implied by a containing species tree. */
public class DuplicationsExtinctionsG extends GeneTreeFit {
	MesquiteSubmenuSpec countOptionSubmenu;
	static int DUPSEXT = 0;
	static int DUPSONLY = 1;
	static int EXTONLY = 2;
	int style = DUPSEXT;
	MesquiteString countOptionName;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		countOptionSubmenu = addSubmenu(null, "Counting Mode (Dup./Ext.)");
		addItemToSubmenu(null, countOptionSubmenu, "Count Duplications & Extinctions", makeCommand("countDE",  this));
 		addItemToSubmenu(null, countOptionSubmenu, "Count Duplications Only", makeCommand("countD",  this));
 		addItemToSubmenu(null, countOptionSubmenu, "Count Extinctions Only", makeCommand("countE",  this));
		countOptionName = new MesquiteString("Count Duplications & Extinctions");
		style = DUPSEXT;
		countOptionSubmenu.setSelected(countOptionName);
		return true;
  	 }
	/*.................................................................................................................*/
 	 public Snapshot getSnapshot(MesquiteFile file) { 
  	 	Snapshot temp = new Snapshot();
  	 	if (style== DUPSEXT)
 	 		temp.addLine("countDE"); 
 	 	else if (style== DUPSONLY)
 	 		temp.addLine("countD"); 
 	 	else if (style== EXTONLY)
 	 		temp.addLine("countE"); 
	 	return temp;
 	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
	   	 	 if (checker.compare(this.getClass(), "Sets counting mode", null, commandName, "countDE")) {
    	 		int current = style;
    	 		style = DUPSEXT;
       			countOptionName.setValue("Count Duplications & Extinctions");
   	 		if (current!=style)
    	 			parametersChanged();
    	 	}
       	 	else if (checker.compare(this.getClass(), "Sets counting mode", null, commandName, "countD")) {
    	 		int current = style;
    	 		style = DUPSONLY;
       			countOptionName.setValue("Count Duplications Only");
   	 		if (current!=style)
    	 			parametersChanged();
    	 	}
       	 	else if (checker.compare(this.getClass(), "Sets counting mode", null, commandName, "countE")) {
    	 		int current = style;
    	 		style = EXTONLY;
    			countOptionName.setValue("Count Extinctions Only");
    			if (current!=style)
    	 			parametersChanged();
    	 	}
    		else {
     			return  super.doCommand(commandName, arguments, checker);
    		}
    		return null;
       	 }
	   	/*.................................................................................................................*/
 	public void calculateCost(ReconstructAssociation reconstructTask, Tree speciesTree, MesquiteTree geneTree, TaxaAssociation association, MesquiteNumber result, MesquiteString r){
        if (result != null){
	        	AssociationHistory history = reconstructTask.reconstructHistory(speciesTree, geneTree, association, result, r);
	        	MesquiteInteger duplications = new MesquiteInteger(0);
	        	MesquiteInteger extinctions = new MesquiteInteger(0);
	        	history.countDuplicationsExtinctions(speciesTree, geneTree, duplications, extinctions);
	        	if (r != null)
	        		r.setValue("Duplications " + duplications.getValue() + "; Extinctions: " + extinctions.getValue());
	        	if (style == DUPSEXT)
	        		result.setValue(duplications.getValue() + extinctions.getValue());
	        	else if (style == DUPSONLY)
	        		result.setValue(duplications.getValue());
	        	else if (style == EXTONLY)
	        		result.setValue(extinctions.getValue());
	        }
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
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
 	/*.................................................................................................................*/
	 public String getExplanation() {
	return "Counts the number of duplication and extinction events for gene tree implied by a containing species tree";
	 }

/*.................................................................................................................*/
	 public String getName() {
	return "Gene Duplication-Extinction (gene tree)";
	 }
}

