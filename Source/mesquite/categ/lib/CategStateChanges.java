package mesquite.categ.lib;

import mesquite.lib.Tree;
import mesquite.lib.characters.CharacterHistory;

public class CategStateChanges {
	
	int[][] min;
	int[][] max;
	int numStates = 0;
	
	public CategStateChanges(int numStates) {
		this.numStates = numStates;
		min = new int[numStates][numStates];
		max = new int[numStates][numStates];
	}
	
	public void addOneMapping(Tree tree, CategoricalHistory history, int whichMapping) {
		int[][] changesInMapping =  history.harvestStateChanges(tree, null);
		if (changesInMapping==null ||  changesInMapping.length != numStates)
			return;
		for (int i=0; i<numStates; i++)
			for (int j=0; j<numStates; j++){
				
			}

	}

	/*.................................................................................................................*
	public mesquite.categ.lib.CategStateChanges getStateChangesArray(Tree tree, CharacterHistory resultStates, int samplingLimit){
		if (tree == null || observedStates == null || !(resultStates instanceof mesquite.categ.lib.CategoricalHistory))
			return null;
		int[][] array;
		mesquite.categ.lib.CategoricalHistory categHistory = (mesquite.categ.lib.CategoricalHistory)resultStates;
		mesquite.categ.lib.CategoricalState state = (mesquite.categ.lib.CategoricalState)categHistory.getCharacterState();
		int numStates = state.getMaxPossibleStateStatic();
		mesquite.categ.lib.CategStateChanges stateChanges = new mesquite.categ.lib.CategStateChanges(state.getMaxPossibleStateStatic());
		if (!useMPRsMode.getValue() || !mprsAvailable) {
			statesAtNodes.clone(resultStates);
			if (resultStates instanceof mesquite.categ.lib.CategoricalHistory){
				array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, null);
				stateChanges.addOneMapping(array);
			}
		}
		else {
			long numMappings = statesAtNodes.getNumResolutions(tree);
			if (numMappings<=samplingLimit) {
				for (int i=0; i<numMappings; i++) {
					resultStates = statesAtNodes.getResolution(tree, resultStates, i);
					if (resultStates instanceof mesquite.categ.lib.CategoricalHistory) {
						array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, null);
						stateChanges.addOneMapping(array);
					}
				}
			}
		}
//		Debugg.println(Integer2DArray.toString(((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, null)));
		return stateChanges;
	}
	/*.................................................................................................................*/

}
