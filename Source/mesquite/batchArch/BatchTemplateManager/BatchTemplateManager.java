/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.batchArch.BatchTemplateManager; 


import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.JLabel;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.AssociationSource;
import mesquite.batchArch.lib.*;

/* =========================== */
public class BatchTemplateManager extends TemplateManager {
//	public static String defaultMatrixExportFileFormat = "NEXUS file interpeter";
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(mesquite.trees.StoredTrees.StoredTrees.class, getName() + " may need a tree, for example to simulate the matrices.",
		"Stored trees are chosen automatically for this if needed.");
	}
	ListableVector templates;
	String templateName = "Simple List";
	TreeSource treeSourceTask;
	ListableVector fileSpecifics;
	ChooseTemplateDLOG chooseTemplateDialog=null;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		templates = new ListableVector();
		fileSpecifics = new ListableVector();
		loadTemplates();
		return true;
	}
 	public void endJob(){
 		templates.dispose(true);
 		fileSpecifics.dispose(true);
		super.endJob();
 	}
   	public String getExpectedPath(){
		return getPath() + "templates";
  	 }
	/*.................................................................................................................*/
 	/** A method called immediately after the file has been read in.*/
 	public void projectEstablished() {
		MesquiteSubmenuSpec msms = getFileCoordinator().addSubmenu(MesquiteTrunk.editMenu, "Batch File Templates");
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.editMenu, msms, "Edit Batch File Templates...", makeCommand("editTemplates", this));
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.editMenu, msms, "Edit Code Snippets for Batch Files...", makeCommand("editFileSpecifics", this));
		super.projectEstablished();
 	}
 	
	/*.................................................................................................................*/

	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "templateName", templateName);  
		return buffer.toString();
	}
	/*.................................................................................................................*/

	public void processSingleXMLPreference (String tag, String content) {
		if ("templateName".equalsIgnoreCase(tag))
			templateName = StringUtil.cleanXMLEscapeCharacters(content);
	}


	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0)
			templateName = prefs[0];
	}
	/*.................................................................................................................*/
	public TemplateRecord loadTemplateFile(String cPath, String fileName, boolean requiresTemplateEnding,  boolean userDef) {
		File cFile = new File(cPath);
		if (cFile.exists() && !cFile.isDirectory() && (!requiresTemplateEnding || fileName.endsWith("template"))) {
			String contents = MesquiteFile.getFileContentsAsString(cPath);
			if (!StringUtil.blank(contents)) {
				TemplateRecord localTemplate = new TemplateRecord(userDef, this);
				localTemplate.path = cPath;
				if  (localTemplate.fillFromString(contents, parser)) {
					templates.addElement(localTemplate, false);
					return localTemplate;
				}
				return null;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	private void loadTemplates(String path, File templateDir, boolean userDef){
		if (templateDir.exists() && templateDir.isDirectory()) {
			String[] templatesList = templateDir.list();
			for (int i=0; i<templatesList.length; i++) {
				if (templatesList[i]==null )
					;
				else {
					String cPath = path + MesquiteFile.fileSeparator + templatesList[i];
					loadTemplateFile(cPath, templatesList[i], true, userDef);
				}
			}
		}
	}
	private void loadTemplates(){
		String path = getPath() + "templates";
		File templateDir = new File(path);
		loadTemplates(path, templateDir, false);
		path = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator + "BatchTemplates";
		templateDir = new File(path);
		loadTemplates(path, templateDir, true);
	}
	SingleLineTextField treeField;
	/*.................................................................................................................*/
	public TemplateRecord chooseTemplate(Taxa taxa){
		TemplateRecord template = null;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		chooseTemplateDialog = new ChooseTemplateDLOG(this, "Choose Template", templateName, taxa, buttonPressed);
		chooseTemplateDialog.completeAndShowDialog();
		boolean ok = (chooseTemplateDialog.query()==0);
		
		if (ok) {
			templateName = chooseTemplateDialog.templateChoiceGet.getSelectedItem();
			int sL = templates.indexOfByName(templateName);
	  		if (sL >=0 && sL < templates.size()) {
	  			template = (TemplateRecord)templates.elementAt(sL);
	  		}
	 		storePreferences();
		}
		chooseTemplateDialog.dispose();
		chooseTemplateDialog = null;
		return template;
	}
	/*.................................................................................................................*/
	public TemplateRecord getTemplate(String name){
		int sL = templates.indexOfByNameIgnoreCase(name);
  		if (sL >=0 && sL < templates.size())
  			return (TemplateRecord)templates.elementAt(sL);
		return null;
	}

	/*.................................................................................................................*/
	void editFileSpecifics(ChooseTemplateDLOG dlog){
		EditFileSpecificsDLOG dialog = new EditFileSpecificsDLOG(this, dlog);
		if (dlog !=null)
			dlog.resetSnippetList();
		dialog.dispose();
		dialog = null;
	}
	/*.................................................................................................................*/
	public boolean getTreeHasBeenChosen(){
		if (chooseTemplateDialog!=null)
			return chooseTemplateDialog.getTreeHasBeenChosen();
		else
			return false;
	}
	/*.................................................................................................................*/
	public ExtensibleDialog getChooseTemplateDLOG(Taxa taxa, String title, ObjectContainer oC, MesquiteInteger buttonPressed, boolean includeMatrices){
		chooseTemplateDialog = new ChooseTemplateDLOG(this, title, templateName, taxa, buttonPressed);
		chooseTemplateDialog.setChoiceContainer(oC);
		chooseTemplateDialog.setIncludeMatrices(includeMatrices);
		return chooseTemplateDialog;
	}
	/*.................................................................................................................*/
	void editTemplates(ChooseTemplateDLOG dlog){
		//EditTemplatesDLOG dialog = new EditTemplatesDLOG(this, templateName, dlog);
		ManageTemplatesDLOG dialog = new ManageTemplatesDLOG(this, dlog);
		dialog.dispose();
		dialog = null;
	}
	
	private String getTextFromArea(TextArea a){
		int count = 0;
		while (count++<10){
			try {
				String s = a.getText();
				if (s==null)
					return "";
				return s;
		        }
		        catch (Exception e){
		        	//MesquiteMessage.warnProgrammer("Exception in getText of SingleLineTextField");
		        }
	        }
	        return "";
	}
	/*.................................................................................................................*/
	public MesquiteString addFileSpecific(String name, String commands){
		MesquiteString s = new MesquiteString();
		s.setName(name);
		s.setValue(commands);
		fileSpecifics.addElement(s, false);	
		return s;
	}
	public int numFileSpecifics(){
		return fileSpecifics.size();
	}
	public MesquiteString getFileSpecific(String name){
		int i = fileSpecifics.indexOfByName(name);
		if (i<0)
			return null;
		return (MesquiteString)fileSpecifics.elementAt(i);	
	}
	public int findFileSpecific(String name){
		return fileSpecifics.indexOfByName(name);
	}
	public MesquiteString getFileSpecific(int i){
		if (i<0 || i>= fileSpecifics.size())
			return null;
		return (MesquiteString)fileSpecifics.elementAt(i);	
	}
	void renameFileSpecific(int i, String name){
		MesquiteString s = getFileSpecific(i);
		if (s !=null)
			s.setName(name);	
	}
	void deleteFileSpecific(int i){
		MesquiteString s = getFileSpecific(i);
		if (s !=null)
			fileSpecifics.removeElement(s, false);
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	for (int i=0; i<fileSpecifics.size(); i++)
  	 		temp.addLine("fileSpecifics " + StringUtil.tokenize(getFileSpecific(i).getName()) + " " + StringUtil.tokenize(getFileSpecific(i).getValue())); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Adds a file specific set of code", "[name][code]", commandName, "fileSpecifics")) {
			String name = parser.getFirstToken(arguments);
			String co = parser.getNextToken();
			return addFileSpecific(name, co);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Asks to bring up editor of batch file templates", null, commandName, "editTemplates")) {
			editTemplates(null);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Loads template file", null, commandName, "loadTemplates")) {
 	 		loadTemplates(getProject().getHomeDirectoryName(), new File(getProject().getHomeDirectoryName()), true);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Asks to bring up editor of code snippets for batch file", null, commandName, "editFileSpecifics")) {
			editFileSpecifics(null);
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
   	 }
   	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Batch File Template Manager";
   	 }
   	 
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages templates used in saving batch files." ;
   	 }
   	 
   	 Tree tree;
	/*.................................................................................................................*/
   	Tree getTree(Taxa taxa){
		tree = null;
		//if (treeSourceTask == null) Ask Wayne about this
			treeSourceTask= (TreeSource)hireNamedEmployee(TreeSource.class, "#StoredTrees");
		if (treeSourceTask!=null) {
			int treeNum = treeSourceTask.queryUserChoose(taxa, "");
			if (MesquiteInteger.isCombinable(treeNum))
				tree = treeSourceTask.getTree(taxa, treeNum);
		}
		return tree;
   	}
   	public Tree getTree(){
   		return tree;
   	}
   	public void forgetTree(){
   		tree = null;
   	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
}






/*=======================================================================*/
class ChooseTemplateDLOG extends ExtensibleExplDialog  implements ItemListener, ActionListener {//************
	String templateName = "Simple List";
	Taxa taxa = null;
	Tree tree = null;
	TemplateRecord currentTemplate;
	FileCoordinator coord;
	SingleLineTextField treeField;
	Button treeButton;
	Choice templateChoiceGet, fsChoice;
	TextArea fsText;
	JLabel extensionText;
	boolean includeMatrices;
	ObjectContainer chosen;
	String editTempButtonString = "Edit Templates...";
	String editSnipButtonString = "Edit Snippets...";
	String chooseTreeButtonString = "Choose tree...";
	BatchTemplateManager ownerModule;
	public ChooseTemplateDLOG (BatchTemplateManager ownerModule, String title, String initialTemplateName, Taxa taxa, MesquiteInteger buttonPressed){
		super(ownerModule.containerOfModule(), title, buttonPressed);
		this.ownerModule = ownerModule;
		coord = ownerModule.getFileCoordinator();
		
		this.taxa = taxa;
		templateName = initialTemplateName;
	}
	
	public void setIncludeMatrices(boolean includeMatrices){
		this.includeMatrices = includeMatrices;
	}
	
	public void addAuxiliaryDefaultPanels(){ //************
		
		Panel panel;
		
		if (includeMatrices) {
			extensionText = addLabel("");
			extensionText.setFont(defaultSmallFont);
		}

		addHorizontalLine(3);
		addLabel("Batch File Template", Label.LEFT,true,true);

		templateChoiceGet = addPopUpMenu ("Batch File Template", ownerModule.templates.getElementArray(), 0);
		
		boolean noChoiceItems = (templateChoiceGet.getItemCount()<=0);
		templateChoiceGet.addItemListener(this);
		int sL = ownerModule.templates.indexOfByName(templateName);
		if (sL <0) {
			sL = 0;
		}
		
		if (!noChoiceItems)
			templateChoiceGet.select(sL);   

//		suppressNewPanel();

		GridBagConstraints gridConstraints;
		gridConstraints = getGridBagConstraints();
		gridConstraints.fill = GridBagConstraints.NONE;
		setGridBagConstraints(gridConstraints);

//		fsChoice = addPopUpMenu ("CODE SNIPPETS NEEDED", (Listable[])null, 0);
		fsText = addTextAreaSmallFontNoScroll("",4, false);
		fsText.setBackground(fsText.getParent().getBackground());
		fsText.setEditable(false);
		
		suppressNewPanel();
		panel = addNewDialogPanel(gridConstraints);
		Button editSnippetsButton = addAButton(editSnipButtonString, panel);
		editSnippetsButton.addActionListener(this);
		addBlankLine();
		
		
		treeField = addTextField(" ","", 20);
		treeField.setEditable(false);
		treeField.setBackground(treeField.getParent().getBackground());
		
		suppressNewPanel();
		gridConstraints = getGridBagConstraints();
		gridConstraints.fill = GridBagConstraints.NONE;
		setGridBagConstraints(gridConstraints);
		panel = addNewDialogPanel(gridConstraints);
		treeButton = addAButton(chooseTreeButtonString, panel);
		gridConstraints.fill = GridBagConstraints.BOTH;
		setGridBagConstraints(gridConstraints);
		treeButton.addActionListener(this);
		addBlankLine();
		
		setTemplate(sL);
		//addLabel("Explanation of template:",  Label.LEFT, true,false);//************
		super.addAuxiliaryDefaultPanels(); //************

		panel = addNewDialogPanel();
		Button editButton = addAButton(editTempButtonString, panel);
		editButton.addActionListener(this);
		suppressNewPanel();
/*
		panel = addNewDialogPanel();
		Button editSnippetsButton = addAButton(editSnipButtonString, panel);
		editSnippetsButton.addActionListener(this);
*/
		
		addHorizontalLine(1);
	}
	public void actionPerformed(ActionEvent e){
		if (editTempButtonString.equals(e.getActionCommand())) {
			ownerModule.editTemplates(this);
			//setTemplate();
		}
		else if (editSnipButtonString.equals(e.getActionCommand()))
			ownerModule.editFileSpecifics(this);
		else if (chooseTreeButtonString.equals(e.getActionCommand())) {
			tree = ownerModule.getTree(taxa);
			if (tree!=null) {
				treeField.setForeground(treeField.getParent().getForeground());
				treeField.setText(tree.getName());
			}
			else {
				treeField.setText("TREE NEEDED");
				treeField.setForeground(Color.red);
				MesquiteMessage.notifyUser("No tree is available; one must be available before this template can be used.");
			}
		}
		else super.actionPerformed(e);
	}
	/*public void dispose(){
		if (tree != null && ownerModule!=null && !ownerModule.isDoomed())
			ownerModule.forgetTree();
		super.dispose();
	}
/*.................................................................................................................*/
  	public void itemStateChanged(ItemEvent e){
  		if (e.getItemSelectable() == templateChoiceGet){
	  		int item = ownerModule.templates.indexOfByName((String)e.getItem());
	  		if (item>=0) {
	  			setTemplate(item);
	  			prepareDialogHideFirst();
	  		}
  		}
  	}
	/*.................................................................................................................*/
	public boolean dialogValuesAcceptable () {
		boolean acceptable=true;
		if (currentTemplate.needsTree() && (tree==null))
			acceptable = false;
		return acceptable;
	}
	/*.................................................................................................................*/
	public void dialogValuesNotAcceptableWarning () {
		MesquiteMessage.notifyUser("This template requires that a tree is chosen.");
	}
/*.................................................................................................................*/
	public void setChoiceContainer(ObjectContainer oC){
		this.chosen = oC;
	}
/*.................................................................................................................*/
	void addTemplate(String name){
		templateChoiceGet.add(name);
	}
/*.................................................................................................................*/
	void renameTemplate(int item, String newName){
		templateChoiceGet.remove(item);
		templateChoiceGet.insert(newName,item);
		templateName=newName;
	}
/*.................................................................................................................*/
	void removeTemplate(String name){
		templateChoiceGet.remove(name);
	}
/*.................................................................................................................*/
	void selectTemplate(String name){
		templateChoiceGet.select(name);
	}
/*.................................................................................................................*/
	ListableVector getSnippetsNeeded() {
		return currentTemplate.snippetsNeeded();
	}
/*.................................................................................................................*/
	boolean getTreeHasBeenChosen() {
		return (tree!=null);
	}
/*.................................................................................................................*/
	String getNameOfFirstUnavailableSnippetNeeded() {
		ListableVector snippets = currentTemplate.snippetsNeeded();
		if (snippets.size()>0) {
			for (int i=0; i<snippets.size(); i++) {
				String s = ((Listable)snippets.elementAt(i)).getName();
				if (ownerModule.findFileSpecific(s)<0)
					return s;
			}
		}
		return null;
	}
  	void setTemplate(int item){
	  		if (item >=0 && item < ownerModule.templates.size()){
	  			currentTemplate = (TemplateRecord)ownerModule.templates.elementAt(item);
		  			setExplainable(currentTemplate); //************
				if (chosen !=null)
					chosen.setObject(currentTemplate);
				if (currentTemplate.needsTree()) {
					treeButton.setEnabled(true);
					if (tree!=null) {
						treeField.setText(tree.getName());
						treeField.setForeground(treeField.getParent().getForeground());
					}
					else {
						treeField.setText("TREE NEEDED");
						treeField.setForeground(Color.red);
					}
				}
				else {
					treeField.setText("NO TREE NEEDED");
					treeField.setForeground(treeField.getParent().getForeground());
					treeButton.setEnabled(false);
				}
				resetTemplateItems();
				return;
			}
		
  	}
  	public void resetExtensionText(){
		if (includeMatrices) {
			if (currentTemplate!=null) {
				String s = currentTemplate.fileExtension(currentTemplate.matrixExportFormat,coord,true);
				if (StringUtil.blank(s))
					extensionText.setText("");		
				else
					extensionText.setText("The extension \"" + s + "\" will be added to the end of each matrix's file name.");		
			}
			else
				extensionText.setText("");		
			extensionText.invalidate();
			extensionText.validate();
		}
  	}
  	public void resetSnippetList(){
		ListableVector snippets = currentTemplate.snippetsNeeded();
		String snip = "";
		if (snippets.size()>0) {
			boolean snippetUnavailable = false;
			snip = "CODE SNIPPETS NEEDED: ";
			for (int i=0; i<snippets.size(); i++) {
				String s = ((Listable)snippets.elementAt(i)).getName();
				if (ownerModule.findFileSpecific(s)<0) {
					s += " (unavailable)";
					snippetUnavailable = true;
				}
				if (i>0)
					snip += ", " + s;
				else
					snip += s;
			}
			if (snippetUnavailable)
				fsText.setForeground(Color.red);
			else
				fsText.setForeground(fsText.getParent().getForeground());
		}
		else {
			snip ="No Code Snippets needed.";
			fsText.setForeground(fsText.getParent().getForeground());
		}
		fsText.setText(snip);		
		fsText.invalidate();
		fsText.validate();
  	}

	 public void resetTemplateItems(){
	 	resetSnippetList();
	 	resetExtensionText();
	 }

	 public void dispose(){
		if (ownerModule.treeSourceTask != null){
			ownerModule.fireEmployee(ownerModule.treeSourceTask);
			ownerModule.treeSourceTask = null;
		}
		super.dispose();
	  }
}




/*=======================================================================*/
class ManageTemplatesDLOG extends ExtensibleListDialog {
	BatchTemplateManager ownerModule;
	//TemplateRecord currentTemplate;
	ChooseTemplateDLOG chooseDLOG;
	Button loadButton;
	static String loadTemplateString="Load...";

	public ManageTemplatesDLOG (BatchTemplateManager ownerModule,ChooseTemplateDLOG chooseDLOG){
		super(ownerModule.containerOfModule(), "Batch File Template Manager", "Batch File Templates", ownerModule.templates);
		this.ownerModule = ownerModule;
		this.chooseDLOG = chooseDLOG;
		String message = "This dialog box allows you to create and edit templates for batch files.";
		message += "  Batch files are used, for instance, in parametric bootstrapping to instruct another program.";
		message += " Templates you create are stored automatically in files in the Mesquite preferences folder.";

		appendToHelpString(message);
		setHelpURL(ownerModule,"");
		
		completeAndShowDialog("Done", null, null, null);
	}
/*.................................................................................................................*/
/** this is the name of the class of objects */
	public  String objectName(){
		return "Template";
	}
/*.................................................................................................................*/
/** this is the plural name of the class of objects */
	public  String pluralObjectName(){
		return "Templates";
	}
/*.................................................................................................................*/
	public void addAdditionalButtonsBesideList() {
		loadButton = addNewButtonBesideList(loadTemplateString);
	}
/*.................................................................................................................*/
	public void deleteElement(int item, int newSelectedItem){
		TemplateRecord template = (TemplateRecord)ownerModule.templates.elementAt(item);
		String oldTemplateName = template.name;
		File f = new File(template.path);
		f.delete();
		ownerModule.templates.removeElement(template, false);
		if (chooseDLOG !=null) {
			chooseDLOG.removeTemplate(oldTemplateName);
			chooseDLOG.selectTemplate(ownerModule.templates.nameOfElementAt(newSelectedItem));
			chooseDLOG.setTemplate(newSelectedItem);
			chooseDLOG.resetTemplateItems();			
			chooseDLOG.prepareDialogHideFirst();
		}
	}
/*.................................................................................................................*/
	private String newTemplatePath(){
		String base = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator +"BatchTemplates";
		if (!MesquiteFile.fileExists(base)) {
			File f = new File(base);
			f.mkdir();
		}
		String candidate = base + "/t1.template";
		int count = 2;
		while (MesquiteFile.fileExists(candidate)){
			candidate = base + "/t" + (count++) + ".template";
		}
		return candidate;
	}
/*.................................................................................................................*/
	public Listable createNewElement(String name, MesquiteBoolean success){
		String candidate = newTemplatePath();

		MesquiteFile.putFileContents(candidate, "", true); 
		
		TemplateRecord localTemplate = new TemplateRecord(true, ownerModule);
		localTemplate.path = candidate;
		localTemplate.name = name;
		localTemplate.explanation= "";
		for (int i=0; i<localTemplate.getNumFiles(); i++) {
			localTemplate.setStartText("", i);
			localTemplate.setTextEach("", i);
			localTemplate.setEndText("", i);
			localTemplate.setBatchFileName("Batch File " + (i+1), i);
		}
		localTemplate.matrixExportFormat = TemplateRecord.defaultExportFormat;
		ownerModule.templates.addElement(localTemplate, false);
		if (chooseDLOG !=null) {
			chooseDLOG.addTemplate(localTemplate.name);
			chooseDLOG.selectTemplate(localTemplate.name);
			chooseDLOG.setTemplate(ownerModule.templates.indexOfByName(localTemplate.name));
			chooseDLOG.resetTemplateItems();
		}
		//setElement
 		ownerModule.storePreferences();  //storing default template
 		success.setValue(true);
 		return localTemplate;
	}
/*.................................................................................................................*/
	public Listable duplicateElement(String name){
		String candidate = newTemplatePath();
		TemplateRecord templateToDuplicate = (TemplateRecord)currentElement;
		
		TemplateRecord localTemplate = new TemplateRecord(true, ownerModule);
		localTemplate.path = candidate;
		localTemplate.name =  name;
		localTemplate.explanation= templateToDuplicate.explanation;
		localTemplate.matrixExportFormat = templateToDuplicate.matrixExportFormat;
		for (int i=0; i<localTemplate.getNumFiles(); i++) {
			localTemplate.setStartText(templateToDuplicate.getStartText(i), i);
			localTemplate.setTextEach(templateToDuplicate.getTextEach(i), i);
			localTemplate.setEndText(templateToDuplicate.getEndText(i), i);
			localTemplate.setBatchFileName(templateToDuplicate.getBatchFileName(i, null, false), i);
		}

		ownerModule.templates.addElement(localTemplate, false);
		
		String contents = localTemplate.toNEXUSString();
		MesquiteFile.putFileContents(localTemplate.path, contents, true); 
		
		if (chooseDLOG !=null) {
			chooseDLOG.addTemplate(localTemplate.name);
			chooseDLOG.selectTemplate(localTemplate.name);
			chooseDLOG.resetTemplateItems();
		}
 		ownerModule.storePreferences();
 		return localTemplate;
	}
/*.................................................................................................................*/
	public void renameElement(int item, Listable element, String newName){
		TemplateRecord template = (TemplateRecord)element;
		String oldName = template.name;
		template.name = newName;
		String contents = template.toNEXUSString();
		MesquiteFile.putFileContents(template.path, contents, true); 
		((TemplateRecord)ownerModule.templates.elementAt(item)).name = newName;
		if (chooseDLOG !=null) {
			chooseDLOG.renameTemplate(item, newName);
			chooseDLOG.selectTemplate(newName);
		}
	}
/*.................................................................................................................*/
//	public Object duplicateElement(String name){
		/*
		MesquiteString s = new MesquiteString();
		s.setName(name);
		s.setValue(((MesquiteString)currentElement).getValue());
		*/
//		return null;
//	}
/*.................................................................................................................*/
	public boolean getEditable(int item){
		return ((TemplateRecord)ownerModule.templates.elementAt(item)).getUserDefined();
	}
/*.................................................................................................................*/
	public void viewElement(int item){
		EditTemplatesDLOG editDialog = new EditTemplatesDLOG(ownerModule, ((TemplateRecord)currentElement).getName(),chooseDLOG, false);
	}
/*.................................................................................................................*/
	public void editElement(int item){
		EditTemplatesDLOG editDialog = new EditTemplatesDLOG(ownerModule, ((TemplateRecord)currentElement).getName(),chooseDLOG, true);
	}
/*.................................................................................................................*/
  	public void newListElementSelected(){
  		if (currentElement!=null) {
	  		boolean userDef = (((TemplateRecord)currentElement).getUserDefined());
	  		setRenameButtonEnabled(userDef);
	  		setDeleteButtonEnabled(userDef);
	  		if (userDef)
	  			setEditButtonLabel("Edit...");
	  		else
	  			setEditButtonLabel("View...");
	  	}
  	}
/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e){ 
		if (loadTemplateString.equals(e.getActionCommand())) {   // loads template from new file, saves it as user defined
			MesquiteString directoryName= new MesquiteString();
			MesquiteString fileName= new MesquiteString();
			String filePath = MesquiteFile.openFileDialog("Choose Template File...", directoryName, fileName);
			if (!StringUtil.blank(filePath)) {
				TemplateRecord newTemplate = ownerModule.loadTemplateFile(filePath,fileName.getValue(), false,  true);
				
				if (newTemplate!=null) {
					String candidate = newTemplatePath();
					newTemplate.path = candidate;
					String contents = newTemplate.toNEXUSString();   // check name
					MesquiteFile.putFileContents(candidate, contents, true); 
					if (chooseDLOG !=null) {
						chooseDLOG.addTemplate(newTemplate.name);
						chooseDLOG.selectTemplate(newTemplate.name);
						chooseDLOG.setTemplate(ownerModule.templates.indexOfByName(newTemplate.name));
						chooseDLOG.resetTemplateItems();
					}
					addNewElement(newTemplate, newTemplate.name);
					
					if (ownerModule.templates.nameAlreadyInList(newTemplate.name,getList().getSelectedIndex())) {
						MesquiteTrunk.mesquiteTrunk.alert("This name is already used by another template; please pick a unique name.");
						renameCurrentElement(true);
					}

				}
			}

		}
		else
			super.actionPerformed(e);
	}
}






/*=======================================================================*/
class EditTemplatesDLOG extends ExtensibleDialog implements ItemListener {
	String[] startTextString;
	String[] repeatTextString;
	String[] endTextString;
	String[] batchFileNameString;
	TextArea[] startTextArea;
	TextArea[] repeatTextArea;
	TextArea[] endTextArea;
	SingleLineTextField[] batchFileNameField;
	int numFiles;
	
	String matrixExportFormatString = TemplateRecord.defaultExportFormat;
	String templateExplanation;

	TextArea templateExplanationArea;
	Tree tree;
	Taxa taxa;
	boolean editable;
	Choice matrixExportFormatChoice;
	String templateName = "Simple List";
	BatchTemplateManager ownerModule;
	MesquiteInteger buttonPressed;
	TemplateRecord currentTemplate;
	ChooseTemplateDLOG chooseDLOG;
	
	public EditTemplatesDLOG (BatchTemplateManager ownerModule, String templateName, ChooseTemplateDLOG chooseDLOG, boolean editable){
		super(ownerModule.containerOfModule(), "Template Editor");
		this.ownerModule = ownerModule;
		this.chooseDLOG = chooseDLOG;
		this.editable = editable;
		this.templateName = templateName;

		TemplateRecord template =  (TemplateRecord)ownerModule.templates.getElement(templateName);
		numFiles = template.getNumFiles();
		startTextString = new String[numFiles];
		repeatTextString = new String[numFiles];
		endTextString = new String[numFiles];
		batchFileNameString = new String[numFiles];
		startTextArea = new TextArea[numFiles];
		repeatTextArea = new TextArea[numFiles];
		endTextArea = new TextArea[numFiles];
		batchFileNameField = new SingleLineTextField[numFiles];
		
		int sL = ownerModule.templates.indexOfByName(templateName);
		if (sL <0) {
			sL = 0;
		}
		setTemplate(sL);

		addBoldLabel("Batch File Template \""+ templateName + "\"");
		getGridBagConstraints().anchor=GridBagConstraints.CENTER;
		addHorizontalLine(3,18);

		PopUpPanelOfCards cardPanel = addPopUpPanelOfCards("");
		Panel[] cardPanels = new Panel[numFiles];
		for (int i=0; i<numFiles; i++) 
			cardPanels[i] = cardPanel.addNewCard("Batch File #"+(i+1));
		
		getGridBagConstraints().ipadx=0;
		getGridBagConstraints().weightx=1;
		getGridBagConstraints().weighty=1;
		getGridBagConstraints().fill=GridBagConstraints.NONE;
		getGridBagConstraints().anchor=GridBagConstraints.WEST;
		
		for (int i=0; i<numFiles; i++)  {
			getGridBagConstraints().fill=GridBagConstraints.NONE;
			getGridBagConstraints().anchor=GridBagConstraints.WEST;
			setAddPanel(cardPanels[i]);
			
			addLabel(" Text for start of batch file:", Label.LEFT);
			getGridBagConstraints().anchor=GridBagConstraints.CENTER;
			startTextArea[i]= addTextAreaSmallFont(startTextString[i],4);
			startTextArea[i].setEditable(editable);
			startTextArea[i].setColumns(60);
			if (!editable)
				startTextArea[i].setBackground(ColorDistribution.brighter(startTextArea[i].getParent().getBackground(),0.5));
			
			getGridBagConstraints().anchor=GridBagConstraints.WEST;
			addLabel(" Repeated text, written for each replicate:", Label.LEFT);
			getGridBagConstraints().anchor=GridBagConstraints.CENTER;
			repeatTextArea[i] = addTextAreaSmallFont(repeatTextString[i],7);
			repeatTextArea[i].setEditable(editable);
			repeatTextArea[i].setColumns(60);
			if (!editable)
				repeatTextArea[i].setBackground(ColorDistribution.brighter(repeatTextArea[i].getParent().getBackground(),0.5));
			
			getGridBagConstraints().anchor=GridBagConstraints.WEST;
			addLabel(" Text for end of batch file:", Label.LEFT);
			getGridBagConstraints().anchor=GridBagConstraints.CENTER;
			endTextArea[i] = addTextAreaSmallFont(endTextString[i],4);
			endTextArea[i].setEditable(editable);
			endTextArea[i].setRows(4);
			endTextArea[i].setColumns(60);
			if (!editable)
				endTextArea[i].setBackground(ColorDistribution.brighter(endTextArea[i].getParent().getBackground(),0.5));
				
			batchFileNameField[i] = addTextField("Batch File Name: ", batchFileNameString[i], 20);
			batchFileNameField[i].setEditable(editable);
			if (!editable)
				batchFileNameField[i].setBackground(ColorDistribution.brighter(batchFileNameField[i].getParent().getBackground(),0.5));
		}
		
		nullifyAddPanel();
		
		getGridBagConstraints().anchor=GridBagConstraints.CENTER;
		addHorizontalLine(3,18);
		
		cardPanel.showCard(0);
		//((CardLayout)cardPanel.getCardLayout()).first(cardPanel.getCardPanel());
		
/* 
Now find all of the fileinterpreters that can export data.
Eventually change this so that it only lists file interpreters that can export data of the sort being generated.
*/
		MesquiteModule[] fInterpreters = ownerModule.getFileCoordinator().getImmediateEmployeesWithDuty(FileInterpreter.class);
		int count=0;
		
		for (int i=0; i<fInterpreters.length; i++){

		if (((FileInterpreter)fInterpreters[i]).canExportEver())
				count++;
		}
		String [] matrixExportFormatNames = new String[count];
		count = 0;
		for (int i=0; i<fInterpreters.length; i++)
			if (((FileInterpreter)fInterpreters[i]).canExportEver()) {
				matrixExportFormatNames[count] = fInterpreters[i].getName();
				count++;
			}
		matrixExportFormatChoice = addPopUpMenu ("Format of Matrices: ", matrixExportFormatNames, 0);
		matrixExportFormatChoice.select(matrixExportFormatString);
		matrixExportFormatChoice.addItemListener(this);
		
		getGridBagConstraints().fill=GridBagConstraints.NONE;
		getGridBagConstraints().anchor=GridBagConstraints.WEST;
		addLabel(" Explanation of template:", Label.LEFT);
		getGridBagConstraints().anchor=GridBagConstraints.CENTER;
		templateExplanationArea = addTextAreaSmallFont(templateExplanation, 4);
		templateExplanationArea.setEditable(editable);
		templateExplanationArea.setColumns(60);
		if (!editable)
			templateExplanationArea.setBackground(ColorDistribution.brighter(templateExplanationArea.getParent().getBackground(),0.5));
		getGridBagConstraints().fill=GridBagConstraints.BOTH;
		addHorizontalLine(2,18);
		
		String helpString;
		
		if (editable) {
			helpString ="This user-defined template will be the basis of a batch file produced.  The contents of the first text field will be written at the start of the batch file.";
			helpString +=" The second field will be written once for each replicate.  The third field will be written at the end of the batch file.  A second batch file will be written by repeating the fourth field, once for each replicate.";
			helpString +=" Any occurrence of <name> in the repeated fields will be replaced by the base name of the matrix file, <number> by the number of the matrix file.";
			helpString +=" <matrixFormat> will be replaced by the name of the format of exported matrices, if matrices are to be exported.";
			helpString +=" If you wish to include other text automatically, create a code snippet and give it a name.  <snippet>code snippet name</snippet> will then be replaced by the code snippet named \"code snippet name\".";
			helpString +=" To include the text specification of a tree within Mesquite, use <tree>.";
			helpString +="\n\nFor more details, click on the web link button in the dialog box.";
		}
		else {
			helpString ="This predefined template will be the basis of a batch file produced.  The contents of the first text field will be written at the start of the batch file.";
			helpString +=" The second field will be written once for each replicate.  The third field will be written at the end of the batch file.  A second batch file will be written by repeating the fourth field, once for each replicate.";
			helpString +=" Any occurrence of <name> in the repeated fields will be replaced by the base name of the matrix file, <number> by the number of the matrix file.";
			helpString +=" <matrixFormat> will be replaced by the name of the format of exported matrices, if matrices are to be exported.";
			helpString +=" If \"<snippet>code snippet name</snippet>\" occurs in the text, it will then be replaced by the code snippet named \"code snippet name\".";
			helpString +=" <tree> will be replaced by the the text specification of a chosen tree within Mesquite.";
			helpString +="\n\nThis template is not editable, as it is predefined.  However, you can duplicate it, and then edit the duplicate.";
			helpString +="\n\nFor more details, click on the web link button in the dialog box.";
		}
		appendToHelpString(helpString);
		setHelpURL(ownerModule,"");
		
		if (editable) {
			completeAndShowDialog("Save", "Cancel",null,null);
			boolean ok = (query()==0);
			if (ok) 
				saveCurrentTemplate();
		}
		else
			completeAndShowDialog("OK", null,null,"OK");
		
		dispose();
	}
	public EditTemplatesDLOG (BatchTemplateManager ownerModule, String templateName){
		this(ownerModule, templateName, null,((TemplateRecord)ownerModule.templates.getElement(templateName)).getUserDefined());
	}
/*.................................................................................................................*/
  	public void itemStateChanged(ItemEvent e){
  		if (e.getItemSelectable() == matrixExportFormatChoice && !editable){
	  		matrixExportFormatChoice.select(matrixExportFormatString);
  		}
  	}
/*.................................................................................................................*/
	private void saveCurrentTemplate(){

		TemplateRecord localTemplate = (TemplateRecord)ownerModule.templates.getElement(templateName);

		localTemplate.name =  templateName;
		localTemplate.explanation= getTextFromArea(templateExplanationArea);
		for (int i=0; i<numFiles; i++) {
			localTemplate.setStartText(getTextFromArea(startTextArea[i]), i);
			localTemplate.setTextEach(getTextFromArea(repeatTextArea[i]), i);
			localTemplate.setEndText(getTextFromArea(endTextArea[i]), i);
			localTemplate.setBatchFileName(batchFileNameField[i].getText(),i);
		}
//		localTemplate.setBatchFileName(localTemplate.setBatchFileName(), 0);
		localTemplate.matrixExportFormat = matrixExportFormatChoice.getSelectedItem();
//		localTemplate.setBatchFileName("Batch File " + i, 1);
//		localTemplate.setStartText(localTemplate.matrixExportFormat, 1);   // note that this is the only thing that can go into the start text of the second batch file.
//		localTemplate.setTextEach(getTextFromArea(repeatTextArea[1]), 1);
		
		String contents = localTemplate.toNEXUSString();
		MesquiteFile.putFileContents(localTemplate.path, contents, true); 
		if (chooseDLOG !=null) {
			chooseDLOG.selectTemplate(localTemplate.name);
			chooseDLOG.setTemplate(ownerModule.templates.indexOfByName(templateName));
			chooseDLOG.resetTemplateItems();
			chooseDLOG.setDefaultComponent(null);
			chooseDLOG.prepareDialogHideFirst();
		}
 		ownerModule.storePreferences();
	}
	private String getTextFromArea(TextArea a){
		int count = 0;
		while (count++<10){
			try {
				String s = a.getText();
				if (s==null)
					return "";
				return s;
		        }
		        catch (Exception e){
		        	//MesquiteMessage.warnProgrammer("Exception in getText of SingleLineTextField");
		        }
	        }
	        return "";
	}
/*.................................................................................................................*/
  	private void setTemplate(int item){
	  		if (item >=0 && item < ownerModule.templates.size()){
	  			currentTemplate = (TemplateRecord)ownerModule.templates.elementAt(item);
				for (int i=0; i<numFiles; i++) {
					startTextString[i] = currentTemplate.getStartText(i);
					repeatTextString[i] = currentTemplate.getTextEach(i);
					endTextString[i] = currentTemplate.getEndText(i);
					batchFileNameString[i] = currentTemplate.getBatchFileName(i, null, false);
				}
				matrixExportFormatString = currentTemplate.matrixExportFormat;
				templateExplanation = currentTemplate.getExplanation();
				if (StringUtil.blank(matrixExportFormatString))
					matrixExportFormatString = TemplateRecord.defaultExportFormat;
				if (startTextArea[0] !=null){
					for (int i=0; i<numFiles; i++) {
						startTextArea[i].setText(currentTemplate.getStartText(i));
						repeatTextArea[i].setText(currentTemplate.getTextEach(i));
						endTextArea[i].setText(currentTemplate.getEndText(i));
						batchFileNameField[i].setText(currentTemplate.getBatchFileName(i, null, false));
					}
					templateExplanationArea.setText(currentTemplate.getExplanation());
					matrixExportFormatChoice.select(matrixExportFormatString);
					return;
				}
				return;
			}
			for (int i=0; i<numFiles; i++) {
				startTextString[i] = "";
				repeatTextString[i] = "";
				endTextString[i] = "";
				batchFileNameString[i] = "Batch File " + i;
			}
			matrixExportFormatString = TemplateRecord.defaultExportFormat;
			templateExplanation = "";
			if (startTextArea[0] !=null){
				for (int i=0; i<numFiles; i++) {
					startTextArea[i].setText("");
					repeatTextArea[i].setText("");
					endTextArea[i].setText("");
					batchFileNameField[i].setText("Batch File " + i);
				}
				templateExplanationArea.setText("");
				matrixExportFormatChoice.select(matrixExportFormatString);
			}
		
  	}
}





/*=======================================================================*/
class EditFileSpecificsDLOG extends ExtensibleListDialog {
	BatchTemplateManager ownerModule;
	ChooseTemplateDLOG chooseDLOG;
	boolean editLastItem = false;
	
	public EditFileSpecificsDLOG (BatchTemplateManager ownerModule, ChooseTemplateDLOG chooseDLOG){
		super(ownerModule.containerOfModule(), "Code Snippet Manager", "Code Snippets", ownerModule.fileSpecifics);
		this.ownerModule = ownerModule;
		this.chooseDLOG = chooseDLOG;
		String message = "This dialog box allows you to create and edit snippets of code stored in the current file or project, to be inserted into batch files according to their names.";
		message += "  They are used, for instance, in parametric bootstrapping to store commands (specific to this file) that might be placed in the batch file to instruct another program.";
		message += " Code snippets are stored in the Mesquite file, and thus in order to save them, you need to save the Mesquite file.";
		appendToHelpString(message);
		setHelpURL(ownerModule,"");
		
		
		if (chooseDLOG!=null) {
			String s = chooseDLOG.getNameOfFirstUnavailableSnippetNeeded();
			if (s!=null) {
				if (AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Create new snippet?", "This template uses an undefined code snippet called \""+s + "\".  Do you want an empty snippet created with that name?", "Create New", "No", 1)) {
					newElement(s);
					editLastItem = true;
				}
			}
		}
		
		completeAndShowDialog("Done", null, true, null);
					
	}
/*.................................................................................................................*/
 	public void windowOpened(WindowEvent e){
 		if (editLastItem)
			editNumberedElement(getLastItem());
		editLastItem = false;
		super.windowOpened(e);
	}
/*.................................................................................................................*/
/** this is the name of the class of objects */
	public  String objectName(){
		return "Code Snippet";
	}
/*.................................................................................................................*/
/** this is the name of the class of objects */
	public  String pluralObjectName(){
		return "Code Snippets";
	}
/*.................................................................................................................*/
	public Listable createNewElement(String name, MesquiteBoolean success){
		MesquiteString ms = new MesquiteString("");
		ms.setName(name);
		success.setValue(true);
		return ms;
	}
/*.................................................................................................................*/
	public void deleteElement(int item, int newSelectedItem){
		ownerModule.deleteFileSpecific(item);
	}
/*.................................................................................................................*/
	public void renameElement(int item, Listable element, String newName){
		ownerModule.renameFileSpecific(item,newName);
	}
/*.................................................................................................................*/
	public Listable duplicateElement(String name){
		MesquiteString s = new MesquiteString();
		s.setName(name);
		s.setValue(((MesquiteString)currentElement).getValue());
		return s;
	}
/*.................................................................................................................*/
	public boolean getEditable(int item){
		return true;
	}
/*.................................................................................................................*/
	public void editElement(int item){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog editDialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Edit Code of Snippett",  buttonPressed);
		editDialog.addLabel("Snippet \"" + ((MesquiteString)currentElement).getName() + "\"", Label.LEFT);
		TextArea snippetCodeTextArea = editDialog.addTextArea(((MesquiteString)currentElement).getValue(), 3);
		editDialog.completeAndShowDialog(true);
		
		boolean ok = (editDialog.query()==0);
		if (ok) 
			((MesquiteString)currentElement).setValue(snippetCodeTextArea.getText());
			
		editDialog.dispose();
	}
}

