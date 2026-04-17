/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.ListOfTaxonIDsInFileTaxonSelector;

import java.awt.Checkbox;

import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.TaxonSelector;
import mesquite.lib.misc.VoucherInfoFromOTUIDDB;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;

public class ListOfTaxonIDsInFileTaxonSelector extends TaxonSelector {
	String fileList = "";
	boolean caseSensitive = false;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()){
			fileList = getListFromFile();
			if (StringUtil.notEmpty(fileList))
				return false;
		}
		return true;
	}
	
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("caseSensitive".equalsIgnoreCase(tag))
			caseSensitive = MesquiteBoolean.fromTrueFalseString(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "caseSensitive", caseSensitive);  
		return buffer.toString();
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	
	/*.................................................................................................................*
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Select Taxa from List of Taxon IDs In File",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		dialog.addLabel("Select Taxa from List of Taxon IDs In File");

		Checkbox caseSensitiveBox = dialog.addCheckBox("case sensitive", caseSensitive);


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			caseSensitive = caseSensitiveBox.getState();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0) ;
	}

	/*.................................................................................................................*/
	public boolean codesMatch(String OTUIDCode, String incomingCode) {
		if (StringUtil.blank(OTUIDCode))
			return false;
		if (!OTUIDCode.contains("/"))  // doesn't contain multiple entries
			return OTUIDCode.equalsIgnoreCase(incomingCode);
		Parser parser = new Parser(OTUIDCode);
		parser.setPunctuationString("/");
		String code = parser.getFirstToken();
		code = StringUtil.stripBoundingWhitespace(code);
		while (StringUtil.notEmpty(code)) {
			if (code.equalsIgnoreCase(incomingCode))
				return true;
			code = parser.getNextToken();
			code = StringUtil.stripBoundingWhitespace(code);
		}
		return false;

	}

	/*.................................................................................................................*/
	public int getTaxonNumber(Taxa taxa, String token) {
		if (StringUtil.blank(token))
			return -1;
		String code = "";
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			code = (String)taxa.getAssociatedString(VoucherInfoFromOTUIDDB.voucherCodeRef, it);
			if (codesMatch(code, token))
						return it;
		}
		return -1;
	}

	/*.................................................................................................................*/
	private String getListFromFile () {

		MesquiteString directoryName = new MesquiteString("");
		MesquiteString fileName = new MesquiteString("");
		String filePath = MesquiteFile.openFileDialog("Choose file containing list of taxon IDs.",  directoryName,  fileName);
		if (filePath != null) {
			String s = MesquiteFile.getFileContentsAsString(filePath);
			if (StringUtil.blank(s)) { 
				alert("Error: File is empty.");
				return null;
			}
			return s;
		}
		return null;
	}
	public void selectTaxa(Taxa taxa, CharacterData data) {
		selectTaxa(taxa);
	}

	public void selectTaxa(Taxa taxa) {
		if (taxa==null || StringUtil.blank(fileList))
			return;
		boolean changed = false;
		String[] lines = StringUtil.getLines(fileList);
		if (lines==null || lines.length==0)
			return;
		for (int i=0; i<lines.length; i++) {
			int it=getTaxonNumber(taxa, lines[i]);
			if (it>=0 && it<taxa.getNumTaxa()){
				taxa.setSelected(it, true);
				changed = true;
			}
		}
		if (changed)
			taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

	}

	public boolean isPrerelease() {
		return false;
	}

	public String getName() {
		return "Select Taxa from List of Taxon IDs In File";
	}
	public String getNameForMenuItem() {
		return "Select Taxa from List of Taxon IDs In File...";
	}
	public String getExplanation() {
		return "Select all taxa whose Taxon IDs appear in a list in a simple text file.  The file should consist of one column, listing the Taxon IDs.";
	}


}
