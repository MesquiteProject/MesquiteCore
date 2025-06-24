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

import mesquite.lib.MesquiteDouble;
import mesquite.lib.characters.CharacterHistory;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;


/* ======================================================================== */
public class DNACharacterAdjustable extends CategoricalAdjustable {
	public DNACharacterAdjustable (Taxa taxa) {
		super(taxa);
		enforcedMaxState = 3;
	}
	public DNACharacterAdjustable (Taxa taxa, int num) {
		super(taxa, num);
		enforcedMaxState = 3;
	}
	/*..........................................  DNACharacterAdjustable  ..................................................*/
	/**returns blank CharacterState object */
	public CharacterState getCharacterState (){
		return new DNAState();
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return DNAState.class;
	}
	/*.................................................................................................................*/
	/**Returns full set of allowed states*/
	public long fullSet (){
		return DNAState.fullSet();
	}
	/*..........................................  DNAState  ...................................................*/
	/**returns CharacterState at node N */
	public CharacterState getCharacterState (CharacterState cs, int N){
		if (checkIllegalNode(N, 5)) {
			if (cs!=null)
				cs.setToUnassigned();
			return cs;
		}
		DNAState c;
		if (cs == null || !(cs instanceof DNAState))
			c = new DNAState();
		else
			c = (DNAState)cs;
		c.setValue(getState(N));
		return c;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return DNAData.class;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return DNAData.DATATYPENAME;
	}
	/** returns the union of all state sets*/
	public long getAllStates() {  //
		long s=0;
		for (int i=0; i< 4; i++) {
			s = CategoricalState.addToSet(s, i);
		}
		return s;
	}
	/*..........................................  DNACharacterAdjustable  ...................................................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public CharacterHistory adjustHistorySize(Tree tree, CharacterHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		CharacterHistory soc =charStates;
		if (charStates==null || ! (charStates.getClass() == DNACharacterHistory.class))
			soc = new DNACharacterHistory(tree.getTaxa(), numNodes);
		else if (numNodes!= charStates.getNumNodes()) 
			soc = new DNACharacterHistory(tree.getTaxa(), numNodes);
		else {
			soc =charStates;
		}
		soc.setParentData(getParentData());
		soc.setParentCharacter(getParentCharacter());
		((CharacterStates)soc).setExplanation(getExplanation());
		return soc;
	}
	/*..........................................  DNACharacterAdjustable  ...................................................*/
	/**returns string describing character states at node  */
	public String toString (int node, String lineEnding) {
		if (checkIllegalNode(node, 8))
			return "";
		if (frequencies != null && frequencies[node]!=null && frequencies[node].length>0) {
			String s="";
			for (int i=0; i<frequencies[node].length; i++) 
				if (frequencies[node][i]!=0)
					s+= DNAState.toString(i) + ":" + MesquiteDouble.toString(frequencies[node][i]) + lineEnding;
			return s;
		}
		else
			return DNAState.toString(getState(node), null, 0, false);
	}
	/*..........................................  DNACharacterAdjustable  ...................................................*/
	/**returns string describing states at nodes. */
	public String toString () {
		String s="";
		for (int i=0; i<getNumNodes(); i++)
			s += DNAState.toString(getState(i), false);
		return s;
	}
	/*.........................................CategoricalAdjustable.................................................*/
	public CategoricalAdjustable makeAdjustable(Taxa taxa){
		return new DNACharacterAdjustable(taxa, taxa.getNumTaxa());
	} 
}

