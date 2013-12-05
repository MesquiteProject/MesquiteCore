/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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
import java.util.*;
import mesquite.lib.duties.*;
/* ======================================================================== */
/**A tree block.  Trees are added to it when trees are read from file or stored.  Many methods could be built here, for 
instance to go through all trees and prune taxa deleted.
Translation table is stored with each TreeVector.*/
public class TreeVector extends ListableVector implements Trees, Commandable, CompatibilityChecker, MesquiteListener {
	Taxa taxa = null;
	TranslationTable translationTable;
	public static long totalCreated = 0;
	public static long totalDisposed = 0;
	long id;
	private boolean writeTreeWeights = false;
	public TreeVector  (Taxa taxa) {
		super();
		setTaxa(taxa);
		totalCreated++;
		id = totalCreated;
	}
	public int getNumberOfTrees(){
		return size();
	}
	public String getDefaultIconFileName(){ //for small 16 pixel icon at left of main bar
		return "treesSmall.gif";
	}
	/*.................................................................................................................*/
	public String searchData(String s, MesquiteString commandResult) {
		if (commandResult != null)
			commandResult.setValue((String)null);
		if (StringUtil.blank(s))
			return null;
		String list = "";
		String fc =""; //to receive the direct command
		int numFound = 0;
		for (int it = 0; it< size(); it++){
			Tree tree = getTree(it);
			String name = tree.getName();
			if (name != null && StringUtil.foundIgnoreCase(name, s)){
				list += "<li>Tree " + (it+1) + ": <strong>" + StringUtil.protectForXML(name) + "</strong>. <a href=\"selectTree:" + it + " " + getID() + "\">Touch tree</a></li>";
				numFound++;
				fc = "selectTree:" + it+ " " + getID() ;
			}
		}
		if (commandResult != null && numFound == 1)
			commandResult.setValue(fc);
		if (StringUtil.blank(list))
			return list;
		return "<h2>Tree block \"" + StringUtil.protectForXML(getName()) + "\"</h2><ul>" + list + "</ul>";
	}

	/*.................................................................................................................*/
	public void attachCloneToFile(MesquiteFile f, ElementManager manager){
		TreeVector trees = new TreeVector(getTaxa());
		trees.setName(getName());
		trees.addToFile(f, getProject(), manager); 
		trees.setWriteWeights(getWriteWeights());
		for (int i=0; i<size(); i++){
			trees.addElement(elementAt(i), false);
			
		}
	}
	/*.................................................................................................................*/
	public void addElement(Listable obj, boolean notify) {
		super.addElement(obj, false);
		if (obj instanceof MesquiteTree)
			((MesquiteTree)obj).setTreeVector(this);
		if (obj instanceof Listened){
			((Listened)obj).addListener(this);
		}
		resetAssignedNumbers();
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {size()-2,1}));
	}
	public void insertElementAt(Listable obj, int index, boolean notify) {
		super.insertElementAt(obj, index, false);
		if (obj instanceof MesquiteTree)
			((MesquiteTree)obj).setTreeVector(this);
		if (obj instanceof Listened){
			((Listened)obj).addListener(this);
		}
		resetAssignedNumbers();
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {index,1}));
	}
	public void removeElement(Listable obj, boolean notify) {
		if (obj == null)
			return;
		super.removeElement(obj, false);
		if (obj instanceof MesquiteTree)
			((MesquiteTree)obj).setTreeVector(null);
		if (obj instanceof Listened){
			((Listened)obj).removeListener(this);
		}
		resetAssignedNumbers();
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
	}
	public void removeElementAt(int index, boolean notify) {
		Object obj = elementAt(index);
		if (obj == null)
			return;
		super.removeElementAt(index, false);
		if (obj instanceof MesquiteTree)
			((MesquiteTree)obj).setTreeVector(null);
		if (obj instanceof Listened){
			((Listened)obj).removeListener(this);
		}
		resetAssignedNumbers();
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
	}
	public void removeAllElements(boolean notify){
		for (int i=0; i<size(); i++){
			Object obj = elementAt(i);
			if (obj instanceof Listened){
				((Listened)obj).removeListener(this);
			}

		}
		super.removeAllElements(notify);
	}
	public void replaceElement(Listable old, Listable replacement, boolean notify) {
		if (old instanceof Listened){
			((Listened)old).removeListener(this);
		}
		if (old instanceof MesquiteTree)
			((MesquiteTree)old).setTreeVector(null);
		super.replaceElement(old, replacement, false);
		if (replacement instanceof MesquiteTree)
			((MesquiteTree)replacement).setTreeVector(null);
		if (replacement instanceof Listened){
			((Listened)replacement).removeListener(this);
		}
		resetAssignedNumbers();
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
	}

	private void resetAssignedNumbers(){
		for (int i=0; i<size(); i++)
			if (elementAt(i) instanceof MesquiteTree)
				((MesquiteTree)elementAt(i)).setAssignedNumber(i);
	}

	/*.................................................................................................................*/
	public int indexOfByTopology(Tree tree, boolean checkBranchLengths){
		if (tree == null)
			return -1;
		for (int i=0; i< size(); i++){
			if (tree.equalsTopology((Tree)elementAt(i), checkBranchLengths)){
				return i;
			}
		}
		return -1;
	}
	/*.................................................................................................................*/
	/** gets the explanation (footnote) of this tree vector*/
	public String getExplanation() {

		String extra = null;
		if (taxa == null){
			extra = "This block of trees has " + size() + " trees for an unspecified taxa block.\n";
		}
		else 
			extra = "This block of trees has " + size() + " trees for the taxa \"" + taxa.getName() + " \".\n";
		return extra;
	}
	/** returns true if object is set of taxa on which tree vector is based */
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report){
		if (obj instanceof Taxa){
			if (obj != taxa && report != null)
				report.setValue("tree block applies to a different block of taxa.");
			return (obj == taxa);
		}
		return true;
	}
	/** Returns whether module is compatible with given object*/
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer) {
		return isCompatible(obj, project, prospectiveEmployer, null);
	}
	/**Returns the unique id number of this tree block*/
	public long getID(){
		return id;
	}
	/**Translates internal numbering system to external (currently, 0 based to 1 based*/
	public static int toExternal(int i){
		if (!MesquiteInteger.isCombinable(i))
			return i;
		else
			return i+1;
	}
	/**Translates external numbering system to internal (currently, 1 based to 0 based*/
	public static int toInternal(int i){
		if (!MesquiteInteger.isCombinable(i))
			return i;
		else
			return i-1;
	}
	public void dispose() {
		for (int i=0; i<size(); i++)
			((MesquiteTree)elementAt(i)).dispose();
		removeAllElements(false);
		if (taxa != null)
			taxa.removeListener(this);
		totalDisposed++;
		super.dispose();
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker){
		if (checker.compare(this.getClass(), "Returns identification number of this tree block", null, commandName, "getID")) {
			return new MesquiteInteger((int)id);
		}
		else if (checker.compare(this.getClass(), "Exports the file element", null, commandName, "exportMe")) {
			ElementManager manager = getManager();
			if (manager!=null) {
				((Commandable)manager).doCommand("exportTreesBlock", Integer.toString(getFile().getProject().getFileElementNumber(this, getClass())), CommandChecker.defaultChecker);
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Prepends to all tree names the given string", "[string to prepend]", commandName, "prefixNames")) {
			String prefix = ParseUtil.getFirstToken(arguments, new MesquiteInteger(0));
			if (StringUtil.blank(prefix))
				return null;
			for (int i=0; i<size(); i++) {
				AdjustableTree t = (AdjustableTree)getTree(i);
				if (t!=null) {
					String n = t.getName();
					if (n == null)
						t.setName(prefix);
					else
						t.setName(prefix + n);
				}
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Shows tree in tree window", null, commandName, "showTreesInWindow")) {
			ElementManager manager = getManager();
			if (manager!=null && manager instanceof MesquiteModule) {
				MesquiteFile file = getFile();
				if (file!=null && file.getProject()!=null) {
					CommandRecord oldR = MesquiteThread.getCurrentCommandRecord();
					CommandRecord scr = new CommandRecord(true);
					MesquiteThread.setCurrentCommandRecord(scr);
					((MesquiteModule)manager).doCommand("showTreesInWindow" ,Integer.toString(getFile().getProject().getFileElementNumber(this, getClass())), CommandChecker.defaultChecker);
					MesquiteThread.setCurrentCommandRecord(oldR);
				}
			}
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/** Set the taxa to which the tree block applies */
	public void setTaxa(Taxa taxa) {
		if (this.taxa!=taxa && taxa !=null){
			if (this.taxa!=null)
				this.taxa.removeListener(this);
			this.taxa = taxa; //need to redo translation table
			taxa.addListenerHighPriority(this);
			translationTable = new TranslationTable(taxa);
		}
	}
	/** Get the taxa to which the tree block applies */
	public Taxa getTaxa(){
		return taxa;
	}

	public Tree getTree(int i) {
		if (i>=0 && i<size())
			return (MesquiteTree)elementAt(i);
		else
			return null;
	}
	/** Set the label of the taxon with given name to be the label passed*/
	public void setTranslationLabel(String label, String taxonName, boolean checkDuplicates){
		if (translationTable==null)
			return;
		if (taxa==null)
			return;
		Taxon t = taxa.getTaxon(taxonName, false, true);
		if (t == null) {
			MesquiteMessage.warnProgrammer("Taxon name in translation table doesn't correspond to name of known taxon (\"" + taxonName + "\" [a])");
		}

		translationTable.setLabel(t, label, checkDuplicates);
		setDirty(true);
	}
	/** DOCUMENT */
	public void checkTranslationTable(){
		if (translationTable==null)
			return;
		translationTable.checkDuplicates();
	}

	/** Get the label of the taxon i.  If no label stored, return external taxon number as string*/
	public String getTranslationLabel(String iName) {
		if (StringUtil.blank(iName))
			return null;
		Taxon t = taxa.getTaxon(iName, false, true);
		if (translationTable!=null) {
			String label = translationTable.getLabel(t);
			if (!StringUtil.blank(label))
				return label;
		}
		if (t != null)
			return Integer.toString(Taxon.toExternal(taxa.whichTaxonNumber(t))); 
		return null;
	}
	/** Get the label of the taxon i.  If no label stored, return external taxon number as string*/
	public String getTranslationLabel(int taxonNumber) {
		if (taxa == null || translationTable==null || translationTable.getLabel(taxonNumber)==null)
			return Integer.toString(Taxon.toExternal(taxonNumber)); 
		return translationTable.getLabel(taxonNumber);
	}

	MesquiteInteger pos = new MesquiteInteger(0);

	/** Get the translation table as a string. */
	public String getTranslationTable() {
		if (translationTable==null || taxa == null)
			return null;
		String temp = "";
		for(int i=0; i<taxa.getNumTaxa(); i++) {
			if (i>0)
				temp += ","+ StringUtil.lineEnding();
			String thisLabel = getTranslationLabel(i);
			temp += "\t\t" + thisLabel + " " + StringUtil.tokenize(taxa.getTaxonName(i)) ;
		}
		return temp;
	}
	/** Sets the translation table to match the passed table. */
	public void setTranslationTable(Vector table) {
		if (table==null)
			return;
		for(int i=0; i<table.size(); i++) {
			String taxonName = (ParseUtil.getFirstToken((String)table.elementAt(i),pos));
			String label = (ParseUtil.getToken((String)table.elementAt(i),pos));
			Taxon t = taxa.getTaxon(taxonName, false, true);
			translationTable.setLabel(t, label, false); //note label set even if taxon not found; signal later to return null for whichtaxon number
			if (t==null) {
				MesquiteMessage.warnProgrammer("Taxon name in translation table doesn't correspond to name of known taxon (\"" + taxonName + "\" [b])");
			}
		}
		checkTranslationTable();
	}
	/** returns true if stored translation table matches the passed taxa. */
	public boolean tableMatchesTaxa(Taxa taxa, Vector table) {
		if (table==null)
			return false;
		for(int i=0; i<table.size(); i++) {
			String taxonName = (ParseUtil.getFirstToken((String)table.elementAt(i),pos));
			if (taxa.whichTaxonNumber(taxonName, false, true) <0)
				return false;
		}
		return true;
	}
	/** Returns which taxon number corresponds to the passed taxon label.  Uses the translation table. */
	public int whichTaxonNumber(String label, boolean forgivingOfTruncation) {
		if (taxa==null)
			return -1;
		if (translationTable==null)
			return taxa.whichTaxonNumber(label, false, forgivingOfTruncation);

		int i = taxa.whichTaxonNumber(translationTable.getTaxon(label));
		if (i<0)
			return taxa.whichTaxonNumber(label, false, forgivingOfTruncation); 
		else
			return i;
	}

	/** Get the name of the tree block. */
	public String getName(){
		if (name==null)
			return "Tree Block";
		return name;
	}
	/** returns true if this has name equivalent to default name*/
	public boolean hasDefaultName() {
		return  (name==null) || name.equals("Tree Block");
	}
	/** Set the name of the tree block. */
	public void setName(String name){
		this.name = name;
		if (getHShow()) {
			if (getProject() != null)
				getProject().refreshProjectWindow();
		}
	}

	/** Get the name & details of the tree block. */
	public String toString(){
		return "TreeVector: " + getName() + " [id " + id + " size " + size() + "]";
	}
	/**Set whether to write the tree weight into file in the format TREE <tree name> = [&W weight] tree, where weight is the tree weight.*/
	public void setWriteWeights(boolean useWts){
		writeTreeWeights = useWts;
	}
	/**Get whether to write the tree weight into file in the format TREE <tree name> = [&W weight] tree, where weight is the tree weight.
	 * Called by the ManageTrees.getTreeBlock method when writing trees to a tree block.*/
	public boolean getWriteWeights(){
		return writeTreeWeights;
	}
	public String toHTMLStringDescription(){
		//String s =  "<li>Trees: <strong>" + getName() + ".  </strong> Number of trees: " + size() + ".   (<a href =\"listTrees:" + getID() + "\">List &amp; Manage</a>)  (<a href =\"viewTrees:" + getID() + "\">View, Explore &amp; Edit</a>) (<a href =\"chartTrees:" + getID() + "\">Chart</a>)  (<a href =\"renameTrees:" + getID() + "\">Rename</a>)  (<a href =\"editCommentTrees:" + getID() + "\">Edit Comment</a>)  (<a href =\"deleteTrees:" + getID() + "\">Delete</a>)";
		String s =  "<li>Trees: <strong>" + getName() + ".  </strong> Number of trees: " + size() + ". ";
		String comment = getAnnotation();
		if (!StringUtil.blank(comment))
			s+= "<br><font color=\"#777777\">" + comment + "</font>";
		if (HTMLDescribable.verbose)
			s += "<ul>" + super.toHTMLStringDescription() + "</ul>";
		s += "</li>";
		return s;
	}
	/** Get the list of tree id's. */
	public String listIDs(){
		String s = "Trees: ";
		for (int i=0; i<size(); i++)
			s += " " + getTree(i).getID();
		return s;
	}
	/*-----------------------------------------*/
	long previous = -1;
	boolean suppressNotifyL = false;
	Thread threadOfTreeChange;
	/** For MesquiteListener interface.  Passes which object changed, along with optional integer (e.g. for character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (notification != null && notification.getNotificationNumber() == previous)
			return;
		if (notification != null)
			previous = notification.getNotificationNumber();
		if (obj == taxa){
			if (Notification.appearsCosmetic(notification))
				return;
			int[] parameters = Notification.getParameters(notification);

			if (parameters!=null && parameters.length>=2 && translationTable!=null)
				translationTable.taxaModified(Notification.getCode(notification), parameters[0], parameters[1]);
			for (int i=0; i<size(); i++) {//modified 19 Nov 01
				Object o = elementAt(i);
				if (o !=null && o instanceof MesquiteTree) {
					MesquiteTree tree = (MesquiteTree)o;
					threadOfTreeChange = Thread.currentThread();
					suppressNotifyL = true; //this and previous needed to avoid this call to "changed" from notifying listeners here
					tree.changed(caller, obj, notification);
					suppressNotifyL = false;
					threadOfTreeChange =  null;
				}
			}
			if (Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED){
				boolean deleted = deleteEmptyTrees();
				if (deleted)
					notifyListeners(this, (new Notification(MesquiteListener.PARTS_DELETED).setNotificationNumber(notification.getNotificationNumber()))); 
			}
		}
		else if (!(suppressNotifyL && threadOfTreeChange == Thread.currentThread()) && obj instanceof Tree && caller != this && Notification.getCode(notification) != MesquiteListener.ANNOTATION_CHANGED && Notification.getCode(notification) != MesquiteListener.ANNOTATION_ADDED && Notification.getCode(notification) != MesquiteListener.ANNOTATION_DELETED)
			notifyListeners(this, (new Notification(MesquiteListener.PARTS_CHANGED).setNotificationNumber(notification.getNotificationNumber()))); 
	}
	boolean deleteEmptyTrees(){
		int count = 0;
		while (deleteEmptyTree()){
			count++;
		}
		if (count>0){
			MesquiteTrunk.mesquiteTrunk.logln(Integer.toString(count) + " tree(s) deleted because each contained no taxa.");
	}
	return count>0;
	}
	boolean deleteEmptyTree(){
		for (int i=0; i<size(); i++) {
			Object o = elementAt(i);
			if (o !=null && o instanceof MesquiteTree) {
				MesquiteTree tree = (MesquiteTree)o;
				if (tree.isEmpty()) {
					removeElement(tree, false);
					return true;
				}
			}
		}
		return false;
	}
	/*-----------------------------------------*/
	/** For MesquiteListener interface.  Passes which object was disposed*/
	public void disposing(Object obj){
		if (obj instanceof Taxa) //modified 19 Nov 01
			for (int i=0; i<size(); i++) {
				Object o = elementAt(i);
				if (o !=null && o instanceof MesquiteTree) {
					MesquiteTree tree = (MesquiteTree)o;
					tree.disposing(obj);
				}
			}
	}
	/*-----------------------------------------*/
	/** For MesquiteListener interface.  Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}

	/*-----------------------------------------*/
	/** For HNode interface.  todo: this should be renamed getHNodeShow*/
	public boolean getHShow(){
		return true;
	}
	/*-----------------------------------------*/
	/** For HNode interface.*/
	public String getTypeName(){
		return "Tree block";
	}

	/* ---------------- for use with touched from HNode interface ----------------------*/
	public void addToBrowserPopup(MesquitePopup popup){
		super.addToBrowserPopup(popup);
		ElementManager manager = getManager();
		if (manager!=null && manager instanceof MesquiteModule) {
			MesquiteFile file = getFile();
			if (file!=null && file.getProject()!=null) {
				popup.add(new MenuItem("-"));
				popup.add(new MesquiteMenuItem("Show list of trees \"" + getName() + "\"", MesquiteTrunk.mesquiteTrunk, MesquiteTrunk.mesquiteTrunk.makeCommand("showMe", this)));
				if (getFile()!=null)
					popup.add(new MesquiteMenuItem("Show trees \"" + getName() + "\" in tree window", MesquiteTrunk.mesquiteTrunk, MesquiteTrunk.mesquiteTrunk.makeCommand("showTreesInWindow" , (MesquiteModule)manager),Integer.toString(getFile().getProject().getFileElementNumber(this, TreeVector.class))));
			}
		}
	}
}




