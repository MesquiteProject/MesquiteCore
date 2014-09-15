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
public class CategoricalEmbedded extends CategoricalDistribution {
	CategoricalData data;
	public CategoricalEmbedded (CategoricalData data, int ic) {
		super(data.getTaxa());
		this.data = data;
		characterNumber = ic; // check if within bounds?
		enforcedMaxState = 0;
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return data.getStateClass();
	}
	public CharacterData getParentData(){
		return data;
	}
	/*..........................................  CategoricalEmbedded  ...................................................*/
	/** get number of taxa available in this categorical character.*/
	public int getNumTaxa() {
		return data.getNumTaxa();
	}
	/*..........................................  CategoricalEmbedded  ...................................................*/
	/** get stateset at node N */
	public long getState (int N) {
		if (checkIllegalNode(N, 4))
			return CategoricalState.unassigned;
		return data.getState(characterNumber, N);
	}
	public String getName(){
		return data.getCharacterName(characterNumber);
	}
	public String toString(){
		return "Character " + characterNumber + " from data " + data + "   " + super.toString();
	}
}


