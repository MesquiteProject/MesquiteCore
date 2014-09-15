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

import mesquite.cont.lib.ContColorTable;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
/** Contains an array of  meristic character states for one character, at each of the taxa or nodes. 
 See notes under <a href = "MeristicData.html">MeristicData</a> regarding items */
public class MeristicHistory extends MeristicAdjustable  implements CharacterHistory {
	private int minState = 0;
	private boolean[] places = new boolean[11];
	private int maxState = 100;
	public MeristicHistory (Taxa taxa, int numNodes, MeristicData data) { 
		super(taxa, numNodes);
		setParentData(data);
		if (data!=null) {
			setItemsAs(data);
		}
	}
	/*..........................................MeristicHistory................*/
	public CharacterHistory clone(CharacterHistory s) {
		MeristicHistory snew;
		if ((s==null) || (s.getNumNodes()!=numNodes) || (!(s instanceof MeristicHistory)))
			snew = new MeristicHistory(getTaxa(), numNodes, (MeristicData)data); 
		else {
			snew = (MeristicHistory)s;
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
		((CharacterStates)snew).setExplanation(getExplanation());
		return snew;
	}
	/*..........................................MeristicHistory................*/
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
	/*..........................................MeristicHistory................*/
	/** Returns a new object indicating the states at the tips (used whether or not History is reconstruction)*/
	public CharacterDistribution getStatesAtTips(Tree tree){
		if (observedStates !=null)
			return (CharacterDistribution)observedStates.getAdjustableClone();
		else {
			MeristicAdjustable d = new MeristicAdjustable(tree.getTaxa(), tree.getTaxa().getNumTaxa());
			d.setItemsAs(this);
			fillDistribution(tree, tree.getRoot(), d);
			return d;
		}
	}
	/*..........................................MeristicHistory................*/
	private void fillDistribution(Tree tree, int node, MeristicAdjustable dist) {  
		if (tree.nodeIsTerminal(node)){
			int t = tree.taxonNumberOfNode(node);
			for (int i =0; i<getNumItems(); i++)
				dist.setState(t, i, getState(node, i));
		}
		else for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			fillDistribution(tree, d, dist);
	}
	/*..........................................MeristicHistory................*/

	CharacterDistribution observedStates;
	/*..........................................MeristicHistory................*/
	/** Returns the states in the terminal taxa (used if History is reconstruction)*/
	public CharacterDistribution getObservedStates(){
		return observedStates;
	}
	/*..........................................MeristicHistory................*/
	/** Sets the states in the terminal taxa (used if History is reconstruction)*/
	public void setObservedStates(CharacterDistribution observed){
		this.observedStates = observed;
	}
	/*..........................................MeristicHistory................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public CharacterHistory adjustSize(Tree tree) {
		if (tree.getNumNodeSpaces() == this.getNumNodes())
			return this;
		else {
			MeristicHistory soc = new MeristicHistory(tree.getTaxa(), tree.getNumNodeSpaces(), (MeristicData)getParentData());
			soc.setItemsAs(this);
			soc.setParentData(getParentData());
			soc.setParentCharacter(getParentCharacter());
			((CharacterStates)soc).setExplanation(getExplanation());
			return soc;
		}
	}
	/*..........................................MeristicHistory................*/
	private void calcMinMaxStates(Tree tree, int node) {
		for (int i=0; i<getNumItems(); i++) {
			int s=getState(node, i); 
			maxState = MesquiteInteger.maximum(maxState, s);
			minState = MesquiteInteger.minimum(minState, s);
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calcMinMaxStates(tree, d);
	}
	/*..........................................MeristicHistory................*/
	private void calcPlaces(Tree tree, int node) {
		for (int i=0; i<getNumItems(); i++) {
			int s=getState(node, i); 
			int place = (int)(((s-minState)*1.0/(maxState-minState))*10); 
			if (place>=0 && place<places.length)
				places[place] = true;
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calcPlaces(tree, d);
	}

	/*..........................................MeristicHistory................*/
	/** Must be called before a tree is shaded.  Goes through all nodes to find states present, to set
	minima and maxima. */
	public void prepareColors(Tree tree, int drawnRoot) {  
		minState=MesquiteInteger.unassigned;
		maxState=MesquiteInteger.unassigned;
		calcMinMaxStates(tree, drawnRoot); 
		if (getParentData()!=null && getParentCharacter()>=0){
			int ic = getParentCharacter();
			MeristicData data  = ((MeristicData)getParentData());
			for (int it = 0; it<data.getNumTaxa(); it++) {
				int s = data.getState(ic, it, 0);
				maxState = MesquiteInteger.maximum(maxState, s);
				minState = MesquiteInteger.minimum(minState, s);
			}
		}
		else if (getObservedStates()!=null){
			int ic = getParentCharacter();
			MeristicDistribution data  = (MeristicDistribution)getObservedStates();
			for (int it = 0; it<data.getNumNodes(); it++) {
				int s = data.getState(it);
				maxState = MesquiteInteger.maximum(maxState, s);
				minState = MesquiteInteger.minimum(minState, s);
			}
		}
		for (int i = 0; i<10; i++)
			places[i] = false;
		calcPlaces(tree, drawnRoot); 
	}

	/*..........................................  MeristicHistory  ..................................................*/
	/** places into the already instantiated ColorDistribution the colors corresponding to the CharacterState, and returns the number of colors.  Uses default colors. 
		Mode is MesquiteColorTable.GRAYSCALE, COLORS, COLORS_NO_BW, or DEFAULT (default depends on subclass)*/
	public int getColorsOfState(CharacterState state, ColorDistribution colors, MesquiteColorTable colorTable) {
		if (colors==null || state == null || ! (state instanceof MeristicState))
			return 0;
		colors.initialize();
		MeristicState cState = (MeristicState)state;
		int colorCount=0;
		if (cState.getNumItems() == 1) {
			int s =cState.getValue(0); 
			colors.setWeight(0, 1.0);
			if (!MesquiteInteger.isCombinable(s))
				colors.setColor(0, Color.white);
			else {
				int place = (int)(((s-minState)*1.0/(maxState-minState))*10); 
				colors.setColor(0, colorTable.getColor(10, place));//bug fixed in 1. 12 that had been introduced in 1. 10
				//colors.setColor(0, colorTable.getColor(s, minState, maxState));  1.10 to 1.11
			}
		}
		else {
			for (int i=0; i<cState.getNumItems() ; i++) {
				int s = cState.getValue(i); 
				colors.setWeight(i, 1.0/cState.getNumItems() );
				if (!MesquiteInteger.isCombinable(s))
					colors.setColor(0, Color.white);
				else {
					int place = (int)(((s-minState)*1.0/(maxState-minState))*10); 
					colors.setColor(i, colorTable.getColor(10, place)); //bug fixed in 1. 12 that had been introduced in 1. 10
					//colors.setColor(i, colorTable.getColor(s, minState, maxState));  1.10 to 1.11
				}
			}
		}

		return cState.getNumItems() ;
	}
	/*..........................................MeristicHistory................*/
	public MesquiteColorTable getColorTable(MesquiteColorTable colorTable) {
		//if (colorTable == null || !(colorTable instanceof ContColorTable))
		//	colorTable =  new ContColorTable();
		//colorTable.disableSetColor(true);
		return colorTable;
	}
	/*..........................................MeristicHistory................*/
	/** Places into Color array the colors at the node, and returns the number of colors.  Uses passed
	stateColors as color key.  If null is passed, uses color system of parent data (using maxState of this
	StsOfCharcter) or, if there is not parent data object, the default colors. */
	public int getColorsAtNode(int node, ColorDistribution colors, MesquiteColorTable stateColors, boolean showWeights) {
		for (int i=0; i<10; i++) colors.initialize();
		if (getNumItems() == 1) {
			int s =firstItem.getValue(node); 
			colors.setWeight(0, 1.0);
			if (!MesquiteInteger.isCombinable(s))
				colors.setColor(0, ColorDistribution.unassigned);
			else {
				int place = (int)(((s-minState)*1.0/(maxState-minState))*10); 
				colors.setColor(0, stateColors.getColor(10, place));  
			}
		}
		else {
			for (int i=0; i<getNumItems(); i++) {
				int s = getState(node,i);
				colors.setWeight(i, 1.0/getNumItems());
				if (!MesquiteInteger.isCombinable(s))
					colors.setColor(0, ColorDistribution.unassigned);
				else {
					int place = (int)(((s-minState)*1.0/(maxState-minState))*10); 
					colors.setColor(i, stateColors.getColor(10, place));
				}
			}
		}

		return getNumItems();
	}
	/*..........................................MeristicHistory................*/
	/** returns the vector of ColorEvents in order, e.g. for stochastic character mapping.  Return null if none.  Not yet supported for MeristicHistory */
	public ColorEventVector getColorSequenceAtNode(int node, MesquiteColorTable stateColors){
		return null;
	}
	/*..........................................MeristicHistory................*
	public int getLegendStates(Color[] cs, String[] stateNames, MesquiteColorTable stateColors) {
		return getLegendStates(cs, stateNames, stateColors, MesquiteColorTable.DEFAULT);
	}


	/*..........................................MeristicHistory................*/
	public int getLegendStates(Color[] cs, String[] stateNames, Point[] tableMappings, MesquiteColorTable stateColors) {
		int colorCount=0;

		double rangeUnit = ((maxState-minState)/10.0); 
		for (int e=0; e<=10; e++) {
			if (places[e]){
				cs[colorCount]= stateColors.getColor(10, e);
				if (tableMappings != null)
					tableMappings[colorCount] = new Point(10, e);
				stateNames[colorCount++]=MesquiteInteger.toString((int)(minState + rangeUnit*e)) + " to " + MesquiteInteger.toString((int)(minState + rangeUnit*(e+1)));
			}
		}
		return colorCount;
	}
	/*..........................................MeristicHistory................*
	public int getLegendStates(Color[] cs, String[] stateNames, int mode) {
		return getLegendStates(cs, stateNames, null, mode);
	}
	 */
}


