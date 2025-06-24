/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.ManageCodeSets;
/*~~  */


import java.util.Enumeration;

import mesquite.lib.EmployeeNeed;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.NexusCommandTest;
import mesquite.lib.ObjectSpecsSet;
import mesquite.lib.SpecsSet;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharSpecsSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.characters.ModelSet;
import mesquite.lib.duties.CharSpecsSetManager;
import mesquite.molec.lib.GenCodeModelSet;

/* ======================================================================== */
public class ManageCodeSets extends CharSpecsSetManager {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.molec.CodesetList.CodesetList.class, getName() + "  uses an assistant to display a list window.",
		"The assistant is arranged automatically");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
	}
	
	
	public void elementsReordered(ListableVector v){
	}
	/*.................................................................................................................*/
	public Class getElementClass(){
		return GenCodeModelSet.class;
	}
	public String upperCaseTypeName(){
		return "Genetic Code Set";
	}
	public String lowerCaseTypeName(){
		return "genetic code set";
	}
	public String nexusToken(){
		return "CODESET";
	}
	public String alternativeNexusToken(){
		return "GENCODESET";
	}
	public Object getSpecification(String token){
		return getProject().getCharacterModel((token));
	}
	public void setSpecification(SpecsSet specsSet, Object specification, int ic){
		if (specsSet==null || !(specsSet instanceof GenCodeModelSet) || !(specification instanceof CharacterModel))
			return;
		ObjectSpecsSet sS = (ObjectSpecsSet)specsSet;
		sS.setProperty(specification,ic);
	}
	public SpecsSet getNewSpecsSet(String name, CharacterData data){
		CharacterModel defaultModel =  data.getDefaultModel("GeneticCode");
 		return new GenCodeModelSet(name, data.getNumChars(), defaultModel, data);
	}
	public boolean appropriateBlockForWriting(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("CODONS");
	}
	public boolean appropriateBlockForReading(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("CODONS") || blockName.equalsIgnoreCase("ASSUMPTIONS");
	}
	public String nexusStringForSpecsSet(CharSpecsSet specsSet, CharacterData data, MesquiteFile file, boolean isCurrent){
		if (specsSet ==null || !(specsSet instanceof GenCodeModelSet))
			return null;
		ModelSet modelSet = (ModelSet)specsSet;
			String s= "";
			if (modelSet !=null && (modelSet.getFile()==file || (modelSet.getFile()==null && data.getFile()==file))) {
				String sT = " ";
				boolean firstTime = true;
				Enumeration enumeration = file.getProject().getCharacterModels().elements();
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
				if (!StringUtil.blank(sT)) {
					s+= "\tCODESET " ;
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
    		/*.................................................................................................................*/
    	 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
    	 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
    	 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    	    	public int getVersionOfFirstRelease(){
    	    		return 110;  
    	    	}
    	    	/*.................................................................................................................*/
    	    	public boolean isPrerelease(){
    	    		return true;
    	    	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage genetic code sets";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages (including NEXUS read/write) genetic code sets (CODESETs)." ;
   	 }
	/*.................................................................................................................*/
   	 
}

class TypesetNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("CODONS") || blockName.equalsIgnoreCase("ASSUMPTIONS")) && commandName.equalsIgnoreCase("CODESET"));
	}
}


