package mesquite.distance.lib;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

public class DNATaxaDistance extends MolecularTaxaDistance {
		
	public DNATaxaDistance(MesquiteModule ownerModule, Taxa taxa, MCharactersDistribution observedStates, boolean estimateAmbiguityDifferences, boolean countDifferencesIfGapInPair){
		super(ownerModule,taxa,observedStates, estimateAmbiguityDifferences,countDifferencesIfGapInPair);
	}
		public int getMaxState(){
			return DNAState.getMaxPossibleStateStatic();
		}
		public CompatibilityTest getCompatibilityTest(){
			return new RequiresAnyDNAData();
		}
		/** used for debugging; turn this on and at least some of the distance calculators will stream their results to the log.  */
		public void logDistancesIfDesired(String name){
			if (false) {
				MesquiteTrunk.mesquiteTrunk.logln("\n\n\n||||||||||||||  " + name);
				distancesToLog();
			}
		}
	}
