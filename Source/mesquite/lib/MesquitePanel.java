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
import java.awt.event.KeyEvent;


/* ======================================================================== */
/** This class adds drop down menu capabilities to panels.  A default component of the drop down
menu is the Font selection */

public class MesquitePanel extends MousePanel  {
	LeakFinder leakFinder = MesquiteTrunk.leakFinderObject;
	MesquitePopup popup=null;
	MesquiteCommand setFontCommand, setFontSizeCommand;
	Font curFont = null;


	 
	public MesquitePanel () {
		super();
		setLayout(null);
		setFontCommand =MesquiteModule.makeCommand("setFont",  this);
		setFontSizeCommand =MesquiteModule.makeCommand("setFontSize",  this);

	}
	public Frame getFrame() {
		Container c = getParent();
		while (c!=null && !(c instanceof Frame))
			 c = c.getParent();
		return (Frame)c;
	}
	public void dispose(){
		setFontCommand =null;
		setFontSizeCommand =null;
		if (popup!=null)
			remove(popup);
		super.dispose();
	}
	public Component add(Component c){
		if (c==null)
			return null;
			else
				return super.add(c, 0);
	}
	public void remove(Component c){
		requestFocusInWindow();
		if (c!=null)
			super.remove(c);
	}
	/*.................................................................................................................*/
	public void setBounds(int x, int y, int width, int height ){
		super.setBounds(x, y, width, height);
		int locX, locY;
		if (width>32)
			locX = (width-32)/2;
		else
			locX = 0;
		if (height>32)
			locY = (height-32)/2;
		else
			locY = 0;
	}
	/*.................................................................................................................*/
	public void setSize(int width, int height ){
		super.setSize(width, height);
		int locX, locY;
		if (width>32)
			locX = (width-32)/2;
		else
			locX = 0;
		if (height>32)
			locY = (height-32)/2;
		else
			locY = 0;
	}
	/*.................................................................................................................*/
	void redoMenu() {
		if (popup==null)
			popup = new MesquitePopup(this);
		MesquiteSubmenu submenuFont=MesquiteSubmenu.getFontSubmenu("Font", popup, null, setFontCommand);
		/*
		String[] fonts = StringUtil.getFontList();

		for (int i=0; i<fonts.length; i++)
			submenuFont.add(new MesquiteMenuItem(fonts[i],  null, setFontCommand, fonts[i]));
		*/
		popup.add(submenuFont);
		MesquiteSubmenu submenuSize=MesquiteSubmenu.getFontSizeSubmenu("Size", popup, null, setFontSizeCommand);
		popup.add(submenuSize);
		add(popup);
	}
	/*.................................................................................................................*/
	public void setFontName (String name) {
		if (curFont==null)
			curFont = getParent().getFont();
 		Font fontToSet = new Font (name, curFont.getStyle(), curFont.getSize());
 		if (fontToSet!= null) {
 			curFont = fontToSet;
 			setPanelFont(curFont);
 		}
    	 }
	/*.................................................................................................................*/
	public void setFontStyle (int style) {
		if (curFont==null)
			curFont = getParent().getFont();
 		Font fontToSet = new Font (curFont.getName(), style, curFont.getSize());
 		if (fontToSet!= null) {
 			curFont = fontToSet;
 			setPanelFont(curFont);
 		}
    	 }
	/*.................................................................................................................*/
    	 public void setFontSize (int size) {
		if (curFont==null)
			curFont = getParent().getFont();
 		Font fontToSet = new Font (curFont.getName(), curFont.getStyle(), size);
 		if (fontToSet!= null) {
 			curFont = fontToSet;
 			setPanelFont(curFont);
 		}
    	 }
    	 
	/*...............................................................................................................*/
	public void setWindowAnnotation(String s, String annotationExplanation) {
		MesquiteWindow f = MesquiteWindow.windowOfItem(this);
		if (f != null && f instanceof MesquiteWindow){
			((MesquiteWindow)f).setAnnotation(s, annotationExplanation);
		}
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
     	 	Snapshot temp = new Snapshot();
     	 	Rectangle b = getBounds();
 		temp.addLine("setBounds " +  b.x + " " + b.y + " " + b.width+ " " + b.height);
  	 	if (curFont!=null) {
	 		temp.addLine("setFont " +  ParseUtil.tokenize(curFont.getName()));  //fixed to tokenize 12 Oct 01
  	 		temp.addLine("setFontSize " +  curFont.getSize());
	 	}
	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the font of the panel", "[font name]", commandName, "setFont")) {
    	 		MesquiteInteger pos = new MesquiteInteger(0);
			if (curFont==null)
				curFont = getParent().getFont();
    	 		Font fontToSet = new Font (ParseUtil.getFirstToken(arguments, pos), curFont.getStyle(), curFont.getSize()); //fixed to get first token 12 Oct 01
    	 		if (fontToSet!= null) {
    	 			curFont = fontToSet;
    	 			setPanelFont(curFont);
	    	 		repaint();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the font size of the panel", "[font size]", commandName, "setFontSize")) {
			if (curFont==null)
				curFont = getParent().getFont();
    	 		MesquiteInteger pos = new MesquiteInteger(0);
    	 		int siz = MesquiteInteger.fromString(ParseUtil.getFirstToken(arguments, pos));
    	 		if (!MesquiteInteger.isCombinable(siz)) {
				siz = MesquiteInteger.queryInteger(MesquiteWindow.windowOfItem(this), "Set Font Size", "Font size", curFont.getSize(), 2, 256);
			}
    	 		if (MesquiteInteger.isCombinable(siz)) {
	    	 		Font fontToSet = new Font (curFont.getName(), curFont.getStyle(), siz);
	    	 		if (fontToSet!= null) {
	    	 			curFont = fontToSet;
	    	 			setPanelFont(curFont);
	    	 			repaint();
	    	 		}
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the bounds of the panel, in pixels", "[x (left bound)] [y (upper bound)] [width] [height]", commandName, "setBounds")) {
    	 		MesquiteInteger pos = new MesquiteInteger(0);
    	 		int xA = MesquiteInteger.fromString(arguments, pos);
    	 		int yA = MesquiteInteger.fromString(arguments, pos);
    	 		int wA =  MesquiteInteger.fromString(arguments, pos);
    	 		int hA  = MesquiteInteger.fromString(arguments, pos);
    	 		setBounds(xA, yA, wA, hA);
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
   	 }
    		public boolean autoFontSubmenu () {
  			return true;
    		}
	/* to be used by subclasses to tell that panel touched */
    	 protected void panelTouched (int modifiers,int x, int y, boolean controlNeeded) {
if (autoFontSubmenu() && MesquiteEvent.rightClick(modifiers) || !controlNeeded) {
			if (popup==null)
				redoMenu();
			popup.show(this, x,y);
		}
	}
	


}
