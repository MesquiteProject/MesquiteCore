/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ReshuffleCharacter;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ReshuffleCharacter extends CharacterSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DataSearcher.class, getName() + " needs a source of the original character to be reshuffled.",
				"The source of the original character is chosen initially.");
	}
	long currentShuffle=0;
	int currentOriginalChar = -1;  //to force a query
	MatrixSourceCoord dataTask;
	MCharactersDistribution matrix;
	CharacterDistribution states;
	int currentDataSet = 0;
	RandomBetween randomTaxon;
	MesquiteLong seed;
	Taxa oldTaxa =null;
		long originalSeed=System.currentTimeMillis(); //0L;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		if (condition!=null)
			dataTask = (MatrixSourceCoord)hireCompatibleEmployee( MatrixSourceCoord.class, condition, "Source of matrices for Reshuffle Character");
 		else
			dataTask = (MatrixSourceCoord)hireEmployee( MatrixSourceCoord.class, "Source of matrices for Reshuffle Character");
		if (dataTask == null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
   		currentShuffle = 0;
	 	randomTaxon= new RandomBetween(originalSeed);
    	 	seed = new MesquiteLong(1);
    	 	seed.setValue(originalSeed);
    	
		addMenuItem("Shuffle Next Character", makeCommand("shuffleNext",  this));
		addMenuItem("Shuffle Previous Character", makeCommand("shufflePrevious",  this));
		addMenuItem("Choose Character to Shuffle", makeCommand("chooseCharacter",  this));
  		addMenuItem("Set Seed (Reshuffle character)...", makeCommand("setSeed",  this));
	 	return true;
  	 }
  	 
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("getCharacterSource ", dataTask);
  	 	temp.addLine("setCharacter " + CharacterStates.toExternal(currentOriginalChar));
  	 	temp.addLine("setShuffle " + CharacterStates.toExternal((int)currentShuffle));
   	 	temp.addLine("setSeed " + originalSeed);
	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Does shuffle of the next character", null, commandName, "shuffleNext")) {
    	 		if (currentOriginalChar>=matrix.getNumChars()-1)
    	 			currentOriginalChar=0;
    	 		else
    	 			currentOriginalChar++;
    	 		currentShuffle = 0;
	 		((AdjustableDistribution)states).setParentCharacter(currentOriginalChar);
			parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Indicates which shuffle of the character", null, commandName, "setShuffle")) {
    	 		long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
    	 		if (s >= 0 && MesquiteLong.isCombinable(s) && s != currentShuffle){
    	 			currentShuffle = s;
				parametersChanged();
			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the source of matrices on which to do ordinations", null, commandName, "setCharacterSource")) { //TEMPORARY for data files using old system without coordinators
    	 		if (dataTask != null)
    	 			return dataTask.doCommand(commandName, arguments, checker);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns employee that is character source", null, commandName, "getCharacterSource")) {
    	 		return dataTask;
    	 	}
     	 	else if (checker.compare(this.getClass(), "Sets the random number seed to that passed", "[long integer seed]", commandName, "setSeed")) {
    	 		long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
    	 		if (!MesquiteLong.isCombinable(s)){
    	 			s = MesquiteLong.queryLong(containerOfModule(), "Random number seed", "Enter an integer value for the random number seed for character reshuffling", originalSeed);
    	 		}
    	 		if (MesquiteLong.isCombinable(s)){
    	 			originalSeed = s;
 				parametersChanged(); //?
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Queries user about which character to shuffle", null, commandName, "chooseCharacter")) {
			int ic = chooseCharacter(matrix);
    	 		if (ic >= 0 && MesquiteInteger.isCombinable(ic)) {
    	 			currentOriginalChar = ic;
 	   	 		currentShuffle = 0;
		 		((AdjustableDistribution)states).setParentCharacter(currentOriginalChar);
				parametersChanged();
 			}
    	 	}
  
    	 	else if (checker.compare(this.getClass(), "Sets which character to shuffle", "[character number]", commandName, "setCharacter")) {
    	 		MesquiteInteger pos = new MesquiteInteger(0);
    	 		int icNum = MesquiteInteger.fromString(arguments, pos);
 	    	 	seed.setValue(originalSeed);
   	 		if (!MesquiteInteger.isCombinable(icNum))
    	 			return null;
    	 		int ic = CharacterStates.toInternal(icNum);
    	 		if (matrix == null || ((ic>=0) && (ic<=matrix.getNumChars()-1))) {
    	 			currentOriginalChar = ic;
 	   	 		currentShuffle = 0;
		 		if (states !=null)
		 			((AdjustableDistribution)states).setParentCharacter(currentOriginalChar);
				parametersChanged();
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Does shuffle of the previous character", null, commandName, "shufflePrevious")) {
    	 		if (currentOriginalChar<=0)
    	 			currentOriginalChar=matrix.getNumChars()-1;
    	 		else
    	 			currentOriginalChar--;
    	 		currentShuffle = 0;
	 		((AdjustableDistribution)states).setParentCharacter(currentOriginalChar);
			parametersChanged();
    	 	}
    	 	else 
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	 
	private int chooseCharacter(MCharactersDistribution matrix){
		if (matrix != null && matrix.getParentData()!=null) {
	 		CharacterData data = matrix.getParentData();
			String[] charNames = new String[data.getNumChars()];
			for (int i=0; i<data.getNumChars(); i++)
				charNames[i]= data.getCharacterName(i);
 			return ListDialog.queryList(containerOfModule(), "Choose character", "Choose character to shuffle", MesquiteString.helpString,charNames, 0);
 		}
	 		
		return MesquiteInteger.queryInteger(containerOfModule(), "Choose character", "Number of character to shuffle ", 1);
 	}
	/*.................................................................................................................*/
   	public CharacterDistribution getCharacter(Taxa taxa, int ic) {
   		dataCheck(taxa);
   		if (matrix == null)
   			return null;
   		currentShuffle = ic;
   		if (currentOriginalChar<matrix.getNumChars()&& currentOriginalChar>=0 && currentShuffle>=0) {
   			CharacterDistribution chs = matrix.getCharacterDistribution(currentOriginalChar);
   			if (chs == null)
   				return null;
			states = (CharacterDistribution)chs.getAdjustableClone(); 
			if (states == null)
				return null;
	 		((AdjustableDistribution)states).setParentCharacter(currentOriginalChar);
			String mName = "";
			if (matrix.getParentData() != null)
				mName = " of " + matrix.getParentData().getName();
   			states.setName( "Shuffle " + currentShuffle + " of character " + CharacterStates.toExternal(currentOriginalChar) + mName);  
			
			randomTaxon.setSeed(originalSeed); 
	   		for (int i=0; i < currentShuffle; i++)
	   			randomTaxon.nextInt();
	   		randomTaxon.setSeed(randomTaxon.nextInt() + 1); //v. 1. 1 Oct 05, modified by adding 1 to prevent adjacent from simply being offsets
	   		
	   		int nT1 = states.getNumTaxa()-1;
	   		for (int i=0; i < nT1; i++) {
	   			int sh = randomTaxon.randomIntBetween(i, nT1);
	   			if (i!=sh)
	   				((AdjustableDistribution)states).tradeStatesBetweenTaxa(i, sh);
	   		}
	   		return states;
	   	}
   		else
   			return null;
   	}
	/*.................................................................................................................*/
   	public int getNumberOfCharacters(Taxa taxa) {
   		dataCheck(taxa);
   		if (matrix == null) 
   			return 0;
   		else 
   			return MesquiteInteger.infinite;
   	}
	/*.................................................................................................................*/
   	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
   	 	matrix = null;
   	 	super.employeeParametersChanged( employee,  source, notification);
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		dataCheck(taxa);
   	}
	/*.................................................................................................................*/
   	private void dataCheck(Taxa taxa) {
   		if (matrix==null  || oldTaxa != taxa) {
   			matrix = dataTask.getCurrentMatrix(taxa);
   			if (matrix == null)
   	   			currentOriginalChar = 0;  
   			else if (currentOriginalChar<0 || currentOriginalChar>= matrix.getNumChars()) {
	   			if (!MesquiteThread.isScripting())
	   				currentOriginalChar = chooseCharacter(matrix);
	   			if (!MesquiteInteger.isCombinable(currentOriginalChar) || currentOriginalChar<0 || currentOriginalChar>=matrix.getNumChars())
	   	   			currentOriginalChar = 0;  
			}
   			currentShuffle = 0;
   			oldTaxa = taxa;
   		}
   	}
	/*.................................................................................................................*/
   	/** returns the name of character ic*/
   	public String getCharacterName(Taxa taxa, int ic){
      		return  "Shuffle " + ic + " of character ";
   	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		if (matrix==null) return "";
		return "Character reshuffle: " + matrix.getName() + ". [seed: " + originalSeed + "]";
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Reshuffle Character";
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
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies characters that are reshufflings of an existing character." ;
   	 }
	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new CharacterStateTest();
  	 }
}

