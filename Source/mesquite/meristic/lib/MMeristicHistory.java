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
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;



/* ======================================================================== */
/**A class for an array of  meristic character states for many characters, at each of the nodes.*/
public class MMeristicHistory  extends MMeristicAdjustable implements MCharactersHistory {
	
	public MMeristicHistory (Taxa taxa, int numChars, int numNodes) {
		super(taxa, numChars, numNodes);
	}
	
	public MMeristicHistory (Taxa taxa) {
		super(taxa);
	}
	
	/*..........................................MMeristicHistory................*/
	/** extract the states of character ic and return as CharacterHistory*/
	public CharacterHistory getCharacterHistory (int ic){
		MeristicHistory soc = new MeristicHistory(getTaxa(), getNumNodes(), (MeristicData)getParentData());
		soc.setItemsAs(this);
		for (int it = 0; it<getNumNodes(); it++) {
			for (int item=0; item<getNumItems(); item++)
				soc.setState(it, item, getState(ic, it, item)); 
		}
		return soc;
	}
	/*..........................................MMeristicHistory................*/
	/** obtain the states of character ic from the given CharacterDistribution*/
	public void transferFrom(int ic, CharacterHistory s) { 
		if (s instanceof MeristicHistory) {
			setItemsAs(((MeristicHistory)s));
			for (int j=0; j<getNumNodes(); j++)
				for (int item=0; item<((MeristicHistory)s).getNumItems(); item++) {
					if (getItem(item)!=null)
						getItem(item).setValue(ic, j,  ((MeristicHistory)s).getState(j, item));
				}
		}
	}
	int minState = MesquiteInteger.unassigned;
	int maxState = MesquiteInteger.unassigned;
	/*..........................................MMeristicHistory................*/
	private void calcMinMaxStates(Tree tree, int node, int item) {
		for (int ic=0; ic<getNumChars(); ic++){
			int s=getState(ic, node, item); 
			minState = MesquiteInteger.minimum(s, minState);
			maxState = MesquiteInteger.maximum(s, maxState);
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				calcMinMaxStates(tree, d, item);
	}
	public void getMinMax(Tree tree, int root, int item, MesquiteInteger min, MesquiteInteger max){
		if (min == null || max==null)
			return;
		minState = MesquiteInteger.unassigned;
		maxState = MesquiteInteger.unassigned;
		calcMinMaxStates(tree, root, item);
		min.setValue(minState);
		max.setValue(maxState);
	}
}

