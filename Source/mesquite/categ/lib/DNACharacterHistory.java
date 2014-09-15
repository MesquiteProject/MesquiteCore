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
public class DNACharacterHistory extends CategoricalHistory {
	public DNACharacterHistory (Taxa taxa) {
		super(taxa);
		enforcedMaxState = 3;
	}
	public DNACharacterHistory (Taxa taxa, int num) {
		super(taxa, num);
		enforcedMaxState = 3;
		allStates=DNAState.span(0,3);
	}
	/*..........................................  DNACharacterHistory  ..................................................*/
	/**returns blank CharacterState object */
	public CharacterState getCharacterState (){
		return new DNAState();
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return DNAState.class;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return DNAData.class;
	}
	/*.................................................................................................................*/
	/**Returns full set of allowed states*/
	public long fullSet (){
		return DNAState.fullSet();
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return DNAData.DATATYPENAME;
	}
	/*..........................................  DNAState  ...................................................*/
	/**returns CharacterState at node N */
	public CharacterState getCharacterState (CharacterState cs, int N){
		if (checkIllegalNode(N, 5)) {
			if (cs!=null)
				cs.setToUnassigned();
			return cs;
		}
		DNAState c;
		if (cs == null || !(cs instanceof DNAState))
			c = new DNAState();
		else
			c = (DNAState)cs;
		c.setValue(getState(N));
		return c;
	}
	/** returns the union of all state sets*/
	public long getAllStates() {  //
		long s=0;
		for (int i=0; i< 4; i++) {
			s = CategoricalState.addToSet(s, i);
		}
		return s;
	}
	/*..........................................  DNACharacterHistory  ..................................................*/
	/**clone this; if passed s is not null, place the clone there (so as to avoid creating new object)*/
	public CharacterHistory clone(CharacterHistory s) {
		DNACharacterHistory snew;
		if ((s==null) || (s.getNumNodes()!=numNodes) || (!(s instanceof DNACharacterHistory)))
			snew = new DNACharacterHistory(getTaxa(), numNodes);
		else {
			snew = (DNACharacterHistory)s;
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
	/*..........................................  DNACharacterHistory  ..................................................*/
	public Color getDefaultColor(int maxState, int istate){
		return DNAData.getDNAColorOfState(istate);
	}
	/*..........................................  DNACharacterHistory  ..................................................*/
	/** Must be called before colors for states are used (e.g., before a tree is shaded).  Goes through all nodes to find states present, to set
	minima and maxima and record all states present. This allows the color range to be known before
	colors for particular states are requested by a tree shade, for example.*/
	public void prepareColors(Tree tree, int drawnRoot) {  
		allStates=DNAState.span(0,3);
		maxState = enforcedMaxState;
	}
	/*..........................................DNACharacterHistory................*/
	public MesquiteColorTable getColorTable(MesquiteColorTable colorTable) {
		
		if (colorTable == null || !(colorTable instanceof DNAColorTable))
			return new DNAColorTable();
		return colorTable;
	}
	/*..........................................  DNACharacterHistory  ..................................................*/
	public int getLegendStates(Color[] cs, String[] stateNames, Point[] tableMappings, MesquiteColorTable stateColors) {
		if (cs==null || stateNames == null)
			return 0;
		int colorCount=0;
		long s = allStates;
		for (int e=0; e<=maxState && colorCount<cs.length && colorCount<stateNames.length; e++) {
			if (CategoricalState.isElement (s,e)) {
				/*if (stateColors==null) {
					if (data == null)
						cs[colorCount]= getDefaultColor(maxState,e);
					else
						cs[colorCount]= ((CategoricalData)data).getColorOfState(-1,e, maxState);
				}
				else {*/
					cs[colorCount]= stateColors.getColor(maxState,e);
					if (tableMappings != null)tableMappings[colorCount] = new Point(maxState, e);

				//}
				if (data!=null && characterNumber>=0 && characterNumber<data.getNumChars() && data instanceof CategoricalData) {
					stateNames[colorCount++]=((DNAData)data).getStateName(characterNumber, e);
				}
				else {
					stateNames[colorCount++]=DNAState.toString(e);
				}
			}
		}
		/*if (colorCount<=0)
			MesquiteMessage.println("getLegendStates: no states found");
		*/
		return colorCount;
	}
	/*..........................................  DNACharacterHistory  ...................................................*/
	/**returns string describing character states at node  */
	public String toString (int node, String lineEnding) {
		if (checkIllegalNode(node, 8))
			return "";
		if (frequencies != null && frequencies[node]!=null && frequencies[node].length>0) {
			String s="";
			for (int i=0; i<frequencies[node].length; i++) 
				if (frequencies[node][i]!=0)
					s+= DNAState.toString(i) + ":" + MesquiteDouble.toString(frequencies[node][i]) + lineEnding;
			return s;
		}
		else
			return DNAState.toString(getState(node), null, 0, false);
			//return getCharacterState(null, node).toDisplayString();
	}
	/*..........................................  DNACharacterHistory  ...................................................*/
	/**returns string describing states at nodes. */
	public String toString () {
		String s="";
		for (int i=0; i<getNumNodes(); i++)
			s += DNAState.toString(getState(i), false);
		return s;
	}
	/*..........................................  DNACharacterHistory  ...................................................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public CharacterHistory adjustHistorySize(Tree tree, CharacterHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		CharacterHistory soc =charStates;
		if (charStates==null || ! (charStates.getClass() == DNACharacterHistory.class))
			soc = new DNACharacterHistory(tree.getTaxa(), numNodes);
		else if (numNodes!= charStates.getNumNodes()) 
			soc = new DNACharacterHistory(tree.getTaxa(), numNodes);
		else {
			soc =charStates;
		}
		soc.setParentData(getParentData());
		soc.setParentCharacter(getParentCharacter());
		((CharacterStates)soc).setExplanation(getExplanation());
		return soc;
	}
	/*..........................................  DNACharacterHistory  ...................................................*/
	public CharacterHistory makeHistory(Tree tree){
		return new DNACharacterHistory(tree.getTaxa(), tree.getNumNodeSpaces());
	}
	/*..........................................  DNACharacterHistory  ...................................................*/
	public CategoricalAdjustable makeAdjustable(Taxa taxa){
		return new DNACharacterAdjustable(taxa, taxa.getNumTaxa());
	} 
}

