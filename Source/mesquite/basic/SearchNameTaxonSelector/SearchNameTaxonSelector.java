/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.SearchNameTaxonSelector;

import java.awt.Button;
import java.awt.Checkbox;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.TaxonSelector;

public class SearchNameTaxonSelector extends TaxonSelector {
	String matchString = "";
	boolean caseSensitive = false;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting())
			if (!queryOptions())
				return false;
		return true;
	}
	
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("matchString".equalsIgnoreCase(tag)) 
			matchString = StringUtil.cleanXMLEscapeCharacters(content);
		if ("caseSensitive".equalsIgnoreCase(tag))
			caseSensitive = MesquiteBoolean.fromTrueFalseString(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "matchString", matchString);  
		StringUtil.appendXMLTag(buffer, 2, "caseSensitive", caseSensitive);  
		return buffer.toString();
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Select Taxa by Name Search",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		dialog.addLabel("Select Taxa by Name Search");

		SingleLineTextField matchField = dialog.addTextField("Search string:", matchString, 20);
		Checkbox caseSensitiveBox = dialog.addCheckBox("case sensitive", caseSensitive);


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			matchString = matchField.getText();
			caseSensitive = caseSensitiveBox.getState();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0) ;
	}

	public boolean match(String name, String matchString) {
		if (StringUtil.blank(matchString) || StringUtil.blank(name))
			return false;
		if (!caseSensitive) {
			return (name.toLowerCase().indexOf(matchString.toLowerCase())>=0);
		}
		else return (name.indexOf(matchString)>=0);
	}

	public void selectTaxa(Taxa taxa, CharacterData data) {
		selectTaxa(taxa);
	}

	public void selectTaxa(Taxa taxa) {
		if (taxa==null)
			return;
		boolean changed = false;
		for (int it = 0; it<taxa.getNumTaxa(); it++)
			if (match(taxa.getTaxonName(it), matchString)) {
					taxa.setSelected(it, true);
					changed = true;
			}
		if (changed)
			taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

	}

	public boolean isPrerelease() {
		return false;
	}

	public String getName() {
		return "Select Taxa from Name Search";
	}
	public String getNameForMenuItem() {
		return "Select Taxa from Name Search...";
	}
	public String getExplanation() {
		return "Select taxa based upon a name search.";
	}


}
