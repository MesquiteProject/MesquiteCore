/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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

/*Last documented:  April 2003 */
/*==========================  Mesquite Basic Class Library    ==========================*/
/*
Characters and their states are represented by various classes.  One, CharacterState, represents a single state of a character (e.g., the state of 
a terminal taxon).  <p>
The class CharacterData and its subclasses represent full datasets with a matrix, character names, and associated sets (model sets,
exclusion sets, etc.).  <p>
Subclasses of MCharactersStates represent a matrix of character X taxa (without associated names, sets, etc.), and are used
for temporary storage of a matrix or for the results of a reconstruction of character evolution in all characters.<p>
Subclasses of MCharactersStates represent an array of character states in a series of taxa or nodes ("M" for "Multiple", as opposed to the subclasses of
CharacterStates which refer to a single character).  The subclasses are:
	<li>MCharactersDistribution - The states in multiple characters in terminal taxa.  Often passed to modules so they can calculate on it.
		<ul>
		<li>MCategoricalDistribution - for categorical characters.
			<ul><li>MCategoricalAdjustable - used for stand-alone characters, such as those coming from a simulation.  Size can be adjusted & states altered.
			<li>MCategoricalEmbedded - used as a reference to amatrix in an existing CharacterData object.
			</ul>
		<li>MContinuousDistribution
			<ul><li>MContinuousAdjustable - used for stand-alone characters, such as those coming from a simulation.  Size can be adjusted & states altered.
			<li>MContinuousEmbedded - used as a reference to a matrix in an existing CharacterData object.
			</ul>
		</ul>
	<li>MCharactersHistory - states at the internal and terminal nodes of a tree.  Returned by reconstruction modules.
			<ul><li>MCategoricalHistory - for categorical characters
			<li>MContinuousHistory - for continuous characters.
			</ul>
<p>
Subclasses of CharacterStates represent a vector of character states in a single character in a series of taxa or nodes.  The subclasses are:
	<li>CharacterDistribution - The states in terminal taxa.  Often passed to modules so they can calculate on it.
		<ul>
		<li>CategoricalDistribution - for a categorical character.
			<ul><li>CategoricalAdjustable - used for stand-alone character, such as that coming from a simulation.  Size can be adjusted & states altered.
			<li>CategoricalEmbedded - used as a reference to a character in an existing CharacterData object.
			</ul>
		<li>ContinuousDistribution
			<ul><li>ContinuousAdjustable - used for stand-alone character, such as that coming from a simulation.  Size can be adjusted & states altered.
			<li>ContinuousEmbedded - used as a reference to a character in an existing CharacterData object.
			</ul>
		</ul>
	<li>CharacterHistory - states at the internal and terminal nodes of a tree.  Returned by reconstruction modules.
			<ul><li>CategoricalHistory - for a categorical character
			<li>ContinuousHistory - for a continuous character.
			</ul>
*/

/* ======================================================================== */
/** An abstraction of a state of a character to allow categorical, continuous and other subclasses. */
public abstract class CharacterState {
	/** returns string representation of the character state*/
	public abstract String toString();
	/** sets its value to the value in CharacterState passed to it*/
	public abstract void setValue(CharacterState cs);
	
	/** sets its value to the value given by the String passed to it*/
	public abstract void setValue(String s, CharacterData parentData);
	
	/** sets its value to the value given by the String passed to it starting at position pos*
	public abstract void setValue(String s, MesquiteInteger pos);
	/**/
	/** sets its value to unassigned*/
	public abstract void setToUnassigned();
	/** sets its value to inapplicable*/
	public abstract void setToInapplicable();
	/** returns whether the value is unassigned.  Unassigned is a special value for each character type, equivalent to
	missing data "?"*/
	public abstract boolean isUnassigned();
	/** returns whether value is inapplicable.  Inapplicable is a special value for each character type, equivalent to
	a gap "-"*/
	public abstract boolean isInapplicable();
	/** returns whether value is valid or not.  Impossible is a special value for each character type, and should be used to indicate
	a state is invalid.*/
	public abstract boolean isImpossible();
	/** returns whether value is combinable (i.e. a valid assigned state) or not.*/
	public abstract boolean isCombinable();
	/** returns whether the contents of the character states are identical*/
	public abstract boolean equals(CharacterState s);
	/** returns whether the contents of the character states are identical, allowing some to be missing if allowMissing is true*/
	public abstract boolean equals(CharacterState s, boolean allowMissing);
	/** returns the subclass of CharacterData that is equivalent to this character type (i.e., in which sort of
	matrix would this character state reside?*/
	public abstract boolean equals(CharacterState s, boolean allowMissing, boolean allowNearExact);
	public boolean equals(CharacterState s, boolean allowMissing, boolean allowNearExact, boolean allowSubset) {
		return equals(s, allowMissing, allowNearExact, false);
	}
	/** returns the subclass of CharacterData that is equivalent to this character type (i.e., in which sort of
	matrix would this character state reside?*/
	public abstract Class getCharacterDataClass();
	/** returns the subclass of MCharactersDistribution that is equivalent to this character type (i.e., in which sort of
	matrix would this character state reside?*/
	public abstract Class getMCharactersDistributionClass();
	/** returns the subclass of CharacterDistribution that is equivalent to this character type (i.e., in which sort of
	Distribution would this character state reside?.  Should return an MAdjustableDistribution*/
	public abstract Class getCharacterDistributionClass();
	/** returns the subclass of CharacterHistory that is equivalent to this character type (i.e., in which sort of
	Distribution would this character state reside?*/
	public abstract Class getCharacterHistoryClass();
	
	/** Returns string as would be displayed to user (not necessarily internal shorthand)*/
	public abstract String toDisplayString();
	
	/** returns a AdjustableDistribution that is equivalent to this character type (i.e., in which sort of
	matrix would this character state reside?*/
	public abstract AdjustableDistribution makeAdjustableDistribution(Taxa taxa, int numNodes);
	/** returns a CharacterHistory that is equivalent to this character type (i.e., in which sort of
	matrix would this character state reside?*/
	public abstract CharacterHistory makeCharacterHistory(Taxa taxa, int numNodes);
	
	/** returns a MCharactersDistribution that is equivalent to this character type (i.e., in which sort of
	matrix would this character state reside?*/
	public MAdjustableDistribution makeMCharactersDistribution(Taxa taxa, int numChars, int numTaxa) {
		Class c = getMCharactersDistributionClass();
		if (c == null) {
			MesquiteMessage.printStackTrace("no class returned by getMCharactersDistributionClass " + this);
			return null;
		}
		try {
			MAdjustableDistribution s = (MAdjustableDistribution)c.newInstance();
			if (s!=null) {
				s.setTaxa(taxa);
				s.setSize(numChars, numTaxa);
				return s;
			}
		}
		catch (IllegalAccessException e){MesquiteTrunk.mesquiteTrunk.alert("iae cs");
		e.printStackTrace(); }
		catch (InstantiationException e){MesquiteTrunk.mesquiteTrunk.alert("ie cs");e.printStackTrace(); }
		return null;
	}
	
	/**Returns the type of data stored.. */
	public abstract String getDataTypeName();
}

