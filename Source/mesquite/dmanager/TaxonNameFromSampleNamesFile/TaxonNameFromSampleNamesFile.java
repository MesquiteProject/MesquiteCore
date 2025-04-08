/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.dmanager.TaxonNameFromSampleNamesFile;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.misc.VoucherInfoFromOTUIDDB;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ExtensibleDialog;

/* ======================================================================== */
public class TaxonNameFromSampleNamesFile extends TaxonNameAlterer  {
	String[][] sampleCodeList;
	int chosenNameCategory = 0;
	boolean preferencesSet = false;
	MesquiteBoolean matchCurrentTaxonName = new MesquiteBoolean(true);
	MesquiteBoolean changeColor = new MesquiteBoolean(true);
	//Parser sampleCodeListParser = null;
//	SingleLineTextField sampleCodeFilePathField = null;
//	String[] nameCategories = new String[]{"<choose column>"};
	//String sampleCodeListPath = null;
	MesquiteTabDelimitedFileProcessor mesquiteTabbedFile;
	
	//TODO: note that the sample code in the names file CANNOT contain "/" 

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		mesquiteTabbedFile = new MesquiteTabDelimitedFileProcessor();
		loadPreferences();
		
		return true;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................
	public boolean processNameCategories() {
		//sampleCodeListParser = new Parser(sampleCodeList);
		//Parser subParser = new Parser();
		//String line = sampleCodeListParser.getRawNextDarkLine();
		subParser.setString(line);
		subParser.setWhitespaceString("\t");
		subParser.setPunctuationString("");
		String s = subParser.getFirstToken(); // should be "code"
//		s = subParser.getNextToken(); // should be "File Name"
		int count = 0;
		while (!subParser.atEnd() && StringUtil.notEmpty(s)){  // let's count
			s = subParser.getNextToken(); // should be  the category name
			if (StringUtil.notEmpty(s))
				count++;
		}
		nameCategories = new String[count];
		subParser.setString(line);
		s = subParser.getFirstToken(); // should be "code"
	//	s = subParser.getNextToken(); // should be "File Name"
		count = 0;
		while (!subParser.atEnd() && StringUtil.notEmpty(s)){
			s = subParser.getNextToken(); // should be the category name
			if (StringUtil.notEmpty(s)){
				nameCategories[count]=s;
				count++;
			}
		}
		return true;

	}*/
	/*.................................................................................................................*
	public boolean processNameCategories() {
		int count = sampleCodeList[0].length;
		for (int i = 1; i<sampleCodeList[0].length; i++){
			if (StringUtil.notEmpty(sampleCodeList[0][i]))
				count++;
		}
		nameCategories = new String[count+1];
		count = 0;
		nameCategories[count++]="<choose column>";
		for (int i = 1; i<sampleCodeList[0].length; i++){
			String s = sampleCodeList[0][i];
			if (StringUtil.notEmpty(s)) {
				nameCategories[count]=s;
				count++;
			}
		}
		return true;

	}
	/*.................................................................................................................*
	public boolean scanTabbedDocument() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getTabDelimitedTextFile(sampleCodeListPath);

			if (sampleCodeList != null && sampleCodeList.length>0 && sampleCodeList[0].length>1) {
				return processNameCategories();
			}
		}	
		return false;

	}
	/*.................................................................................................................*/
	public boolean queryForOptionsAsNeeded() {
		if (StringUtil.blank(mesquiteTabbedFile.getSampleCodeListPath()))
			return queryOptions();
		return mesquiteTabbedFile.processNameCategories();
	}
	/*.................................................................................................................*/
	/** A stub method for querying the user about options. If alterIndividualTaxonNames is used to 
   	alter the names, and options need to be specified for the operation, then optionsQuery should be overridden.*/
	public  boolean getOptions(Taxa taxa, int firstSelected){
		mesquiteTabbedFile.scanTabbedDocument();  // do this just in case there is already one there
		if (!queryOptions())
			return false;
		if (!mesquiteTabbedFile.scanTabbedDocument())
			return false;
		return true;
	}

	/*.................................................................................................................*
	public void initialize() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);

			if (!StringUtil.blank(sampleCodeList)) {
				sampleCodeListParser = new Parser(sampleCodeList);
				processNameCategories();
			}
		}			
	}

	/*.................................................................................................................*/
	public boolean optionsSpecified(){
		return StringUtil.notEmpty(mesquiteTabbedFile.getSampleCodeListPath());
	}


	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
	/*	if ("sampleCodeListPath".equalsIgnoreCase(tag)){
			sampleCodeListPath = StringUtil.cleanXMLEscapeCharacters(content);
		}
		if ("chosenNameCategory".equalsIgnoreCase(tag)){
			chosenNameCategory = MesquiteInteger.fromString(content);
		}
		*/
		if ("matchCurrentTaxonName".equalsIgnoreCase(tag)){
			matchCurrentTaxonName.setValue(content);
		}
		if ("changeColor".equalsIgnoreCase(tag)){
			changeColor.setValue(content);
		}
		mesquiteTabbedFile.processSingleXMLPreference(tag, content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
	//StringUtil.appendXMLTag(buffer, 2, "sampleCodeListPath", sampleCodeListPath);  
		//StringUtil.appendXMLTag(buffer, 2, "chosenNameCategory", chosenNameCategory);  
		StringUtil.appendXMLTag(buffer, 2, "matchCurrentTaxonName", matchCurrentTaxonName);  
		StringUtil.appendXMLTag(buffer, 2, "changeColor", changeColor);  
		mesquiteTabbedFile.preparePreferencesForXML(buffer);
		preferencesSet = true;
		return buffer.toString();
	}
	Choice categoryChoice ;

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Rename Taxa Based On Table with File",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		mesquiteTabbedFile.addTabbedFileChooser(dialog, "File with Replacement Names", "Column for Replacement Names");
		dialog.addLabelSmallText("(Touch on help button, below, for a description of the format of the file)");
		dialog.addHorizontalLine(1);
		Checkbox matchTaxonName = dialog.addCheckBox("Match Current Taxon Name (otherwise Taxon ID code)", matchCurrentTaxonName.getValue());
		Checkbox colorChanged = dialog.addCheckBox("Color changed taxa", changeColor.getValue());
		
		//sampleCodeFilePathField = dialog.addTextField("File with Replacement Names:", sampleCodeListPath,26);
		//sampleCodeFilePathField.addTextListener(this);
		//final Button dnaCodesBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		//dnaCodesBrowseButton.setActionCommand("TaxonNameFileBrowse");
		/*
		String[] categories=null;

		if (nameCategories==null) {
			categories = new String[1];
			categories[0]="Sample Code                  ";
		} else
			categories = nameCategories;

		int currentCategory = chosenNameCategory;
		if (currentCategory<0)
			currentCategory=0;
		categoryChoice = dialog.addPopUpMenu("Column for Replacement Names:", categories, currentCategory);
*/
		
		
		String s = "This file must contain in its first line the titles of each of the columns, delimited by tabs.  The first column must be the target to match (current taxon name or Taxon ID code), ";
		s+= "and the second and later columns should contain naming schemes for the sequences. Each of the following lines must contain the entry for one sample.\n\n";
		s+= "<BR><BR>For example, the file might look like this:<br><br>\n";
		s+= "code  &lt;tab&gt;  Short name &lt;tab&gt;  Simple name  &lt;tab&gt;  Name with numbers  &lt;tab&gt;  Name with localities <br>\n";
		s+= "001  &lt;tab&gt;  Bemb_quadrimaculatum_001 &lt;tab&gt;  Bembidion quadrimaculatum  &lt;tab&gt;  Bembidion quadrimaculatum 001  &lt;tab&gt;  Bembidion quadrimaculatum ONT <br>\n";
		s+= "002  &lt;tab&gt;  Bemb_festivum_002 &lt;tab&gt;  Bembidion festivum  &lt;tab&gt;  Bembidion festivum 002  &lt;tab&gt;  Bembidion festivum CA:Fresno <br>\n";
		s+= "003  &lt;tab&gt;  Bemb_occultator_003 &lt;tab&gt;  Bembidion occultator  &lt;tab&gt;  Bembidion occultator 003  &lt;tab&gt;  Bembidion occultator ON:Dwight <br>\n";
		s+= "004  &lt;tab&gt;  Lion_chintimini_004 &lt;tab&gt;  Lionepha chintimini  &lt;tab&gt;  Lionepha chintimini 004  &lt;tab&gt;  Lionepha chintimini OR:Marys Peak <br><br>\n";
		s+= "You will need to choose which of the later columns is to be used as the source of the new name for each taxon.\n\n";
		
		

		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			mesquiteTabbedFile.processTabbedFileChoiceExtensibleDialog();
			mesquiteTabbedFile.processNameCategories();
			sampleCodeList = mesquiteTabbedFile.getSampleCodeList();
			chosenNameCategory = mesquiteTabbedFile.getChosenNameCategory();

			//sampleCodeListPath = sampleCodeFilePathField.getText();
			//chosenNameCategory = categoryChoice.getSelectedIndex();
			changeColor.setValue(colorChanged.getState());
			matchCurrentTaxonName.setValue(matchTaxonName.getState());
			//	initialize();  // is this needed?
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}
	/*.................................................................................................................*/
	/**Returns true if the module is to appear in menus and other places in which users can choose, and if can be selected in any way other than by direct request*/
	public boolean getUserChooseable(){
		return true; 
	}
	/*.................................................................................................................*

	public  String getSeqNamesFromTabDelimitedFile(MesquiteString sampleCode) {
		//public  String[] getSeqNamesFromTabDelimitedFile(MesquiteString sampleCode) {
		queryForOptionsAsNeeded();
		if (sampleCodeListParser==null)
			return null;
		String sampleCodeString  = sampleCode.getValue();
		if (sampleCodeString==null)
			return null;
		sampleCodeListParser.setPosition(0);
		Parser subParser = new Parser();
		String line = sampleCodeListParser.getRawNextDarkLine();
		while (StringUtil.notEmpty(line)) {  
			subParser.setString(line);
			subParser.setWhitespaceString("\t");
			subParser.setPunctuationString("");
			String code = subParser.getFirstRawToken();
			if (sampleCodeString.equalsIgnoreCase(code)) {
				//String fileName = subParser.getNextToken(); //
				String sequenceName=subParser.getNextToken();
				for (int i=0; i<chosenNameCategory; i++) {
					sequenceName=subParser.getNextToken();
				}
					if (StringUtil.blank(sequenceName))
					sequenceName=fileName;
				return new String[]{fileName, sequenceName};
				return sequenceName;
			}
			line = sampleCodeListParser.getRawNextDarkLine();
		}
		// got here and no match found -- log an error
		MesquiteMessage.warnUser("No Taxon ID code named '" + sampleCode + "' found in taxon names file.");
		return null;
	}
	/*.................................................................................................................*
	public  String getNameFromTabDelimitedFile(MesquiteString sampleCode, String taxonName) {
		String sampleCodeString  = sampleCode.getValue();
		if (sampleCodeString==null){
			MesquiteMessage.warnUser("Taxon \"" + taxonName + "\" has no target string to match.");
			return null;
		}
		if (sampleCodeString.contains("/"))  // if there is more than one item, then take only the first one.
			sampleCodeString = StringUtil.getFirstItem(sampleCodeString, "/");
		for (int row = 0; row<sampleCodeList.length; row++){
			if (chosenNameCategory< sampleCodeList[row].length){
				String code = sampleCodeList[row][0];
				if (sampleCodeString.equalsIgnoreCase(code)) {
					return sampleCodeList[row][chosenNameCategory];
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public  String getSeqNamesFromTabDelimitedFile(MesquiteString sampleCode, String taxonName) {
		queryForOptionsAsNeeded();
		return mesquiteTabbedFile.getNameFromTabDelimitedFile(sampleCode, taxonName);
	}


	/*.................................................................................................................*/
	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
	public boolean alterName(Taxa taxa, int it){
		boolean nameChanged = false;
		String name = taxa.getTaxonName(it);
		if (name!=null){
			String vc = getTargetString(taxa, it);
			/*String[] names = getSeqNamesFromTabDelimitedFile(new MesquiteString(vc));
			if (names!=null && names.length>1&& StringUtil.notEmpty(names[1])){
				taxa.setTaxonName(it, names[1], false);
			}*/
			String newName = getSeqNamesFromTabDelimitedFile(new MesquiteString(vc), taxa.getTaxonName(it));
			if (StringUtil.notEmpty(newName)){
				if (newName.equalsIgnoreCase(taxa.getTaxonName(it)))
					logln(""+it+". Taxon \"" + taxa.getTaxonName(it) +"\" kept current name");
				else
					logln(""+it+". Taxon \"" + taxa.getTaxonName(it) +"\" renamed to \"" + newName + "\"");
				taxa.setTaxonName(it, newName, false);
				if (changeColor.getValue())
					taxa.setColor(it, "#0000ff");
				
			} else
				logln(""+it+". Taxon \"" + taxa.getTaxonName(it) +"\": no entry in names file.");
			nameChanged = true;
		}
		return nameChanged;
	}
	/*.................................................................................................................*/
	public String getTargetString(Taxa taxa, int ic){

		if (taxa!=null) {
			if (matchCurrentTaxonName.getValue()){
				return taxa.getTaxonName(ic);
			}
			else {
			String s = (String)taxa.getAssociatedString(VoucherInfoFromOTUIDDB.voucherCodeRef, ic);
			return s;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Alters taxon names to match those in a file", "[]", commandName, "alterNames")) {
			if (taxa !=null){
				alterTaxonNames(taxa,table);
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("TaxonNameFileBrowse")) {
			MesquiteString dnaNumberListDir = new MesquiteString();
			MesquiteString dnaNumberListFile = new MesquiteString();
			String s = MesquiteFile.openFileDialog("Choose file containing Taxon ID codes and names", dnaNumberListDir, dnaNumberListFile);
			if (!StringUtil.blank(s)) {
				sampleCodeListPath = s;
				if (sampleCodeFilePathField!=null) 
					sampleCodeFilePathField.setText(sampleCodeListPath);
			}
		}
	}
	public void textValueChanged(TextEvent e) {
		if (e.getSource().equals(sampleCodeFilePathField)) {
			sampleCodeListPath = sampleCodeFilePathField.getText();
			if (StringUtil.notEmpty(sampleCodeListPath)) {
				chosenNameCategory=-1;
				scanTabbedDocument();
				//	processNameCategories();
				//initialize(sampleCodeListPath);
			}	
			categoryChoice.removeAll();
			if (nameCategories!=null) {			
				for (int i=0; i<nameCategories.length; i++) {
					if (!StringUtil.blank(nameCategories[i])) {
						categoryChoice.add(nameCategories[i]);
					}
				}
			}
			categoryChoice.repaint();
			//				categoryChoice = dialog.addPopUpMenu("Names to use:", xmlProcessor.getNameCategoryDescriptions(), tagNumber);

		}
	}

	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Rename Taxa Based On Table in File...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Rename Taxa Based On Table in File";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Renames the taxa based upon chosen columns in a tab-delimited text file, using Taxon IDs or curent taxon names to find the relevant row within that file.";
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}

	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

}




