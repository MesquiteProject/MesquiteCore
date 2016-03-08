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
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import java.util.*;
/* ======================================================================== */
/** A window that displays text.  Yet to do: make it editable or not, have getText, etc.. */
public class MesquiteHTMLWindow extends MesquiteWindow implements HyperlinkListener {
	MesqJEditorPane tA;
	String assignedTitle;
	MesquiteCommand linkTouchedCommand;
	Vector pastTexts = new Vector();
	MHTMLControl controls;
	JScrollPane scrollPane;
	MesquiteWindow dataWindow;
	boolean showBack = true;
	int controlHeight = 0;
	boolean backEnabled = true;
	public MesquiteHTMLWindow(MesquiteModule module, MesquiteCommand linkTouchedCommand, String assignedTitle, boolean showInfoBar) {
		this(module, linkTouchedCommand, assignedTitle, true, showInfoBar);
	}
	public MesquiteHTMLWindow(MesquiteModule module, MesquiteCommand linkTouchedCommand, String assignedTitle, boolean showPanel, boolean showInfoBar) {
		super(module, showInfoBar);// ���
		this.assignedTitle = assignedTitle;
		setTitle(assignedTitle);
		//setBackground(Color.white);
		//setWindowSize(600, 400);
		this.linkTouchedCommand = linkTouchedCommand;
		tA= new MesqJEditorPane("text/html","<html></html>");
		tA.setEditable(false);
		tA.setBackground(Color.white);
		tA.setForeground(Color.black);
		tA.setVisible(true);
		tA.addHyperlinkListener(this);
		showBack = (linkTouchedCommand != null);
		scrollPane = new  JScrollPane(); 
		scrollPane.getViewport().add( tA,  BorderLayout.CENTER ); 
		if (showPanel)
			showPanel();
		resetTitle();
	}
	public void showPanel(){
		addToWindow(scrollPane);
		if (showBack){
			controlHeight = 18;
			controls = new MHTMLControl(this);
			addToWindow(controls);
			controls.setBounds(0, 0, getWidth(), controlHeight);
			controls.setBackground(Color.white);
		}
		scrollPane.setBounds(0, controlHeight, getWidth(), getHeight() - controlHeight);
	}
	public void setLinkTouchedCommand(MesquiteCommand c){
		linkTouchedCommand = c;
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle(assignedTitle);
	}
	/*.................................................................................................................*/
	public void setDataWindow(MesquiteWindow w){
		this.dataWindow = w;
	}
	public MesquiteWindow getDataWindow(){
		if (dataWindow != null){
			if (dataWindow.disposed())
				return null;
		}
		return dataWindow;
	}
	/*.................................................................................................................*/
	/** Gets the minimum height of the content area of the window */
	public int getMinimumContentHeight(){
		return 100;
	}
	public void setText(String s) {
		try {
			if (s!=null && tA!=null) {
				if (backEnabled && pastTexts!=null){
					pastTexts.addElement(tA.getText());
				}
				tA.setText(s);  
			}
		}
		catch (Exception e){
			if (MesquiteTrunk.debugMode)
				MesquiteMessage.println("HTMLWindow: setText caused exception, " + e.toString());
		}
		//tA.repaint();
		if (showBack)
			controls.repaint();
	}
	void goBack(){
		if (backEnabled && pastTexts.size() <= 1)
			return;
		String t = (String)pastTexts.lastElement();
		pastTexts.removeElementAt(pastTexts.size()-1);
		tA.setText(t);
		//tA.repaint();
		if (showBack)
			controls.repaint();
	}
	public void setBackEnabled(boolean en){
		backEnabled = en;
		if (showBack)
		controls.repaint();
	}
	public String getText() {
		return tA.getText();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Goes back to previous page",null, commandName, "goBack")) {
			goBack();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** to be overridden by MesquiteWindows for a text version of their contents*/
	public String getTextContents() {
		return getText();
	}
	/*.................................................................................................................*/
	public void windowResized(){
		super.windowResized();
		if (scrollPane!=null){
			scrollPane.setBounds(0, controlHeight, getWidth(), getHeight() - controlHeight);
			scrollPane.invalidate();
			scrollPane.validate();
			
			if (showBack)
				controls.setBounds(0, 0, getWidth(), controlHeight);
		}
	}

	/** Sets the window size.  To be used instead of setSize. 
	public void containerSizeSet(int width, int height) {
	}
	/*.................................................................................................................*/
	/** Sets the window size.  To be used instead of setSize. */
	public void setWindowSize(int width, int height) {
		super.setWindowSize(width, height);
		if (scrollPane!=null){
			scrollPane.setBounds(0, controlHeight, getWidth(), getHeight() - controlHeight);
			if (showBack)
				controls.setBounds(0, 0, getWidth(), controlHeight);
		}
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


	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			JEditorPane pane = (JEditorPane) e.getSource();

			if (e instanceof HTMLFrameHyperlinkEvent) {
				HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
				HTMLDocument doc = (HTMLDocument)pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent(evt);
			} else {
				try {
					pane.setPage(e.getURL());
				} catch (Throwable t) {
					if (linkTouchedCommand != null)
						linkTouchedCommand.doItMainThread(ParseUtil.tokenize(e.getDescription()), null, false, false);
				}
			}
		}
	}

}
class MHTMLControl extends MesquitePanel {
	MesquiteHTMLWindow window;
	static Image backImage, backDimImage;

	MesquiteCommand goBackCommand = null;
	public MHTMLControl (MesquiteHTMLWindow window){
		if (	backImage==null){
			backImage=  MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "back.gif");  
			backDimImage=  MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "backDim.gif");  
		}
		this.window = window;
		goBackCommand = new MesquiteCommand("goBack", window);
	}
	/*.................................................................................................................*/
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (x>16 && x<46 && y>0 && y<window.controlHeight)
			goBackCommand.doItMainThread(null, null, null);
	}
	public void paint(Graphics g){
		if (window.backEnabled && window.pastTexts.size() > 1)
			g.drawImage(backImage, 16, 0, this);
		else
			g.drawImage(backDimImage, 16, 0, this);

		g.fillRect(0,getBounds().height -2,getBounds().width, 4);
	}
}

