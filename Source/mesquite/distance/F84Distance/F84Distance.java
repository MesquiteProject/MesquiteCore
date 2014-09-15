/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.distance.F84Distance;
/*~~  */


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.distance.lib.*;

/* ======================================================================== */
/* incrementable, with each being based on a different matrix */
public class F84Distance extends DNATaxaDistFromMatrixFreq {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }	 

	public TaxaDistance getTaxaDistance(Taxa taxa, MCharactersDistribution observedStates){
		if (observedStates==null) {
			MesquiteMessage.warnProgrammer("Observed states null in "+ getName());
			return null;
		}
		if (!(observedStates.getParentData() instanceof DNAData)) {
			return null;
		}
		F84TD taxDist = new F84TD( this,taxa, observedStates, getEstimateAmbiguityDifferences(), getCountDifferencesIfGapInPair());
		taxDist.calculateDistances();
		return taxDist;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "F84 distance";  
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "F84 (Felsenstein, 1984) distance from a DNA matrix." ;
	}

	public boolean requestPrimaryChoice(){
		return true;
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

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
}

class F84TD extends DNATaxaDistance {
	DNATaxaDistFromMatrixFreq dnaTDMF;
	MCharactersDistribution observedStates;
	MesquiteModule ownerModule;
	
	public F84TD(MesquiteModule ownerModule, Taxa taxa, MCharactersDistribution observedStates, boolean estimateAmbiguityDifferences, boolean countDifferencesIfGapInPair){
		super(ownerModule, taxa, observedStates, estimateAmbiguityDifferences, countDifferencesIfGapInPair);
		 dnaTDMF = (DNATaxaDistFromMatrixFreq)ownerModule;
		 this.observedStates = observedStates;
	}
	public void calculateDistances(){
		double[] pi = null;
		MesquiteDouble N = new MesquiteDouble();
		MesquiteDouble D = new MesquiteDouble();
		double B=1.0;
		double A = 1.0;
		double C = 1.0;
		if (dnaTDMF.getBaseFreqEntireMatrix()) {
			pi = dnaTDMF.getPi(observedStates, -1, -1);			
			A = pi[1]*pi[3]/(pi[1]+pi[3])+pi[0]*pi[2]/(pi[0]+pi[2]);
			B = pi[1]*pi[3]+pi[0]*pi[2];
			C = (pi[1]+pi[3])*(pi[0]+pi[2]);
		}

//MesquiteTimer timer = new MesquiteTimer();
//timer.start();
		
		setEstimateAmbiguityDifferences(dnaTDMF.getEstimateAmbiguityDifferences());
	//	MesquiteTimer timer = new MesquiteTimer();
	//	timer.start();
		int progMax = getNumTaxa()*getNumTaxa()/2;
		ProgressIndicator progIndicator = new ProgressIndicator(dnaTDMF.getProject(),getName(), "Calculating distance matrix",progMax , true);
		if (progIndicator!=null){
			progIndicator.setButtonMode(ProgressIndicator.OFFER_CONTINUE);
			progIndicator.setOfferContinueMessageString("Are you sure you want to stop the alignment?");
			progIndicator.start();
		}
		int count=0;
		for (int taxon1=0; taxon1<getNumTaxa(); taxon1++)
			for (int taxon2=taxon1; taxon2<getNumTaxa(); taxon2++) {
				double[][] fxy = calcPairwiseDistance(taxon1, taxon2, N, D);
				if (D.getValue()>=0.75)  //TODO: check this
					distances[taxon1][taxon2]= MesquiteDouble.infinite;  
				else {
					double P = fxy[0][2] + fxy[2][0] + fxy[1][3] + fxy[3][1];
					double Q = D.getValue() - P;
					if (!dnaTDMF.getBaseFreqEntireMatrix()) {
						pi = dnaTDMF.getPi(observedStates, taxon1, taxon2);
						A = pi[1]*pi[3]/(pi[1]+pi[3])+pi[0]*pi[2]/(pi[0]+pi[2]);
						B = pi[1]*pi[3]+pi[0]*pi[2];
						C = (pi[1]+pi[3])*(pi[0]+pi[2]);
					}
					distances[taxon1][taxon2]=-2*A*Math.log(1.0-P/(2*A)-(A-B)*Q/(2*A*C)) + 2*(A-B-C)*Math.log(1-Q/(2*C));
				}
				if (progIndicator != null) {
					if (progIndicator.isAborted()) {
							progIndicator.goAway();
							return;
					}
					count++;
					progIndicator.setCurrentValue(count);
				}

			}
		if (progIndicator != null)
			progIndicator.goAway();

		copyDistanceTriangle();

		logDistancesIfDesired(dnaTDMF.getName());	
	}
	public String getName() {
		return dnaTDMF.getName();
	}
}





