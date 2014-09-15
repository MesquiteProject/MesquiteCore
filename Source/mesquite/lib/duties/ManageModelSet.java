/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
public abstract class ManageModelSet extends CharSpecsSetManager {
	public boolean getSearchableAsModule(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
	}
	public abstract Class getModelClass();
	public abstract String getParadigm();
	public abstract ModelSet getNewModelSet(String name, int numChars, CharacterModel defaultModel, mesquite.lib.characters.CharacterData data);
	/*.................................................................................................................*/
 	public void fileElementAdded(FileElement element) {
		
 		if (element instanceof mesquite.lib.characters.CharacterData) {
	 		if (getProject()==null)
	 			MesquiteMessage.println("project is null in iMS " + getName());
			ModelSet currentModelSet;
	 		CharacterModel defaultModel=null;
	 		mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)element;
	 		if (data.getCurrentSpecsSet(getElementClass()) == null) {
		 		defaultModel =  (CharacterModel)data.getDefaultModel(getParadigm());
		 		//MesquiteMessage.println("Default model for " + data.getName() + " " + (defaultModel !=null));
		 		currentModelSet= getNewModelSet("UNTITLED", data.getNumChars(), defaultModel, data);
		 		currentModelSet.addToFile(element.getFile(), getProject(), null);
		 		data.setCurrentSpecsSet(currentModelSet, getElementClass());
		 	}
	 		if (getProject().getCharacterModels()==null)
	 			MesquiteMessage.println("charModels null in iMS Init Prob");
		 }
 		 
	}
	public Object getSpecification(String token){
		return getProject().getCharacterModel((token));
	}
	public void setSpecification(SpecsSet specsSet, Object specification, int ic){
		if (specsSet==null || getElementClass() == null || !(getElementClass().isAssignableFrom(specsSet.getClass())) || getModelClass() == null || specification == null || !(getModelClass().isAssignableFrom(specification.getClass())))
			return;
		ObjectSpecsSet sS = (ObjectSpecsSet)specsSet;
		sS.setProperty(specification,ic);
	}
	public SpecsSet getNewSpecsSet(String name, mesquite.lib.characters.CharacterData data){
		CharacterModel defaultModel =  data.getDefaultModel(getParadigm());
 		return getNewModelSet(name, data.getNumChars(), defaultModel, data);
	}
	public boolean appropriateBlockForWriting(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("MESQUITECHARMODELS");
	}
	public boolean appropriateBlockForReading(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("ASSUMPTIONS") || blockName.equalsIgnoreCase("MESQUITECHARMODELS");
	}
	public String nexusStringForSpecsSet(CharSpecsSet specsSet, mesquite.lib.characters.CharacterData data, MesquiteFile file, boolean isCurrent){
		return nexusStringForSpecsSetStandard(specsSet,data,file,isCurrent);
   	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new ModelsetNexusCommandTest(nexusToken());
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage " + lowerCaseTypeName() + "s";
   	 }
   	 
   	public boolean isSubstantive(){
   		return false;  
   	}
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages (including NEXUS read/write) " + lowerCaseTypeName() + "s (" + nexusToken() + "\'s)." ;
   	 }
	/*.................................................................................................................*/
   	 
}

class ModelsetNexusCommandTest  extends NexusCommandTest{
	String token;
	public ModelsetNexusCommandTest(String token){
		super();
		this.token = token;
	}
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("ASSUMPTIONS") || blockName.equalsIgnoreCase("MESQUITECHARMODELS")) && (commandName.equalsIgnoreCase(token)));
	}
}


