/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.
 
 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org
 
 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.cont.XYMatrixToItems;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class XYMatrixToItems extends CharMatrixSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.charMatrices.StoredMatrices.StoredMatrices.class, getName() + "  needs a source of continuous matrices.",
		"The source of continuous matrices is arranged initially");
	}
	CharMatrixSource dataTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		
		dataTask = (CharMatrixSource)hireNamedEmployee(CharMatrixSource.class, "#StoredMatrices", ContinuousState.class);
		if (dataTask == null) {
			return sorry(getName() + " can't be started because no source of matrices was obtained");
		}
		return true; 
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
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
	NameReference xRef = NameReference.getNameReference("x");
	NameReference yRef = NameReference.getNameReference("y");

	private MCharactersDistribution translate(MCharactersDistribution mData){
		if (mData != null && mData.getParentData() != null){
			CharacterData data = mData.getParentData();
			if (data instanceof ContinuousData){
				ContinuousData dData = (ContinuousData)data;
				if (dData.getNumItems()>1){
					discreetAlert("Source matrix has multiple items; to convert to XY Items, the cells in the source need to each have a single item");
					return null;
				}
				int numChars = dData.getNumChars();
				MContinuousAdjustable xyMatrix = new MContinuousAdjustable (data.getTaxa(), numChars/2, data.getNumTaxa());
				Double2DArray xMatrix = xyMatrix.establishItem(xRef);
				Double2DArray yMatrix = xyMatrix.establishItem(yRef);
				for (int ic = 0; ic< numChars; ic+=2){
					int icXY = ic/2;
					for (int it = 0; it < dData.getNumTaxa(); it++){
						double stateX = dData.getState(ic, it, 0);
						double stateY = dData.getState(ic+1, it, 0);
						xMatrix.setValue(icXY, it, stateX);
						yMatrix.setValue(icXY, it, stateY);

					}
				}
				return xyMatrix;
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
		MCharactersDistribution xy = translate(orig);
		if (xy != null)
			xy.setName("XY compaction of " + orig.getName());
		return xy;
	}
	/** gets the indicated matrix.*/
	public  MCharactersDistribution getMatrix(Taxa taxa, int im){
		MCharactersDistribution orig = dataTask.getMatrix(taxa, im);
		MCharactersDistribution xy = translate(orig);
		if (xy != null)
			xy.setName("XY compaction of " + orig.getName());
		return xy;
	}
	/** gets name of the indicated matrix.*/
	public  String getMatrixName(Taxa taxa, int im){
		return "XY compaction of " + dataTask.getMatrixName(taxa, im);
	}
	/** returns the number of the current matrix*/
	public int getNumberCurrentMatrix(){
		return dataTask.getNumberCurrentMatrix();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Compact to (X,Y) matrix";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Converts a matrix of alternating X and Y columns (e.g. for landmarks) to a matrix each cell of which has X,Y values." ;
	}
	
}

