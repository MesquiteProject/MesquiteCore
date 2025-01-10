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
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.categ.lib.*;

/** A class that provides root states for probabilistic models, using frequencies for the root states. 
This class must be subclassed, with the method setStateProbabilities specified. */
/* ======================================================================== */
public class  RateMatrixTiTvModel extends RateMatrixDNAModel {
	double tratio=MesquiteDouble.unassigned;
	double tratioTemp=MesquiteDouble.unassigned;
	DoubleField tratioField;
	boolean hasDefaultValues = true;
	String errorMessage;
	boolean alreadyWarned = false;

	public RateMatrixTiTvModel (CompositProbCategModel probabilityModel, double tratio) {
		super(probabilityModel);
		this.tratio = tratio;
	//	this.probabilityModel = probabilityModel;
 	}
	public RateMatrixTiTvModel () {
		super(null);
		//this.tratio = tratio;
 	}
	public boolean isFullySpecified(){
		return tratio != MesquiteDouble.unassigned;
	}
 	/*.................................................................................................................*/
	public  CharacterModel cloneModelWithMotherLink(CharacterModel formerClone) {
		RateMatrixTiTvModel model = new RateMatrixTiTvModel(probabilityModel, tratio);
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel pm){
		if (pm == null)
			return;
		if (pm instanceof RateMatrixTiTvModel) {
			RateMatrixTiTvModel gi = (RateMatrixTiTvModel) pm;
			gi.tratio = tratio;
		}
		super.copyToClone(pm);
	}
 	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {
		tratioField = dialog.addDoubleField("ti/tv ratio:",tratio, 30);
	}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {
		tratioTemp = tratioField.getValue();
		return true;
	}
 	/*.................................................................................................................*/
	public boolean checkOptions() {
		errorMessage = "";
		tratioTemp = tratioField.getValue();
		if (!MesquiteDouble.isCombinable(tratioTemp)) {
			errorMessage = "The transition/transversion ratio is not valid.";
			return false;
		}
		if (tratioTemp<=0.0) {
			errorMessage = "The transition/transversion ratio must be greater than 0.0";
			return false;
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
		setTRatio(tratioTemp);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
 	/*.................................................................................................................*/
	public void setTRatio(double tratio) {
		this.tratio = tratio;
	}
 	/*.................................................................................................................*/
	public double getTRatio() {
		return tratio;
	}
	/*.................................................................................................................*/
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "RateMatrixTiTv";
	}
 	/*.................................................................................................................*/
	public String getNexusSpecification() {
		return "tRatio = " + MesquiteDouble.toString(tratio);
	}
 	/*.................................................................................................................*/
	public void fromString (String description, MesquiteInteger stringPos, int format) {
		hasDefaultValues = false;
		ParseUtil.getToken(description, stringPos);   // shape
		ParseUtil.getToken(description, stringPos);  // =
		String s = ParseUtil.getToken(description, stringPos);
   		tratio =  MesquiteDouble.fromString(s);
	}

	/* this part of the code corrects a problem with the old version of the code (before 10/30/96) , the old
	code was only problematic when base frequencies were not all 1/4 */

	/*--------------------------------------------------------------------------------------------------
	|
	|   SetBeta
	|
	|   Set beta parameter (and associated variables) based on a given value of kappa (HKY model)
	|   or K (F84) model.
	|
	|   Note that beta is set such that mean substitution rate will be 1.  E.g., for J-C model,
	|   beta=4/3, where 12(1/4)(1/4)(4/3) = 1.
	*/
	/*--------------------------------------------------------------------
	|
	|     HkyChangeMat
	|
	|     Calculates the change probabilities for the HKY 1985 model.
	|
	*/
	protected void setChangeProbabilities (double branchLength, Tree tree, int node) {
		int		i, j;
		double	u, w, x, y, z,  pij, bigPij;
		double[] 	bigPi_j;
		double gBeta;
		double prob;
				
		bigPi_j = new double[4];
		double kappa = getKappaFromTRatio(tratio);

		gBeta = 0.5 / ((getStateFreq(0, tree, node) + getStateFreq(2, tree, node))*(getStateFreq(1, tree, node) + getStateFreq(3, tree, node)) + kappa*((getStateFreq(0, tree, node)* getStateFreq(2, tree, node)) + (getStateFreq(1, tree, node)*getStateFreq(3, tree, node))));

		bigPi_j[0] = getStateFreq(0, tree, node) + getStateFreq(2, tree, node);
		bigPi_j[1] = getStateFreq(1, tree, node) + getStateFreq(3, tree, node);
		bigPi_j[2] = getStateFreq(0, tree, node) + getStateFreq(2, tree, node);
		bigPi_j[3] = getStateFreq(1, tree, node) + getStateFreq(3, tree, node);
		
		for (i=0; i<4; i++) {
			if (bigPi_j[i]==0.0) {
				if (!alreadyWarned) {
					probabilityModel.getCurator().discreetAlert("Illegal change probablities in model of nucleotide sequence evolution, probably caused by self-contradictory model.  Results will not be valid.");
					alreadyWarned =true;
				}
			}
		}

		for (i=0; i<4; i++) {
			for (j=0; j<4; j++) {
				bigPij = bigPi_j[j];
				pij = getStateFreq(j, tree, node);
				u = 1.0/bigPij - 1.0;
				w = -gBeta * (1.0 + bigPij * (kappa - 1.0));
				x = Math.exp(-gBeta * branchLength);
				y = Math.exp(w * branchLength);
				z = (bigPij - pij) / bigPij;
				if (i == j)
					prob = pij + pij * u * x + z * y;
				else if ((i == 0 && j == 2) || (i == 2 && j == 0) || (i == 1 && j == 3) || (i == 3 && j == 1))
					prob = pij + pij * u * x - (pij/bigPij) * y;
				else
					prob =  pij * (1.0 - x);
				setChangeProbability(i,j,node,prob);
			}
		}
	}
 	/*.................................................................................................................*/
 	public void invalidateProbabilitiesAtNodes(){
 	}


	/*--------------------------------------------------------------------------------------------------
	|
	|   RtoK
	|
	|   Converts transition/transversion ratio ('r') to transition/transversion parameter used in
	|   Felsenstein's model ("K") or K2P/HKY models ("kappa").  The current base-frequency parameters
	|   must also be supplied in array 'g'.
	*/

	public double getKappaFromTRatio(double r)
	    {
	    double      atac, gtgc, agct, ag, ct, gR, gY;

	    gR = getStateFreq(0, null, 0) + getStateFreq(2, null, 0);
	    gY = getStateFreq(1, null, 0) + getStateFreq(3, null, 0);
	    agct = getStateFreq(0, null, 0)*getStateFreq(2, null, 0) + getStateFreq(1, null, 0)*getStateFreq(3, null, 0);


	    return r*gR*gY/agct;
	    }

	/*--------------------------------------------------------------------------------------------------
	|
	|   KtoR
	|
	|   Converts from transition-transversion parameter used in Felsenstein's model ("K") or K2P/HKY
	|   models (kappa) to transition/transversion ratio R.
	*/

	public double getTRatioFromKappa(double k)
	    {
	    double      atac, gtgc, agct, ag, ct, gR, gY;

	    gR = getStateFreq(0, null, 0) + getStateFreq(2, null, 0);
	    gY = getStateFreq(1, null, 0) + getStateFreq(3, null, 0);
	    agct = getStateFreq(0, null, 0)*getStateFreq(2, null, 0) + getStateFreq(1, null, 0)*getStateFreq(3, null, 0);

	    return k*agct/(gR*gY);
	    

	    }


 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "Two parameter, with separate rates for transitions and transversions, ti/tv ratio: " + tratio;
	}

}

