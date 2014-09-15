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
public class MDNAHistory extends MCategoricalHistory  {
	public MDNAHistory(Taxa taxa, int numChars, int numNodes) {
		super(taxa, numChars,  numNodes);
	}
	/*..........................................  MDNAHistory  ..................................................*/
	/**return CharacterHistory object for character ic */
	/** extract the states of character ic and return as CharacterHistory*/
	public  CharacterHistory getCharacterHistory (int ic){
		CategoricalHistory cH = new DNACharacterHistory(getTaxa(), getNumNodes());
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
	
	/*..........................................    ..................................................*/
	/**return CharacterDistribution object for character ic */
	public CharacterDistribution getCharacterDistribution (int ic){
		DNACharacterAdjustable soc = new DNACharacterAdjustable(getTaxa(), numTaxa);
		for (int it = 0; it<numTaxa; it++)
			soc.setState(it, getState(ic, it));
		soc.setParentData(getParentData());
		soc.setParentCharacter(ic);
		if (getParentData()!=null)
			soc.setName("Character " + ic + " of " + getParentData().getName());
		else
			soc.setName("Character " + ic );
		return soc;
	}
	/*..........................................................*/
	/**return CharacterData filled with same values as this matrix */
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){
		DNAData data = new DNAData(manager, getNumTaxa(), getNumChars(), taxa);
		data.setMatrix(this);
		if (getAnnotation()!=null)
			data.setAnnotation(getAnnotation(), false);
		return data;
	}
	/*..........................................................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public MCharactersHistory adjustHistorySize(Tree tree, MCharactersHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		MCharactersHistory soc =charStates;
		if (charStates==null || ! (charStates.getClass() == MDNAHistory.class)) 
			soc = new MDNAHistory(tree.getTaxa(), getNumChars(), numNodes); 
		else if (numNodes!= charStates.getNumNodes() || charStates.getNumChars()!= getNumChars()) 
			soc = new MDNAHistory(tree.getTaxa(), getNumChars(), numNodes);
		else {
			soc =charStates;
		}
		soc.setParentData(getParentData());
		return soc;
	}
}

