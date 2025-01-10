/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.ExamplesNavigator;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MQTextArea;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MousePanel;
import mesquite.lib.ui.WindowButton;

/* ======================================================================== */

public class ExamplesNavigator extends FileAssistantN  {
	String nextProjectName = null;
	String prevProjectName = null;
	String title = null;
	ExamplesNavigatorWindow npw;
	MesquiteFile file = null;
	//Color bgColor;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		setModuleWindow( npw = new ExamplesNavigatorWindow(this));
		//npw.setMinimalMenus(true);
		if (!MesquiteThread.isScripting()){
			getModuleWindow().setVisible(true);
		}
		makeMenu("Navigator");

		if (MesquiteThread.isScripting() && CommandRecord.getScriptingFileS()!=null) {
			file = CommandRecord.getScriptingFileS();
			file.addListener(this);
		}
		/*
		MesquiteSubmenuSpec mmis = addSubmenu(null, "Background Color", makeCommand("setBackground",  this));
		mmis.setList(ColorDistribution.standardColorNames);
		 */
		addCheckMenuItem(null, "Show filenames", makeCommand("toggleFileNames", this), npw.showFileNames);
		addMenuItem("Add File Link", makeCommand("addFileLink", npw));
		addMenuItem("Add Web Link", makeCommand("addWebLink", npw));
		resetContainingMenuBar();
		resetAllWindowsMenus();
		return true;
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (file == obj)
			windowGoAway(npw);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (npw !=null){
			if (!StringUtil.blank(npw.getNextText()))
				temp.addLine("setNextFileName " + StringUtil.tokenize(npw.getNextText()));
			if (!StringUtil.blank(npw.getPrevText()))
				temp.addLine("setPrevFileName " + StringUtil.tokenize(npw.getPrevText()));
			/*
			if (!StringUtil.blank(npw.getJumpExplanation()))
			   	 		temp.addLine("setExplanation " + StringUtil.tokenize(npw.getJumpExplanation()));
			if (!StringUtil.blank(npw.getExampleTitle()))
			   	 		temp.addLine("setTitle " + StringUtil.tokenize(npw.getExampleTitle()));
			temp.addLine("toggleFileNames " + showFileNames.toOffOnString());
			 */
			temp.addLine("getWindow");
			temp.addLine("tell It");
			Snapshot fromWindow = npw.getSnapshot(file);
			temp.incorporate(fromWindow, true);
			temp.addLine("endTell");
			temp.addLine("showWindow");
		}
		else {
			if (!StringUtil.blank(nextProjectName))
				temp.addLine("setNextFileName " + StringUtil.tokenize(nextProjectName));
		}
		/*if (!StringUtil.blank(npw.getJumpExplanation()))
		   	 		temp.addLine("setExplanation " + StringUtil.tokenize(npw.getJumpExplanation()));
		 */
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the file name to which to jump when the Next button is hit", "[path to file; if relative than to home file of project]", commandName, "setNextFileName")) {
			if (npw == null)
				return null;
			nextProjectName =parser.getFirstToken(arguments);
			npw.setNextText(nextProjectName);
		}
		else if (checker.compare(this.getClass(), "Sets the file name to which to jump when the Prev button is hit", "[path to file; if relative than to home file of project]", commandName, "setPrevFileName")) {
			if (npw == null)
				return null;
			prevProjectName =parser.getFirstToken(arguments);
			npw.setPrevText(prevProjectName);
		}
		else if (checker.compare(this.getClass(), "Sets the title", "[title]", commandName, "setTitle")) {
			if (npw == null)
				return null;
			title =parser.getFirstToken(arguments);
			npw.setExampleTitle(title);
		}
		else if (checker.compare(this.getClass(), "Sets explanation to appear in text edit area", "[explanation string]", commandName, "setExplanation")) {
			if (npw == null)
				return null;
			npw.setJumpExplanation(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to show file names", "[on or off]", commandName, "toggleFileNames")) {
			boolean current = npw.showFileNames.getValue();
			npw.showFileNames.toggleValue(parser.getFirstToken(arguments));
			if (current!=npw.showFileNames.getValue() && npw != null) {
				npw.showFileNames(npw.showFileNames.getValue());
			}
		}
		else if (checker.compare(this.getClass(), "Jumps to next project recorded", null, commandName, "next")) {
			if (npw == null)
				return null;
			nextProjectName = npw.getNextText();
			if (StringUtil.blank(nextProjectName))
				return null;
			while (getProject().developing)
				;
			String openCommand = "openFile ";
			if (StringUtil.startsWithIgnoreCase(nextProjectName,"http") || !getProject().getHomeFile().isLocal())
				openCommand = "openURL ";
			String commands = "newThread; getProjectID; Integer.id *It; tell Mesquite; getWindowAutoShow; String.was *It; windowAutoShow off; closeProjectByID *Integer.id; " + openCommand;
			commands +=  StringUtil.tokenize(MesquiteFile.composePath(getProject().getHomeDirectoryName(), nextProjectName)) + "; ifNotExists It;  showAbout; endIf; windowAutoShow *String.was; endTell;";
			Puppeteer p = new Puppeteer(this);
			MesquiteInteger pos = new MesquiteInteger(0);
			p.execute(getFileCoordinator(), commands, pos, "", false);
			iQuit();
		}
		else if (checker.compare(this.getClass(), "Jumps to previous project recorded", null, commandName, "prev")) {
			if (npw == null)
				return null;
			prevProjectName = npw.getPrevText();
			if (StringUtil.blank(prevProjectName))
				return null;
			while (getProject().developing)
				;


			String openCommand = "openFile ";
			if (StringUtil.startsWithIgnoreCase(prevProjectName,"http") || !getProject().getHomeFile().isLocal())
				openCommand = "openURL ";

			String commands = "newThread; getProjectID; Integer.id *It; tell Mesquite; getWindowAutoShow; String.was *It; windowAutoShow off; closeProjectByID *Integer.id; " + openCommand;
			commands +=  StringUtil.tokenize(MesquiteFile.composePath(getProject().getHomeDirectoryName(), prevProjectName)) + "; ifNotExists It;  showAbout; endIf; windowAutoShow *String.was; endTell;";
			Puppeteer p = new Puppeteer(this);
			MesquiteInteger pos = new MesquiteInteger(0);
			p.execute(getFileCoordinator(), commands, pos, "", false);
			iQuit();
		}
		else if (checker.compare(this.getClass(), "Jumps to project", null, commandName, "fileLink")) {
			if (npw == null)
				return null;
			String path = parser.getFirstToken(arguments);
			if (StringUtil.blank(path))
				return null;
			while (getProject().developing)
				;
			String openCommand = "openFile ";
			if (StringUtil.startsWithIgnoreCase(path,"http") || !getProject().getHomeFile().isLocal())
				openCommand = "openURL ";
			String commands = "newThread; getProjectID; Integer.id *It; tell Mesquite; getWindowAutoShow; String.was *It; windowAutoShow off; closeProjectByID *Integer.id; " + openCommand;
			commands +=  StringUtil.tokenize(MesquiteFile.composePath(getProject().getHomeDirectoryName(), path)) + "; ifNotExists It;  showAbout; endIf; windowAutoShow *String.was; endTell;";
			Puppeteer p = new Puppeteer(this);
			MesquiteInteger pos = new MesquiteInteger(0);
			p.execute(getFileCoordinator(), commands, pos, "", false);
			iQuit();
		}
		else if (checker.compare(this.getClass(), "Requests web page", null, commandName, "showPage")) {
			String targetName = parser.getFirstToken(arguments);
			if (StringUtil.blank(targetName))
				return null;
			String path = null;
			if (StringUtil.startsWithIgnoreCase(targetName,"mesquite:")) {
				path = mesquiteDirectoryPath + targetName.substring(9, targetName.length());
			}
			else if (StringUtil.startsWithIgnoreCase(targetName,"project:")) {
				path = getProject().getHomeDirectoryName() + targetName.substring(8, targetName.length());
			}
			else
				path = targetName;
			showWebPage(path, false);
		}
		/*else if (checker.compare(this.getClass(), "Sets background color of window", "[name of color]", commandName, "setBackground")) {
    	 		Color bc = ColorDistribution.getStandardColor(parser.getFirstToken(arguments));
			if (bc == null)
				return null;
			bgColor = bc;
			if (npw == null)
				return null;
			//npw.setColor(bc);
			if (npw.isVisible())
				npw.repaint();
    	 	}
    	 	else if (checker.compare(this.getClass(), "NOT USED", null, commandName, "makeWindow")) {
    	 	} */
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void shutDown(){
		if (!isDoomed() && npw!=null)
			windowGoAway(npw);
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
	public String getName() {
		return "Examples Navigator";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a Examples Navigator window with explanation and buttons to link to other files";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "New Examples Navigator";
	}
	public boolean isSubstantive(){
		return false;
	}

}

/* ======================================================================== */
class ExamplesNavigatorWindow extends MesquiteWindow implements TextListener {
	//TextField nextText;
	//TextField prevText;
	//TextField titleText;
	String nextName = "Next";
	String prevName = "Previous";
	MesquiteBoolean showFileNames;
	Parser parser = new Parser();
	Panel contents, controls, textFields;
	FieldPanel nextPanel, prevPanel, titlePanel;
	TitlePanel buttons;
	Panel extras;

	//TextField prevLabel;
	//TextField nextLabel;
	Button prev, next;
	TextArea explanation;
	public ExamplesNavigatorWindow(ExamplesNavigator module) {
		super(module, true);
		setWindowSize(220, 260);
		setBackground(ColorDistribution.lightGreen);
		contents = getGraphicsArea();
		contents.setLayout(new BorderLayout());

		explanation= new MQTextArea("", 12, 12, TextArea.SCROLLBARS_VERTICAL_ONLY);
		contents.add("Center", explanation);
		explanation.setBackground(Color.white);
		controls = new MQPanel();
		buttons = new TitlePanel(this);
		controls.setLayout(new BorderLayout());
		controls.setBackground(ColorDistribution.lightGreen);
		buttons.setLayout(new BorderLayout());
		controls.add("North", buttons);
		buttons.add("West", prev = new WindowButton(prevName, this));
		buttons.add("East", next = new WindowButton(nextName, this));
		prev.setBackground(ColorDistribution.lightGreen);
		next.setBackground(ColorDistribution.lightGreen);
		Font df = new Font("Dialog", Font.PLAIN, 12);
		next.setFont(df);
		prev.setFont(df);

		textFields = new MQPanel();

		nextPanel = new FieldPanel(true, nextName, module.nextProjectName, this, null, false);
		nextPanel.setVisible(true);

		prevPanel = new FieldPanel(true, prevName, module.prevProjectName, this, null, false);
		prevPanel.setVisible(true);

		titlePanel = new FieldPanel(false, "Title", module.title, this, null, false);
		titlePanel.setVisible(true);

		textFields.setLayout(new BorderLayout());
		textFields.add("North", titlePanel);
		textFields.add("Center", nextPanel);
		textFields.add("South", prevPanel);
		controls.add("Center", textFields);

		extras = new MQPanel();
		extras.setLayout(extrasLayout = new GridLayout(0, 1));
		extras.setVisible(true);

		contents.add("North", controls);
		contents.add("South", extras);
		textValueChanged(null);
		setWindowSize(220, 260);
		showFileNames = new MesquiteBoolean(true);
		showFileNames(showFileNames.getValue());
		next.setVisible(!StringUtil.blank(nextPanel.getText()));
		prev.setVisible(!StringUtil.blank(prevPanel.getText()));
		resetTitle();

	}
	/*.................................................................................................................*/
	public void setWindowFont(Font fontToSet) {
		super.setWindowFont(fontToSet);
		if (explanation != null && titlePanel != null && nextPanel != null && prevPanel != null){
			explanation.setFont(fontToSet);
			titlePanel.setFont(fontToSet);
			nextPanel.setFont(fontToSet);
			prevPanel.setFont(fontToSet);
		}
	}
	public boolean showInfoTabs(){
		return false;
	}
	public boolean permitViewMode(){
		return false;
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree lists, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Examples Navigator");
	}
	public void windowResized(){
		if (contents != null){
			explanation.setSize(getWidth(), getHeight());
			contents.setSize(getWidth(), getHeight());
			contents.invalidate();
			contents.validate();
		}
		super.windowResized();
	}
	/*.................................................................................................................*/
	/** to be overridden by MesquiteWindows for a text version of their contents*/
	public String getTextContents() {
		return explanation.getText();
	}
	/*.................................................................................................................*/
	/** Gets the minimum height of the content area of the window */
	public int getMinimumContentHeight(){
		return 100;
	}
	void setExampleTitle(String title){
		buttons.setTitle(title);
		titlePanel.setText(title);
	}
	String getExampleTitle(){
		return buttons.title;
	}

	public void textValueChanged(TextEvent e){
		next.setVisible(!StringUtil.blank(nextPanel.getText()));
		prev.setVisible(!StringUtil.blank(prevPanel.getText()));
		prev.setLabel(prevPanel.getLabel());
		next.setLabel(nextPanel.getLabel());
		nextName = nextPanel.getLabel();
		prevName = prevPanel.getLabel();
		buttons.invalidate();
		buttons.validate();
		contents.invalidate();
		contents.validate();
		buttons.setTitle(titlePanel.getText());
		buttons.repaint();
	}

	private void setPrevButtonName(String s){
		prev.setLabel(s);
		prevPanel.setLabel(s);
		prevName = s;
	}
	private void setNextButtonName(String s){
		next.setLabel(s);
		nextPanel.setLabel(s);
		nextName = s;
	}
	void toggleShowFileNames(){
		showFileNames.toggleValue(null);
		showFileNames(showFileNames.getValue());
	}
	public void showFileNames(boolean show){
		nextPanel.setVisible(show);
		prevPanel.setVisible(show);
		titlePanel.setVisible(show);
		textFields.setVisible(show);
		controls.invalidate();
		controls.validate();
		contents.invalidate();
		contents.validate();
	}
	public void setNextText(String s) {
		if (nextPanel == null)
			return;

		nextPanel.setText(s);
		textValueChanged(null);
		nextPanel.repaint();
	}
	public void setPrevText(String s) {
		if (prevPanel == null)
			return;
		prevPanel.setText(s);
		textValueChanged(null);
		prevPanel.repaint();
	}
	public String getNextText() {
		if (nextPanel == null)
			return null;
		return nextPanel.getText();
	}
	public String getPrevText() {
		if (prevPanel == null)
			return null;
		return prevPanel.getText();
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
	public void go(String path, boolean fileLink) {
		if (fileLink)
			getOwnerModule().doCommand("fileLink", StringUtil.tokenize(path), CommandChecker.defaultChecker);
		else
			getOwnerModule().doCommand("showPage", StringUtil.tokenize(path),  CommandChecker.defaultChecker);
	}
	Vector links = new Vector();
	GridLayout extrasLayout;
	boolean editorsVisible = false;
	void toggleShowExtrasEditors(){
		editorsVisible = !editorsVisible;
		for (int i=0; i<links.size(); i++){
			LinkPanel link = (LinkPanel)links.elementAt(i);
			link.pathField.setVisible(editorsVisible);
			link.pathField.invalidate();
			link.pathField.validate();
			link.titleField.setVisible(editorsVisible);
			link.titleField.invalidate();
			link.titleField.validate();
			link.invalidate();
			link.validate();
		}
		extras.invalidate();
		extras.validate();
		contents.invalidate();
		contents.validate();
	}
	void addLink(LinkPanel link){
		links.addElement(link);
		link.setVisible(true);
		link.pathField.setVisible(editorsVisible);
		link.titleField.setVisible(editorsVisible);
		extrasLayout.setRows(extrasLayout.getRows()+1);
		extras.add(link);
		extras.invalidate();
		extras.validate();
		contents.invalidate();
		contents.validate();
	}
	void removeLink(LinkPanel link){
		links.removeElement(link);
		link.setVisible(false);
		extrasLayout.setRows(extrasLayout.getRows()-1);
		extras.remove(link);
		extras.invalidate();
		extras.validate();
		contents.invalidate();
		contents.validate();
	}
	/*=============*/
	public void buttonHit(String label, Button button) {
		if (getOwnerModule()==null)
			return;
		if (label.equalsIgnoreCase(nextName)) {
			getOwnerModule().doCommand("Next", null,  CommandChecker.defaultChecker);
		}
		else if (label.equalsIgnoreCase(prevName)) {
			getOwnerModule().doCommand("Prev", null,  CommandChecker.defaultChecker);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		Snapshot fromWindow = super.getSnapshot(file);
		temp.incorporate(fromWindow, true);
		if (!StringUtil.blank(getJumpExplanation()))
			temp.addLine("setExplanation " + StringUtil.tokenize(getJumpExplanation()));
		if (!StringUtil.blank(getExampleTitle()))
			temp.addLine("setTitle " + StringUtil.tokenize(getExampleTitle()));
		temp.addLine("toggleFileNames " + showFileNames.toOffOnString());
		if (!StringUtil.blank(prevPanel.getLabel()))
			temp.addLine("setPrevButtonName " + StringUtil.tokenize(prevPanel.getLabel()));
		else
			temp.addLine("setPrevButtonName Previous");
		if (!StringUtil.blank(nextPanel.getLabel()))
			temp.addLine("setNextButtonName " + StringUtil.tokenize(nextPanel.getLabel()));
		else
			temp.addLine("setNextButtonName Next");
		if (editorsVisible)
			temp.addLine("setEditorsVisible");
		for (int i=0; i<links.size(); i++){
			LinkPanel link = (LinkPanel)links.elementAt(i);
			if (link.fileLink)
				temp.addLine("addFileLink " + StringUtil.tokenize(link.getTitle()) + "  " + StringUtil.tokenize(link.getPath()));
			else
				temp.addLine("addWebLink " + StringUtil.tokenize(link.getTitle()) + "  " + StringUtil.tokenize(link.getPath()));
		}
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the title", "[title]", commandName, "setTitle")) {
			setExampleTitle(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets editors to be visible", null, commandName, "setEditorsVisible")) {
			editorsVisible = true;
		}
		else if (checker.compare(this.getClass(), "Sets explanation to appear in text edit area", "[explanation string]", commandName, "setExplanation")) {
			setJumpExplanation(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets name of button that is by default 'Previous'", "[name of button]", commandName, "setPrevButtonName")) {
			setPrevButtonName(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets name of button that is by default 'Next'", "[name of button]", commandName, "setNextButtonName")) {
			setNextButtonName(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Adds a link to a web page", "[Title][path]", commandName, "addWebLink")) {
			String t = parser.getFirstToken(arguments);
			addLink(new LinkPanel("Show page", t, "URL",  parser.getNextToken(), this, false));
		}
		else if (checker.compare(this.getClass(), "Adds a link to a file", "[Title][path]", commandName, "addFileLink")) {
			String t = parser.getFirstToken(arguments);
			addLink(new LinkPanel("Go", t, "Path to file",  parser.getNextToken(), this, true));
		}

		else if (checker.compare(this.getClass(), "Sets whether or not to show file names", "[on or off]", commandName, "toggleFileNames")) {
			boolean current = showFileNames.getValue();
			showFileNames.toggleValue(parser.getFirstToken(arguments));
			if (current!=showFileNames.getValue()) {
				showFileNames(showFileNames.getValue());
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
}

class FieldPanel extends MQPanel implements ActionListener {
	TextField label, text; 
	LinkPanel linkPanel;
	public FieldPanel (boolean labelEditable, String initialLabel, String initialText, TextListener listener, LinkPanel linkPanel, boolean removeButton){
		setLayout(new BorderLayout());
		this.linkPanel = linkPanel;
		setBackground(Color.white);
		label= new TextField(9);
		label.setEditable(false);
		if (!StringUtil.blank(initialLabel))
			label.setText(initialLabel);
		label.setEditable(labelEditable);
		if (labelEditable && listener !=null)
			label.addTextListener(listener);
		text= new TextField(400);
		if (!StringUtil.blank(initialText))
			text.setText(initialText);
		text.setEditable(true);
		text.setBackground(Color.white);
		if (listener !=null)
			text.addTextListener(listener);
		add("West", label);
		add("Center", text);
		if (removeButton){
			Button q =new Button("Remove");
			q.addActionListener(this);
			add("East", q);
		}
		text.setVisible(true);
	}
	public void setText(String s){
		text.setText(s);
	}
	public String getText(){
		return text.getText();
	}
	public String getLabel(){
		return label.getText();
	}
	public void setLabel(String s){
		label.setText(s);
	}
	public void actionPerformed(ActionEvent e){
		linkPanel.remove();
	}

}


class LinkPanel extends MQPanel implements ActionListener, TextListener {
	String title;
	String path;
	LinkBanner banner;
	Button b;
	boolean fileLink;
	FieldPanel titleField, pathField;
	ExamplesNavigatorWindow window;
	public LinkPanel(String buttonLabel, String initTitle, String pathLabel, String initPath, ExamplesNavigatorWindow window, boolean fileLink){
		super();
		this.window = window;
		this.fileLink = fileLink;
		setLayout(new BorderLayout());
		title = initTitle;
		path = initPath;
		titleField = new FieldPanel(false, "Title", initTitle, this, this, true);
		titleField.setVisible(false);
		pathField = new FieldPanel(false, pathLabel, initPath, this, this, false);
		pathField.setVisible(false);

		banner = new LinkBanner(this);
		if (fileLink)
			banner.setBackground(ColorDistribution.veryLightGreen);
		else
			banner.setBackground(Color.cyan);

		b = new Button(buttonLabel);
		banner.add("West", b);
		b.addActionListener(this);

		add("North", banner);
		add("Center", titleField);
		add("South", pathField);

	}
	public void actionPerformed(ActionEvent e){
		window.go(path, fileLink);
	}
	void remove(){
		window.removeLink(this);
	}
	public void textValueChanged(TextEvent e){
		title = titleField.getText();
		path = pathField.getText();
		banner.repaint();
	}
	public void setTitle(String title){
		this.title = title;
	}
	public String getTitle(){
		return title;
	}
	public void setPath(String path){
		this.path = path;
	}
	public String getPath(){
		return path;
	}
	public void toggleShowEditor(){
		window.toggleShowExtrasEditors();
	}

}

class LinkBanner extends MousePanel {
	LinkPanel parent;
	public LinkBanner(LinkPanel parent){
		super();
		setLayout(new BorderLayout());
		this.parent = parent;
		setFont(new Font ("SanSerif", Font.PLAIN, 14));
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteEvent.commandOrControlKeyDown(modifiers))
			parent.toggleShowEditor();
	}
	public void paint (Graphics g){
		if (parent.getTitle()!=null)
			g.drawString(parent.getTitle(), StringUtil.getStringCenterPosition(parent.getTitle(), g, 0, getBounds().width, null), getBounds().height - 8);
	}
}

class TitlePanel extends MousePanel {
	String title;
	ExamplesNavigatorWindow window;
	public TitlePanel(ExamplesNavigatorWindow window){
		this.window = window;
		setFont(new Font ("SanSerif", Font.PLAIN, 14));
	}
	void setTitle(String title){
		this.title = title;
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteEvent.commandOrControlKeyDown(modifiers))
			window.toggleShowFileNames();
	}
	public void paint (Graphics g){
		if (title!=null)
			g.drawString(title, StringUtil.getStringCenterPosition(title, g, 0, getBounds().width, null), getBounds().height - 8);
	}
}
/*
class FileELink extends FileElement{
	ExamplesNavigator ownerModule;
	public FileELink(ExamplesNavigator ownerModule){
		this.ownerModule = ownerModule;
	}
	public void dispose(){
		ownerModule.shutDown();
		super.dispose();
	}
}
 */
