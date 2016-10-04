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
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class CategoricalHistory extends CategoricalAdjustable implements CharacterHistory {
	private int watchPoint=-1;
	Vector[] internodeHistories = null;  //for stochastic character mapping
	long[][] conditionalStateSets = null;

	public CategoricalHistory (Taxa taxa, int numNodes) {
		super(taxa, numNodes);
	}
	public CategoricalHistory (Taxa taxa) {
		super(taxa);
	}
	/*.........................................CategoricalAdjustable.................................................*/
	/**set all states to missing (unassigned)*/
	public void deassignStates(){
		super.deassignStates();
		conditionalStateSets = null;
		if (internodeHistories == null)
			return;
		for (int i = 0; i< numNodes && i <internodeHistories.length; i++)
			if (internodeHistories[i] != null)
				internodeHistories[i].removeAllElements();
	}	

	/*.................................................................................................................*/
	public long getNumResolutions(Tree tree) {
		MPRProcessor mprProcessor = new MPRProcessor(this);
		return mprProcessor.getNumResolutions(tree);
	}

	/*.................................................................................................................*/
	public CharacterHistory getResolution(Tree tree, CharacterHistory results, long num) {
		MPRProcessor mprProcessor = new MPRProcessor(this);
		return mprProcessor.getResolution(tree,results,num);
	}

	/*..........................................  CategoricalHistory  ..................................................*/
	public boolean hasConditionalStateSets(){
		return conditionalStateSets != null;
	}
	public void setConditionalStateSet(long s, int node, int stateAtAncestor){
		if (conditionalStateSets == null){
			conditionalStateSets = new long[numNodes][CategoricalState.maxCategoricalState+1];
		}
		conditionalStateSets[node][stateAtAncestor] = s;
	}
	public long getConditionalStateSet(int node, int stateAtAncestor){
		if (conditionalStateSets == null)
			return CategoricalState.unassigned;
		return conditionalStateSets[node][stateAtAncestor];
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	public void prepareInternodeHistories(){ //for stochastic character mapping
		internodeHistories = new Vector[numNodes];
		for (int i = 0; i< numNodes; i++)
			internodeHistories[i] = new Vector();
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	public Vector getInternodeHistoryVector(int node){ //for stochastic character mapping
		if (internodeHistories == null)
			return null;
		if (node < 0 || node>= internodeHistories.length)
			return null;
		return internodeHistories[node];
	}
	public int getNumberOfEvents(int node){
		if (internodeHistories == null)
			return 0;
		if (node < 0 || node>= internodeHistories.length)
			return 0;
		return internodeHistories[node].size();

	}
	public CategInternodeEvent getInternodeEvent(int node, int i){
		if (internodeHistories == null)
			return null;
		if (node < 0 || node>= internodeHistories.length)
			return null;
		if (i<0 || i>= internodeHistories[node].size())
			return null;
		return (CategInternodeEvent)internodeHistories[node].elementAt(i);
	}
	public void addInternodeEvent(int node, CategInternodeEvent event){
		if (internodeHistories == null)
			prepareInternodeHistories();
		if (node < 0 || node>= internodeHistories.length)
			return;
		internodeHistories[node].addElement(event);
	}
	/*..........................................  CategoricalHistory  ...................................................*/
	private void harvestChanges(Tree tree,int node,  int[][] changes, int stateBelow) {
		if (node != tree.getRoot() && tree.nodeExists(node)) {
			if (getNumberOfEvents(node)>0){
				for (int k = 0; k<getNumberOfEvents(node); k++) {
					CategInternodeEvent event = getInternodeEvent(node, k);
					long st = event.getState();
					int stateHere = CategoricalState.getOnlyElement(st);
					if (stateBelow >=0 && stateHere >=0 && stateBelow != stateHere)
						changes[stateBelow][stateHere]++;						
					stateBelow = stateHere;
				}
			}
			else {
				long st=getState(node);
				int stateHere = CategoricalState.getOnlyElement(st);
				if (stateBelow >=0 && stateHere >=0 && stateBelow != stateHere) {
					changes[stateBelow][stateHere]++;
				}
				stateBelow = stateHere;
			}
		}
		if (tree.nodeIsInternal(node)){
			for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d)) 
				harvestChanges(tree,d, changes, stateBelow);
		}

	}	
	/*..........................................   ...................................................*/

	public int[][] harvestStateChanges(Tree tree, int node, int[][] results){
		surveyStates(tree, node);
		if (results == null || results.length < maxState+1)
			results = new int[maxState+1][maxState+1];
		for (int i=0; i<results.length; i++)
			for (int k =0; k<results.length; k++)
				results[i][k]=0;
		harvestChanges(tree, node, results, CategoricalState.getOnlyElement(getState(node)));
		return results;
	}

	/*..........................................  CategoricalHistory  ...................................................*/
	/**returns string describing character states at node  */
	public String toString (int node, String lineEnding) {
		String ss = super.toString(node, lineEnding);
		int num = getNumberOfEvents(node);
		if (num>0){
			boolean showEvents = false;
			String par = " (";
			if (num>2)
				par += (num-1) + " events: ";
			else if (num>1)
				par += (num-1) + " event: ";
			for (int i= 0; i<num; i++){
				CategInternodeEvent event = getInternodeEvent(node, i);
				if (event.getChangeVersusSample()){
					showEvents = true;
					par += "\t" + CategoricalState.toString(event.getState()) + "@" + MesquiteDouble.toStringDigitsSpecified(event.getPosition(), 4);
				}

			}
			if (showEvents)
				ss += par + "\t)";
		}
		
		return ss;
	}

	/*..........................................  CategoricalHistory  ..................................................*/
	/**set the passed history to be a clone of this one (should be renamed to setToClone!);
	 *  if passed s is not null, place the clone there (so as to avoid creating new object)*/
	public CharacterHistory clone(CharacterHistory s) {
		CategoricalHistory snew;
		if ((s==null) || (s.getNumNodes()!=numNodes) || (!(s instanceof CategoricalHistory)))
			snew = new CategoricalHistory(getTaxa(), numNodes);
		else {
			snew = (CategoricalHistory)s;
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
		if (hasConditionalStateSets()) {
			for (int it = 0; it<numNodes; it++)
				for (int ist = 0; ist<= CategoricalState.maxCategoricalState; ist++)
					snew.setConditionalStateSet(getConditionalStateSet(it, ist), it, ist);
		}
		else
			snew.conditionalStateSets = null;
		snew.data = data;
		snew.characterNumber = characterNumber;  //setting parent character
		if (observedStates!=null)
			snew.setObservedStates((CharacterDistribution)observedStates.getAdjustableClone());
		if (internodeHistories == null)
			snew.internodeHistories = null;
		else {
			snew.prepareInternodeHistories();
			for (int node = 0; node<numNodes; node++){
				for (int i = 0; i<getNumberOfEvents(node); i++)
					snew.addInternodeEvent(node, getInternodeEvent(node, i).cloneEvent());
			}
		}
		snew.setExplanation(getExplanation());
		return snew;
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	/** Returns a new object indicating the states at the tips (used whether or not History is reconstruction)*/
	public CharacterDistribution getStatesAtTips(Tree tree){
		if (observedStates !=null)
			return (CharacterDistribution)observedStates.getAdjustableClone();
		else {
			CharacterState c = getCharacterState();
			CategoricalAdjustable d = (CategoricalAdjustable)c.makeAdjustableDistribution(tree.getTaxa(), tree.getTaxa().getNumTaxa());
			fillDistribution(tree, tree.getRoot(), d);
			return d;
		}
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	private void fillDistribution(Tree tree, int node, CategoricalAdjustable dist) {  
		if (tree.nodeIsTerminal(node)){
			int t = tree.taxonNumberOfNode(node);
			dist.setState(t, getState(node));
		}
		else for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			fillDistribution(tree, d, dist);
	}

	CharacterDistribution observedStates;
	/*..........................................  CategoricalHistory  ..................................................*/
	/** Returns the states in the terminal taxa (used if History is reconstruction)*/
	public CharacterDistribution getObservedStates(){
		return observedStates;
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	/** Sets the states in the terminal taxa (used if History is reconstruction)*/
	public void setObservedStates(CharacterDistribution observed){
		this.observedStates = observed;
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	private void surveyStates(Tree tree, int node) {  // establishes min, max, etc. for use in color returning
		if (checkIllegalNode(node, 9))
			return;
		long s = 0L;
		if (frequenciesExist())
			s=getSetFromFrequencies(node);
		else
			s=getState(node);
		if (getNumberOfEvents(node)>0){
			for (int k = 0; k<getNumberOfEvents(node); k++) {
				CategInternodeEvent event = getInternodeEvent(node, k);
				long st = event.getState();
				if (CategoricalState.isCombinable(st))
					s |= st;
			}
		}
		allStates|=s; 
		if ((!minFound) || (CategoricalState.minimum(s)<minState)) {
			minState=CategoricalState.minimum(s);
			minFound=true;
		}
		if ((!maxFound) || (CategoricalState.maximum(s)>maxState)) {
			maxState=CategoricalState.maximum(s);
			maxFound=true;
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			surveyStates(tree, d);
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	private void surveyTerminalStates(Tree tree, int node) {  // establishes min, max, etc. for use in color returning
		if (tree.nodeIsTerminal(node)) {
			checkIllegalNode(tree.taxonNumberOfNode(node), 10);
			long s = 0L;
			if (frequenciesExist()) 
				s = getSetFromFrequencies(tree.taxonNumberOfNode(node));
			else 
				s=getState(tree.taxonNumberOfNode(node));
			allStates|=s; 
			if ((!minFound) || (CategoricalState.minimum(s)<minState)) {
				minState=CategoricalState.minimum(s);
				minFound=true;
			}
			if ((!maxFound) || (CategoricalState.maximum(s)>maxState)) {
				maxState=CategoricalState.maximum(s);
				maxFound=true;
			}
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			surveyTerminalStates(tree, d);
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	/** Must be called before colors for states are used (e.g., before a tree is shaded).  Goes through all nodes to find states present, to set
	minima and maxima and record all states present. This allows the color range to be known before
	colors for particular states are requested by a tree shade, for example.*/
	public void prepareColors(Tree tree, int drawnRoot) {  
		minFound=false;
		maxFound=false;
		allStates=0;
		if (tree.getNumNodeSpaces()>getNumNodes()) {
			surveyTerminalStates(tree, drawnRoot);
		}
		else {
			surveyStates(tree, drawnRoot);
		}
		long s = allStates;
		if (data!=null && getParentCharacter()>=0){
			long obsAll = ((CategoricalData)data).getAllStates(getParentCharacter());
			s |= obsAll;
		}
		else if (observedStates!=null && observedStates instanceof CategoricalDistribution){
			long obsAll = ((CategoricalDistribution)observedStates).getAllStates();
			s |= obsAll;
		}
		if (enforcedMaxState == 0) {
			maxState = CategoricalState.maximum(s);//+getExtraNumFreqCategories();
		}
		else
			maxState = enforcedMaxState;
	}
	/*..........................................CategoricalHistory................*/
	public MesquiteColorTable getColorTable(MesquiteColorTable colorTable) {

		if (colorTable == null) // || !(colorTable instanceof CategColorTable))
			return new MesquiteColorTable();
		   if (colorTable.getMode() == MesquiteColorTable.COLORS_NO_BW)
			   colorTable.setMode(MesquiteColorTable.COLORS);
		return colorTable;
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	/** places into the already instantiated ColorDistribution the colors corresponding to the CharacterState, and returns the number of colors.  Uses default colors. 
		Mode is MesquiteColorTable.GRAYSCALE, COLORS, COLORS_NO_BW, or DEFAULT (default depends on subclass)*/
	public int getColorsOfState(CharacterState state, ColorDistribution colors, MesquiteColorTable colorTable) {
		if (colors==null || state == null || ! (state instanceof CategoricalState))
			return 0;
		colors.initialize();
		int colorCount=0;
		long s = ((CategoricalState)state).getValue();
		for (int i=0; i<=maxState; i++) {
			if (CategoricalState.isElement (s,i)) {
				colors.setWeight(colorCount, 1.0);
				if (data==null) 
					colors.setColor(colorCount++, colorTable.getColor(maxState,i));//HERE
				else 
					colors.setColor(colorCount++, ((CategoricalData)data).getColorOfState(characterNumber, i, maxState, colorTable));//HERE
			}
		}
		return colorCount;
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	/** places into the already instantiated ColorDistribution the colors corresponding to the CharacterState, and returns the number of colors.  Uses default colors. 
		Mode is MesquiteColorTable.GRAYSCALE, COLORS, COLORS_NO_BW, or DEFAULT (default depends on subclass)*
	public int getColorsAtNode(int node, ColorDistribution colors, boolean showWeights,int mode) {
		if (checkIllegalNode(node, 11))
			return 0;
		return getColorsAtNode(node, colors, null, showWeights, mode);
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	/** Places into ColorDistribution the colors at the node, and returns the number of colors.  Uses passed
	stateColors as color key.  If null is passed, uses color system of parent data (using maxState of this
	StsOfCharcter) or, if there is not parent data object, the default colors. *
	public int getColorsAtNode(int node, ColorDistribution colors,MesquiteColorTable stateColors, boolean showWeights) {
		return getColorsAtNode(node, colors, stateColors, showWeights, MesquiteColorTable.DEFAULT);
	}
	 */
	public Color getDefaultColor(int maxState, int istate){
		return MesquiteColorTable.getDefaultColor(maxState,istate, MesquiteColorTable.COLORS);
	}
	/*..........................................CategoricalHistory................*/
	public int getColorsAtNode(int node, ColorDistribution colors, MesquiteColorTable stateColors, boolean showWeights) {
		if (checkIllegalNode(node, 12))
			return 0;
		if (colors==null)
			return 0;
		colors.initialize();
		int colorCount=0;
		long s;
		if (frequenciesExist() && showWeights)
			s = CategoricalState.compressFromPresence(getFrequencies(node));
		else 
			s = states[node];

		for (int i=0; i<=maxState; i++) {
			if (CategoricalState.isElement (s,i)) {
				if (frequenciesExist() && showWeights)
					colors.setWeight(colorCount, getFrequency(node, i));
				else
					colors.setWeight(colorCount, 1.0);
				if (stateColors==null) {
					if (data==null) {
						colors.setColor(colorCount++, getDefaultColor(maxState,i));
					}
					else {
						colors.setColor(colorCount++, ((CategoricalData)data).getColorOfState(characterNumber, i, maxState));
					}
				}
				else {
					colors.setColor(colorCount++, stateColors.getColor(maxState,i));
				}
			}
		}
		if (extraFrequenciesExist() && showWeights)
			for (int i=0; i<getExtraNumFreqCategories(); i++) 
				if (getExtraFrequency(node,i)>0.0) {
					colors.setWeight(colorCount, getExtraFrequency(node,i));
					if (getExtraFrequencyColors()!=null)
						colors.setColor(colorCount++, getExtraFrequencyColors().getColor(i));
				}

		if (colorCount<=0) {
			colors.setColor(0, ColorDistribution.unassigned);
			colorCount = 1;
		}
		return colorCount;
	}
	/*..........................................CategoricalHistory................*/
	/** returns the vector of ColorEvents in order, e.g. for stochastic character mapping.  Return null if none */
	public ColorEventVector getColorSequenceAtNode(int node, MesquiteColorTable colorTable){
		if (checkIllegalNode(node, 1214))
			return null;
		if (getNumberOfEvents(node) <1)
			return null;

		ColorEventVector vector = new ColorEventVector();
		for (int i=0; i< getNumberOfEvents(node); i++) {

			CategInternodeEvent event = getInternodeEvent(node, i);
			Color color = null;
			int state = CategoricalState.minimum(event.getState());
			if (colorTable != null)
				color = colorTable.getColor(maxState, state);
			else if (data==null)
				color = getDefaultColor(maxState,state);
			else 
				color = ((CategoricalData)data).getColorOfState(characterNumber, state, maxState);

			vector.addElement(new ColorEvent(color, event.getPosition()));

		}
		vector.sort();
		return vector;
	}
	/*..........................................CategoricalHistory................*/
	/**Get the information about states occurring in this object for a legend.  Colors and state names of states
	are place in the passed array.  The colors for states are taken from the passed MesquiteColorTable.*/
	public int getLegendStates(Color[] cs, String[] stateNames, Point[] tableMappings, MesquiteColorTable stateColors) {
		if (cs==null || stateNames == null)
			return 0;
		int colorCount=0;
		long s = allStates;
		int maxStateLegend = maxState;

		if (data!=null && getParentCharacter()>=0){
			long obsAll = ((CategoricalData)data).getAllStates(getParentCharacter());
			s |= obsAll;
			int mso = ((CategoricalData)data).getMaxState(getParentCharacter());
			if (maxStateLegend<mso)
				maxStateLegend = mso;
		}
		for (int e=0; e<=maxStateLegend && colorCount<cs.length && colorCount<stateNames.length; e++) {
			if (CategoricalState.isElement (s,e)) {
				/*if (stateColors==null) {
					if (data == null)
						cs[colorCount]= getDefaultColor(maxStateLegend,e); //HERE
					else
						cs[colorCount]= ((CategoricalData)data).getColorOfState(-1,e, maxStateLegend);//HERE
				}
				else {*/
				if (stateColors==null)
					cs[colorCount] = Color.gray;
				else
					cs[colorCount]= stateColors.getColor(maxStateLegend,e);
				if (tableMappings != null){
					tableMappings[colorCount] = new Point(maxStateLegend, e);
				}
				//}
				if (data!=null && characterNumber>=0 && characterNumber<data.getNumChars() && data instanceof CategoricalData) {
					stateNames[colorCount++]=((CategoricalData)data).getStateName(characterNumber, e);
				}
				else
					stateNames[colorCount++]=Integer.toString(e);
			}
		}
		if (extraFrequenciesExist())
			for (int i=0; i<getExtraNumFreqCategories(); i++) {
				if (getExtraFrequencyColors()!=null)
					cs[colorCount] = getExtraFrequencyColors().getColor(i);
				String [] names = getExtraFrequencyNames();
				if (names!=null && i>=0 && i<names.length)
					stateNames[colorCount++]=names[i];
			}
		/*if (colorCount<=0)
			MesquiteMessage.println("getLegendStates: no states found");
		 */
		return colorCount;
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	/**Get the information about states occurring in this object for a legend.  Colors and state names of states
	are place in the passed array.*
	public int getLegendStates(Color[] cs, String[] stateNames, int mode) {
		return getLegendStates(cs, stateNames, null, mode);
	}
	/*..........................................  CategoricalHistory  ..................................................*/
	public CharacterHistory adjustSize(Tree tree) {
		if (tree.getNumNodeSpaces() == this.getNumNodes())
			return this;
		else {
			CategoricalHistory soc = new CategoricalHistory(tree.getTaxa(), tree.getNumNodeSpaces());
			soc.setParentData(getParentData());
			soc.setParentCharacter(getParentCharacter());
			soc.setExplanation(getExplanation());
			return soc;
		}
	}
	public CharacterHistory makeHistory(Tree tree){
		return new CategoricalHistory(tree.getTaxa(), tree.getNumNodeSpaces());
	}
	/*................................................................................................................*/
	public void polymorphToUncertainties(int N, Tree tree) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			polymorphToUncertainties(d, tree);
		}
		if (tree.nodeIsInternal(N)){
			long s = getState(N);
			if (CategoricalState.cardinality(s)>1)
				setState(N, CategoricalState.setUncertainty(s, true));
		}
	}
}


