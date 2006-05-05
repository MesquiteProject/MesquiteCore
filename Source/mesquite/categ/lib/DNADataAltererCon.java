package mesquite.categ.lib;

import mesquite.lib.*;
import mesquite.lib.duties.*;

public abstract class DNADataAltererCon extends DataAltererCon {

 	 public Class getDutyClass() {
    	 	return DNADataAltererCon.class;
    	 }
 	/*.................................................................................................................*/
 	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
 	public CompatibilityTest getCompatibilityTest(){
 		return new RequiresAnyDNAData();
 	}

}
