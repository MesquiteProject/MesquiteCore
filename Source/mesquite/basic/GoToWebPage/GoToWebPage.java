/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.GoToWebPage;

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */

public class GoToWebPage extends FileAssistant {
	public String targetName = null;

	GoToWebPageWindow npw;

	Color bgColor;

	/*
	 * public Class getDutyClass(){ return GoToWebPage.class; } /*.................................................................................................................
	 */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		setModuleWindow(npw = new GoToWebPageWindow(this));
		npw.setMinimalMenus(true);
		if (!MesquiteThread.isScripting()) {
			getModuleWindow().setVisible(true);
		}
		makeMenu("Web_Page_Link");
		MesquiteSubmenuSpec mmis = addSubmenu(null, "Background Color", makeCommand("setBackground", this));
		mmis.setList(ColorDistribution.standardColorNames);
		resetContainingMenuBar();
		resetAllWindowsMenus();
		return true;
	}

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (npw != null) {
			if (!StringUtil.blank(npw.getText()))
				temp.addLine("setPagePath " + StringUtil.tokenize(npw.getText()));
			if (!StringUtil.blank(npw.getJumpExplanation()))
				temp.addLine("setExplanation " + StringUtil.tokenize(npw.getJumpExplanation()));
			// temp.addLine("makeWindow");
			temp.addLine("getWindow");
			temp.addLine("tell It");
			Snapshot fromWindow = npw.getSnapshot(file);
			temp.incorporate(fromWindow, true);
			temp.addLine("endTell");
			if (bgColor != null) {
				String bName = ColorDistribution.getStandardColorName(bgColor);
				if (bName != null)
					temp.addLine("setBackground " + StringUtil.tokenize(bName));
			}

			temp.addLine("showWindow");
		}
		else {
			if (!StringUtil.blank(targetName))
				temp.addLine("setPagePath " + StringUtil.tokenize(targetName));
		}
		if (!StringUtil.blank(npw.getJumpExplanation()))
			temp.addLine("setExplanation " + StringUtil.tokenize(npw.getJumpExplanation()));
		return temp;
	}

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the URL of the web page to be opened when the button is hit", "[path to web page; if no http:// then relative to Mesquite]", commandName, "setPagePath")) {
			targetName = parser.getFirstToken(arguments);
			((GoToWebPageWindow) getModuleWindow()).setText(targetName);
		}
		else if (checker.compare(this.getClass(), "Sets explanation to appear in text edit area", "[explanation string]", commandName, "setExplanation")) {
			((GoToWebPageWindow) getModuleWindow()).setJumpExplanation(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Requests web page recorded in window", null, commandName, "show")) {
			targetName = ((GoToWebPageWindow) getModuleWindow()).getText();
			if (StringUtil.blank(targetName))
				return null;
			String path = null;
			if (targetName.startsWith("mesquite:")) {
				path = mesquiteDirectoryPath + targetName.substring(9, targetName.length());
			}
			else if (targetName.startsWith("project:")) {
				path = getProject().getHomeDirectoryName() + targetName.substring(8, targetName.length());
			}
			else
				path = targetName;
			showWebPage(path, false);

		}
		else if (checker.compare(this.getClass(), "Sets background color of window", "[name of color]", commandName, "setBackground")) {
			Color bc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
			if (bc == null)
				return null;
			bgColor = bc;
			npw.setColor(bc);
			npw.repaint();
		}
		else if (checker.compare(this.getClass(), "NOT USED", null, commandName, "makeWindow")) {
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/* ................................................................................................................. */
	public void windowGoAway(MesquiteWindow whichWindow) {
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}

	/* ................................................................................................................. */
	public String getName() {
		return "Web page link";
	}

	/* ................................................................................................................. */
	public String getExplanation() {
		return "Provides a window to jump to a web page";
	}

	public boolean isSubstantive() {
		return false;
	}

}

/* ======================================================================== */
class GoToWebPageWindow extends MesquiteWindow {
	TextField tF;

	TextArea explanation;

	public GoToWebPageWindow(GoToWebPage module) {
		super(module, false);
		setBackground(Color.cyan);
		Panel contents = getGraphicsArea();
		contents.setLayout(new BorderLayout());
		contents.setBackground(Color.cyan);

		explanation = new TextArea("", 8, 3, TextArea.SCROLLBARS_NONE);
		tF = new TextField();
		if (!StringUtil.blank(module.targetName))
			tF.setText(module.targetName);
		tF.setEditable(true);
		tF.setBackground(Color.cyan);
		tF.setVisible(true);
		contents.add("North", tF);
		contents.add("Center", explanation);
		Panel buttons = new Panel();
		Font f = explanation.getFont();
		if (f != null) {
			Font fontToSet = new Font(f.getName(), f.getStyle(), f.getSize() + 4);
			if (fontToSet != null) {
				explanation.setFont(fontToSet);
			}
		}
		contents.add("South", buttons);
		Button ok = null;
		buttons.add("South", ok = new WindowButton("Show", this));
		Font df = new Font("Dialog", Font.PLAIN, 12);
		ok.setFont(df);

		setWindowSize(120, 60);
		resetTitle();
	}

	/* ................................................................................................................. */
	/**
	 * When called the window will determine its own title. MesquiteWindows need to be self-titling so that when things change (names of files, tree lists, etc.) they can reset their titles properly
	 */
	public void resetTitle() {
		setTitle("Web page link");
	}

	public void setColor(Color c) {
		setBackground(c);
		tF.setBackground(c);
		Panel contents = getGraphicsArea();
		contents.setBackground(c);
		repaintAll();
	}

	public void setText(String s) {
		tF.setText(s);
		tF.repaint();
		repaint();
	}

	public String getText() {
		return tF.getText();
	}

	public void setJumpExplanation(String s) {
		explanation.setText(s);
		explanation.repaint();
		repaint();
	}

	public String getJumpExplanation() {
		return explanation.getText();
	}

	/* ============= */
	public void buttonHit(String label, Button button) {
		if (label.equalsIgnoreCase("Show")) {
			getOwnerModule().doCommand("show", null, CommandChecker.defaultChecker);
		}
	}
}

