/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.AssignCodonPositions;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.align.lib.MultipleSequenceAligner;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.molec.lib.CodonPositionAssigner;

/* ======================================================================== */
public class AssignCodonPositions extends DNADataAlterer implements AltererMetadata {
	CodonPositionAssigner assigner = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		assigner= (CodonPositionAssigner)hireNamedEmployee(CodonPositionAssigner.class, arguments);
		if (assigner !=null) {
			assigner = (CodonPositionAssigner)hireNamedEmployee(CodonPositionAssigner.class, arguments);
			if (assigner == null)
				return sorry(getName() + " couldn't start because the requested assigner wasn't successfully hired.");
		}
		else {
			assigner = (CodonPositionAssigner)hireEmployee(MultipleSequenceAligner.class, "Codon Position Assigner");
			if (assigner == null)
				return sorry(getName() + " couldn't start because no assigner module obtained.");
		}
		return true;
	}
	public  Class getHireSubchoice(){
		return CodonPositionAssigner.class;
	}
	/*.................................................................................................................*/
	 public Snapshot getSnapshot(MesquiteFile file) { 
 	 	Snapshot temp = new Snapshot();
	 	temp.addLine("setAssigner ", assigner); 
	 	return temp;
	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
  	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
  	 	if (checker.compare(this.getClass(), "Sets the assigner", "[name of module]", commandName, "setAssigner")) {
  	 		CodonPositionAssigner temp = (CodonPositionAssigner)replaceEmployee(CodonPositionAssigner.class, arguments, "Codon position assigner", assigner);
			if (temp !=null){
				assigner = temp;
  	 			return assigner;
  	 		}
  	 	}
  	 	
		else return  super.doCommand(commandName, arguments, checker);
		return null;
 	 }

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
	//	this.table = table;
		if (assigner ==  null || data == null)
			return ResultCodes.INPUT_NULL;
		if (!(data instanceof DNAData)){
			MesquiteMessage.warnProgrammer("Can use " + getName() + " only on nucleotide data");
			return ResultCodes.INCOMPATIBLE_DATA;
		}
		CodonPositionsSet modelSet = (CodonPositionsSet) data.getCurrentSpecsSet(CodonPositionsSet.class);
		if (modelSet == null) {
			modelSet= new CodonPositionsSet("Codon Positions", data.getNumChars(), data);
			modelSet.addToFile(data.getFile(), getProject(), findElementManager(CodonPositionsSet.class)); //THIS
			data.setCurrentSpecsSet(modelSet, CodonPositionsSet.class);
		}
		int success = assigner.assignCodonPositions((DNAData)data,  modelSet);
		if (success==ResultCodes.SUCCEEDED)
			data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource
		
		return success;
	}
	

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Assign Codon Positions";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Assigns codon positions to selected sites." ;
	}

}

