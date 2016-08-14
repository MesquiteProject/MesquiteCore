/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.AlteredMatrix;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.charMatrices.lib.*;

/* ======================================================================== */
public class AlteredMatrix extends SourceModifiedMatrix {  //So that Altered Matrices are exposed to Make New Matrix From>Alter Matrix>
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DataAlterer.class, getName() + " needs a method to alter matrices.",
				"You can request how matrices are altered either initially, or later under the Alter Matrix submenu.");
	}
	MCharactersDistribution matrix;
	long currentModification;
	DataAlterer altererTask;
	MesquiteString altererName;
	MesquiteCommand stC;
	CharacterData tempData;
	protected boolean createdNewDataObject;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		if (!super.startJob(arguments, condition, hiredByName))
 			return false;
 		if (arguments ==null)
 			altererTask = (DataAlterer)hireEmployee(DataAlterer.class, "Alterer of data");
	 	else {
	 		altererTask = (DataAlterer)hireNamedEmployee(DataAlterer.class, arguments);
 			if (altererTask == null)
 				altererTask = (DataAlterer)hireEmployee(DataAlterer.class, "Alterer of data");
 		}
 		if (altererTask == null) {
 			return sorry(getName() + " couldn't start because no data alterer was obtained.");
 		}
 	 	stC = makeCommand("setAlterer",  this);
 	 	altererTask.setHiringCommand(stC);
 		altererName = new MesquiteString();
	 	  altererName.setValue(altererTask.getName());
		if (numModulesAvailable(DataAlterer.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Matrix Alterer", stC, DataAlterer.class);
 			mss.setSelected(altererName);
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
	public  Class getHireSubchoice(){
		return DataAlterer.class;
	}
	public void endjob(){
		if (createdNewDataObject && tempData!=null) {
			tempData.dispose();
			tempData=null;
		}
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setAlterer ", altererTask); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the matrix modifier", "[name of module]", commandName, "setAlterer")) {
    	 		DataAlterer temp = (DataAlterer)replaceEmployee(DataAlterer.class, arguments, "Alterer of data", altererTask);
				if (temp !=null){
					altererTask = temp;
	    	 			altererName.setValue(altererTask.getName());
			 	 	altererTask.setHiringCommand(stC);
					parametersChanged();
	    	 			return altererTask;
	    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
   	 }
  	 
	/*.................................................................................................................*/
  	private MCharactersDistribution getM(Taxa taxa){
 			
		MCharactersDistribution matrix = getBasisMatrix(taxa); //TODO: shouldn't have to do this for every matrix, just first after change of currentMatrix
		if (matrix==null)
			return null;
//		MAdjustableDistribution modified = matrix.makeBlankAdjustable();
		createdNewDataObject = matrix.getParentData()==null; 
		CharacterData data = CharacterData.getData(this,  matrix, taxa); 
		tempData = data.cloneData();
		if (createdNewDataObject)  
			data.dispose();  

		
	   	altererTask.alterData(tempData, null, null);
		String origName = null;
		
   		if (matrix.getName()!=null)
   			origName = "matrix " + matrix.getName();
   		else if (matrix.getParentData() != null)
   			origName = "matrix " + matrix.getParentData().getName();
   		else
   			origName = "unknown matrix";

   		tempData.setName( "Alteration of  " + origName + " by " + altererTask.getName());
  		return tempData.getMCharactersDistribution();
   	}
	/*.................................................................................................................*/
    	 public String getMatrixName(Taxa taxa, int ic) {
   		return "Alteration of matrix by " + altererTask.getName();
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
		return "Matrix modification: " + origName;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
   		return "Alter Matrix";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	public boolean isSubstantive(){
   		return true;  
   	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies character matrices that are altered from an existing matrix." ;
   	 }
   	 
}

