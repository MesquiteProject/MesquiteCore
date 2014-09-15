/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.ManageProbModelSets;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ManageProbModelSets extends CharSpecsSetManager {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.stochchar.ProbModelSetList.ProbModelSetList.class, getName() + "  uses an assistant to display a list window.",
		"The assistant is arranged automatically");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
	}
  	 public boolean isPrerelease(){
  	 	return false;
  	 }
	
	/*.................................................................................................................*/
 	public void fileElementAdded(FileElement element) {
		
 		if (element instanceof CharacterData) {
	 		if (getProject()==null)
	 			System.out.println("project is null in iMS Init Prob");
			ProbabilityModelSet currentLikelihoodModels;
	 		CharacterModel defaultModel=null;
	 		CharacterData data = (CharacterData)element;
	 		if (data.getCurrentSpecsSet(ProbabilityModelSet.class) == null) {
		 		defaultModel =  data.getDefaultModel("Likelihood");
		 		currentLikelihoodModels= new ProbabilityModelSet("UNTITLED", data.getNumChars(), defaultModel, data);
		 		currentLikelihoodModels.addToFile(element.getFile(), getProject(), null);
		 		data.setCurrentSpecsSet(currentLikelihoodModels, ProbabilityModelSet.class);
		 	}
	 		if (getProject().getCharacterModels()==null)
	 			System.out.println("charModels null in iMS Init Prob");
		 }
 		 
	}
	/*.................................................................................................................*/
	public void elementsReordered(ListableVector v){
	}
	public Class getElementClass(){
		return ProbabilityModelSet.class;
	}
	public String upperCaseTypeName(){
		return "Probability Model Set";
	}
	public String lowerCaseTypeName(){
		return "probability model set";
	}
	public String nexusToken(){
		return "ProbModelSet";
	}
	public String alternativeNexusToken(){
		return "MODELSET";
	}
	public Object getSpecification(String token){
		return getProject().getCharacterModel((token));
	}
	public void setSpecification(SpecsSet specsSet, Object specification, int ic){
		if (specsSet==null || !(specsSet instanceof ProbabilityModelSet) || !(specification instanceof CharacterModel))
			return;
		ObjectSpecsSet sS = (ObjectSpecsSet)specsSet;
		sS.setProperty(specification,ic);
	}
	public SpecsSet getNewSpecsSet(String name, CharacterData data){
		CharacterModel defaultModel =  data.getDefaultModel("Probability");
 		return new ProbabilityModelSet(name, data.getNumChars(), defaultModel, data);
	}
	public boolean appropriateBlockForWriting(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("MESQUITECHARMODELS");
	}
	public boolean appropriateBlockForReading(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS") || blockName.equalsIgnoreCase("MESQUITECHARMODELS");
	}
	/*.................................................................................................................*/
	public String nexusStringForSpecsSet(CharSpecsSet specsSet, CharacterData data, MesquiteFile file, boolean isCurrent){
		return nexusStringForSpecsSetStandard(specsSet,data,file,isCurrent);
   	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new ModelsetNexusCommandTest();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage probability model sets";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages (including NEXUS read/write) probability model sets (PROBMODELSETs)." ;
   	 }
	/*.................................................................................................................*/
   	 
}

class ModelsetNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS") || blockName.equalsIgnoreCase("MESQUITECHARMODELS")) && (commandName.equalsIgnoreCase("MODELSET") || commandName.equalsIgnoreCase("PROBMODELSET")));
	}
}


