/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.AnnotPanel; 

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import java.awt.image.*;


/** ======================================================================== */
public class AnnotPanel extends DataWindowAssistantID implements CellColorer, CellColorerCharacters, CellColorerTaxa, CellColorerMatrix, AnnotPanelOwner {
	CharacterData data;
	MesquiteTable table;
	boolean defaultNewEachCell = false;
	boolean defaultAutoAttach = false;
	AnnotationsPanel panel = null;
	String findString = null;
	int findNumber = 0;
	int currentColumn = -1;
	int currentRow = -1;
	int currentNoteNumber = -1;
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
		addItemToSubmenu(null, annotMenu, "Move Footnotes to ANNOTATIONs", makeCommand("moveFootnotes",  this));
		addItemToSubmenu(null, annotMenu, "Copy Footnotes to ANNOTATIONs", makeCommand("copyFootnotes",  this));
		addItemToSubmenu(null, annotMenu, "Copy ANNOTATIONs to Footnotes", makeCommand("copyAnnotations",  this));

		return true;
	}
	public boolean setActiveColors(boolean active){
		setActive(true);
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		setPanel();
	}
	public MesquiteModule getModule(){
		return this;
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
				if (table.singleTableCellSelected()){
					Dimension d = table.getFirstTableCellSelected();
					focusInCell(d.width, d.height);
				}
				else 
					focusInCell(-2,-2);

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
	void showNote(AttachedNotesVector aim, int column, int row, int noteNumber){
		if (column<-1 || row < -1 || data == null || panel == null || column>= data.getNumChars() || row >= data.getNumTaxa()){
			if (panel != null){
				if (panel.isAttachable()){
					panel.setNotes(null, "", "", column, row, noteNumber);
					panel.setAttachable(false);
				}
			}
			return;
		}
		if (panel != null)
			panel.setAttachable(true);
		if (currentColumn == column && currentRow == row && noteNumber == currentNoteNumber)
			return;
		currentColumn  = column;
		currentRow = row;
		String s = null;
		String loc = null;
		if (column >= 0 || row >= 0){
			if (column<0){
				s = "taxon \"" + data.getTaxa().getTaxonName(row) + "\"";
				loc = "t." + Integer.toString(row+1);
			}
			else if (row<0){
				if (data.characterHasName(column))
					s = "character \"" + data.getCharacterName(column) + "\"";
				else
					s = data.getCharacterName(column);
				loc =  "c." + Integer.toString(column+1);
			}
			else {
				if (data.characterHasName(column))
					s = "character \"" + data.getCharacterName(column) + "\"";
				else
					s = data.getCharacterName(column);
				s += " in taxon \"" + data.getTaxa().getTaxonName(row) + "\"";
				loc = "t." + Integer.toString(row+1) + "/c." + Integer.toString(column+1);
			}
		}
		if (!panel.isVisible()){
			showPanel.setValue(true);
			setPanel();
		}
		panel.setNotes(aim, s, loc, column, row, noteNumber);
		currentNoteNumber = noteNumber;
		/*
		if (!table.anythingSelected() || table.singleTableCellSelected()){
			table.deselectAll();
			table.selectCell(column, row);
			table.setFocusedCell(column, row);

			MesquiteWindow w = MesquiteWindow.windowOfItem(table);
			if (w !=null)
				w.setExplanation("Annotation displayed in separate window for " + s);
		}
		 */

	}
	void showNote(int column, int row, int noteNumber){
		if (column<-1 || row < -1 || data == null || panel == null || column>= data.getNumChars() || row >= data.getNumTaxa()){
			if (panel != null){
				if (panel.isAttachable()){
					panel.setNotes(null, "", "", column, row, noteNumber);
					panel.setAttachable(false);
				}
			}
			return;
		}
		if (panel != null)
			panel.setAttachable(true);
		AttachedNotesVector hL = null;
		if (column<0)  //taxon
			hL = (AttachedNotesVector)data.getTaxa().getAssociatedObject(notesNameRef, row);
		else if (row < 0) //character
			hL = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, column);
		else 
			hL = (AttachedNotesVector)data.getCellObject(notesNameRef, column, row);
		showNote(hL, column, row, noteNumber);
	}

	boolean findAndShowNote(String searchString, int i){
		Taxa taxa = data.getTaxa();

		int count = 0;
		//search taxa
		for (int it = 0; it<taxa.getNumTaxa(); it++) {
			AttachedNotesVector hL = (AttachedNotesVector)taxa.getAssociatedObject(notesNameRef, it);
			if (hL != null){
				for (int noteNum = 0; noteNum<hL.getNumNotes(); noteNum++){
					AttachedNote note = hL.getAttachedNote(noteNum);
					if (note.containsString(searchString)){
						if (count == i) {
							showNote(-1, it, noteNum);
							return true;
						}
						count++;
					}
				}
			}
		}
		//search characters
		for (int ic = 0; ic<data.getNumChars(); ic++) {
			AttachedNotesVector hL = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, ic);
			if (hL != null){
				for (int noteNum = 0; noteNum<hL.getNumNotes(); noteNum++){
					AttachedNote note = hL.getAttachedNote(noteNum);
					if (note.containsString(searchString)){
						if (count == i) {
							showNote(ic, -1, noteNum);
							return true;
						}
						count++;
					}
				}
			}
		}
		//search cells
		for (int ic = 0; ic<data.getNumChars(); ic++) {
			for (int it = 0; it<taxa.getNumTaxa(); it++) {

				AttachedNotesVector hL = (AttachedNotesVector)data.getCellObject(notesNameRef, ic, it);
				if (hL != null){
					for (int noteNum = 0; noteNum<hL.getNumNotes(); noteNum++){
						AttachedNote note = hL.getAttachedNote(noteNum);
						if (note.containsString(searchString)){
							if (count == i) {
								showNote(ic, it, noteNum);
								return true;
							}
							count++;
						}
					}
				}
			}
		}
		MesquiteMessage.println("There are no more occurrences of \"" + searchString + "\"");
		MesquiteMessage.beep();
		return false;

	}

	void transferFootnotes(boolean move){
		Taxa taxa = data.getTaxa();
		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		for (int it = 0; it<numTaxa; it++) {  //taxa
			String s = taxa.getAnnotation(it);
			if (s != null){
				CommandRecord.tick("Moving footnote for taxon " + (it+1));
				AttachedNotesVector anv = (AttachedNotesVector)taxa.getAssociatedObject(notesNameRef, it);
				if (anv == null) {
					anv = new AttachedNotesVector(taxa);
					taxa.setAssociatedObject(notesNameRef, it, anv);
				}
				AttachedNote hL = new AttachedNote();
				hL.setComment(new String(s), false);
				anv.addNoteFirst(hL, false);
				if (move)
					taxa.setAnnotation(it, null);
			}
		}

		for (int ic = 0; ic<numChars; ic++) { //characters
			String s = data.getAnnotation(ic);
			if (s != null){
				CommandRecord.tick("Moving footnote for character " + (ic+1));
				AttachedNotesVector anv = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, ic);
				if (anv == null) {
					anv = new AttachedNotesVector(data);
					data.setAssociatedObject(notesNameRef, ic, anv);
				}

				AttachedNote hL = new AttachedNote();
				hL.setComment(new String(s), false);
				anv.addNoteFirst(hL, false);
				if (move)
					data.setAnnotation(ic, null);
			}
		}
		for (int ic = 0; ic<numChars; ic++) { //characters
			CommandRecord.tick("Moving footnotes for character " + (ic+1));
			for (int it = 0; it<numTaxa; it++) {  //taxa
				String s = data.getAnnotation(ic, it);
				if (s != null){
					AttachedNotesVector anv = (AttachedNotesVector)data.getCellObject(notesNameRef, ic, it);
					if (anv == null) {
						anv = new AttachedNotesVector(data);
						data.setCellObject(notesNameRef, ic, it, anv);
						data.setCellObjectDisplay(ic, it);
					}
					AttachedNote hL = new AttachedNote();
					hL.setComment(new String(s), false);
					anv.addNoteFirst(hL, false);
					if (move)
						data.setAnnotation(ic, it, null);
				}
			}
		}
		taxa.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_ADDED));
		data.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_ADDED));

	}
	/*.................................................................................................................*/
	public void transferAnnotations(){
		Taxa taxa = data.getTaxa();
		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		for (int it = 0; it<numTaxa; it++) {  //taxa
			AttachedNotesVector anv = (AttachedNotesVector)taxa.getAssociatedObject(notesNameRef, it);
			if (anv != null && anv.getNumNotes()>0) {
				CommandRecord.tick("Moving footnotes for taxon " + (it+1));
				AttachedNote note = anv.getAttachedNote(0);
				if (note.getComment()!=null)
					taxa.setAnnotation(it, note.getComment());
			}

		}

		for (int ic = 0; ic<numChars; ic++) { //characters
			AttachedNotesVector anv = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, ic);
			if (anv != null && anv.getNumNotes()>0) {
				CommandRecord.tick("Moving annotation for character " + (ic+1));
				AttachedNote note = anv.getAttachedNote(0);
				if (note.getComment()!=null)
					data.setAnnotation(ic, note.getComment());
			}
		}
		for (int ic = 0; ic<numChars; ic++) { //characters
			CommandRecord.tick("Moving annotations for character " + (ic+1));
			for (int it = 0; it<numTaxa; it++) {  //taxa
				AttachedNotesVector anv = (AttachedNotesVector)data.getCellObject(notesNameRef, ic, it);
				if (anv != null && anv.getNumNotes()>0) {
					AttachedNote note = anv.getAttachedNote(0);
					if (note.getComment()!=null)
						data.setAnnotation(ic, it, note.getComment());
				}
			}
		}
		taxa.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_ADDED));
		data.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_ADDED));

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
		else if (checker.compare(this.getClass(),  "Moves single footnotes to multiple note ANNOTATIONs system", null, commandName, "moveFootnotes")) {
			if (!AlertDialog.query(containerOfModule(), "Move Notes?", "Are you sure you want to move all of the footnotes into the new multiple note ANNOTATIONs system?  The old footnotes will be deleted.  You can copy the notes back into the old footnotes system by selecting \"Copy ANNOTATIONs to Footnotes\"", "OK", "Cancel", -1))
				return null;
			transferFootnotes(true);
		}
		else if (checker.compare(this.getClass(),  "Copies single footnotes to multiple note ANNOTATIONs system", null, commandName, "copyFootnotes")) {
			if (!AlertDialog.query(containerOfModule(), "Copy Notes?", "Are you sure you want to copy all of the footnotes into the new multiple note ANNOTATIONs system?", "OK", "Cancel", -1))
				return null;
			transferFootnotes(false);
		}
		else if (checker.compare(this.getClass(),  "Copies ANNOTATIONs into old footnotes system", null, commandName, "copyAnnotations")) {
			if (!AlertDialog.query(containerOfModule(), "Copy Annotations?", "Are you sure you want to copy the \"Comments\" field of the first note of each cell of the matrix into the old footnotes system?  If there is a comment, then any existing footnote for that cell will be replaced by the comment.", "OK", "Cancel", -1))
				return null;
			transferAnnotations();
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
			if (!findAndShowNote(findString, findNumber))
				findNumber = 0;
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
		int column = currentColumn;
		int row = currentRow;
		AttachedNotesVector aiv = null;
		if (column<0) {
			//taxon
			aiv = (AttachedNotesVector)data.getTaxa().getAssociatedObject(notesNameRef, row);
			if (aiv == null)
				aiv = new AttachedNotesVector(data.getTaxa());
			data.getTaxa().setAssociatedObject(notesNameRef, row, aiv);
		}
		else if (row < 0){
			//character
			aiv = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, column);
			if (aiv == null)
				aiv = new AttachedNotesVector(data);
			data.setAssociatedObject(notesNameRef, column, aiv);
		}
		else {
			aiv = (AttachedNotesVector)data.getCellObject(notesNameRef, column, row);
			if (aiv == null)
				aiv = new AttachedNotesVector(data);
			data.setCellObject(notesNameRef, column, row, aiv);
			data.setCellObjectDisplay(column, row);
		}
		return aiv;
	}
	public void chooseAndAttachImage(AttachedNote hL, boolean local){
		if (hL == null)
			return;
		//attach image to window w

		hL.attachImage(this, local);
		showNote(currentColumn, currentRow, currentNoteNumber);
		if (table != null)
			table.repaintAll();
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	ColorRecord[] legend;
	public ColorRecord[] getLegendColors(){
		if (legend == null) {
			legend = new ColorRecord[2];
			legend[0] = new ColorRecord(Color.white, "No Annotations");
			legend[1] = new ColorRecord(ColorDistribution.veryLightGreen, "Annotations attached");
		}
		return legend;
	}
	public String getColorsExplanation(){
		return null;
	}
	public Color getCellColor(int ic, int it){
		if (data == null)
			return null;
		else if (ic== -1 && data.getTaxa().getAssociatedObject(notesNameRef, it)!=null){ //taxon
			return ColorDistribution.veryLightGreen;
		}
		else if (it == -1 && data.getAssociatedObject(notesNameRef, ic) !=null){ //character
			return ColorDistribution.veryLightGreen;
		}
		else if (data.getCellObject(notesNameRef, ic, it) !=null)
			return ColorDistribution.veryLightGreen;
		else {
			return null;
		}

	}
	public String getCellString(int ic, int it){
		if (data == null || !isActive())
			return null;
		else if (ic== -1 && data.getTaxa().getAssociatedObject(notesNameRef, it)!=null){ //taxon
			return "Cell has annotation(s) attached";
		}
		else if (it == -1 && data.getAssociatedObject(notesNameRef, ic) !=null){ //character
			return "Cell has annotation(s) attached";
		}
		else if (data.getCellObject(notesNameRef, ic, it) !=null)
			return "Cell has annotation(s) attached";
		else {
			return "Cell lacks annotations";
		}

	}
	int count = 0;
	public void focusInCell(int ic, int it){
		if (data == null)
			return;
		if (panel == null || panel.isVisible())
			showNote(ic, it, 0);
		return;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Annotation Attached";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Annotation Panel module";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides tools with which to attach notes (including images) to cells of the matrix and show them.";
	}
}

/* ======================================================================== */


