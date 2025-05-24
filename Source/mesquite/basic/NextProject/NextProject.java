/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.NextProject;

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MQTextArea;
import mesquite.lib.ui.MQTextField;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.WindowButton;

/* ======================================================================== */

public class NextProject extends FileAssistant  {
	public String nextProjectName = null;
	NextProjectWindow npw;
	Color bgColor;
	MesquiteFile file;
	/*public Class getDutyClass(){
		return NextProject.class;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		
		setModuleWindow( npw = new NextProjectWindow(this));
		npw.setMinimalMenus(true);
		if (!MesquiteThread.isScripting()){
			getModuleWindow().setVisible(true);
		}
		
		if (MesquiteThread.isScripting() && CommandRecord.getScriptingFileS()!=null) {
			file = CommandRecord.getScriptingFileS();
			file.addListener(this);
		}
		makeMenu("Go-To-File");
		MesquiteSubmenuSpec mmis = addSubmenu(null, "Background Color", makeCommand("setBackground",  this));
		mmis.setList(ColorDistribution.standardColorNames);
 		resetContainingMenuBar();
 		resetAllWindowsMenus();
 		return true;
 	}
		/*.................................................................................................................*/
	 public String getExplanation() {
	return "Provides a window to jump to another file";
	 }
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (file == obj)
			windowGoAway(npw);
	}
	/*.................................................................................................................*/
 	public void endJob() {
			if (npw != null) {
				npw.hide();
				npw.dispose();
			}
			if (file !=null)
				file.removeListener(this);
			super.endJob();
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
   	 	if (npw !=null){
			if (!StringUtil.blank(npw.getText()))
			   	 		temp.addLine("setNextFileName " + StringUtil.tokenize(npw.getText()));
			if (!StringUtil.blank(npw.getJumpExplanation()))
			   	 		temp.addLine("setExplanation " + StringUtil.tokenize(npw.getJumpExplanation()));
			//temp.addLine("makeWindow");
   			temp.addLine("getWindow");
			temp.addLine("tell It");
	  	 	Snapshot fromWindow = npw.getSnapshot(file);
	  	 	temp.incorporate(fromWindow, true);
			temp.addLine("endTell");
	  	 	if (bgColor !=null) {
	  	 		String bName = ColorDistribution.getStandardColorName(bgColor);
	  	 		if (bName!=null)
	  	 			temp.addLine("setBackground " + StringUtil.tokenize(bName));
	  	 	}
				
			temp.addLine("showWindow");
		}
    	 	else {
    	 	if (!StringUtil.blank(nextProjectName))
   	 		temp.addLine("setNextFileName " + StringUtil.tokenize(nextProjectName));
   	 	}
		if (!StringUtil.blank(npw.getJumpExplanation()))
		   	 		temp.addLine("setExplanation " + StringUtil.tokenize(npw.getJumpExplanation()));
	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the file name to which to jump when the button is hit", "[path to file; if relative than to home file of project]", commandName, "setNextFileName")) {
			if (npw == null)
				return null;
    	 		nextProjectName =parser.getFirstToken(arguments);
    	 		((NextProjectWindow)getModuleWindow()).setText(nextProjectName);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets explanation to appear in text edit area", "[explanation string]", commandName, "setExplanation")) {
			if (npw == null)
				return null;
    	 		((NextProjectWindow)getModuleWindow()).setJumpExplanation(parser.getFirstToken(arguments));
    	 	}
    	 	else if (checker.compare(this.getClass(), "Jumps to next project recorded", null, commandName, "go")) {
    	 		nextProjectName = ((NextProjectWindow)getModuleWindow()).getText();
   	 		if (StringUtil.blank(nextProjectName))
   	 			return null;
   	 		while (getProject().developing)
   	 			;
			String commands = "newThread; getProjectID; Integer.id *It; tell Mesquite; getWindowAutoShow; String.was *It; windowAutoShow off; closeProjectByID *Integer.id; openFile ";
			commands +=  StringUtil.tokenize(MesquiteFile.composePath(getProject().getHomeDirectoryName(), nextProjectName)) + "; ifNotExists It;  debug; showAbout; endIf; windowAutoShow *String.was; endTell;";
			Puppeteer p = new Puppeteer(this);
			MesquiteInteger pos = new MesquiteInteger(0);
			p.execute(getFileCoordinator(), commands, pos, "", false);
			iQuit();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets background color of window", "[name of color]", commandName, "setBackground")) {
    	 		Color bc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
			if (bc == null)
				return null;
			bgColor = bc;
			if (npw == null)
				return null;
			npw.setColor(bc);
			if (npw.isVisible())
				npw.repaint();
    	 	}
    	 	else if (checker.compare(this.getClass(), "NOT USED", null, commandName, "makeWindow")) {
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
 	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
			whichWindow.hide();
			whichWindow.dispose();
			iQuit();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Go-to Window";
   	 }
	public boolean isSubstantive(){
		return false;
	}
   	 
}

/* ======================================================================== */
class NextProjectWindow extends MesquiteWindow {
	TextField tF;
	TextArea explanation;
	public NextProjectWindow(NextProject module) {
		super(module, false);
		
		setWindowSize(120, 60);
		setBackground(ColorDistribution.lightGreen);
		Panel contents = getGraphicsArea();
		contents.setLayout(new BorderLayout());
		contents.setBackground(ColorDistribution.lightGreen);
		
		explanation= new MQTextArea("", 8, 3, TextArea.SCROLLBARS_NONE);
		tF= new MQTextField();
   	 	if (!StringUtil.blank(module.nextProjectName))
   	 		tF.setText(module.nextProjectName);
		tF.setEditable(true);
		tF.setBackground(ColorDistribution.lightGreen);
		contents.add("North", tF);
		contents.add("Center", explanation);
		tF.setVisible(true);
		Panel buttons = new MQPanel();
		Font f = explanation.getFont();
 		if (f!=null){
	 		Font fontToSet = new Font (f.getName(), f.getStyle(), f.getSize()+4);
	 		if (fontToSet!= null) {
	 			explanation.setFont(fontToSet);
	 		}
 		}
		contents.add("South", buttons);
		Button ok;
		buttons.add("South", ok = new WindowButton("Go", this));
		Font df = new Font("Dialog", Font.PLAIN, 12);
		ok.setFont(df);
		
		setWindowSize(120, 60);
		resetTitle();
		
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree lists, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Go To File");
	}
	
	public void setColor(Color c){
		if (tF == null)
			return;
		setBackground(c);
		tF.setBackground(c);
		Panel contents = getGraphicsArea();
		contents.setBackground(c);
		repaintAll();
	}
	public void setText(String s) {
		if (tF == null)
			return;
		tF.setText(s);
		tF.repaint();
		repaint();
	}
	public String getText() {
		if (tF == null)
			return null;
		return tF.getText();
	}
	public void setJumpExplanation(String s) {
		if (explanation == null)
			return;
		explanation.setText(s);
		explanation.repaint();
		repaint();
	}
	public String getJumpExplanation() {
		if (explanation == null)
			return null;
		return explanation.getText();
	}
	/*=============*/
	public void buttonHit(String label, Button button) {
		if (label.equalsIgnoreCase("Go")) {
			getOwnerModule().doCommand("Go", null,  CommandChecker.defaultChecker);
		}
	}
}

