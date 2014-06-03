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
package mesquite.cont.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
/** Contains an array of  continuous character states for one character, at each of the taxa or nodes. 
 See notes under <a href = "ContinuousData.html">ContinuousData</a> regarding items */
public class ContinuousHistory extends ContinuousAdjustable  implements CharacterHistory {
	private double minState = 0;
	private double maxState = 100;
	public ContinuousHistory (Taxa taxa, int numNodes, ContinuousData data) { 
		super(taxa, numNodes);
		setParentData(data);
		if (data!=null) {
			setItemsAs(data);
		}
	}
	/*..........................................ContinuousHistory................*/
	public CharacterHistory clone(CharacterHistory s) {
		ContinuousHistory snew;
		if ((s==null) || (s.getNumNodes()!=numNodes) || (!(s instanceof ContinuousHistory)))
			snew = new ContinuousHistory(getTaxa(), numNodes, (ContinuousData)data); 
		else {
			snew = (ContinuousHistory)s;
			snew.data = data;
		}
		snew.setItemsAs(this);
		for (int i=0; i<numNodes; i++) {
			for (int im = 0; im<getNumItems(); im++)
				snew.setState(i, im, getState(i, im));
		}
		snew.characterNumber = characterNumber;
		if (observedStates!=null)
			snew.setObservedStates((CharacterDistribution)observedStates.getAdjustableClone());
		snew.setExplanation(getExplanation());
		return snew;
	}
	/*..........................................ContinuousHistory................*/
	/** for MPRs, etc., gets the ith resolution of the history */
	public CharacterHistory getResolution(Tree tree, CharacterHistory resultStates, long i){
		resultStates = adjustHistorySize(tree, resultStates);
		clone(resultStates);
		return resultStates;
	}
	/** for MPRs, etc., gets the number of resolutions of the history */
	public long getNumResolutions(Tree tree){
		return 1;
	}
	/*..........................................ContinuousHistory................*/
	/** Returns a new object indicating the states at the tips (used whether or not History is reconstruction)*/
	public CharacterDistribution getStatesAtTips(Tree tree){
		if (observedStates !=null)
			return (CharacterDistribution)observedStates.getAdjustableClone();
		else {
			ContinuousAdjustable d = new ContinuousAdjustable(tree.getTaxa(), tree.getTaxa().getNumTaxa());
			d.setItemsAs(this);
			fillDistribution(tree, tree.getRoot(), d);
			return d;
		}
	}
	/*..........................................ContinuousHistory................*/
	private void fillDistribution(Tree tree, int node, ContinuousAdjustable dist) {  
		if (tree.nodeIsTerminal(node)){
			int t = tree.taxonNumberOfNode(node);
			for (int i =0; i<getNumItems(); i++)
				dist.setState(t, i, getState(node, i));
		}
		else for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			fillDistribution(tree, d, dist);
	}
	/*..........................................ContinuousHistory................*/

	CharacterDistribution observedStates;
	/*..........................................ContinuousHistory................*/
	/** Returns the states in the terminal taxa (used if History is reconstruction)*/
	public CharacterDistribution getObservedStates(){
		return observedStates;
	}
	/*..........................................ContinuousHistory................*/
	/** Sets the states in the terminal taxa (used if History is reconstruction)*/
	public void setObservedStates(CharacterDistribution observed){
		this.observedStates = observed;
	}
	/*..........................................ContinuousHistory................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public CharacterHistory adjustSize(Tree tree) {
		if (tree.getNumNodeSpaces() == this.getNumNodes())
			return this;
		else {
			ContinuousHistory soc = new ContinuousHistory(tree.getTaxa(), tree.getNumNodeSpaces(), (ContinuousData)getParentData());
			soc.setItemsAs(this);
			soc.setParentData(getParentData());
			soc.setParentCharacter(getParentCharacter());
			((CharacterStates)soc).setExplanation(getExplanation());
			return soc;
		}
	}
	/*..........................................ContinuousHistory................*/
	public void calcMinMaxStates(Tree tree, int node) {
		for (int i=0; i<getNumItems(); i++) {
			double s=getState(node, i); 
			maxState = MesquiteDouble.maximum(maxState, s);
			minState = MesquiteDouble.minimum(minState, s);
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calcMinMaxStates(tree, d);
	}
	/*..........................................................*/
	public double getMinState(){
		return minState;
	}
	/*..........................................................*/
	public double getMaxState(){
		return maxState;
	}
	/*..........................................ContinuousHistory................*/
	/** Must be called before a tree is shaded.  Goes through all nodes to find states present, to set
	minima and maxima. */
	public void prepareColors(Tree tree, int drawnRoot) {  
		minState=MesquiteDouble.unassigned;
		maxState=MesquiteDouble.unassigned;
		calcMinMaxStates(tree, drawnRoot); 
		if (getParentData()!=null && getParentCharacter()>=0){
			int ic = getParentCharacter();
			ContinuousData data  = ((ContinuousData)getParentData());
			for (int it = 0; it<data.getNumTaxa(); it++) {
				double s = data.getState(ic, it, 0);
				maxState = MesquiteDouble.maximum(maxState, s);
				minState = MesquiteDouble.minimum(minState, s);
			}
		}
		else if (getObservedStates()!=null){
			int ic = getParentCharacter();
			ContinuousDistribution data  = (ContinuousDistribution)getObservedStates();
			for (int it = 0; it<data.getNumNodes(); it++) {
				double s = data.getState(it);
				maxState = MesquiteDouble.maximum(maxState, s);
				minState = MesquiteDouble.minimum(minState, s);
			}
		}
	}

	/*..........................................  ContinuousHistory  ..................................................*/
	public double getBinBoundary(int i, MesquiteColorTable colorTable){  //the boundary after bin i

		int numBinBoundaries = 10;
		double[] binBoundaries = colorTable.getPreferredBinBoundaries();
		if (binBoundaries != null)
			numBinBoundaries = binBoundaries.length;
		else
			return minState + (i+1)*(maxState-minState)/numBinBoundaries;


		double localMin, localMax;
		int localE;

		localMin = minState;
		localMax = MesquiteDouble.unassigned;
		int localNumBoundaries;
		int localMinK= -1;
		int localMaxK = numBinBoundaries;
		for (int k=0; k< numBinBoundaries; k++){  //what are the defined boundaries on either side of i?
			if (MesquiteDouble.isCombinable(binBoundaries[k])){
				if (k<i){
					localMin = binBoundaries[k];
					localMinK = k;
				}
				else if (!MesquiteDouble.isCombinable(localMax)){
					localMax = binBoundaries[k];
					localMaxK = k;
				}
			}
		}
		if (!MesquiteDouble.isCombinable(localMax))
			localMax = maxState;
		if (localMax< localMin)
			localMax = localMin;
		localE = i-localMinK-1;
		localNumBoundaries = localMaxK-localMinK;

		double rangeUnit = (localMax-localMin)*1.0;
		if (localNumBoundaries>0)
			rangeUnit = rangeUnit/localNumBoundaries;

		return localMin + rangeUnit*(localE+1);

	}

	/*..........................................  ContinuousHistory  ..................................................*/
	public int getPlace(double d, MesquiteColorTable colorTable){

		int numBinBoundaries = 10;
		double[] binBoundaries = colorTable.getPreferredBinBoundaries();

		if (binBoundaries != null) 
			numBinBoundaries = binBoundaries.length;
		else
			return (int)(((d-minState)/(maxState-minState))*numBinBoundaries);

		for (int e=0; e<=numBinBoundaries; e++) {
			double localMin, localMax;
			int localE;
			int localNumBoundaries;

			//here look for closest defined bin boundaries
			localMin = minState;
			localMax = MesquiteDouble.unassigned;
			int localMinK= -1;
			int localMaxK = numBinBoundaries;
			for (int k=0; k< numBinBoundaries; k++){
				if (MesquiteDouble.isCombinable(binBoundaries[k])){
					if (k<e){
						localMin = binBoundaries[k];
						localMinK = k;
					}
					else if (!MesquiteDouble.isCombinable(localMax)){
						localMax = binBoundaries[k];
						localMaxK = k;
					}
				}
			}
			if (!MesquiteDouble.isCombinable(localMax))
				localMax = maxState;
			if (localMax< localMin)
				localMax = localMin;
			localE = e-localMinK-1;
			localNumBoundaries = localMaxK-localMinK;
			double rangeUnit = (localMax-localMin)*1.0;
			if (localNumBoundaries != 0)
				rangeUnit = rangeUnit/localNumBoundaries; 
			if (d <=localMin + rangeUnit*(localE+1)) //d > localMin + rangeUnit*localE && 
				return e;

		}
		return numBinBoundaries;

	}
	/*..........................................  ContinuousHistory  ..................................................*/
	/** places into the already instantiated ColorDistribution the colors corresponding to the CharacterState, and returns the number of colors.  Uses default colors. 
		Mode is MesquiteColorTable.GRAYSCALE, COLORS, COLORS_NO_BW, or DEFAULT (default depends on subclass)*/
	public int getColorsOfState(CharacterState state, ColorDistribution colors, MesquiteColorTable colorTable) {
		if (colors==null || state == null || ! (state instanceof ContinuousState))
			return 0;
		int numBinBoundaries = 10;
		double[] binBoundaries = colorTable.getPreferredBinBoundaries();
		if (binBoundaries != null)
			numBinBoundaries = binBoundaries.length;
		colors.initialize();
		ContinuousState cState = (ContinuousState)state;
		if (cState.getNumItems() == 1) {
			double s =cState.getValue(0); 
			colors.setWeight(0, 1.0);
			if (!MesquiteDouble.isCombinable(s))
				colors.setColor(0, Color.white);
			else {
				int place = getPlace(s, colorTable); //(int)(((s-minState)/(maxState-minState))*numBinBoundaries); 
				colors.setColor(0, colorTable.getColor(numBinBoundaries, place));//bug fixed in 1. 12 that had been introduced in 1. 10
				//colors.setColor(0, colorTable.getColor(s, minState, maxState));  1.10 to 1.11
			}
		}
		else {
			for (int i=0; i<cState.getNumItems() ; i++) {
				double s = cState.getValue(i); 
				colors.setWeight(i, 1.0/cState.getNumItems() );
				if (!MesquiteDouble.isCombinable(s))
					colors.setColor(0, Color.white);
				else {
					int place = getPlace(s, colorTable); //(int)(((s-minState)/(maxState-minState))*numBinBoundaries); 
					colors.setColor(i, colorTable.getColor(numBinBoundaries, place)); //bug fixed in 1. 12 that had been introduced in 1. 10
					//colors.setColor(i, colorTable.getColor(s, minState, maxState));  1.10 to 1.11
				}
			}
		}

		return cState.getNumItems() ;
	}
	/*..........................................ContinuousHistory................*/
	public MesquiteColorTable getColorTable(MesquiteColorTable colorTable) {

	//	if (colorTable == null || !(colorTable instanceof ContColorTable))
		//	colorTable =  new ContColorTable();
		//	colorTable.disableSetColor(true);
		return colorTable;
	}
	/*..........................................ContinuousHistory................*/
	/** Places into Color array the colors at the node, and returns the number of colors.  Uses passed
	stateColors as color key.  If null is passed, uses color system of parent data (using maxState of this
	StsOfCharcter) or, if there is not parent data object, the default colors. */
	public int getColorsAtNode(int node, ColorDistribution colors, MesquiteColorTable stateColors, boolean showWeights) {
		int numBinBoundaries = 10;
		double[] binBoundaries = stateColors.getPreferredBinBoundaries();
		if (binBoundaries != null)
			numBinBoundaries = binBoundaries.length;
		for (int i=0; i<10; i++) colors.initialize();
		if (getNumItems() == 1) {
			double s =firstItem.getValue(node); 
			colors.setWeight(0, 1.0);
			if (!MesquiteDouble.isCombinable(s))
				colors.setColor(0, ColorDistribution.unassigned);
			else {
				int place = getPlace(s, stateColors); //(int)(((s-minState)/(maxState-minState))*numBinBoundaries); 
				colors.setColor(0, stateColors.getColor(numBinBoundaries, place));  
			}
		}
		else {
			for (int i=0; i<getNumItems(); i++) {
				double s = getState(node,i);
				colors.setWeight(i, 1.0/getNumItems());
				if (!MesquiteDouble.isCombinable(s))
					colors.setColor(0, ColorDistribution.unassigned);
				else {
					int place = getPlace(s, stateColors); //(int)(((s-minState)/(maxState-minState))*numBinBoundaries); 
					colors.setColor(i, stateColors.getColor(numBinBoundaries, place));
				}
			}
		}

		return getNumItems();
	}
	/*..........................................ContinuousHistory................*/
	/** returns the vector of ColorEvents in order, e.g. for stochastic character mapping.  Return null if none.  Not yet supported for ContinuousHistory */
	public ColorEventVector getColorSequenceAtNode(int node, MesquiteColorTable stateColors){
		return null;
	}

	/*..........................................ContinuousHistory................*/
	public int getLegendStates(Color[] cs, String[] stateNames, Point[] tableMappings, MesquiteColorTable stateColors) {
		int colorCount=0;
		int numBinBoundaries = 10;
		double[] binBoundaries = stateColors.getPreferredBinBoundaries();
		if (binBoundaries != null) 
			numBinBoundaries = binBoundaries.length;
		for (int e=0; e<=numBinBoundaries; e++) {
			if (binBoundaries == null){
				double rangeUnit = (maxState-minState)*1.0/numBinBoundaries; 
				cs[colorCount]= stateColors.getColor(numBinBoundaries,e);
				if (tableMappings != null)
					tableMappings[colorCount] = new Point(numBinBoundaries, e);
				stateNames[colorCount++]=MesquiteDouble.toString(minState + rangeUnit*e) + " to " + MesquiteDouble.toString(minState + rangeUnit*(e+1));
			}
			else {
				double localMin, localMax;
				int localE;
				int localNumBoundaries;
				//here look for closest defined bin boundaries
				localMin = minState;
				localMax = MesquiteDouble.unassigned;
				int localMinK= -1;
				int localMaxK = numBinBoundaries;
				for (int k=0; k< numBinBoundaries; k++){
					if (MesquiteDouble.isCombinable(binBoundaries[k])){
						if (k<e){
							localMin = binBoundaries[k];
							localMinK = k;
						}
						else if (!MesquiteDouble.isCombinable(localMax)){
							localMax = binBoundaries[k];
							localMaxK = k;
						}
					}
				}
				if (!MesquiteDouble.isCombinable(localMax))
					localMax = maxState;
				if (localMax< localMin)
					localMax = localMin;
				localE = e-localMinK-1;
				localNumBoundaries = localMaxK-localMinK;
				double rangeUnit = (localMax-localMin)*1.0;
				if (localNumBoundaries != 0)
					rangeUnit = rangeUnit/localNumBoundaries; 
				cs[colorCount]= stateColors.getColor(numBinBoundaries,e);
				if (tableMappings != null)
					tableMappings[colorCount] = new Point(numBinBoundaries, e);
				stateNames[colorCount++]=MesquiteDouble.toString(localMin + rangeUnit*localE) + " to " + MesquiteDouble.toString(localMin + rangeUnit*(localE+1));
			}
		}
		return colorCount;
	}
	/*..........................................ContinuousHistory................*
		public int getLegendStates(Color[] cs, String[] stateNames, Point[] tableMappings, MesquiteColorTable stateColors) {
		int colorCount=0;
		int numBinBoundaries = 10;
		double[] binBoundaries = stateColors.getPreferredBinBoundaries();
		if (binBoundaries != null)
			numBinBoundaries = binBoundaries.length;
		double rangeUnit = (maxState-minState)*1.0/numBinBoundaries; 
		for (int e=0; e<=numBinBoundaries; e++) {
				cs[colorCount]= stateColors.getColor(numBinBoundaries,e);
				if (tableMappings != null)
					tableMappings[colorCount] = new Point(numBinBoundaries, e);
			stateNames[colorCount++]=MesquiteDouble.toString(minState + rangeUnit*e) + " to " + MesquiteDouble.toString(minState + rangeUnit*(e+1));
		}
		return colorCount;
	}

	 */
}


