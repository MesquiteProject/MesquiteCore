/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.mb.BestBayesTree;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Supplies trees from tree blocks in a file.  Reads trees only when needed; hence suitable for files with too many trees to be held in memory at once, but slower than StoredTrees.*/
public class BestBayesTree extends TreeSource implements MesquiteListener {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.trees.ManyTreesFromFile.ManyTreesFromFile.class, getName() + "  needs a source of trees.",
		"The source of trees is arranged initially");
	}
	/*.................................................................................................................*/
	int bestTree = -1;
	String scoresPath = null;
	boolean loaded = false;
	String treename = null;
	TreeSource treeSource;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeSource = (TreeSource)hireNamedEmployee(TreeSource.class, "#ManyTreesFromFile");
		if (!MesquiteThread.isScripting())
			scoresPath = obtainPath();
		return true;
  	 }
  	 
	/*.................................................................................................................*/
  	 private String obtainPath(){
 		MainThread.incrementSuppressWaitWindow();
		 alert("In the following dialog, choose the \".p\" (scores & parameters) file from MrBayes.  Its associated .t file with trees must be in its same directory and otherwise with the same name.");
  		MesquiteFileDialog fdlg= new MesquiteFileDialog(containerOfModule(), "Choose scores file", FileDialog.LOAD);
			fdlg.setResizable(true);
			fdlg.setVisible(true);
				String fName=fdlg.getFile();
			String dName =fdlg.getDirectory();
			// fdlg.dispose();
			MainThread.decrementSuppressWaitWindow();
			return dName + fName;
  	 }
  	 private double loadFile(){ 
  	 	bestTree = -1;
  	 	if (scoresPath == null)
  	 		return MesquiteDouble.unassigned;
  	 	
		String[] lines = MesquiteFile.getFileContentsAsStrings(scoresPath);
		if (lines == null)
			return MesquiteDouble.unassigned;
		MesquiteInteger pos = new MesquiteInteger();
		double bestValue = -10000000000000000000.0;
		for (int i= 2; i< lines.length; i++){
			CommandRecord.tick("examining scores file line " + i);
			pos.setValue(0);
			int tree = MesquiteInteger.fromString(lines[i], pos);
			double score = MesquiteDouble.fromString(lines[i], pos);
			if (score > bestValue) {
				bestValue = score;
				bestTree = i-3;
			}
		}
		StringBuffer treeFile = new StringBuffer(scoresPath);
		treeFile.setCharAt(scoresPath.length()-1, 't');
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		MesquiteThread.setCurrentCommandRecord(new CommandRecord(true));
		treeSource.doCommand("setFilePath", StringUtil.tokenize(treeFile.toString()), CommandChecker.defaultChecker);
		MesquiteThread.setCurrentCommandRecord(cr);
		loaded = true;
  	 	return bestValue;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets path to scores file", null, commandName, "setScoresPath")) {
    	 		scoresPath = parser.getFirstToken(arguments);
    	 		loaded = false;
    	 		if (scoresPath != null)
    	 			scoresPath = MesquiteFile.composePath(getProject().getHomeDirectoryName(), scoresPath);
    	 		parametersChanged();
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
	return null;
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
  	public void setPreferredTaxa(Taxa taxa) {
  	}
	/*.................................................................................................................*/
  	public void initialize(Taxa taxa) {
  	//	checkTreeBlock(taxa);
  	}
  	
	/*.................................................................................................................*/
   	public Tree getCurrentTree(Taxa taxa) {
		double bestValue =  MesquiteDouble.unassigned;
		if (!loaded)
			bestValue = loadFile();
		if (bestTree>0){
			Tree t = treeSource.getTree(taxa, bestTree);
			if (t instanceof Attachable){
				double d = bestValue;
				if (MesquiteDouble.isCombinable(d)){
					MesquiteDouble s = new MesquiteDouble(-d);
					s.setName("MrBayesScore");
					((Attachable)t).attachIfUniqueName(s);
				}
			}
			return t;
		}
   		return null;
   	}
	/*.................................................................................................................*/
   	public Tree getTree(Taxa taxa, int itree) {
  		return getCurrentTree(taxa);
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
		return 1; 
   	}
   
	/*.................................................................................................................*/
   	public String getTreeNameString(Taxa taxa, int itree) {
   		return treename;
   	}
	/*.................................................................................................................*/
   	public String getCurrentTreeNameString(Taxa taxa) {
   		return treename;
   	}
	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    	public int getVersionOfFirstRelease(){
    		return 110;  
    	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "MrBayes Max. A-Post. Tree";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies tree from MrBayes with highest posterior probability (MAP tree).";
   	 }
	/*.................................................................................................................*/
   	public String getParameters() {
		return "Trees obtained from file " + scoresPath;
   	}
}


