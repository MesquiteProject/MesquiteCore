package mesquite.categ.lib;

import mesquite.lib.*;

public class CategStateChanges {

	int[][] min;
	int[][] max;
	double[][] avg;
	double[][] total;
	int numStates = 0;
	long numMappings = 0;
	long numHistories = 0;

	public CategStateChanges(int numStates) {
		this.numStates = numStates;
		min = new int[numStates][numStates];
		max = new int[numStates][numStates];
		avg = new double[numStates][numStates];
		initializeArrays();
	}
	/*.................................................................................................................*/
	public void initializeArrays() {
		for (int i=0; i<numStates; i++) 
			for (int j=0; j<numStates; j++) {
				min[i][j]= Integer.MAX_VALUE;
				max[i][j]= 0;
				avg[i][j]=0.0;
			}
	}

	/*.................................................................................................................*/

	public void addOneMapping(Tree tree, CategoricalHistory history, int whichMapping) {
		int[][] array =  history.harvestStateChanges(tree, null);
		if (array==null ||  array.length != numStates)
			return;
		addOneMapping(array,false);

	}
	/*.................................................................................................................*/

	public void addOneMapping(int[][] array, boolean useTotal) {
		if (array==null)
			return;
		for (int i=0; i<numStates; i++)
			for (int j=0; j<numStates; j++){
				min[i][j] = MesquiteInteger.minimum(min[i][j],array[i][j]);
				max[i][j] = MesquiteInteger.maximum(max[i][j],array[i][j]);
				if (useTotal)
					total[i][j] = total[i][j]+array[i][j];
				else
					avg[i][j] = ((avg[i][j]*numMappings)+array[i][j])/numMappings;
			}
		numMappings++;

	}
	/*.................................................................................................................*/

	public boolean mappingsAvailable(){
		return true;
	}
	/*.................................................................................................................*/

	public void addOneHistory(Tree tree, CategoricalHistory history, int samplingLimit) {
		CategoricalHistory resultStates=null;
		numHistories++;
		int[][] array;
		if (!mappingsAvailable()) {
			history.clone(resultStates);
			if (resultStates instanceof mesquite.categ.lib.CategoricalHistory){
				array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, null);
				addOneMapping(array,true);
			}
		}
		else {
			long numMappings = history.getNumResolutions(tree);
			if (numMappings<=samplingLimit) {
				for (int i=0; i<numMappings; i++) {
					resultStates = (CategoricalHistory)history.getResolution(tree, resultStates, i);
					if (resultStates instanceof mesquite.categ.lib.CategoricalHistory) {
						array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, null);
						addOneMapping(array,true);
					}
				}
			}

		}
		if (numMappings>0)
			for (int i=0; i<numStates; i++)
				for (int j=0; j<numStates; j++){
					avg[i][j] = avg[i][j]+total[i][j]/numMappings;
				}

	}

	/*.................................................................................................................*/

	public void cleanUp() {
		if (numHistories>0)
			for (int i=0; i<numStates; i++)
				for (int j=0; j<numStates; j++){
					avg[i][j] = avg[i][j]/numHistories;
				}
	}
	
	/*.................................................................................................................*/
	public int[][] getMin() {
		return min;
	}
	/*.................................................................................................................*/
	public int[][] getMax() {
		return max;
	}
	/*.................................................................................................................*/
	public double[][] getAvg() {
		return avg;
	}




}
