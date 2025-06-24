/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.distance.lib;

import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.RequiresAnyDNAData;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.taxa.Taxa;

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
