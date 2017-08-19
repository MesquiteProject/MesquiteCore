/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.ManageTypesets;
/*~~  */

import java.awt.*;
import java.util.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class ManageTypesets extends CharSpecsSetManager {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.parsimony.TypesetList.TypesetList.class, getName() + "  uses an assistant to display a list window.",
		"The assistant is arranged automatically");
	}
	public String getName() {
		return "Manage parsimony model sets";
	}
	public String getExplanation() {
		return "Manages (including NEXUS read/write) parsimony model sets (TYPESETs)." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}


	public void elementsReordered(ListableVector v){
	}
	/*.................................................................................................................*/
	public Class getElementClass(){
		return ParsimonyModelSet.class;
	}
	public String upperCaseTypeName(){
		return "Parsimony Model Set";
	}
	public String lowerCaseTypeName(){
		return "parsimony model set";
	}
	public String nexusToken(){
		return "TYPESET";
	}
	public String alternativeNexusToken(){
		return "PARSMODELSET";
	}
	public Object getSpecification(String token){
		return getProject().getCharacterModel((token));
	}
	public void setSpecification(SpecsSet specsSet, Object specification, int ic){
		if (specsSet==null || !(specsSet instanceof ParsimonyModelSet) || !(specification instanceof CharacterModel))
			return;
		ObjectSpecsSet sS = (ObjectSpecsSet)specsSet;
		sS.setProperty(specification,ic);
	}
	public SpecsSet getNewSpecsSet(String name, CharacterData data){
		CharacterModel defaultModel =  data.getDefaultModel("Parsimony");
		return new ParsimonyModelSet(name, data.getNumChars(), defaultModel, data);
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
		ModelSet modelSet = (ModelSet)specsSet;
		String sT = " ";
		boolean firstTime = true;
		Enumeration enumeration = data.getProject().getCharacterModels().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			CharacterModel cm = (CharacterModel)obj;
			String q = ListableVector.getListOfMatches(modelSet.getModels(), cm, CharacterStates.toExternal(0));
			if (q != null) {
				if (!firstTime)
					sT += ", ";
				sT += StringUtil.tokenize(cm.getNEXUSName()) + ": " + q ;
				firstTime = false;
			}
		}

		return sT;
	}
	public String nexusStringForSpecsSet(CharSpecsSet specsSet, CharacterData data, MesquiteFile file, boolean isCurrent){
		if (specsSet ==null || !(specsSet instanceof ParsimonyModelSet))
			return null;
		ModelSet modelSet = (ModelSet)specsSet;
		String s= "";
		if (modelSet !=null && (modelSet.getFile()==file || (modelSet.getFile()==null && data.getFile()==file))) {
			String sT = nexusCoreStringForSpecsSet(specsSet,data);
			if (!StringUtil.blank(sT)) {
				s+= "TYPESET " ;
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(modelSet.getName()) + " ";
				if (writeLinkWithCharacterMatrixName(file, data))
					s+= " (CHARACTERS = " +  StringUtil.tokenize(data.getName()) + ")";
				s+= "  = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new TypesetNexusCommandTest();
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return false;
	}

}

class TypesetNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS")) && commandName.equalsIgnoreCase("TYPESET"));
	}
}


