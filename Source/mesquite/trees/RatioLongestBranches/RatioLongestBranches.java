/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.RatioLongestBranches;

import java.awt.Checkbox;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeUtil;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.duties.BranchLengthStatistic;

/** this is a silly little module that can be used as a demonstration for NumberForTree modules */
public class RatioLongestBranches extends NumberForTree  implements BranchLengthStatistic {
	MesquiteNumber nt;
	boolean unrooted = true;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!MesquiteThread.isScripting()){
			if (!queryOptions())
				return false;
		}
 		nt= new MesquiteNumber();
		addMenuItem("Branch Length Ratio Options...", new MesquiteCommand("queryOptions", this));
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
 	/*.................................................................................................................*/
 	public boolean requestPrimaryChoice(){
 		return true;
 	}
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return 361;  
 	}
	/*-----------------------------------------*/
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("unrooted " + unrooted);
		return temp;
	}

	/*.................................................................................................................*/
	public boolean queryOptions(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Flag by Ratio of Longest Branches",buttonPressed);  
		Checkbox unrootedCB = dialog.addCheckBox("Treat tree as unrooted", unrooted);

		dialog.addBlankLine();
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			unrooted = unrootedCB.getState();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Queries options", null, commandName, "queryOptions")) {
			if (queryOptions()){
				if (!MesquiteThread.isScripting()){
					parametersChanged();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to treat trees as unrooted.", "[true or false]", commandName, "unrooted")) {
			boolean temp = MesquiteBoolean.fromTrueFalseString(parser.getFirstToken(arguments));
			if (temp != unrooted){
				unrooted = temp; 
				if (!MesquiteThread.isScripting()){
					parametersChanged();
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 if (result==null || tree==null)
    	 		return;
    	clearResultAndLastResult(result);
    	MesquiteDouble longest = new MesquiteDouble();
    	MesquiteDouble secondLongest = new MesquiteDouble();
    	TreeUtil.getLongestBranches(tree, tree.getRoot(), null, longest, secondLongest, unrooted);
    	if (longest.isCombinable() && secondLongest.isCombinable() && secondLongest.getValue() != 0){
		nt.setValue(longest.getValue()/secondLongest.getValue());
		result.setValue(nt);
		if (resultString!=null)
			resultString.setValue("Ratio: "+ nt.toString());
    	}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
    	 public String getName() {
		return "Ratio of Longest Branches";
   	 }
	/*.................................................................................................................*/
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Gets the ratio of the two greatest branch lengths in a tree." ;
   	 }
   	 
}

