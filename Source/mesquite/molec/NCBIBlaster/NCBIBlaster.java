package mesquite.molec.NCBIBlaster;

import mesquite.categ.lib.DNAData;
import mesquite.lib.MesquiteModule;
import mesquite.molec.lib.Blaster;
import mesquite.molec.lib.NCBIUtil;

public class NCBIBlaster extends Blaster {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public void initialize() {
	}

	public void blastForMatches(String blastType, String sequenceName, String sequence, boolean isNucleotides, int numHits, int maxTime, StringBuffer blastResponse) {
		NCBIUtil.blastForMatches(blastType, sequenceName, sequence, isNucleotides, numHits, 300, blastResponse);
	}

	public String fetchGenBankSequencesFromAccessions(String[] accessionNumbers, boolean isNucleotides, MesquiteModule mod, boolean writeLog, StringBuffer report) {
		return NCBIUtil.fetchGenBankSequencesFromAccessions(accessionNumbers, isNucleotides, mod, writeLog, report);	
	}

	public String fetchTaxonomyList(String accession, boolean isNucleotides, boolean writeLog, StringBuffer report) {
		return NCBIUtil.fetchTaxonomyList(accession, isNucleotides, writeLog, report);
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
