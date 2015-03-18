/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ManageCharsets;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/** Manages CHARSETs (not character matrices; see ManageCharacters), including reading the NEXUS command for CHARSETs */
public class ManageCharsets extends CharSpecsSetManager {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.lists.CharSetList.CharSetList.class, getName() + "  uses an assistant to display a list window.",
		"The assistant is arranged automatically");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
	}
	
	public void elementsReordered(ListableVector v){
	}
	public Class getElementClass(){
		return CharSelectionSet.class;
	}

	public String upperCaseTypeName(){
		return "Character Set";
	}
	public String lowerCaseTypeName(){
		return "character set";
	}
	public String nexusToken(){
		return "CHARSET";
	}
	public Object getSpecification(String token){
		return null;
	}
	public boolean hasSpecificationTokens(){
		return false;
	}
	public void setSpecification(SpecsSet specsSet, Object specification, int ic){
		if (specsSet==null || !(specsSet instanceof CharSelectionSet))
			return;
		CharSelectionSet sS = (CharSelectionSet)specsSet;
		sS.setSelected(ic, true);
	}
	public SpecsSet getNewSpecsSet(String name, CharacterData data){
 		CharSelectionSet selectionSet = new CharSelectionSet(name, data.getNumChars(), data);
		selectionSet.deselectAll(); 
		return selectionSet;
	}
	public boolean appropriateBlockForWriting(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("SETS");
	}
	public boolean appropriateBlockForReading(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS");
	}
   	
	public String nexusStringForSpecsSet(CharSpecsSet specsSet, CharacterData data, MesquiteFile file, boolean isCurrent){
			if (specsSet ==null || !(specsSet instanceof CharSelectionSet))
				return null;
			CharSelectionSet selectionSet = (CharSelectionSet)specsSet;
			String s= "";
			if (selectionSet!=null && (selectionSet.getFile()==file || (selectionSet.getFile()==null && data.getFile()==file))) {
				String sT = "";
				int continuing = 0;
				int lastWritten = -1;
				
				for (int ic=0; ic<data.getNumChars(); ic++) {
					if (selectionSet.isBitOn(ic)) {
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
						if (lastWritten != ic-1) {
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
				if (!StringUtil.blank(sT)) {
					s += "\tCHARSET ";
					if (isCurrent)
						s += "* ";
					s+= StringUtil.tokenize(selectionSet.getName()) + " ";
					if (file.getProject().getNumberCharMatrices()>1 || (!file.useSimplifiedNexus&& !data.hasDefaultName() && !NexusBlock.suppressTITLE))
						s+= " (CHARACTERS = " +  StringUtil.tokenize(data.getName()) + ")";
					s += "  =  " + sT + ";" + StringUtil.lineEnding();
				}
			}
			return s;
   	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new CharsetNexusCommandTest();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage character sets";
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages (including NEXUS read/write) character sets (CHARSETS)." ;
   	 }
	/*.................................................................................................................*/
   	 
}

class CharsetNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS"))&& commandName.equalsIgnoreCase("CHARSET"));
	}
}


