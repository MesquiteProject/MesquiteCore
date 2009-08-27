/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.7, August 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.categ.lib;

import mesquite.lib.*;
import mesquite.lib.duties.CharHistorySource;

public class CategStateChanges {
	int maxChangesRecorded = 10;
	int[][] min;
	int[][] max;
	double[][] avg;
	double[][] total;
	double[][][] fractionWithAmount;
	double[][][] totalWithAmount;
	double[][] totalChanges;
	boolean [][] acceptableChange;

	int numStates = 0;
	long numMappings = 0;
	long numHistories = 0;

	public CategStateChanges(int numStates, int maxChanges) {
		maxChangesRecorded = maxChanges;
		this.numStates = numStates;
		min = new int[numStates][numStates];
		max = new int[numStates][numStates];
		avg = new double[numStates][numStates];
		total = new double[numStates][numStates];
		totalChanges = new double[numStates][numStates];
		fractionWithAmount = new double[numStates][numStates][maxChangesRecorded];
		totalWithAmount = new double[numStates][numStates][maxChangesRecorded];
		acceptableChange = new boolean[numStates][numStates];
		initializeArrays();
	}
	/*.................................................................................................................*/
	public int getNumStates(){
		return numStates;
	}
	/*.................................................................................................................*/
	public void adjustNumStates(int numStatesNew){
		min =Integer2DArray.cloneIncreaseSize(min,numStatesNew, numStatesNew);
		max =Integer2DArray.cloneIncreaseSize(max,numStatesNew, numStatesNew);
		avg =Double2DArray.cloneIncreaseSize(avg,numStatesNew, numStatesNew);
		total =Double2DArray.cloneIncreaseSize(total,numStatesNew, numStatesNew);
		totalChanges =Double2DArray.cloneIncreaseSize(totalChanges,numStatesNew, numStatesNew);
		fractionWithAmount =Double2DArray.cloneIncreaseSize(fractionWithAmount,numStatesNew, numStatesNew, maxChangesRecorded);
		totalWithAmount =Double2DArray.cloneIncreaseSize(totalWithAmount,numStatesNew, numStatesNew, maxChangesRecorded);
		numStates = numStatesNew;
	}
	/*.................................................................................................................*/
	public void initializeArrays() {
		for (int i=0; i<numStates; i++) 
			for (int j=0; j<numStates; j++) {
				min[i][j]= Integer.MAX_VALUE;
				max[i][j]= 0;
				avg[i][j]=0.0;
				total[i][j]=0.0;
				totalChanges[i][j]=0.0;
				acceptableChange[i][j]=true;
				for (int k=0; k<maxChangesRecorded; k++) {
					fractionWithAmount[i][j][k] = 0.0;
					totalWithAmount[i][j][k] = 0.0;
				}
			}
	}
	
	/*.................................................................................................................*/
	public void setAcceptableChange(int i, int j, boolean b) {
		acceptableChange[i][j]=b;

	}

	/*.................................................................................................................*/
	public void zeroTotals() {
		for (int i=0; i<numStates; i++) 
			for (int j=0; j<numStates; j++) {
				total[i][j]=0.0;
				totalChanges[i][j]=0.0;
				for (int k=0; k<maxChangesRecorded; k++) {
					totalWithAmount[i][j][k] = 0.0;
				}
			}
	}


	/*.................................................................................................................*/

	public boolean addOneMapping(Tree tree, CategoricalHistory history, int node, int whichMapping) {
		if (!tree.nodeExists(node))
			node = tree.getRoot();
		int[][] array =  history.harvestStateChanges(tree, node,null);
		if (array==null ||  array.length != numStates)
			return false;
		return addOneMapping(array,false);

	}
	/*.................................................................................................................*/
	public boolean acceptableMapping(int[][] array) {
		for (int i=0; i<numStates && i<array.length; i++)
			for (int j=0; j<numStates &&j<array[i].length; j++)
				if (!acceptableChange[i][j] && array[i][j]>0)
					return false;
		return true;
	}
	/*.................................................................................................................*/

	public boolean addOneMapping(int[][] array, boolean useTotal) {
		if (array==null)
			return false;
		if (!acceptableMapping(array))
			return false;
		numMappings++;
		for (int i=0; i<numStates && i<array.length; i++)
			for (int j=0; j<numStates &&j<array[i].length; j++)
			{
				min[i][j] = MesquiteInteger.minimum(min[i][j],array[i][j]);
				max[i][j] = MesquiteInteger.maximum(max[i][j],array[i][j]);
				if (useTotal)
					total[i][j] = total[i][j]+array[i][j];
				else
					avg[i][j] = ((avg[i][j]*numMappings-1)+array[i][j])/numMappings;
				if (array[i][j]>=maxChangesRecorded)
					totalWithAmount[i][j][maxChangesRecorded-1]++;
				else
					totalWithAmount[i][j][array[i][j]]++;
				totalChanges[i][j]++;
			}
		return true;
	}
	/*.................................................................................................................*/

	public void oneMappingToString (int[][] array, StringBuffer sb, String lineStart) {
		if (array==null || sb==null)
			return;
		if (!acceptableMapping(array))
			return;
		if (StringUtil.notEmpty(lineStart))
			sb.append(lineStart+"\t");
		
		for (int i=0; i<numStates && i<array.length; i++)
			for (int j=0; j<numStates &&j<array[i].length; j++)
				if (i!=j)
					sb.append(""+array[i][j]+"\t");
		sb.append("\n");
	}
	/*.................................................................................................................*/

	public boolean mappingsAvailable(){
		return true;
	}
	/*.................................................................................................................*/

	public void addOneHistory(Tree tree, CharHistorySource historySource,int ic, int node, MesquiteInteger numMappingsSampled, int samplingLimit, MesquiteInteger newSamplingLimit, boolean queryChangeSamplingLimit, StringBuffer fullDetails, String lineStart) {
		CategoricalHistory resultStates=null;
		CategoricalHistory history = null;
		zeroTotals();
		int[][] array;
		int mappingsAdded=0;
		if (!mappingsAvailable()) {
			history = (CategoricalHistory)historySource.getMapping(0, history, null);
			 if (history.getMaxState()+1>getNumStates())
					adjustNumStates(history.getMaxState()+1);
			history.clone(resultStates);
			if (resultStates instanceof mesquite.categ.lib.CategoricalHistory){
				array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, node, null);
				if (addOneMapping(array, true)) mappingsAdded++;
				oneMappingToString(array, fullDetails, lineStart);
			
			}
		}
		else {
			long numMappings = historySource.getNumberOfMappings(tree, ic);
			if (queryChangeSamplingLimit && !MesquiteThread.isScripting() && newSamplingLimit!=null) {
				int newLimit = MesquiteInteger.queryInteger(historySource.getModuleWindow(), "Maximum number of mappings to sample", "Maximum number of mappings to sample for the character on each tree",samplingLimit, 1, Integer.MAX_VALUE);
				if (MesquiteInteger.isCombinable(newLimit))
					newSamplingLimit.setValue(newLimit);
			}
			if (numMappings == MesquiteLong.infinite || !MesquiteLong.isCombinable(numMappings)) {
				for (int i=0; i<samplingLimit; i++) {
					resultStates = (CategoricalHistory)historySource.getMapping(i, resultStates, null);
					if (resultStates instanceof mesquite.categ.lib.CategoricalHistory) {
						array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, node,null);
						if (addOneMapping(array, true)) mappingsAdded++;
						oneMappingToString(array, fullDetails,lineStart);
					}
				}
			}
			else 
				if (numMappings<=samplingLimit) {
					for (int i=0; i<numMappings; i++) {
						resultStates = (CategoricalHistory)historySource.getMapping(i, resultStates, null);
						if (resultStates instanceof mesquite.categ.lib.CategoricalHistory) {
							array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, node,null);
							if (addOneMapping(array, true)) mappingsAdded++;
							oneMappingToString(array, fullDetails,lineStart);
						}
					}
				}
				else {
					for (int i=0; i<samplingLimit; i++) {
						resultStates = (CategoricalHistory)historySource.getMapping(RandomBetween.getLongStatic(0,numMappings-1),resultStates,null);
						if (resultStates instanceof mesquite.categ.lib.CategoricalHistory) {
							array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, node, null);
							if (addOneMapping(array, true)) mappingsAdded++;
							oneMappingToString(array, fullDetails,lineStart);
						}
					}
				}

		}
		if (mappingsAdded>0)
			numHistories++;
		if (numMappingsSampled!=null) numMappingsSampled.setValue(mappingsAdded);

		if (mappingsAdded>0)
			for (int i=0; i<numStates; i++)
				for (int j=0; j<numStates; j++)
				{
					avg[i][j] = avg[i][j]+total[i][j]/mappingsAdded;
					if (totalChanges[i][j]>0 && i!=j)
						for (int k=0; k<maxChangesRecorded; k++) {
							fractionWithAmount[i][j][k] = fractionWithAmount[i][j][k]+totalWithAmount[i][j][k]/totalChanges[i][j];
						}
				}

	}
	
	/*.................................................................................................................*/

	public void addOneHistory(Tree tree, CategoricalHistory history,int node, int samplingLimit) {
		CategoricalHistory resultStates=null;
		zeroTotals();
		int[][] array;
		int mappingsAdded=0;
		if (!mappingsAvailable()) {
			history.clone(resultStates);
			if (resultStates instanceof mesquite.categ.lib.CategoricalHistory){
				array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, node, null);
				if (addOneMapping(array, true)) mappingsAdded++;
			}
		}
		else {
			long numMappings = history.getNumResolutions(tree);
			if (numMappings == MesquiteLong.infinite) {
				for (int i=0; i<samplingLimit; i++) {
					resultStates = (CategoricalHistory)history.getResolution(tree, resultStates, i);
					if (resultStates instanceof mesquite.categ.lib.CategoricalHistory) {
						array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, node,null);
						if (addOneMapping(array, true)) mappingsAdded++;
					}
				}
			}
			else if (MesquiteLong.isCombinable(numMappings))
				if (numMappings<=samplingLimit) {
					for (int i=0; i<numMappings; i++) {
						resultStates = (CategoricalHistory)history.getResolution(tree, resultStates, i);
						if (resultStates instanceof mesquite.categ.lib.CategoricalHistory) {
							array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, node,null);
							if (addOneMapping(array, true)) mappingsAdded++;
						}
					}
				}
				else {
					for (int i=0; i<samplingLimit; i++) {
						resultStates = (CategoricalHistory)history.getResolution(tree, resultStates, RandomBetween.getLongStatic(0,numMappings-1));
						if (resultStates instanceof mesquite.categ.lib.CategoricalHistory) {
							array= ((mesquite.categ.lib.CategoricalHistory)resultStates).harvestStateChanges(tree, node, null);
							if (addOneMapping(array, true)) mappingsAdded++;
						}
					}
				}

		}
		if (mappingsAdded>0)
			numHistories++;
		if (mappingsAdded>0)
			for (int i=0; i<numStates; i++)
				for (int j=0; j<numStates; j++)
				{
					avg[i][j] = avg[i][j]+total[i][j]/mappingsAdded;
					if (totalChanges[i][j]>0 && i!=j)
						for (int k=0; k<maxChangesRecorded; k++) {
							fractionWithAmount[i][j][k] = fractionWithAmount[i][j][k]+totalWithAmount[i][j][k]/totalChanges[i][j];
						}
				}

	}


	/*.................................................................................................................*/

	public void cleanUp() {
		for (int i=0; i<numStates; i++)
			for (int j=0; j<numStates; j++) {
				if (min[i][j]== Integer.MAX_VALUE)
					min[i][j] = 0;
				if (numHistories>0) {
					avg[i][j] = avg[i][j]/numHistories;
					if (i!=j) for (int k=0; k<maxChangesRecorded; k++) {
						fractionWithAmount[i][j][k] = fractionWithAmount[i][j][k]/numHistories;
					}
				}
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

	/*.................................................................................................................*/
	public String toVerboseString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Minimum, maximum, and average number of each kind across all trees\n");
		sb.append("------------------------------------\n");
		sb.append("change\tmin\tmax\tavg\n");
		for (int i=0; i<numStates; i++)
			for (int j=0; j<numStates; j++){
				if (i!=j)
					sb.append(""+i+"->"+j+" \t"+min[i][j] +"\t"+max[i][j] +"\t"+avg[i][j]+"\n"); 
			}
		sb.append("\n\n\nFraction of trees with specific number of changes of each kind\n");
		sb.append("------------------------------------\n");
		sb.append("change\t#changes\tfraction\n");
		for (int i=0; i<numStates; i++)
			for (int j=0; j<numStates; j++)
				if (i!=j) {
					for (int k=0; k<maxChangesRecorded-1; k++) {
						sb.append(""+i+"->"+j+" \t"+k +"\t"+fractionWithAmount[i][j][k]+"\n"); 
					}
					sb.append(""+i+"->"+j+" \t"+(maxChangesRecorded-1) +"+\t"+fractionWithAmount[i][j][maxChangesRecorded-1]+"\n"); 
					sb.append("\n");
				}
		return sb.toString();
	}






}
