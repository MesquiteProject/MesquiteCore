/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.IDsDefaults;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
public class IDsDefaults extends DefaultsAssistant {
	public String getName() {
		return "Unique IDs default";
	}
	public String getExplanation() {
		return "Sets the preferences for storing unique ids.";
	}
	/*.................................................................................................................*/
	MesquiteBoolean autoInventTaxaIDs, autoInventCharIDs;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		autoInventTaxaIDs = new MesquiteBoolean(Taxa.inventUniqueIDs);
		autoInventCharIDs = new MesquiteBoolean(CharacterData.defaultInventUniqueIDs);
		loadPreferences();
		addCheckMenuItemToDefaults(null, "Invent Unique IDs for New Taxa", makeCommand("autoInventTaxaIDs",  this), autoInventTaxaIDs);
		addCheckMenuItemToDefaults(null, "Invent Unique IDs for New Characters", makeCommand("autoInventCharIDs",  this), autoInventCharIDs);
		return true;
	}
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			Taxa.inventUniqueIDs = "store".equalsIgnoreCase(prefs[0]);
			if (prefs.length>1){
				CharacterData.defaultInventUniqueIDs = "store".equalsIgnoreCase(prefs[1]);
			}

		}
	}
	/*.................................................................................................................*
	public String[] preparePreferencesForFile () {
		if (!StringUtil.blank(MesquiteModule.author.getName())) {
			if (Taxa.inventUniqueIDs) {
				if (CharacterData.defaultInventUniqueIDs)
					return (new String[] {"store", "store"});
				else
					return (new String[] {"store", "noStore"});
			}
			else {
				if (CharacterData.defaultInventUniqueIDs)
					return (new String[] {"noStore", "store"});
				else
					return (new String[] {"noStore", "noStore"});
			}
		}
		return null;
	}
	*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("autoInventTaxaIDs".equalsIgnoreCase(tag)){
			autoInventTaxaIDs.setValue(content);
			Taxa.inventUniqueIDs = autoInventTaxaIDs.getValue();
		
		}
		else if ("autoInventCharIDs".equalsIgnoreCase(tag)){
			autoInventCharIDs.setValue(content);
			CharacterData.defaultInventUniqueIDs = autoInventCharIDs.getValue();
		}
	}
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "autoInventTaxaIDs", autoInventTaxaIDs);  
		StringUtil.appendXMLTag(buffer, 2, "autoInventCharIDs", autoInventCharIDs);  
		return buffer.toString();
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Toggles taxa id storage", null, commandName, "autoInventTaxaIDs")) {
			autoInventTaxaIDs.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting() && autoInventTaxaIDs.getValue() != Taxa.inventUniqueIDs && !Taxa.inventUniqueIDs){
				alert("Already existing taxa will not have new Unique ID's made for them automatically.  To assign ID's, show the column Unique IDs via the List menu of the List of Taxa, and use the column's drop down menu to adjust IDs");
			}
			Taxa.inventUniqueIDs = autoInventTaxaIDs.getValue();

			storePreferences();

			return null;

		}
		else if (checker.compare(getClass(), "Toggles character id storage", null, commandName, "autoInventCharIDs")) {
			autoInventCharIDs.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting() && autoInventCharIDs.getValue() != CharacterData.defaultInventUniqueIDs && !CharacterData.defaultInventUniqueIDs){
				alert("Already existing characters will not have new Unique ID's made for them automatically.  To assign ID's, show the column Unique IDs via the List menu of the List of Characters, and use the column's drop down menu to adjust IDs");
			}
			CharacterData.defaultInventUniqueIDs = autoInventCharIDs.getValue();
			for (int ip = 0; ip<MesquiteTrunk.getProjectList().getNumProjects(); ip++){
				MesquiteProject proj = MesquiteTrunk.getProjectList().getProject(ip);
				for (int im = 0; im<proj.getNumberCharMatrices(); im++){
					CharacterData m = proj.getCharacterMatrix(im);
					m.setInventUniqueIDs(CharacterData.defaultInventUniqueIDs);
				}
			}
			storePreferences();

			return null;

		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

}


