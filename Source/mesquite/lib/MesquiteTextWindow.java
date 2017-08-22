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
import java.awt.event.*;

import mesquite.lib.duties.*;
import java.awt.datatransfer.*;

/* ======================================================================== */
/** A window that displays text.  Yet to do: make it editable or not, have getText, etc.. */
public class MesquiteTextWindow extends MesquiteWindow implements FocusListener {
	protected TextArea tA;
	String assignedTitle;
	MesquiteCommand copyCommand;
	MesquiteCommand pasteCommand = null;
	public MesquiteTextWindow(MesquiteModule module, String assignedTitle, boolean showInfoBar) {
		this(module, assignedTitle, showInfoBar, true, false);
	}
	public MesquiteTextWindow(MesquiteModule module, String assignedTitle, boolean showInfoBar, boolean wrap, boolean allowPaste) {
		super(module, showInfoBar);// ���
		this.assignedTitle = assignedTitle;
		//setBackground(Color.white);
		setWindowSize(400, 450);
		if (wrap)
			tA= new TextArea("", 90, 90,  TextArea.SCROLLBARS_VERTICAL_ONLY);
		else
			tA= new TextArea("", 90, 90,  TextArea.SCROLLBARS_BOTH);
		tA.setEditable(false);
		tA.setBackground(Color.white);
		tA.setForeground(Color.black);
		tA.setVisible(true);
		tA.addFocusListener(this);
		addToWindow(tA);
		tA.setSize(getWidth(), getHeight());
		try {
			tA.setFocusTraversalKeysEnabled(false);
		}
		catch (Error e){
			MesquiteMessage.printStackTrace("error with setFocusTraversalKeysEnabled");
		}
		//	pack();
		resetTitle();
		if (allowPaste)
			pasteCommand = MesquiteModule.makeCommand("paste", this);
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle(assignedTitle);
	}
	public void setTitle(String s){ //in case set after instantiation, so that change stays
		super.setTitle(s);
		assignedTitle = s;
	}
	public MesquiteCommand getPasteCommand() {
		return pasteCommand; 
	}

	/*.................................................................................................................*/
	/** Gets the minimum height of the content area of the window */
	public int getMinimumContentHeight(){
		return 100;
	}
	public void setEditable(boolean ed) {
		tA.setEditable(ed);
	}
	public void append(String s) {
		if (tA == null)
			return;
		if (s == null)
			s = "";
		try{
			tA.append(s);
		}
		catch (Exception e){
		}
	}
	public void consume(int i) {
		int L = tA.getText().length();
		tA.replaceRange("", L-i, L);
	}
	public void setText(String s) {
		tA.setText(s);
		try {
			tA.setCaretPosition(0);
		}
		catch (Exception e){
		}
		tA.repaint();
	}
	public String getText() {
		return tA.getText();
	}
	public void focusGained(FocusEvent e){

	}
	public void focusLost(FocusEvent e){

	}
	/*.................................................................................................................*/
	/** to be overridden by MesquiteWindows for a text version of their contents*/
	public String getTextContents() {
		return getText();
	}
	/*.................................................................................................................*/
	public void printWindow(MesquitePrintJob pjob) {
		if (pjob != null) {
			if (infoBar.getMode()>0) 
				super.printWindow(pjob);
			else 
				pjob.printText(getText(), new Font("Monospaced", Font.PLAIN, 12));
		}
	}
	/*.................................................................................................................*/
	/**
	@author Peter Midford
	 */
	public void windowToPDF(MesquitePDFFile pdfFile, int fitToPage) {
		if (pdfFile != null) {
			if (infoBar.getMode()>0)
				super.windowToPDF(pdfFile, fitToPage);
			else
				//Changed font size to 10, since courier is the only adobe supplied monospace font and
				//it is a little larger than the system supplied monospace font.
				pdfFile.printText(getText(), new Font("Monospaced", Font.PLAIN, 10));
		}
	}
	/*.................................................................................................................*/
	public void windowResized(){
		super.windowResized();
		if (tA!=null)
			tA.setSize(getWidth(), getHeight()); //getFullWidth
	}
	/** Sets the window size.  To be used instead of setSize. 
	public void containerSizeSet(int width, int height) {
	}
	/*.................................................................................................................*/
	/** Sets the window size.  To be used instead of setSize. */
	public void setWindowSize(int width, int height) {
		super.setWindowSize(width, height);
		if (tA!=null)
			tA.setSize(getWidth(), getHeight());
	}
	/*.................................................................................................................*/
	/** Sets the window font size.  */
	public boolean setWindowFontSize(int fontSize) {
		if (!MesquiteInteger.isCombinable(fontSize))
			fontSize = MesquiteInteger.queryInteger(MesquiteWindow.windowOfItem(this), "Font size", "Font size for window", currentFont.getSize(), 4, 256);
		if (!MesquiteInteger.isCombinable(fontSize))
			return false;
		Font fontToSet = new Font (currentFont.getName(), currentFont.getStyle(), fontSize);
		if (fontToSet!= null) {
			currentFont = fontToSet;
			if (tA!=null)
				tA.setFont(fontToSet); //added here 12 Oct 01 because text area font was not being set in OS X 10.1
		}
		return super.setWindowFontSize(fontSize);
	}
	/*.................................................................................................................*/
	/** Sets the window font.  */
	public void setWindowFont(String fontName) {
		Font fontToSet = new Font (fontName, currentFont.getStyle(), currentFont.getSize());
		if (fontToSet!= null) {
			currentFont = fontToSet;
			if (tA!=null)
				tA.setFont(fontToSet); //added here 12 Oct 01 because text area font was not being set in OS X 10.1
		}
		super.setWindowFont(fontName);
	}
	/*.................................................................................................................*/
	public void copyGraphicsPanel(){
		if (tA == null)
			return;
		String s = tA.getSelectedText();
		if (!StringUtil.blank(s)) {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection ss = new StringSelection(s);
			clip.setContents(ss, ss);
		}
		else {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection ss = new StringSelection(tA.getText());
			clip.setContents(ss, ss);
		}
	}
	/*.................................................................................................................*/
	/** Respond to commands sent to the window. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets the font of the window", "[name of font]", commandName, "setFont")) { 
			String fontName = ParseUtil.getFirstToken(arguments, pos);
			setWindowFont(fontName);
		}
		else if (checker.compare(getClass(), "Sets the font size of the window", "[font size]", commandName, "setFontSize")) {
			int fontSize = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (!setWindowFontSize(fontSize))
				return null;
		}
		else if (checker.compare(getClass(), "Prints the contents of the window", null, commandName, "printWindow")) {
			prWindow(MesquitePrintJob.AUTOFIT);
		}
		else if (checker.compare(getClass(), "Pastes selection from clipboard", null, commandName, "paste")) {
			if (tA == null)
				return null;
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = clip.getContents(this);
			try {
				String s = (String)t.getTransferData(DataFlavor.stringFlavor);
				if (s!=null && tA!=null) {
					String text = tA.getText();
					String newText = text.substring(0, tA.getSelectionStart()) + s + text.substring(tA.getSelectionEnd(), text.length());
					tA.setText(newText);
					return s;
				}
			}
			catch(Exception e){
				MesquiteMessage.printStackTrace(e);
			}
			return null;
		}
		else 
			return  super.doCommand(commandName, arguments, checker); //note that this will repeat the setFont for the window
		return null;
	}
}


