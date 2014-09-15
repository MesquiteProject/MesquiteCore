/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ConcatRespMatrices;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ConcatRespMatrices extends CharMatrixSource {
	MatrixSourceCoordObed dataTask1, dataTask2;
	/*.................................................................................................................*/
	public String getName() {
		return "Concatenate Respective Matrices from Two Sources";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies matrices by concatenating respective matrices from two sources.  The i'th matrix supplied is the concatenation of the i'th matrix from each of the two sources.  Assumptions like weights and character models are NOT transferred.  For categorical data, state names are not included.  For continuous data, new items may need to be created to accommodate differences in items between the matrices.";
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 260;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		int sad = CharacterSource.storedAsDefault;
		CharacterSource.storedAsDefault = -1;
		if (condition!=null) 
			dataTask1 = (MatrixSourceCoordObed)hireCompatibleEmployee( MatrixSourceCoordObed.class, condition, "First source of matrices for concatenation");
		else 
			dataTask1 = (MatrixSourceCoordObed)hireEmployee( MatrixSourceCoordObed.class, "First source of matrices for concatenation");
		CharacterSource.storedAsDefault = sad;
		if (dataTask1 == null) {
			return sorry(getName() + " can't be started because no first source of matrices was obtained");
		}
		CharacterSource.storedAsDefault = -1;
		if (condition!=null) 
			dataTask2 = (MatrixSourceCoordObed)hireCompatibleEmployee( MatrixSourceCoordObed.class, condition, "Second source of matrices for concatenation");
		else 
			dataTask2 = (MatrixSourceCoordObed)hireEmployee( MatrixSourceCoordObed.class, "Second source of matrices for concatenation");
		CharacterSource.storedAsDefault = sad;
		if (dataTask2 == null) {
			return sorry(getName() + " can't be started because no second source of matrices was obtained");
		}
		return true; 
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
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
		temp.addLine("getCharacterSource1 ", dataTask1);
		temp.addLine("getCharacterSource2 ", dataTask2);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns employee that is matrix source", null, commandName, "getCharacterSource1")) {
			return dataTask1;
		}
		else if (checker.compare(this.getClass(), "Returns employee that is matrix source", null, commandName, "getCharacterSource2")) {
			return dataTask2;
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		dataTask1.initialize(taxa);
	}

	/*.................................................................................................................*/
	public  int getNumberOfMatrices(Taxa taxa){
		int num1 =  dataTask1.getNumberOfMatrices(taxa); 
		int num2 =  dataTask2.getNumberOfMatrices(taxa); 
		if (MesquiteInteger.isCombinable(num1) && MesquiteInteger.isCombinable(num2))
			return MesquiteInteger.minimum(num1, num2);
		else if (MesquiteInteger.isCombinable(num1) )
			return num1;
		else
			return num2;
	}
	/*.................................................................................................................*/
	public String getMatrixName(Taxa taxa, int ic) {
		String name1 = dataTask1.getMatrixName(taxa, ic);
		String name2 = dataTask2.getMatrixName(taxa, ic);
		return "Concatenation of \"" + name1 + "\" and \"" + name2 +"\"";
	}
	/*.................................................................................................................*/
	public  MCharactersDistribution getMatrix(Taxa taxa, int im){
		currentMatrix = im;
		MCharactersDistribution matrix1 = dataTask1.getMatrix(taxa, im);
		MCharactersDistribution matrix2 = dataTask2.getMatrix(taxa, im);
		if (!matrix1.getStateClass().isAssignableFrom(matrix2.getStateClass())){
			discreetAlert("Sorry, can't concatenate because matrices are of different types");
			return null;
		}
		int numChars1 = matrix1.getNumChars();
		int numChars2 = matrix2.getNumChars();
		MAdjustableDistribution concat = matrix1.makeBlankAdjustable();
		concat.setSize(numChars1 + numChars2, taxa.getNumTaxa());
		CharacterState cs = null;
		for (int it=0; it<taxa.getNumTaxa(); it++){
			for (int ic=0; ic< numChars1; ic++){
				concat.setCharacterState(cs=matrix1.getCharacterState(cs, ic, it), ic, it);
			}
			for (int ic=0; ic< numChars2; ic++){
				concat.setCharacterState(cs=matrix2.getCharacterState(cs, ic, it), ic+ numChars1, it);
			}
		}
		String name1 = dataTask1.getMatrixName(taxa, im);
		String name2 = dataTask2.getMatrixName(taxa, im);
		concat.setName( "Concatenation of \"" + name1 + "\" and \"" + name2 +"\"");
		return concat;
	}
int currentMatrix= 0;
	public MCharactersDistribution getCurrentMatrix(Taxa taxa) {
		return getMatrix(taxa, currentMatrix);
	}

	public int getNumberCurrentMatrix() {
		return currentMatrix;
	}
}

