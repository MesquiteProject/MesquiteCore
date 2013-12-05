/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
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
/**A class for an array of  categorical character states for many characters, at each of the taxa  or nodes.*/
public abstract class MCategoricalDistribution extends MCategoricalStates implements MCharactersDistribution {
	public MCategoricalDistribution (Taxa taxa) {
		super(taxa);
	}
	
	
	/*..........................................MCategoricalDistribution................*/
	/**return blank adjustable MCharactersDistribution if this same type */
	public MAdjustableDistribution makeBlankAdjustable(){
		return new MCategoricalAdjustable(getTaxa(), getNumChars(), getNumTaxa());
	}
	/*..........................................MCategoricalDistribution................*/
	/**return CharacterData filled with same values as this matrix */
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){
		CategoricalData data = new CategoricalData(manager, getNumTaxa(), getNumChars(), taxa);
		data.setMatrix(this);
		if (this instanceof Annotatable && ((Annotatable)this).getAnnotation()!=null)
			data.setAnnotation(((Annotatable)this).getAnnotation(), false);
		else if (getParentData()!=null && getParentData().getAnnotation()!=null)
			data.setAnnotation(getParentData().getAnnotation(), false);
		return data;
	}
	/*..........................................MCategoricalDistribution................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public MCharactersHistory adjustHistorySize(Tree tree, MCharactersHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		MCharactersHistory soc =charStates;
		if (charStates==null || ! (charStates.getClass() == MCategoricalHistory.class)) 
			soc = new MCategoricalHistory(tree.getTaxa(), getNumChars(), numNodes); 
		else if (numNodes!= charStates.getNumNodes() || charStates.getNumChars()!= getNumChars()) 
			soc = new MCategoricalHistory(tree.getTaxa(), getNumChars(), numNodes);
		else {
			soc =charStates;
		}
		soc.setParentData(getParentData());
		return soc;
	}
	/**returns raw state set of character ic in taxon */
	public long getStateRaw (int ic, int it){
		return getState(ic, it);
	}
}


