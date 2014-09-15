/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SprinkleMissing;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.charMatrices.lib.*;

/* ======================================================================== */
public class SprinkleMissing extends RandomMatrixModifier {
	double probMissing=0.1;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		if (!MesquiteThread.isScripting()){
    	 		double s = MesquiteDouble.queryDouble(containerOfModule(), "Probability of Missing", "Enter the probability that a cell will be converted to missing data", probMissing, 0, 1);
 	 		if (MesquiteDouble.isCombinable(s))
 	 			probMissing = s;
 	 		else
 				return false;
  		}
  		addMenuItem("Probability of Missing Data...", makeCommand("setProb",  this));
  	 	return true; 
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setProb " + probMissing);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	if (checker.compare(this.getClass(), "Sets the probability that a cell will be converted to missing data", "[number]", commandName, "setProb")) {
    	 		MesquiteInteger pos = new MesquiteInteger(0);
    	 		double s = MesquiteDouble.fromString(arguments, pos);
    	 		if (!MesquiteDouble.isCombinable(s)){
    	 			s = MesquiteDouble.queryDouble(containerOfModule(), "Probability of Missing", "Enter the probability that a cell will be converted to missing data", probMissing, 0, 1);
    	 		}
    	 		if (MesquiteDouble.isCombinable(s)){
    	 			probMissing = s;
 				if (!MesquiteThread.isScripting())
 					parametersChanged(); 
 			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
	/*.................................................................................................................*/
  	public void modifyMatrix(MCharactersDistribution matrix, MAdjustableDistribution modified, RandomBetween rng){
		if (matrix==null || modified == null)
			return;
		int numTaxa = matrix.getNumTaxa();
		int numChars = matrix.getNumChars();
		int count = 0;
		for (int ic = 0; ic<numChars; ic++) //first, make copy
			modified.transferFrom(ic, matrix.getCharacterDistribution(ic));
		
		//next, sprinkle on missing data
		CharacterState cs = matrix.getCharacterState(null, 0, 0);
		cs.setToUnassigned();
		for (int it = 0; it<numTaxa; it++) 
			for (int ic = 0; ic<numChars; ic++) {
		   		if (rng.nextDouble()<probMissing)
		   			modified.setCharacterState(cs, ic, it);
	 	 	}
   	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		return"Probability of missing data: " + probMissing;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
   		return "Sprinkle Missing";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }
  	/*.................................................................................................................*/
 	 public boolean requestPrimaryChoice() {
		return true;
  	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Randomly converts entries in the character matrix to missing data, with a certain probability (i.e., it \"sprinkles\" missing data around the matrix)." ;
   	 }
   	 
}

