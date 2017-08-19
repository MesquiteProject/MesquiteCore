/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ManageCharInclusion;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/** Manages specification of character inclusion/exclusion, including reading/writing EXSETs in NEXUS file*/
public class ManageCharInclusion extends CharSpecsSetManager {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.lists.CharInclSetList.CharInclSetList.class, getName() + "  uses an assistant to display a list window.",
		"The assistant is arranged automatically");
	}
	public void elementsReordered(ListableVector v){
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
	}
	public boolean isPrerelease(){
		return false;
	}
	public Class getElementClass(){
		return CharInclusionSet.class;
	}
	public String upperCaseTypeName(){
		return "Character Inclusion Set";
	}
	public String lowerCaseTypeName(){
		return "character inclusion set";
	}
	public String nexusToken(){
		return "EXSET";
	}
	public Object getSpecification(String token){
		return null;
	}
	public boolean hasSpecificationTokens(){
		return false;
	}
	public void setSpecification(SpecsSet specsSet, Object specification, int ic){
		if (specsSet==null || !(specsSet instanceof CharInclusionSet))
			return;
		CharInclusionSet sS = (CharInclusionSet)specsSet;
		sS.setSelected(ic, false);
	}
	public SpecsSet getNewSpecsSet(String name, CharacterData data){
 		CharInclusionSet inclusionSet = new CharInclusionSet(name, data.getNumChars(), data);
		inclusionSet.selectAll(); //default is selected (EXSET deselects, i.e. excludes, some characters
		return inclusionSet;
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
		CharInclusionSet inclusionSet = (CharInclusionSet)specsSet;
		String sT = "";
		int continuing = 0;
		int lastWritten = -1;
		for (int ic=0; ic<data.getNumChars(); ic++) {
			if (!inclusionSet.isBitOn(ic)) {
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
				if (lastWritten !=ic-1){
					sT += " " + CharacterStates.toExternal(ic-1);
					lastWritten = ic-1;
				}
				else
					lastWritten = -1;
				continuing = 0;
			}

		}
		if (continuing>1)
			sT += " " + CharacterStates.toExternal(data.getNumChars()-1);

		return sT;
	}

	public String nexusStringForSpecsSet(CharSpecsSet specsSet, CharacterData data, MesquiteFile file, boolean isCurrent){
			if (specsSet ==null || !(specsSet instanceof CharInclusionSet))
				return null;
			CharInclusionSet inclusionSet = (CharInclusionSet)specsSet;
			String s= "";
			if (inclusionSet!=null && (inclusionSet.getFile()==file || (inclusionSet.getFile()==null && data.getFile()==file))) {
				String sT = nexusCoreStringForSpecsSet(specsSet, data);
				s += "EXSET ";
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(inclusionSet.getName()) + " ";
				if (writeLinkWithCharacterMatrixName(file, data))
					s+= " (CHARACTERS = " +  StringUtil.tokenize(data.getName()) + ")";
				s += " = " + sT + ";" + StringUtil.lineEnding();
				
			}
			return s;
   	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new CharsetNexusCommandTest();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage character inclusion sets";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages (including NEXUS read/write) character inclusion sets (EXSETS)." ;
   	 }
	/*.................................................................................................................*/
   	 
}

class CharsetNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS")) && commandName.equalsIgnoreCase("EXSET"));
	}
}


