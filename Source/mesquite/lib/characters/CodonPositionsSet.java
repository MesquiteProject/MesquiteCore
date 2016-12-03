/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
  	
	int endSequenceByThree(int targetPos, int numChars, int ic, int mainCount, boolean[] include, MesquiteInteger charNumberOfLastThird){
		int unassignedPosition=4;
		int previousThird = mainCount; 
		charNumberOfLastThird.setValue(ic);  //store the char number of the previous end of the triplets
		int count=mainCount;
		int ik=0;
		for (ik = ic+1; ik< numChars; ik++){
			if (include==null || ik>=include.length || include[ik]){
				count++;
				int thisPos = getInt(ik);
				if (thisPos == targetPos || (thisPos==MesquiteInteger.unassigned && (targetPos==unassignedPosition))) {
					//is a match; if not modulus 3 on from ic then return previousThird 
					if ((count-mainCount) % 3 !=0){
						return previousThird;
					}
					else {
						charNumberOfLastThird.setValue(ik);  //store the char number of the previous end of the triplets
						previousThird = count;
					}
				}
				else {
					//is not a match; if modulus 3 on from ic then return previousThird 
					if ((count-mainCount) % 3 ==0){
						return previousThird;
					}
				}
			}
		}
		return previousThird;
	}
  	public String getListOfMatches(int targetPos){
  		return getListOfMatches(targetPos, 0, null);
  	}
 	public String getListOfMatches(int targetPos, int offset, boolean[] include){
 		return getListOfMatches(targetPos, offset, include, false);
 	}
 	public String getListOfMatches(int targetPos, int offset, boolean[] include, boolean writeCommas){  //offset is number to add to character numbers
 		//this had been badly broken in 310 (commas written everywhere...
 		int lastWritten = -1;
		int unassignedPosition=4;
		String list = "";
		int continuing = 0;
		int count = -1;
		boolean writeSeparator = false;
		MesquiteInteger charNumberOfLastThird = new MesquiteInteger(-1);
		for (int ic=0; ic<getNumberOfParts(); ic++) {
			if (include==null || ic>=include.length || include[ic]){   // it's one to consider
				count++;
				int thisPos = getInt(ic);
				if (thisPos == targetPos || (thisPos==MesquiteInteger.unassigned && (targetPos==unassignedPosition))) {
					if (continuing == 0) { 
						//first, check to see if there is a series of thirds....
						int lastThird = endSequenceByThree(targetPos, getNumberOfParts(), ic, count, include, charNumberOfLastThird);
						//if so, then go the series of thirds 
						if (lastThird != count){
							if (writeSeparator) {
								list += ", ";
								writeSeparator=false;
							}

							list += " " + CharacterStates.toExternal(count+offset) + " - " +  CharacterStates.toExternal(lastThird+offset) + "\\3";
							ic = charNumberOfLastThird.getValue();
							count = lastThird;
						
						}
						else { //otherwise write as normal*/
							if (writeSeparator) {
								list += ", ";
								writeSeparator=false;
							}
							lastWritten = count;
							list += " " + CharacterStates.toExternal(count+offset);
							continuing = 1;
						}
					}
					else if (continuing == 1) {  // we know there are at least two in a row of the same thing
						list += "-";
						continuing = 2;   //now set it so that we are waiting for the last one in the series
					}
				}
				else if (continuing>0) {   // we are in a contiguous stretch of the same thing
					if (lastWritten != count-1){  // last one we wrote wasn't the one just before
						if (writeSeparator) {
							list += ", ";
							writeSeparator=false;
						}
						list += " " + CharacterStates.toExternal(count-1+offset);
						lastWritten = count-1;
					}
					else {
						if (writeCommas)
							writeSeparator=true;
						lastWritten = -1;
					}
					continuing = 0;
				}
			}
		}
		if (continuing>1) {  // we are waiting for the last one
			if (writeSeparator) {
				list += ", ";
				writeSeparator=false;
			}
			list += " " + CharacterStates.toExternal(count+offset) + " ";
			//thisValueString += " " + CharacterStates.toExternal(data.getNumChars()-1+offset) + " ";
		}
		return list;
 	}
}


