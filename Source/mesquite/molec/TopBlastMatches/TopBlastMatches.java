/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.TopBlastMatches; 


import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.ContinuousState;
import mesquite.molec.lib.*;


/* ======================================================================== */
public class TopBlastMatches extends CategDataSearcher implements ItemListener { 
	MesquiteTable table;
	CharacterData data;
	StringBuffer results;
	String[] accessionNumbers;
	String[] ID;
	Blaster blasterTask;
	
	int blastType = Blaster.BLAST;

	boolean importTopMatches = false;
	boolean saveResultsToFile = true;
	int maxHits = 1;
	double  minimumBitScore = 0.0;
	boolean preferencesSet = false;
	boolean fetchTaxonomy = false;
	boolean interleaveResults = false;
	boolean adjustSequences = true;
	boolean addInternalGaps = true;
//	boolean blastx = false;
	int maxTime = 300;
	static int upperMaxHits = 30;

	double eValueCutoff = 10.0;


	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(Blaster.class, getName() + "  needs a Blast module.","");
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		blasterTask = (Blaster)hireEmployee(Blaster.class, "Blaster (for " + getName() + ")"); 
		if (blasterTask==null)
			return sorry(getName() + " couldn't start because no Blast module could be obtained.");
		else if (!blasterTask.initialize())
			return false;
		results = new StringBuffer();
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("fetchTaxonomy".equalsIgnoreCase(tag))
			fetchTaxonomy = MesquiteBoolean.fromTrueFalseString(content);
		else if ("saveResultsToFile".equalsIgnoreCase(tag))
			saveResultsToFile = MesquiteBoolean.fromTrueFalseString(content);
		else if ("importTopMatches".equalsIgnoreCase(tag))
			importTopMatches = MesquiteBoolean.fromTrueFalseString(content);
		else if ("interleaveResults".equalsIgnoreCase(tag))
			interleaveResults = MesquiteBoolean.fromTrueFalseString(content);
		else if ("adjustSequences".equalsIgnoreCase(tag))
			adjustSequences = MesquiteBoolean.fromTrueFalseString(content);
		else if ("addInternalGaps".equalsIgnoreCase(tag))
			addInternalGaps = MesquiteBoolean.fromTrueFalseString(content);
		else if ("blastType".equalsIgnoreCase(tag))
			blastType = MesquiteInteger.fromString(content);
		else if ("eValueCutoff".equalsIgnoreCase(tag))
			eValueCutoff = MesquiteDouble.fromString(content);
		else if ("maxTime".equalsIgnoreCase(tag))
			maxTime = MesquiteInteger.fromString(content);
		else if ("maxHits".equalsIgnoreCase(tag))
			maxHits = MesquiteInteger.fromString(content);		
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "fetchTaxonomy", fetchTaxonomy);  
		StringUtil.appendXMLTag(buffer, 2, "maxTime", maxTime);  
		StringUtil.appendXMLTag(buffer, 2, "importTopMatches", importTopMatches);  
		StringUtil.appendXMLTag(buffer, 2, "interleaveResults", interleaveResults);  
		StringUtil.appendXMLTag(buffer, 2, "addInternalGaps", addInternalGaps);  
		StringUtil.appendXMLTag(buffer, 2, "adjustSequences", adjustSequences);  
//		StringUtil.appendXMLTag(buffer, 2, "blastx", blastx);  
		StringUtil.appendXMLTag(buffer, 2, "eValueCutoff", eValueCutoff);  
		StringUtil.appendXMLTag(buffer, 2, "maxHits", maxHits);  
		StringUtil.appendXMLTag(buffer, 2, "blastType", blastType);  
		StringUtil.appendXMLTag(buffer, 2, "saveResultsToFile", saveResultsToFile);  

		preferencesSet = true;
		return buffer.toString();
	}
//	Checkbox blastXCheckBox ;
	Choice blastTypeChoice ;
	Checkbox saveFileCheckBox ;
	Checkbox fetchTaxonomyCheckBox;
	Checkbox importCheckBox;
	Checkbox interleaveResultsCheckBox;
	Checkbox adjustSequencesCheckBox;
	Checkbox addInternalGapsCheckBox;
	/*.................................................................................................................*/
	private void checkEnabling(){
		//importCheckBox.setEnabled(!blastXCheckBox.getState());
		fetchTaxonomyCheckBox.setEnabled( saveFileCheckBox.getState());
	}
	/*.................................................................................................................*/
	public void itemStateChanged(ItemEvent e) {
		checkEnabling();
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Top Blast Matches",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Options for Top Blast Matches");
		int oldBlastType = blastType;

		IntegerField maxHitsField = dialog.addIntegerField("Maximum number of matches:",  maxHits,5,1,upperMaxHits);
//		blastXCheckBox = dialog.addCheckBox("use blastx for nucleotides",blastx);
		DoubleField eValueCutoffField = dialog.addDoubleField("Reject hits with eValues greater than: ", eValueCutoff, 20, 0.0, Double.MAX_VALUE);
		saveFileCheckBox = dialog.addCheckBox("save report of results to file",saveResultsToFile);
		blastTypeChoice = dialog.addPopUpMenu("BLAST type for nucleotides", Blaster.getBlastTypeNames(), blastType);
		fetchTaxonomyCheckBox = dialog.addCheckBox("fetch taxonomic lineage",fetchTaxonomy);
		importCheckBox = dialog.addCheckBox("import top matches into matrix",importTopMatches);
		interleaveResultsCheckBox = dialog.addCheckBox("insert hits after sequence that was BLASTed",interleaveResults);
		adjustSequencesCheckBox = dialog.addCheckBox("reverse complement and align imported sequences if needed",adjustSequences);
		addInternalGapsCheckBox = dialog.addCheckBox("allow new internal gaps during alignment",addInternalGaps);
		
		IntegerField maxTimeField = dialog.addIntegerField("Maximum time for BLAST response (seconds):",  maxTime,5);
	//	blastXCheckBox.addItemListener(this);
		saveFileCheckBox.addItemListener(this);
		checkEnabling();


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			maxHits = maxHitsField.getValue();
			saveResultsToFile = saveFileCheckBox.getState();
			fetchTaxonomy = fetchTaxonomyCheckBox.getState();
			eValueCutoff = eValueCutoffField.getValue();
//			blastx = blastXCheckBox.getState();
			blastType = blastTypeChoice.getSelectedIndex();
			if (blastType<0) blastType=oldBlastType;
			importTopMatches = importCheckBox.getState();
			interleaveResults = interleaveResultsCheckBox.getState();
			adjustSequences = adjustSequencesCheckBox.getState();
			addInternalGaps = addInternalGapsCheckBox.getState();
			maxTime=maxTimeField.getValue();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("defaults")) {
		}
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	/** Processing to be done after each search. Returns true if  */
	public boolean processAfterEachTaxonSearch(CharacterData data, int it){
		logln("\nSearch results: \n"+ results.toString());
		//		logln("**** IDs: " +StringArray.toString(ID)); 
		int numTaxaAdded =0;

		if (importTopMatches ){  
			int originalNumChars = data.getNumChars();
			int insertAfterTaxon = data.getNumTaxa()-1;
			if (interleaveResults)
				insertAfterTaxon = it;
			//NCBIUtil.getGenBankIDs(accessionNumbers, false,  this, false);
			logln("About to import top matches.", true);
			StringBuffer report = new StringBuffer();

			if (blastType==Blaster.BLASTX && data instanceof DNAData) {
				//	ID = NCBIUtil.getNucIDsFromProtIDs(ID);
				ID = blasterTask.getNucleotideIDsfromProteinIDs(ID);
				//	logln("****AFTER NucToProt IDs: " +StringArray.toString(ID)); 
			}
			//String newSequencesAsFasta = NCBIUtil.fetchGenBankSequencesFromIDs(ID, data instanceof DNAData, this, true, report);	

			StringBuffer blastResponse = new StringBuffer();
			String newSequencesAsFasta = blasterTask.getFastaFromIDs(ID,  data instanceof DNAData, blastResponse);


			numTaxaAdded = data.getNumTaxa();
			if (StringUtil.notEmpty(newSequencesAsFasta))
				NCBIUtil.importFASTASequences(data, newSequencesAsFasta, this, report, insertAfterTaxon, it, adjustSequences, addInternalGaps);
			else
				logln("BLAST database returned no sequences in response to query.");
			data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
			data.getTaxa().notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
			logln(report.toString());
			
			numTaxaAdded = data.getNumTaxa()-numTaxaAdded;
/*			if (lastSearched!=null && lastSearched.isCombinable()) {
				if (interleaveResults) { 
					//lastSearched.add(numTaxaAdded);
				}
			}
			*/
			return data.getNumChars()!=originalNumChars;
		}
		return true;
	}
	/*.................................................................................................................*/
	/** message if search failed to find anything.  */
	public void unsuccessfulSearchMessage(){
		logln("BLAST database returned no sequences in response to query.");
	}
	/*.................................................................................................................*/
	/** Called to search on the data in selected cells.  Returns true if data searched*/
	public boolean searchData(CharacterData data, MesquiteTable table){
		this.data = data;
		results.setLength(0);
		if (!(data instanceof DNAData || data instanceof ProteinData)){
			discreetAlert( "Only DNA or protein data can be searched using this module.");
			return false;
		} 
		else {
			if (!queryOptions())
				return false;
			if (saveResultsToFile)
				prepareReportDirectory();
			logln("\nSearching for top BLAST hits (" + blasterTask.getName() + ")");

			boolean searchOK = searchSelectedTaxa(data,table);
			if (saveResultsToFile)
				saveResults(results);
			return searchOK;

		}
	}

	/*.................................................................................................................*/
	public boolean isNucleotides(CharacterData data){
		return data instanceof DNAData;
	}
	/*.................................................................................................................*/
	public boolean acceptableHit(int hitCount, double bitScore, double eValue) {
		return hitCount<=maxHits;
	}
	/*.................................................................................................................*/
	protected URL getESearchAddress(String s)
	throws MalformedURLException {
		return new URL(s);
	}

	String reportDirectoryPath=null;
	/*.................................................................................................................*/
	public boolean prepareReportDirectory() {
		String folderName = "BLAST reports";
		if (blasterTask!=null)
			folderName = "BLAST to " + blasterTask.getDatabaseName();
		reportDirectoryPath = MesquiteFileUtil.createDirectoryForFiles(this, MesquiteFileUtil.BESIDE_HOME_FILE, folderName,".");
		return StringUtil.notEmpty(reportDirectoryPath);
	}
	/*.................................................................................................................*/
	public void saveBLASTReport(String name, String contents) {
		if (StringUtil.blank(reportDirectoryPath))
			return;
		
		String blastReportPath = reportDirectoryPath + MesquiteFile.fileSeparator + name;  // directory into which processed files go
		blastReportPath = MesquiteFile.getUniqueNumberedPath(blastReportPath);
		if (!StringUtil.blank(blastReportPath)) {
			MesquiteFile.putFileContents(blastReportPath, contents, false);
		}
	}
	/*.................................................................................................................*/
	public void saveResults(StringBuffer results) {
		if (StringUtil.blank(reportDirectoryPath))
			return;
		
		String blastSummaryPath = reportDirectoryPath + "BLAST summary";  // directory into which processed files go
		if (!StringUtil.blank(blastSummaryPath)) {
			MesquiteFile.putFileContents(blastSummaryPath, results.toString(), false);
		}
	}
	/*.................................................................................................................*/
	public boolean searchOneTaxon(CharacterData data, int it, int icStart, int icEnd){
		if (data==null || blasterTask==null)
			return false;
		String sequenceName = data.getTaxa().getTaxonName(it);
		results.append("\n   BLASTing "+ sequenceName+ "\n");
		StringBuffer sequence = new StringBuffer(data.getNumChars());
		for (int ic = icStart; ic<=icEnd; ic++) {
			data.statesIntoStringBuffer(ic, it, sequence, false, false, false);
		}
		
		StringBuffer response = new StringBuffer();
		//blasterTask.setBlastx(blastx);
		blasterTask.setBlastType(blastType);
		if (data instanceof ProteinData)
			blasterTask.blastForMatches("blastp", sequenceName, sequence.toString(), true, maxHits, maxTime, eValueCutoff,response, true);
		else {	
			blasterTask.basicDNABlastForMatches(blastType, sequenceName, sequence.toString(), maxHits, maxTime, eValueCutoff, response, true);
		}

		BLASTResults blastResults = new BLASTResults(maxHits);
		
		if (saveResultsToFile)
			saveBLASTReport(sequenceName+" to " + blasterTask.getDatabaseName(),response.toString());
		blastResults.processResultsFromBLAST(response.toString(), false, eValueCutoff);
		blasterTask.postProcessingCleanup(blastResults);

		if (blastResults.someHits())
			results.append("   Top hits\n\tAccession [eValue] Definition): \n");
		else
			results.append("   No hits returned.\n");

		for (int i=0; i<maxHits; i++) {
			if (StringUtil.notEmpty(blastResults.getAccession(i))) {
				results.append("\t"+blastResults.getAccession(i));
				results.append("\t["+blastResults.geteValue(i)+"]");
				results.append("\t"+blastResults.getDefinition(i));
				String tax = "";
				if (fetchTaxonomy) {
					tax = NCBIUtil.fetchTaxonomyFromSequenceID(blastResults.getID(i), data instanceof DNAData, true, null);
					//	tax = NCBIUtil.fetchTaxonomyFromSequenceID(blastResults.getID(i), data instanceof DNAData, true, null);
					results.append("\t"+tax);
				}
				results.append("\n");
			}
		}
		accessionNumbers = blastResults.getAccessions();
		ID = blastResults.getIDs();
		return blastResults.someHits();
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Top BLAST Matches...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Top BLAST Matches";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Does a BLAST search against GenBank on selected data and returns the top BLAST matches for each sequence selected.";
	}
}

/*
Search string into Genbank Entrez:

http://www.ncbi.nlm.nih.gov:80/entrez/query.fcgi?cmd=Search&db=nucleotide&dopt=GenBank&term=Bembidion

http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?DATABASE=nr&FORMAT_TYPE=HTML&PROGRAM=blastn&CLIENT=web&SERVICE=plain&PAGE=Nucleotides&CMD=Put&QUERY= 
http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?DATABASE=nr&HITLIST_SIZE=10&FILTER=L&EXPECT=10&FORMAT_TYPE=HTML&PROGRAM=blastn&CLIENT=web&SERVICE=plain&NCBI_GI=on&PAGE=Nucleotides&CMD=Put&QUERY= 


http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=put&PAGE=Nucleotides&program=blast&QUERY_FILE=fasta&query="CCAAGTCCTTCTTGAAGGGGGCCATTTACCCATAGAGGGTGCCAGGCCCGTAGTGACCATTTATATATTTGGGTGAGTTTCTCCTTAGAGTCGGGTTGCTTGAGAGTGCAGCTCTAAGTGGGTGGTAAACTCCATCTAAGGCTAAATATGACTGCGAAACCGATAGCGAACAAGTACCGTGAGGGAAAGTTGAAAAGAACTTTGAAGAGAGAGTTCAAGAGTACGTGAAACTGTTCAGGGGTAAACCTGTGGTGCCCGAAAGTTCGAAGGGGGAGATTC"

 */



