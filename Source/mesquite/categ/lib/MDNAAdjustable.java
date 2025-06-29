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

import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.MAdjustableDistribution;
import mesquite.lib.characters.MCharactersHistory;
import mesquite.lib.duties.CharMatrixManager;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;


/* ======================================================================== */
public class MDNAAdjustable extends MCategoricalAdjustable {
	public MDNAAdjustable (Taxa taxa, int numChars, int numTaxa) {
		super(taxa, numChars, numTaxa);
	}
	public MDNAAdjustable (Taxa taxa) {
		super(taxa);
	}
	public MDNAAdjustable () {
		super(null);
	}
	public CharacterDistribution getCharacterDistribution (int ic){
		DNACharacterAdjustable soc = new DNACharacterAdjustable(getTaxa(), numTaxa);
		for (int it = 0; it<numTaxa; it++)
			soc.setState( it, getState(ic, it));
		soc.setParentData(getParentData());
		soc.setParentCharacter(ic);
		if (getParentData()!=null)
			soc.setName("Character " + ic + " of " + getParentData().getName());
		else
			soc.setName("Character " + ic );
		return soc;
	}
	/*..........................................  MDNAAdjustable  ..................................................*/
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return DNAState.class;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return DNAData.class;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return DNAData.DATATYPENAME;
	}
	/*..........................................MDNAAdjustable................*/
	/** get CharacterState at node N*/
	public CharacterState getCharacterState (CharacterState cs, int ic, int it){  
		if (cs !=null && cs instanceof DNAState) {
			((DNAState)cs).setValue(getState(ic, it));
			return cs;
		}
		return new DNAState(getState(ic, it)); 
	}
	/*..........................................MDNAAdjustable................*/
	/**return blank adjustable MCharactersDistribution if this same type */
	public MAdjustableDistribution makeBlankAdjustable(){
		return new MDNAAdjustable(getTaxa(), getNumChars(), getNumTaxa());
	}
	/*..........................................MDNAAdjustable................*/
	/**return CharacterData filled with same values as this matrix */
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){
		DNAData data = new DNAData(manager, getNumTaxa(), getNumChars(), taxa);
		data.setMatrix(this);
		if (getAnnotation()!=null)
			data.setAnnotation(getAnnotation(), false);
		return data;
	}
	/** returns the union of all state sets*/
	public long getAllStates() {  //
		long s=0;
		for (int i=0; i< 4; i++) {
			s = CategoricalState.addToSet(s, i);
		}
		return s;
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


