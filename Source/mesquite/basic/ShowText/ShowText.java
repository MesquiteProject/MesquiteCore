/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.ShowText;

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class ShowText extends TextDisplayer {
	boolean goAwayable;
	boolean wrap = true;
	boolean allowPaste = false;
	int poppedOut = 2;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	
	public void setWrap(boolean w){
		this.wrap = w;
	}
	public void setAllowPaste(boolean w){
		this.allowPaste = w;
	}
	public void showFile(String pathName, int maxCharacters, boolean goAwayable, int fontSize, boolean monospaced){
		String s = MesquiteFile.getFileContentsAsString(pathName, maxCharacters);
		showText(s, pathName, goAwayable, fontSize, monospaced);
	}
	public void showFile(String pathName, int maxCharacters, boolean goAwayable){
		String s = MesquiteFile.getFileContentsAsString(pathName, maxCharacters);
		showText(s, pathName, goAwayable);
	}
	public void showFile(MesquiteFile file, int maxCharacters, boolean goAwayable, int fontSize, boolean monospaced){
		String s = file.getFileContentsAsString(maxCharacters);
		showText(s, file.getFileName(), goAwayable, fontSize, monospaced);
	}
	public void showFile(MesquiteFile file, int maxCharacters, boolean goAwayable){
		String s = file.getFileContentsAsString(maxCharacters);
		showText(s, file.getFileName(), goAwayable);
	}
	public void showText(String s, String title, boolean goAwayable){
		showText(s,title,goAwayable, 0, false);
	}
	public void setPoppedOut(int w){
		poppedOut = w;
	}
	public void showText(String s, String title, boolean goAwayable, int fontSize, boolean monospaced){
		this.goAwayable = goAwayable;
		if (s == null)
			s = "(No Text to show)";
		if (getModuleWindow()==null) {
			setModuleWindow( new MesquiteTextWindow(this, title, true, wrap, allowPaste)); 
			((MesquiteTextWindow)getModuleWindow()).setText(s);
			if (poppedOut > 0){
				if (poppedOut==1)
					getModuleWindow().setPopAsTile(true);
				getModuleWindow().popOut(false);
			}
			getModuleWindow().setWindowSize(500, 500);
			if (fontSize>0)
				getModuleWindow().setWindowFontSize(fontSize);
			if (monospaced) {
				((MesquiteTextWindow)getModuleWindow()).setWindowFont("Monospaced");
			}
			getModuleWindow().setVisible(true);
			resetContainingMenuBar();
			resetAllWindowsMenus();
		}
		else {
			((MesquiteTextWindow)getModuleWindow()).setText(s);
		}
	}
	/*.................................................................................................................
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	if (window!= null && window.isVisible())
  	 		temp.addLine("showScript "); 
  	 	return temp;
  	 }
	/*.................................................................................................................
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), nullxxx, null, commandName, "showScript")) {  
    	 		MesquiteBlock b = null;
    	 		if (blocks == null) {
    	 			b = makeBlock(null, "BEGIN MESQUITE;\nEND;"); //TODO: into which file assigned? (dialog box if more than one file opened)
    	 		}
    	 		else if (StringUtil.blank(arguments)) {
	    	 			//have dialog requesting which??
	    	 			b = ((MesquiteBlock)blocks.elementAt(0));
	    	 	}
	    	 	else {
    	 			int whichBlock = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
	    	 		if (whichBlock >=0 && whichBlock < blocks.size())
	    	 			b = ((MesquiteBlock)blocks.elementAt(whichBlock));
   	 		}
    	 		if (b!=null) {
	    			if (window==null)
	    				window = new MesquiteTextWindow(this, "Mesquite script", false); //infobar
	    			((MesquiteTextWindow)window).setText(b.getText());
	    			currentBlock = b;
	    			((MesquiteTextWindow)window).setEditable(true);
	    			window.setVisible(true);
	    			return window;
    			}
    	 	}
    	 	else if (checker.compare(this.getClass(), nullxxx, null, commandName, "getSnapshots")) {  
    	 		int w = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
    	 		MesquiteModule mb = MesquiteTrunk.mesquiteTrunk.findEmployeeWithIDNumber(w);
    	 		if (mb!=null) {
    	 			return getSnapshotCommands(mb, null, "\t");
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	 
   	
 	/*.................................................................................................................*/
  	public void windowGoAway(MesquiteWindow whichWindow) {
   		if (goAwayable && whichWindow == getModuleWindow()) {
    			whichWindow.hide();
    			whichWindow.dispose();
    			iQuit();
    		}
   	}


	/*.................................................................................................................*/
    	 public String getName() {
		return "Show text";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Shows text in a window" ;
   	 }
   	 
}
	

