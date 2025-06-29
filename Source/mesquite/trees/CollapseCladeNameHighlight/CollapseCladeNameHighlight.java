/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.CollapseCladeNameHighlight;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.DrawTreeCoordinator;
import mesquite.lib.duties.TreeDrawAssistantI;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.ui.MesquiteMenuSpec;
import mesquite.lib.ui.MesquiteSubmenuSpec;

/* ======================================================================== */
public class CollapseCladeNameHighlight extends TreeDrawAssistantI {
	MesquiteBoolean collapsedBold = new MesquiteBoolean(true);
	MesquiteBoolean collapsedItalics = new MesquiteBoolean(false);
	MesquiteBoolean collapsedUnderline = new MesquiteBoolean(true);
	MesquiteBoolean collapsedBig = new MesquiteBoolean(true);
	DrawTreeCoordinator drawCoordinatorTask = null;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();
		MesquiteMenuSpec textMenu = findMenuAmongEmployers("Text");
		MesquiteSubmenuSpec collapsedCladeSubmenu = addSubmenu(textMenu, "Highlight for Collapsed Clades");
		addCheckMenuItemToSubmenu(textMenu, collapsedCladeSubmenu, "Bold", new MesquiteCommand("toggleBoldCollapsed", this), collapsedBold);
		addCheckMenuItemToSubmenu(textMenu, collapsedCladeSubmenu, "Italics", new MesquiteCommand("toggleItalicsCollapsed", this), collapsedItalics);
		addCheckMenuItemToSubmenu(textMenu, collapsedCladeSubmenu, "Underline", new MesquiteCommand("toggleUnderlineCollapsed", this), collapsedUnderline);
		addCheckMenuItemToSubmenu(textMenu, collapsedCladeSubmenu, "Big", new MesquiteCommand("toggleBigCollapsed", this), collapsedBig);
		addItemToSubmenu(textMenu, collapsedCladeSubmenu, "-", null);
		addItemToSubmenu(textMenu, collapsedCladeSubmenu, "Save as Defaults", new MesquiteCommand("saveAsDefaults", this));
		drawCoordinatorTask = (DrawTreeCoordinator)findEmployerWithDuty(DrawTreeCoordinator.class);
		return true;
	} 
	
	int getCollapsedMode(){
		int cM = 0;
		if (collapsedBold.getValue())
			cM += TreeDisplay.cCHM_BOLD;
		if (collapsedItalics.getValue())
			cM += TreeDisplay.cCHM_ITALICS;
		if (collapsedBig.getValue())
			cM += TreeDisplay.cCHM_BIG;
		return cM;
	}
	void setCollapsedMode(int i){
		if (!MesquiteInteger.isCombinable(i))
			return;
		collapsedBold.setValue((TreeDisplay.cCHM_BOLD & i) !=0);
		collapsedItalics.setValue((TreeDisplay.cCHM_ITALICS & i) !=0);
		collapsedBig.setValue((TreeDisplay.cCHM_BIG & i) !=0);
		
	}

 	public void treeDisplayCreated(TreeDisplay treeDisplay){
		treeDisplay.collapsedCladeHighlightMode = getCollapsedMode();
		treeDisplay.collapsedCladeUnderline = collapsedUnderline.getValue();
 	}
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "mode", getCollapsedMode());   
		StringUtil.appendXMLTag(buffer, 2, "underline", collapsedUnderline.getValue());   
		return buffer.toString();
	}

	public void processSingleXMLPreference (String tag, String content) {
		if ("mode".equalsIgnoreCase(tag))
			setCollapsedMode(MesquiteInteger.fromString(content));
		if ("underline".equalsIgnoreCase(tag))
			collapsedUnderline.setValue(MesquiteBoolean.fromTrueFalseString(content));
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		
		temp.addLine("setMode " + getCollapsedMode());
		return temp;
	}	
	
	void setUnderlined(boolean underline){
		for (int i = 0; i<drawCoordinatorTask.getNumTreeDisplays(); i++){
			TreeDisplay tD = drawCoordinatorTask.getTreeDisplay(i);
			tD.collapsedCladeUnderline = underline;			
		}
	}
	void setBit(int bit, MesquiteBoolean b){
		for (int i = 0; i<drawCoordinatorTask.getNumTreeDisplays(); i++){
			TreeDisplay tD = drawCoordinatorTask.getTreeDisplay(i);
			if (b.getValue())
				tD.collapsedCladeHighlightMode = tD.collapsedCladeHighlightMode | bit;
			else
				tD.collapsedCladeHighlightMode = tD.collapsedCladeHighlightMode & (~bit  & 7);				
		}
	}
	void addBit(int bit){
		for (int i = 0; i<drawCoordinatorTask.getNumTreeDisplays(); i++){
			TreeDisplay tD = drawCoordinatorTask.getTreeDisplay(i);
			tD.collapsedCladeHighlightMode = tD.collapsedCladeHighlightMode | bit;
		}
	}
	void clearBit(int bit){
		for (int i = 0; i<drawCoordinatorTask.getNumTreeDisplays(); i++){
			TreeDisplay tD = drawCoordinatorTask.getTreeDisplay(i);
			tD.collapsedCladeHighlightMode = tD.collapsedCladeHighlightMode & (~bit  & 7);
		}
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets collapsed mode", "[integer]", commandName, "setMode")) {
			int mode = MesquiteInteger.fromString(arguments);
			setCollapsedMode(mode);
			setBit( TreeDisplay.cCHM_BOLD, collapsedBold);
			setBit( TreeDisplay.cCHM_ITALICS, collapsedItalics);
			setBit( TreeDisplay.cCHM_BIG, collapsedBig);
		}
		else if (checker.compare(this.getClass(), "Whether collapsed clades are bold", "[on or off]", commandName, "toggleBoldCollapsed")) {
			boolean was = collapsedBold.getValue();
			collapsedBold.toggleValue(arguments);
			if (was != collapsedBold.getValue()){
					setBit( TreeDisplay.cCHM_BOLD, collapsedBold);
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Whether collapsed clades are bold", "[on or off]", commandName, "toggleItalicsCollapsed")) {
			boolean was = collapsedItalics.getValue();
			collapsedItalics.toggleValue(arguments);
			if (was != collapsedItalics.getValue()){
				setBit( TreeDisplay.cCHM_ITALICS, collapsedItalics);
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Whether collapsed clade names are big", "[on or off]", commandName, "toggleBigCollapsed")) {
			boolean was = collapsedBig.getValue();
			collapsedBig.toggleValue(arguments);
			if (was != collapsedBig.getValue()){
				setBit( TreeDisplay.cCHM_BIG, collapsedBig);
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Whether collapsed clades are underlined", "[on or off]", commandName, "toggleUnderlineCollapsed")) {
			boolean was = collapsedUnderline.getValue();
			collapsedUnderline.toggleValue(arguments);
			if (was != collapsedUnderline.getValue()){
					setUnderlined(collapsedUnderline.getValue());
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Save current as defaults", "[]", commandName, "saveAsDefaults")) {
			storePreferences();
		}
			else
				return  super.doCommand(commandName, arguments, checker);
			return null;
		}	
 		 
 		 /*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "Control Collapsed Clade Name Highlight";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Controls appearance of names of collapsed clades.";
   	 }
}



