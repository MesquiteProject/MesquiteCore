/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
/* Jeff Oliver May 08
 * 
 * Almost identical to UncorrectedDistance, but total distance is not 
 * divided by total number of characters.*/


package mesquite.distance.AbsoluteDistance;

import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.MCategoricalDistribution;
import mesquite.cont.lib.MContinuousDistribution;
import mesquite.distance.lib.TaxaDistFromMatrix;
import mesquite.distance.lib.TaxaDistance;
import mesquite.lib.Double2DArray;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.taxa.Taxa;

/* ======================================================================== */
/* incrementable, with each being based on a different matrix */
public class AbsoluteDistance extends TaxaDistFromMatrix {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
	/*.................................................................................................................*/
	public TaxaDistance getTaxaDistance(Taxa taxa, MCharactersDistribution observedStates){
		if (observedStates==null) {
			MesquiteMessage.warnProgrammer("Observed states null in Absolute Distance");
			return null;
		}
		SimpleTD simpleTD = new SimpleTD( taxa, observedStates);
		return simpleTD;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Absolute Distance (General)";  
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Absolute distance from a character matrix." ;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
 	/*.................................................................................................................*/
 	public int getVersionOfFirstRelease(){
 		return 250;  
 	}
}

/* ======================================================================== */
class SimpleTD extends TaxaDistance {
	double[][] distances;
	int numTaxa;
	Taxa taxa;
	CharacterData data;

	/*.................................................................................................................*/
	public SimpleTD(Taxa taxa, MCharactersDistribution observedStates){
		super(taxa);
		this.taxa = taxa;
		CharacterData data = observedStates.getParentData();
		this.data = data;
		CharInclusionSet incl = null;
		if (data !=null)
			incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
			
		if (observedStates instanceof MContinuousDistribution){
			MContinuousDistribution contStates = (MContinuousDistribution)observedStates;
			numTaxa = taxa.getNumTaxa();
			distances= new double[numTaxa][numTaxa];
			Double2DArray.deassignArray(distances);
			for (int taxon1=0; taxon1<numTaxa; taxon1++)
				for (int taxon2=0; taxon2<numTaxa; taxon2++) {
					double sumDiffs =0;
					if (taxon1!=taxon2)
						for (int ic=0; ic< contStates.getNumChars(); ic++) {
							if (incl == null || incl.isBitOn(ic)){
								double one =contStates.getState(ic, taxon1, 0);
								double two = contStates.getState(ic, taxon2, 0);
								if (MesquiteDouble.isCombinable(one) && MesquiteDouble.isCombinable(two))
									sumDiffs += Math.abs(one-two); //item 0
							}
						}
					distances[taxon1][taxon2]=sumDiffs;
				}
		}
		else if (observedStates instanceof MCategoricalDistribution){
			MCategoricalDistribution catStates = (MCategoricalDistribution)observedStates;
			numTaxa = taxa.getNumTaxa();
			distances= new double[numTaxa][numTaxa];
			for (int taxon1=0; taxon1<numTaxa; taxon1++)
				for (int taxon2=0; taxon2<numTaxa; taxon2++) {
					double total = 0;
					double sumDiffs =0;
					distances[taxon1][taxon2]=0.0;
					if (taxon1!=taxon2){
						for (int ic=0; ic< catStates.getNumChars(); ic++) {
							if (incl == null || incl.isBitOn(ic)){
								long one =catStates.getState(ic, taxon1) & CategoricalState.dataBitsMask;
								long two = catStates.getState(ic, taxon2) & CategoricalState.dataBitsMask;
								if (CategoricalState.cardinality(one)>0 || CategoricalState.cardinality(two)>0)
									total+=1.0;
								if (CategoricalState.isInapplicable(one) || CategoricalState.isInapplicable(two))  //if either inapplicable, don't count difference; should have option
									continue;
								if (CategoricalState.isUnassigned(one) || CategoricalState.isUnassigned(two))  //if either unassigned, don't count difference
									continue;
								if (one==two)  // they are the same
									continue;
								if (CategoricalState.statesShared(one, two) && (CategoricalState.isUncertain(one) || CategoricalState.isUncertain(two)))
									continue;
								sumDiffs += 1.0; 
							}
						}
						distances[taxon1][taxon2]=sumDiffs;
					}
				}
		}
	}
	/*.................................................................................................................*/
	public double getDistance(int taxon1, int taxon2){
		if (taxon1>=0 && taxon1<numTaxa && taxon2>=0 && taxon2<numTaxa)
			return distances[taxon1][taxon2];
		else
			return MesquiteDouble.unassigned;
		
	}
	
	public double[][] getReducedMatrix(boolean resetTaxaWithNoData) {
		if (resetTaxaWithNoData) {
			if (taxa==null || data==null)
				return distances;
			int  emptyTaxa = 0;
			for (int it=0; it<taxa.getNumTaxa(); it++) 
				if (!data.hasDataForTaxon(it)) {
					emptyTaxa++;
				}					
			double[][] reducedDistances = new double[getNumTaxa()-emptyTaxa][getNumTaxa()-emptyTaxa]; 
			int taxon1Count = 0;
			for (int taxon1=0; taxon1<getNumTaxa(); taxon1++) {
				if (data.hasDataForTaxon(taxon1)) {
					int taxon2Count = 0;
					for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
						if (data.hasDataForTaxon(taxon2)) {
							reducedDistances[taxon1Count][taxon2Count] = distances[taxon1][taxon2]; 
							taxon2Count++;
						}
					}	
					taxon1Count++;
				}
			}
			return reducedDistances;

		}
		else
			return distances;

	}
	public Taxa getReducedTaxa(boolean resetTaxaWithNoData, boolean useT0Names) {
		if (taxa==null || data==null)
			return taxa;
		int  emptyTaxa = 0;
		for (int it=0; it<taxa.getNumTaxa(); it++) 
			if (resetTaxaWithNoData && (!data.hasDataForTaxon(it))) {
				emptyTaxa++;
			}					
		Taxa reducedTaxa = new Taxa(taxa.getNumTaxa()-emptyTaxa);
		int count = 0;
		for (int it=0; it<taxa.getNumTaxa(); it++) 
			if (!resetTaxaWithNoData || (data.hasDataForTaxon(it))) {
				if (useT0Names)
					reducedTaxa.setTaxonName(count, "t"+it);
				else 
					reducedTaxa.setTaxonName(count, taxa.getTaxonName(it));
				count++;
			}					

		return reducedTaxa;
	}

	/*.................................................................................................................*/
	public double[][] getMatrix(){
		return distances;
	}
	/*.................................................................................................................*/
	public boolean isSymmetrical(){
		return true;
	}
}
