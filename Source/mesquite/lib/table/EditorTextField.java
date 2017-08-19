/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.table;

import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;

import java.util.*;


/* ======================================================================== */
/** A text field that is moved from place to place as the text is edited in a panel of a MesquiteTable.*/
public class EditorTextField extends TextField  {
	int row, column;
	EditorPanel panel;
	boolean editing;
	boolean forgetFocusGained=false;
	boolean suppressFocusLost = false;
	FontMetrics fontMetrics;
	boolean wasEditingListener=false;
	String previousText = null;
	boolean allowReturn = true;
	MesquiteCommand textReturnedCommand;
	KListener kListener;
	String originalText = null;
	Undoer originalUndoer = null;
	Listened listened = new Listened();
	boolean somethingTyped = false;
	
	public EditorTextField (EditorPanel panel, int column, int row) {
		super();
		try {
			setFocusTraversalKeysEnabled(false);
		}
		catch (Error e){
		}
		this.panel = panel;
		this.column=column;
		this.row = row;
		editing = false;
		textReturnedCommand = MesquiteModule.makeCommand("returnText", panel);
		textReturnedCommand.setSuppressLogging(true);
		setBackground(ColorDistribution.veryLightGray);
	//	setBackground(ColorDistribution.cyan);
		kListener = new KListener(panel.getMesquiteWindow(), this);
		addKeyListener(kListener); 
		addFocusListener(new FListener());
	}
	
	public Listened getListened() {
		return listened;
	}
	public void setEditing(boolean editing) {
		selStart = -1;
		selEnd = -1;
		this.editing = editing;
		wasEditingListener = true;
		kListener.setWindow(panel.getMesquiteWindow());
	}
	public boolean getEditing() {
		return editing;
	}
	public void setFont(Font f){
		super.setFont(f);
		Graphics g = getGraphics();
		if (g==null)
			return;
		fontMetrics = g.getFontMetrics(getFont());
		g.dispose();
	}
	

private int ccc = 0;
	public void setText(String s){
		if (s == null)
			super.setText("");
		else {
			super.setText(s);
			originalText = s;
		}
		allowReturn = true;
	}
	
	public String getOriginalText(){
		return originalText;
	}
	public int getMinimalWidth(){
		if (fontMetrics==null) {
			Graphics g=getGraphics();
			if (g==null)
				return 0;
			fontMetrics = g.getFontMetrics(getFont());
			g.dispose();
			if (fontMetrics==null)
				return 0;
		}
		return fontMetrics.stringWidth(getText())+MesquiteModule.textEdgeCompensationWidth;
	}
	public void dontRestoreIfFocusGained() {
		forgetFocusGained = true;
	}
	public void setSuppressFocusLost(boolean b) {
		suppressFocusLost = b;
	}
	public void resetLocation(){
		if (panel == null || panel.getTable() == null || panel.getWindow() == null)
			return;
		if (somethingTyped && originalUndoer != null){ 
			//this is a bit of a kludge, given that it dives into the table to find a CharacterData
			if (panel.getTable().getColumnAssociable() != null && panel.getTable().getColumnAssociable() instanceof mesquite.lib.characters.CharacterData)
				panel.getWindow().setUndoer(new UndoInstructions(UndoInstructions.SINGLEDATACELL, column, row, new MesquiteString(getOriginalText()),new MesquiteString(getText()), (mesquite.lib.characters.CharacterData)panel.getTable().getColumnAssociable(), panel.getTable()));
			else
				panel.getWindow().setUndoer((Undoer)null);
				
		}
		somethingTyped = false;
		//setUndoer
		originalUndoer = null;
	}
	public void setColumnRow (int column, int row) {
		if (this.column!=column || this.row!=row){
			previousText = null;
			resetLocation();
		}
		this.column=column;
		this.row = row;
	}
	public void paint(Graphics g){ //^^^
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		super.paint(g);
		if (fontMetrics==null) {
			fontMetrics = g.getFontMetrics(getFont());
			panel.checkBounds();
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	
	
	public int getColumn() {
		return column;
	}
	public int getRow() {
		return row;
	}
	int selStart=0;
	int selEnd=0;
	public void offEditDontRecord() {  //1. 12
		if (editing) {
			selStart = -1;
			selEnd = -1;
			allowReturn = false;
			setEditing(false);
		}
	}
	public void offEdit() {
		if (editing) {
			boolean refreshPanel = false;
			if (column>=-1 && row >=-1 && allowReturn){
				panel.aboutToReturnText(column, row);
				resetLocation();
				textReturnedCommand.doItMainThread(Integer.toString(column) + " " + row + "  " + ParseUtil.tokenize(getText()), null, this);
				refreshPanel = true;
			}
			else 
				resetLocation();

			selStart = -1;
			selEnd = -1;
			allowReturn = false;
			setEditing(false);
			if (refreshPanel)
				panel.checkEditFieldLocation();
		}
	}
	public void recordEdit() {
		if (editing) {
			if (column>=-1 && row >=-1 && allowReturn){
				panel.aboutToReturnText(column, row);
				resetLocation();
				textReturnedCommand.doItMainThread(Integer.toString(column) + " " + row + "  " + ParseUtil.tokenize(getText()), null, this);
			}
			else
				resetLocation();
			allowReturn = false;
		}
	}
	class FListener extends FocusAdapter {
		public void focusLost(FocusEvent e){
			
			if (editing && !suppressFocusLost) {
				wasEditingListener = editing;
				selStart = getSelectionStart();
				selEnd = getSelectionEnd();
				previousText = getText();
				panel.recordEdit();
			}
		}
		public void focusGained(FocusEvent e){
			if (wasEditingListener && !forgetFocusGained) {
				if (previousText !=null)
					setText(previousText);
				previousText = null;
				try{
					if (selStart<0 || selEnd <0) {
						selectAll();
						selStart = getSelectionStart();
						selEnd = getSelectionEnd();
					}
					else {
						setSelectionStart(selStart);
						setSelectionEnd(selEnd);
					}
				}
				catch (NullPointerException npe){
				}
				setEditing(true);
			}
		}

	}
	class KListener extends KeyAdapter {
		MesquiteWindow window = null;
		EditorTextField textField;
		public KListener (MesquiteWindow window, EditorTextField textField){
			super();
			this.window=window;
			this.textField = textField;
		}
		public void setWindow(MesquiteWindow window) {
			this.window = window;
		}
		public void keyPressed(KeyEvent e){
			//Event queue
		if (e.getKeyCode()== KeyEvent.VK_ENTER) {
				panel.enterPressed(e);
			}
			else if (e.getKeyCode()== KeyEvent.VK_TAB) {
				panel.tabPressed(e);
			}
			else if (e.getKeyCode()== KeyEvent.VK_RIGHT){
				if (getSelectionStart()==0 && getSelectionEnd()== getText().length())
					panel.rightArrowPressed(e);
			}
			else if (e.getKeyCode()== KeyEvent.VK_LEFT) {
				if (getSelectionStart()==0 && getSelectionEnd()== getText().length())
					panel.leftArrowPressed(e);
			}
			else if (e.getKeyCode()== KeyEvent.VK_UP)
				panel.upArrowPressed(e);
			else if (e.getKeyCode()== KeyEvent.VK_DOWN)
				panel.downArrowPressed(e);
			else 	if (MesquiteEvent.commandOrControlKeyDown(MesquiteEvent.getModifiers(e))) {
				if (window!=null & somethingTyped) {
					window.setNewUndoState(new MesquiteString(textField.getText()));
				}
			}
			else {  //not commandORControl down
				somethingTyped=true;
				if (window!=null)
					if (textField.getOriginalText()!=null) {
						originalUndoer = new UndoInstructions(UndoInstructions.EDITTEXTFIELD, new MesquiteString(textField.getOriginalText()),new MesquiteString(""), textField);
						window.setUndoer(originalUndoer);
					}
			}

		}
	}
}

