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

/* ======================================================================== */
/**A CharacterPartition is a specification of which property applies to each character (CHARPARTITION in NEXUS 1 file format).
  */

public class CharacterPartition  extends CharObjectSpecsSet {
	public CharacterPartition (String name, int numChars, Object defaultProperty, CharacterData data) {
		super(name, numChars, defaultProperty, data);
	}
	
	public SpecsSet cloneSpecsSet(){
		CharacterPartition ms = new CharacterPartition(new String(name), getNumberOfParts(), (CharactersGroup)getDefaultProperty(), data);
		for (int i=0; i<getNumberOfParts(); i++)
			ms.setProperty(getProperty(i), i);
		return ms;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof CharacterData))
			return null;
		return new CharacterPartition("Partition", numParts, getDefaultProperty(), (CharacterData)parent);
	}
	public String getTypeName(){
		return "Character partition";
	}

	public CharactersGroup getCharactersGroup(int part){
		return (CharactersGroup)getProperty(part);
	}
	/**Returns an array of all the partitions for all the charactes*/
	public CharactersGroup[] getGroups(){
		CharactersGroup[] temp = new CharactersGroup[getNumberOfParts()];
		int next = 0;
		for (int i=0; i<getNumberOfParts(); i++) {
			CharactersGroup mq = getCharactersGroup(i);
			if (mq!=null) {
				if (ObjectArray.indexOf(temp, mq)<0){
					temp[next++]=mq;
				}
			}
		}
		if (next==0)
			return null;
		CharactersGroup[] result = new CharactersGroup[next];
		for (int i=0; i<result.length; i++)
			result[i]=temp[i];
		
		return result;
	}
 	/*.................................................................................................................*/
	/**/
	public int getNumberInGroup(CharactersGroup target){
		int num =0;
		for (int i=0; i<getNumberOfParts(); i++) {
			CharactersGroup mq = getCharactersGroup(i);
			if (mq==target)
				num++;
		}
		return num;
	}
 	/*.................................................................................................................*/
	/**/
	public boolean getAnyCurrentlyUnassigned(){
		for (int i=0; i<getNumberOfParts(); i++) {
			CharactersGroup mq = getCharactersGroup(i);
			if (mq==null)
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
 	/** gets storage for set of properties*/
	public Object[] getNewPropertyStorage(int numParts){
		return new CharactersGroup[numParts];
	}
 	/*.................................................................................................................*/
 	/** return array of character properties (can also get indivitually using getProperty)*/
	public CharactersGroup[] getPartitions() {
		CharactersGroup[] p = new CharactersGroup[getNumChars()];
		for (int i=0; i<getNumChars(); i++) {
			p[i] =(CharactersGroup)getProperty(i);
		}
		return p;
	}
	

}


