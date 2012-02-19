package mesquite.molec.NCBIBlaster;

import mesquite.categ.lib.DNAData;
import mesquite.lib.MesquiteModule;
import mesquite.molec.lib.Blaster;
import mesquite.molec.lib.NCBIUtil;

public class NCBIBlaster extends Blaster {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public boolean initialize() {
		return true;
	}

	public void blastForMatches(String blastType, String sequenceName, String sequence, boolean isNucleotides, int numHits, int maxTime, StringBuffer blastResponse) {
		NCBIUtil.blastForMatches(blastType, sequenceName, sequence, isNucleotides, numHits, 300, blastResponse);
	}


	public boolean isPrerelease() {
		return true;
	}


	public String getName() {
		return "Blast NCBI Server";
	}

	public String getExplanation() {
		return "Blasts the NCBI GenBank server at NIH";
	}


}
