/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.lib; 


import java.net.*;
import java.util.Iterator;
import java.util.List;
import java.io.*;

import org.dom4j.Element;

import mesquite.lib.*;
import mesquite.lib.Bits;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.io.InterpretFastaDNA.InterpretFastaDNA;   //is this guaranteed to be an installed package?
import mesquite.io.InterpretFastaProtein.InterpretFastaProtein;   //is this guaranteed to be an installed package?
import mesquite.io.lib.*;


/* ======================================================================== */
/** A set of static methods for NCBI*/
public class NCBIUtil {
	public final static NameReference EVALUE = new NameReference("BLASTeValue");
	public final static NameReference BITSCORE = new NameReference("BLASTbitScore");
	public final static NameReference DEFINITION = new NameReference("GenBankdefinition");
	public final static NameReference ACCESSION = new NameReference("GenBankAccession");
	public final static NameReference FRAME = new NameReference("GenBankFrame");
	public final static NameReference TAXONOMY = new NameReference("GenBankTaxonomy");
	/*.................................................................................................................*/
	public static boolean responseSaysBLASTIsReady(String response) {
		if (StringUtil.blank(response))
			return false;
		String  s = StringUtil.getAllAfterSubString(response, "QBlastInfoBegin");
		if (!StringUtil.blank(s)) {
			Parser parser = new Parser(s);
			String token = parser.getNextToken();
			while (!StringUtil.blank(token)) {
				if ("Status".equalsIgnoreCase(token)) {
					token = parser.getNextToken();  // =
					token = parser.getNextToken();
					return ("READY".equalsIgnoreCase(token));
				}
			}
		}
		return true;
	}
	/*.................................................................................................................*/
	protected static String getTaxID(String response){
		Parser parser = new Parser();
		//		String[] stringList=null;
		parser.setString(response);
		if (!parser.isXMLDocument(false))   // check if XML
			return null;
		MesquiteString nextTag = new MesquiteString();
		MesquiteString attributes = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);
		while (!StringUtil.blank(nextTag.getValue())) {
			if ("eSummaryResult".equalsIgnoreCase(nextTag.getValue())) {  //make sure it has the right root tag
				parser.setString(tagContent);
				tagContent = parser.getNextXMLTaggedContent(nextTag);
				while (!StringUtil.blank(nextTag.getValue())) {
					if ("DocSum".equalsIgnoreCase(nextTag.getValue())) {
						parser.setString(tagContent);
						tagContent = parser.getNextXMLTaggedContent(nextTag, attributes);
						while (!StringUtil.blank(nextTag.getValue())) {
							if ("Item".equalsIgnoreCase(nextTag.getValue()) && Parser.attributesContains(attributes.getValue(), "Name", "TaxID")) {
								return tagContent;
							}
							tagContent = parser.getNextXMLTaggedContent(nextTag, attributes); 
						}
					}
					tagContent = parser.getNextXMLTaggedContent(nextTag); 
				}
			} 
			tagContent = parser.getNextXMLTaggedContent(nextTag);
		}
		return "";
	}
	/*.................................................................................................................*/
	public static String fetchGenBankTaxID(String id, boolean isNucleotides, boolean writeLog, StringBuffer report){ 
		try {
			URL queryURL = getFetchTaxIDAddress(id, isNucleotides);
			URLConnection connection = queryURL.openConnection();
			InputStream in = connection.getInputStream();

			StringBuffer fetchBuffer = new StringBuffer();
			int c;
			int count = 0;
			while ((c = in.read()) != -1) {
				fetchBuffer.append((char) c);
				count++;
				if (count % 200==0 && writeLog)
					MesquiteMessage.print(".");
			}
			in.close();
			String taxID = getTaxID(fetchBuffer.toString());
			return taxID;

		} catch ( Exception e ){
			// give warning
			return "";
		}
	}
	/*.................................................................................................................*/
	protected static String getLineage(String response){
		Parser parser = new Parser();
		parser.setString(response);
		if (!parser.isXMLDocument(false))   // check if XML
			return null;
		MesquiteString nextTag = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);
		while (!StringUtil.blank(nextTag.getValue())) {
			if ("TaxaSet".equalsIgnoreCase(nextTag.getValue())) {  //make sure it has the right root tag
				parser.setString(tagContent);
				tagContent = parser.getNextXMLTaggedContent(nextTag);
				while (!StringUtil.blank(nextTag.getValue())) {
					if ("Taxon".equalsIgnoreCase(nextTag.getValue())) {
						parser.setString(tagContent);
						tagContent = parser.getNextXMLTaggedContent(nextTag);
						while (!StringUtil.blank(nextTag.getValue())) {
							if ("Lineage".equalsIgnoreCase(nextTag.getValue())) {
								return tagContent;
							}
							tagContent = parser.getNextXMLTaggedContent(nextTag); 
						}
					}
					tagContent = parser.getNextXMLTaggedContent(nextTag); 
				}
			} 
			tagContent = parser.getNextXMLTaggedContent(nextTag);
		}
		return "";
	}
	/*.................................................................................................................*/
	public static String fetchGenBankTaxonomy(String taxonomyID, boolean writeLog, StringBuffer report){ 
		try {
			URL queryURL = getFetchTaxonomyAddress(taxonomyID);
			URLConnection connection = queryURL.openConnection();
			InputStream in = connection.getInputStream();

			StringBuffer fetchBuffer = new StringBuffer();
			int c;
			int count = 0;
			while ((c = in.read()) != -1) {
				fetchBuffer.append((char) c);
				count++;
				if (count % 200==0 && writeLog)
					MesquiteMessage.print(".");
			}
			in.close();
			return getLineage(fetchBuffer.toString());
		} catch ( Exception e ){
			// give warning
			return "";
		}
	}
	/*.................................................................................................................*/
	public static String fetchTaxonomyFromAccession(String accession, boolean isNucleotides, boolean writeLog, StringBuffer report){ 
		String id = getGenBankID(accession, isNucleotides,  null, false);
		if (StringUtil.blank(id))
			return "";
		String taxID = fetchGenBankTaxID(id,isNucleotides, writeLog, report);
		if (StringUtil.blank(taxID))
			return "";
		return fetchGenBankTaxonomy(taxID, writeLog, report);
	}
	/*.................................................................................................................*/
	public static String fetchTaxonomyFromSequenceID(String id, boolean isNucleotides, boolean writeLog, StringBuffer report){ 
		if (StringUtil.blank(id))
			return "";
		String taxID = fetchGenBankTaxID(id,isNucleotides, writeLog, report);
		if (StringUtil.blank(taxID))
			return "";
		return fetchGenBankTaxonomy(taxID, writeLog, report);
	}

	/*.................................................................................................................*/
	public static String getBlastPutQueryURL(String blastType, boolean isNucleotides, String sequenceName, StringBuffer sequence,  int maxHits,  double eValueCutoff, int wordSize, StringBuffer report){
		if (sequence==null)
			return null;

		String seq = sequence.toString();
		if (!StringUtil.blank(sequenceName))
			seq = "%3E" + StringUtil.encodeForURL(sequenceName) + "%0D%0A"+seq;

		if (!StringUtil.blank(seq) && (seq.length()>49)) {
			String url = "https://www.ncbi.nlm.nih.gov/blast/Blast.cgi?"+ getMesquiteGenBankURLMarker();
			url += "&DATABASE=nr&FORMAT_TYPE=HTML";
			url += "&PROGRAM=" + blastType;
			url += "&CLIENT=web&SERVICE=plain&PAGE=";
			if (isNucleotides)
				url+="Nucleotides";
			else
				url+="Protein";
			if (wordSize>0)
				url += "&WORD_SIZE=" + wordSize;
			if (eValueCutoff >= 0.0)
				url += "&EXPECT=" + eValueCutoff;
			url += "&HITLIST_SIZE="+ maxHits + "&CMD=Put&QUERY=";
			return url+seq;
		}
		else {
			if (report!=null)
				report.append("Sorry, to use the BLAST search you need to have one or more regions of 50 or more nucleotides or amino acids selected.\n");
			return null;
		}
	}
	/*.................................................................................................................*
	public static String getPutQueryURL(CharacterData data, int it, int icStart, int icEnd, int maxHits,  StringBuffer report){
		if (data==null)
			return null;
		String sequenceName = data.getTaxa().getTaxonName(it);
		StringBuffer sequence = new StringBuffer(data.getNumChars());
		for (int ic = icStart; ic<=icEnd; ic++) {
			data.statesIntoStringBuffer(ic, it, sequence, false, false, false);
		}

		if (data instanceof ProteinData) 
			return getPutQueryURL("blastp", false, sequenceName, sequence, maxHits, report);
		else
			return getPutQueryURL("blastn", true, sequenceName,  sequence, maxHits, report);
	}
	/*.................................................................................................................*/
	public static String getGetQueryURL(String rid, int numDesc, int maxHits, double eValueCutoff){
		String url = "https://www.ncbi.nlm.nih.gov/blast/Blast.cgi?"+NCBIUtil.getMesquiteGenBankURLMarker();
		url += "&CMD=Get&RID="+rid+"&FORMAT_TYPE=XML";
		if (eValueCutoff>-0.5)
			url += "&EXPECT="+eValueCutoff;
		url += "&DESCRIPTIONS="+numDesc + "&ALIGNMENTS="+ maxHits;
		return url;
	}
	/*.................................................................................................................*/
	public static boolean isNucleotides(CharacterData data){
		return (data instanceof DNAData);
	}
	/*.................................................................................................................*/
	public static String getMesquiteGenBankURLMarker(){
		return "tool=mesquite" + MesquiteTrunk.mesquiteTrunk.getVersionInt() + "&email=info@mesquiteproject.org";
	}
	/*.................................................................................................................*/
	public static URL getFetchTaxonomyAddress(String taxid)
	throws MalformedURLException {
		String query = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?"+getMesquiteGenBankURLMarker() +"&db=taxonomy&id="+taxid+"&retmode=xml";
		return new URL(query);
	}
	/*.................................................................................................................*/
	public static URL getFetchTaxIDAddress(String uid, boolean isNucleotides)
	throws MalformedURLException {
		String query = getMesquiteGenBankURLMarker() + "&db=" ;
		if (isNucleotides)
			query += "nucleotide";
		else
			query += "protein";
		query += "&id="+uid+"&retmode=xml";

		return new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?" + query);
	}
	/*.................................................................................................................*/
	public static URL getFetchSequenceAddress(String uid, String fileFormat, String retMode, boolean isNucleotides)
	throws MalformedURLException {
		String format = "fasta";
		if (!StringUtil.blank(fileFormat))
			format = fileFormat;
		String retM = "";
		if (!StringUtil.blank(retMode))
			retM = "&retMode="+retMode;

		String query = getMesquiteGenBankURLMarker() + "&db=" ;
		if (isNucleotides)
			query += "nucleotide";
		else
			query += "protein";
		
		query += "&id="+uid+"&rettype="+format+retM+"&retmax=1";

		return new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?" + query);
	}
	/*.................................................................................................................*/
	public static URL getFetchSequenceAddress(String uid, String fileFormat, boolean isNucleotides)
	throws MalformedURLException {
		return getFetchSequenceAddress(uid,fileFormat,null,isNucleotides);
	}
	/*.................................................................................................................*/
	public static URL getFetchSequenceAddress(String uid, boolean isNucleotides)
	throws MalformedURLException {
		return getFetchSequenceAddress(uid,"fasta", isNucleotides);
	}
	/*.................................................................................................................*/
	public static URL getESearchAddress(String accessionNumber, boolean nucleotides)
	throws MalformedURLException {
		String query = getMesquiteGenBankURLMarker() + "&db=" ;
		if (nucleotides)
			query += "nucleotide";
		else
			query += "protein";
		query+= "&retmode=xml&term="+accessionNumber;

		return new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?" + query);
	}
	/*.................................................................................................................*/
	public static URL getEUtilsAddressForIDToAccession(String ID, boolean nucleotides)
	throws MalformedURLException {
		String query = getMesquiteGenBankURLMarker() + "&db=" ;
		if (nucleotides)
			query += "nucleotide";
		else
			query += "protein";
		query+= "&id="+ID;

		//		https://eutils.ncbi.nlm.nih.gov/efetch.fcgi?db=nucleotide&id=91092690&retmode=xml
		return new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?" + query + "&retmode=xml");
	}
	/*.................................................................................................................*/
	public static URL getEUtilsAddressForProtToNuc(String id)
	throws MalformedURLException {
		String query = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?"+ getMesquiteGenBankURLMarker() + "&dbfrom=protein&db=nucleotide&id=\""+id +"\"";
		return new URL(query);
	}
	/*.................................................................................................................*/
	public static String[] requestGenBankIDs(String eSearchResponse){
		Parser parser = new Parser();
		String[] stringList=null;
		parser.setString(eSearchResponse);
		if (!parser.isXMLDocument(false))   // check if XML
			return null;
		MesquiteString nextTag = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);
		while (!StringUtil.blank(nextTag.getValue())) {
			if ("eSearchResult".equalsIgnoreCase(nextTag.getValue())) {  //make sure it has the right root tag
				parser.setString(tagContent);
				tagContent = parser.getNextXMLTaggedContent(nextTag);
				while (!StringUtil.blank(tagContent)) {
					if ("idList".equalsIgnoreCase(nextTag.getValue())) {
						int count=0;
						String idListContent=tagContent;
						parser.setString(idListContent);
						tagContent = parser.getNextXMLTaggedContent(nextTag);
						while (!StringUtil.blank(tagContent)) {
							if ("id".equalsIgnoreCase(nextTag.getValue())) {
								count++;
							}
							tagContent = parser.getNextXMLTaggedContent(nextTag);
						}
						if (count>0)
							stringList=new String[count];
						if (stringList!=null) {
							count=0;
							parser.setString(idListContent);
							tagContent = parser.getNextXMLTaggedContent(nextTag);
							while (!StringUtil.blank(tagContent)) {
								if ("id".equalsIgnoreCase(nextTag.getValue())) {
									stringList[count]=tagContent;
									count++;
								}
								tagContent = parser.getNextXMLTaggedContent(nextTag);
							}
						}
					}
					tagContent = parser.getNextXMLTaggedContent(nextTag); 
				}
				return stringList;
			} 
			tagContent = parser.getNextXMLTaggedContent(nextTag);
		}
		return null;
	}
	/*.................................................................................................................*/
	public static String[] requestGenBankAccessions2(String eSearchResponse){
		Parser parser = new Parser();
		String[] stringList=null;
		parser.setString(eSearchResponse);
		if (!parser.isXMLDocument(false))   // check if XML
			return null;
		MesquiteString nextTag = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);
		while (!StringUtil.blank(nextTag.getValue())) {
			if ("eSearchResult".equalsIgnoreCase(nextTag.getValue())) {  //make sure it has the right root tag
				parser.setString(tagContent);
				tagContent = parser.getNextXMLTaggedContent(nextTag);
				while (!StringUtil.blank(tagContent)) {
					if ("idList".equalsIgnoreCase(nextTag.getValue())) {
						int count=0;
						String idListContent=tagContent;
						parser.setString(idListContent);
						tagContent = parser.getNextXMLTaggedContent(nextTag);
						while (!StringUtil.blank(tagContent)) {
							if ("accession".equalsIgnoreCase(nextTag.getValue())) {
								count++;
							}
							tagContent = parser.getNextXMLTaggedContent(nextTag);
						}
						if (count>0)
							stringList=new String[count];
						if (stringList!=null) {
							count=0;
							parser.setString(idListContent);
							tagContent = parser.getNextXMLTaggedContent(nextTag);
							while (!StringUtil.blank(tagContent)) {
								if ("id".equalsIgnoreCase(nextTag.getValue())) {
									stringList[count]=tagContent;
									count++;
								}
								tagContent = parser.getNextXMLTaggedContent(nextTag);
							}
						}
					}
					tagContent = parser.getNextXMLTaggedContent(nextTag); 
				}
				return stringList;
			} 
			tagContent = parser.getNextXMLTaggedContent(nextTag);
		}
		return null;
	}
	/*.................................................................................................................*/
	public static String[] requestGenBankAccessions(String eSearchResponse, int number){
		if (eSearchResponse==null)
			return null;
		String[] accessions = new String[number];
		Element gbSetElement = XMLUtil.getRootXMLElementFromString("GBSet",eSearchResponse);
		if (gbSetElement==null)
			return null;
		int count=0;
		String s = "";
		List gbSeqList = gbSetElement.elements("GBSeq");
		for (Iterator iter = gbSeqList.iterator(); iter.hasNext();) {   // this is going through all of the hits
			Element gbSeqElement = (Element) iter.next();
			if (gbSeqElement!=null) {
				if (count<accessions.length){
					accessions[count] = gbSeqElement.elementText("GBSeq_primary-accession");
				}
				else return accessions;
				count++;
			}

		}
		return accessions;
	}

/** returns up to 20 GenBank IDs given a list of accession numbers.  Note that the IDs may not be listed in the same order as the accession numbers!!!*/
	/*.................................................................................................................*/
	public synchronized static String[] getGenBankIDs20(String[] accessionNumbers, int startIndex, boolean nucleotides,  MesquiteModule mod, boolean writeLog){ 
		try {
			String searchString="";
			for (int i=0; i<maxGenBankRequest && i+startIndex<accessionNumbers.length;i++) {
				if (StringUtil.notEmpty(accessionNumbers[i+startIndex])) {
					searchString+=accessionNumbers[i+startIndex];
					if (i==maxGenBankRequest-1 || i<accessionNumbers.length-1)
						searchString+="+OR+";
				}
			}
			if (StringUtil.blank(searchString))
				return null;
			if (writeLog && mod!=null)
				mod.log(".");

			StringBuffer sb = new StringBuffer();
			URL queryURL = getESearchAddress(searchString, nucleotides);
			if (writeLog && mod!=null)
				mod.log(".");
			URLConnection connection = queryURL.openConnection();
			if (writeLog && mod!=null)
				mod.log(".");
			InputStream in = connection.getInputStream();

			int c;
			int count=0;
			while ((c = in.read()) != -1) {
				sb.append((char) c);
				count++;
				if (count % 100==0 && writeLog && mod!=null)
					mod.log(".");
			}
			in.close();

			String[] idList = requestGenBankIDs(sb.toString());

			return idList;
		} catch ( Exception e ){
			mod.logln("Cannot get GenBank IDs");
			return null;
		}

	}
	static int maxGenBankRequest = 20;
	/*.................................................................................................................*/
	public synchronized static String[] getGenBankIDs(String[] accessionNumbers, boolean nucleotides,  MesquiteModule mod, boolean writeLog){ 
		if (accessionNumbers==null || accessionNumbers.length==0)
			return null;
		if (accessionNumbers.length<=maxGenBankRequest)
			return getGenBankIDs20(accessionNumbers, 0,nucleotides, mod, writeLog);
		String[] idList = new String[accessionNumbers.length];
		for (int i=0;i<idList.length; i+=20) {
			String[] nextList = getGenBankIDs20(accessionNumbers, i, nucleotides, mod, writeLog);
			if (nextList!=null)
				for (int j=0; j<20 && j<nextList.length && i+j<idList.length; j++) {
					idList[i+j]=nextList[j];
				}
		}
		return idList;
	}
	/*.................................................................................................................*/
	public static String[] getGenBankAccessionFromID(String[] ID, boolean nucleotides,  MesquiteModule mod, boolean writeLog){ 
		try {
			String searchString="";
			for (int i=0; i<ID.length;i++) {
				searchString+=ID[i];
				if (i<ID.length-1)
					searchString+="+OR+";
			}
			if (writeLog && mod!=null)
				mod.log(".");

			StringBuffer sb = new StringBuffer();
			URL queryURL = getEUtilsAddressForIDToAccession(searchString, nucleotides);
			if (writeLog && mod!=null)
				mod.log(".");
			URLConnection connection = queryURL.openConnection();
			if (writeLog && mod!=null)
				mod.log(".");
			InputStream in = connection.getInputStream();

			int c;
			int count=0;
			while ((c = in.read()) != -1) {
				sb.append((char) c);
				count++;
				if (count % 100==0 && writeLog && mod!=null)
					mod.log(".");
			}
			in.close();

			String[] idList = requestGenBankAccessions(sb.toString(), ID.length);

			return idList;
		} catch ( Exception e ){
			// give warning
			return null;
		}

	}
	/*.................................................................................................................*/
	public static String getGenBankGeneIDFromProteinID(String proteinID, MesquiteModule mod, boolean writeLog){ 
		try {
			String searchString="";
			if (writeLog && mod!=null)
				mod.log(".");

			StringBuffer sb = new StringBuffer();
			URL queryURL = getEUtilsAddressForProtToNuc(proteinID);
			if (writeLog && mod!=null)
				mod.log(".");
			URLConnection connection = queryURL.openConnection();
			if (writeLog && mod!=null)
				mod.log(".");
			InputStream in = connection.getInputStream();

			int c;
			int count=0;
			while ((c = in.read()) != -1) {
				sb.append((char) c);
				count++;
				if (count % 100==0 && writeLog && mod!=null)
					mod.log(".");
			}
			in.close();

			String geneID = getGenIDFromELinkResults(sb.toString());

			return geneID;
		} catch ( Exception e ){
			// give warning
			return null;
		}

	}
	/*.................................................................................................................*/
	public static String getGenBankID(String accessionNumber, boolean nucleotides,  MesquiteModule mod, boolean writeLog){ 
		String[] accessionNumbers = new String[1];
		accessionNumbers[0]=accessionNumber;
		String[] idList = getGenBankIDs(accessionNumbers,nucleotides,mod, writeLog);
		if (idList!=null && idList.length>0)
			return idList[0];
		else
			return null;
	}
	/*.................................................................................................................*/
	public static void importFASTASequences(CharacterData data, String fastaSequences, MesquiteModule mod,StringBuffer report, int insertAfterTaxonRequested, int referenceTaxon, boolean adjustNewSequences, boolean addNewInternalGaps){
		importFASTASequences(data, fastaSequences, mod, report, insertAfterTaxonRequested, referenceTaxon, adjustNewSequences, addNewInternalGaps, "");
	}
	/*.................................................................................................................*/
	public static void importFASTASequences(CharacterData data, String fastaSequences, MesquiteModule mod,StringBuffer report, int insertAfterTaxonRequested, int referenceTaxon, boolean adjustNewSequences, boolean addNewInternalGaps, String appendToTaxonName){
		if (data==null)
			return;
		
		Taxa taxa = data.getTaxa();
		int oldNumTaxa = taxa.getNumTaxa();
		data.setCharNumChanging(true);
		int insertAfterTaxon = taxa.getNumTaxa()-1;
		if (insertAfterTaxonRequested>=0)
			insertAfterTaxon = insertAfterTaxonRequested;
		if (data instanceof ProteinData) {
			InterpretFastaProtein importer = new InterpretFastaProtein();
			importer.readString(data,fastaSequences, insertAfterTaxon,appendToTaxonName);
		} else {
			InterpretFastaDNA importer = new InterpretFastaDNA();
			importer.readString(data,fastaSequences, insertAfterTaxon,appendToTaxonName);
		}
		data.setCharNumChanging(false);
		taxa.notifyListeners(mod, new Notification(MesquiteListener.PARTS_ADDED));
		data.notifyListeners(mod, new Notification(MesquiteListener.PARTS_ADDED));

		Bits newTaxa = new Bits(taxa.getNumTaxa());
		int taxaAdded = taxa.getNumTaxa()-oldNumTaxa;
		int itStart = insertAfterTaxon+1;
		int itEnd = itStart+taxaAdded-1;
		for (int it=itStart; it<=itEnd; it++) {
			newTaxa.setBit(it);
		}
		
		
		if (adjustNewSequences) {
			MesquiteMessage.println("Adjusting sequences ");
			if (!data.someApplicableInTaxon(insertAfterTaxon, false)){  
				MesquiteMessage.println("The reference sequence contains no data; adjustment cancelled.");
			    adjustNewSequences = false;
			}
			if (adjustNewSequences) {
				
				if (data instanceof DNAData){
					MolecularDataUtil.reverseComplementSequencesIfNecessary((DNAData) data, mod, taxa, newTaxa, insertAfterTaxon, false, false);
				}
				
				MolecularDataUtil.pairwiseAlignMatrix(mod, (MolecularData)data, referenceTaxon, newTaxa,0, addNewInternalGaps, true);
				data.notifyListeners(mod, new Notification(CharacterData.DATA_CHANGED, null, null));
			}
		}


		
		String s;
		if (report!=null){
			report.append("Acquired: \n");
			for (int it = itStart; it<= itEnd; it++){
				s = taxa.getTaxon(it).getName();
				report.append("  " + s + "\n");
				report.append("  Length: " + data.getTotalNumApplicable(it,false)+ "\n");

			}
			report.append("\n");
		}
	}
	/*.................................................................................................................*/
	public static String createFastaString(CharacterData data, int icStart, int icEnd, int it){
		if (data==null)
			return null;
		Taxa taxa = data.getTaxa();
		StringBuffer sb = new StringBuffer();
		sb.append(">"+ taxa.getTaxonName(it) + StringUtil.lineEnding());
		int counter=0;
		for (int ic=icStart; ic<=icEnd; ic++) {
			if (data.isUnassigned(ic, it)){
				if (data instanceof ProteinData)
					sb.append("X");
				else
					sb.append("N");
			}
			else  {
				data.statesIntoStringBuffer(ic, it, sb, false);
				counter ++;
				if ((counter % 50 == 1) && (counter > 1)) {    // modulo
					sb.append(StringUtil.lineEnding());
				}
			}
		}
		sb.append(StringUtil.lineEnding());
		return sb.toString();
	}
	/*.................................................................................................................*/
	public static String createFastaString(String sequenceName, String sequence, boolean isNucleotides){
		StringBuffer sb = new StringBuffer();
		sb.append(">"+ sequenceName + StringUtil.lineEnding());
		sb.append(sequence + StringUtil.lineEnding());
		return sb.toString();
	}
	/*.................................................................................................................*/
	public synchronized static String fetchGenBankSequence(String id, boolean isNucleotides,  MesquiteModule mod, boolean writeLog, String fileFormat, String retMode, StringBuffer report){ 
		try {
			URL queryURL = getFetchSequenceAddress(id, fileFormat, retMode, isNucleotides);
			URLConnection connection = queryURL.openConnection();
			InputStream in = connection.getInputStream();

			StringBuffer fetchBuffer = new StringBuffer();
			int c;
			int count = 0;
			while ((c = in.read()) != -1) {
				fetchBuffer.append((char) c);
				count++;
				if (count % 500==0 && writeLog && mod!=null)
					mod.log(".");
			}
			in.close();
			return fetchBuffer.toString();
		} catch ( Exception e ){
			// give warning
			mod.logln("could not fetch GenBank sequences " + id);
			return null;
		}

	}
	/*.................................................................................................................*/
	public static String fetchGenBankSequence(String id, boolean isNucleotides,  MesquiteModule mod, boolean writeLog, StringBuffer report){ 
		return fetchGenBankSequence(id,isNucleotides, mod, writeLog,"fasta", null, report);
	}
	/*.................................................................................................................*/
	public static String fetchGenBankSequenceAsFASTA(String id,  boolean isNucleotides, MesquiteModule mod, boolean writeLog, StringBuffer results, StringBuffer report){ 
		if (results==null)
			return null;
		try {
			URL queryURL = getFetchSequenceAddress(id, isNucleotides);
			URLConnection connection = queryURL.openConnection();
			InputStream in = connection.getInputStream();

			StringBuffer fetchBuffer = new StringBuffer();
			int c;
			int count = 0;
			while ((c = in.read()) != -1) {
				fetchBuffer.append((char) c);
				count++;
				if (count % 200==0 && writeLog && mod!=null)
					mod.log(".");
			}
			in.close();
			results.append(fetchBuffer);
			return fetchBuffer.toString();
		} catch ( Exception e ){
			// give warning
			return null;
		}

	}
	/*.................................................................................................................*/
	public synchronized static String[] fetchGenBankSequenceStrings(String[] idList, boolean isNucleotides,  MesquiteModule mod, boolean writeLog, String fileFormat, String retMode, StringBuffer report){ 
		String[] sequences = new String[idList.length];
		for (int i=0; i<idList.length; i++) {
			if (!StringUtil.blank(idList[i])) {

				if (writeLog && mod!=null){
					mod.log("Fetching " + idList[i] + " (" + (i+1) + " of " + idList.length+")");
					mod.log(".");
				}
				String seq = fetchGenBankSequence(idList[i], isNucleotides,  mod, writeLog, fileFormat, retMode, report);
				if (StringUtil.notEmpty(seq))
					sequences[i]=seq;
				else if (i==0)
					return null;
				mod.logln("");
			}
		}
		return sequences;

	}
	/*.................................................................................................................*/
	public static String fetchGenBankSequences(String[] idList, boolean isNucleotides,  MesquiteModule mod, boolean writeLog, StringBuffer report){ 
		StringBuffer sequences = new StringBuffer();
		for (int i=0; i<idList.length; i++) {
			if (!StringUtil.blank(idList[i])) {

				if (writeLog && mod!=null){
					mod.log("Fetching " + idList[i]);
					mod.log(".");
				}
				String seq = fetchGenBankSequence(idList[i], isNucleotides,  mod, writeLog, report);
				if (StringUtil.notEmpty(seq))
					sequences.append(seq);
				else if (i==0)
					return null;
				mod.logln("");
			}
		}
		return sequences.toString();

	}
	/*.................................................................................................................*/
	public static String fetchGenBankSequences(String[] idList, boolean isNucleotides,  MesquiteModule mod, boolean writeLog, StringBuffer results, StringBuffer report){ 
		if (results==null)
			return null;
		StringBuffer sequences = new StringBuffer();
		for (int i=0; i<idList.length; i++) {
			if (!StringUtil.blank(idList[i])) {

				if (writeLog && mod!=null){
					mod.log("Fetching " + idList[i]);
					mod.log(".");
				}
				String seq = fetchGenBankSequenceAsFASTA(idList[i], isNucleotides,  mod, writeLog, results, report);
				if (StringUtil.notEmpty(seq)){
					sequences.append(seq);
				}
				else if (i==0)
					return null;
				//	mod.logln("");
			}
		}
		return sequences.toString();

	}
	/*.................................................................................................................*/
	public static String fetchGenBankSequencesFromIDs(String[] idList,  boolean isNucleotides, MesquiteModule mod, boolean writeLog, StringBuffer report){ 
		String sequences = fetchGenBankSequences(idList,isNucleotides, mod, writeLog, report);
		if (mod!=null && writeLog)
			mod.logln("");
		return sequences;

	}
	/*.................................................................................................................*/
	public static String fetchGenBankSequencesFromIDs(String[] idList,  boolean isNucleotides, MesquiteModule mod, boolean writeLog, StringBuffer results, StringBuffer report){ 
		if (results==null || idList==null)
			return null;
		String sequences = fetchGenBankSequences(idList,isNucleotides, mod, writeLog, results, report);
		if (mod!=null && writeLog)
			mod.logln("");
		return sequences;

	}
	/*.................................................................................................................*/
	public static String fetchGenBankSequencesFromAccessions(String[] accessionNumbers,  boolean isNucleotides, MesquiteModule mod, boolean writeLog, StringBuffer report){ 
		String[] idList = getGenBankIDs(accessionNumbers, isNucleotides,  mod, writeLog);
		if (idList==null)
			return null;
		String sequences = fetchGenBankSequences(idList,isNucleotides, mod, writeLog, report);
		if (mod!=null && writeLog)
			mod.logln("");
		return sequences;

	}
	/*.................................................................................................................*/
	public static String fetchGenBankSequencesFromAccessions(String[] accessionNumbers,  boolean isNucleotides, MesquiteModule mod, boolean writeLog, StringBuffer results, StringBuffer report){ 
		if (results==null)
			return null;
		String[] idList = getGenBankIDs(accessionNumbers,  isNucleotides,  mod, writeLog);
		if (idList==null)
			return null;
		String sequences = fetchGenBankSequences(idList,isNucleotides, mod, writeLog, results, report);
		if (mod!=null && writeLog)
			mod.logln("");
		return sequences;

	}
	/*.................................................................................................................*/
	public static String getRID(String response, MesquiteInteger responseTime) {
		String  s = StringUtil.getAllAfterSubString(response, "!--QBlastInfoBegin");
		responseTime.setValue(0);
		if (!StringUtil.blank(s)) {
			int valuesAcquired = 0;
			String rid = null;
			Parser parser = new Parser(s);
			parser.setPunctuationString("=");
			String token = parser.getNextToken();
			while (!StringUtil.blank(token)) {
				if ("RID".equalsIgnoreCase(token)) {
					token = parser.getNextToken(); // = sign
					rid = parser.getNextToken(); // RID
					valuesAcquired++;
				}
				else if ("RTOE".equalsIgnoreCase(token)) {
					token = parser.getNextToken(); // = sign
					token = parser.getNextToken(); //response time

					responseTime.setValue(token);
					valuesAcquired++;
				}
				if (valuesAcquired>=2)
					return rid;
				token = parser.getNextToken();
			}
		}
		return null;
	}

	/*.................................................................................................................*/
	public static void blastForMatches(String blastType, String sequenceName, String sequence, boolean isNucleotides, int numHits, int maxTime, double eValueCutoff, int wordSize, StringBuffer blastResponse){
		StringBuffer report = new StringBuffer();
		String searchString = NCBIUtil.getBlastPutQueryURL(blastType, isNucleotides, sequenceName, new StringBuffer(sequence),numHits, eValueCutoff, wordSize, report);

		if (!StringUtil.blank(searchString)) {
			try {
				URL queryURL = new URL(searchString);
				URLConnection connection = queryURL.openConnection();
				InputStream in = connection.getInputStream();

				//logln("Processing initial response", true);
				StringBuffer sb = new StringBuffer();
				int c;
				while ((c = in.read()) != -1) {
					sb.append((char) c);
				}
				in.close();
				
				MesquiteInteger responseTime = new MesquiteInteger();
				String rid = getRID(sb.toString(), responseTime);
				//		logln("   Expected time of completion of BLAST is " + responseTime.toString()+ " seconds.", true);

				int checkInterval = responseTime.getValue();
				if (checkInterval<10) checkInterval = 10;
				//pauseForSeconds(responseTime.getValue()-checkInterval);
				int count=0;
				String response="";

				String recoverURLString = NCBIUtil.getGetQueryURL(rid,1, 1, eValueCutoff);
				MesquiteTimer timer = new MesquiteTimer();
				timer.start(); 
				int totalTime = 0;
				if (!StringUtil.blank(recoverURLString)) {
					while (!NCBIUtil.responseSaysBLASTIsReady(response) && totalTime<maxTime) {
						int seconds = (int)(timer.timeSinceVeryStart()/1000);
						//					if (count>0) logln(" Not done.  (" + seconds + " seconds.)");
						int waitSeconds;
						if (count==0) waitSeconds = checkInterval+2;
						else if (count==1) waitSeconds = 3;
						else waitSeconds = checkInterval;
						for (int i = 0; i<waitSeconds*10; i++) {
							Thread.sleep(100);
							if (i%10==0) {
								int sec = seconds+ i/10;
								CommandRecord.tick("Waiting for BLAST. ("+sec + ")");
							}
						}
						//					log("   Querying to see if BLAST has completed.  ", true);
						queryURL = new URL(recoverURLString);
						connection = queryURL.openConnection();
						in = connection.getInputStream();

						StringBuffer responseBuffer = new StringBuffer();
						while ((c = in.read()) != -1) {
							responseBuffer.append((char) c);
						}
						in.close();
						response = responseBuffer.toString();
						count++;
						totalTime +=checkInterval;
					}
					CommandRecord.tick("");
					//	if (NCBIUtil.responseSaysBLASTIsReady(response)) MesquiteMessage.println(" Done."); else MesquiteMessage.println(" Not completed in time.");
					//					results.append("\n" + taxonName + "\t");
					if (blastResponse!=null){
						blastResponse.setLength(0);
						blastResponse.append(response);
					}
				}
				timer.end();
			}
			catch (Exception e) {
				MesquiteMessage.println("Connection error");
			}
		}

	}

	/*.................................................................................................................*
	public static void nearestMatch2(String blastType, String sequenceName, String sequence, boolean isNucleotides, int maxTime, StringBuffer matchReport){

		NCBIUtil.nearestMatches("blastn", sequenceName, sequence.toString(), true, 300, matchReport);
	}

	/*.................................................................................................................*
	public static void processResultsFromBLAST(String response, int maxHits, boolean fetchTaxonomy, boolean isNucleotides, StringArray topHitAccessions, BLASTResults blastResults){

		Parser parser = new Parser();
		parser.setString(response);
		if (!parser.isXMLDocument(false))   // check if XML
			return;
		MesquiteString nextTag = new MesquiteString();
		String tagContent;
		String accession="";
		double bitScore = 0.0;
		double eValue = -1.0;
		if (parser.resetToXMLTagContents("BLASTOUTPUT"))
			if (parser.resetToXMLTagContents("BLASTOUTPUT_iterations")) 
				if (parser.resetToXMLTagContents("Iteration") && parser.resetToXMLTagContents("Iteration_hits")){
					tagContent = parser.getNextXMLTaggedContent(nextTag);
					int hitCount = 0;
					while (!StringUtil.blank(nextTag.getValue()) && hitCount<maxHits) {
						if ("Hit".equalsIgnoreCase(nextTag.getValue())) {   // here is a hit
							String tax=null;
							StringBuffer tempBuffer = new StringBuffer();
							Parser hitParser = new Parser(tagContent);
							tagContent = hitParser.getNextXMLTaggedContent(nextTag);
							String def = "";
							while (!StringUtil.blank(nextTag.getValue())) {
								if ("Hit_def".equalsIgnoreCase(nextTag.getValue())) {  
									def=StringUtil.stripTrailingWhitespace(tagContent);
								}
								else if ("Hit_accession".equalsIgnoreCase(nextTag.getValue())) {  
									tempBuffer.append(StringUtil.stripTrailingWhitespace(tagContent)+"\t");
									accession=tagContent;
									if (fetchTaxonomy) {
										tax = NCBIUtil.fetchTaxonomyList(accession, isNucleotides, true, null);
									}
								}
								else if ("Hit_hsps".equalsIgnoreCase(nextTag.getValue())) {  
									Parser subParser = new Parser(tagContent);
									tagContent = subParser.getNextXMLTaggedContent(nextTag);
									while (!StringUtil.blank(nextTag.getValue())) {
										if ("Hsp".equalsIgnoreCase(nextTag.getValue())) {
											subParser.setString(tagContent);
											tagContent = subParser.getNextXMLTaggedContent(nextTag);
											while (!StringUtil.blank(nextTag.getValue())) {
												if ("Hsp_bit-score".equalsIgnoreCase(nextTag.getValue())) {
													tempBuffer.append(StringUtil.stripTrailingWhitespace(tagContent)+"\t");
													bitScore = MesquiteDouble.fromString(tagContent);
												}
												else if ("Hsp_evalue".equalsIgnoreCase(nextTag.getValue())) {
													tempBuffer.append(StringUtil.stripTrailingWhitespace(tagContent)+"\t");
													eValue = MesquiteDouble.fromString(tagContent);
												}
												tagContent = subParser.getNextXMLTaggedContent(nextTag); 
											}
										}
										tagContent = subParser.getNextXMLTaggedContent(nextTag); 
									}
								} 
								tagContent = hitParser.getNextXMLTaggedContent(nextTag);
							}
							if (blastResults!=null) {
								blastResults.setAccession(accession,hitCount);
								blastResults.setBitScore(bitScore,hitCount);
								blastResults.setDefinition(def,hitCount);
								blastResults.seteValue(eValue,hitCount);
								blastResults.setTaxonomy(tax,hitCount);
							}
							if (topHitAccessions!=null)
								topHitAccessions.addAndFillNextUnassigned(accession);

							hitCount++;
						}
						tagContent = parser.getNextXMLTaggedContent(nextTag);

					}
				}



	}

	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public static String[] cleanUpID(String[] ID){ 
		String s = "";
		for (int i =0; i<ID.length; i++){
			if (ID[i].indexOf("gi|")>=0) {
				s = StringUtil.getPiece(ID[i], "gi|", "|");
				if (StringUtil.notEmpty(s))
					ID[i] = s;
			}
		}
		return ID;
	}
	/*.................................................................................................................*/
	public static String cleanUpID(String ID){ 
		if (StringUtil.blank(ID))
			return ID;
		String s = "";
		if (ID.indexOf("gi|")>=0) {
			s = StringUtil.getPiece(ID, "gi|", "|");
			if (StringUtil.notEmpty(s))
				ID = s;
		}

		return ID;
	}
	/*.................................................................................................................*/
	public static String[] getNucIDsFromProtIDs(String[] ID){ 
		String[] protID= new String[ID.length];
		for (int i =0; i<protID.length; i++){
			protID[i]=ID[i];
			ID[i] = getGenBankGeneIDFromProteinID(protID[i], null, false);
		}
		return ID;
	}

	/*.................................................................................................................*/
	public static String getGenIDFromELinkResults(String response){
		if (response==null)
			return null;
		Element eLinkResultElement = XMLUtil.getRootXMLElementFromString("eLinkResult",response);
		if (eLinkResultElement==null)
			return null;
		String s = "";
		Element LinkSetElement = eLinkResultElement.element("LinkSet");
		if (LinkSetElement!=null) {
			//	Element LinkSetDBElement = LinkSetElement.element("LinkSetDb");
			List hitList = LinkSetElement.elements("LinkSetDb");
			for (Iterator iter = hitList.iterator(); iter.hasNext();) {   // this is going through all of the hits
				Element hitElement = (Element) iter.next();
				if (hitElement!=null) {
					String linkName = hitElement.elementText("LinkName");
					if ("protein_nuccore_mrna".equalsIgnoreCase(linkName)){
						Element LinkElement = hitElement.element("Link");
						if (LinkElement!=null) {
							s = LinkElement.elementText("Id");
							return s;
						}
					}
				}
			}
		}
		return null;
	}

}





