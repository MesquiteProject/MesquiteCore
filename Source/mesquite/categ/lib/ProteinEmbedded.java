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

import mesquite.lib.characters.CharacterHistory;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;

/* ======================================================================== */
/** A class for an array of  categorical character states for one character, at each of the taxa  or nodes*/
public class ProteinEmbedded extends CategoricalEmbedded {
	public ProteinEmbedded (ProteinData data, int ic) {
		super(data, ic);
		enforcedMaxState = ProteinState.maxProteinState;
	}
	/** returns the union of all state sets*/
	public long getAllStates() {  //
		long s=0;
		for (int i=0; i<= ProteinState.maxProteinState; i++) {
			s = CategoricalState.addToSet(s, i);
		}
		return s;
	}
	/*.......................................... ProteinState  ...................................................*/
	/**returns CharacterState at node N */
	public CharacterState getCharacterState (CharacterState cs, int N){
		if (checkIllegalNode(N, 5)) {
			if (cs!=null)
				cs.setToUnassigned();
			return cs;
		}
		ProteinState c;
		if (cs == null || !(cs instanceof ProteinState))
			c = new ProteinState();
		else
			c = (ProteinState)cs;
		c.setValue(getState(N));
		return c;
	}
	/*.................................................................................................................*/
	/**Returns full set of allowed states*/
	public long fullSet (){
		return ProteinState.fullSet();
	}
	/*..........................................  ProteinEmbedded  ...................................................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public CharacterHistory adjustHistorySize(Tree tree, CharacterHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		CharacterHistory soc =charStates;
		if (charStates==null || ! (charStates.getClass() == ProteinCharacterHistory.class))
			soc = new ProteinCharacterHistory(tree.getTaxa(), numNodes);
		else if (numNodes!= charStates.getNumNodes()) 
			soc = new ProteinCharacterHistory(tree.getTaxa(), numNodes);
		else {
			soc =charStates;
		}
		soc.setParentData(getParentData());
		soc.setParentCharacter(getParentCharacter());
		((CharacterStates)soc).setExplanation(getExplanation());
		return soc;
	}
	public CategoricalAdjustable makeAdjustable(Taxa taxa){
		return new ProteinAdjustable(taxa, taxa.getNumTaxa());
	} 
}

