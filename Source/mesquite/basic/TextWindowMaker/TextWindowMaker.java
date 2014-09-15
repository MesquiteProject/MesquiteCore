/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.TextWindowMaker;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class TextWindowMaker extends FileAssistantN {
	MesquiteTextWindow textWindow;
	String pathToPicture;
	Color bgColor;
	static int numMade = 0;
	String noteTitle = "";
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) { //todo: should behave differently depending on whether home file was url or not!!!
		noteTitle = "Note " + (++numMade);
		textWindow = new MesquiteTextWindow(this, noteTitle, true, true, true);
		textWindow.setEditable(true);
 		setModuleWindow(textWindow);
		if (!MesquiteThread.isScripting()){ //file dialog to choose picture
			textWindow.setPopAsTile(true);
			textWindow.popOut(true);
			textWindow.setVisible(true);
		}
		makeMenu("Note");
		MesquiteSubmenuSpec mmis = addSubmenu(null, "Background Color", makeCommand("setBackground",  this));
		mmis.setList(ColorDistribution.standardColorNames);
		addMenuItem("Title...", makeCommand("queryTitle", this));
		addMenuItem("Delete Note", makeCommand("deleteNote", this));
 		resetContainingMenuBar();
 		resetAllWindowsMenus();
 		return true;
  	 }
	 
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	if (textWindow ==null) // || !textWindow.isVisible())
  	 		return null;
   	 	Snapshot temp = new Snapshot();
   	 	temp.addLine("setText " + StringUtil.tokenize(textWindow.getText()));
   	 	temp.addLine("setTitle " + StringUtil.tokenize(noteTitle));
  	 	temp.addLine("getWindow");
  	 	Snapshot fromWindow = textWindow.getSnapshot(file);
 		
		temp.addLine("tell It");
		temp.incorporate(fromWindow, true);
		temp.addLine("endTell");

  	 	if (bgColor !=null) {
  	 		String bName = ColorDistribution.getStandardColorName(bgColor);
  	 		if (bName!=null)
  	 			temp.addLine("setBackground " + StringUtil.tokenize(bName));
  	 	}
		if (textWindow.isVisible())
			temp.addLine("showWindow");
		else
			temp.addLine("hideWindow");
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	 if (checker.compare(this.getClass(), "Sets the text in the text window", "[text]", commandName, "setText")){
				textWindow.setText(parser.getFirstToken(arguments));
    	 	 } 
     	 	 else if (checker.compare(this.getClass(), "Sets the title of the text window", "[text]", commandName, "setTitle")){
				textWindow.setTitle(parser.getFirstToken(arguments));
    	 	 } 
     	 	 else if (checker.compare(this.getClass(), "Queries the user for the title of the text window", "[text]", commandName, "queryTitle")){
     	 		 MesquiteString ms = new MesquiteString(noteTitle);
				if ( QueryDialogs.queryShortString(containerOfModule(), "Title", "Title of note:", ms)){
					noteTitle = ms.getValue();
					textWindow.setTitle(noteTitle);
				}
    	 	 } 
    	 	 else if (checker.compare(this.getClass(), "Deletes the text window", null, commandName, "deleteNote")){
     	 		textWindow.hide();
     	 		textWindow.dispose();
     			iQuit();
     	 	 } 
     	 	 else if (checker.compare(this.getClass(), "Hides the text window", null, commandName, "hideWindow")){
      	 		textWindow.hide();
      	 	 } 
     	 	 else if (checker.compare(this.getClass(), "Sets the color of the window", "[name of color]", commandName, "setBackground")) {
    	 		Color bc = ColorDistribution.getStandardColor(ParseUtil.getFirstToken(arguments, stringPos));
			if (bc == null)
				return null;
			bgColor = bc;
			setBackgroundAll(textWindow.getOuterContentsArea(), bc);
			textWindow.repaintAll();
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	
     	 	 return null;
   	 }
	/** calls repaint of all components*/
	void setBackgroundAll(Component c, Color col){
		if (c==null)
			return;
		c.setBackground(col);
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					setBackgroundAll(cc[i], col);
		}
		
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Text Window";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "New Notes Window";
   	 }
	/*.................................................................................................................*/
 	public void windowGoAway(MesquiteWindow whichWindow) {
			whichWindow.hide();
		//	whichWindow.dispose();
		//	iQuit();
	}
	public boolean isSubstantive(){
		return false;
	}
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Displays Text in a window." ;
   	 }
}
	

