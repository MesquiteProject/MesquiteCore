/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.ListInFileTaxonSelector;

import java.awt.Button;
import java.awt.Checkbox;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.TaxonSelector;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;

public class ListInFileTaxonSelector extends TaxonSelector {
	String fileList = "";
	boolean caseSensitive = false;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()){
			fileList = getListFromFile();
			if (StringUtil.notEmpty(fileList) && !queryOptions())
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
		return 310;  
	}
	
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Select Taxa from List In File",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		dialog.addLabel("Select Taxa from List In File");

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
	private String getListFromFile () {

		MesquiteString directoryName = new MesquiteString("");
		MesquiteString fileName = new MesquiteString("");
		String filePath = MesquiteFile.openFileDialog("Choose file containing list of taxa.",  directoryName,  fileName);
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
			int it=taxa.whichTaxonNumber(lines[i],caseSensitive,false);
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
		return "Select Taxa from List In File";
	}
	public String getNameForMenuItem() {
		return "Select Taxa from List In File...";
	}
	public String getExplanation() {
		return "Select all taxa whose names appear in a list in a simple text file.  The file should consist of one column, listing the names.";
	}


}
