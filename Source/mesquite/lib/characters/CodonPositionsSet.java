/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import java.awt.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;

/*======================================================================== */
/** a designation of codon positions for characters. */
public class CodonPositionsSet  extends CharNumSet {
	
	public CodonPositionsSet (String name, int numChars, CharacterData data) {
		super(name, numChars, new MesquiteNumber(0), data);
	}
	public String getTypeName(){
		return "Codon Positions set";
	}
	public SpecsSet cloneSpecsSet(){
		CodonPositionsSet ms = new CodonPositionsSet(new String(name), getNumberOfParts(),  data);
		MesquiteNumber position = new MesquiteNumber();
		for (int i=0; i< getNumberOfParts(); i++) {
			placeValue(i, position);
			ms.setValue(i, position);
		}
		return ms;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof CharacterData))
			return null;
		return new CodonPositionsSet("Codon Positions", numParts, (CharacterData)parent);
	}
 	/*.................................................................................................................*/
	/** Add num parts just after "starting" (filling with default values)  */
  	public boolean addParts(int starting, int num){  
		boolean success = super.addParts(starting, num);
		MesquiteNumber position = new MesquiteNumber(0);
		if (starting >=0 && getInt(starting)!=MesquiteInteger.unassigned)
			
		for (int i=0; i< num; i++) 
			setValue(i+starting+1, position);
		
		return success;
	}
  	
	int endSequenceByThree(int targetPos, int numChars, int ic){
		int unassignedPosition=4;
		int previousThird = ic;
		for (int ik = ic+1; ik< numChars; ik++){
			int thisPos = getInt(ik);
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
  	public String getListOfMatches(int targetPos){
  		return getListOfMatches(targetPos, 0);
  	}
  	
 	public String getListOfMatches(int targetPos, int offset){  //offset is number to add to character numbers
		int lastWritten = -1;
		int unassignedPosition=4;
		String thisValueString = "";
		int continuing = 0;
		for (int ic=0; ic<getNumberOfParts(); ic++) {
			int thisPos = getInt(ic);
			if (thisPos == targetPos || (thisPos==MesquiteInteger.unassigned && (targetPos==unassignedPosition))) {
				if (continuing == 0) {
					//first, check to see if there is a series of thirds....
					int lastThird = endSequenceByThree(targetPos, getNumberOfParts(), ic);
					//if so, then go the series of thirds 
					if (lastThird != ic){
						thisValueString += " " + CharacterStates.toExternal(ic+offset) + " - " +  CharacterStates.toExternal(lastThird+offset) + "\\3";
						ic = lastThird;
					}
					else { //otherwise write as normal*/
						lastWritten = ic;
						thisValueString += " " + CharacterStates.toExternal(ic+offset);
						continuing = 1;
					}
				}
				else if (continuing == 1) {
					thisValueString += "-";
					continuing = 2;
				}
			}
			else if (continuing>0) {
				if (lastWritten != ic-1){
					thisValueString += " " + CharacterStates.toExternal(ic-1+offset);
					lastWritten = ic-1;
				}
				else
					lastWritten = -1;
				continuing = 0;
			}

		}
		if (continuing>1) {
			thisValueString += " " + CharacterStates.toExternal(data.getNumChars()-1+offset) + " ";
		}
		return thisValueString;
 	}
}


