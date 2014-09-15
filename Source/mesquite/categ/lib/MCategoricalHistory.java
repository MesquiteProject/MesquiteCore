/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**DOCUMENT*/
public class MCategoricalHistory extends MCategoricalAdjustable implements MCharactersHistory  {
	int numNodes;
	public MCategoricalHistory(Taxa taxa, int numChars, int numNodes) {
		super(taxa, numChars,  numNodes);
		this.numNodes = numNodes;
	}
	public int getNumNodes(){
		return numNodes;
	}
	/*..........................................  MCategoricalHistory  ..................................................*/
	/**return CharacterHistory object for character ic */
	/** extract the states of character ic and return as CharacterHistory*/
	public  CharacterHistory getCharacterHistory (int ic){
		CategoricalHistory cH = new CategoricalHistory(getTaxa(), getNumNodes());
		if (frequenciesExist()){
			cH.setNumFreqCategories(getNumFreqCateg());
		}
		if (extraFrequenciesExist()){
			cH.setNumExtraFreqCategories(getNumExtraFreqCateg());
		}
		for (int j=0; j<getNumNodes(); j++){
			cH.setState(j, getState(ic, j)); 
			if (frequenciesExist()){
				for (int categ = 0; categ< getNumFreqCateg(); categ++)
					cH.setFrequency(j, categ, getFrequency(ic, j, categ));
			}
			if (extraFrequenciesExist()){
				for (int categ = 0; categ< getNumExtraFreqCateg(); categ++)
					cH.setExtraFrequency(j, categ, getExtraFrequency(ic, j, categ));
			}
		}
		return cH;
	}
	
	/*..........................................  MCategoricalHistory  ..................................................*/
	/**obtain states of character ic from passed CharacterHistory object */
	public void transferFrom(int ic, CharacterHistory s) {
		if (s instanceof CategoricalHistory) {
			CategoricalHistory cat = (CategoricalHistory)s;
			for (int j=0; j<numNodes; j++) {
				setState(ic, j, cat.getState(j));
				if (cat.frequenciesExist()) {
					setFrequencies(ic, j, cat.getFrequencies(j));
				}
				if (cat.extraFrequenciesExist()) {
					setExtraFrequencies(ic, j, cat.getExtraFrequencies(j));
				}
			}
		}
	}
	/** returns the union of all state sets*/
	public long getAllStates() {  //
		long s=0;
		for (int i=0; i< 4; i++) {
			s = CategoricalState.addToSet(s, i);
		}
		return s;
	}
}

