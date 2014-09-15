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
public class CategoricalAdjustable extends CategoricalDistribution  implements AdjustableDistribution {
	public long states[];
	private int watchPoint=-1;
	protected int  numNodes=0;  
	
	public CategoricalAdjustable (Taxa taxa, int num) {
		super(taxa);
		this.numNodes=num;
		states = new long[numNodes];
		deassignStates();
		if (watchPoint>0)
			MesquiteMessage.println("CategoricalAdjustable:  new states while watchpoint on for " + watchPoint);
	}
	public CategoricalAdjustable (Taxa taxa) {
		super(taxa);
	}
	
	 
	/*.........................................CategoricalAdjustable.................................................*/
	/** get number of taxa available in this categorical character.*/
	public int getNumTaxa() {
		return numNodes;
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/** get number of nodes available in this categorical character.*/
	public int getNumNodes() {
		return numNodes;
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/**set all states to missing (unassigned)*/
	public void deassignStates(){
		for (int i=0; i<numNodes; i++)
			states[i]=CategoricalState.unassigned;
		frequencies = null;
		/*if (frequencies != null)
			for (int i=0; i<frequencies.length; i++)
				for (int j=0; j<frequencies[i].length;j++) 
					frequencies[i][j] = MesquiteDouble.unassigned;
		*/	
		if (watchPoint>0)
			MesquiteMessage.println("CategoricalAdjustable:  zero states while watchpoint on for " + watchPoint);
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/** Set states array to be one passed */
	public void setStates (long[] sStates) {
		states = sStates;
		numNodes = sStates.length;
		if (watchPoint>0)
			MesquiteMessage.println("CategoricalAdjustable:  reset states while watchpoint on for " + watchPoint);
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/** get stateset at node N */
	public long getState (int N) {
		if (checkIllegalNode(N, 4))
			return CategoricalState.unassigned;
		return CategoricalState.dataBitsMask & states[N];
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/** sets state at node N to stateset passed */
	public void setState (int N, long d) {
		if (checkIllegalNode(N, 7))
			return;
		if (N==watchPoint)
			MesquiteMessage.println("CategoricalAdjustable:  attempt to set state of watched node " + N);
		states[N] = d;
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/** sets state at node N to stateset passed */
	public void setCharacterState (int N, CharacterState cs) {
		if (checkIllegalNode(N, 7))
			return;
		if (cs == null || !(cs instanceof CategoricalState))
			return; //TODO: warning
		if (N==watchPoint)
			MesquiteMessage.println("CategoricalAdjustable:  attempt to set state of watched node " + N);
		states[N] = ((CategoricalState)cs).getValue();
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/**set watchpoint at node N.  Subsequently moniters to see if its value changed and gives notification.  May not work well (changes may not be checked in all places.*/
	public void setWatchPoint (int N) {
		if (checkIllegalNode(N, 6))
			return;
		watchPoint=N;
		MesquiteMessage.println("Watchpoint set for node " + N);
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/**Trade states of nodes it and it2 */
	public void tradeStatesBetweenTaxa(int it, int it2) {
		if (checkIllegalNode(it, 9127) && checkIllegalNode(it, 9126))
			return;
		long temp = states[it];
		states[it] =  states[it2];
		states[it2] = temp;
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public AdjustableDistribution adjustSize(Taxa taxa) {
		if (taxa.getNumTaxa() == this.getNumTaxa())
			return this;
		else {
			CategoricalAdjustable soc = makeAdjustable(taxa);
			soc.setParentData(getParentData());
			soc.setParentCharacter(getParentCharacter());
			((CharacterStates)soc).setExplanation(getExplanation());
			return soc;
		}
	}
}

