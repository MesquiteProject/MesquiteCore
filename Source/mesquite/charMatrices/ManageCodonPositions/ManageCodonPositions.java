/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ManageCodonPositions;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/** Manages specifications of codon positions, including reading the NEXUS command for CODONPOSSET */
public class ManageCodonPositions extends CharSpecsSetManager {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.lists.CodePosSetsList.CodePosSetsList.class, getName() + "  uses an assistant to display a list window.",
		"The assistant is arranged automatically");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}

	public void elementsReordered(ListableVector v){
	}
	public Class getElementClass(){
		return CodonPositionsSet.class;
	}
	public String upperCaseTypeName(){
		return "Codon Positions Set";
	}
	public String lowerCaseTypeName(){
		return "codon positions";
	}
	public String nexusToken(){
		return "CODONPOSSET";
	}
	public Object getSpecification(String token){
		if (token == null)
			return null;
		MesquiteNumber num = new MesquiteNumber();
		if (token.length() == 1 && (token.charAt(0) == 'N' || token.charAt(0) == 'n'))
			num.setValue(0); 
		else
			num.setValue(token);
		return num;
	}
	public void setSpecification(SpecsSet specsSet, Object specification, int ic){
		if (specsSet==null || !(specsSet instanceof CodonPositionsSet) || !(specification instanceof MesquiteNumber))
			return;
		CodonPositionsSet characterPartition = (CodonPositionsSet)specsSet;
		characterPartition.setValue(ic, (MesquiteNumber)specification);
	}
	public SpecsSet getNewSpecsSet(String name, CharacterData data){
		return new CodonPositionsSet(name, data.getNumChars(), data);
	}
	public boolean appropriateBlockForReading(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("CODONS");
	}
	public boolean appropriateBlockForWriting(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("CODONS");
	}
	int endSequenceByThree(CodonPositionsSet codSet, int targetPos, int numChars, int ic){
		int unassignedPosition=4;
		int previousThird = ic;
		for (int ik = ic+1; ik< numChars; ik++){
			int thisPos = codSet.getInt(ik);
			if (thisPos == targetPos || (thisPos==MesquiteInteger.unassigned && (targetPos==unassignedPosition))) {
				//is a match; if not modulus 3 on from ic then return previousThird 
				if ((ik-ic) % 3 !=0)
					return previousThird;
				else
					previousThird = ik;
			}
			else {
				//is not a match; if modulus 3 on from ic then return previousThird 
				if ((ik-ic) % 3 ==0)
					return previousThird;
			}

		}
		return previousThird;
	}
	/*.................................................................................................................*/
	public String OLDnexusStringForSpecsSet(CharSpecsSet specsSet, CharacterData data, MesquiteFile file, boolean isCurrent){
		if (specsSet ==null || !(specsSet instanceof CodonPositionsSet))
			return null;
		CodonPositionsSet codSet = (CodonPositionsSet)specsSet;
		String s= "";
		int unassignedPosition=4;
		if (codSet !=null && (codSet.getFile()==file || (codSet.getFile()==null && data.getFile()==file))) {
			String sT = " ";
			String thisValueString = "";
			boolean firstTime = true;
			int numChars = data.getNumChars();
			MesquiteNumber position = new MesquiteNumber();
			boolean someValues=false;
			for (int targetPos = 0; targetPos<5; targetPos++){
				position.setValue(targetPos);
				int continuing = 0;
				thisValueString = "";
				if (!firstTime)
					thisValueString = ", ";
				if (targetPos == 0)
					thisValueString += "N: ";
				else if (targetPos == unassignedPosition)
					thisValueString += "?: ";
				else
					thisValueString += targetPos + ":" ;
				int lastWritten = -1;
				someValues=false;
				for (int ic=0; ic<numChars; ic++) {
					int thisPos = codSet.getInt(ic);
					if (thisPos == targetPos || (thisPos==MesquiteInteger.unassigned && (targetPos==unassignedPosition))) {
						if (continuing == 0) {
							//first, check to see if there is a series of thirds....
							int lastThird = endSequenceByThree(codSet, targetPos, numChars, ic);
							//if so, then go the series of thirds 
							if (lastThird != ic){
								thisValueString += " " + CharacterStates.toExternal(ic) + " - " +  CharacterStates.toExternal(lastThird) + "\\3";
								ic = lastThird;
								someValues = true;
							}
							else { //otherwise write as normal*/
								lastWritten = ic;
								thisValueString += " " + CharacterStates.toExternal(ic);
								continuing = 1;
								someValues = true;
							}
						}
						else if (continuing == 1) {
							thisValueString += "-";
							continuing = 2;
							someValues = true;
						}
					}
					else if (continuing>0) {
						if (lastWritten != ic-1){
							thisValueString += " " + CharacterStates.toExternal(ic-1);
							lastWritten = ic-1;
							someValues = true;
						}
						else
							lastWritten = -1;
						continuing = 0;
					}

				}
				if (continuing>1) {
					thisValueString += " " + CharacterStates.toExternal(data.getNumChars()-1) + " ";
					someValues = true;
				}
				if (someValues) {
					sT += thisValueString;
					firstTime = false;
				}
			} 
			if (!StringUtil.blank(sT)) {
				s+= "\tCODONPOSSET " ;
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(codSet.getName()) + " ";
				if (file.getProject().getNumberCharMatrices()>1 && MesquiteFile.okToWriteTitleOfNEXUSBlock(file, data))
					s+= " (CHARACTERS = " +  StringUtil.tokenize(data.getName()) + ")";
				s+= " = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	public String nexusStringForSpecsSet(CharSpecsSet specsSet, CharacterData data, MesquiteFile file, boolean isCurrent){
		if (specsSet ==null || !(specsSet instanceof CodonPositionsSet))
			return null;
		CodonPositionsSet codSet = (CodonPositionsSet)specsSet;
		String s= "";
		int unassignedPosition=4;
		if (codSet !=null && (codSet.getFile()==file || (codSet.getFile()==null && data.getFile()==file))) {
			String sT = " ";
			String thisValueString = "";
			boolean firstTime = true;
			MesquiteNumber position = new MesquiteNumber();
			for (int targetPos = 0; targetPos<5; targetPos++){
				position.setValue(targetPos);
				thisValueString = "";
				if (!firstTime)
					thisValueString = ", ";
				if (targetPos == 0)
					thisValueString += "N: ";
				else if (targetPos == unassignedPosition)
					thisValueString += "?: ";
				else
					thisValueString += targetPos + ":" ;
				String locs = codSet.getListOfMatches(targetPos);
				if (!StringUtil.blank(locs)) {
					sT += thisValueString + locs;
					firstTime = false;
				}
			} 
			if (!StringUtil.blank(sT)) {
				s+= "\tCODONPOSSET " ;
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(codSet.getName()) + " ";
				if (writeLinkWithCharacterMatrixName(file, data))
					s+= " (CHARACTERS = " +  StringUtil.tokenize(data.getName()) + ")";
				s+= " = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new PosNexusCommandTest();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Manage codon positions";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages (including NEXUS read/write) codon position sets." ;
	}
	/*.................................................................................................................*/

}

/* ======================================================================== */
class PosNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("CODONS")) && commandName.equalsIgnoreCase("CODONPOSSET"));
	}
}


