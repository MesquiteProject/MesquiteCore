/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.distance.JukesCantorDistance;
/*~~  */


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.distance.lib.*;

/* ======================================================================== */
/* incrementable, with each being based on a different matrix */
public class JukesCantorDistance extends DNATaxaDistFromMatrix {

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
		JukesCantorTD simpleTD = new JukesCantorTD( this,taxa, observedStates, getEstimateAmbiguityDifferences(), getCountDifferencesIfGapInPair());
		return simpleTD;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Jukes Cantor distance";  
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Jukes Cantor distance from a DNA matrix." ;
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

class JukesCantorTD extends DNATaxaDistance {

	public JukesCantorTD(MesquiteModule ownerModule, Taxa taxa, MCharactersDistribution observedStates, boolean estimateAmbiguityDifferences, boolean countDifferencesIfGapInPair){
		super(ownerModule, taxa, observedStates, estimateAmbiguityDifferences, countDifferencesIfGapInPair);
		MesquiteDouble N = new MesquiteDouble();
		MesquiteDouble D = new MesquiteDouble();

		setEstimateAmbiguityDifferences(((DNATaxaDistFromMatrix)ownerModule).getEstimateAmbiguityDifferences());
		for (int taxon1=0; taxon1<getNumTaxa(); taxon1++)
			for (int taxon2=taxon1; taxon2<getNumTaxa(); taxon2++) {
				double[][] fxy = calcPairwiseDistance(taxon1, taxon2, N, D);
				if (D.getValue()>=0.75)
					distances[taxon1][taxon2]= MesquiteDouble.infinite;
				else
					distances[taxon1][taxon2]=-3.0/4.0*(Math.log(1-(D.getValue()*4.0/3.0)));
			}
		copyDistanceTriangle();

		logDistancesIfDesired(ownerModule.getName());	}
	public String getName() {
		return "Jukes Cantor distance";
		}

}





