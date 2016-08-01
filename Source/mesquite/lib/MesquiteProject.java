/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

Modifications:
28 July 01 (WPM) getCompatibleFileElements added
 */
package mesquite.lib;

import java.awt.*;
import java.util.*;
import java.net.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
/** An object of this class represents a collection of information (data matrices, tree blocks, etc.) that may reside in more than one
file (MesquiteFile, which represents files on disk or at a URL).  More than one project can be open at once.  Information from different projects
generally doesn't interact (e.g., a data matrix from one project can't belong to a set of taxa from another project), but within a single project
information can interact regardless of what file the information belongs to.<p>
Although more than one file can belong to the project, the first file opened is treated as the "home file".  In the future the idea of a "project" may be made stronger,
and a separate sort of file (the project file) may be created.  tHis would eliminate the need for a "home file".
 */
public class MesquiteProject extends Attachable implements Listable, MesquiteListener, Commandable, HNode, HTMLDescribable {
	LeakFinder leakFinder = MesquiteTrunk.leakFinderObject;
	/** A vector of the sets of taxa within the project.*/
	public ListableVector taxas;  
	/** Vector of data sets (including matrix) within the project. */
	public ListableVector treeVectors;  
	/** Vector of tree vectors within the project. */
	public ListableVector datas;  
	/** Character models belonging to the project.*/
	protected ModelVector charModels; 
	/** Other file elements belonging to the project.*/
	protected ListableVector otherElements; 

	/** frame for this project */
	private MesquiteFrame frame = null;

	/** Nexus blocks belonging to the files in the project.*/
	protected ListableVector nexusBlocks; 
	/** */
	CentralModelListener modelListener;
	public boolean virginProject = true;
	public long startupTime = 0;
	public MesquiteWindow windowToActivate, activeWindowOfProject;
	/** The files belonging to the project. */
	ListableVector files;
	/** The home file of the project. */
	MesquiteFile homeFile;

	/** Commands used with standard Close, Save and Save As...and other menu items*/
	private MesquiteCommand closeCommand, closeFilesCommand, showFileOnDiskCommand, saveCommand, saveFilesCommand, saveAsCommand, saveLinkagesCommand, revertCommand, linkFileCommand, linkURLCommand, includeFileCommand, includeURLCommand;
	public MesquiteCommand exportCommand, newLinkFileCommand, getInfoCommand;
	/** The file coordinator module that owns this object*/
	public FileCoordinator ownerModule=null; 

	/** Background color scheme for project's windows*/ 
	public int projectColor;  

	public static int totalCreated = 0;  // a static counter of the number of projects that have been opened
	public static int totalDisposed = 0;  // to catch memory leaks
	public static int totalFinalized = 0;  // to catch memory leaks
	private int idNumber;
	public boolean developing = true;
	public boolean autosave = false;
	public boolean isDoomed = false;
	public long timePreviouslySavedAsRecorded = 0;
	public long timePreviouslySavedByFile = 0;

	public MesquiteProject(FileCoordinator ownerModule) {
		this.ownerModule = ownerModule;
		startupTime = System.currentTimeMillis();
		datas = new ListableVector();  
		taxas = new ListableVector();  
		charModels = new ModelVector();
		datas.addListener(this);
		taxas.addListener(this);
		charModels.addListener(this);
		modelListener = new CentralModelListener();
		treeVectors = new ListableVector();
		otherElements = new ListableVector();
		otherElements.setName("other file elements in project");
		files = new ListableVector();
		nexusBlocks = new ListableVector( 1);
		idNumber = totalCreated;
		totalCreated++;


		projectColor = MesquiteTrunk.mesquiteTrunk.projects.requestNextColor();

		closeCommand= MesquiteModule.makeCommand("closeFile", ownerModule);
		closeFilesCommand= MesquiteModule.makeCommand("closeProject", ownerModule);
		showFileOnDiskCommand = MesquiteModule.makeCommand("showFileOnDisk", ownerModule);

		saveCommand= MesquiteModule.makeCommand("saveFile", ownerModule);
		saveFilesCommand= MesquiteModule.makeCommand("saveFiles", ownerModule);
		saveAsCommand= MesquiteModule.makeCommand("saveFileAs", ownerModule);
		revertCommand= MesquiteModule.makeCommand("revert", ownerModule);
		exportCommand= MesquiteModule.makeCommand("export", ownerModule);
		getInfoCommand= MesquiteModule.makeCommand("getInfo", ownerModule);
		saveLinkagesCommand= MesquiteModule.makeCommand("saveLinkagesAs", ownerModule);
		linkFileCommand = MesquiteModule.makeCommand("linkFile", ownerModule);
		newLinkFileCommand = MesquiteModule.makeCommand("newLinkedFile", ownerModule);
		linkURLCommand = MesquiteModule.makeCommand("linkURL", ownerModule);
		includeFileCommand = MesquiteModule.makeCommand("includeFile", ownerModule);
		includeURLCommand = MesquiteModule.makeCommand("includeURL", ownerModule);
	}

	public void refreshProjectWindow(){
		if (refreshSuppression == 0){
			refreshPending = false;
			if (ownerModule != null)
				ownerModule.refreshProjectWindow();
		}
		else {
			refreshPending = true;
		}
	}
	public void repaintProjectWindow(){
		if (ownerModule != null && ownerModule.getModuleWindow() != null)
			ownerModule.getModuleWindow().repaintAll();
	}
	public void showProjectWindow(){
		if (ownerModule != null)
			ownerModule.showProjectWindow();
	}

	public int refreshSuppression = 0;
	public boolean refreshPending = false;
	/*.................................................................................................................*/
	/** Increments suppression level of menus; if 0 then menus can be reset. */
	public void incrementProjectWindowSuppression(){
		refreshSuppression++;

		if (refreshSuppression ==0)
			refreshSuppression = 1;
	}
	/*.................................................................................................................*/
	/** Decrements suppression level of menus to zero; then menus can be reset. */
	public void zeroProjectWindowSuppression(){
		refreshSuppression =1;
		decrementProjectWindowSuppression();
	}
	/*.................................................................................................................*/
	/** Decrements suppression level of menus; if 0 then menus can be reset. */
	public void decrementProjectWindowSuppression(){
		if (refreshSuppression ==0) {
			MesquiteMessage.warnProgrammer("decrementProjectWindowSuppression when already zero");
			return;
		}
		refreshSuppression--;
		if (refreshSuppression<0)
			refreshSuppression =0;
		if (refreshSuppression ==0){  //menu suppression just removed and requests pending; reset menus
			if (refreshPending) {
				if (ownerModule != null)
					ownerModule.refreshProjectWindow();
			}
		}
	}

	public String toHTMLStringDescription(){

		String s = "";
		if (getHomeFileName() == null)
			s += "<h2>Project (without home file)</h2>";
		else
			s +="<h2>Project with home file " + getHomeFileName() + "</h2>";
		if (getHomeDirectoryName() != null)
			s += "Location: in directory <a href = \"showDirectory:\">" + getHomeDirectoryName() + "</a><p>";
		if (files.size()>1){

			s += "<hr><h3>Linked files</h3>";
			for (int i = 0; i<files.size(); i++){
				MesquiteFile file = (MesquiteFile)files.elementAt(i);
				if (file != homeFile)
					s += "<strong>" + file.getName() + "</strong> at " + file.getDirectoryName();
			}
			s += "<hr>";
		}
		if (taxas.size()>0){
			s += "<ul>";
			for (int i = 0; i< taxas.size(); i++){
				Taxa t = (Taxa)taxas.elementAt(i);
				s += t.toHTMLStringDescription(); 
				if (getNumberCharMatrices(t)>0){
					s += "<ul>";
					for (int k = 0; k<getNumberCharMatrices(t); k++){
						CharacterData data = getCharacterMatrix(t, k);
						s += data.toHTMLStringDescription();
					}
					s += "</ul>";
				}
				if (getNumberOfFileElements(TreeVector.class)>0){
					String tr = "";
					for (int k = 0; k<getNumberOfFileElements(TreeVector.class); k++){
						TreeVector trees = (TreeVector)getFileElement(TreeVector.class, k);
						if (trees.getTaxa() == t)
							tr += trees.toHTMLStringDescription();
					}
					if (!StringUtil.blank(tr)){
						s += "<ul>" + tr + "</ul>";
					}
				}

			}
			s += "</ul>";
		}
		return s;
	}

	/*.................................................................................................................*/
	/** returns the Trees with given id number */
	public TreeVector getTreesByID(long id) {
		//second look for match with .# file number
		for (int i=0; i<getNumberOfFileElements(TreeVector.class); i++) {
			TreeVector trees = (TreeVector)getFileElement(TreeVector.class, i);
			if (id == trees.getID())
				return trees;
		}
		return null;
	}

	/*.................................................................................................................*/
	/** returns the number of Tree Vectors */
	public int getNumberTreeVectors() {
		return treeVectors.size();
		/*ListableVector v = new ListableVector();		
		for (int i=0; i<getNumberOfFileElements(TreeVector.class); i++) {
			TreeVector trees = (TreeVector)getFileElement(TreeVector.class, i);
			if (!trees.isDoomed())
				v.addElement(trees, false);
		}
		return v;*/
	}
	/*.................................................................................................................*/
	/** returns the Tree Vectors */
	public ListableVector getTreeVectors() {
		return treeVectors;
		/*ListableVector v = new ListableVector();		
		for (int i=0; i<getNumberOfFileElements(TreeVector.class); i++) {
			TreeVector trees = (TreeVector)getFileElement(TreeVector.class, i);
			if (!trees.isDoomed())
				v.addElement(trees, false);
		}
		return v;*/
	}

	/*.................................................................................................................*/
	public String searchData(String s, MesquiteString commandResult) {
		if (StringUtil.blank(s))
			return "<h2>Nothing to search for (searched: \"" + s + "\")</h2>";
		if (commandResult != null)
			commandResult.setValue((String)null);
		String list = "";
		String cR;
		if (taxas.size()>0){
			for (int i = 0; i< taxas.size(); i++){
				Taxa t = (Taxa)taxas.elementAt(i);
				cR = t.searchData(s, null);
				if (!StringUtil.blank(cR))
					list += cR;
				if (getNumberCharMatrices(t)>0){

					for (int k = 0; k<getNumberCharMatrices(t); k++){
						CharacterData data = getCharacterMatrix(t, k);
						cR = data.searchData(s, null);
						if (!StringUtil.blank(cR))
							list += cR;
					}
				}
				if (getNumberOfFileElements(TreeVector.class)>0){
					String tr = "";
					for (int k = 0; k<getNumberOfFileElements(TreeVector.class); k++){
						TreeVector trees = (TreeVector)getFileElement(TreeVector.class, k);
						if (trees.getTaxa() == t){
							cR = trees.searchData(s, null);
							if (!StringUtil.blank(cR))
								list += cR;
						}
					}
				}
			}
		}
		if (charModels.size()>0){
			for (int k = 0; k<charModels.size(); k++){
				FileElement fe = (FileElement)charModels.elementAt(k);
				cR = fe.searchData(s, null);
				if (!StringUtil.blank(cR))
					list += cR;
			}
		}
		if (treeVectors.size()>0){
			for (int k = 0; k<treeVectors.size(); k++){
				FileElement fe = (FileElement)treeVectors.elementAt(k);
				cR = fe.searchData(s, null);
				if (!StringUtil.blank(cR))
					list += cR;
			}
		}
		if (otherElements.size()>0){
			for (int k = 0; k<otherElements.size(); k++){
				FileElement fe = (FileElement)otherElements.elementAt(k);
				cR = fe.searchData(s, null);
				if (!StringUtil.blank(cR))
					list += cR;
			}
		}
		if (StringUtil.blank(list))
			return "<h2>No matches found (searched: \"" + s + "\")</h2>";

		return "<h2>Matches to search string in " + getName() + ": \"" + s + "\"</h2>" + list;
	}
	public void setFrame(MesquiteFrame f){
		frame = f;
	}
	public MesquiteFrame getFrame(){
		return frame;
	}
	/*.................................................................................................................*/
	/** instantiations of projects are numbered sequentially so they can be referred to by number*/
	public long getID(){
		return idNumber;
	}

	/*.................................................................................................................*/
	/** Disposes of the components of the project. */
	public void dispose(){  //TODO: should all the dispose methods be renamed to something  else???
		isDoomed = true;


		for (int i=0; i< files.size(); i++) {
			((MesquiteFile)files.elementAt(i)).projectClosing = true;
			((MesquiteFile)files.elementAt(i)).close();
		}
		windowToActivate = null;
		activeWindowOfProject = null;

		files.dispose(true);
		taxas.dispose(true);
		datas.dispose(true);
		charModels.dispose(true);
		treeVectors.dispose(true);
		otherElements.dispose(true);
		nexusBlocks.dispose(true);
		files = null;
		taxas = null;
		datas = null;
		charModels = null;
		otherElements = null;
		treeVectors = null;
		nexusBlocks = null;
		frame = null;

		ownerModule = null; // to minimize chance of memory leaks
		closeCommand= null;
		closeFilesCommand= null;
		saveCommand= null;
		saveFilesCommand= null;
		saveAsCommand= null;
		exportCommand= null;
		getInfoCommand= null;
		saveLinkagesCommand= null;
		linkFileCommand =null;
		newLinkFileCommand = null;
		linkURLCommand = null;
		includeFileCommand = null;
		includeURLCommand = null;
		totalDisposed++;

	}
	public void finalize() throws Throwable {
		totalFinalized++;
		super.finalize();
	}
	/*.................................................................................................................*/
	/** returns whether any files are dirty */
	public boolean isDirty(){  
		Enumeration enumeration=files.elements();
		MesquiteFile fi;
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof MesquiteFile) {
				fi = (MesquiteFile)obj;
				if (fi.isDirty())
					return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (file == null || file != getHomeFile())
			return null;
		boolean saveSnapAttach = anyAttachments();
		if (name != null || saveSnapAttach) {
			Snapshot temp = new Snapshot();
			if (name != null)
				temp.addLine("setName " + ParseUtil.tokenize(name));
			if (saveSnapAttach)
				temp.addLine("attachments "+ writeAttachments(false));
			return temp;
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Here as a placeholder; doesn't do anything at the moment. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns coordinator module of project", null, commandName, "getCoordinatorModule")) {
			return ownerModule;
		}
		else if (checker.compare(this.getClass(), "Stores attachments", null, commandName, "attachments")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			ParseUtil.getToken(arguments, pos);
			readAttachments(arguments, pos);
		}
		else if (checker.compare(this.getClass(), "Sets name", null, commandName, "setName")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			String n = ParseUtil.getToken(arguments, pos);
			if (n == null && !MesquiteThread.isScripting())
				n = MesquiteString.queryString(ownerModule.containerOfModule(), "Name of Project", "Name of Project", getName());
			if (n != null)
				name = n;
			if (!MesquiteThread.isScripting()){
				MesquiteWindow.resetAllTitles();
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Returns the background color scheme for the project's windows.  This is 0, unless multiple projects are open
	simultaneously, in which case other colors are used as well (to distinguish windows belonging to different projects). */
	public int getProjectColor(){
		return projectColor;
	}
	/* ---------------------------- DIRECTORIES/NAMES ------------------------------*/

	/** Returns name of directory of home file. */
	public String getHomeDirectoryName() {
		if (getHomeFile()==null)
			return null;
		return getHomeFile().getDirectoryName();
	}
	/*.................................................................................................................*/
	/** Returns name of home file. */
	public String getHomeFileName() {
		if (getHomeFile()==null)
			return null;
		return getHomeFile().getFileName();
	}
	/*.................................................................................................................*/
	/** Returns the name of the project.  Currently (temporarily?), the name is based upon the name of the home file. */
	public String getName() {  
		if (name != null)
			return name;
		if (StringUtil.blank(getHomeFileName()))
			return "Project";
		return "Project of \"" + getHomeFileName() + "\"";
	}
	/*.................................................................................................................*/
	String name = null;
	/** Sets the name of the project.   */
	public void setName(String n) { 
		name = n;
	}
	public boolean hasName(){
		return name != null;
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	public URL getHomeURL() {
		if (getHomeFile()==null)
			return null;
		return getHomeFile().getURL();
	}
	/* ---------------------------- FILES/DIRECTORES ------------------------------*/
	/*.................................................................................................................*/
	/** DOCUMENT */
	public void setHomeFile(MesquiteFile file){
		homeFile = file;
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	public MesquiteFile getHomeFile(){
		return homeFile;
	}
	/*.................................................................................................................*/
	/*  writes backup to same directory as home file but with suffix appended to filename.  Returns path to backup*/
	public String writeBackupOfHomeFile(String suffix){
		String ac = homeFile.getAutoCopySuffix();
		homeFile.setWriteAsAutoCopy(suffix);
		ownerModule.writeFile(homeFile);
		String name = homeFile.getTempAutoCopyName();
		homeFile.setWriteAsAutoCopy(ac);
		return name;
	}
	/* Loads backup at path as project with same name and location as current.*/
	public void revertToBackupOfHomeFile(String path){
		ownerModule.closeWindows();
		String oldHomeFileName = getHomeFileName();
		String dir = getHomeDirectoryName();
		MesquiteProject newProj = MesquiteTrunk.mesquiteTrunk.newProject(path, 1, true);
		if (newProj == null)
			return;

		newProj.getHomeFile().changeLocation(dir, oldHomeFileName);
		MesquiteFile.deleteFile(dir+ oldHomeFileName);
		MesquiteFile.rename(path, dir+ oldHomeFileName);
		ownerModule.iQuit();
	}
	/*.................................................................................................................*/
	/** add a file to list of currently linked files */
	public void addFile(MesquiteFile file){
		files.addElement(file, true);
		refreshProjectWindow();
	}
	/*.................................................................................................................*/
	/** remove a file from list of currently linked files */
	public void removeFile(MesquiteFile file){
		removeNexusBlocks(file);
		files.removeElement(file, true);
		refreshProjectWindow();
	}
	/*.................................................................................................................*/
	/** Puts all file elements belonging to passed file into home file, and gets rid of record of file. */
	public void mergeFile(MesquiteFile file){
		if (file==homeFile)
			return;
		if (files.indexOf(file)<0)
			return;
		//first, all file elements move to other home file
		Enumeration enumeration = datas.elements();
		while (enumeration.hasMoreElements()){
			FileElement obj = (FileElement)enumeration.nextElement();
			if (obj.getFile() == file) {
				obj.setFile(homeFile, false);
				homeFile.addFileElement(obj);
			}
		}
		enumeration = taxas.elements();
		while (enumeration.hasMoreElements()){
			FileElement obj = (FileElement)enumeration.nextElement();
			if (obj.getFile() == file) {
				obj.setFile(homeFile, false);
				homeFile.addFileElement(obj);
			}
		}
		enumeration = treeVectors.elements();
		while (enumeration.hasMoreElements()){
			FileElement obj = (FileElement)enumeration.nextElement();
			if (obj.getFile() == file) {
				obj.setFile(homeFile, false);
				homeFile.addFileElement(obj);
			}
		}
		enumeration = charModels.elements();
		while (enumeration.hasMoreElements()){
			FileElement obj = (FileElement)enumeration.nextElement();
			if (obj.getFile() == file) {
				obj.setFile(homeFile, false);
				homeFile.addFileElement(obj);
			}
		}
		enumeration = otherElements.elements();
		while (enumeration.hasMoreElements()){
			FileElement obj = (FileElement)enumeration.nextElement();
			if (obj.getFile() == file) {
				obj.setFile(homeFile, false);
				homeFile.addFileElement(obj);
			}
		}
		removeFile(file);
	}
	/*.................................................................................................................*/
	/** returns number of currently linked files */
	public int getNumberLinkedFiles(){
		if (files == null)
			return 0;
		return files.size();
	}
	/*.................................................................................................................*/
	/** returns the index of a currently linked file */
	public int getFileNumber(MesquiteFile f){
		if (f == null || files == null)
			return -1;
		return files.indexOf(f);
	}
	/*.................................................................................................................*/
	/** returns a currently linked file */
	public MesquiteFile getFile(String name){
		return (MesquiteFile)files.getElement(name);
	}
	/*.................................................................................................................*/
	/** returns a currently linked file */
	public MesquiteFile getFile(int num){
		if (num<0 || files == null || num>= files.size())
			return null;
		return (MesquiteFile)files.elementAt(num);
	}
	/*.................................................................................................................*/
	/** returns a currently linked file */
	public MesquiteFile getFileByID(int id){  //TODO: FIND BY id number!!!
		Enumeration enumeration=files.elements();
		MesquiteFile fi;
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof MesquiteFile) {
				fi = (MesquiteFile)obj;
				if (fi.getID()==id) {
					return fi;
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns currently linked files */
	public ListableVector getFiles(){
		return files;
	}

	/*.................................................................................................................*/
	/** Select one of the currently linked files; selects first if only one or if scripting. 12 Jan 02 */
	public MesquiteFile chooseFile( String message){
		if (getNumberLinkedFiles()==1 || MesquiteThread.isScripting())
			return getHomeFile();
		else 
			return (MesquiteFile)ListDialog.queryList(ownerModule.containerOfModule(), "Select file", message, MesquiteString.helpString, files, 0);
	}
	/* ---------------------------- COMMANDS ------------------------------*/
	/*.................................................................................................................*/
	/** returns command to close a file*/
	public MesquiteCommand getCloseCommand(){
		return closeCommand;
	}
	/*.................................................................................................................*/
	/** returns command to close project (linked files)*/
	public MesquiteCommand getCloseFilesCommand(){
		return closeFilesCommand;
	}
	/*.................................................................................................................*/
	/** returns command to close a file*/
	public MesquiteCommand getShowFileOnDiskCommand(){
		return showFileOnDiskCommand;
	}

	/*.................................................................................................................*/
	/** returns command to save a file*/
	public MesquiteCommand getSaveCommand(){
		return saveCommand;
	}
	/*.................................................................................................................*/
	/** returns command to save project (linked files)*/
	public MesquiteCommand getSaveFilesCommand(){
		return saveFilesCommand;
	}
	/*.................................................................................................................*/
	/** returns command to revert home file to that saved*/
	public MesquiteCommand getRevertCommand(){
		return revertCommand;
	}
	/*.................................................................................................................*/
	/** returns command to link file*/
	public MesquiteCommand getLinkFileCommand(){
		return linkFileCommand;
	}
	/*.................................................................................................................*/
	/** returns command to make new linked file*/
	public MesquiteCommand getNewLinkFileCommand(){
		return newLinkFileCommand;
	}

	/*.................................................................................................................*/
	/** returns command to link file*/
	public MesquiteCommand getLinkURLCommand(){
		return linkURLCommand;
	}
	/*.................................................................................................................*/
	/** returns command to incorporate file*/
	public MesquiteCommand getIncludeFileCommand(){
		return includeFileCommand;
	}
	/*.................................................................................................................*/
	/** returns command to incorporate file*/
	public MesquiteCommand getIncludeURLCommand(){
		return includeURLCommand;
	}
	/*.................................................................................................................*/
	/** returns command to save as*/
	public MesquiteCommand getSaveAsCommand(){
		return saveAsCommand;
	}
	/*.................................................................................................................*/
	/** returns command to save as project (linked files)*/
	public MesquiteCommand getSaveLinkagesCommand(){
		return saveLinkagesCommand;
	}
	/*.................................................................................................................*/
	/** returns the file coordinator module in charge of the project*/
	public FileCoordinator getCoordinatorModule(){
		return ownerModule;
	}
	/* ---------------------------- FILE ELEMENTS ------------------------------*/
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if ((obj == datas || obj == taxas || obj == charModels) && (code == MesquiteListener.PARTS_ADDED || code == MesquiteListener.PARTS_DELETED || code == MesquiteListener.PARTS_MOVED || code == MesquiteListener.ITEMS_ADDED)){
			ownerModule.resetAllMenuBars();
			return;
		}

		if (code != MesquiteListener.SELECTION_CHANGED)
			notifyListeners(this, new Notification(MesquiteListener.ELEMENT_CHANGED));
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	/*.................................................................................................................*/
	/** Adds the passed element to the project.  */
	public void addFileElement(FileElement element) {
		if (element==null)
			return;

		if (element instanceof Taxa) {
			if (taxas.indexOf(element)<0){
				taxas.addElement(element, true);
				element.addListener(taxas);
			}
		}
		else if (element instanceof mesquite.lib.characters.CharacterData){
			if (datas.indexOf(element)<0){
				datas.addElement(element, true);
				element.addListener(datas);
			}
		}
		else if (element instanceof TreeVector) {
			if (treeVectors.indexOf(element)<0){
				treeVectors.addElement(element, true);
			}
		}
		else if (element instanceof CharacterModel) {
			if (charModels.indexOf(element)<0){
				charModels.addElement(element, true);
				modelListener.addModel((CharacterModel)element);
			}
		}
		else {
			if (otherElements.indexOf(element)<0)
				otherElements.addElement(element, true);
		}
		element.addListener(this);
		broadcastAddFileElement(ownerModule, element);
		notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	public void removeFileElement(FileElement element) {
		if (element==null)
			return;
		element.removeListener(this);
		if (element instanceof Taxa) {
			if (taxas != null) {
				taxas.removeElement(element, true);
				element.removeListener(taxas);
			}
			//taxas.notifyListenersOfDisposed(element);
		}
		else if (element instanceof mesquite.lib.characters.CharacterData) {
			if (datas != null) {
				datas.removeElement(element, true);
				element.removeListener(datas);
			}
			//datas.notifyListenersOfDisposed(element);
		}
		else if (element instanceof TreeVector) {
			if (treeVectors != null) {
				treeVectors.removeElement(element, true);
				element.removeListener(treeVectors);
			}
			//datas.notifyListenersOfDisposed(element);
		}
		else if (element instanceof CharacterModel) {
			if (charModels != null)
				charModels.removeElement(element, true);
			//charModels.notifyListenersOfDisposed(element);
			if (modelListener != null)
				modelListener.removeModel((CharacterModel)element);
		}
		else {
			if (otherElements != null)
				otherElements.removeElement(element, true);
			//otherElements.notifyListenersOfDisposed(element);
		}
		notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
		//TODO: shouldn't broadcase of deletion be here?
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	public void removeAllFileElements(Class c, boolean notify) {
		if (Taxa.class.isAssignableFrom(c)) {
			if (taxas != null){
				for (int i = 0; i< taxas.size(); i++){
					FileElement element = (FileElement)taxas.elementAt(i);
					element.deleteMe(false);
				}
				taxas.removeAllElements(notify);
			}
		}
		else if (mesquite.lib.characters.CharacterData.class.isAssignableFrom(c)){

			if (datas != null){
				for (int i = 0; i< datas.size(); i++){
					FileElement element = (FileElement)datas.elementAt(i);
					element.deleteMe(false);
				}
				datas.removeAllElements(notify);
			}
		}
		else if (TreeVector.class.isAssignableFrom(c)){

			if (treeVectors != null){
				for (int i = 0; i< treeVectors.size(); i++){
					FileElement element = (FileElement)treeVectors.elementAt(i);
					element.deleteMe(false);
				}
				treeVectors.removeAllElements(notify);
			}
		}
		else if (CharacterModel.class.isAssignableFrom(c)) {
			if (charModels != null){
				for (int i = 0; i< charModels.size(); i++){
					FileElement element = (FileElement)charModels.elementAt(i);
					element.deleteMe(false);
				}
				charModels.removeAllElements(notify);
			}
		}
		else {
			if (otherElements != null){
				for (int i = 0; i< otherElements.size(); i++){
					FileElement element = (FileElement)otherElements.elementAt(i);
					if (element.getClass().isAssignableFrom(c))
						element.deleteMe(false);
				}
			}
		}
	}	
	/*.................................................................................................................*/
	public void removeAllFileElements(boolean notify) {
		if (taxas != null){
			for (int i = 0; i< taxas.size(); i++){
				FileElement element = (FileElement)taxas.elementAt(i);
				element.deleteMe(false);
			}
			taxas.removeAllElements(notify);
		}

		if (datas != null){
			for (int i = 0; i< datas.size(); i++){
				FileElement element = (FileElement)datas.elementAt(i);
				element.deleteMe(false);
			}
			datas.removeAllElements(notify);
		}

		if (treeVectors != null){
			for (int i = 0; i< treeVectors.size(); i++){
				FileElement element = (FileElement)treeVectors.elementAt(i);
				element.deleteMe(false);
			}
			treeVectors.removeAllElements(notify);
		}
		if (charModels != null){
			for (int i = 0; i< charModels.size(); i++){
				FileElement element = (FileElement)charModels.elementAt(i);
				element.deleteMe(false);
			}
			charModels.removeAllElements(notify);
		}

		if (otherElements != null){
			for (int i = 0; i< otherElements.size(); i++){
				FileElement element = (FileElement)otherElements.elementAt(i);
				element.deleteMe(false);
			}
		}
	}	

	/*.................................................................................................................*/
	/** Returns the number of FileElements that are subclasses of the given class.  */
	public int getNumberOfFileElements(Class c) {
		if (c ==null)
			return 0;

		if (Taxa.class.isAssignableFrom(c)) {
			return taxas.size();
		}
		else if (mesquite.lib.characters.CharacterData.class.isAssignableFrom(c))
			return datas.size();
		else if (CharacterModel.class.isAssignableFrom(c)) {
			return charModels.size();
		}
		else if (TreeVector.class.isAssignableFrom(c)) {
			return treeVectors.size();
		}
		else {
			return otherElements.size(c);
		}
	}
	/*.................................................................................................................*/
	/** Sets elements of file as not dirty (i.e. saved).  */
	public void fileSaved(MesquiteFile f) {
		try {
			if (f == null)
				return;
			f.setDirtiedByCommand(false);
			for (int i=0; i<taxas.size(); i++) {
				FileElement fe = (FileElement)taxas.elementAt(i);
				if (fe.getFile() == f)
					fe.setDirty(false);
			}
			for (int i=0; i<datas.size(); i++) {
				FileElement fe = (FileElement)datas.elementAt(i);
				if (fe.getFile() == f)
					fe.setDirty(false);
			}
			for (int i=0; i<treeVectors.size(); i++) {
				FileElement fe = (FileElement)treeVectors.elementAt(i);
				if (fe.getFile() == f)
					fe.setDirty(false);
			}
			for (int i=0; i<charModels.size(); i++) {
				FileElement fe = (FileElement)charModels.elementAt(i);
				if (fe.getFile() == f)
					fe.setDirty(false);
			}
			for (int i=0; i<otherElements.size(); i++) {
				FileElement fe = (FileElement)otherElements.elementAt(i);
				if (fe.getFile() == f)
					fe.setDirty(false);
			}
		}
		catch (NullPointerException e){
		}

	}
	/*.................................................................................................................*/
	/** Returns a vector of FileElements that are of the given class or are of its subclasses.  */
	public Listable[] getFileElements(Class c) {
		if (c ==null)
			return null;

		if (Taxa.class.isAssignableFrom(c)) {
			return taxas.getElementArray();
		}
		else if (mesquite.lib.characters.CharacterData.class == c)
			return datas.getElementArray();
		else if (TreeVector.class == c)
			return treeVectors.getElementArray();
		else if (mesquite.lib.characters.CharacterData.class.isAssignableFrom(c)) {
			int count =0;
			for (int i=0; i<datas.size(); i++)
				if (c.isAssignableFrom(datas.elementAt(i).getClass()))
					count++;
			Listable[] v = new Listable[count];
			count =0;
			for (int i=0; i<datas.size(); i++)
				if (c.isAssignableFrom(datas.elementAt(i).getClass())) {
					v[count] = (Listable)datas.elementAt(i);
					count++;
				}
			return v;
		}
		else if (CharacterModel.class == c)
			return charModels.getElementArray();
		else if (CharacterModel.class.isAssignableFrom(c)) {
			int count =0;
			for (int i=0; i<charModels.size(); i++)
				if (c.isAssignableFrom(charModels.elementAt(i).getClass()))
					count++;
			Listable[] v = new Listable[count];
			count =0;
			for (int i=0; i<charModels.size(); i++)
				if (c.isAssignableFrom(charModels.elementAt(i).getClass())) {
					v[count] = (Listable)charModels.elementAt(i);
					count++;
				}
			return v;
		}
		else {
			int count =0;
			for (int i=0; i<otherElements.size(); i++)
				if (c.isAssignableFrom(otherElements.elementAt(i).getClass())) {
					count++;
				}
			Listable[] v = new Listable[count];
			count =0;
			for (int i=0; i<otherElements.size(); i++)
				if (c.isAssignableFrom(otherElements.elementAt(i).getClass())) {
					v[count] = (Listable)otherElements.elementAt(i);
					count++;
				}
			return v;
		}

	}
	/*.................................................................................................................*/
	/** Returns a vector of FileElements that are of the given class or are of its subclasses, and which are compatible with the passed object.  */
	public Listable[] getCompatibleFileElements(Class c, Object condition) {
		if (c ==null)
			return null;
		Listable[] elementArray = getFileElements(c); //first get all elements, then filter for compatibility
		if (elementArray == null)
			return null;
		if (condition == null)
			return elementArray;
		int count =0;
		for (int i=0; i<elementArray.length; i++) {
			Object element = elementArray[i];
			if (element instanceof CompatibilityChecker){
				if (((CompatibilityChecker)element).isCompatible(condition, this, null)){
					count++;
				}
			}
			else
				count++;
		}
		Listable[] v = new Listable[count];
		count =0;
		for (int i=0; i<elementArray.length; i++) {
			Object element = elementArray[i];
			if (element instanceof CompatibilityChecker){
				if (((CompatibilityChecker)element).isCompatible(condition, this, null)){
					v[count] = (Listable)element;
					count++;
				}
			}
			else {
				v[count] = (Listable)element;
				count++;
			}
		}
		return v;
	}
	/*.................................................................................................................*/
	/** Returns the ith FileElement that is a subclass of the given class.  */
	public FileElement getFileElement(Class c, int i) {
		if (c ==null || i<0)
			return null;

		if (Taxa.class.isAssignableFrom(c)) {
			if (i< taxas.size())
				return (FileElement)taxas.elementAt(i);
		}
		else if (mesquite.lib.characters.CharacterData.class.isAssignableFrom(c)) {
			if (i< datas.size())
				return (FileElement)datas.elementAt(i);
		}
		else if (TreeVector.class.isAssignableFrom(c)) {
			if (i< treeVectors.size())
				return (FileElement)treeVectors.elementAt(i);
		}
		else if (CharacterModel.class.isAssignableFrom(c)) {
			if (i< charModels.size())
				return (FileElement)charModels.elementAt(i);
		}
		else {
			int count =0;
			Enumeration e = otherElements.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (c.isAssignableFrom(obj.getClass())) {
					if (count == i)
						return (FileElement)obj;
					count ++;
				}
			}
		}
		return null;
	}
	
	/*.................................................................................................................*/
	/** Returns the index position of the  FileElement among others of its the subclass c.  */
	public int getFileElementNumber(FileElement f, Class c) {
		if (f ==null || c==null)
			return -1;

		if (Taxa.class.isAssignableFrom(c)) {
			return getTaxaNumber((Taxa)f);
		}
		else if (TreeVector.class.isAssignableFrom(c)) {
			return getTreeVectorNumber((TreeVector)f);
		}
		else if (mesquite.lib.characters.CharacterData.class.isAssignableFrom(c)) {
			return getMatrixNumber((mesquite.lib.characters.CharacterData)f);
		}
		else if (CharacterModel.class.isAssignableFrom(c)) {
			int count =0;
			Enumeration e = charModels.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (c.isAssignableFrom(obj.getClass())) {
					if (obj == f)
						return count;
					count ++;
				}
			}
		}
		else {
			int count =0;
			Enumeration e = otherElements.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (c.isAssignableFrom(obj.getClass())) {
					if (obj == f)
						return count;
					count ++;
				}
			}
		}
		return -1;
	}
	/*.................................................................................................................*/
	/* A method called to broadcast to all descendent modules that file element has been added.*/
	private void broadcastAddFileElement(MesquiteModule module, FileElement element) {
		module.fileElementAdded(element);
		EmployeeVector e = module.getEmployeeVector();
		for (int i = 0; i<e.size(); i++) {
			MesquiteModule employee = (MesquiteModule)e.elementAt(i);
			broadcastAddFileElement(employee, element);
		}
	}
	/*.................................................................................................................*/
	/** Adds the passed NEXUS block to the project. */
	public void addNexusBlock(NexusBlock block) {
		nexusBlocks.addElement(block, true);
	}
	/*.................................................................................................................*/
	/** Removes the passed NEXUS block  from the project. */
	public void removeNexusBlock(NexusBlock block) {
		if (nexusBlocks != null) {
			if (block !=null) {
				if (files.indexOf(block.getFile())<0)
					MesquiteMessage.warnProgrammer("Error: NEXUS block found referring to file that no longer exists " + block.getClass());
				block.dispose();
				nexusBlocks.removeElement(block, true);
			}
		}
	}
	/*.................................................................................................................*/
	/** Removes those NEXUS blocks belonging to the given file. */
	public void removeNexusBlocks(MesquiteFile f) {
		if (nexusBlocks==null)
			return;
		int count = 0;
		for (int i = 0; i<nexusBlocks.size(); i++) {
			NexusBlock nb = (NexusBlock)nexusBlocks.elementAt(i);
			if (nb.getFile()==f)
				count++;
		}
		if (count==0)
			return;
		NexusBlock[] nbs = new NexusBlock[count];
		count=0;
		for (int i = 0; i<nexusBlocks.size(); i++) {
			NexusBlock nb = (NexusBlock)nexusBlocks.elementAt(i);
			if (nb.getFile()==f) {
				nbs[count]= nb;
				count++;
			}
		}

		for (int i = 0; i<count; i++) {
			removeNexusBlock(nbs[i]);
		}
	}
	/*.................................................................................................................*/
	/** Returns an array of NEXUS blocks of the given type and belonging to the given file. */
	public NexusBlock[] getNexusBlocks(Class c, MesquiteFile f) {
		int count = 0;
		for (int i = 0; i<nexusBlocks.size(); i++) {
			NexusBlock nb = (NexusBlock)nexusBlocks.elementAt(i);
			if (c.isAssignableFrom(nb.getClass()) && (nb.getFile()==f || f == null))
				count++;
		}
		if (count==0)
			return null;
		NexusBlock[] nbs = new NexusBlock[count];
		count=0;
		for (int i = 0; i<nexusBlocks.size(); i++) {
			NexusBlock nb = (NexusBlock)nexusBlocks.elementAt(i);
			if (c.isAssignableFrom(nb.getClass()) &&  (nb.getFile()==f || f == null)) {
				nbs[count]= nb;
				count++;
			}
		}
		return nbs;
	}
	/*.................................................................................................................*/
	/** Returns an array of NEXUS blocks of the given type name. 12 Jan 02*/
	public NexusBlock[] getNexusBlocks(String typeName) {
		if (StringUtil.blank(typeName))
			return null;
		int count = 0;
		for (int i = 0; i<nexusBlocks.size(); i++) {
			NexusBlock nb = (NexusBlock)nexusBlocks.elementAt(i);
			if (typeName.equalsIgnoreCase(nb.getBlockName()))
				count++;
		}
		if (count==0)
			return null;
		NexusBlock[] nbs = new NexusBlock[count];
		count=0;
		for (int i = 0; i<nexusBlocks.size(); i++) {
			NexusBlock nb = (NexusBlock)nexusBlocks.elementAt(i);
			if (typeName.equalsIgnoreCase(nb.getBlockName())) {
				nbs[count]= nb;
				count++;
			}
		}
		return nbs;
	}
	/*.................................................................................................................*/
	/** Returns the ListableVector of NEXUS blocks in the project. */
	public ListableVector getNexusBlocks() {
		return nexusBlocks;
	}

	/* ---------------------------- AUTHORS ------------------------------*/
	ListableVector authors;
	public ListableVector getAuthors(){
		if (authors == null)
			authors = new ListableVector();
		return authors;
	}

	public int numAuthors(){
		if (authors == null)
			return 0;
		return authors.size();
	}
	public Author findAuthor(String s, boolean tryName){
		if (s == null)
			return null;
		//first, interpret as code
		if (MesquiteModule.author != null && s.equals(MesquiteModule.author.getCode()))
			return MesquiteModule.author;
		if (numAuthors()>0){
			ListableVector v = getAuthors();
			for (int i = 0; i< v.size(); i++){
				Author a = (Author)v.elementAt(i);
				if (a.getCode()!= null && s.equals(a.getCode()))
					return a;
			}
		}
		if (tryName){
			//second, try as author name
			if (MesquiteModule.author != null && s.equals(MesquiteModule.author.getName()))
				return MesquiteModule.author;
			if (numAuthors()>0){
				ListableVector v = getAuthors();
				for (int i = 0; i< v.size(); i++){
					Author a = (Author)v.elementAt(i);
					if (a.getName()!= null && s.equals(a.getName()))
						return a;
				}
			}
		}
		return null;
	}
	//hackathon
	public ListableVector getOtherElements(){
		return otherElements;
	}
	/* ---------------------------- TAXA ------------------------------*/
	public Taxa createTaxaBlock(int numTaxa){
		TaxaManager mTaxa = (TaxaManager)ownerModule.findEmployeeWithDuty(TaxaManager.class);
		Object t = null;
		if (mTaxa!=null)
			t = mTaxa.doCommand("newTaxa",Integer.toString(numTaxa) + " " + ParseUtil.tokenize("taxa"), CommandChecker.defaultChecker); //treat as scripting so as not to complain if something not found 19Jan02
		return (Taxa)t;
	}
	/*.................................................................................................................*/
	/** returns the list of Taxa  */
	public ListableVector getTaxas() {
		return taxas;
	}
	/*.................................................................................................................*/
	/** Returns the number of FileElements that are subclasses of the given class.  */
	public FileElement getElementByID(Class c, int id) {
		if (c ==null)
			return null;

		if (Taxa.class.isAssignableFrom(c)) {
			for (int i = 0; i<taxas.size(); i++){
				FileElement element = (FileElement)taxas.elementAt(i);
				if (element.getID() == id)
					return element;
			}
		}
		else if (TreeVector.class.isAssignableFrom(c)) {
			for (int i = 0; i<treeVectors.size(); i++){
				FileElement element = (FileElement)treeVectors.elementAt(i);
				if (element.getID() == id)
					return element;
			}
		}
		else if (mesquite.lib.characters.CharacterData.class.isAssignableFrom(c)) {
			for (int i = 0; i<datas.size(); i++){
				FileElement element = (FileElement)datas.elementAt(i);
				if (element.getID() == id)
					return element;
			}
		}
		else if (CharacterModel.class.isAssignableFrom(c)) {
			for (int i = 0; i<charModels.size(); i++){
				FileElement element = (FileElement)charModels.elementAt(i);
				if (element.getID() == id)
					return element;
			}
		}
		else {
			for (int i = 0; i<otherElements.size(); i++){
				FileElement element = (FileElement)otherElements.elementAt(i);
				if (element.getID() == id)
					return element;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the Taxa of given number */
	public Taxa getTaxa(int taxaNumber) {
		if (taxaNumber>=0 && taxaNumber < taxas.size())
			return (Taxa)taxas.elementAt(taxaNumber);
		MesquiteMessage.warnProgrammer("Error: attempt to get taxa set " + taxaNumber + "; maximum  " + taxas.size());
		MesquiteMessage.printStackTrace();
		return null;
	}
	/*.................................................................................................................*/
	/** returns the Taxa with given id number */
	public Taxa getTaxaByID(long id) {
		//second look for match with .# file number
		for (int i=0; i<taxas.size(); i++) {
			Taxa taxa = (Taxa)taxas.elementAt(i);
			if (id == taxa.getID())
				return taxa;
		}
		return null;
	}

	/*.................................................................................................................*/
	/** returns the Taxa of given name */
	public Taxa getTaxaLastFirst(String name) {
		return getTaxaLastFirst(null, name);
	}
	/*.................................................................................................................*/
	/** returns the Taxa of given reference, name or number (as indicated by string passed) */
	public Taxa getTaxaLastFirst(MesquiteFile f, String ref) {
		if (ref==null)
			return null;
		if (ref.length()>1 && ref.charAt(0)=='#'){ //using id number reference
			String id = ref.substring(1, ref.length());
			//first look for direct match in same file
			for (int i=taxas.size()-1; i>=0; i--) {
				Taxa taxa = (Taxa)taxas.elementAt(i);
				if ((f==null || f == taxa.getFile()) && id.equals(taxa.getAssignedID()))
					return taxa;

			}
			//second look for match with .# file number
			for (int i=taxas.size()-1; i>=0; i--) {
				Taxa taxa = (Taxa)taxas.elementAt(i);
				if (f != null && (id.indexOf('.') < 0)){
					String supID = id + "." + f.getID();
					if (supID.equals(taxa.getAssignedID())) {
						return taxa;
					}
				}
			}
			//third strip file number and see if match
			for (int i=taxas.size()-1; i>=0; i--) {
				Taxa taxa = (Taxa)taxas.elementAt(i);
				if ((taxa.getAssignedID().indexOf('.') >= 0)){
					String supID2 = taxa.getAssignedID().substring(0, taxa.getAssignedID().indexOf('.'));
					if (id.equals(supID2)) {
						return taxa;
					}
				}

			}
		}
		else { 
			for (int i=taxas.size()-1; i>=0; i--) {
				//check to see if name of taxa is being passed
				Taxa t = (Taxa)taxas.elementAt(i);
				if (t.getName()!=null && t.getName().equalsIgnoreCase(ref) && (f==null || t.getFile() == f))
					return (Taxa)taxas.elementAt(i);
			}

			return getTaxa(f, MesquiteInteger.fromString(ref)); //try to see if number

		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the Taxa of given name */
	public Taxa getTaxa(String name) {
		return getTaxa(null, name);
	}
	/*.................................................................................................................*/
	/** returns the Taxa of given reference, name or number (as indicated by string passed) */
	public Taxa getTaxa(MesquiteFile f, String ref) {
		if (ref==null)
			return null;
		if (ref.length()>1 && ref.charAt(0)=='#'){ //using id number reference
			String id = ref.substring(1, ref.length());
			//first look for direct match in same file
			for (int i=0; i<taxas.size(); i++) {
				Taxa taxa = (Taxa)taxas.elementAt(i);
				if ((f==null || f == taxa.getFile()) && id.equals(taxa.getAssignedID()))
					return taxa;

			}
			//second look for match with .# file number
			for (int i=0; i<taxas.size(); i++) {
				Taxa taxa = (Taxa)taxas.elementAt(i);
				if (f != null && (id.indexOf('.') < 0)){
					String supID = id + "." + f.getID();
					if (supID.equals(taxa.getAssignedID())) {
						return taxa;
					}
				}
			}
			//third strip file number and see if match
			for (int i=0; i<taxas.size(); i++) {
				Taxa taxa = (Taxa)taxas.elementAt(i);
				if ((taxa.getAssignedID().indexOf('.') >= 0)){
					String supID2 = taxa.getAssignedID().substring(0, taxa.getAssignedID().indexOf('.'));
					if (id.equals(supID2)) {
						return taxa;
					}
				}

			}
		}
		else { 
			for (int i=0; i<taxas.size(); i++) { //check to see if name of taxa is being passed
				Taxa t = (Taxa)taxas.elementAt(i);
				if (t.getName()!=null && t.getName().equalsIgnoreCase(ref) && (f==null || t.getFile() == f))
					return (Taxa)taxas.elementAt(i);
			}

			return getTaxa(f, MesquiteInteger.fromString(ref)); //try to see if number

		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the jth of taxa blocks with given unique ID*/
	public Taxa getTaxa(String uniqueID, int j) {
		int count=0;
		for (int i=0; i<taxas.size(); i++) { 
			Taxa taxa = (Taxa)taxas.elementAt(i);

			if (!taxa.isDoomed()  && uniqueID != null && taxa.getUniqueID() != null && taxa.getUniqueID().equals(uniqueID)) {
				if (count==j) {
					return taxa;
				}
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the Taxa of given number belonging to given file */
	public Taxa getTaxa(MesquiteFile f, int taxaNumber) {
		int count=0;
		for (int i=0; i<taxas.size(); i++) {
			Taxa taxa = ((Taxa)taxas.elementAt(i));
			if (f== null || taxa.getFile()==f) {
				if (count==taxaNumber)
					return taxa;
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the number (index position) of the Taxa of given name */
	public int getTaxaNumber(String name) {
		for (int i=0; i<taxas.size(); i++) {
			if (((Taxa)taxas.elementAt(i)).getName().equalsIgnoreCase(name))
				return i;
		}
		return -1;
	}
	/*.................................................................................................................*/
	/** returns the number (index position) of the Taxa */
	public int getTaxaNumber(MesquiteFile file, Taxa taxa) {
		if (file == null)
			return getTaxaNumber(taxa);
		if (taxa==null)
			return -1;
		int count =0;
		for (int i=0; i<taxas.size(); i++) {
			Taxa t = ((Taxa)taxas.elementAt(i));
			if (t== taxa) 
				return count;
			if (t.getFile()==file)
				count++;
		}
		return -1;
	}
	/*.................................................................................................................*/
	/** returns the number (index position) of the Taxa */
	public int getTaxaNumber(Taxa taxa) {
		if (taxa==null)
			return -1;
		if (taxa.isDuplicate()){ //This subverts the taxa if it's a duplicate, instead giving number of original copy
			for (int i=0; i<taxas.size(); i++) {
				Taxa t =(Taxa)taxas.elementAt(i);
				if (taxa.equals(t, false, true) && !t.isDuplicate()) {
					return i;
				}
			}
		}
		for (int i=0; i<taxas.size(); i++) {
			if (((Taxa)taxas.elementAt(i)) == taxa) {
				return i;
			}
		}
		return -1;
	}
	/*.................................................................................................................*/
	/** returns a string that can be used to refer to  taxa block */
	public String getTaxaReferenceInternal(Taxa taxa) {
		if (taxa == null)
			return null;
		return "#" + taxa.getAssignedID();
	}
	/*.................................................................................................................*/
	/** returns a string that can be used to refer to  taxa block */
	public String getTaxaReferenceExternal(Taxa taxa) {
		if (taxa == null)
			return null;
		return "#" + taxa.getAssignedIDNumber();
	}
	/*.................................................................................................................*/
	/** This is deprecated and should be avoided */
	public String getTaxaReference(Taxa taxa) {
		return getTaxaReferenceExternal(taxa);
	}
	/*.................................................................................................................*/
	public Taxa chooseTaxa(MesquiteWindow frame, String message){
		return chooseTaxa(frame, message, false,null,null);
	}
	/*.................................................................................................................*/
	public Taxa chooseTaxa(MesquiteWindow frame, String message, boolean askEvenIfOne){
		return chooseTaxa(frame,message,askEvenIfOne,null,null);
	}
	/*.................................................................................................................*/
	public Taxa chooseTaxa(MesquiteWindow frame, String message, boolean askEvenIfOne, String okButton, String cancelButton){
		int num = getNumberTaxas();
		if (num==0)
			return null;
		else if ((num==1 && !askEvenIfOne) || (MesquiteThread.isScripting() && num>=1))
			return getTaxa(0);
		String[] taxaNames = new String[num];
		for (int i=0; i<num;i++) 
			taxaNames[i] = getTaxa(i).getName();
		int whichTaxa = ListDialog.queryList(frame, "Select taxa", message, MesquiteString.helpString, taxaNames, 0, okButton, cancelButton);
		if (!MesquiteInteger.isCombinable(whichTaxa)) 
			return null;
		else if (whichTaxa>=0) 
			return getTaxa(whichTaxa);
		else
			return getTaxa(0);
	}
	/*.................................................................................................................*/
	public Taxa findTaxa(MesquiteFile file, String token){
		Taxa t = getTaxaLastFirst(file, token);
		if (t == null && file != null){
			Listable ms = file.taxaNameTranslationTable.elementWithName(token);
			if (ms != null)
				token = ((MesquiteString)ms).getValue();  //substitute translated name
		}
		if (t==null)
			t = getTaxaLastFirst(token);
		if (t==null){
			int wt = MesquiteInteger.fromString(token);
			if (MesquiteInteger.isCombinable(wt))
				t = getTaxa(wt-1);
		}
		if (t == null && getNumberTaxas(file)==1)
			t = getTaxa(file, 0);
		if (t == null && getNumberTaxas(file)>1)
			t = getTaxa(file, getNumberTaxas(file)-1);
		if (t == null && getNumberTaxas()==1)
			t = getTaxa(0);
		if (t == null && getNumberTaxas()>1)
			t = getTaxa(getNumberTaxas()-1);
		return t;
	}
	/*.................................................................................................................*/
	public CharacterData findCharacterMatrix(MesquiteFile file, Taxa defaultTaxa, String token){
		CharacterData d = getCharacterMatrixReverseOrder(file, token);
		if (d == null && file != null){
			Listable ms = file.characterDataNameTranslationTable.elementWithName(token);
			if (ms != null)
				token = ((MesquiteString)ms).getValue();  //substitute translated name
		}
		if (d==null)
			d = getCharacterMatrixReverseOrder(token);
		if (d==null){
			int wt = MesquiteInteger.fromString(token);
			if (MesquiteInteger.isCombinable(wt))
				d = getCharacterMatrix(defaultTaxa, wt-1);
		}
		if (d == null && getNumberCharMatrices(file)==1){
			d = getCharacterMatrix(file, 0);
		}
		if (d == null && getNumberCharMatrices()==1){
			d = getCharacterMatrix(0);
		}
		return d;
	}
	/*.................................................................................................................*/
	//TODO: ��� have general get number of elements, passing project, or file; counts
	/** returns the number of Taxa objects stored in project belonging to a file*/
	public int  getNumberTaxas(MesquiteFile f) {
		int count = 0;
		for (int i=0; i<taxas.size(); i++) {
			if (f==null || ((Taxa)taxas.elementAt(i)).getFile()==f)
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public int  getNumberTaxas(String uniqueID) {
		int count = 0;
		for (int i=0; i<taxas.size(); i++) {
			Taxa taxa = (Taxa)taxas.elementAt(i);
			if (uniqueID != null && taxa.getUniqueID() != null && taxa.getUniqueID().equals(uniqueID))
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	/** returns the number of Taxa objects stored in project */
	public int  getNumberTaxas() {
		if (taxas == null)
			return 0;
		return taxas.size();
	}

	/*.................................................................................................................*/
	public int getTreeVectorNumber(FileElement f){
		if (f == null)
			return -1;
		for (int i=0; i<treeVectors.size(); i++) {
			if (((FileElement)treeVectors.elementAt(i)) == f) {
				return i;
			}
		}
		return -1;
	}
	/*.................................................................................................................*/
	public TreeVector storeTree(MesquiteWindow parent, TreeVector trees, Tree tree, boolean askIfSingleBlock){
		if (ownerModule == null || tree == null)
			return null;
		if (trees == null){
			TreesManager manager = (TreesManager)ownerModule.findElementManager(TreeVector.class);
			if (manager !=null){
				int numLists = manager.getNumberTreeBlocks(tree.getTaxa());
				if (numLists==0) {
					String treesListName = MesquiteString.queryString(parent, "New Tree Block" , "Name of new Tree Block: ", "Trees");
					MesquiteFile f = ownerModule.getProject().chooseFile("In which file should new tree block be placed?");  
					trees = manager.makeNewTreeBlock(tree.getTaxa(), treesListName, f);
				}
				else if (numLists == 1 && !askIfSingleBlock){
					trees = manager.getTreeBlock(tree.getTaxa(), 0);
				}
				else {
					Listable[] lists = new Listable[numLists+1];
					for (int i=0; i<numLists; i++)
						lists[i] = manager.getTreeBlock(tree.getTaxa(), i);
					lists[numLists] = new MesquiteString("New Trees Block...", "New Trees Block...");
					Object obj = ListDialog.queryList(parent, "Where to store tree?", "Choose tree block in which to store tree:",MesquiteString.helpString, lists, 0);
					if (obj instanceof MesquiteString){
						ListableVector v = manager.getTreeBlockVector();
						String suggestedName = v.getUniqueName("Trees");
						String treesListName = MesquiteString.queryString(parent, "New Tree Block" , "Name of new Tree Block: ", suggestedName);
						MesquiteFile f = ownerModule.getProject().chooseFile("In which file should new tree block be placed?");  
						trees = manager.makeNewTreeBlock(tree.getTaxa(), treesListName, f);
					}
					else
						trees = (TreeVector)obj;
				}
			}
		}
		if (trees!=null) {
			trees.addElement(tree, true);
			return trees;
		}
		return null;
	}
	/* ---------------------------- CHARACTERDATA ------------------------------*/
	/*.................................................................................................................*/
	/** returns jth data set in file*/
	public ListableVector getCharacterMatrices() {
		return datas;
	}
	/*.................................................................................................................*/
	//NOTE: dataClass to be used is subclass of CharacterState, not of CharacterData
	public boolean compatibleMatrix(Object dataClass, mesquite.lib.characters.CharacterData data){
		if (dataClass ==null)
			return true;
		if (dataClass instanceof Class){
			return ((Class)dataClass).isAssignableFrom(data.getStateClass());
		}
		else if (dataClass instanceof Class[]){
			Class[] dataClasses = ((Class[])dataClass);
			for (int i=0; i<dataClasses.length; i++){
				if (dataClasses[i].isAssignableFrom(data.getStateClass()))
					return true;
			}
			return false;
		}
		return false;
	}
	/*===============================================================*/
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets in file*/
	public int  getNumberCharMatrices() { //CORE, collab, chromaseq, STRAT TOOLS
		return getNumberCharMatrices(null, null, null, false);
	}
	/*......................*/
	/** returns jth data set in file*/
	public mesquite.lib.characters.CharacterData getCharacterMatrix(int j) {  //core, chromaseq, collab; can use RAW
		return getCharacterMatrix(j, false);
	}
	public mesquite.lib.characters.CharacterData getCharacterMatrixDoomedOrNot(int j) {  
		int count=0;
		mesquite.lib.characters.CharacterData data;
		for (int i=0; i<datas.size(); i++) {
			data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
			if (count==j)
				return data;
			count++;

		}
		MesquiteTrunk.mesquiteTrunk.logln("Error: attempt to get data set beyond number (0) [" + j + "] {" + datas.size() + " }"  + getName());
		MesquiteMessage.printStackTrace("Error: attempt to get data set beyond number (0) [" + j + "] {" + datas.size() + " }"  + getName());
		return null;
	}
	public mesquite.lib.characters.CharacterData getCharacterMatrix(int j, boolean onlyVisible) {  //core, chromaseq, collab; can use RAW
		int count=0;
		mesquite.lib.characters.CharacterData data;
		for (int i=0; i<datas.size(); i++) {
			data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
			if (!data.isDoomed() && (!onlyVisible || data.isUserVisible())) {
				if (count==j)
					return data;
				count++;
			}
		}
		MesquiteTrunk.mesquiteTrunk.logln("Error: attempt to get data set beyond number (0) [" + j + "] {" + datas.size() + " }"  + getName());
		MesquiteMessage.printStackTrace("Error: attempt to get data set beyond number (0) [" + j + "] {" + datas.size() + " }"  + getName());
		return null;
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets in file*/
	public int  getNumberCharMatricesVisible() { //���
		return getNumberCharMatrices(null, null, null, true);
	}
	/*......................*/
	/** returns jth data set in file*/
	public mesquite.lib.characters.CharacterData getCharacterMatrixVisible(int j) {  //core, chromaseq, collab; can use RAW
		int count=0;
		mesquite.lib.characters.CharacterData data;
		for (int i=0; i<datas.size(); i++) {
			data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
			if (data.isUserVisible() && !data.isDoomed()) {
				if (count==j)
					return data;
				count++;
			}
		}
		MesquiteTrunk.mesquiteTrunk.logln("Error: attempt to get data set beyond number (0) [" + j + "] {" + datas.size() + " }"  + getName());
		MesquiteMessage.printStackTrace("Error: attempt to get data set beyond number (0) [" + j + "] {" + datas.size() + " }"  + getName());
		return null;
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets belonging to given taxa*/
	public int  getNumberCharMatrices(Taxa taxa) { //�����
		return getNumberCharMatrices(null, taxa, null, false);
	}
	/*......................*/
	/** returns the jth data set belonging to given taxa.*/
	public mesquite.lib.characters.CharacterData getCharacterMatrix(Taxa taxa, int j) { //core, collab, ppod, rLink
		return getCharacterMatrix(null, taxa, null, j, false);
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets belonging to given taxa*/
	public int  getNumberCharMatricesVisible(Taxa taxa) { //�����
		return getNumberCharMatrices(null, taxa, null, true);
	}
	/*......................*/
	/** returns the jth data set belonging to given taxa.*/
	public mesquite.lib.characters.CharacterData getCharacterMatrixVisible(Taxa taxa, int j) { //core, collab, ppod, rLink
		return getCharacterMatrix(null, taxa, null, j, true);
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets of a given data class (CharacterState subclass is passed)*/
	public int  getNumberCharMatricesVisible(Object dataClass) {  //�����
		return getNumberCharMatrices(null, null, dataClass, true);
	}
	/*......................*/
	/** returns the jth data set for given CharacterState subclass.*/
	public mesquite.lib.characters.CharacterData getCharacterMatrixVisible(int j, Object dataClass) {  //stepmatrix model ONLY
		return getCharacterMatrix(null, null, dataClass, j, true);
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets of a given data class (CharacterState subclass is passed)*/
	public int  getNumberCharMatrices(Object dataClass) {  //�����
		return getNumberCharMatrices(null, null, dataClass, false);
	}
	/*......................*/
	/** returns the jth data set for given CharacterState subclass.*/
	public mesquite.lib.characters.CharacterData getCharacterMatrix(int j, Object dataClass) {  //stepmatrix model ONLY
		return getCharacterMatrix(null, null, dataClass, j, false);
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets of a given data class (CharacterState subclass is passed) belonging to given taxa*/
	public int  getNumberCharMatricesVisible(Taxa taxa, Object dataClass) {  //�����
		return getNumberCharMatrices(null, taxa, dataClass, true);
	}
	/*......................*/
	/** returns the jth data set belonging to given taxa for given CharacterState subclass.*/
	public mesquite.lib.characters.CharacterData getCharacterMatrixVisible(Taxa taxa, int j, Object dataClass) { //����
		return getCharacterMatrix(null, taxa, dataClass, j, true);
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets belonging to a given file*/
	public int  getNumberCharMatrices(MesquiteFile f) { //�����
		return getNumberCharMatrices(f, null, null, false);
	}
	/*......................*/
	/** returns the jth of data sets belonging to a given file*/
	public mesquite.lib.characters.CharacterData getCharacterMatrix(MesquiteFile f, int j) {  //�����
		return getCharacterMatrix(f, null, null, j, false);
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets of a given data class (CharacterState subclass is passed) belonging to given taxa.  If permitTaxaMatching is true, 
	 * considers taxa equal if names coincide*/
	public int  getNumberCharMatricesDoomedOrNot(MesquiteFile file, Taxa taxa, Object dataClass) {  //MesquiteProject
		int count=0;
		if (datas == null)
			return 0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
			if ((file == null || data.getFile()==file) && (taxa == null || taxa == data.getTaxa()) && (dataClass==null || compatibleMatrix(dataClass, data)))
				count++;
		}
		return count;
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets of a given data class (CharacterState subclass is passed) belonging to given taxa.  If permitTaxaMatching is true, 
	 * considers taxa equal if names coincide*/
	public int  getNumberCharMatrices(MesquiteFile file, Taxa taxa, Object dataClass, boolean visibleOnly) {  //MesquiteProject
		int count=0;
		if (datas == null)
			return 0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
			if ((!visibleOnly || data.isUserVisible()) && !data.isDoomed() && (file == null || data.getFile()==file) && (taxa == null || taxa == data.getTaxa()) && (dataClass==null || compatibleMatrix(dataClass, data)))
				count++;
		}
		return count;
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns number of data sets of a given data class (CharacterState subclass is passed) belonging to given taxa.  If permitTaxaMatching is true, 
	 * considers taxa equal if names coincide*/
	public int  getNumberCharMatricesExactClass(MesquiteFile file, Taxa taxa, Object dataClass, boolean visibleOnly) {  //MesquiteProject
		int count=0;
		if (datas == null)
			return 0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
			if ((!visibleOnly || data.isUserVisible()) && !data.isDoomed() && (file == null || data.getFile()==file) && (taxa == null || taxa == data.getTaxa()) && (dataClass==null || dataClass == data.getStateClass()))
				count++;
		}
		return count;
	}
	/*......................*/
	/** returns the jth of data sets belonging to a given file*/
	public mesquite.lib.characters.CharacterData getCharacterMatrixVisible(MesquiteFile f, Taxa taxa, Object dataClass, int j) {  //���� not used yet
		return getCharacterMatrix(f, taxa, dataClass, j, true);
	}
	/*......................*/
	/** returns the jth of data sets belonging to a given file*/
	private mesquite.lib.characters.CharacterData getCharacterMatrix(MesquiteFile f, Taxa taxa, Object dataClass, int j) {  //����
		return getCharacterMatrix(f, taxa, dataClass, j, false);
	}
	/*......................*/
	/** returns the jth of data sets belonging to a given file*/
	public mesquite.lib.characters.CharacterData getCharacterMatrix(MesquiteFile f, Taxa taxa, Object dataClass, int j, boolean visibleOnly) {  //MesquiteProject only
		int count=0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);

			if ((!visibleOnly || data.isUserVisible()) && !data.isDoomed() && (f== null || data.getFile()==f) && (taxa == null || taxa == data.getTaxa()) && (dataClass == null || compatibleMatrix(dataClass, data))) {
				if (count==j) {
					return data;
				}
				count++;
			}
		}
		return null;
	}
	/*......................*/
	/** returns the jth of data sets belonging to a given file*/
	public mesquite.lib.characters.CharacterData getCharacterMatrixExactClass(MesquiteFile f, Taxa taxa, Object dataClass, int j, boolean visibleOnly) {  //MesquiteProject only
		int count=0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);

			if ((!visibleOnly || data.isUserVisible()) && !data.isDoomed() && (f== null || data.getFile()==f) && (taxa == null || taxa == data.getTaxa()) && (dataClass == null || dataClass == data.getStateClass())) {
				if (count==j) {
					return data;
				}
				count++;
			}
		}
		return null;
	}
	/*---------===---------===---------===---------===---------===---------===-------------------*/
	/** returns the jth of data sets with given unique ID*/
	public mesquite.lib.characters.CharacterData getCharacterMatrixByUniqueID(String uniqueID, int j) {  //ManageCharacters only
		int count=0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);

			if (!data.isDoomed()  && uniqueID != null && data.getUniqueID() != null && data.getUniqueID().equals(uniqueID)) {
				if (count==j) {
					return data;
				}
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** gets the data set of given reference string (ref. number, name, number) from the project */
	public mesquite.lib.characters.CharacterData getCharacterMatrixByReference(MesquiteFile f, String ref) {  //CORE, pPod
		return getCharacterMatrixByReference(f, null, null, ref);
	}
	/*.................................................................................................................*/
	/** gets the data set of given reference string (ref. number, name, number) from the project */
	public mesquite.lib.characters.CharacterData getCharacterMatrixByReference(MesquiteFile f, String ref, boolean onlyUserVisible) {  //CORE, pPod
		return getCharacterMatrixByReference(f, null, null, ref, onlyUserVisible);
	}
	/*.................................................................................................................*/
	/** gets the data set of given reference string (ref. number, name, number) from the project */
	public mesquite.lib.characters.CharacterData getCharacterMatrixByReference(MesquiteFile f, Taxa taxa, Object dataClass, String ref, boolean onlyUserVisible) {  //StoredCharacters, StoredMatrices, MesquiteProject
		if (ref !=null && ref.length()>1 && ref.charAt(0)=='#'){ //using id number reference
			String id = ref.substring(1, ref.length());
			//restricted case first
			if (f != null && (id.indexOf('.') < 0)){
				for (int i=0; i<datas.size(); i++) {
					mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
					String supID = id + "." + f.getID();
					if (!data.isDoomed() && (supID.equals(data.getAssignedID())) && (taxa==null || data.getTaxa() == taxa) && (dataClass == null || compatibleMatrix(dataClass, data)) && (!onlyUserVisible || data.isUserVisible())) {
						return data;
					}
				}
			}
			for (int i=0; i<datas.size(); i++) {
				mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
				if (!data.isDoomed() && ((id.indexOf('.') < 0 && id.equals(Long.toString(data.getAssignedIDNumber()))) ||(id.indexOf('.') >= 0 && id.equals(data.getAssignedID()))) && (taxa==null || data.getTaxa() == taxa) && (dataClass == null || compatibleMatrix(dataClass, data))&& (!onlyUserVisible || data.isUserVisible())) {
					return data;
				}
			}
		}
		else { //using name or number
			for (int i=0; i<datas.size(); i++) {
				mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
				if (!data.isDoomed() && data.getName().equalsIgnoreCase(ref) && (f==null || data.getFile() == f) && (taxa==null || data.getTaxa() == taxa)&& (dataClass == null || compatibleMatrix(dataClass, data))&& (!onlyUserVisible || data.isUserVisible()))
					return data;
			}
			return getCharacterMatrix(f, taxa, dataClass, MesquiteInteger.fromString(ref), onlyUserVisible); //try to see if number
		}
		return null;  //CHECK FOR COMPATIBLE
	}
	/*.................................................................................................................*/
	/** gets the data set of given reference string (ref. number, name, number) from the project */
	public mesquite.lib.characters.CharacterData getCharacterMatrixByReference(MesquiteFile f, Taxa taxa, Object dataClass, String ref) {  //StoredCharacters, StoredMatrices, MesquiteProject
		return getCharacterMatrixByReference(f,taxa,dataClass,ref,false);
	}
	/*.................................................................................................................*/
	/** gets the data set of given reference string (ref. number, name, number) from the project */
	public mesquite.lib.characters.CharacterData getCharacterMatrixReverseOrder(String name) {  //CORE (check)
		return getCharacterMatrixReverseOrder((MesquiteFile)null, (Taxa)null, null, name);
	}
	/*.................................................................................................................*/
	/** gets the data set of given reference string (ref. number, name, number) from the project */
	public mesquite.lib.characters.CharacterData getCharacterMatrixReverseOrder(MesquiteFile file, String name) {  //Collab
		return getCharacterMatrixReverseOrder(file, (Taxa)null, null, name);
	}
	/*.................................................................................................................*/
	/** gets the data set of given reference string (ref. number, name, number) from the project */
	public mesquite.lib.characters.CharacterData getCharacterMatrixReverseOrder(MesquiteFile f, Taxa taxa, Object dataClass, String ref) {  //Collab, MesquiteProject
		if (ref !=null && ref.length()>1 && ref.charAt(0)=='#'){ //using id number reference
			String id = ref.substring(1, ref.length());
			//restricted case first
			if (f != null && (id.indexOf('.') < 0)){
				for (int i=datas.size()-1; i>=0; i--) {
					mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
					String supID = id + "." + f.getID();
					if (!data.isDoomed() && (supID.equals(data.getAssignedID())) && (taxa==null || data.getTaxa() == taxa) && (dataClass == null || compatibleMatrix(dataClass, data))) {
						return data;
					}
				}
			}
			for (int i=datas.size()-1; i>=0; i--) {
				mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
				if (!data.isDoomed() && ((id.indexOf('.') < 0 && id.equals(Long.toString(data.getAssignedIDNumber()))) ||(id.indexOf('.') >= 0 && id.equals(data.getAssignedID()))) && (taxa==null || data.getTaxa() == taxa) && (dataClass == null || compatibleMatrix(dataClass, data))) {
					return data;
				}
			}
		}
		else { //using name or number
			for (int i=datas.size()-1; i>=0; i--) {
				mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);

				if (!data.isDoomed() && data.getName().equalsIgnoreCase(ref) && (f==null || data.getFile() == f) && (taxa==null || data.getTaxa() == taxa)&& (dataClass == null || compatibleMatrix(dataClass, data)))
					return data;
			}
			return getCharacterMatrix(f, taxa, dataClass, MesquiteInteger.fromString(ref)); //try to see if number
		}
		return null;  //CHECK FOR COMPATIBLE
	}
	/*.................................................................................................................*/
	/** returns how many character matrices have this name */
	public int getNumberCharacterMatricesWithName(MesquiteFile f, Taxa taxa, Object dataClass, String name) {  
		int count = 0;
		for (int i=datas.size()-1; i>=0; i--) {
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);

			if (!data.isDoomed() && data.getName().equalsIgnoreCase(name) && (f==null || data.getFile() == f) && (taxa==null || data.getTaxa() == taxa)&& (dataClass == null || compatibleMatrix(dataClass, data)))
				count++;
		}

		return count;
	}

	/*.................................................................................................................*/
	/** gets the number (index position) of the data set. */
	public int getMatrixNumber(mesquite.lib.characters.CharacterData data) {   //core
		for (int i=0; i<datas.size(); i++) {
			if ((mesquite.lib.characters.CharacterData)datas.elementAt(i) == data)
				return i;
		}
		return -1;
	}
	/*.................................................................................................................*/
	/** returns a string that can be used to refer to  taxa block */
	public String getCharMatrixReferenceInternal(mesquite.lib.characters.CharacterData data) {   //Core, strat tools
		if (data == null)
			return null;
		return "#" + data.getAssignedID();
	}
	/*.................................................................................................................*/
	/** returns a string that can be used to refer to  taxa block */
	public String getCharMatrixReferenceExternal(mesquite.lib.characters.CharacterData data) {
		if (data == null)
			return null;
		return "#" + data.getAssignedIDNumber();
	}
	/*.................................................................................................................*/
	public mesquite.lib.characters.CharacterData chooseData(MesquiteWindow frame, Taxa taxa, Object dataClass, String message){ 
		return chooseData(frame, null, taxa, dataClass, message);
	}
	/*.................................................................................................................*/
	public mesquite.lib.characters.CharacterData chooseData(MesquiteWindow frame, MesquiteFile file, Taxa taxa, Object dataClass, String message){
		return chooseData(frame, file, taxa, dataClass, message, false);
	}
	/*.................................................................................................................*/
	public mesquite.lib.characters.CharacterData chooseData(MesquiteWindow frame, MesquiteFile file, Taxa taxa, Object dataClass, String message, boolean askEvenIfOne){
		return chooseData(frame, file, taxa, dataClass, message, askEvenIfOne,null,null);

	}
	/*.................................................................................................................*/
	public mesquite.lib.characters.CharacterData chooseData(MesquiteWindow frame, MesquiteFile file, Taxa taxa, Object dataClass, String message, boolean askEvenIfOne, String okButton, String cancelButton){
		int num = getNumberCharMatrices(file, taxa, dataClass, true);
		if (num==0)
			return null;
		else if ((num==1 && !askEvenIfOne) || (MesquiteThread.isScripting() && num>=1))
			return getCharacterMatrix(file, taxa, dataClass, 0, true);
		String[] dataNames = new String[num];
		for (int i=0; i<num;i++) 
			dataNames[i] = getCharacterMatrix(file, taxa, dataClass, i, true).getName();
		int whichData = ListDialog.queryList(frame, "Select data matrix", message, MesquiteString.helpString, dataNames, 0, okButton, cancelButton);
		if (!MesquiteInteger.isCombinable(whichData)) 
			return null;
		else if (whichData>=0) 
			return getCharacterMatrix(file, taxa, dataClass, whichData, true);
		else
			return getCharacterMatrix(file, taxa, dataClass, 0, true);
	}
	/*.................................................................................................................*/
	public mesquite.lib.characters.CharacterData chooseDataExactClass(MesquiteWindow frame, MesquiteFile file, Taxa taxa, Object dataClass, String message, boolean askEvenIfOne, String okButton, String cancelButton){
		int num = getNumberCharMatricesExactClass(file, taxa, dataClass, true);
		if (num==0)
			return null;
		else if ((num==1 && !askEvenIfOne) || (MesquiteThread.isScripting() && num>=1))
			return getCharacterMatrixExactClass(file, taxa, dataClass, 0, true);
		String[] dataNames = new String[num];
		for (int i=0; i<num;i++) 
			dataNames[i] = getCharacterMatrixExactClass(file, taxa, dataClass, i, true).getName();
		int whichData = ListDialog.queryList(frame, "Select data matrix", message, MesquiteString.helpString, dataNames, 0, okButton, cancelButton);
		if (!MesquiteInteger.isCombinable(whichData)) 
			return null;
		else if (whichData>=0) 
			return getCharacterMatrixExactClass(file, taxa, dataClass, whichData, true);
		else
			return getCharacterMatrixExactClass(file, taxa, dataClass, 0, true);
	}
	/* ---------------------------- CHARACTER MODELS ------------------------------*/
	/*.................................................................................................................*/
	/** adds the character model to those stored in the project 
 	public void addCharacterModel(CharacterModel model) {
 		charModels.addElement(model);
 	}
 	/*.................................................................................................................*/
	/** returns the vector of stored character models */
	public ModelVector getCharacterModels() {
		return charModels;
	}
	/*.................................................................................................................*/
	/** returns the number of character models stored */
	public int getNumModels() {
		if (charModels == null)
			return 0;
		return charModels.size();
	}
	/*.................................................................................................................*/
	/** returns a vector of stored character models that are subclasses of the given class*/
	public Listable[] getCharacterModels(Class c, Class stateClass) {
		if (charModels == null || c==null)
			return null;
		int numModels = 0;
		for (int i=0; i<getNumModels(); i++) {
			CharacterModel mod = getCharacterModel(i);
			if (c.isAssignableFrom(mod.getClass()) && (stateClass==null || mod.getStateClass() == stateClass))
				numModels++;
		}
		Listable[] mV = new Listable[numModels];
		int count = 0;
		for (int i=0; i<getNumModels(); i++) {
			CharacterModel mod = getCharacterModel(i);
			if (c.isAssignableFrom(mod.getClass()) && (stateClass==null || mod.getStateClass() == stateClass))
				mV[count++] = mod;
		}
		return mV;
	}
	/*.................................................................................................................*/
	/** returns a vector of stored character models that are compatible with the passed information*/
	public Listable[] getCharacterModels(ModelCompatibilityInfo mci, Class stateClass) {
		if (charModels == null || mci==null)
			return null;
		int numModels = 0;
		for (int i=0; i<getNumModels(); i++) {
			CharacterModel mod = getCharacterModel(i);
			if (mod.isCompatible(mci, null, null))
				numModels++;
		}
		Listable[] mV = new Listable[numModels];
		int count = 0;
		for (int i=0; i<getNumModels(); i++) {
			CharacterModel mod = getCharacterModel(i);
			if (mod.isCompatible(mci, null, null))
				mV[count++] = mod;
		}
		return mV;
	}
	/*.................................................................................................................*/
	public CentralModelListener getCentralModelListener(){
		return modelListener;
	}
	/*.................................................................................................................*/
	/** returns the index'th character model. */
	public CharacterModel getCharacterModel(int index) {
		return (CharacterModel)charModels.elementAt(index);
	}
	/*.................................................................................................................*/
	/** returns the index'th character model that is a subclass of c. */
	public CharacterModel getCharacterModel(ModelCompatibilityInfo mci, int index) {
		if (charModels == null || mci==null)
			return null;
		int count = 0;
		for (int i=0; i<getNumModels(); i++) {
			CharacterModel mod = getCharacterModel(i);
			if (mod.isCompatible(mci, null, null)) {
				if (index== count)
					return mod;
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the character model of name "name' that is a subclass of c. */
	public CharacterModel getCharacterModel(ModelCompatibilityInfo mci, String name) {
		if (charModels == null || mci==null || StringUtil.blank(name))
			return null;
		int count = 0;
		for (int i=0; i<getNumModels(); i++) {
			CharacterModel mod = getCharacterModel(i);
			if (mod.isCompatible(mci, null, null)) {
				if (!StringUtil.blank(mod.getName()) && name.equalsIgnoreCase(mod.getName()))
					return mod;
				if (!StringUtil.blank(mod.getNEXUSName()) &&  name.equalsIgnoreCase(mod.getNEXUSName()) )
					return mod;
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the index of the passed character model among compatible models. Added Apr 02*/
	public int getWhichCharacterModel(ModelCompatibilityInfo mci, CharacterModel model) {
		if (charModels == null || model == null)
			return -1;
		if (mci == null){
			int count = 0;
			for (int i=0; i<getNumModels(); i++) {
				CharacterModel mod = getCharacterModel(i);
				if (model == mod || model.getMother()== mod)  
					return count;
				count++;
			}
			return -1;
		}
		int count = 0;
		for (int i=0; i<getNumModels(); i++) {
			CharacterModel mod = getCharacterModel(i);
			if (mod.isCompatible(mci, null, null)) {
				if (model == mod || model.getMother()== mod)  
					return count;
				count++;
			}
		}
		return -1;
	}
	/*.................................................................................................................*/
	/** returns the index'th character model that is a subclass of c. */
	public CharacterModel getCharacterModel(Class c, boolean allowBuiltIn, int index) {
		if (charModels == null || c==null)
			return null;
		int count = 0;
		for (int i=0; i<getNumModels(); i++) {
			CharacterModel mod = getCharacterModel(i);
			if (c.isAssignableFrom(mod.getClass()) && (allowBuiltIn || !mod.isBuiltIn())) {
				if (index== count)
					return mod;
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the index'th character model that is a subclass of c. */
	public CharacterModel getCharacterModel(Class c, int index) {
		return getCharacterModel(c, true, index);
	}
	/*.................................................................................................................*/
	/** returns the character model with the given name */
	public CharacterModel getCharacterModel(String name) {
		return charModels.getCharacterModel(name);
	}
	/* ---------------------------- HNODE ------------------------------*/
	/* ---------------- for HNode interface ----------------------*/
	/** for HNode interface */
	public HNode[] getHDaughters(){
		ListableVector files = getFiles();
		if (files == null || files.size()== 0)
			return null;
		HNode[] daughters = new HNode[files.size()];
		for (int i = 0; i < files.size(); i++)
			daughters[i] = (HNode)files.elementAt(i);
		return daughters;
	}
	/* ---------------- for HNode interface ----------------------*/
	/** for HNode interface */
	public HNode getHMother(){
		return null; //TODO: should return a class that contains links to all open projects
	}
	/* ---------------- for HNode interface ----------------------*/
	/** for HNode interface */
	public int getNumSupplements(){
		return 0;
	}
	/* ---------------- for HNode interface ----------------------*/
	/** for HNode interface */
	public String getSupplementName(int index){
		return null;
	}
	/* ---------------- for HNode interface ----------------------*/
	/** for HNode interface */
	public void hNodeAction(Container c, int x, int y, int action){
		if (c==null)
			return;
		if (action == HNode.MOUSEDOWN){
			MesquitePopup popup = new MesquitePopup(c);
			popup.addItem(getName(), ownerModule, null);
			popup.addItem("Bring All Windows To Front", ownerModule, ownerModule.makeCommand("allToFront", ownerModule));
			popup.addItem("Close Project", ownerModule, ownerModule.makeCommand("closeProject", ownerModule));
			popup.showPopup( x,y);
		}
		else if (action == HNode.MOUSEMOVE){
			MesquiteWindow f = MesquiteWindow.windowOfItem(c);
			if (f != null && f instanceof MesquiteWindow){
				String st = "This represents a project, which currently represents information from ";
				if (getNumberLinkedFiles() == 1) {
					st += " a single file.";
					if (!StringUtil.blank(getHomeDirectoryName()))
						st += " The file is located at " + getHomeDirectoryName() + ".";
				}
				else {
					st += getNumberLinkedFiles() + ". The home file of the project is " + getHomeFileName();
					if (!StringUtil.blank(getHomeDirectoryName()))
						st +=  " and is located at " + getHomeDirectoryName() + ".";
				}
				((MesquiteWindow)f).setExplanation(st);
			}
		}
		else if (action == HNode.MOUSEEXIT){
			/*
			Frame f = ownerModule.containerOfModule();
			if (f != null && f instanceof MesquiteWindow){
				((MesquiteWindow)f).setExplanation("");
			}
			 */
		}
	}
	/* ---------------- for HNode interface ----------------------*/
	/** for HNode interface */
	public String getTypeName(){
		return null; //"Project"; not needed since name of project includes "Project"
	}
	/* ---------------- for HNode interface ----------------------*/
	/** for HNode interface */
	public void hSupplementTouched(int index){
	}
	/* ---------------- for HNode interface ----------------------*/
	public Image getHImage(){
		return null;
	}
	/* ---------------- for HNode interface ----------------------*/
	public Color getHColor(){
		return ColorTheme.getInterfaceBackgroundPale();  //project color
	}
	/* ---------------- for HNode interface ----------------------*/
	public boolean getHShow(){
		return true; 
	}
}


