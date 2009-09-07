/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.71, September 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ancstates.UnambigChangesAtNodes;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class UnambigChangesAtNodes extends NumbersForNodesAndHistory {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public void initialize(Tree tree, MCharactersHistory charsHistory) {
	}

	public Class getRequiredStateClass(){
		return CategoricalState.class;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}

	MesquiteTree tempTree;
	/*.................................................................................................................*/
	public void addToNodeValue(CharacterState cs, CharacterState parentCS, MesquiteNumber sum) {
		if (sum==null || cs==null || parentCS == null)
			return;
		if (!(cs instanceof CategoricalState) || !(parentCS instanceof CategoricalState))
			return;

		if (cs.isCombinable() && parentCS.isCombinable())  // neither is a gap or missing data or other impossible state
			if (!((CategoricalState)cs).statesShared((CategoricalState)parentCS)) // no states are shared
				sum.add(1);
	}
	/*.................................................................................................................*/
	public void visitNodes(int node, Tree tree, CharacterHistory cH, NumberArray result) {
		if (node != tree.getRoot()){// && tree.numberOfTerminalsInClade(node)>3){
			MesquiteNumber sum = new MesquiteNumber(0.0);
			int[] parents = tree.parentsOfNode(node);
			CharacterState cs = cH.getCharacterState (null, node);
			for (int i=0;i<parents.length; i++) {
				CharacterState parentCS = cH.getCharacterState (null, parents[i]);
				addToNodeValue(cs, parentCS,sum);
			}
			result.addValue(node, sum);
		}
		if (tree.nodeIsInternal(node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
				visitNodes(d, tree, cH, result);
	}
	/*.................................................................................................................*/
	public void calculateNumbers(Tree tree, MCharactersHistory charsHistory, NumberArray result, MesquiteString resultString) {
		if (tree==null || charsHistory==null)
			return;
		if (result==null)
			return;
		CharacterData data = charsHistory.getParentData();
		if (!(data instanceof CategoricalData))
			return;
		clearResultAndLastResult(result);
		result.zeroArray();
		if (resultString!=null)
			resultString.setValue("");
		tempTree = new MesquiteTree(tree.getTaxa());
		CharInclusionSet incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		for (int ic=0; ic<charsHistory.getNumChars(); ic++) {
			if (incl == null || incl.isBitOn(ic)) {
				CharacterHistory cH = charsHistory.getCharacterHistory(ic);
				visitNodes(tree.getRoot(), tree, cH, result);		
			}
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}

	public String getName() {
		return "Unambiguous Changes";
	}
	public String getExplanation() {
		return "Counts number of unambiguous changes along branch below based upon the character reconstruction.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

}
