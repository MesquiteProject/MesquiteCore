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

import mesquite.cont.lib.ContColorTable;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class ProteinCharacterHistory extends CategoricalHistory {
	public ProteinCharacterHistory (Taxa taxa) {
		super(taxa);
		enforcedMaxState = ProteinState.maxProteinState;
	}
	public ProteinCharacterHistory (Taxa taxa, int num) {
		super(taxa, num);
		enforcedMaxState = ProteinState.maxProteinState;
	}
	/*.......................................... ProteinState  ...................................................*/
	/**returns CharacterState at node N */
	public CharacterState getCharacterState (CharacterState cs, int N){
		if (checkIllegalNode(N, 5)) {
			if (cs!=null)
				cs.setToUnassigned();
			return cs;
		}
		ProteinState c;
		if (cs == null || !(cs instanceof ProteinState))
			c = new ProteinState();
		else
			c = (ProteinState)cs;
		c.setValue(getState(N));
		return c;
	}
	/*..........................................  ProteinCharacterHistory  ..................................................*/
	/*.................................................................................................................*/
	/**Returns full set of allowed states*/
	public long fullSet (){
		return ProteinState.fullSet();
	}
	/**returns blank CharacterState object */
	public CharacterState getCharacterState (){
		return new ProteinState();
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return ProteinState.class;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return ProteinData.class;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return ProteinData.DATATYPENAME;
	}
	/** returns the union of all state sets*/
	public long getAllStates() {  //
		long s=0;
		for (int i=0; i<= ProteinState.maxProteinState; i++) {
			s = CategoricalState.addToSet(s, i);
		}
		return s;
	}
	/*..........................................  ProteinCharacterHistory  ..................................................*/
	/**clone this; if passed s is not null, place the clone there (so as to avoid creating new object)*/
	public CharacterHistory clone(CharacterHistory s) {
		ProteinCharacterHistory snew;
		if ((s==null) || (s.getNumNodes()!=numNodes) || (!(s instanceof ProteinCharacterHistory)))
			snew = new ProteinCharacterHistory(getTaxa(), numNodes);
		else {
			snew = (ProteinCharacterHistory)s;
		}
		for (int i=0; i<numNodes; i++) {
			snew.setState(i, getState(i));
		}
		if (frequenciesExist())
			copyFrequencies(this, snew);
		else
			snew.disposeFrequencies();
		if (extraFrequenciesExist())
			copyExtraFrequencies(this, snew);
		else
			snew.disposeExtraFrequencies();
		snew.data = data;
		snew.characterNumber = characterNumber;  //setting parent character
		snew.setExplanation(getExplanation());
		return snew;
	}
	/*..........................................  ProteinCharacterHistory  ..................................................*/
	public Color getDefaultColor(int maxState, int istate){
		return ProteinData.getProteinColorOfState(istate);
	}
	/*..........................................ProteinCharacterHistory................*/
	public MesquiteColorTable getColorTable(MesquiteColorTable colorTable) {
		
		if (colorTable == null || !(colorTable instanceof ProteinColorTable))
			return new ProteinColorTable();
		return colorTable;
	}
	/*..........................................  ProteinCharacterHistory  ..................................................*/
	/** Must be called before colors for states are used (e.g., before a tree is shaded).  Goes through all nodes to find states present, to set
	minima and maxima and record all states present. This allows the color range to be known before
	colors for particular states are requested by a tree shade, for example.*/
	public void prepareColors(Tree tree, int drawnRoot) {  
		allStates=ProteinState.span(0,ProteinState.maxProteinState);
		maxState = enforcedMaxState;
	}
	/*..........................................  ProteinCharacterHistory  ...................................................*/
	/**returns string describing character states at node  */
	public String toString (int node, String lineEnding) {
		if (checkIllegalNode(node, 8))
			return "";
		if (frequencies != null && frequencies[node]!=null && frequencies[node].length>0) {
			String s="";
			for (int i=0; i<frequencies[node].length; i++) 
				if (frequencies[node][i]!=0)
					s+= ProteinState.toString(i) + ":" + MesquiteDouble.toString(frequencies[node][i]) + lineEnding;
			return s;
		}
		else
			return ProteinState.toString(getState(node), null, 0, false);
	}
	/*..........................................  ProteinCharacterHistory  ...................................................*/
	/**returns string describing states at nodes. */
	public String toString () {
		String s="";
		for (int i=0; i<getNumNodes(); i++)
			s += ProteinState.toString(getState(i), false);
		return s;
	}
	/*..........................................  ProteinCharacterHistory  ...................................................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public CharacterHistory adjustHistorySize(Tree tree, CharacterHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		CharacterHistory soc =charStates;
		if (charStates==null || ! (charStates.getClass() == ProteinCharacterHistory.class))
			soc = new ProteinCharacterHistory(tree.getTaxa(), numNodes);
		else if (numNodes!= charStates.getNumNodes()) 
			soc = new ProteinCharacterHistory(tree.getTaxa(), numNodes);
		else {
			soc =charStates;
		}
		soc.setParentData(getParentData());
		soc.setParentCharacter(getParentCharacter());
		((CharacterStates)soc).setExplanation(getExplanation());
		return soc;
	}
	/*..........................................  ProteinCharacterHistory  ...................................................*/
	public CharacterHistory makeHistory(Tree tree){
		return new ProteinCharacterHistory(tree.getTaxa(), tree.getNumNodeSpaces());
	}
	/*..........................................  ProteinCharacterHistory  ...................................................*/
	public CategoricalAdjustable makeAdjustable(Taxa taxa){
		return new ProteinAdjustable(taxa, taxa.getNumTaxa());
	} 
}

