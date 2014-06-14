/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
 Version 2.75, September 2011.
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.
 
 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org
 
 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.CondensedPatternMatrix;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.ParsimonyModelSet;
import mesquite.categ.lib.*;

/*  module initiated by David Maddison 
 * */
public class CondensedPatternMatrix extends CharMatrixSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.charMatrices.StoredMatrices.StoredMatrices.class, getName() + "  needs a source of categorical matrices.",
		"The source of categorical matrices is arranged initially");
	}
	CharMatrixSource dataTask;
	CharWeightSet weightSet =null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		
		dataTask = (CharMatrixSource)hireNamedEmployee(CharMatrixSource.class, "#StoredMatrices", CategoricalState.class);
		if (dataTask == null) {
			return sorry(getName() + " can't be started because no source of matrices was obtained");
		}
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
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("getCharacterSource ", dataTask);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns employee that is matrix source", null, commandName, "getCharacterSource")) {
			return dataTask;
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		//return null;
	}
	
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	 happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		dataTask.initialize(taxa);
	}
	
	/** Does the core condensation of the matrix.  Note that it makes a copy of the matrix and then condenses that.
	  It also creates a weight set that stores in it the number of times that pattern appeared in the matrix.
	 * */
	private MCharactersDistribution condense(MCharactersDistribution mData){
		if (mData != null && mData.getParentData() != null){
			CharacterData data = mData.getParentData();
				
			if (data instanceof CategoricalData){
				CategoricalData condensedData  = ((CategoricalData)data).getDataCopy();
				int numChars = data.getNumChars();
				CharWeightSet weightSet= new CharWeightSet("Frequency of Patterns", data.getNumChars(), data);  // making a weight set
				weightSet.addToFile(getProject().getHomeFile(), getProject(), findElementManager(CharWeightSet.class)); //attaching the weight set to a file

				for (int ic = 0; ic< numChars; ic++)
					weightSet.setValue(ic,1);
				
				for (int ic = 0; ic< numChars; ic++){
					for (int oic = ic+1; oic< numChars; oic++){
						if (weightSet.getInt(oic)!=0 && condensedData.samePattern(oic, ic)){  //if weight zero already been here and found it
							int curWeight = weightSet.getInt(ic);
							weightSet.setValue(ic,curWeight+1);
							weightSet.setValue(oic,0);
							condensedData.clearCharacter(oic);
						}
					}
				}

				condensedData.removeCharactersThatAreEntirelyGaps(false);
			
				// delete the extra parts of the weightset
				int i = numChars-1;
				while (i>=0) {
					if (weightSet.getInt(i)==0){
						weightSet.deleteParts(i, 1);
					}
					i--;
				}
				
				
				// now re-sort by frequency
				int newNumChars = condensedData.getNumChars();
				for (int ic=-1; ic<newNumChars; ic++){
					int bestWeight = 1;
					int bestCharacter = -1;
					for (int ic2=ic+1; ic2<newNumChars; ic2++){
						int current = weightSet.getInt(ic2);
						if (current>bestWeight){
							bestWeight=current;
							bestCharacter=ic2;
						}
					}
					if (bestCharacter>=0 && ic<newNumChars){
						weightSet.swapParts(ic+1, bestCharacter);
						condensedData.swapParts(ic+1, bestCharacter);
					}
				}
				condensedData.setCurrentSpecsSet(weightSet, CharWeightSet.class); 

								
				logln("\nFrequencies of commonest patterns:");
				for (int ic = 0; ic< weightSet.getNumberOfParts() && ic<20; ic++){
					logln(" "+(ic+1)+": " + MesquiteDouble.toStringDigitsSpecified(((1.0*weightSet.getInt(ic))/numChars), 4));
				}
				logln("\n[To see the frequencies of all patterns, go to the List of Characters window for this pattern matrix and choose Columns>Current Weights to see the weights applied to each character.]");
			
				return condensedData.getMCharactersDistribution();
			}
		}
		return null;
		
	}
	
	/** returns the number of character matrices that can be supplied for the given taxa*/
	public int getNumberOfMatrices(Taxa taxa){
		return dataTask.getNumberOfMatrices(taxa);
	}
	/** gets the current matrix.*/
	public  MCharactersDistribution getCurrentMatrix(Taxa taxa){
		MCharactersDistribution orig = dataTask.getCurrentMatrix(taxa);
		MCharactersDistribution condensedData = condense(orig);
		CharacterData data = condensedData.getParentData();
		if (data!=null)
			data.setName("Patterns in " + orig.getName());
		return condensedData;
	}
	/** gets the indicated matrix.*/
	public  MCharactersDistribution getMatrix(Taxa taxa, int im){
		MCharactersDistribution orig = dataTask.getMatrix(taxa, im);
		MCharactersDistribution condensedData = condense(orig);
		CharacterData data = condensedData.getParentData();
		if (data!=null)
			data.setName("Patterns in " + orig.getName());
		return condensedData;
	}
	/** gets name of the indicated matrix.*/
	public  String getMatrixName(Taxa taxa, int im){
		return "Patterns in " + dataTask.getMatrixName(taxa, im);
	}
	/** returns the number of the current matrix*/
	public int getNumberCurrentMatrix(){
		return dataTask.getNumberCurrentMatrix();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Condense to Matrix of Patterns";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Condenses a categorical matrix to one having exactly one character of each pattern with that character weighted by the number of copies of that pattern in the original matrix." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

	/*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }

}

