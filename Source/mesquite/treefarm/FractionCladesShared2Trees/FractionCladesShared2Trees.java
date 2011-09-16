/* Mesquite source code, Treefarm package.  Copyright 1997-2010 W. Maddison, D. Maddison and P. Midford. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.FractionCladesShared2Trees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;



/* ======================================================================== */
public class FractionCladesShared2Trees extends DistanceBetween2Trees {
	StringArray terminalsAbove, terminalsBelow, otherTerminalsAbove, otherTerminalsBelow;
	MesquiteBoolean verboseOutput= new MesquiteBoolean(false);
	boolean isDistance = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		isDistance = (getHiredAs() == DistanceBetween2Trees.class);
		addCheckMenuItem(null, "Verbose Output to Log", makeCommand("toggleVerboseOutput",  this), verboseOutput);
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public boolean largerIsFurther(){  
		return false;
	}
	private void visitOriginal(Tree tree,int node,  Tree otherTree, MesquiteInteger numConsistent){
		if (tree.nodeIsInternal(node)){
			Bits b = tree.getTerminalTaxaAsBits(node);
			if (otherTree.isClade(b)) {
				int otherNode = otherTree.mrca(b);
				//				Debugg.println("    tree node terminals: " + tree.numberOfTerminalsInClade(node)+",    otherTree node terminals: " + otherTree.numberOfTerminalsInClade(otherNode));
				numConsistent.increment();
			}
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				visitOriginal(tree, daughter, otherTree, numConsistent);

		}
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree t1, Tree t2) {

	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether verbose output is listed to the log file or not", "", commandName, "toggleVerboseOutput")) {
 			boolean current = verboseOutput.getValue();
 			verboseOutput.toggleValue(parser.getFirstToken(arguments));
	 			if (current!=verboseOutput.getValue())
	 				parametersChanged();
	}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	MesquiteTree tree1eq, tree2eq;
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree1, Tree tree2, MesquiteNumber result, MesquiteString resultString) {
		if (result==null)
			return;
		clearResultAndLastResult(result);
		if (tree1 == null)
			return;
		if (tree2 == null)
			return;
		if (tree1.numberOfTerminalsInClade(tree1.getRoot()) == tree2.numberOfTerminalsInClade(tree2.getRoot())) {

			MesquiteInteger numCon = new MesquiteInteger(0);

			int numCladeTree1 = tree1.numberOfInternalsInClade(tree1.getRoot())-1;  //remove the root
			int numCladeTree2 = tree2.numberOfInternalsInClade(tree2.getRoot())-1;

			visitOriginal(tree1, tree1.getRoot(), tree2, numCon);
			if (tree1.getTerminalTaxaAsBits(tree1.getRoot()).equals(tree2.getTerminalTaxaAsBits(tree2.getRoot())))
				numCon.decrement();

			int numC = numCon.getValue();
			double fraction = 0.0;
			if (numCladeTree1+numCladeTree2>0)
				fraction  = (numC*2.0)/(numCladeTree1+numCladeTree2);
			

			if (verboseOutput.getValue()) {
				logln("\nnumber of clades in "+tree1.getName() + ": "+numCladeTree1);
				logln("number of clades in "+tree2.getName() + ": "+numCladeTree2);
				logln("number of clades in common: "+numC);
				logln("fraction: "+fraction);
			}

			result.setValue(fraction);
			if (resultString!=null) {
				resultString.setValue("Fraction Shared Clades: "+ result.toString());
			}
		} else {
			result.setValue(MesquiteDouble.inapplicable);
			if (resultString!=null) {
				resultString.setValue("Trees differ in size and cannot be compared.");
			}
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}

	/*.................................................................................................................*
    	 public String getParameters() {
		return "Shared Clades";
   	 }
	/*.................................................................................................................*/
	public String getName() {
		return "Fraction of Clades that are Shared";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Calculates the fraction of the total number of clades in two trees that are shared between the  trees (excludes the clade consisting of all taxa).";
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
}
