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
/**A class for an array of  categorical character states for many characters, at each of the taxa  or nodes.*/
public class MCategoricalAdjustable extends MCategoricalDistribution implements MAdjustableDistribution {
	int numTaxa=0;
	int numChars=0;
	private long[][] states;
	String annotation;
	public MCategoricalAdjustable (Taxa taxa, int numChars, int numTaxa) {
		super(taxa);
		this.numTaxa=numTaxa;
		this.numChars=numChars;
		states = new long[numChars][numTaxa];
	}
	public MCategoricalAdjustable (Taxa taxa) {
		super(taxa);
		deassignStates();
	}
	public MCategoricalAdjustable () {
		super(null);
	}
	public void setAnnotation(String s, boolean notify){
		annotation = s;
	}
	public String getAnnotation(){
		return annotation;
	}
	/*..........................................  MCategoricalAdjustable  ..................................................*/
	/**sets the parent CharacterData from which this CharacterDistribution is derived or related*/
	public void setParentData (CharacterData cd){
		data = cd;
	}
	/*..........................................  MCategoricalAdjustable  ..................................................*/
	public void setSize(int numChars, int numTaxa) {
		if (numChars!= this.numChars || numTaxa !=this.numTaxa) {
			this.numTaxa=numTaxa;
			this.numChars=numChars;
			states = new long[numChars][numTaxa];
		}
	}
	/*..........................................  MCategoricalAdjustable  ..................................................*/
	/**assign missing data (unassigned) to all characters and nodes */
	public void deassignStates(){
		for (int j=0; j<numChars; j++)
			for (int i=0; i<numTaxa; i++)
				states[j][i]=CategoricalState.unassigned;
	}
	/*..........................................  MCategoricalAdjustable  ..................................................*/
	/**returns state set of character ic at node */
	public long getState (int ic, int N) {
		if (checkIllegalNode(N, 106))
			return CategoricalState.unassigned;
		return CategoricalState.dataBitsMask & states[ic][N];
	}
	/*..........................................  MCategoricalAdjustable  ..................................................*/
	/**set state set of character ic at node to d */
	public void setState (int ic, int N, long d) {
		if (checkIllegalNode(N, 107))
			return;
		states[ic][N] = d;
	}
	/*..........................................  MCategoricalAdjustable  ..................................................*/
	/**sets the character state of character ic and taxon it to that in the passed CharacterState*/
   	public void setCharacterState(CharacterState s, int ic, int it){
   		if (s instanceof CategoricalState){
			if (checkIllegalNode(it, 907))
				return;
   			states[ic][it] = ((CategoricalState)s).getValue();
   		}
   	}
	/*..........................................  MCategoricalAdjustable  ..................................................*/
   	/** trades the states of character ic between taxa it and it2.  Used for reshuffling.*/
	public void tradeStatesBetweenTaxa(int ic, int it, int it2) {
		if (checkIllegalNode(it, 9123) && ic <states.length)
			return;
		long temp = states[ic][it];
		states[ic][it] =  states[ic][it2];
		states[ic][it2] = temp;
	}
	/*..........................................  MCategoricalAdjustable  ..................................................*/
	/**obtain states of character ic from passed CharacterDistribution object */
	public void transferFrom(int ic, CharacterDistribution s) {
		if (s instanceof CategoricalDistribution)
			for (int j=0; j<numTaxa; j++)
				states[ic][j]=((CategoricalDistribution)s).getState(j);
	}
	/*..........................................  MCategoricalAdjustable  ..................................................*/
	/**return CharacterDistribution object for character ic */
	public CharacterDistribution getCharacterDistribution (int ic){
		CategoricalAdjustable soc = new CategoricalAdjustable(getTaxa(), numTaxa);
		soc.setStates(states[ic]);
		soc.setParentData(getParentData());
		soc.setParentCharacter(ic);
		if (getParentData()!=null)
			soc.setName("Character " + ic + " of " + getParentData().getName());
		else
			soc.setName("Character " + ic );
		return soc;
	}
	/*..........................................MCategoricalAdjustable................*/
	/**return CharacterData filled with same values as this matrix */
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){
		CategoricalData data = new CategoricalData(manager, getNumTaxa(), getNumChars(), taxa);
		data.setMatrix(this);
		if (getAnnotation()!=null)
			data.setAnnotation(getAnnotation(), false);
		return data;
	}
	public int getNumTaxa(){
		return numTaxa;
	}
	public int getNumChars(){
		return numChars;
	}
}


