package mesquite.ancstates.UnambigChangesAtNodes;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class UnambigChangesAtNodes extends NumbersForNodesAndHistory {

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		return true;
	}
	public void initialize(Tree tree, MCharactersHistory charsHistory, CommandRecord commandRec) {
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
	public void visitNodes(int node, Tree tree, CharacterHistory cH, NumberArray result, CommandRecord commandRec) {
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
				visitNodes(d, tree, cH, result, commandRec);
	}
	/*.................................................................................................................*/
	public void calculateNumbers(Tree tree, MCharactersHistory charsHistory, NumberArray result, MesquiteString resultString, CommandRecord commandRec) {
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
			if (incl == null || incl.isSelected(ic)) {
				CharacterHistory cH = charsHistory.getCharacterHistory(ic);
				visitNodes(tree.getRoot(), tree, cH, result, commandRec);		
			}
		}
		saveLastResults(result);
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
