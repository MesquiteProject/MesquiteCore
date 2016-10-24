/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
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
	
	public  int getUpperLimitMaxHits(){
		return 40;
	}

	/*.................................................................................................................*/
	public String getDatabaseName () {
		return "NCBI GenBank";
	}

	public void blastForMatches(String database, String blastType,  String sequenceName, String sequence, boolean isNucleotides, int numHits, int maxTime, double eValueCutoff, int wordSize, StringBuffer blastResponse, boolean writeCommand) {
		timer.timeSinceLast();
		NCBIUtil.blastForMatches(blastType, sequenceName, sequence, isNucleotides, numHits, 300, eValueCutoff, wordSize, blastResponse);
		logln("BLAST completed in " +timer.timeSinceLastInSeconds()+" seconds");
	}

	public String getFastaFromIDs(String[] idList, boolean isNucleotides, StringBuffer fastaBlastResults, int databaseNumber) {
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
		return false;
	}


	public String getName() {
		return "BLAST NCBI GenBank Server";
	}

	public String getExplanation() {
		return "BLASTs the NCBI GenBank server at NIH";
	}


}
