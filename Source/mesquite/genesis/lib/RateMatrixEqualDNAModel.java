/* Mesquite source code (Genesis package).  Copyright 2001-2011 D. Maddison and W. Maddison. 
Version 2.75, September 2011.
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

/* ======================================================================== */
public class RateMatrixEqualDNAModel extends RateMatrixDNAModel {
	double instantaneous = 1.0;

	public RateMatrixEqualDNAModel (CompositProbCategModel probabilityModel) {
		super(probabilityModel);
	}
	/*.................................................................................................................*/
	public String getNexusSpecification(){
		return "";
	}
 	/*.................................................................................................................*/
	public  CharacterModel cloneModelWithMotherLink(CharacterModel formerClone) {
		RateMatrixEqualDNAModel model = new RateMatrixEqualDNAModel(probabilityModel);
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/*.................................................................................................................*/
 	public void invalidateProbabilitiesAtNodes(){
 	}
  	/*.................................................................................................................*/
	protected void setChangeProbabilities (double branchLength, Tree tree, int node) {
		double probChangeBase =Math.exp(-instantaneous*branchLength);
		double probChange;
		for (int i=0; i<4; i++)
			for (int j=0; j<4; j++) {
				if (i!=j)
					probChange = getStateFreq(j, tree, node) * (1- probChangeBase);
				else
					probChange = getStateFreq(j, tree, node) + (1 -getStateFreq(j, tree, node)) * probChangeBase;
				setChangeProbability(i,j, node, probChange);	
			}
	}
	public boolean isFullySpecified(){
		return true;
	}
	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {
			return true;
	}
	/*.................................................................................................................*/
	public boolean checkOptions() {
		return true;
	}
	/*.................................................................................................................*/
	public String checkOptionsReport() {
		return "";
	}
 	/*.................................................................................................................*/
	public void setOptions() {}
 	/*.................................................................................................................*/
 	/*.................................................................................................................*/
	/** returns name of model*/
	public String getName() {
		return "Single Rate";
	}
 	/*.................................................................................................................*/
	/** returns name of model*/
	public String getNEXUSName() {
		return "singleRate";
	}
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "single rate";
	}
}

