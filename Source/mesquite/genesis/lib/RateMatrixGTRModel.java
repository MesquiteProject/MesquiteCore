/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;
import pal.substmodel.*;


/** A class that provides a General Time Reversible rate matrix. */
/* ======================================================================== */
public class RateMatrixGTRModel extends RateMatrixDNAModel {
	double[][] rateMatrix;
	double[][] rateMatrixTemp;
	TransitionProbability transProbs = null;
	boolean hasDefaultValues = true;
	GTR palGTR;
	String errorMessage="";

	boolean matricesSet = false;
	DoubleSqMatrixFields rateMatrixField;

	public RateMatrixGTRModel (CompositProbCategModel probabilityModel, double[][] rateMatrix) {
		super(probabilityModel);
		this.rateMatrix = rateMatrix;
		initRateMatrix();
 	}
	public RateMatrixGTRModel () {
		super(null);
		rateMatrix = new double[4][4];
		for (int i = 0; i<getNumStates(); i++)
			for (int j = 0; j<getNumStates(); j++)
				rateMatrix[i][j] =MesquiteDouble.unassigned;
		rateMatrix[2][3] = 1.0;
		initRateMatrix();
 	}
 	/*.................................................................................................................*/
 	void initRateMatrix() {
		//eigenVectorsWrapper = new DoubleVector[getNumStates()];
		int nStates = getNumStates();
		rateMatrixTemp = new double[nStates][nStates];
		for (int i = 0; i<getNumStates(); i++)
			for (int j = 0; j<getNumStates(); j++)
				rateMatrixTemp[i][j] = MesquiteDouble.unassigned;

				
 	}
	 /*.................................................................................................................*/
	void copyUpperRightToLowerLeft(double[][] matrix) {
		for (int i = 1; i<getNumStates(); i++) 
			for (int j = 0; j<i; j++) 
				matrix[i][j] =matrix[j][i];
	}
 	/*.................................................................................................................*/
 	void calculateDiagonal(double[][] matrix) {
		double offDiagonal;
		for (int i = 0; i<getNumStates(); i++) {
			offDiagonal = 0.0;
			for (int j = 0; j<getNumStates(); j++) 
				if (i!=j) 
					offDiagonal += matrix[i][j];
			matrix[i][i] = -offDiagonal;
 		}
 	}
 	/*.................................................................................................................*/
	public boolean isFullySpecified(){
		return rateMatrix[0][0] != MesquiteDouble.unassigned;
	}
 	/*.................................................................................................................*/
	public  CharacterModel cloneModelWithMotherLink(CharacterModel formerClone) {
		RateMatrixGTRModel model = new RateMatrixGTRModel(probabilityModel, rateMatrix);
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel pm){
		if (pm == null)
			return;
		if (pm instanceof RateMatrixGTRModel) {
			RateMatrixGTRModel gtr = (RateMatrixGTRModel)pm;
			gtr.initRateMatrix();
			for (int i = 0; i<rateMatrix.length; i++) 
				for (int j = 0; j<rateMatrix[i].length; j++) 
					gtr.rateMatrix[i][j] =rateMatrix[i][j];
		}
		super.copyToClone(pm);
	}
 	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {
		rateMatrixField = new DoubleSqMatrixFields(dialog,rateMatrix,new String[]{"A", "C", "G", "T"}, true,false,9);
	}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {
		for (int i = 0; i<getNumStates(); i++)
			for (int j = 0; j<getNumStates(); j++)
				if (j>i) 
					rateMatrixTemp[i][j] =rateMatrixField.getValue(i,j);
		copyUpperRightToLowerLeft(rateMatrixTemp);
		calculateDiagonal(rateMatrixTemp);
		return true;
	}
	/*.................................................................................................................*/
	public boolean checkOptions() {
		errorMessage = "";
		for (int i = 0; i<getNumStates(); i++)
			for (int j = 0; j<getNumStates(); j++)
				if (j>i)  {
					rateMatrixTemp[i][j] =rateMatrixField.getValue(i,j);
					if (!MesquiteDouble.isCombinable(rateMatrixTemp[i][j]) || !rateMatrixField.getValidDouble()) {
						errorMessage = "At least one of the rates is not valid.";
						return false;
					}
					if (rateMatrixTemp[i][j]<0.0) {
						errorMessage = "All rates must be greater than or equal to 0.0";
						return false;
					}
				}
		return true;
	}
	/*.................................................................................................................*/
	public String checkOptionsReport() {
		if (!checkOptions())
			return errorMessage;
		return "";
	}
 	/*.................................................................................................................*/
	public void setOptions() {
		setRateMatrix(rateMatrixTemp);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
 	/*.................................................................................................................*/
	public void setRateMatrix(double[][] rateMatrixTemp) {
		this.rateMatrix = rateMatrixTemp;
	}
 	/*.................................................................................................................*/
	public double[][] getRateMatrix() {
		return rateMatrix;
	}
	/*.................................................................................................................*/
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "RateMatrixGTR";
	}
 	/*.................................................................................................................*/
	public String toRMatrixString() {
		String s = "";		
		boolean firstElement = true;
		for (int i = 0; i<getNumStates(); i++)
			for (int j = i+1; j<getNumStates();j++) {
				if (!firstElement)
					s+= " ";
				s += MesquiteDouble.toString(rateMatrix[i][j]);
				firstElement = false;
			}
		return s;
	}
 	/*.................................................................................................................*/
	public String getNexusSpecification() {
		String s = "RMatrix = (" + toRMatrixString() +  ") ";
		return s;
	}
 	/*.................................................................................................................*/
	public void fromString (String description, MesquiteInteger stringPos, int format) {
		hasDefaultValues = false;
		ParseUtil.getToken(description, stringPos);   // RMatrix
		ParseUtil.getToken(description, stringPos);  // =
		ParseUtil.getToken(description, stringPos);  // (
		for (int i = 0; i<getNumStates(); i++)
			for (int j = i+1; j<getNumStates();j++) {
				String s = ParseUtil.getToken(description, stringPos);
				if (s.equalsIgnoreCase(")") || StringUtil.blank(s)) 
					return;
				rateMatrix[i][j] =MesquiteDouble.fromString(s);
			}
		copyUpperRightToLowerLeft(rateMatrix);
		calculateDiagonal(rateMatrix);
	}
	/*.................................................................................................................*/
	double[] freqs = new double[4];
	private void resetMatrices(Tree tree, int node) {
		for (int i = 0; i<4; i++)
			freqs[i] = getStateFreq(i, tree, node) ;
		palGTR = new GTR(rateMatrix[0][1], rateMatrix[0][2], rateMatrix[0][3], rateMatrix[1][2], rateMatrix[1][3], freqs);
		invalidateProbabilitiesAtNodes();
		matricesSet = true;
	}
 	/*.................................................................................................................*/
	final static boolean allowArchives = true;
 	/*.................................................................................................................*/
	protected void setChangeProbabilities (double branchLength, Tree tree, int node) {
	// it is in this method that one calculates the probability of change given the branch length and the rateMatrix and other parameters...
		if (probabilityModel==null) {
				MesquiteMessage.warnProgrammer("probabilityModel null!!!");
			return;
		}
		if (probabilityModel.getEquilStatesModel()==null) {
				MesquiteMessage.warnProgrammer("probabilityModel.getEquilStatesModel() null!!!");
			return;
		}
		if (probabilityModel.getEquilStatesModel().isLineageSpecific() || !matricesSet)
			resetMatrices(tree, node);
		boolean useArchives = allowArchives && !probabilityModel.getEquilStatesModel().isLineageSpecific();
		checkMatrixSizes(tree);

		if (useArchives){
			//if previous branch length was same, then don't need to recacluate since last matric stored using setChangeProbability is still good
			if (previousBranchLengths[node] == branchLength) 
				return;
			previousBranchLengths[node] = branchLength; //remember current branch length
			/**/
			//see if branch length is found among recently seen branch lengths at node, then get probabilities form archive
			int match = findMatchingArchive(node, branchLength);
			if (match>-1){
				for (int i = 0; i<getNumStates(); i++)
					for (int j = 0; j<getNumStates();j++) {
						setChangeProbability(i,j, node, archivedProbabilities[node][match][4*i+j+1]);
					}
				return;  //We reused archived probabilities; we're done!
			}
		}
		/**************/
		transProbs = palGTR.getTransitionProbability(transProbs);
		transProbs.setTime(0.0, branchLength);
		/**************/

		int d =-1;
		if (useArchives){
			//prepare to archive probabilities for future
			d= findOpenArchive(node);
			if (d>=0) 
				archivedProbabilities[node][d][0] = branchLength; //remember in place 0 the branch length
		}
		for (int i = 0; i<getNumStates(); i++)
			for (int j = 0; j<getNumStates();j++) {
				/**************/
				double p = transProbs.getTransitionProbability(i, j);
				/**************/
				setChangeProbability(i,j, node, p);
				if (useArchives && d>=0)  //archive the probability
					archivedProbabilities[node][d][4*i+j+1] = p;
			}
		//mark the next one in the archives as available the next time archiving is needed
		if (useArchives){
			if (d==maxArchivePlaces-1)
				archivedProbabilities[node][0][0] = MesquiteDouble.unassigned; //marking as open for next time
			else
				archivedProbabilities[node][d+1][0] = MesquiteDouble.unassigned; //marking as open for next time
		}
		
	}


 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "GTR rate matrix model, with rates (A<->C, A<->G, A<->T, C<->G, C<->T, G<->T): " + toRMatrixString();
	}

}

