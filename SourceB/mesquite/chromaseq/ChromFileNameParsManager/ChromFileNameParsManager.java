package mesquite.chromaseq.ChromFileNameParsManager;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import mesquite.batchArch.lib.TemplateRecord;
import mesquite.chromaseq.lib.*;
import mesquite.lib.*;

public class ChromFileNameParsManager extends NameParserManager {
	public String prefDirectoryName = "ChromNameParsingRules";
	CommandRecord commandRec;
	String nameParsingRulesName;
	ChooseNameParsingRuleDLOG chooseNameParsingRuleDialog;

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		loadPreferences();
		this.commandRec = commandRec;
		nameParsingRules = new ListableVector();
		loadNameParsingRules();
		if (getNumRules()<=0) {
			ChromFileNameParsing defaultRule = new ChromFileNameParsing();
			nameParsingRules.addElement(defaultRule, false);
		}
		return true;
	}
	
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "nameParsingRulesName", nameParsingRulesName);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("nameParsingRulesName".equalsIgnoreCase(tag))
			nameParsingRulesName = StringUtil.cleanXMLEscapeCharacters(content);
	}
	/*.................................................................................................................*/
	public ChromFileNameParsing loadNameRulesFile(String cPath, String fileName, boolean requiresEnding,  boolean userDef) {
		File cFile = new File(cPath);
		if (cFile.exists() && !cFile.isDirectory() && (!requiresEnding || fileName.endsWith("xml"))) {
			String contents = MesquiteFile.getFileContentsAsString(cPath);
			if (!StringUtil.blank(contents)) {
				ChromFileNameParsing localNameParsingRules = new ChromFileNameParsing();
				localNameParsingRules.path = cPath;
				if  (localNameParsingRules.readXML(contents, commandRec)) {
					nameParsingRules.addElement(localNameParsingRules, false);
					return localNameParsingRules;
				}
				return null;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	private void loadNameParsingRules(String path, File nameRulesDir, boolean userDef){
		if (nameRulesDir.exists() && nameRulesDir.isDirectory()) {
			String[] nameRulesList = nameRulesDir.list();
			for (int i=0; i<nameRulesList.length; i++) {
				if (nameRulesList[i]==null )
					;
				else {
					String cPath = path + MesquiteFile.fileSeparator + nameRulesList[i];
					loadNameRulesFile(cPath, nameRulesList[i], true, userDef);
				}
			}
		}
	}
	private void loadNameParsingRules(){
		String path = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator + prefDirectoryName;
		File nameRulesDir = new File(path);
		loadNameParsingRules(path, nameRulesDir, true);
	}

	public ChromFileNameParsing chooseNameParsingRules(ChromFileNameParsing rule) {
		ChromFileNameParsing nameParsingRule = rule;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		chooseNameParsingRuleDialog = new ChooseNameParsingRuleDLOG(this, nameParsingRulesName, buttonPressed);
		//chooseNameParsingRuleDialog.completeAndShowDialog();
		boolean ok = (buttonPressed.getValue()==0);
		
		if (ok && choice !=null) {
			nameParsingRulesName = choice.getSelectedItem();
			int sL = nameParsingRules.indexOfByName(nameParsingRulesName);
	  		if (sL >=0 && sL < nameParsingRules.size()) {
	  			nameParsingRule = (ChromFileNameParsing)nameParsingRules.elementAt(sL);
	  		}
	 		storePreferences();
		}
		chooseNameParsingRuleDialog.dispose();
		chooseNameParsingRuleDialog = null;
		return nameParsingRule;
	}

	
	/*.................................................................................................................*/
	public int numNameRules(){
		return nameParsingRules.size();
	}
	/*.................................................................................................................*/
	public MesquiteString getNameRule(String name){
		int i = nameParsingRules.indexOfByName(name);
		if (i<0)
			return null;
		Listable listable = nameParsingRules.elementAt(i);
		if (listable!=null)
			return new MesquiteString(listable.getName());	
		else 
			return null;
	}
	/*.................................................................................................................*/
public int findNameRule(String name){
		return nameParsingRules.indexOfByName(name);
	}
	/*.................................................................................................................*/
public MesquiteString getNameRule(int i){
		if (i<0 || i>= nameParsingRules.size())
			return null;
		Listable listable = nameParsingRules.elementAt(i);
		if (listable!=null)
			return new MesquiteString(listable.getName());	
		else 
			return null;
	}
/*.................................................................................................................*/
private String newNameRulePath(String name){
	String base = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator + prefDirectoryName;
	if (!MesquiteFile.fileExists(base)) {
		File f = new File(base);
		f.mkdir();
	}
	String candidate = base + MesquiteFile.fileSeparator + StringUtil.punctuationToUnderline(name)+ ".xml";
	if (!MesquiteFile.fileExists(candidate))
			return candidate;
	candidate = base + MesquiteFile.fileSeparator  + "nameRule1.xml";
	int count = 2;
	while (MesquiteFile.fileExists(candidate)){
		candidate = base + MesquiteFile.fileSeparator  + "nameRule" + (count++) + ".xml";
	}
	return candidate;
}
/*.................................................................................................................*/
public void addNameRule(ChromFileNameParsing chromFileNameParsing, String name){
	chromFileNameParsing.save(newNameRulePath(name), name);
	nameParsingRules.addElement(chromFileNameParsing, false);	
	choice.add(name);
	nameParsingRulesName = name;
//	return s;
}
/*.................................................................................................................*/
public ChromFileNameParsing duplicateNameRule(ChromFileNameParsing chromFileNameParsing, String name){
	ChromFileNameParsing rule = new ChromFileNameParsing(chromFileNameParsing);
	rule.setName(name);
	rule.setPath(newNameRulePath(name));
	rule.save();
	nameParsingRules.addElement(rule, false);	
	choice.add(name);
	nameParsingRulesName = name;
	return rule;
//	return s;
}
/*.................................................................................................................*/
	void renameNameRule(int i, String name){
		ChromFileNameParsing rule = (ChromFileNameParsing)nameParsingRules.elementAt(i);
		rule.setName(name);
		rule.save();
		choice.remove(i);
		choice.insert(name,i);
		nameParsingRulesName=name;
	}
	/*.................................................................................................................*/
	void deleteNameRule(int i){
		ChromFileNameParsing rule = (ChromFileNameParsing)nameParsingRules.elementAt(i);
		if (rule!=null) {
			String oldTemplateName = rule.getName();
			File f = new File(rule.path);
			f.delete();		
			//MesquiteString s = getNameRule(i);
			//if (s !=null)
			nameParsingRules.removeElement(rule, false);  //deletes it from the vector
			choice.remove(i);
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Chromatogram File Name Rules Manager";
	}

}	
	
	
	
	/*=======================================================================*/
	class ChooseNameParsingRuleDLOG extends ExtensibleListDialog {
		ChromFileNameParsManager ownerModule;
		boolean editLastItem = false;
		
		public ChooseNameParsingRuleDLOG (ChromFileNameParsManager ownerModule, String nameParsingRulesName, MesquiteInteger buttonPressed){
			super(ownerModule.containerOfModule(), "File Name Rules Manager", "File Name Rules", ownerModule.nameParsingRules);
			this.ownerModule = ownerModule;
/*
			String message = "This dialog box allows you to create and edit snippets of code stored in the current file or project, to be inserted into batch files according to their names.";
			message += "  They are used, for instance, in parametric bootstrapping to store commands (specific to this file) that might be placed in the batch file to instruct another program.";
			message += " Code snippets are stored in the Mesquite file, and thus in order to save them, you need to save the Mesquite file.";
			appendToHelpString(message);
			setHelpURL(ownerModule,"");
*/		
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
			return "Chromatogram File Name Rule";
		}
	/*.................................................................................................................*/
	/** this is the name of the class of objects */
		public  String pluralObjectName(){
			return "Chromatogram File Name Rules";
		}
	
		/*.................................................................................................................*/
		public Listable createNewElement(String name, MesquiteBoolean success){
			hide();
			ChromFileNameParsing chromFileNameParsing = new ChromFileNameParsing();
			if (chromFileNameParsing.queryOptions(name)) {
				addNewElement(chromFileNameParsing,name);  //add name to list
				ownerModule.addNameRule(chromFileNameParsing, name);
				if (success!=null) success.setValue(true);
				show();
				return chromFileNameParsing;
				
			}
			else  {
				if (success!=null) success.setValue(false);
				show();
				return null;
			}
		}
	/*.................................................................................................................*/
		public void deleteElement(int item, int newSelectedItem){
			hide();
			ownerModule.deleteNameRule(item);
			show();
		}
	/*.................................................................................................................*/
		public void renameElement(int item, Listable element, String newName){
			ownerModule.renameNameRule(item,newName);
		}
	/*.................................................................................................................*/
		public Listable duplicateElement(String name){
			ChromFileNameParsing rule = ownerModule.duplicateNameRule((ChromFileNameParsing)currentElement, name);
			return rule;
		}
	/*.................................................................................................................*/
		public boolean getEditable(int item){
			return true;
		}
	/*.................................................................................................................*/
		public void editElement(int item){
			hide();
			ChromFileNameParsing rule = ((ChromFileNameParsing)ownerModule.nameParsingRules.elementAt(item));
			if (rule.queryOptions(rule.getName()))
				rule.save();
			show();
		}
	
	
			
		
	}
	
