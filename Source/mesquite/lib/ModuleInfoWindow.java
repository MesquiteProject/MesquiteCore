/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.lib;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/** A Dialog giving information about an instantiated module or a class of modules*/
public class ModuleInfoWindow extends ExtensibleDialog {
	TextArea t;
	public ModuleInfoWindow (MesquiteModule module) {
		super(null, "Active Module: " + module.getName());
		String versionString = module.getVersion();
		if (versionString == null)
			versionString = "?";
		t = addTextArea("Information concerning active module \n\nModule: "  + module.getName() + "\n" 
		+ "Version: " + versionString + "\n" 
		+ "Author(s): " + module.getAuthors()+ "\n\n" 
		+ "Class: " + module.getClass().getName()  + "\n"
		+ "[id: " + module.getID() + "]\n\n"
		+ "Duty Performed: " + module.getDutyName()+  " (" + module.getDutyClass().getName() + ")\n\n" 
		+ "Explanation: " + module.getExplanation()+ "\n\n" 
		+ "Current Parameters: " + module.getParameters(), 20);
		addAuxiliaryDefaultPanels();
		addPrimaryButtonRow("OK");
		prepareAndDisplayDialog();
	}
	public ModuleInfoWindow (MesquiteWindow parent, Class dutyClass, String name) {
		super(parent, "");
		MesquiteModuleInfo mbi =MesquiteTrunk.mesquiteModulesInfoVector.findModule(dutyClass, name);
		if (mbi!=null) { 
			try {
				MesquiteModule mb = (MesquiteModule)mbi.mbClass.newInstance();
				if (mb!=null) {
					setTitle("Information concerning module \n\nModule: " + mb.getName());
					String versionString = mb.getVersion();
					if (versionString == null)
						versionString = "?";
					t = addTextArea("Module: " + mb.getName() + "\n\n" 
					+ "Version: " + versionString + "\n\n" 
					+ "Author(s): " + mb.getAuthors()+ "\n\n" 
					+ "Duty Performed: " + mb.getDutyName()+ "\n\n" 
					+ "Explanation: " + mb.getExplanation(),20);
					setVisible(true);
				}
			}
			catch (Exception e){
				MesquiteTrunk.mesquiteTrunk.alert("Sorry, there was a problem");
				MesquiteFile.throwableToLog(this, e);
				dispose(); }
		}
		addAuxiliaryDefaultPanels();
		addPrimaryButtonRow("OK");
		prepareAndDisplayDialog();
	}
}

