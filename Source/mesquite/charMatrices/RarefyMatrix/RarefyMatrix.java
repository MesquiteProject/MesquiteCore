/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.RarefyMatrix;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.charMatrices.lib.*;

/* ======================================================================== */
public class RarefyMatrix extends RandomMatrixModifier {
	int numDeleted=1;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		if (!MesquiteThread.isScripting()){
    	 	int s = MesquiteInteger.queryInteger(containerOfModule(), "Number of characters to delete", "Enter the number of randomly chosen characters to delete", numDeleted);
 	 		if (MesquiteInteger.isCombinable(s) && s >=0)
 	 			numDeleted = s;
 	 		else
 				return false;
  		}
  		addMenuItem("Number of Characters Deleted...", makeCommand("setNumberDeleted",  this));
  	 	return true; 
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setNumberDeleted " + numDeleted);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	if (checker.compare(this.getClass(), "Sets the number of randomly chosen characters to delete", "[number]", commandName, "setNumberDeleted")) {
    	 		int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
    	 		if (!MesquiteInteger.isCombinable(s) && !MesquiteThread.isScripting()){
    	 			s = MesquiteInteger.queryInteger(containerOfModule(), "Number of characters to delete", "Enter the number of randomly chosen characters to delete", numDeleted);
    	 		}
    	 		if (MesquiteInteger.isCombinable(s) && s>=0){
    	 			numDeleted = s;
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
		if (matrix==null || modified == null || numDeleted < 0)
			return;
		int numTaxa = matrix.getNumTaxa();
		int numChars = matrix.getNumChars();
   		int numTK = numChars-numDeleted;
   		if (numTK<0)
   			numTK = 0;
   			
		boolean[] toKeep = randomBits(numChars, numTK, rng);
		if (modified.getNumTaxa()!=numTaxa || modified.getNumChars()!=numTK)
			modified.setSize(numTK, numTaxa);
		int count = 0;
		for (int ic = 0; ic<numChars; ic++) {
	   		if (toKeep[ic])
	   			modified.transferFrom(count++, matrix.getCharacterDistribution(ic));
 	 	}
   	}
	boolean[] randomBits(int total, int on, RandomBetween rng){
		boolean[] bits = new boolean[total];
   		for (int i=0; i < total; i++)
   			bits[i] = (i<on);
		
   		for (int i=0; i < (total-1); i++) { //randomly scramble
   			int sh = rng.randomIntBetween(i, total-1);
   			if (i!=sh){
	   			boolean bsh = bits[sh];
	   			bits[sh] = bits[i];
	   			bits[i]=bsh;
   			}
   		}
   		return bits;
	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		return"Number of characters deleted randomly: " + numDeleted;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
   		return "Rarefy Characters";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
  	/*.................................................................................................................*/
 	 public boolean requestPrimaryChoice() {
		return true;
  	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Deletes characters randomly to rarefy matrix." ;
   	 }
   	 
}

