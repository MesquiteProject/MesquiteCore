package mesquite.molec.NCBIBlaster;

import mesquite.categ.lib.DNAData;
import mesquite.lib.*;
import mesquite.molec.lib.BLASTResults;
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

	public void blastForMatches(String blastType, String sequenceName, String sequence, boolean isNucleotides, int numHits, int maxTime, double eValueCutoff, StringBuffer blastResponse, boolean writeCommand) {
		timer.timeSinceLast();
		NCBIUtil.blastForMatches(blastType, sequenceName, sequence, isNucleotides, numHits, 300, eValueCutoff, blastResponse);
		logln("Blast completed in " +timer.timeSinceLastInSeconds()+" seconds");
	}

	public String getFastaFromIDs(String[] idList, boolean isNucleotides, StringBuffer fastaBlastResults) {
		return NCBIUtil.fetchGenBankSequencesFromIDs(idList,  isNucleotides, null, false,  fastaBlastResults,  null);
	}
	
	/*.................................................................................................................*/
	public  String getTaxonomyFromID(String id, boolean isNucleotides, boolean writeLog, StringBuffer report){
		 return NCBIUtil.fetchTaxonomyFromSequenceID(id, isNucleotides, writeLog, report);
	}


	/*.................................................................................................................*/
	public  void postProcessingCleanup(BLASTResults blastResult){
		blastResult.setIDFromElement("|", 2);
	}

	public  String[] getNucleotideIDsfromProteinIDs(String[] ID){
		ID = NCBIUtil.cleanUpID(ID);
		return NCBIUtil.getNucIDsFromProtIDs(ID);
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
