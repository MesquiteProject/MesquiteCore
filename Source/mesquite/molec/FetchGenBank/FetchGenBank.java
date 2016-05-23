/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FetchGenBank; 


import java.awt.*;
import java.util.Iterator;
import java.util.List;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;
import org.dom4j.*;

/* ======================================================================== */
public class FetchGenBank extends UtilitiesAssistant { 
	String genBankNumbers;
	String[] originalGeneNames = null;
	String[] standardizedGeneNames = null;
	boolean isDNA = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();
		addMenuItem(null, "Fetch GenBank Sequences...", makeCommand("fetchGenBank", this));
		originalGeneNames = new String[]{"28S", "large subunit", "18S", "small subunit", "cytochrome oxidase", "Arginine", "carbomyl", "wingless", "topoisomerase", "muscle", "MSP"};
		standardizedGeneNames = new String[]{"28S", "28S", "18S", "18S", "COI", "ArgK", "CAD", "wg", "Topo", "MSP", "MSP"};
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryFilesDialog = new ExtensibleDialog(containerOfModule(), "Fetch GenBank Sequences",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		queryFilesDialog.addLabel("Accession Numbers (separated by commas); \nranges with commas allowed:");

		genBankNumbers = "";
		TextArea numbersArea = queryFilesDialog.addTextArea("",  5);

		queryFilesDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			genBankNumbers = numbersArea.getText();
		}
		queryFilesDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	String getVoucherInfo(Element featureTableElement) {
		List featureElements = featureTableElement.elements();
		for (Iterator iter = featureElements.iterator(); iter.hasNext();) {   // this is going through all of the notices
			Element featureElement = (Element) iter.next();
			Element gbFeatureQualElement = featureElement.element("GBFeature_quals");
			List qualifierElements = gbFeatureQualElement.elements();
			for (Iterator iter2 = qualifierElements.iterator(); iter2.hasNext();) {   // this is going through all of the notices
				Element qualifierElement = (Element) iter2.next();
				Element gbQualifierName = qualifierElement.element("GBQualifier_name");
				if ("specimen_voucher".equalsIgnoreCase(gbQualifierName.getText())) {
					Element gbQualifierValue = qualifierElement.element("GBQualifier_value");
					return gbQualifierValue.getText();

				}
			}
		}
		return "";
	}
	/*.................................................................................................................*/
	String translateGeneName(String originalGeneName) {
		
		return originalGeneName;
	}
	/*.................................................................................................................*/
	String getGeneInfo(Element featureTableElement, boolean useTranslationTable) {
		String geneName = "";
		List featureElements = featureTableElement.elements();
		for (Iterator iter = featureElements.iterator(); iter.hasNext();) {   // this is going through all of the notices
			Element featureElement = (Element) iter.next();
			Element gbFeatureQualElement = featureElement.element("GBFeature_quals");
			List qualifierElements = gbFeatureQualElement.elements();
			for (Iterator iter2 = qualifierElements.iterator(); iter2.hasNext();) {   // this is going through all of the notices
				Element qualifierElement = (Element) iter2.next();
				Element gbQualifierName = qualifierElement.element("GBQualifier_name");
				if ("product".equalsIgnoreCase(gbQualifierName.getText())) {
					Element gbQualifierValue = qualifierElement.element("GBQualifier_value");
					geneName = gbQualifierValue.getText();
					if (useTranslationTable && StringUtil.notEmpty(geneName)) {
						for (int i=0;i<originalGeneNames.length; i++) {
							if (geneName.toLowerCase().indexOf(originalGeneNames[i].toLowerCase())>=0)
								return standardizedGeneNames[i];
								
						}
					}
				}
			}
		}
		return geneName;
	}
	/*.................................................................................................................*/
	void processGenBankXML (String gbxml, MesquiteString taxonName, MesquiteString voucherCode, MesquiteString sequence, MesquiteString geneName){
		Document doc = XMLUtil.getDocumentFromString("GBSet", gbxml);
		Element root = doc.getRootElement();
		Element gbSeqElement = root.element("GBSeq");
		Element taxonNameElement = gbSeqElement.element("GBSeq_organism");
		String taxName = taxonNameElement.getText();
		Element sequenceElement = gbSeqElement.element("GBSeq_sequence");
		String seq = sequenceElement.getText();
		Element featureTableElement = gbSeqElement.element("GBSeq_feature-table");

		String voucherInfo = getVoucherInfo(featureTableElement);
		String voucherC = null;
		if (StringUtil.notEmpty(voucherInfo)) {
			voucherC=StringUtil.getLastItem(voucherInfo, ":");
		}

		String geneN = getGeneInfo(featureTableElement, true);

		if (taxonName!=null)
			taxonName.setValue(taxName);
		if (voucherCode!=null && voucherC!=null)
			voucherCode.setValue(voucherC);
		if (sequence!=null)
			sequence.setValue(seq);
		if (geneName!=null)
			geneName.setValue(geneN);
	}

	/*.................................................................................................................*/
	/** Called to operate on the data in all cells.  Returns true if data altered*/
	public boolean fetchGenBank(){ 
		logln("\nFetching GenBank entries: "  + genBankNumbers);
		try {
			String directory = MesquiteFile.chooseDirectory("Choose directory into which files will be saved:");
			if (StringUtil.blank(directory))
				return false;
			if (!directory.endsWith(MesquiteFile.fileSeparator))
				directory+=MesquiteFile.fileSeparator;


			String[] accessionNumbers = StringUtil.delimitedTokensToStrings(genBankNumbers,',',true);
			for (int i=0; i<accessionNumbers.length; i++) 
				if (!StringUtil.blank(accessionNumbers[i])) 				
					logln ("Accession numbers " + accessionNumbers[i]);

			logln("Querying for IDs of entries.");
			String[] idList = NCBIUtil.getGenBankIDs(accessionNumbers, isDNA,  this, true);
			if (idList==null)
				return false;
			logln("IDs acquired.");
			/*for (int i=0; i<idList.length; i++) 
						if (!StringUtil.blank(idList[i])) 				
							logln ("To Fetch " + idList[i]);*/
			
			boolean saveAsFasta = true;

			MesquiteString taxonName= new MesquiteString();
			MesquiteString voucherCode= new MesquiteString();
			MesquiteString sequence= new MesquiteString();
			MesquiteString geneName= new MesquiteString();
			MesquiteString fragmentName= new MesquiteString();
			boolean sequencesFetched = false;
			logln("\nRequesting sequences.\n");
			StringBuffer report = new StringBuffer();
			String[] sequences = NCBIUtil.fetchGenBankSequenceStrings(idList,isDNA, this, true, "gb", "xml", report);
			if (sequences!=null && sequences.length>0) {
				for (int i=0; i<sequences.length; i++) {
					if (StringUtil.notEmpty(sequences[i])) {
						String fileName = sequences[i] ;
						String fileContent = "";
						processGenBankXML(sequences[i], taxonName, voucherCode, sequence, geneName);
						String filePath = directory;
						if (!voucherCode.isBlank())
							filePath += "&v"+voucherCode.getValue()+"_";
						if (!geneName.isBlank())
							filePath += "&g"+geneName.getValue()+"_";
						if (!fragmentName.isBlank())
							filePath += "&f"+fragmentName.getValue()+"_";
						filePath += "&a"+accessionNumbers[i];
						if (!taxonName.isBlank())
							filePath += "_&n"+taxonName.getValue();
						StringBuffer fileContents = new StringBuffer();
						if (saveAsFasta) {
							filePath += ".fas";
							fileContents.append(">");
							if (!taxonName.isBlank())
								fileContents.append(taxonName.getValue()+" ");
							if (!geneName.isBlank())
								fileContents.append(geneName.getValue()+ " ");
							if (!fragmentName.isBlank())
								fileContents.append(fragmentName.getValue()+ " ");
							if (!voucherCode.isBlank())
								fileContents.append(voucherCode.getValue()+ " ");
							fileContents.append(StringUtil.lineEnding());
							if (!sequence.isBlank())
								fileContents.append(sequence.getValue());
							fileContents.append(StringUtil.lineEnding());

						}
						else {
							filePath += ".xml";
							fileContents.append(sequences[i]);
						}
						MesquiteFile.putFileContents(filePath, fileContents.toString(), true);

						sequencesFetched=true;
					}
				}
			}


			log(report.toString());
			return sequencesFetched;


		} catch ( Exception e ){
			// better warning
			return false;
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Fetches GenBanks sequences and saves them in separate files.", null, commandName, "fetchGenBank")) {

			if (queryOptions()) {
				fetchGenBank();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		return temp;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Fetch GenBank Sequences...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Fetch GenBank Sequences";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Fetches GenBank nucleotide sequences given their GenBank accession numbers saves them to files.";
	}
}





