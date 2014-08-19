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
/** A class for an array of  categorical character states for one character, at each of the taxa  or nodes*/
public abstract class CategoricalDistribution extends CategoricalStates implements CharacterDistribution {
	static long zeroOne = CategoricalState.setUncertainty(CategoricalState.addToSet(CategoricalState.makeSet(0), 1), true);
	public CategoricalDistribution (Taxa taxa) {
		super(taxa);
	}
	public int getNumNodes(){
		return getNumTaxa();
	}
	/*.................................................................................................................*/
	/**Returns full set of allowed states*/
	public long fullSet (){
		if (getMaxState()<0)
			return zeroOne;
		return CategoricalState.fullSet();
	}
	/*.................................................................................................................*/
	/**Returns largest state value*/
	public int getMaxState (){
		int max = -1;
		for (int i=0; i< getNumNodes(); i++) {
			int ms = CategoricalState.maximum(getState(i));
			if (ms>max)
				max = ms;
		}
		return max;
	}
	/*.................................................................................................................*/
	/**Returns whether or not the character has polymorphic or multstate uncertain data*/
	public boolean hasMultipleStatesInTaxon(){
		for (int i=0; i< getNumNodes(); i++)
			if (CategoricalState.hasMultipleStates(getState(i)))
				return true;
		return false;
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
	/**Returns whether or not the character has polymorphic or multstate uncertain data in the taxa of the tree*/
	public boolean hasMultipleStatesInTaxon(Tree tree, int node){
		if (tree.nodeIsTerminal(node)){
			if (CategoricalState.hasMultipleStates(getState(tree.taxonNumberOfNode(node))))
				return true;
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (hasMultipleStatesInTaxon(tree, d))
					return true;
		}
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
	/**Returns whether or not the character is all binary and no missing in the taxa of the tree*/
	public static boolean isBinaryNoMissing(CharacterDistribution dist, Tree tree){
		if (!(dist instanceof CategoricalDistribution))
			return false;
		CategoricalDistribution cd = (CategoricalDistribution)dist;
		return inIsBinary(cd, tree, tree.getRoot(), false);
	}
	/*.................................................................................................................*/
	/**Returns whether or not the character is all binary in the taxa of the tree*/
	public static boolean isBinary(CharacterDistribution dist, Tree tree){
		if (!(dist instanceof CategoricalDistribution))
			return false;
		CategoricalDistribution cd = (CategoricalDistribution)dist;
		return inIsBinary(cd, tree, tree.getRoot(), true);
	}
	static boolean inIsBinary(CategoricalDistribution dist, Tree tree, int node, boolean permitMissing){

		if (tree.nodeIsTerminal(node)){
			if (permitMissing && !dist.isBinary(tree.taxonNumberOfNode(node)))
				return false;
			if (!permitMissing && !dist.isBinaryCombinable(tree.taxonNumberOfNode(node)))
					return false;
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (!inIsBinary(dist, tree, d, permitMissing))
					return false;
			}
		}
		return true;
	}
	/**Returns whether or not the character is binary*/
	public boolean isBinary(int node){
		return ((getState(node) & CategoricalState.statesBitsMask) | 3L) == 3L;
	}
	/**Returns whether or not the character is binary and with an actual state*/
	public boolean isBinaryCombinable(int node){
		return isBinary(node) && ((getState(node) & 3L) != 0L);
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
	/*..........................................  CategoricalDistribution  ...................................................*/
	/** returns a CharacterDistribution that is adjustable and has the same states*/
	public AdjustableDistribution getAdjustableClone(){
		CategoricalAdjustable soc = makeAdjustable(getTaxa());
		for (int it=0; it<getNumTaxa(); it++)
			soc.setState(it, getState(it));
		soc.setParentData(getParentData());
		soc.setParentCharacter(getParentCharacter());
		((CharacterStates)soc).setExplanation(getExplanation());
	return soc;
	}
	/*..........................................  CategoricalDistribution  ...................................................*/
	public CategoricalAdjustable makeAdjustable(Taxa taxa){
		return new CategoricalAdjustable(taxa, taxa.getNumTaxa());
	} 
	/*..........................................  CategoricalDistribution  ...................................................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public CharacterHistory adjustHistorySize(Tree tree, CharacterHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		CharacterHistory soc =charStates;
		if (charStates==null || ! (charStates.getClass() == CategoricalHistory.class))
//		if (charStates==null || ! (charStates instanceof CategoricalHistory))
			soc = new CategoricalHistory(tree.getTaxa(), numNodes);
		else if (numNodes!= charStates.getNumNodes()) 
			soc = new CategoricalHistory(tree.getTaxa(), numNodes);
		else {
			soc =charStates;
		}
		soc.setParentData(getParentData());
		soc.setParentCharacter(getParentCharacter());
		((CharacterStates)soc).setExplanation(getExplanation());
		return soc;
	}

}

