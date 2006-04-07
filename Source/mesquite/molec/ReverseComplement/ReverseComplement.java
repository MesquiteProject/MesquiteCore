package mesquite.molec.ReverseComplement;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;
import mesquite.molec.lib.*;

public class ReverseComplement extends DNADataAltererCon {
 	
   	
	public boolean alterBlockInTaxon(CharacterData data, int icStart, int icEnd, int it) {
		if (data==null || !(data instanceof DNAData))
			return false;
		((DNAData)data).reverseComplement(icStart, icEnd, it, false, true);
		return true;
	}
  
	public String getName() {
		return "Reverse Complement";
	}
	
	public boolean requestPrimaryChoice() {
		return true;
	}


}
