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

/*Last documented:  April 2003*/
/* ======================================================================== */
/**An abstract class for an array of  character states for many characters, at each of the taxa  or nodes.
Although its subclasses (for categorical and continuous data) store matrices of character states, it is not
a variety of data matrix (CharacterData), because it does not contained associated model sets etc. etc.
It is is used mainly for calculations to store their results, e.g. downstates,
or reconstructed states at nodes.
  See general discussion of character storage classes under CharacterState*/
public abstract class MCharactersStates implements MCharactersStatesHolder   {
	protected CharacterData data = null;
	public static long totalCreated = 0;
	private String name = null;
	private Taxa taxa;
	Tree basisTree; //the tree on which this was based (e.g. if simulated); important in case both tree and matrix are needed
	
	public MCharactersStates(Taxa taxa) {
		totalCreated++;
		this.taxa = taxa;
	}
	
	public void setBasisTree(Tree tree){ 
		if (tree == null) {
			basisTree = null;
		}
		else {
			basisTree = tree.cloneTree();//TODO: establish listener of Taxa, but if so how to remove as there is no dispose method?
		}
	}
	public Tree getBasisTree(){
		return basisTree;
	}
	/*.................................................................................................................*/
	/** returns taxa object */
	public Taxa getTaxa (){
		return taxa;
	}
	/*.................................................................................................................*/
	/** sets taxa object */
	public void setTaxa (Taxa taxa){
		if (taxa != this.taxa)
			basisTree = null;
		this.taxa = taxa;
	
	}
	/*.................................................................................................................*/
	/** returns name */
	public String getName (){
		return name;
	}
	/*.................................................................................................................*/
	/** sets name */
	public void setName (String name){
		this.name = name;
	}
	/** Indicates the type of character stored */ 
	public abstract Class getStateClass();

	/**Returns the type of data stored. */
	public abstract String getDataTypeName();
	
	/** Returns the parent CharacterData */ 
	public CharacterData getParentData() {
		return data;
	}
	/** get CharacterState of character ic at node or taxon it*/
	public abstract CharacterState getCharacterState (CharacterState cs, int ic, int it);

	public boolean isCurrentlyIncluded(int ic){
		if (data!=null)
			return data.isCurrentlyIncluded(ic);
		return true;
	}
	/*.................................................................................................................*/
	protected boolean checkIllegalNode(int node, int where) {
		if (node<0 || node>=getNumNodes()) {
			return true;
			//MesquiteMessage.println("illegal node in MCharactersStates (node: " + node + " max: " + getNumNodes() + ") " + where);
		}
		return false;
	}
	
	public int getNumNodes(){
		return getNumTaxa();
	}
	
	public String matrixToString(){
		String s = "";
		CharacterState cs  = null;
		for (int it = 0; it<getNumNodes(); it++){
			for (int ic = 0; ic<getNumChars(); ic++){
				cs = getCharacterState(cs, ic, it);
				s += " " + cs;
			}
			s += "\n";
		}
		return s;
	}
}

