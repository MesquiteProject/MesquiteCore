/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.BLASTSearch; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class BLASTSearch extends CategDataSearcher { 
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
   	/** Called to search on the data in selected cells.  Returns true if data searched*/
   	public boolean searchData(CharacterData data, MesquiteTable table){
		this.data = data;
		if (!(data instanceof DNAData || data instanceof ProteinData)){
			discreetAlert( "Only DNA or protein data can be searched using this module.");
			return false;
		}
		return searchSelectedTaxa(data,table);
	}
	/*.................................................................................................................*/
   	public boolean searchOneTaxon(CharacterData data, int it, int icStart, int icEnd){
   		if (data==null)
   			return false;
   		String firstLine = data.getTaxa().getTaxonName(it);
   		if (!StringUtil.blank(firstLine))
   			firstLine ="%3E" + StringUtil.encodeForURL(firstLine) + "%0D%0A";  //to make it a FASTA format
   		else
   			firstLine ="%3Etaxon " + it + "%0D%0A";  //to make it a FASTA format
   		StringBuffer searchBuffer = new StringBuffer(data.getNumChars());
   		for (int ic = icStart; ic<=icEnd; ic++) {
   			data.statesIntoStringBuffer(ic, it, searchBuffer, false, false, false);
   		}
   		String seq = searchBuffer.toString();
   		if (MesquiteTrunk.debugMode){
   			logln("BLAST query sequence ("+ data.getTaxa().getTaxonName(it)+"):");
   			logln(seq);
   		}
		if (!StringUtil.blank(seq) && (seq.length()>2)) {
			String url = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?"+ NCBIUtil.getMesquiteGenBankURLMarker();
			url += "&DATABASE=nr&FORMAT_TYPE=HTML";
			if (data instanceof ProteinData) 
				url += "&PROGRAM=blastp";
			else
				url += "&PROGRAM=blastn";
			url += "&CLIENT=web&SERVICE=plain&PAGE=Nucleotides&CMD=Put&QUERY=";
			MesquiteModule.showWebPage(url + firstLine+ seq, true);
			return true;
		}
		else 
			discreetAlert( "Sorry, to use the BLAST search you need to have one or more stretches of sequence selected.");
		return false;

   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "BLAST in Web Browser";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return false;
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "BLASTs selected data against GenBank.";
   	 }
 	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
}

/*
Search string into Genbank Entrez:

http://www.ncbi.nlm.nih.gov:80/entrez/query.fcgi?cmd=Search&db=nucleotide&dopt=GenBank&term=Bembidion

http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?DATABASE=nr&FORMAT_TYPE=HTML&PROGRAM=blastn&CLIENT=web&SERVICE=plain&PAGE=Nucleotides&CMD=Put&QUERY= 
http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?DATABASE=nr&HITLIST_SIZE=10&FILTER=L&EXPECT=10&FORMAT_TYPE=HTML&PROGRAM=blastn&CLIENT=web&SERVICE=plain&NCBI_GI=on&PAGE=Nucleotides&CMD=Put&QUERY= 


http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=put&PAGE=Nucleotides&program=blast&QUERY_FILE=fasta&query="CCAAGTCCTTCTTGAAGGGGGCCATTTACCCATAGAGGGTGCCAGGCCCGTAGTGACCATTTATATATTTGGGTGAGTTTCTCCTTAGAGTCGGGTTGCTTGAGAGTGCAGCTCTAAGTGGGTGGTAAACTCCATCTAAGGCTAAATATGACTGCGAAACCGATAGCGAACAAGTACCGTGAGGGAAAGTTGAAAAGAACTTTGAAGAGAGAGTTCAAGAGTACGTGAAACTGTTCAGGGGTAAACCTGTGGTGCCCGAAAGTTCGAAGGGGGAGATTC"

*/
	


