/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.ParsimonySquared;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
/** Based largely on, and tested against, MacClade's code for ordered categorical characters*/
public class ParsimonySquared extends ParsAncStatesForModel {
	public String getName() {
		return "Parsimony Squared-change";
	}
	public String getExplanation() {
		return "Reconstructs the ancestral states of continuous characters so as to minimize the sum of squared changes (squared-change or least squares parsimony; Brownian motion likelihood)." ;
	}
	/*.................................................................................................................*/
	CharacterDistribution observedStates;
	ContinuousHistory downA, downB, upA, upB, finalC, finalD;
	double minSQLength;
	MesquiteBoolean useWeights; 
	boolean rootedMode = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (useWeights == null)
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("toggleWeight " + useWeights.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not branch lengths are used in weighting the squared changes", "[on = weight; off]", commandName, "toggleWeight")) {
			if (useWeights==null) {
				useWeights = new MesquiteBoolean(true);
				addCheckMenuItem(null, "Weight branches (sq. change)", makeCommand("toggleWeight", this), useWeights);
				useWeights.toggleValue(parser.getFirstToken(arguments));
				resetContainingMenuBar();
			}
			else
				useWeights.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	private void checkWeights() {
		if (useWeights==null) {
			useWeights = new MesquiteBoolean(true);
			addCheckMenuItem(null, "Weight branches (sq. change)", makeCommand("toggleWeight", this), useWeights);
			useWeights.setValue(true);
			resetContainingMenuBar();
		}
	}
	/*.................................................................................................................*/
	boolean warnedReticulation = false;
	boolean warnedUnbranched = false;
	boolean warnedSoftPoly = false;
	boolean warnedMissing = false;
	boolean warnedUnrooted = false;
	public boolean warn(Tree tree, ContinuousDistribution observedStates, MesquiteString resultString){
		if (tree == null || observedStates == null)
			return false;
		if (tree.hasSoftPolytomies(tree.getRoot())) {
			String message = "Trees with soft polytomies not allowed in squared-change parsimony calculations.  Calculations for one or more trees were not completed.  To change polytomies to hard, change the default setting in the Tree Defaults submenu of the Defaults submenu of the File menu, or use the Tree window's Alter/Transform Tree submenu";
			if (!warnedSoftPoly){
				discreetAlert( message);
				warnedSoftPoly = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (!tree.getRooted()) {
			String message = "Unrooted trees not allowed in squared-change parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedUnrooted){
				discreetAlert( message);
				warnedUnrooted = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		/*	if (observedStates.hasMissing(tree, tree.getRoot()) || observedStates.hasInapplicable(tree, tree.getRoot())) {
			String s ="Missing data, gaps are not currently supported by squared-change parsimony calculations.  Calculations for one or more characters were not completed.";
			if (!warnedMissing) {
				discreetAlert( s);
				warnedMissing = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		 */
		if (tree.hasUnbranchedInternals(tree.getRoot())) {
			String message = "Trees with unbranched internal nodes not allowed in squared-change parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedUnbranched) {
				discreetAlert( message);
				warnedUnbranched = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (tree.hasReticulations()) {
			String message = "Trees with reticulations not allowed in squared-change parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedReticulation) {
				discreetAlert( message);
				warnedReticulation = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/* Calculations assume tree is rooted. (i.e. gives same results as "rooted" option in MacClade*/
	SquaredReconstructor reconstructor= new SquaredReconstructor();
	boolean[] deleted;

	void calculateDeleted(Tree tree, CharacterDistribution observedStates){
		if (observedStates.hasMissing(tree, tree.getRoot()) || observedStates.hasInapplicable(tree, tree.getRoot())){
			if (deleted == null || deleted.length <  tree.getNumNodeSpaces())
				deleted = new boolean[tree.getNumNodeSpaces()];
			for (int i = 0; i<deleted.length; i++) deleted[i] = false;
			for (int it = 0; it< tree.getTaxa().getNumTaxa(); it++)
				if (observedStates.isUnassigned(it) || observedStates.isInapplicable(it)) {
					tree.virtualDeleteTaxon(it, deleted);
				}
		}
		else
			deleted = null;
	}
	/*.................................................................................................................*/
	public  void calculateStates(Tree tree, CharacterDistribution observedStates, CharacterHistory statesAtNodes, CharacterModel model, MesquiteString resultString, MesquiteNumber stepsObject) {  
		if (observedStates==null || tree == null)
			return;
		if (stepsObject!=null)
			stepsObject.setToUnassigned();
		if (warn(tree, (ContinuousDistribution)observedStates, resultString))
			return;
		this.observedStates = observedStates;
		((ContinuousHistory)statesAtNodes).setItemsAs((ContinuousDistribution)observedStates);
		((ContinuousHistory)statesAtNodes).deassignStates();
		minSQLength = 0; //not designed to be thread safe
		checkWeights();
		calculateDeleted(tree, observedStates);
		reconstructor.reconstruct(tree, (ContinuousDistribution)observedStates, useWeights.getValue(), true, deleted);
		reconstructor.placeReconstructedStates((ContinuousHistory)statesAtNodes);
		minSQLength = reconstructor.getSumSquaredLengths()[0];
		if (stepsObject != null)
			stepsObject.setValue(minSQLength);
		if (resultString!=null)
			resultString.setValue("Parsimony reconstruction (Squared) [Squared length: " + stepsObject + "]");
	}
	/*.................................................................................................................*/
	public  void calculateSteps(Tree tree, CharacterDistribution observedStates, CharacterModel model, MesquiteString resultString, MesquiteNumber stepsObject) {  
		if (observedStates==null || tree == null || stepsObject == null)
			return;
		if (stepsObject!=null)
			stepsObject.setToUnassigned();
		if (warn(tree, (ContinuousDistribution)observedStates, resultString))
			return;
		this.observedStates = observedStates;
		minSQLength = 0; //not designed to be thread safe
		checkWeights();

		calculateDeleted(tree, observedStates);
		reconstructor.reconstruct(tree, (ContinuousDistribution)observedStates, useWeights.getValue(), true, deleted);
		minSQLength = reconstructor.getSumSquaredLengths()[0];
		stepsObject.setValue(minSQLength);
		if (resultString!=null)
			resultString.setValue("Parsimony squared length: " + stepsObject + " (Squared)");
	}
	/*.................................................................................................................*/
	public boolean compatibleWithContext(CharacterModel model, CharacterDistribution observedStates) {
		return (model.getName().equalsIgnoreCase("Squared")) && observedStates instanceof ContinuousDistribution;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	public boolean isPrerelease(){
		return false; 
	}
}


