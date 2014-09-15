/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.RandomModifMatrix;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.charMatrices.lib.*;

/* ======================================================================== */
public class RandomModifMatrix extends SourceModifiedMatrix {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(RandomMatrixModifier.class, getName() + " needs a method to randomly modify matrices.",
				"You can request how matrices are randomly modified either initially, or later under the Random Modifier of Matrix submenu.");
	}
	MCharactersDistribution matrix;
	long currentModification;
	RandomBetween rng;
	MesquiteLong seed;
	long originalSeed=System.currentTimeMillis(); //0L;
	RandomMatrixModifier modifierTask;
	MesquiteString modifierName;
	MesquiteCommand stC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		if (!super.startJob(arguments, condition, hiredByName))
 			return false;
 		if (arguments ==null)
 			modifierTask = (RandomMatrixModifier)hireEmployee(RandomMatrixModifier.class, "Random modifier of matrix");
	 	else {
	 		modifierTask = (RandomMatrixModifier)hireNamedEmployee(RandomMatrixModifier.class, arguments);
 			if (modifierTask == null)
 				modifierTask = (RandomMatrixModifier)hireEmployee(RandomMatrixModifier.class, "Random modifier of matrix");
 		}
 		if (modifierTask == null) {
 			return sorry(getName() + " couldn't start because no random matrix modifier was obtained.");
 		}
 	 	stC = makeCommand("setModifier",  this);
 	 	modifierTask.setHiringCommand(stC);
 		modifierName = new MesquiteString();
	 	  modifierName.setValue(modifierTask.getName());
		if (numModulesAvailable(RandomMatrixModifier.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Random Modifier of Matrix", stC, RandomMatrixModifier.class);
 			mss.setSelected(modifierName);
  		}
 	 	seed = new MesquiteLong(1);
 	 	seed.setValue(originalSeed);
  		addMenuItem("Set Seed (Random matrix modification)...", makeCommand("setSeed",  this));
	 	rng= new RandomBetween(originalSeed);
  	 	return true; 
  	 }
	/*.................................................................................................................*/
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new CharacterStateTest();
  	 }
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return RandomMatrixModifier.class;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setSeed " + originalSeed);
  	 	temp.addLine("setModifier ", modifierTask); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the matrix modifier", "[name of module]", commandName, "setModifier")) {
				RandomMatrixModifier temp = (RandomMatrixModifier)replaceEmployee(RandomMatrixModifier.class, arguments, "Random modifier of matrix", modifierTask);
				if (temp !=null){
					modifierTask = temp;
	    	 			modifierName.setValue(modifierTask.getName());
			 	 	modifierTask.setHiringCommand(stC);
					parametersChanged();
	    	 			return modifierTask;
	    	 		}
    	 	}
    	 	 else	if (checker.compare(this.getClass(), "Sets the random number seed", "[long integer seed]", commandName, "setSeed")) {
    	 		 long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
    	 		 if (!MesquiteLong.isCombinable(s)){
    	 			 s = MesquiteLong.queryLong(containerOfModule(), "Random number seed", "Enter an integer value for the random number seed for random matrix modification", originalSeed);
    	 		 }
    	 		 if (MesquiteLong.isCombinable(s)){
    	 			 originalSeed = s;
    	 			 parametersChanged(); //?
    	 		 }
    	 	 }
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
   	 }
  	 
	/*.................................................................................................................*/
  	private MCharactersDistribution getM(Taxa taxa){
		rng.setSeed(originalSeed); 
		long rnd =originalSeed;  //v. 1. 12 this had been 0 and thus first would always use seed 1
   		for (int i=0; i < currentModification; i++)
   			rnd = rng.nextInt();
		rng.setSeed(rnd+1);
  		seed.setValue(rnd + 1);  //v. 1. 1, October 05: changed so as to avoid two adjacent trees differing merely by a frameshift of random numbers
 			
		MCharactersDistribution matrix = getBasisMatrix(taxa); //TODO: shouldn't have to do this for every matrix, just first after change of currentMatrix
		if (matrix==null)
			return null;
		MAdjustableDistribution modified = matrix.makeBlankAdjustable();
		
	   	modifierTask.modifyMatrix(matrix, modified, rng);
		String origName = null;
		
   		if (matrix.getName()!=null)
   			origName = "matrix " + matrix.getName();
   		else if (matrix.getParentData() != null)
   			origName = "matrix " + matrix.getParentData().getName();
   		else
   			origName = "unknown matrix";

   		modified.setName( "Modification #" + (currentModification+1) + " of  " + origName + " by " + modifierTask.getName());
  		return modified;
   	}
	/*.................................................................................................................*/
    	 public String getMatrixName(Taxa taxa, int ic) {
   		return "Modification #" + (ic+1) + " of  matrix by " + modifierTask.getName();
   	 }
	/*.................................................................................................................*/
  	public MCharactersDistribution getCurrentMatrix(Taxa taxa){
   		return getM(taxa);
   	}
	/*.................................................................................................................*/
  	public MCharactersDistribution getMatrix(Taxa taxa, int im){
   		currentModification = im;
   		return getM(taxa);
   	}
	/*.................................................................................................................*/
    	public  int getNumberOfMatrices(Taxa taxa){
    		return MesquiteInteger.infinite; //TODO: convert all these to MesquiteInteger.infinite
    	}
	/*.................................................................................................................*/
   	/** returns the number of the current matrix*/
   	public int getNumberCurrentMatrix(){
   		return (int)currentModification;
   	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		if (matrix==null) return "";
   		String origName = "";
   		if (matrix.getName()!=null)
   			origName = "matrix " + matrix.getName();
   		else if (matrix.getParentData() != null)
   			origName = "matrix " + matrix.getParentData().getName();
   		else
   			origName = "unknown matrix";
		return "Matrix modification: " + origName + ". [seed: " + originalSeed + "]";
   	}
	/*.................................................................................................................*/
    	 public String getName() {
   		return "Randomly Modify Matrix";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
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
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies character matrices that are randomly modified from an existing matrix." ;
   	 }
   	 
}

