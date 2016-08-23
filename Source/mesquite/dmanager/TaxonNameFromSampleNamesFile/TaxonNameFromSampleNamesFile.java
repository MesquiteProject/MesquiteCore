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
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxonNameFromSampleNamesFile extends TaxonNameAlterer implements ActionListener, TextListener {
	String sampleCodeListPath = null;
	String sampleCodeList = "";
	Parser sampleCodeListParser = null;
	boolean preferencesSet = false;
	SingleLineTextField sampleCodeFilePathField = null;
	int chosenNameCategory = -1;
	String[] nameCategories = null;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	public boolean processNameCategories() {
		sampleCodeListParser = new Parser(sampleCodeList);
		Parser subParser = new Parser();
		String line = sampleCodeListParser.getRawNextDarkLine();
		subParser.setString(line);
		subParser.setWhitespaceString("\t");
		subParser.setPunctuationString("");
		String s = subParser.getFirstToken(); // should be "code"
		s = subParser.getNextToken(); // should be "File Name"
		int count = 0;
		while (!subParser.atEnd() && StringUtil.notEmpty(s)){  // let's count
			s = subParser.getNextToken(); // should be  the category name
			if (StringUtil.notEmpty(s))
				count++;
		}
		nameCategories = new String[count];
		subParser.setString(line);
		s = subParser.getFirstToken(); // should be "code"
		s = subParser.getNextToken(); // should be "File Name"
		count = 0;
		while (!subParser.atEnd() && StringUtil.notEmpty(s)){
			s = subParser.getNextToken(); // should be the category name
			if (StringUtil.notEmpty(s)){
				nameCategories[count]=s;
				count++;
			}
		}
		return true;

	}
	/*.................................................................................................................*/
	public boolean scanTabbedDocument() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);

			if (!StringUtil.blank(sampleCodeList)) {
				sampleCodeListParser = new Parser(sampleCodeList);
				return processNameCategories();
			}
		}	
		return false;

	}
	/*.................................................................................................................*/
	public boolean queryForOptionsAsNeeded() {
		if (StringUtil.blank(sampleCodeListPath)){
			return queryOptions();
		}			
		if (StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);
		}
		if (!StringUtil.blank(sampleCodeList)) 
			sampleCodeListParser = new Parser(sampleCodeList);
		if (nameCategories==null){
			return processNameCategories();
		}
		return true;
	}
	/*.................................................................................................................*/
   	/** A stub method for querying the user about options. If alterIndividualTaxonNames is used to 
   	alter the names, and options need to be specified for the operation, then optionsQuery should be overridden.*/
 	public  boolean getOptions(Taxa taxa, int firstSelected){
		scanTabbedDocument();  // do this just in case there is already one there
		if (!queryOptions())
			return false;
		if (!scanTabbedDocument())
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
		return StringUtil.notEmpty(sampleCodeListPath);
	}


	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("sampleCodeListPath".equalsIgnoreCase(tag)){
			sampleCodeListPath = StringUtil.cleanXMLEscapeCharacters(content);
		}
		if ("chosenNameCategory".equalsIgnoreCase(tag)){
			chosenNameCategory = MesquiteInteger.fromString(content);
		}
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "sampleCodeListPath", sampleCodeListPath);  
		StringUtil.appendXMLTag(buffer, 2, "chosenNameCategory", chosenNameCategory);  
		preferencesSet = true;
		return buffer.toString();
	}
	Choice categoryChoice ;

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Rename Taxa Based On ID Code File",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		sampleCodeFilePathField = dialog.addTextField("Taxon names file:", sampleCodeListPath,26);
		sampleCodeFilePathField.addTextListener(this);
		final Button dnaCodesBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		dnaCodesBrowseButton.setActionCommand("TaxonNameFileBrowse");

		String[] categories=null;

		if (nameCategories==null) {
			categories = new String[1];
			categories[0]="Sample Code                  ";
		} else
			categories = nameCategories;

		int currentCategory = chosenNameCategory;
		if (currentCategory<0)
			currentCategory=0;
		categoryChoice = dialog.addPopUpMenu("Names to use:", categories, currentCategory);

		String s = "This file must contain in its first line the titles of each of the columns, delimited by tabs.  The first column must be the OTU ID code, ";
		s+= "and the second and later columns should contain naming schemes for the sequences. Each of the following lines must contain the entry for one sample.\n\n";
		s+= "<BR><BR>For example, the file might look like this:<br><br>\n";
		s+= "code  &lt;tab&gt;  Short name &lt;tab&gt;  Simple name  &lt;tab&gt;  Name with numbers  &lt;tab&gt;  Name with localities <br>\n";
		s+= "001  &lt;tab&gt;  Bemb_quadrimaculatum_001 &lt;tab&gt;  Bembidion quadrimaculatum  &lt;tab&gt;  Bembidion quadrimaculatum 001  &lt;tab&gt;  Bembidion quadrimaculatum ONT <br>\n";
		s+= "002  &lt;tab&gt;  Bemb_festivum_002 &lt;tab&gt;  Bembidion festivum  &lt;tab&gt;  Bembidion festivum 002  &lt;tab&gt;  Bembidion festivum CA:Fresno <br>\n";
		s+= "003  &lt;tab&gt;  Bemb_occultator_003 &lt;tab&gt;  Bembidion occultator  &lt;tab&gt;  Bembidion occultator 003  &lt;tab&gt;  Bembidion occultator ON:Dwight <br>\n";
		s+= "004  &lt;tab&gt;  Lion_chintimini_004 &lt;tab&gt;  Lionepha chintimini  &lt;tab&gt;  Lionepha chintimini 004  &lt;tab&gt;  Lionepha chintimini OR:Marys Peak <br><br>\n";
		s+= "You will need to choose which of the later columns are to be used as the source of the new name for each taxon.\n\n";

		
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			sampleCodeListPath = sampleCodeFilePathField.getText();
			chosenNameCategory = categoryChoice.getSelectedIndex();
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
	/*.................................................................................................................*/

	public  String[] getSeqNamesFromTabDelimitedFile(MesquiteString sampleCode) {
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
			String code = subParser.getFirstToken();
			if (sampleCodeString.equalsIgnoreCase(code)) {
				String fileName = subParser.getNextToken();
				String sequenceName=subParser.getNextToken();
				for (int i=0; i<chosenNameCategory; i++) {
					sequenceName=subParser.getNextToken();
				}
				if (StringUtil.blank(sequenceName))
					sequenceName=fileName;
				return new String[]{fileName, sequenceName};
			}
			line = sampleCodeListParser.getRawNextDarkLine();
		}
		// got here and no match found -- log an error
		MesquiteMessage.warnUser("No OTU ID code named '" + sampleCode + "' found in taxon names file.");
		return null;
	}

	/*.................................................................................................................*/
	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
	public boolean alterName(Taxa taxa, int it){
		boolean nameChanged = false;
		String name = taxa.getTaxonName(it);
		if (name!=null){
			String vc = getVoucherCode(taxa, it);
			String[] names = getSeqNamesFromTabDelimitedFile(new MesquiteString(vc));
			if (names!=null && names.length>1&& StringUtil.notEmpty(names[1])){
				taxa.setTaxonName(it, names[1], false);
			}
			nameChanged = true;
		}
		return nameChanged;
	}
	public String getVoucherCode(Taxa taxa, int ic){

		if (taxa!=null) {
			String s = (String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, ic);
			return s;
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

	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("TaxonNameFileBrowse")) {
			MesquiteString dnaNumberListDir = new MesquiteString();
			MesquiteString dnaNumberListFile = new MesquiteString();
			String s = MesquiteFile.openFileDialog("Choose file containing OTU ID codes and names", dnaNumberListDir, dnaNumberListFile);
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
				for (int i=0; i<nameCategories.length; i++) 
					if (!StringUtil.blank(nameCategories[i])) {
						categoryChoice.add(nameCategories[i]);
					}
			}
			categoryChoice.repaint();
			//				categoryChoice = dialog.addPopUpMenu("Names to use:", xmlProcessor.getNameCategoryDescriptions(), tagNumber);

		}
	}

	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Rename Taxa Based On ID Code File...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Rename Taxa Based On ID Code File";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Renames the taxa based upon chosen columns in a tab-delimited text file, using OTU IDs to find the relevant row within that file.";
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}

	/*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }

}




