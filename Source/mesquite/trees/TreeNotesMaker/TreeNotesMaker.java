/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.TreeNotesMaker;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */

public class TreeNotesMaker extends TreeDisplayAssistantD  {
	Vector notes;
	
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
    	 	notes = new Vector();
    	 	setUseMenubar(false);
   		addMenuItem("Close Note", makeCommand("closeNote", this));
		addSubmenu(null, "Font", makeCommand("setFont", this), MesquiteSubmenu.getFontList());
		addSubmenu(null, "Font Size", makeCommand("setFontSize", this), MesquiteSubmenu.getFontSizeList());
    	 	return true;
 	}
 	
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		TreeNotesOperator newNote = new TreeNotesOperator(this, treeDisplay);
		notes.addElement(newNote);
		return newNote;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	public boolean isSubstantive(){
   		return false;  
   	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	if (notes.size()>0){
	  	 	TreeNotesOperator tno = (TreeNotesOperator)notes.elementAt(0);
	  	 	TreeNote tn = tno.getNote();
	  	 	temp.addLine("getNote");
	  	 	temp.addLine("tell It");
	  	 	temp.incorporate(tn.getSnapshot(file), true);
	  	 	temp.addLine("endTell");
			
		}
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Turns off this note", null, commandName, "closeNote")) {
    	 		closeAllNotes();
    	 		iQuit();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the note object", null, commandName, "getNote")) {
  	 		if (notes.size()>0){
		  	 	TreeNotesOperator tno = (TreeNotesOperator)notes.elementAt(0);
		  	 	return tno.getNote();
  	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the font", null, commandName, "setFont")) {
			Enumeration e = notes.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeNotesOperator) {
					TreeNotesOperator tCO = (TreeNotesOperator)obj;
		 			tCO.treeNote.doCommand("setFont", arguments, checker);
		 		}
			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the font size", null, commandName, "setFontSize")) {
			Enumeration e = notes.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeNotesOperator) {
					TreeNotesOperator tCO = (TreeNotesOperator)obj;
		 			tCO.treeNote.doCommand("setFontSize", arguments, checker);
		 		}
			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	
	/*.................................................................................................................*/
 	public void closeAllNotes() {
		Enumeration e = notes.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TreeNotesOperator) {
				TreeNotesOperator tCO = (TreeNotesOperator)obj;
	 			tCO.turnOff();
	 		}
		}
	}
	/*.................................................................................................................*/
 	public void endJob() {
    	 	closeAllNotes();
 		super.endJob();
		resetContainingMenuBar();
 	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Add Note On Tree";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Draws editable notes in a tree display.";
   	 }
   	 
}
/* ======================================================================== */
class TreeNotesOperator extends TreeDisplayDrawnExtra {
	Tree myTree;
	TreeNotesMaker notesModule;
	TreeNote treeNote;
	
	
	public TreeNotesOperator (TreeNotesMaker ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		this.notesModule=ownerModule;
		treeNote = new TreeNote(notesModule, this);
		treeNote.setVisible(false);
		addPanelPlease(treeNote);
	}
	public TreeNote getNote(){
		return treeNote;
	}
	/*.................................................................................................................*/
	public   void placeNote(Tree tree, int drawnRoot) {
		int noteX = (int) treeDisplay.getTreeDrawing().x[drawnRoot]+treeNote.getOffsetX();  //integer nodeloc approximation
		int noteY = (int) treeDisplay.getTreeDrawing().y[drawnRoot]+treeNote.getOffsetY(); //integer nodeloc approximation
		
		if (noteX>treeDisplay.getBounds().width) {
			treeNote.setOffsetX((int)(treeDisplay.getBounds().width-treeDisplay.getTreeDrawing().x[drawnRoot]-40));
			noteX = (int) treeDisplay.getTreeDrawing().x[drawnRoot]+treeNote.getOffsetX();
		}
		else if (noteX<0) {
			treeNote.setOffsetX((int)(-treeDisplay.getTreeDrawing().x[drawnRoot]+4));
			noteX = (int) treeDisplay.getTreeDrawing().x[drawnRoot]+treeNote.getOffsetX();
		}
			
		if (noteY>treeDisplay.getBounds().height) {
			treeNote.setOffsetY((int)(treeDisplay.getBounds().height-treeDisplay.getTreeDrawing().y[drawnRoot]-40));
			noteY = (int) treeDisplay.getTreeDrawing().y[drawnRoot]+treeNote.getOffsetY();
		}
		else if (noteY<0) {
			treeNote.setOffsetY((int)(-treeDisplay.getTreeDrawing().y[drawnRoot]+4));
			noteY =(int)  treeDisplay.getTreeDrawing().y[drawnRoot]+treeNote.getOffsetY();
		}
		if ((noteX!=treeNote.getBounds().x) || (noteY!=treeNote.getBounds().y))
			treeNote.setLocation(noteX, noteY);
			
		if (!treeNote.isVisible()) {
			treeNote.setVisible(true);
		}
	}
	/*.................................................................................................................*
	old, tree root based position
	public   void placeNote(Tree tree, int drawnRoot) {
		int noteX = treeDisplay.getTreeDrawing().x[drawnRoot]+treeNote.getOffsetX();
		int noteY = treeDisplay.getTreeDrawing().y[drawnRoot]+treeNote.getOffsetY();
		
		if (noteX>treeDisplay.getBounds().width) {
			treeNote.setOffsetX(treeDisplay.getBounds().width-treeDisplay.getTreeDrawing().x[drawnRoot]-40);
			noteX = treeDisplay.getTreeDrawing().x[drawnRoot]+treeNote.getOffsetX();
		}
		else if (noteX<0) {
			treeNote.setOffsetX(-treeDisplay.getTreeDrawing().x[drawnRoot]+4);
			noteX = treeDisplay.getTreeDrawing().x[drawnRoot]+treeNote.getOffsetX();
		}
			
		if (noteY>treeDisplay.getBounds().height) {
			treeNote.setOffsetY(treeDisplay.getBounds().height-treeDisplay.getTreeDrawing().y[drawnRoot]-40);
			noteY = treeDisplay.getTreeDrawing().y[drawnRoot]+treeNote.getOffsetY();
		}
		else if (noteY<0) {
			treeNote.setOffsetY(-treeDisplay.getTreeDrawing().y[drawnRoot]+4);
			noteY = treeDisplay.getTreeDrawing().y[drawnRoot]+treeNote.getOffsetY();
		}
		if ((noteX!=treeNote.getBounds().x) || (noteY!=treeNote.getBounds().y))
			treeNote.setLocation(noteX, noteY);
			
		if (!treeNote.isVisible()) {
			treeNote.setVisible(true);
		}
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (!treeNote.isVisible()) {
			treeNote.setVisible(true);
		}
		//placeNote(tree, drawnRoot);
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (!treeNote.isVisible()) {
			treeNote.setVisible(true);
		}
		//placeNote(tree, drawnRoot);
		//treeNote.print(g);
	}
	
	/*.................................................................................................................*/
	public void turnOff() {
		if (treeNote!=null && treeDisplay!=null)
			removePanelPlease(treeNote);
		super.turnOff();
	}
}

/* ======================================================================== */
class TreeNote extends ResizableLegend {
	TreeNotesMaker ownerModule;
	TreeNotesOperator notesOperator;
	int noteWidth=100;
	int lineHeight=16;
	int noteHeight=20;
	//boolean dragging = false;
	//boolean sizing = false;
	//int offsetX = 20;
	//int offsetY=0;
	int topEdge = 6;
	int cornerEdge = 4;
	//int origTouchX, origTouchY, dragOffsetX, dragOffsetY, sizeOffsetX, sizeOffsetY;
	boolean invalid;
	StringInABox textBox;
	StringBuffer sb;
	
	boolean checkHeight = true;
	static Image dropDownArrow=null;
	Font font=null;
	
	static {
		dropDownArrow = Toolkit.getDefaultToolkit().getImage(MesquiteModule.getRootPath() +"images/menuDropArrow.gif");
	}

	public TreeNote(TreeNotesMaker ownerModule, TreeNotesOperator notesOperator) {
		super(100, 20);
		this.ownerModule = ownerModule;
		this.notesOperator = notesOperator;
		//setBackground(getParent().getBackground());
		setLayout(null);
		sb = new StringBuffer("");
		font = getFont();
		textBox = new StringInABox(sb, getFont(), noteWidth);
		checkHeight = true;
		if (dropDownArrow==null)
			dropDownArrow = Toolkit.getDefaultToolkit().getImage(MesquiteModule.getRootPath() +"images/menuDropArrow.gif");
		setSize(noteWidth, noteHeight);
	}
	
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.incorporate(super.getSnapshot(file), false);
  	 	temp.addLine("setNote " + StringUtil.tokenize(textBox.getString()));
  	 	temp.addLine("setOffsetX " + offsetX);
  	 	temp.addLine("setOffsetY " + offsetY);
  	 	return temp;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the text of the note", "[text]", commandName, "setNote")) {
    	 		setText(ParseUtil.getFirstToken(arguments, pos));
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the initial horizontal offset of the note from home position", "[offset in pixels]", commandName, "setOffsetX")) {
    	 		MesquiteInteger pos = new MesquiteInteger(0);
    	 		int ox = MesquiteInteger.fromString(arguments, pos);
    	 		if (MesquiteInteger.isCombinable(ox))
    	 			setOffsetX(ox);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the initial vertical offset of the note from home position", "[offset in pixels]", commandName, "setOffsetY")) {
    	 		MesquiteInteger pos = new MesquiteInteger(0);
    	 		int oy = MesquiteInteger.fromString(arguments, pos);
    	 		if (MesquiteInteger.isCombinable(oy))
    	 			setOffsetY(oy);
    	 	}
    	 	else 
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	public void invalidate(){
		invalid = true;
	}
	
	
	
	public void setText(String s) {
		sb.setLength(0);
		sb.append(s);
		textBox.setString(sb);
		checkMinimumHeight();
		repaint();
	}
	
	
	public void setBounds(int x, int y, int width, int height) {
		noteWidth = width;
		noteHeight = height;
		textBox.setWidth(noteWidth);
		int gH =textBox.getHeight();
		if (height<gH) {
			noteHeight = gH;
		}
		super.setBounds(x, y, noteWidth, noteHeight);
	}
	public void setSize(int width, int height) {
		noteWidth = width;
		noteHeight = height;
		textBox.setWidth(noteWidth);
		int gH =textBox.getHeight();
		if (height<gH) {
			noteHeight = gH;
		}
		super.setSize(noteWidth, noteHeight);
	}
	public void resetHeight(int height) {
		noteHeight = height;
		super.setSize(noteWidth, height);
	}
	void checkMinimumHeight() {
		int gH =textBox.getHeight();
		if (getBounds().height<gH)
			resetHeight(gH);
	}
	public void setPanelFont(Font f) {
		super.setPanelFont(f);
		setFont(f);
	}
	
	public void setFont(Font f) {
		font = f;
		textBox.setFont(f);
		checkMinimumHeight();
		super.setFont(f);
	}
	
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		invalid = false;
		if (g instanceof PrintGraphics) {
			textBox.draw(g,0,0);
		}
		else {
			if (font==null) {
				font = getFont();
				textBox.setFont(font);
			}
			g.setColor(Color.cyan);
			g.setClip(0,0,noteWidth, topEdge);
			g.fillRoundRect(0, 0, noteWidth-1, noteHeight, 8, 8);
			g.setColor(Color.blue);
			g.drawRoundRect(0, 0, noteWidth-1, noteHeight, 8, 8);
			g.setClip(0,0,noteWidth, noteHeight);
			g.drawLine(0, topEdge, noteWidth-1, topEdge);
			g.drawImage(dropDownArrow, noteWidth - 16, 0, this);
			g.setColor(Color.cyan);
			g.drawRect(0, topEdge, noteWidth-1, noteHeight-1-topEdge);
			g.fillRect(noteWidth - 8, noteHeight - cornerEdge, 8, cornerEdge);
			g.fillRect(noteWidth - cornerEdge, noteHeight - 8, cornerEdge, 8);
			g.setColor(Color.black);
			
			textBox.draw(g,0,0);
			
			if (invalid)
				repaint();
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (y<=topEdge) {
			if (x>= noteWidth - 16) {
				if (MesquiteEvent.controlKeyDown(modifiers))
						panelTouched(modifiers, x,y, false);
				else
					ownerModule.showPopUp(this, x+8, y+8);
			}
			else {
				super.mouseDown(modifiers, clickCount, when, x, y, tool);
			}
		}
		else if (y> noteHeight-8 && x > noteWidth - 8) {
				super.mouseDown(modifiers, clickCount, when, x, y, tool);
		}
		else {
			String edited = MesquiteString.queryMultiLineString(ownerModule.containerOfModule(), "Note", "Note for tree:", sb.toString(), 8, false, false);
			if (edited!=null)
				setText(edited);
		}
	}
}


