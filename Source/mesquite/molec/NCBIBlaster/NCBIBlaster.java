package mesquite.molec.NCBIBlaster;

import mesquite.categ.lib.DNAData;
import mesquite.lib.*;
import mesquite.molec.lib.Blaster;
import mesquite.molec.lib.NCBIUtil;

public class NCBIBlaster extends Blaster {
	MesquiteTimer timer = new MesquiteTimer();

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		timer.start();
		return true;

	}

	public boolean initialize() {
		return true;
	}

	public void blastForMatches(String blastType, String sequenceName, String sequence, boolean isNucleotides, int numHits, int maxTime, StringBuffer blastResponse) {
		timer.timeSinceLast();
		NCBIUtil.blastForMatches(blastType, sequenceName, sequence, isNucleotides, numHits, 300, blastResponse);
		logln("Blast completed in " +timer.timeSinceLastInSeconds()+" seconds");
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
