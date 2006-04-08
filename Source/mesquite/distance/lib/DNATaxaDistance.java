package mesquite.distance.lib;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

public class DNATaxaDistance extends MolecularTaxaDistance {
		
	public DNATaxaDistance(MesquiteModule ownerModule, Taxa taxa, MCharactersDistribution observedStates, boolean estimateAmbiguityDifferences){
		super(ownerModule,taxa,observedStates, estimateAmbiguityDifferences);
	}
		public int getMaxState(){
			return DNAState.getMaxPossibleStateStatic();
		}
		public CompatibilityTest getCompatibilityTest(){
			return new DNAStateOnlyTest();
		}
	}
