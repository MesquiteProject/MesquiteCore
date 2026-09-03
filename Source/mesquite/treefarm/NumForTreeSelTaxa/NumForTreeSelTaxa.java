/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.NumForTreeSelTaxa;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.NumberArray;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.treefarm.lib.NForTaxonWithTree;

public class NumForTreeSelTaxa extends NumberForTree {
	NForTaxonWithTree numberTask;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	MesquiteSubmenuSpec mss;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		ntC =makeCommand("setNumberTask",  this);
		numberTaskName = new MesquiteString();
		if (numberTask == null)
			numberTask = (NForTaxonWithTree)hireEmployee(NForTaxonWithTree.class, "Value for tree using selected taxa");//shouldn't ask as this is an init and might not be needed.  "Value to calculate for character state in taxon"

		if (numberTask != null){
			numberTask.setHiringCommand(ntC);
			numberTaskName.setValue(numberTask.getName());
		}
		else 
			return false;
		if (numModulesAvailable(NForTaxonWithTree.class)>0) {
			mss = addSubmenu(null, "Values for Taxa", ntC, NForTaxonWithTree.class);
			mss.setSelected(numberTaskName);
			mss.setEnabled(false);
		}
 		return true;
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

  	 
	/** (called in case this module needs to initialize anything; this module doesn't) */
   	public void initialize(Tree tree){
   	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setNumberTask ", numberTask);  
		temp.incorporate(super.getSnapshot(file), false);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that calculates numbers to be averaged among selected taxa", "[name of module]", commandName, "setNumberTask")) {
			NForTaxonWithTree temp =  (NForTaxonWithTree)replaceEmployee(NForTaxonWithTree.class, arguments, "Module to calculate numbers to be averaged among selected taxa", numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				parametersChanged();
				return numberTask;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	NumberArray results;


	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null || tree == null)
    	 		return;
    		Taxa taxa = tree.getTaxa();
    		int numTaxa = taxa.getNumTaxa();
    	   	clearResultAndLastResult(result);
    		if (results == null)
    			results = new NumberArray();
    		results.resetSize(numTaxa);
    		results.zeroArray();
    		numberTask.calculateNumbers(taxa, tree, results, resultString);
    		int count = 0;
    		double sum = 0;
    		for (int it = 0; it<numTaxa; it++){
    			if (taxa.isSelected(it) && results.isCombinable(it)){
    				sum += results.getDouble(it);
    				count++;
    			}
    		}
    		if (count==0)
    			result.setToUnassigned();
    		else
    			result.setValue(sum*1.0/count);
    		
		if (resultString!=null)
			resultString.setValue(numberTask.getName() + " for selected taxa: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	
	/** Explains what the module does.*/
    	 public String getExplanation() {
		return "Calculates average number for selected taxa for a given tree.";
   	 }
   	 
	/** Returns the name of the module in very short form, for use for column headings and other constrained places.*/
 	public String getVeryShortName(){
 		return "Value w/ sel. taxa";
 	}
 	
   	public boolean isPrerelease(){
   		return true;
   	}
	/** Name of module*/
    	 public String getName() {
		return "Avg. value for selected taxa with tree";
   	 }

   	 public boolean showCitation(){
   	 	return true;
   	 }
   	 
}

