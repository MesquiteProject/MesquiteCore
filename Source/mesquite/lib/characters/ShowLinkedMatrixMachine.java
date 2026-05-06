/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import mesquite.charMatrices.BasicDataWindowCoord.BasicDataWindowCoord;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.Commandable;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Puppeteer;
import mesquite.lib.Snapshot;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.MesquiteMenu;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */

public class ShowLinkedMatrixMachine implements Commandable  {
	MesquiteModule module;
	MesquiteModule matrixWindowModule;
	MesquiteTree myTree;
	public ShowLinkedMatrixMachine(MesquiteModule module){
		this.module = module;
	}
	
	public void addPopupChoices(MesquitePopup popup, MesquiteTree tree){
			this.myTree = tree;
			if (matrixWindowModule != null && !matrixWindowModule.isDoomed())
				popup.addItem("Show Linked Matrix", module, new MesquiteCommand("showLinkedMatrix", this), "continue");
			else {
				MesquiteMenu whereWindowSubmenu = new MesquiteMenu("Show Linked Matrix");
				MesquiteMenuItem mmi = new MesquiteMenuItem("In Window", module, new MesquiteCommand("showLinkedMatrix", this), "in");
				whereWindowSubmenu.add(mmi);
				mmi = new MesquiteMenuItem("In Separate Tile", module, new MesquiteCommand("showLinkedMatrix", this), "tile");
				whereWindowSubmenu.add(mmi);
				mmi = new MesquiteMenuItem("Popped Out as Separate Window", module, new MesquiteCommand("showLinkedMatrix", this), "poppedOut");
				whereWindowSubmenu.add(mmi);

				popup.add(whereWindowSubmenu);			
			}
		}
	
	/*---------------------------------------------------------------*/
	public void showMatrix(CharacterData data, int where) {
		MesquiteModule bdwC = module.findNearestColleagueWithDuty(BasicDataWindowCoord.class);
		MesquiteProject proj = module.getProject();
		int imNext = proj.getMatrixNumber(data);
		if (matrixWindowModule == null || matrixWindowModule.isDoomed()) {
			MesquiteModule mb = (MesquiteModule)bdwC.doCommand("showExtraDataWindow", Integer.toString(imNext));
			MesquiteWindow mw = mb.containerOfModule();
			if (where == 1)
				mw.doCommand("toggleTileOutWindow","true", CommandChecker.defaultChecker);
			else if (where == 2)
				mw.doCommand("togglePopOutWindow","true", CommandChecker.defaultChecker);
			matrixWindowModule = (MesquiteModule)mb;		

		}
		else {
			CommandRecord previous = MesquiteThread.getCurrentCommandRecord();
			CommandRecord record = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(record);
			MesquiteModule mb = (MesquiteModule)bdwC.doCommand("showExtraDataWindow", Integer.toString(imNext));
			String cloneCommand =  Snapshot.getSnapshotCommands(matrixWindowModule, null, "");
			Puppeteer p = new Puppeteer(matrixWindowModule);
			MesquiteInteger pos = new MesquiteInteger(0);
			MesquiteModule.incrementMenuResetSuppression();	
			Object obj = p.sendCommands(mb, cloneCommand, pos, "", false, null,CommandChecker.defaultChecker);
			MesquiteModule.decrementMenuResetSuppression();	
			MesquiteThread.setCurrentCommandRecord(previous);
			MesquiteWindow oldWindow = matrixWindowModule.containerOfModule();
			oldWindow.doCommand("closeWindow","true", CommandChecker.defaultChecker);
			matrixWindowModule = mb;
		}
	}
/*.................................................................................................................*/
public Object doCommand(String commandName, String arguments, CommandChecker checker) {
	if (checker.compare(this.getClass(), "Shows linked matrix", null, commandName, "showLinkedMatrix")) {
		int where = 0;
		if (arguments == null || arguments.equals("in"))
			where = 0;
		else if (arguments.equalsIgnoreCase("tile"))
			where = 1;
		else if (arguments.equalsIgnoreCase("poppedOut"))
			where = 2;

		CharacterData data = myTree.findLinkedMatrix(module.getProject());
		if (data == null)
			module.logln("Sorry; no matrix linked to the tree has been found");
		else
			showMatrix(data, where);
		return null;
	}
	
	return null;
}
}


