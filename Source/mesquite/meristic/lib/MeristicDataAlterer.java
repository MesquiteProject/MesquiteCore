package mesquite.meristic.lib;

import mesquite.categ.lib.RequiresAnyCategoricalData;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.duties.DataAlterer;

public abstract class MeristicDataAlterer extends DataAlterer  {
  	 public Class getDutyClass() {
  	 	return MeristicDataAlterer.class;
  	 }
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMeristicData();
	}

}
