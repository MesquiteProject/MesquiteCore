/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.NoiseToBranchLengths;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/* TODO: put in options beyond simple Normal noise (e.g., binary?)  */
public class NoiseToBranchLengths extends RndTreeModifier {
	double variance = 0.1;
	MesquiteBoolean proport;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		proport = new MesquiteBoolean(true);
  		if (!MesquiteThread.isScripting()){
    	 		double s = queryVariance();
 	 		if (MesquiteDouble.isCombinable(s) && s>=0)
 	 			variance = s;
 	 		else
 				return false;
  		}
  		addMenuItem("Variance of Noise...", makeCommand("setNoise",  this));
		addCheckMenuItem(null,"Variance of Noise Proportional to Length", makeCommand("toggleProport",  this), proport);
  		return true;
  	 }
  	 private double queryVariance(){
		if (proport.getValue())
			return MesquiteDouble.queryDouble(containerOfModule(), "Variance of noise", "Enter the variance multiplier of noise to add to branch lengths (variance of noise = multiplier * branch length)", variance);
		else
			return MesquiteDouble.queryDouble(containerOfModule(), "Variance of noise", "Enter the variance of noise to add to branch lengths", variance);
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setNoise " + variance);
  	 	temp.addLine("toggleProport " + proport.toOffOnString());
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	if (checker.compare(this.getClass(), "Sets the variance of noise to add to branch lengths", "[real number]", commandName, "setNoise")) {
    	 		double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
    	 		if (!MesquiteDouble.isCombinable(s)){
    	 			s = queryVariance();
    	 		}
    	 		if (MesquiteDouble.isCombinable(s) && s>=0){
    	 			variance = s;
 					parametersChanged(); 
 				}
    	 	}
		else if (checker.compare(getClass(), "Sets whether variance is to be proportional to current branch length", null, commandName, "toggleProport")) {
    	 		proport.toggleValue(parser.getFirstToken(arguments));
 			parametersChanged(); 
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
	/*.................................................................................................................*/
	
	void addNoise(MesquiteTree tree, int node, RandomBetween rng, double sd){
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			addNoise(tree, d, rng, sd);
		double bL = tree.getBranchLength(node);		
		if (MesquiteDouble.isCombinable(bL)){ //add noise only if defined
			if (proport.getValue())
				bL += bL*rng.nextGaussian()*sd;
			else
				bL += rng.nextGaussian()*sd;
			if (bL<0)
				bL = 0;
			tree.setBranchLength(node, bL, false);
		}
	}
	/*.................................................................................................................*/
   	 public void modifyTree(Tree tree, MesquiteTree modified, RandomBetween rng){
   		if (tree == null || modified == null)
   			return;
		double sd = Math.sqrt(variance);
		addNoise(modified, modified.getRoot(), rng, sd);
		
   	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		if (proport.getValue())
   			return"Variance multiplier of noise added to branch lengths proportionately: " + variance;
   		else
   			return"Variance of noise added to branch lengths: " + variance;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Add Noise to Branch Lengths";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Adds noise to branch lengths of tree.  Noise is Normally distributed, with variance as selected.  By default the variance is be proportional to current branch length, so that if branch length is 10 and you've indicated a variance multiplier of 0.1, the noise added will have a variance of 1.0.  Negative branch lengths are not allowed, and are changed to zero.";
   	 }
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
   	 
}

