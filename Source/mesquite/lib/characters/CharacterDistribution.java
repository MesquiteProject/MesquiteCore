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

import mesquite.lib.tree.Tree;

/*Last documented:  April 2003*/


/* ======================================================================== */
/** Stores the states in the terminal taxa.  Often passed to calculations routines so that they can count steps, likelihood, reconstruct ancestral states,
and so on.  See general discussion of character storage classes under CharacterState*/
public interface CharacterDistribution extends CharacterStatesHolder {
	
	/** returns a CharacterDistribution that is adjustable and has the same states*/
	public AdjustableDistribution getAdjustableClone();
	
	/** Adjusts the size of the passed CharacterHistory to be of the same type as this CharacterHistory and to be prepared
	to store states for each of the nodes of the tree.*/
	public  CharacterHistory adjustHistorySize(Tree tree, CharacterHistory charStates);
	/*.................................................................................................................*/
	/**Returns whether or not the character has missing (unassigned) data*/
	public boolean hasMissing ();
	/**Returns whether or not the character has missing (unassigned) data in the taxa of the tree*/
	public boolean hasMissing(Tree tree, int node);
	/*.................................................................................................................*/
	/**Returns whether or not the character has inapplicable codings (gaps)*/
	public boolean hasInapplicable();
	/*.................................................................................................................*/
	/**Returns whether or not the character has inapplicable codings (gaps) in the taxa of the tree*/
	public boolean hasInapplicable(Tree tree, int node);
	/*.................................................................................................................*/
	/**Returns whether or not the character is constant (all states are the same)*/
	public boolean isConstant();
	/*.................................................................................................................*/
	/**Returns whether or not the character is constant (all states are the same) in the taxa of the tree*/
	public boolean isConstant(Tree tree, int node);
	/*.................................................................................................................*/
}

