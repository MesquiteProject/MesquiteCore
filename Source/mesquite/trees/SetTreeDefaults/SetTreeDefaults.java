/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.SetTreeDefaults;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SetTreeDefaults extends DefaultsAssistant {
	MesquiteBoolean polytomiesHard, convertInternalNames; //, warnReticulations;
	static boolean changed = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		polytomiesHard = new MesquiteBoolean(true);
		convertInternalNames  = new MesquiteBoolean(false);
		//warnReticulations = new MesquiteBoolean(true);
		loadPreferences();

		MesquiteSubmenuSpec treeDefaultsSubmenu = addSubmenuToDefaults("Tree Defaults");
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.defaultsSubmenu, treeDefaultsSubmenu, "Polytomies Hard by Default", makeCommand("polytomiesHard",  this), polytomiesHard);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.defaultsSubmenu,  treeDefaultsSubmenu, "Convert Internal Names to Notes", makeCommand("convertInternalNames",  this), convertInternalNames);
		
		//addCheckMenuItemToSubmenu(null,  mss, "Warn with Reticulations", makeCommand("warnReticulations",  this), warnReticulations);
		return true;
  	 }
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			if (!changed) {
				if (prefs[0].equals("soft") && polytomiesHard.getValue()) {
					polytomiesHard.setValue(false);
					MesquiteTree.polytomyDefaultHard = false;
					MesquiteModule.mesquiteTrunk.classFieldChanged(Tree.class, "polytomyDefaultHard");
				}
				if (prefs.length>1){
					MesquiteTree.convertInternalNames = prefs[1].equals("convertInternalNames");
				}
				/*
    	 			if (prefs.length>2){
					   MesquiteTree.warnReticulations = prefs[2].equals("warnReticulations");
				 }
				 */
	
			}
		}
	}
	public void processSingleXMLPreference (String tag, String content) {
		if ("polytomiesHard".equalsIgnoreCase(tag)){
			polytomiesHard.setValue(content);
			MesquiteTree.polytomyDefaultHard = polytomiesHard.getValue();

			MesquiteModule.mesquiteTrunk.classFieldChanged(Tree.class, "polytomyDefaultHard");
		
		}
		else if ("convertInternalNames".equalsIgnoreCase(tag)){
			convertInternalNames.setValue(content);
			MesquiteTree.convertInternalNames = convertInternalNames.getValue();
		}
	}
	
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "polytomiesHard", polytomiesHard);  
		StringUtil.appendXMLTag(buffer, 2, "convertInternalNames", convertInternalNames);  
		return buffer.toString();
	}
	/*.................................................................................................................*
	public String[] preparePreferencesForFile () {
		String pString, cString;
		if (polytomiesHard.getValue())
			pString = "hard";
		else
			pString = "soft";
		if (convertInternalNames.getValue())
			cString = "convertInternalNames";
		else
			cString = "noconvertInternalNames";
		
		return (new String[] {pString, cString}); //, wString
	}
	


	*/
	
	
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets universal, Mesquite-wide setting whether polytomies are hard or soft by default", "[on = polytomies hard; off = soft]", commandName, "polytomiesHard")) {  
    	 		boolean current = polytomiesHard.getValue();
    	 		polytomiesHard.toggleValue(parser.getFirstToken(arguments));
    	 		if (current!=polytomiesHard.getValue()) {
    	 			MesquiteTree.polytomyDefaultHard = polytomiesHard.getValue();
    	 			//tell all modules that default has changed  classFieldChanged(Class class, String fieldName)
    	 			MesquiteModule.mesquiteTrunk.classFieldChanged(Tree.class, "polytomyDefaultHard");
    	 			storePreferences();
    	 			changed = true;
    	 			
    	 		}
			return polytomiesHard;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets universal, Mesquite-wide setting whether node labels converted to string annotations", "[on = convert; off = not]", commandName, "convertInternalNames")) {  
    	 		boolean current = convertInternalNames.getValue();
    	 		convertInternalNames.toggleValue(parser.getFirstToken(arguments));
    	 		if (current!=convertInternalNames.getValue()) {
    	 			MesquiteTree.convertInternalNames = convertInternalNames.getValue();
    	 			storePreferences();
    	 			changed = true;
    	 			
    	 		}
			return convertInternalNames;
    	 	}
    	 /*
    	 	else if (checker.compare(this.getClass(), "Sets universal, Mesquite-wide setting whether to warn if reticulations found", "[on = warn; off = not]", commandName, "warnReticulations")) {  
    	 		boolean current = warnReticulations.getValue();
    	 		warnReticulations.toggleValue(parser.getFirstToken(arguments));
    	 		if (current!=warnReticulations.getValue()) {
    	 			MesquiteTree.warnReticulations = warnReticulations.getValue();
    	 			storePreferences();
    	 			changed = true;
    	 			
    	 		}
			return warnReticulations;
    	 	}
    	*/
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
   	 }
  	 
	/*.................................................................................................................*/
    	 public String getName() {
		return "Set Tree Defaults";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Sets the default state for polytomies & other aspects of tree handling.";
   	 }
}


