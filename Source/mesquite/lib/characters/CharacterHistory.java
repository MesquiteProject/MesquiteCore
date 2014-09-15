/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import java.awt.*;

import mesquite.lib.duties.*;
import mesquite.lib.*;

/*Last documented:  April 2003 */

/* ======================================================================== */
/**Stores the states at all the nodes of a tree.  Has methods to set and get the character states, and to survey the assigned
states to determine the colors that could be used for character tracing. Often returned by modules reconstructing ancestral states. 
  See general discussion of character storage classes under CharacterState*/
public interface CharacterHistory extends AdjustableDistribution, Explainable{

	/** surveys states at nodes to prepare use of getColorsAtNode. */
	public void prepareColors(Tree tree, int drawnRoot);

	public MesquiteColorTable getColorTable(MesquiteColorTable colorTable);
	
	/** places into the already instantiated ColorDistribution the colors at the node, and returns the number of colors.  Uses default colors. 
		showWeights for indicates whether weights of states (e.g. frequencies or likelihoods for categorical states) are displayed or indicated
		Mode is MesquiteColorTable.GRAYSCALE, COLORS, COLORS_NO_BW, or DEFAULT (default depends on subclass).*/
	//public int getColorsAtNode(int node, ColorDistribution colors, int mode, boolean showWeights);
	/** places into the already instantiated ColorDistribution the colors at the node, and returns the number of colors.  Uses color table passed.*/
	public int getColorsAtNode(int node, ColorDistribution colors, MesquiteColorTable stateColors, boolean showWeights);
	
	/** returns the vector of ColorEvents in order, e.g. for stochastic character mapping.  Return null if none */
	public ColorEventVector getColorSequenceAtNode(int node, MesquiteColorTable stateColors);
	
	/** places into the already instantiated ColorDistribution the colors corresponding to the CharacterState, and returns the number of colors.  Uses default colors. 
		Mode is MesquiteColorTable.GRAYSCALE, COLORS, COLORS_NO_BW, or DEFAULT (default depends on subclass)*/
	public int getColorsOfState(CharacterState state, ColorDistribution colors, MesquiteColorTable stateColors);
	/** gets colors and statesnames of states in reconstruction.  For continuous types may break down into ranges of states
			Mode is MesquiteColorTable.GRAYSCALE, COLORS, COLORS_NO_BW, or DEFAULT (default depends on subclass)*
	public int getLegendStates(Color[] cs, String[] stateNames, int mode);
	/** gets colors and statesnames of states in reconstruction.  For continuous types may break down into ranges of states.*/
	public int getLegendStates(Color[] cs, String[] stateNames, Point[] tableMappings, MesquiteColorTable stateColors);
	
	/** clones given CharacterHistory.  May be used to generate a similar object without reference to subclass*/
	public CharacterHistory clone(CharacterHistory s);
	
	/** for MPRs, etc., gets the ith resolution of the history */
	public CharacterHistory getResolution(Tree tree, CharacterHistory resultStates, long i);
	/** for MPRs, etc., gets the number of resolutions of the history */
	public long getNumResolutions(Tree tree);
	
	/** Returns a new object indicating the states at the tips (used whether or not History is reconstruction)*/
	public CharacterDistribution getStatesAtTips(Tree tree);

	/** Returns the observed states in the terminal taxa (used if History is reconstruction)*/
	public CharacterDistribution getObservedStates();
	/** Sets the states in the terminal taxa (used if History is reconstruction)*/
	public void setObservedStates(CharacterDistribution observed);
		
	/** Adjusts the size of this to store states for each of the terminal taxa.*/
	public CharacterHistory adjustSize(Tree tree);
	/*======*/
}


