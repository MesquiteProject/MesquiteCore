/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ManageCharWeights;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/** Manages specifications of character weights, including reading the NEXUS command for WTSETs */
public class ManageCharWeights extends CharSpecsSetManager {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.lists.WeightSetList.WeightSetList.class, getName() + "  uses an assistant to display a list window.",
		"The assistant is arranged automatically");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
	}
	
	public void elementsReordered(ListableVector v){
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	public Class getElementClass(){
		return CharWeightSet.class;
	}
	public String upperCaseTypeName(){
		return "Character Weight Set";
	}
	public String lowerCaseTypeName(){
		return "character weight set";
	}
	public String nexusToken(){
		return "WTSET";
	}
	public Object getSpecification(String token){
		MesquiteNumber num = new MesquiteNumber();
		num.setValue(token);
		return num;
	}
	public void setSpecification(SpecsSet specsSet, Object specification, int ic){
		if (specsSet==null || !(specsSet instanceof CharWeightSet) || !(specification instanceof MesquiteNumber))
			return;
		CharWeightSet characterWts = (CharWeightSet)specsSet;
		characterWts.setValue(ic, (MesquiteNumber)specification);
	}
	public SpecsSet getNewSpecsSet(String name, CharacterData data){
		return new CharWeightSet(name, data.getNumChars(), data);
	}
	public boolean appropriateBlockForWriting(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("ASSUMPTIONS");
	}
	public boolean appropriateBlockForReading(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS");
	}
	
	public static String nexusCoreStringForSpecsSet(CharSpecsSet specsSet, CharacterData data){
		CharWeightSet wtSet = (CharWeightSet)specsSet;
		String sT = " ";
		int numChars = data.getNumChars();
		NumberArray distinctWeights = new NumberArray(numChars);
		distinctWeights.deassignArray();
		MesquiteNumber weight = new MesquiteNumber();
		MesquiteNumber secondWeight = new MesquiteNumber();
		boolean firstTime = true;
		for (int iw = 0; iw<numChars; iw++){
			wtSet.placeValue(iw, weight);
			if (distinctWeights.findValue(weight)<0){
				int continuing = 1;
				distinctWeights.setOpenValue(weight);
				int lastWritten = -1;
				if (!firstTime)
					sT += ", ";
				firstTime = false;
				sT += weight.toString() + ": " + CharacterStates.toExternal(iw);
				for (int ic=iw+1; ic<data.getNumChars(); ic++) {
					wtSet.placeValue(ic, secondWeight);
					if (secondWeight.equals( weight)) {
						if (continuing == 0) {
							sT += " " + CharacterStates.toExternal(ic);
							lastWritten = ic;
							continuing = 1;
						}
						else if (continuing == 1) {
							sT += " - ";
							continuing = 2;
						}
					}
					else if (continuing>0) {
						if (lastWritten!= ic-1) {
							sT += " " + CharacterStates.toExternal(ic-1);
							lastWritten = ic-1;
						}
						else
							lastWritten = -1;
						continuing = 0;
					}

				}
				if (continuing>1)
					sT += " " + CharacterStates.toExternal(data.getNumChars()-1) + " ";
			}
		} 
		return sT;
	}

	/*.................................................................................................................*/
	public String nexusStringForSpecsSet(CharSpecsSet specsSet, CharacterData data, MesquiteFile file, boolean isCurrent){
		if (specsSet ==null || !(specsSet instanceof CharWeightSet))
			return null;
		CharWeightSet wtSet = (CharWeightSet)specsSet;
		String s= "";
		if (wtSet !=null && (wtSet.getFile()==file || (wtSet.getFile()==null && data.getFile()==file))) {
			
			String sT = nexusCoreStringForSpecsSet(specsSet, data);
			
			if (!StringUtil.blank(sT)) {
				s+= "WTSET " ;
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(wtSet.getName()) + " ";
				if (writeLinkWithCharacterMatrixName(file, data))
					s+= " (CHARACTERS = " +  StringUtil.tokenize(data.getName()) + ")";
				s+= " = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
   	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new WtsetNexusCommandTest();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage character weight sets";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages (including NEXUS read/write) character weight sets." ;
   	 }
	/*.................................................................................................................*/
   	 
}

/* ======================================================================== */
class WtsetNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS")) && commandName.equalsIgnoreCase("WTSET"));
	}
}


