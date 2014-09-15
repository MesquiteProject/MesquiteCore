/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
/* ======================================================================== */
/** Contains an array of  continuous character states for one character, at each of the taxa or nodes 
 See notes under <a href = "ContinuousData.html">ContinuousData</a> regarding items */
public abstract class ContinuousDistribution  extends ContinuousStates implements CharacterDistribution {
	
	public ContinuousDistribution (Taxa taxa) {
		super(taxa);
	}
	/*..........................................ContinuousDistribution................*/
	public int getNumNodes(){
		return getNumTaxa();
	}
	/*.................................................................................................................*/
	/**Returns whether or not the character has missing (unassigned) data*/
	public boolean hasMissing (){
		for (int i=0; i< getNumNodes(); i++)
			if (isUnassigned(i))
				return true;
		return false;
	}
	/*.................................................................................................................*/
	/**Returns whether or not the character has inapplicable codings (gaps)*/
	public boolean hasInapplicable (){
		for (int i=0; i< getNumNodes(); i++)
			if (isInapplicable(i))
				return true;
		return false;
	}
	/*.................................................................................................................*/
	/**Returns whether or not the character has missing (unassigned) data in the taxa of the tree*/
	public boolean hasMissing(Tree tree, int node){
		if (tree.nodeIsTerminal(node)){
			if (isUnassigned(tree.taxonNumberOfNode(node)))
				return true;
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (hasMissing(tree, d))
					return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/**Returns whether or not the character has inapplicable codings (gaps) in the taxa of the tree*/
	public boolean hasInapplicable(Tree tree, int node){
		if (tree.nodeIsTerminal(node)){
			if (isInapplicable(tree.taxonNumberOfNode(node)))
				return true;
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (hasInapplicable(tree, d))
					return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/**Returns whether or not the character is constant (all states are the same)*/
	public boolean isConstant(){
		if (getNumNodes()<1)
			return true;
		CharacterState cs = getCharacterState(null, 0);
		CharacterState cs2 = null;
		for (int i=1; i< getNumNodes(); i++)
			if (!cs.equals(cs2 = getCharacterState(cs2, i)))
				return false;
		return true;
	}
	/*.................................................................................................................*/
	/**Returns whether or not the character is constant (all states are the same) in the taxa of the tree*/
	private boolean isConstantRec(Tree tree, int node, CharacterState cs, CharacterState cs2){
		if (tree.nodeIsTerminal(node)){
			if (!cs.equals(cs2 = getCharacterState(cs2, tree.taxonNumberOfNode(node))))
				return false;
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (!isConstantRec(tree, d, cs, cs2))
					return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	/**Returns whether or not the character is constant (all states are the same) in the taxa of the tree*/
	public boolean isConstant(Tree tree, int node){
		if (getNumNodes()<1)
			return true;
		CharacterState cs = getCharacterState(null, 0);
		CharacterState cs2 = getCharacterState(null, 0);
		return isConstantRec(tree, node, cs, cs2);
	}
	/*..........................................ContinuousDistribution................*/
	/** returns a CharacterDistribution that is adjustable and has the same states*/
	public AdjustableDistribution getAdjustableClone(){
		ContinuousAdjustable soc = new ContinuousAdjustable(getTaxa(), getNumTaxa());
		soc.setItemsAs(this);
		for (int it = 0; it<getNumTaxa(); it++) {
			for (int item = 0; item<getNumItems(); item++)
				soc.setState(it, item, getState(it, item)); 
		}
		soc.setParentData(getParentData());
		soc.setParentCharacter(getParentCharacter());
		((CharacterStates)soc).setExplanation(getExplanation());
		return soc;
	}
	/*..........................................ContinuousDistribution................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public CharacterHistory adjustHistorySize(Tree tree, CharacterHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		CharacterHistory soc =charStates;
		if (charStates==null || ! (charStates instanceof ContinuousHistory)) 
			soc = new ContinuousHistory(tree.getTaxa(), numNodes, (ContinuousData)getParentData()); //should keep items!!! (since StsOfChar may have different number from parent data!
		else if (numNodes!= charStates.getNumNodes()) 
			soc = new ContinuousHistory(tree.getTaxa(), numNodes, (ContinuousData)getParentData());
		else {
			soc =charStates;
		}
		soc.setParentData(getParentData());
		soc.setParentCharacter(getParentCharacter());
		((CharacterStates)soc).setExplanation(getExplanation());
		return soc;
	}

}



