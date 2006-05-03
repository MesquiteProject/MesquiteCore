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

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 107;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

}
