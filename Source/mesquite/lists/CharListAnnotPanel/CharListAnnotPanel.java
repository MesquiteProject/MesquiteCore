/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharListAnnotPanel;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharListAnnotPanel extends CharListAssistantI implements AnnotPanelOwner{
	/*.................................................................................................................*/
	public String getName() {
		return "Annotation Panel module (list of characters)";
	}
	public String getExplanation() {
		return "Provides tools with which to attach notes (including images) to characters and show them.";
	}
	/*.................................................................................................................*/
	CharacterData data;
	MesquiteTable table;
	AnnotationsPanel panel = null;
	String findString = null;
	int findNumber = 0;
	int currentColumn, currentRow, currentNoteNumber;
	MesquiteSubmenuSpec annotMenu;
	MesquiteBoolean showPanel;
	MesquiteButton annotButton;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		showPanel = new MesquiteBoolean(false);
		annotButton = new MesquiteButton(this, makeCommand("togglePanel",  this), null, true, MesquiteModule.getRootImageDirectoryPath()  + "annot.gif", 12, 16);
		annotButton.setShowBackground(false);
		annotButton.setButtonExplanation("Show/Hide Annotations Panel");
		addCheckMenuItem(null, "Show Annotations Panel", makeCommand("togglePanel", this), showPanel);

		annotMenu = addSubmenu(null, "Annotations");
		MesquiteMenuItemSpec mmi = addItemToSubmenu(null, annotMenu, "Find Annotation", MesquiteModule.makeCommand("searchAnnotations", this));
		mmi.setShortcut(KeyEvent.VK_3);
		mmi = addItemToSubmenu(null, annotMenu, "Find Again", MesquiteModule.makeCommand("searchAgain", this));
		mmi.setShortcut(KeyEvent.VK_8);

		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public MesquiteModule getModule(){
		return this;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		setPanel();
	}
	/*.................................................................................................................*/
	void setPanel(){
		MesquiteWindow f = containerOfModule();
		if (f instanceof TableWindow){
			((TableWindow)f).addControlButton(annotButton);
			if (showPanel.getValue()){
				if (panel == null) {
					panel = new AnnotationsPanel(this);
					addItemToSubmenu(null, annotMenu, "-", null);
					MesquiteMenuItemSpec mmi = addItemToSubmenu(null, annotMenu, "Make Item Label", MesquiteModule.makeCommand("makeItemLabel", panel));
					mmi.setShortcut(KeyEvent.VK_L);
					addItemToSubmenu(null, annotMenu, "Recover Offscreen Labels", MesquiteModule.makeCommand("recoverLostLabels", panel));
					resetContainingMenuBar();
				}
				((TableWindow)f).addSidePanel(panel, 300);
				panel.setVisible(true);

			}
			else {
				if (panel != null)
					((TableWindow)f).removeSidePanel(panel);
			}

		}
	}
	public boolean hasDisplayModifications(){
		return false;
	}
	public void panelGoAway(Panel p){
		showPanel.setValue(false);
		setPanel();
	}
	/*.................................................................................................................*/
	void showNote(AttachedNotesVector aim, int row, int noteNumber){
		if (row<-1 || data == null || panel == null || row>= data.getNumChars())
			return;
		currentRow = row;
		String s = null;
		String loc = null;
		if (row >= 0){
			if (data.characterHasName(row))
				s = "character \"" + data.getCharacterName(row) + "\"";
			else
				s = data.getCharacterName(row);
			loc = Integer.toString(row+1);
		}
		if (!panel.isVisible()){
			showPanel.setValue(true);
			setPanel();
		}
		panel.setNotes(aim, s, loc, -1, row, noteNumber);
	}
	void showNote(int row, int noteNumber){
		if ( row < -1 || data == null || panel == null || row>= data.getNumChars())
			return;
		AttachedNotesVector hL = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, row);
		showNote(hL, row, noteNumber);
	}

	boolean findAndShowNote(String searchString, int i){
		int count = 0;
		//search characters
		for (int ic = 0; ic<data.getNumChars(); ic++) {
			AttachedNotesVector hL = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, ic);
			if (hL != null){
				for (int noteNum = 0; noteNum<hL.getNumNotes(); noteNum++){
					AttachedNote note = hL.getAttachedNote(noteNum);
					if (note.containsString(searchString)){
						if (count == i) {
							showNote(ic,  noteNum);
							return true;
						}
						count++;
					}
				}
			}
		}
		MesquiteMessage.println("There are no more occurrences of \"" + searchString + "\"");
		MesquiteMessage.beep();
		return false;

	}

	/*.................................................................................................................*/
	public void endJob() {
		//removePanel;
		super.endJob();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("togglePanel " + showPanel.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	NameReference notesNameRef = NameReference.getNameReference("notes");
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not the annotations panel is shown", "[on = shown; off]", commandName, "togglePanel")) {
			showPanel.toggleValue(parser.getFirstToken(arguments));
			setPanel();
		}
		else if (checker.compare(this.getClass(),  "Searches for text within notes", null, commandName, "searchAnnotations")) {
			if (MesquiteThread.isScripting()) //todo: should support argument passed
				return null;
			String temp = MesquiteString.queryString(containerOfModule(), "Search annotations", "Search annotations of cells of matrix to find the following string.  Comments, references and labels of images will be searched.", findString, 2);
			if (StringUtil.blank(temp)) {
				return null;
			}
			findString = temp;
			findNumber = 0;
			if (!findAndShowNote(findString, findNumber))				findNumber = 0;
			else
				findNumber = 1;

		}
		else if (checker.compare(this.getClass(),  "Searches for text within notes", null, commandName, "searchAgain")) {
			if (MesquiteThread.isScripting()) //todo: should support argument passed
				return null;
			if (StringUtil.blank(findString))
				return null;
			if (!findAndShowNote(findString, findNumber))				findNumber = 0;
			else
				findNumber++;

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public AttachedNotesVector makeNotesVector(AnnotationsPanel w){
		//attach image to window w
		int row = currentRow;
		AttachedNotesVector aiv  = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, row);
		if (aiv == null)
			aiv = new AttachedNotesVector(data);
		data.setAssociatedObject(notesNameRef, row, aiv);
		return aiv;
	}
	public void chooseAndAttachImage(AttachedNote hL, boolean local){
		if (hL == null)
			return;
		//attach image to window w

		hL.attachImage(this, local);
		showNote( currentRow, currentNoteNumber);
		if (table != null)
			table.repaintAll();
	}
	/*.................................................................................................................*/
	public void focusInRow(int row){
		if (data == null)
			return;
		if (panel == null || panel.isVisible())
			showNote(row, 0);
		return;
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}

}
