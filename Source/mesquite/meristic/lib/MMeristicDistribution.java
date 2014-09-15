/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.meristic.lib;

import java.awt.*;
import java.util.*;

import mesquite.categ.lib.MDNAHistory;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**A class for an array of  meristic character states for many characters, at each of the taxa or nodes.*/
public abstract class MMeristicDistribution  extends MMeristicStates implements MCharactersDistribution {
	public MMeristicDistribution (Taxa taxa) {
		super(taxa);
	}
	/*..........................................MMeristicDistribution................*/
	public abstract CharacterDistribution getCharacterDistribution (int ic);
	/*-----*/
	/*..........................................MMeristicDistribution................*/
	public abstract Integer2DArray getItem(int index);
	/*..........................................MMeristicDistribution................*/
	/**return blank adjustable MMeristicDistribution if this same type */
	public MAdjustableDistribution makeBlankAdjustable(){
		MMeristicAdjustable mca = new MMeristicAdjustable(getTaxa(), getNumChars(), getNumNodes()); 
		mca.setItemsAs(this);
		return mca;
	}
	/*..........................................MMeristicDistribution................*/
	/**return CharacterData filled with same values as this matrix */
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){
		MeristicData data = new MeristicData(manager, taxa.getNumTaxa(), getNumChars(), taxa);
		data.setMatrix(this); 
		if (this instanceof Annotatable && ((Annotatable)this).getAnnotation()!=null)
			data.setAnnotation(((Annotatable)this).getAnnotation(), false);
		else if (getParentData()!=null && getParentData().getAnnotation()!=null)
			data.setAnnotation(getParentData().getAnnotation(), false);
		return data;
	}
	/*..........................................MMeristicDistribution................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public MCharactersHistory adjustHistorySize(Tree tree, MCharactersHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		MCharactersHistory soc =charStates;
		if (charStates==null || ! (charStates.getClass() == MMeristicHistory.class)) 
			soc = new MMeristicHistory(tree.getTaxa(), getNumChars(), numNodes);
		else if (numNodes!= charStates.getNumNodes() || charStates.getNumChars()!= getNumChars()) 
			soc = new MMeristicHistory(tree.getTaxa(), getNumChars(), numNodes);
		else {
			soc =charStates;
		}
		((MMeristicHistory)soc).setItemsAs(this);
		soc.setParentData(getParentData());
		return soc;
	}
}

