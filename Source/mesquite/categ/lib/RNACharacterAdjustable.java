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
/** A subclass of CharacterDistribution for DNA data.  Enforces max state at 4*/
public class RNACharacterAdjustable extends DNACharacterAdjustable {
	public RNACharacterAdjustable (Taxa taxa) {
		super(taxa);
		enforcedMaxState = 3;
	}
	public RNACharacterAdjustable (Taxa taxa, int num) {
		super(taxa, num);
		enforcedMaxState = 3;
	}
	/*..........................................  RNACharacterAdjustable  ..................................................*/
	/**returns blank CharacterState object */
	public CharacterState getCharacterState (){
		return new RNAState();
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return RNAState.class;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return RNAData.class;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return RNAData.DATATYPENAME;
	}
	/*.................................................................................................................*/
	/**Returns full set of allowed states*/
	public long fullSet (){
		return RNAState.fullSet();
	}
	/*..........................................  RNAState  ...................................................*/
	/**returns CharacterState at node N */
	public CharacterState getCharacterState (CharacterState cs, int N){
		if (checkIllegalNode(N, 5)) {
			if (cs!=null)
				cs.setToUnassigned();
			return cs;
		}
		RNAState c;
		if (cs == null || !(cs instanceof RNAState))
			c = new RNAState();
		else
			c = (RNAState)cs;
		c.setValue(getState(N));
		return c;
	}

	/*..........................................  RNACharacterAdjustable  ...................................................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public CharacterHistory adjustHistorySize(Tree tree, CharacterHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		CharacterHistory soc =charStates;
		if (charStates==null || ! (charStates.getClass() == RNACharacterHistory.class))
			soc = new RNACharacterHistory(tree.getTaxa(), numNodes);
		else if (numNodes!= charStates.getNumNodes()) 
			soc = new RNACharacterHistory(tree.getTaxa(), numNodes);
		else {
			soc =charStates;
		}
		soc.setParentData(getParentData());
		soc.setParentCharacter(getParentCharacter());
		((CharacterStates)soc).setExplanation(getExplanation());
		return soc;
	}
	public CategoricalAdjustable makeAdjustable(Taxa taxa){
		return new RNACharacterAdjustable(taxa, taxa.getNumTaxa());
	} 
}

