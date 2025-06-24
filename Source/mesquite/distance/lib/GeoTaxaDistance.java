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

import mesquite.cont.lib.GeographicData;
import mesquite.cont.lib.GeographicStateTest;
import mesquite.cont.lib.MContinuousDistribution;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.MesquiteModule;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.taxa.Taxa;

public abstract class GeoTaxaDistance extends TaxaDistance {
	protected MContinuousDistribution geoStates;
	protected GeographicData data; 
	
	public GeoTaxaDistance(MesquiteModule ownerModule, Taxa taxa, MCharactersDistribution observedStates){
		super(taxa);
		if (observedStates==null)
			return;
		data = (GeographicData)observedStates.getParentData();
		
		geoStates = (MContinuousDistribution)observedStates;
	}
	
	public GeoTaxaDistance(Taxa taxa){
		super(taxa);
	}
		public CompatibilityTest getCompatibilityTest(){
			return new GeographicStateTest();
		}

	}
