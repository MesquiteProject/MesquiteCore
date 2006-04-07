package mesquite.distance.lib;

import mesquite.categ.lib.*;
import mesquite.cont.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

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
