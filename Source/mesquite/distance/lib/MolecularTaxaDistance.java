/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.distance.lib;

import java.awt.Checkbox;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

public abstract class MolecularTaxaDistance extends TaxaDistance {
		protected double[][] distances;
		protected MCategoricalDistribution catStates;
		protected CharInclusionSet incl = null;
		int maxNumStates;
		CategoricalData data; 
		public static final boolean DEFAULTESTIMATEAMBIGUITYDIFFERENCES = true;
		public static final boolean DEFAULTCOUNTDIFFERENCESIFGAPINPAIR = false;
    	boolean estimateAmbiguityDifferences = DEFAULTESTIMATEAMBIGUITYDIFFERENCES;
    	boolean countDifferencesIfGapInPair = DEFAULTCOUNTDIFFERENCESIFGAPINPAIR;
    	MesquiteModule ownerModule;
    	boolean isDNAData=false;
    	boolean allIncluded = true;
		
		public MolecularTaxaDistance(MesquiteModule ownerModule, Taxa taxa, MCharactersDistribution observedStates, boolean estimateAmbiguityDifferences, boolean countDifferencesIfGapInPair){
			super(taxa);
			if (observedStates==null)
				return;
			this.ownerModule = ownerModule;
			this.estimateAmbiguityDifferences = estimateAmbiguityDifferences;
			this.countDifferencesIfGapInPair = countDifferencesIfGapInPair;
			data = (CategoricalData)observedStates.getParentData();
			isDNAData = data instanceof DNAData;
			isDNAData = false;
			
			if (data !=null) {
				incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
				if (incl!=null && !incl.allBitsOn()) 
					allIncluded=false;
			}
				
			catStates = (MCategoricalDistribution)observedStates;
			distances= new double[getNumTaxa()][getNumTaxa()];
		}

		public void copyDistanceTriangle() {
			for (int taxon1=0; taxon1<getNumTaxa(); taxon1++)
				distances[taxon1][taxon1]= 0.0;  
			for (int taxon1=0; taxon1<getNumTaxa(); taxon1++)
				for (int taxon2=taxon1; taxon2<getNumTaxa(); taxon2++)
					distances[taxon2][taxon1]=distances[taxon1][taxon2];
		}
		
		public void setEstimateAmbiguityDifferences(boolean estimateAmbiguityDifferences){
			this.estimateAmbiguityDifferences = estimateAmbiguityDifferences;
		}
		public double getDistance(int taxon1, int taxon2){
			if (taxon1>=0 && taxon1<getNumTaxa() && taxon2>=0 && taxon2<getNumTaxa())
				return distances[taxon1][taxon2];
			else
				return MesquiteDouble.unassigned;
			
		}
		public abstract int getMaxState();
		
		public double[][] calcPairwiseDistance(int taxon1, int taxon2, MesquiteDouble N, MesquiteDouble D){
			if (catStates==null)
				return null;
			if (taxonBits!=null && (!taxonBits.isBitOn(taxon1) || !taxonBits.isBitOn(taxon2)))
				return null;
			int numChars = catStates.getNumChars();
			int numStates = getMaxState()+1;
			double[][] fxy = new double[numStates][numStates];
			double[][] unambiguousFxy = new double[numStates][numStates];
			for (int i=0; i<numStates;i++)
				for (int j=0; j<numStates; j++) {
					fxy[i][j]=0.0;
					unambiguousFxy[i][j]=0.0;
				}

			if (estimateAmbiguityDifferences)  // calculate pairings for unambiguous
				for (int ic=0; ic< numChars; ic++) {
					if (allIncluded || incl.isBitOn(ic)){
						long one =catStates.getState(ic, taxon1) & CategoricalState.dataBitsMask;
						long two = catStates.getState(ic, taxon2) & CategoricalState.dataBitsMask;
						int oneState;
						int twoState;

						if (isDNAData) {
							oneState = DNAState.getOnlyElement(one);
							twoState = DNAState.getOnlyElement(two);
						}
						else {
							oneState = CategoricalState.getOnlyElement(one);
							twoState = CategoricalState.getOnlyElement(two);
						}
					//	boolean oneIsMissingInapplicable = CategoricalState.isInapplicable(one) || CategoricalState.isUnassigned(one);
					//	boolean twoIsMissingInapplicable = CategoricalState.isInapplicable(two) || CategoricalState.isUnassigned(two);
						if (oneState>=0 && oneState<numStates && twoState>=0 && twoState<numStates){  // they have single states
							unambiguousFxy[oneState][twoState] ++;
							//unambiguousFxy[twoState][oneState] ++;
						}
						else if (CategoricalState.statesShared(one,two)) {  // states overlap !oneIsMissingInapplicable &&  !twoIsMissingInapplicable && 
							long sharedStates = CategoricalState.intersection(one,two);
							int card = CategoricalState.cardinality(sharedStates);
							for (int i=0; i<numStates; i++) {
								if (CategoricalState.isElement(sharedStates,i)) ;
									unambiguousFxy[i][i] += 1.0/card;  
							}
						}
					}
				}

			int count = 0;
			int countTotal = 0;
			double sumDiffs =0;
			double gapDifferences = 0.0;

					
			for (int ic=0; ic< numChars; ic++) {
				if (allIncluded || incl.isBitOn(ic)){
					count++;
					countTotal++;
					long oneAllBits =catStates.getState(ic, taxon1);
					long twoAllBits = catStates.getState(ic, taxon2);
					long one = oneAllBits & CategoricalState.dataBitsMask;
					long two = twoAllBits & CategoricalState.dataBitsMask;
					int oneState;
					int twoState;
					if (isDNAData) {
						oneState = DNAState.getOnlyElement(one);
						twoState = DNAState.getOnlyElement(two);
					}
					else {
						oneState = CategoricalState.getOnlyElement(one);
						twoState = CategoricalState.getOnlyElement(two);
					}
					boolean oneIsMissingInapplicable = CategoricalState.isInapplicable(oneAllBits) || CategoricalState.isUnassigned(oneAllBits);
					boolean twoIsMissingInapplicable = CategoricalState.isInapplicable(twoAllBits) || CategoricalState.isUnassigned(twoAllBits);
					 if (countDifferencesIfGapInPair && (CategoricalState.isInapplicable(oneAllBits) != CategoricalState.isInapplicable(twoAllBits))) { 
						 gapDifferences+=1.0;
					 }
					 else if (oneState>=0 && oneState<numStates && twoState>=0 && twoState<numStates){  // they have single states
						fxy[oneState][twoState] ++;
					}
					else if (oneIsMissingInapplicable &&  twoIsMissingInapplicable) {  // both are missing or inapplicable, skip site
						count--;
					}
					else if (!countDifferencesIfGapInPair && (CategoricalState.isInapplicable(oneAllBits) ||  CategoricalState.isInapplicable(twoAllBits))) { // one is inapplicable and we are not counting these sites, skip site
						count--; //count--;
					}
					else if (!oneIsMissingInapplicable &&  !twoIsMissingInapplicable && CategoricalState.statesShared(one,two)) {  // states overlap   
						long sharedStates = CategoricalState.intersection(one,two);
						int card = CategoricalState.cardinality(sharedStates);
						for (int i=0; i<numStates; i++) {
							if (CategoricalState.isElement(sharedStates,i))
								fxy[i][i] += 1.0/card;  
						}
					}
					else {  //at least one is not inapplicable or missing, and they don't overlap
						if (CategoricalState.isUnassigned(oneAllBits) || (!countDifferencesIfGapInPair && CategoricalState.isInapplicable(oneAllBits))) 
							if (isDNAData)
								one = DNAState.fullSet();
							else if (isDNAData)
								one = ProteinState.fullSet();
						if (CategoricalState.isUnassigned(twoAllBits) || (!countDifferencesIfGapInPair && CategoricalState.isInapplicable(twoAllBits))) 
							if (isDNAData)
								two = DNAState.fullSet();
							else if (isDNAData)
								two = ProteinState.fullSet();
						
						
						//TODO: deal with polymorphism
						if (estimateAmbiguityDifferences) {// at least one of them has multiple states or is empty
							double sum = 0.0;
							double combinationCount = 0.0;
							for (int i=0; i<numStates; i++) {
								if (CategoricalState.isElement(one,i))
									for (int j=0; j<numStates; j++) {
										if (CategoricalState.isElement(two,j)) {  // i is in one, j is in two
											sum += unambiguousFxy[i][j];
											//if (i!=j) sum+= unambiguousFxy[j][i];
											combinationCount++;
											//if (i!=j) combinationCount++;
										}
									}
							}
							
							if (sum>0) {   // applicable patterns occur at least once in the unambiguous
								for (int i=0; i<numStates; i++) {
									if (CategoricalState.isElement(one,i))
										for (int j=0; j<numStates; j++) {
											if (CategoricalState.isElement(two,j)) {  // i is in one, j is in two
												fxy[i][j] += unambiguousFxy[i][j]/sum;
												//if (i!=j) fxy[i][j] += unambiguousFxy[j][i]/sum;
											}
										}
								}
							
							}
							else   //at this point we could bail if estimateAmbiguityDifferences, which PAUP seems to do, and not count this site.  However, instead we will estimate the values in another way
								if (oneIsMissingInapplicable) {  // first one is missing/inapplicable
								int card =CategoricalState.cardinality(two);
								for (int i=0; i<numStates; i++) {
									if (CategoricalState.isElement(two,i))
											fxy[i][i] += 1.0/card;
									}
							}
							else if (twoIsMissingInapplicable) {
								int card = CategoricalState.cardinality(one);
								for (int i=0; i<numStates; i++) {
									if (CategoricalState.isElement(one,i))
											fxy[i][i] += 1.0/card;
									}
							} 
							else {  //sum is zero, therefore other sites suggest no similarities, so cycle through and distribute fraction through all combinations
								for (int i=0; i<numStates; i++) {
									if (CategoricalState.isElement(one,i))
										for (int j=0; j<numStates; j++) {
											if (CategoricalState.isElement(two,j)) {  // i is in one, j is in two
												fxy[i][j] += 1.0/combinationCount;
												//if (i!=j) fxy[i][j] += 1.0/combinationCount;
											}
										}
								}
							}

						}	
					}
				}
			}

			double sumF = gapDifferences;
			double nonDiagonals = 0.0;
			for (int i=0; i<numStates;i++)
				for (int j=0; j<numStates; j++)
					sumF += fxy[i][j];
			for (int i=0; i<numStates;i++)
				nonDiagonals -= fxy[i][i];
			if (sumF==0)
				nonDiagonals = -1.0;
			else
				nonDiagonals /= sumF;

			//nonDiagonals = nonDiagonals*1.0*count/countTotal;
			nonDiagonals += 1.0;
			nonDiagonals = Math.abs(nonDiagonals);
			for (int i=0; i<numStates;i++)
				for (int j=0; j<numStates; j++)
					fxy[i][j]=fxy[i][j]/sumF;
			if (N!=null)
				N.setValue(sumF);
			if (D!=null)
				D.setValue(nonDiagonals);
			return fxy;
		}
		public double[][] getMatrix(){
			return distances;
		}
		public void distancesToLog(){
				for (int taxon1=0; taxon1<getNumTaxa(); taxon1++) {
					for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
						if (taxon1<taxon2)
							if (MesquiteDouble.isInfinite(distances[taxon1][taxon2]))
								MesquiteTrunk.mesquiteTrunk.logln("" + (taxon1+1) + "-"+ (taxon2+1) + ": INFINITE");
							else
								MesquiteTrunk.mesquiteTrunk.logln("" + (taxon1+1) + "-"+ (taxon2+1) + ": " + distances[taxon1][taxon2]);
					}					
					MesquiteTrunk.mesquiteTrunk.logln("");
				}
		}
		public boolean isSymmetrical(){
			return true;
		}
		public CompatibilityTest getCompatibilityTest(){
			return new RequiresAnyMolecularData();
		}
	}
