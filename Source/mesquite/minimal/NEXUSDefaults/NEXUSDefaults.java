/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.NEXUSDefaults;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NEXUSDefaults extends DefaultsAssistant {
	public String getName() {
		return "NEXUS Defaults";
	}
	public String getExplanation() {
		return "Sets whether TITLE and LINK commands are to be suppressed where possible in saving NEXUS files.";
	}
	/*.................................................................................................................*/
	MesquiteBoolean suppressTitleLink;
	MesquiteBoolean suppressIDS;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		suppressTitleLink = new MesquiteBoolean(false);
		suppressIDS = new MesquiteBoolean(false);
		loadPreferences();
		addMenuItemToDefaults( "Automatic NEXUS backups...", makeCommand("autobackup",  this));
		addCheckMenuItemToDefaults( null, "Suppress TITLE and LINK in NEXUS files", makeCommand("toggleSuppress",  this), suppressTitleLink);
		addCheckMenuItemToDefaults( null, "Suppress IDs and BLOCKIDs in NEXUS files", makeCommand("toggleSuppressIDS",  this), suppressIDS);
		return true;
	}
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			if (prefs[0].equals("suppress")) {
				suppressTitleLink.setValue(true);
				NexusBlock.suppressNEXUSTITLESANDLINKS = true;
			}
			else  {
				suppressTitleLink.setValue(false);
				NexusBlock.suppressNEXUSTITLESANDLINKS = false;
			}
			if (prefs.length>1){
				int numBackups = MesquiteInteger.fromString(prefs[1]);
				if (MesquiteInteger.isCombinable(numBackups))
					NexusBlock.numBackups = numBackups;
			}
		}
	}

	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "suppressTitleLink", suppressTitleLink);   
		StringUtil.appendXMLTag(buffer, 2, "suppressIDS", suppressIDS);   
		StringUtil.appendXMLTag(buffer, 2, "numBackups", NexusBlock.numBackups);  
		return buffer.toString();
	}
	
	public void processSingleXMLPreference (String tag, String content) {
		if ("suppressTitleLink".equalsIgnoreCase(tag)) {
			suppressTitleLink.setValue(content);
			NexusBlock.suppressNEXUSTITLESANDLINKS = suppressTitleLink.getValue();
		}
		else if ("suppressIDS".equalsIgnoreCase(tag)) {
			suppressIDS.setValue(content);
			NexusBlock.suppressNEXUSIDS = suppressIDS.getValue();
		}
		else if ("numBackups".equalsIgnoreCase(tag))
			NexusBlock.numBackups = MesquiteInteger.fromString(content);
	}
	
	
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether TITLE and LINK commands are to be suppressed where possible in saving NEXUS files", "[on = suppress; off = no]", commandName, "toggleSuppress")) {  
			boolean current = suppressTitleLink.getValue();
			suppressTitleLink.toggleValue(parser.getFirstToken(arguments));
			if (current!=suppressTitleLink.getValue()) {
				NexusBlock.suppressNEXUSTITLESANDLINKS = suppressTitleLink.getValue();
				storePreferences();
			}
			return suppressTitleLink;
		}
		else if (checker.compare(this.getClass(), "Sets whether ID and BLOCKID commands are to be suppressed where possible in saving NEXUS files", "[on = suppress; off = no]", commandName, "toggleSuppressIDS")) {  
			boolean current = suppressIDS.getValue();
			suppressIDS.toggleValue(parser.getFirstToken(arguments));
			if (current!=suppressIDS.getValue()) {
				NexusBlock.suppressNEXUSIDS = suppressIDS.getValue();
				storePreferences();
			}
			return suppressIDS;
		}
		else if (checker.compare(getClass(), "Sets the number of previous backups", "[num backups]", commandName, "autobackup")) {
			int numBackups = MesquiteInteger.fromString(arguments);
			if (!MesquiteInteger.isCombinable(numBackups))
				numBackups = MesquiteInteger.queryInteger(containerOfModule(), "Number of backups", "Number of backups of NEXUS files saved automatically (enter 0 for no backups)", NexusBlock.numBackups, 0, 10000);
			if (!MesquiteInteger.isCombinable(numBackups) || numBackups == NexusBlock.numBackups)
				return null;
			NexusBlock.numBackups = numBackups;
			storePreferences();
			return null;

		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

}


