/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.ManageTrees;
/*~~  */

import java.util.*;

import javax.swing.JLabel;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;


/** Manages blocks of trees, including reading the NEXUS blocks of trees */
public class ManageTrees extends TreesManager implements ItemListener {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.lists.TreesList.TreesList.class, "This manages the List of Trees windows.",
				"It is activated automatically. ");
		EmployeeNeed e3 = registerEmployeeNeed(mesquite.lists.TreeblockList.TreeblockList.class, "This manages the List of Tree Blocks windows.",
				"It is activated automatically. ");



		EmployeeNeed e2 = registerEmployeeNeed(TreeBlockFiller.class, "Tree blocks can be generated and added to the file.",
				"To create and fill a new tree block, select Make New Trees Block From... in the Taxa&amp;Trees menu. ");
		e2.setPriority(2);
		e2.setAlternativeEmployerLabel("Trees block manager");
		e2.setEntryCommand("newFilledTreeBlockInt");
		EmployeeNeed e4 = registerEmployeeNeed(TreeInferer.class, "Tree blocks can come from a tree inference method and be added to the file.",
				"To request a tree inference method, select Tree Inference... in the Taxa&amp;Trees menu. ");
		e4.setPriority(2);
		e4.setAlternativeEmployerLabel("Trees block manager");
		e4.setEntryCommand("newFilledTreeBlockInferenceInt");
	}
	ListableVector treesVector;
	ListableVector taxas;
	TreeBlockFiller treeFillerTask;  //For make new trees block from  
	String treeFillerTaxaAssignedID = null;  
	Vector blockListeners = null;
	boolean fillingTreesNow = false;
	MesquiteBoolean separateThreadFill; 
	MesquiteBoolean autoSaveInference ;
	//todo: have a single TreeBlockFiller employee belong to the module causes re-entrancy problems, if several long searches are on separate threads.
	//The searches themselves should work fine, but there is a possibility of user-interface confuses.

	boolean showTreeFiller = false; //adds menu item that can be used to set default tree filler; an aid in writing scripts, for then the tree filler snapshot is put into files
	Vector fillerThreads;  // for the TreeBlockThread and TreeMonitorThreads, to be able to shut them off as needed
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		fillerThreads = new Vector();
		blockListeners = new Vector();
		setMenuToUse(MesquiteTrunk.treesMenu);
		separateThreadFill = new MesquiteBoolean(true);
		autoSaveInference = new MesquiteBoolean(false);
		loadPreferences();
		return true;
	}

	/*.................................................................................................................*/
	RadioButtons interpretation, branchesOrNodes;
	TextField nameField;
	JLabel otherNameLabel, bOn;
	public boolean queryAboutNumericalLabelIntepretation(boolean[] interps, String c, MesquiteString n){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "How to interpret numbers in tree description",  buttonPressed);
		queryDialog.addLargeTextLabel("A tree being read includes numbers written as node labels (e.g., \"" + c + "\"). " +
				"Some programs write special information, e.g. bootstrap frequency or posterior probabilities, as node labels. " +
				" Please indicate how you want these numbers to be interpreted.");
		queryDialog.addLabel("Interpretation:", Label.LEFT);
		String[] names = new String[]{"Treat as text", "Bootstrap frequency",  "Posterior probability", "Consensus frequency", "Other"};
		interpretation = queryDialog.addRadioButtons(names, 0);
		interpretation.addItemListener(this);
		otherNameLabel = queryDialog.addLabel("If Other, give a short name for these numbers (no punctuation!):", Label.LEFT);
		nameField = queryDialog.addTextField("otherValue", 30);
		bOn = queryDialog.addLabel("If Other, please indicate whether the number is associated with the nodes or the branches between the nodes:", Label.LEFT);
		String[] branchNode = new String[]{"Nodes", "Branches"};
		branchesOrNodes = queryDialog.addRadioButtons(branchNode, 0);

		queryDialog.addHorizontalLine(2);
		Checkbox remember = queryDialog.addCheckBox ("Remember this interpretation for the rest of this Mesquite run", false);
		itemStateChanged(null);
		queryDialog.completeAndShowDialog(true);

		boolean ok = (queryDialog.query()==0);

		String name = "";
		if (ok) {
			int interp = interpretation.getValue();
			interps[0] = interp >0;
			if (interp == 1)
				name = "bootstrapFrequency";
			else if (interp == 2)
				name = "posteriorProbability";
			else if (interp == 3)
				name = "consensusFrequency";
			else if (interp == 4){
				name = nameField.getText();
				if (StringUtil.blank(name)){
					name = "otherValue";
				}
				interps[1] = branchesOrNodes.getValue() == 1;
			}
			interps[2] = remember.getState();
			n.setValue(name);
			
		}
		queryDialog.dispose();
		return ok;
	}
	/*.................................................................................................................*/
	public void itemStateChanged(ItemEvent arg0) {
		boolean en = interpretation.getValue()==4;
		nameField.setEnabled(en);	
		branchesOrNodes.setEnabled(0, en);	
		branchesOrNodes.setEnabled(1, en);	
		otherNameLabel.setEnabled(en);
		bOn.setEnabled(en);
	}

	public void elementsReordered(ListableVector v){
		if (v == treesVector){
			NexusBlock.equalizeOrdering(v, getProject().getNexusBlocks());
		}
	}
	public void addBlockListener(MesquiteListener ml){
		blockListeners.addElement(ml);
		for (int i= 0; i<treesVector.size(); i++)
			((TreeVector)treesVector.elementAt(i)).addListener(ml);
	}
	public void removeBlockListener(MesquiteListener ml){
		blockListeners.removeElement(ml);
		for (int i= 0; i<treesVector.size(); i++)
			((TreeVector)treesVector.elementAt(i)).removeListener(ml);
	}
	/*.................................................................................................................*/
	public MesquiteModule showElement(FileElement e){
		if (e instanceof TreeVector)
			return showTreesList((TreeVector)e);
		return null;
	}
	public void deleteElement(FileElement e){
		if (e instanceof TreeVector){
			TreeVector trees = (TreeVector)e;

			trees.doom();
			getProject().removeFileElement(trees);//must remove first, before disposing

			trees.dispose();
		}
	}
	/*.................................................................................................................*/
	MesquiteModule showTreesList(TreeVector trees){
		//Check to see if already has lister for this
		boolean found = false;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant)
				if (((ManagerAssistant)e).showing(trees)) {
					((ManagerAssistant)e).getModuleWindow().setVisible(true);
					return ((ManagerAssistant)e);
				}
		}
		ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize("Trees List"));
		if (lister==null){
			alert("Sorry, no module was found to list the trees");
			return null;
		}
		lister.showListWindow(trees);
		if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
			lister.getModuleWindow().setVisible(true);
		return lister;
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (fillerThreads != null){
			int numThreads = fillerThreads.size();
			for (int i = numThreads-1; i>=0; i--){
				FillerThread thread = (FillerThread)fillerThreads.elementAt(i);
				thread.stopFilling();
				thread.threadGoodbye();
				thread.interrupt();
			}
		}
		if (taxas!=null) {
			int numTaxas = taxas.size();
			for (int i=0; i<numTaxas; i++){
				Taxa taxa = (Taxa)taxas.elementAt(i);
				taxa.removeListener(this);
			}
			taxas.removeListener(this);
		}
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa) {
			Taxa taxa = (Taxa)obj;
			taxa.removeListener(this);
			int numSets = treesVector.size();
			for (int i=numSets-1; i>=0 ; i--) {
				TreeVector treeBlock = (TreeVector)treesVector.elementAt( i);
				if (treeBlock!=null && treeBlock.getTaxa()==taxa) {
					getProject().removeFileElement(treeBlock);//must remove first, before disposing
					treeBlock.dispose();
				}
			}
			/*
			boolean someDeleted = true;
			while (someDeleted){
				someDeleted = false;
				for (int i=numSets; i>=0 && !someDeleted ; i--) {
					TreeVector treeBlock = (TreeVector)treesVector.elementAt( i);
					if (treeBlock!=null && treeBlock.getTaxa()==taxa) {
						treesVector.removeElement(treeBlock, true);
						someDeleted = true;
						treeBlock.dispose();
					}
				}
			}
			 */
			resetAllMenuBars();

		}
	}
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj instanceof ListableVector && obj == taxas) {
			reviseListeners();
		}
		else if (obj instanceof Taxa) {
		}
	}
	/*.................................................................................................................*/
	void reviseListeners(){
		if (taxas==null)
			return;
		int numTaxas = taxas.size();
		for (int i=0; i<numTaxas; i++){
			Taxa taxa = (Taxa)taxas.elementAt(i);
			if (taxa!=null && !taxa.amIListening(this))
				taxa.addListener(this);
		}
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "-", null);
		treesVector = getProject().getTreeVectors(); //new ListableVector();
		MesquiteSubmenuSpec mmis = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "List of Trees", makeCommand("showTrees",  this), treesVector);
		mmis.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "List of Tree Blocks", makeCommand("showTreeBlocks",  this));
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "Delete Tree Blocks...", makeCommand("deleteTreeBlocks",  this));
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "New Empty Block of Trees...", makeCommand("newTreeBlock",  this));
		getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "Make New Trees Block from", makeCommand("newFilledTreeBlockInt",  this), TreeBlockFiller.class);
		if (numModulesAvailable(TreeInferer.class)>0 && MesquiteTrunk.mesquiteModulesInfoVector.findModule(null, "#TreeInferenceCoordinator")==null)  //ExternalTreeSearcher
			getFileCoordinator().addSubmenu(MesquiteTrunk.analysisMenu, "Tree Inference", makeCommand("newFilledTreeBlockInferenceInt",  this), TreeInferer.class);
		MesquiteSubmenuSpec mss = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "Import File with Trees");
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.treesMenu, mss, "Link Contents...", makeCommand("linkTreeFile",  this));
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.treesMenu, mss, "Include Contents...", makeCommand("includeTreeFile",  this));
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.treesMenu, mss, "Include Partial Contents...", makeCommand("includePartialTreeFile",  this));
		MesquiteSubmenuSpec mmis2 = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "Save Copy of Tree Block...", makeCommand("exportTreesBlock",  this),  treesVector);
		mmis2.setBehaviorIfNoChoice(MesquiteSubmenuSpec.SHOW_SUBMENU);
		getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "Save Copies of Tree Blocks", makeCommand("exportTreesBlocks",  this), TreeBlockSource.class);
		if (showTreeFiller)
			getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "SetTreeFillerTask", makeCommand("setTreeSource",  this), TreeBlockFiller.class);
		getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "Save Trees To File from", makeCommand("saveDrectTreeFileInt",  this), TreeSource.class);
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "-", null);
		getFileCoordinator().addModuleMenuItems( MesquiteTrunk.treesMenu, makeCommand("newAssistant",  getFileCoordinator()), FileAssistantT.class);
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "-", null);

		taxas = getProject().getTaxas();

		taxas.addListener(this);
		reviseListeners();
		super.projectEstablished();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (treesVector != null && (file == null || file == getProject().getHomeFile())){
			for (int i = 0; i< treesVector.size(); i++) {
				TreeVector trees = (TreeVector)treesVector.elementAt(i);
				Snapshot fromTrees = trees.getSnapshot(file);
				if (fromTrees != null && fromTrees.getNumLines() > 0) {
					temp.addLine("getTreeBlock " + i);  // a bit of a danger here that treeblock misidentified
					temp.addLine("tell It");
					temp.incorporate(fromTrees, true);
					temp.addLine("endTell");
				}
			}
		}
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals("Trees List")) {
				Object o = e.doCommand("getTreeBlock", null, CommandChecker.defaultChecker);
				if (o !=null && o instanceof TreeVector) {
					int wh = getTreeBlockNumber((TreeVector)o);
					temp.addLine("showTrees " + wh, e); 
				}
			}
		}
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals("Tree Blocks List")) {
				temp.addLine("showTreeBlocks ", e); 
			}
		}

		if (fillingTreesNow && treeFillerTask !=null && treeFillerTask.getReconnectable()!=null){  //Defunct when new system in place
			temp.addLine("restartTreeSource ", treeFillerTask);
			temp.addLine("reconnectTreeSource " + StringUtil.tokenize(treeFillerTaxaAssignedID));
		}
		else if (showTreeFiller && treeFillerTask !=null)  
			temp.addLine("setTreeSource ", treeFillerTask);

		return temp;
	}

	/*.................................................................................................................*/
	MesquiteModule showTrees(TreeVector tx){
		boolean found = false;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant)
				if (((ManagerAssistant)e).showing(tx)) {
					((ManagerAssistant)e).getModuleWindow().setVisible(true);
					return ((ManagerAssistant)e);
				}
		}

		ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, "#TreesList");
		if (lister==null){
			discreetAlert("Sorry, no module was found to list the trees");
			return null;
		}
		lister.showListWindow(tx);
		if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
			lister.getModuleWindow().setVisible(true);
		return lister;
	}

	/*.................................................................................................................*/
	public void exportTreesBlock(TreeVector trees, String path) {
		if (trees==null)
			return;
		incrementMenuResetSuppression();

		FileCoordinator coord = getFileCoordinator();
		MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(path), CommandChecker.defaultChecker); //TODO: never scripting???
		trees.attachCloneToFile(tempDataFile,this);
		coord.writeFile(tempDataFile);

		decrementMenuResetSuppression();
		tempDataFile.close();
	}
	/* ................................................................................................................. */
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis"). */
	public String purposeOfEmployee(MesquiteModule employee) {
		if (employee instanceof TreeBlockFiller)
			return "to make block of trees";
		return "for the trees manager"; // to be overridden
	}

	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns a trees block", "[number of trees block; 0 based]", commandName, "getTreeBlock")) {
			int t = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(t) && t<treesVector.size()) {
				TreeVector tx = (TreeVector)treesVector.elementAt(t);
				return tx;
			}
		}
		else if (checker.compare(this.getClass(), "Shows lists of trees in a trees block", "[number of trees block; 0 based]", commandName, "showTrees")) {
			int t = MesquiteInteger.fromFirstToken(arguments, pos);
			if (StringUtil.blank(arguments) || !MesquiteInteger.isCombinable(t) || t>treesVector.size()) {
				for (int i = 0; i< treesVector.size(); i++) {
					showTrees((TreeVector)treesVector.elementAt(i));
				}
			}
			else {
				TreeVector tx = (TreeVector)treesVector.elementAt(t);
				return showTrees(tx);
			}
		}
		else if (checker.compare(this.getClass(), "Shows a tree window showing a particular trees block", "[number of tree block to show]", commandName, "showTreesInWindow")) {
			int t = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(t) && t<getProject().getNumberOfFileElements(TreeVector.class)) {
				MesquiteModule fCoord = getFileCoordinator();
				MesquiteModule treeWindowCoord = null;
				if (fCoord!=null)
					treeWindowCoord = fCoord.findEmployeeWithName("Tree Window Coordinator");
				if (treeWindowCoord!=null){
					TreeVector trees = (TreeVector)getProject().getFileElement(TreeVector.class, t);
					if (trees == null)
						return null;
					Taxa taxa = trees.getTaxa();
					//send script to tree window coord to makeTreeWindow with set of taxa and then set to stored trees and this tree vector
					int whichTreeBlock = getTreeBlockNumber(taxa, trees);

					//first, find if there is a tree window showing these trees
					MesquiteModule twm = null;
					ListableVector v = treeWindowCoord.getEmployeeVector();
					for (int i = 0; i< v.size(); i++){
						if (v.elementAt(i) instanceof TreeWindowMaker){
							MesquiteWindow tw = ((MesquiteModule)v.elementAt(i)).getModuleWindow();
							if (tw != null){
								Object obj = tw.doCommand("getTreeVector", null, CommandChecker.defaultChecker);
								if (obj != null && obj == trees)
									twm = (MesquiteModule)v.elementAt(i);
							}
						}
					}
					if (twm != null){  // trees block is being shown; go there
						MesquiteWindow tw = twm.getModuleWindow();
						tw.setVisible(true);
						tw.getParentFrame().showFrontWindow();
						return null;
					}


					//not shown; need to make new tree window
					CommandRecord oldCR = MesquiteThread.getCurrentCommandRecord();
					CommandRecord scr = new CommandRecord(true);
					MesquiteThread.setCurrentCommandRecord(scr);
					String commands = "makeTreeWindow " + getProject().getTaxaReferenceInternal(taxa) + "  #BasicTreeWindowMaker; tell It; setTreeSource  #StoredTrees;";
					commands += " tell It; setTaxa " + getProject().getTaxaReferenceInternal(taxa) + " ;  setTreeBlock " + TreeVector.toExternal(whichTreeBlock)  + "; endTell; getWindow; tell It; setSize 400 300; endTell; showWindowForce; endTell; ";
					MesquiteInteger pos = new MesquiteInteger(0);
					Puppeteer p = new Puppeteer(this);
					p.execute(treeWindowCoord, commands, pos, null, false);
					MesquiteThread.setCurrentCommandRecord(oldCR);
					/*
					 */
				}
			}
		}
		else if (checker.compare(this.getClass(), "Saves copy of a trees block to a separate file", "[index of trees block]", commandName, "exportTreesBlock")) {
			int t = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(t) && t< getProject().getNumberOfFileElements(TreeVector.class)) {
				String path = parser.getNextToken();
				int id = MesquiteInteger.fromString(path);
				if (MesquiteInteger.isCombinable(id))
					path = parser.getNextToken();
				TreeVector d = (TreeVector)getProject().getFileElement(TreeVector.class, t);
				if (d!=null) {
					if (StringUtil.blank(path))
						path = MesquiteFile.saveFileAsDialog("Save copy of tree block to file");
					else
						path = MesquiteFile.composePath(getProject().getHomeDirectoryName(), path);
					if (!StringUtil.blank(path))
						exportTreesBlock(d, path);
				}
			}

		}
		else if (checker.compare(this.getClass(), "Saves copies of a series of tree blocks to files", "[name of module to fill the trees blocks]", commandName, "exportTreesBlocks")) {
			//ask user how which taxa, how many characters
			//create chars block and add to file
			//return chars
			if (fillingTreesNow){
				discreetAlert( "Sorry, a new tree block is currently being filled.  You must wait for that to finish before asking for exporting trees blocks.");
				return null;
			}

			TreeVector newTrees=null;
			Taxa taxa = null;
			if (getProject().getNumberTaxas()==0) {
				alert("Trees blocks cannot be created until taxa exist in file.");
				return null;
			}
			else 
				taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to save copies of trees blocks?");
			if (taxa == null)
				return null;
			TreeBlockSource treeBlocksForExportTask;
			treeFillerTaxaAssignedID = getProject().getTaxaReferenceExternal(taxa);
			if (StringUtil.blank(arguments))
				treeBlocksForExportTask = (TreeBlockSource)hireEmployee(TreeBlockSource.class, "Save copies of trees blocks from:");
			else
				treeBlocksForExportTask = (TreeBlockSource)hireNamedEmployee(TreeBlockSource.class, arguments);
			if (treeBlocksForExportTask != null) {
				String basePath = MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)");
				if (StringUtil.blank(basePath)) {
					fireEmployee(treeBlocksForExportTask);
					resetAllMenuBars();
					return null;
				}
				treeBlocksForExportTask.initialize(taxa);

				int num = treeBlocksForExportTask.getNumberOfTreeBlocks(taxa);
				if (!MesquiteInteger.isCombinable(num))
					num = MesquiteInteger.queryInteger(containerOfModule(), "How many trees blocks?", "How many trees blocks of which to save copies?", 10);
				if (!MesquiteInteger.isCombinable(num)) {
					fireEmployee(treeBlocksForExportTask);
					resetAllMenuBars();
					return null;
				}
				for (int iBlock = 0; iBlock<num; iBlock++){
					TreeVector trees = treeBlocksForExportTask.getBlock(taxa, iBlock);
					if (trees!=null)
						exportTreesBlock(trees, basePath + iBlock + ".nex");
				}
				if (!showTreeFiller){
					fireEmployee(treeBlocksForExportTask);
					treeFillerTaxaAssignedID = null;
				}
				resetAllMenuBars();
			}
		}
		else if (checker.compare(this.getClass(), "Shows a list of the stored tree blocks", null, commandName, "showTreeBlocks")) {
			//Check to see if already has lister for this
			boolean found = false;
			for (int i = 0; i<getNumberOfEmployees(); i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ManagerAssistant)
					if ( ((ManagerAssistant)e).getName().equals("Tree Blocks List")) {
						((ManagerAssistant)e).getModuleWindow().setVisible(true);
						return e;
					}
			}
			ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize("Tree Blocks List"));
			if (lister==null){
				alert("Sorry, no module was found to list the tree blocks");
				return null;
			}
			lister.showListWindow(null);
			if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
				lister.getModuleWindow().setVisible(true);
			return lister;
		}
		else if (checker.compare(this.getClass(), "Deletes tree blocks from the project", null, commandName, "deleteTreeBlocks")) {
			Listable[] chosen = ListDialog.queryListMultiple(containerOfModule(), "Select Tree Blocks to Delete", "Select one or more tree blocks to be deleted", (String)null, "Delete", false, getProject().getTreeVectors(), (boolean[])null);
			if (chosen != null){
				for (int i = chosen.length-1; i>=0; i--) {  
					((FileElement)chosen[i]).doom();
				}
				getProject().incrementProjectWindowSuppression();
				for (int i = chosen.length-1; i>=0; i--) {  
					logln("Deleting " + chosen[i].getName());
					deleteElement((FileElement)chosen[i]);
				}
				getProject().decrementProjectWindowSuppression();
			}
		}
		else if (checker.compare(this.getClass(), "Restarts to unfinished tree block filling", "[name of tree block filler module]", commandName, "restartTreeSource")) { 
			TreeBlockFiller temp=  (TreeBlockFiller)replaceEmployee(TreeBlockFiller.class, arguments, "Source of trees", treeFillerTask);
			if (temp!=null) {
				treeFillerTask = temp;
			}
			return treeFillerTask;
		}
		else if (checker.compare(this.getClass(), "Reconnects to unfinished tree block filling", "[name of tree block filler module]", commandName, "reconnectTreeSource")) { 
			TreeBlockMonitorThread thread = new TreeBlockMonitorThread(this, parser.getFirstToken(arguments), treeFillerTask);
			fillingTreesNow = true;
			fillerThreads.addElement(thread);

			thread.start();
			return null;
		}

		else if (checker.compare(this.getClass(), "Informs Manage trees that trees are ready", "[ID of tree block filler module]", commandName, "treesReady")) { 
			// may need to pass more info to be able to connect to right filltask etc, especially if multithreading
			if (treeFillerTask != null){
				String taxaID = parser.getFirstToken(arguments);
				Taxa taxa = null;
				if (taxaID !=null)
					taxa = getProject().getTaxa(taxaID);
				if (taxa == null)
					taxa = getProject().getTaxa(0);
				TreeVector trees = new TreeVector(taxa); 
				treeFillerTask.retrieveTreeBlock(trees, 100);
				trees.addToFile(getProject().getHomeFile(), getProject(), this);
				doneQuery(treeFillerTask, trees.getTaxa(), trees, true);
				fireTreeFiller();
				resetAllMenuBars();
			}
			return null;
		}

		else if (checker.compare(this.getClass(), "Fires the tree source for use in filling newly created tree blocks",null, commandName, "fireTreeSource")) { 
			if (treeFillerTask!=null) {
				fireTreeFiller();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the tree source for use in filling newly created tree blocks", "[name of tree block filler module]", commandName, "setTreeSource")) { 
			if (fillingTreesNow){
				discreetAlert( "Sorry, a new tree block is currently being filled.  You must wait for that to finish before setting a new tree source.");
				return null;
			}
			TreeBlockFiller temp=  (TreeBlockFiller)replaceEmployee(TreeBlockFiller.class, arguments, "Source of trees", treeFillerTask);
			if (temp!=null) {
				treeFillerTask = temp;
			}
			return treeFillerTask;
		}
		else if (checker.compare(this.getClass(), "Links file with trees", null, commandName, "linkTreeFile")) { 
			MesquiteModule fCoord = getFileCoordinator();
			fCoord.doCommand("linkTreeFile", StringUtil.argumentMarker + "fuseTreeBlocks", checker);
		}
		else if (checker.compare(this.getClass(), "Includes file with trees", null, commandName, "includeTreeFile")) { 
			MesquiteModule fCoord = getFileCoordinator();
			fCoord.doCommand("includeTreeFile", null, checker);
		}
		else if (checker.compare(this.getClass(), "Includes file with trees (partial)", null, commandName, "includePartialTreeFile")) { 
			includeTreeFile(commandName, arguments, checker, true);
		}
		else if (checker.compare(this.getClass(), "Includes file with trees (ask if partial)", null, commandName, "includeTreeFileAskPartial")) {
			boolean all = AlertDialog.query(containerOfModule(), "Read All Trees?", "Read all trees, or only a sample of the trees?", "All", "Sample");

			includeTreeFile(commandName, arguments, checker, !all);
		}
		else if (checker.compare(this.getClass(), "Creates a new tree block", "[reference of taxa block] [identification number of file to which tree block will belong] [name of tree block]", commandName, "newTreeBlock")) { 
			//first argument: taxa; second argument: id of file to which to add; third argument name of tree block
			Taxa taxa=null;
			MesquiteFile file=null;
			String listName = null;
			if (getProject().getNumberTaxas()==0){
				discreetAlert("A taxa block must be created first before making a tree block");
				return null;
			}
			if (StringUtil.blank(arguments)){ //no taxa specified && no file specified
				if (getProject().getNumberTaxas()==1 || MesquiteThread.isScripting()) {
					taxa = getProject().getTaxa(0);
				}
				else {
					ListableVector taxas = getProject().getTaxas();
					taxa = (Taxa)ListDialog.queryList(containerOfModule(), "Select taxa", "Select taxa (for new tree block)", MesquiteString.helpString,taxas, 0);
				}
				file = chooseFile( taxa);
			}
			else {
				taxa = getProject().getTaxaLastFirst(parser.getFirstToken(arguments));
				int fileID = MesquiteInteger.fromString(parser.getNextToken());
				if (!MesquiteInteger.isCombinable(fileID))
					file = getProject().getHomeFile();
				else
					file = getProject().getFileByID(fileID);
				listName = parser.getNextToken();
			}
			if (taxa==null || file == null)
				return null;
			if (StringUtil.blank(listName)) {
				if (!MesquiteThread.isScripting()) 
					listName = MesquiteString.queryShortString(containerOfModule(), "Name of trees block", "Name of trees block", treesVector.getUniqueName("Trees"));

				if (StringUtil.blank(listName))
					return null;
			}
			TreeVector trees = makeNewTreeBlock(taxa, listName, file);

			resetAllMenuBars();

			return trees;
		}
		else if (checker.compare(this.getClass(), "Concatenates the last tree block into the second last", null, commandName, "concatLastTwo")) { 
			int n = treesVector.size();
			if (n<2)
				return null;
			TreeVector ultimate = (TreeVector)treesVector.elementAt(n-1);
			TreeVector penultimate = (TreeVector)treesVector.elementAt(n-2);
			int target = n-3;
			while (penultimate.getTaxa()!=ultimate.getTaxa() && target>0)
				penultimate = (TreeVector)treesVector.elementAt(target);
			if (penultimate.getTaxa()!=ultimate.getTaxa())
				return null;
			for (int j=0; j<ultimate.size(); j++){
				Tree tree = ultimate.getTree(j);
				if (tree!=null)
					penultimate.addElement(tree.cloneTree(), false);
			}
			getProject().removeFileElement(ultimate);//must remove first, before disposing
			ultimate.dispose();
		}
		else if (checker.compare(this.getClass(), "Disposes the last block of trees", null, commandName, "disposeLastTreeBlock")) { 
			int n = treesVector.size();
			if (n<1)
				return null;
			TreeVector ultimate = (TreeVector)treesVector.elementAt(n-1);
			getProject().removeFileElement(ultimate);
			ultimate.dispose();
		}
		else if (checker.compare(this.getClass(), "Creates a new filled tree block (internally called, used for scripting)", "[reference of taxa block] [optional -- if id then next token is id not number][number of file in which the tree block should be stored] [name of tree block] [how many trees to make]", commandName, "newFilledTreeBlockIntS")) { 
			if (treeFillerTask == null) //needs to have been previously set
				return null;
			Taxa taxa = getProject().getTaxa(parser.getFirstToken(arguments));
			if (taxa == null)
				return null;
			if (fillingTreesNow){
				discreetAlert( "Sorry, another new tree block is currently being filled.  You must wait for that to finish before asking for another new tree block.");
				return null;
			}
			treeFillerTaxaAssignedID = getProject().getTaxaReferenceExternal(taxa);
			boolean useID = false;
			MesquiteFile file=null;
			String idd = parser.getNextToken();
			if ("home".equalsIgnoreCase(idd))
				file = getProject().getHomeFile();
			else {
				if ("id".equalsIgnoreCase(idd)) {
					idd = parser.getNextToken();
					useID = true;
				}

				int whichFile = MesquiteInteger.fromString(idd);
				if (!MesquiteInteger.isCombinable(whichFile))
					return null;
				if (useID)
					file = getProject().getFileByID(whichFile);
				else
					file = getProject().getFile(whichFile);
			}
			if (file == null)
				return null;
			String name = parser.getNextToken();


			TreeVector trees = new TreeVector(taxa);
			if (trees == null)
				return null;
			int howManyTrees =0;
			if (treeFillerTask instanceof TreeSource)
				howManyTrees =((TreeSource)treeFillerTask).getNumberOfTrees(trees.getTaxa());
			if (!treeFillerTask.hasLimitedTrees(trees.getTaxa())){
				howManyTrees = MesquiteInteger.fromString(parser.getNextToken());
				if (!MesquiteInteger.isCombinable(howManyTrees)) {
					return null;
				}
			}

			int separateThread = 0;
			MesquiteBoolean autoSave = new MesquiteBoolean(false);
			if (!MesquiteThread.isScripting() && treeFillerTask.permitSeparateThreadWhenFilling())
				separateThread= separateThreadQuery("Fill tree block", autoSave, false);
			if (separateThread==1) {  //separateThread
				fillingTreesNow = true;
				TreeBlockThread tLT = new TreeBlockThread(this, treeFillerTask, trees, howManyTrees, autoSave, file);
				fillerThreads.addElement(tLT);
				/*DISCONNECTABLE: have third option, Run and Come Back (Disconnect).  This is available only for some tree block fillers that say they can do it.
				Add to tree block filler a method startTreeFilling(TreesDoneListener this) that is called 
				(not on a separate thread -- that is the responsibility of the tree block filler, as sometimes it will be the filler's own time involved, sometimes
				an external program, and it will be the tree block filler's responsibility to poll for the external being done, and it will have to do that on its own thread.)

				When the tree block filler detects the trees are ready, it calls a method to notify the TreesDoneListener that the trees are ready.  
				This perhaps will be done via an intermediary command on the main thread so that the response is on the main thread.  
				ManageTrees will therefore get notified that the trees are ready, and will therefore ask the tree block filler to actually fill the block of trees, and continue.

				If the file is saved and closed before it's done, ManageTrees should snapshot setOngoingTreeBlockFiller in which it hires the tree block filler and then
				re-registers as the TreesDoneListener.  The tree block filler would save a snapshot to remember what are the locations of the files and external searcher, 
				and the criteria for its being done (e.g. the presence of a file).  When rehired it would load all that and check to see if the criterion was met, starting
				a thread to check, and when done, would notify the TreesDoneListener


				 */
				tLT.start();
			}
			else if (separateThread == 0) {// same thread
				long s = System.currentTimeMillis();
				int before = trees.size();
				treeFillerTask.fillTreeBlock(trees, howManyTrees);

				if (trees.size()==before) {
					logln("Sorry, no trees were returned by " + treeFillerTask.getName());
					return null;
				}

				trees.setName(name);
				logln(Integer.toString(trees.size()) + " trees stored in tree block, from " + treeFillerTask.getName());
				trees.addToFile(file, getProject(), this);
				if (autoSave != null && autoSave.getValue()){
					FileCoordinator fCoord = getFileCoordinator();
					fCoord.writeFile(file);
				}
				return trees;
			}

		}
		else if (checker.compare(this.getClass(), "Creates a new filled tree block (internally called from menu item)", "[name of tree source module]", commandName, "newFilledTreeBlockInt")) {
			if (getProject().getNumberTaxas()==0){
				discreetAlert("A taxa block must be created first before making a tree block");
				return null;
			}
			newTreeBlockFilledInt(commandName, arguments, checker, false, "Fill tree block", false);
		}
		else if (checker.compare(this.getClass(), "Creates a new tree file written directly from a tree source (internally called from menu item)", "[name of tree source module]", commandName, "saveDrectTreeFileInt")) {
			saveDirectTreeFileCoord(commandName, arguments, checker, false);
		}
		else if (checker.compare(this.getClass(), "Creates a new filled tree block (internally called from menu item) using a tree inference procedure", "[name of tree source module]", commandName, "newFilledTreeBlockInferenceInt")) {
			if (getProject().getNumberTaxas()==0){
				discreetAlert("A taxa block must be created first before making a tree block");
				return null;
			}
			newTreeBlockFilledInt(commandName, arguments, checker, true, "Do tree inference", true);  //This handler will be Defunct when new system in place
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*-----------------------------------------------------------------*/
	void includeTreeFile(String commandName, String arguments, CommandChecker checker, boolean partial){
		if (!partial){
			MesquiteModule fCoord = getFileCoordinator();
			fCoord.doCommand("includeTreeFile", null, checker);
			return;
		}
		//this changed considerably between 1.0 and 1.01, using ManyTreesFromFile to avoid memory overflow
		if (!MesquiteThread.isScripting()){  //only non-scripting
			MesquiteInteger buttonPressed = new MesquiteInteger();
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Trees to Include",  buttonPressed);
			IntegerField firstTree = dialog.addIntegerField("First Tree to Read:", 1, 16);
			IntegerField lastTree = dialog.addIntegerField("Last Tree to Read:", MesquiteInteger.infinite, 16);
			IntegerField everyNth = dialog.addIntegerField("Sample Every nth tree:", 1, 16);
			dialog.completeAndShowDialog(true);
			dialog.dispose();
			if (buttonPressed.getValue()!=0) 
				return;
			arguments = "";

			TreeSource temp = (TreeSource)hireNamedEmployee(TreeSource.class, "#mesquite.trees.ManyTreesFromFile.ManyTreesFromFile");
			if (temp == null)
				discreetAlert( "Sorry, the file could not be read because the module \"Trees Directly from File\" could not be started");
			else {
				int start = 0;
				int last = MesquiteInteger.infinite;
				int every = 1;
				if (MesquiteInteger.isCombinable(firstTree.getValue()) && firstTree.getValue() != 1)
					start = firstTree.getValue() - 1;
				if (MesquiteInteger.isCombinable(lastTree.getValue()))
					last = (lastTree.getValue() - 1);
				if (MesquiteInteger.isCombinable(everyNth.getValue()) && everyNth.getValue() != 1)
					every = (everyNth.getValue());
				Tree first = temp.getTree(null, 0);  //get first tree to figure out taxa block!
				if (first == null) {
					fireEmployee(temp);
					discreetAlert( "Sorry, no tree was obtained");
					return;
				}
				Taxa taxa = first.getTaxa();
				MesquiteFile file = chooseFile( taxa);
				if (file == null){
					fireEmployee(temp);
					return;
				}
				TreeVector trees = new TreeVector(taxa);
				Tree tree = null;
				for (int i= start; (i < last) && (i == start || tree !=null); i+= every){
					tree = temp.getTree(taxa, i);
					if (tree !=null)
						trees.addElement(tree, false);
				}

				trees.setName(temp.getParameters());
				trees.addToFile(file, getProject(), this);

				fireEmployee(temp);
				resetAllMenuBars();
			}
		}

	}

	void fireTreeFiller(){
		fireEmployee(treeFillerTask);  
		treeFillerTask = null;
		treeFillerTaxaAssignedID = null;
		fillingTreesNow = false;
	}
	/*-----------------------------------------------------------------*/
	//isInference argument will presumably be defunct when inference switches to new module
	Object newTreeBlockFilledInt(String commandName, String arguments, CommandChecker checker, boolean suppressAsk, String taskName, boolean isInference){
		//arguments that should be accepted: (1) tree source, (2) which taxa, (3)  file id, (4) name of tree block, (5) how many trees  [number of taxa block] [identification number of file in which the tree block should be stored] [name of tree block] [how many trees to make]
		if (fillingTreesNow){
			discreetAlert( "Sorry, another new tree block or tree file is currently being filled.  You must wait for that to finish before asking for another new tree block.");
			return null;
		}
		Taxa taxa=null;
		MesquiteFile file=null;

		if (getProject().getNumberTaxas()==1) 
			taxa = getProject().getTaxa(0);
		else {
			ListableVector taxas = getProject().getTaxas();
			taxa = (Taxa)ListDialog.queryList(containerOfModule(), "Select taxa", "Select taxa (for new trees block)",MesquiteString.helpString, taxas, 0);
		}

		doCommand("setTreeSource", arguments, checker);  

		if (treeFillerTask==null)  
			return null;
		file = chooseFile( taxa);
		if (taxa==null || file == null)
			return null;

		treeFillerTaxaAssignedID = getProject().getTaxaReferenceExternal(taxa);   //don't use the treeFillerTaxaAssignedID of themodule
		TreeVector trees = new TreeVector(taxa);
		if (trees == null)
			return null;
		int howManyTrees = 0;
		if (!treeFillerTask.hasLimitedTrees(trees.getTaxa())){
			if (treeFillerTask instanceof TreeSource)
				howManyTrees =((TreeSource)treeFillerTask).getNumberOfTrees(trees.getTaxa());
			if (!MesquiteInteger.isCombinable(howManyTrees))
				howManyTrees = MesquiteInteger.queryInteger(containerOfModule(), "How many trees?", "How many trees?", 100, 1, 100000000);
			if (!MesquiteInteger.isCombinable(howManyTrees)) {
				return null;
			}
		}
		int separateThread = 0;
		MesquiteBoolean autoSave = new MesquiteBoolean(false);
		if (!MesquiteThread.isScripting() && treeFillerTask.permitSeparateThreadWhenFilling())
			separateThread= separateThreadQuery(taskName, autoSave, isInference);
		if (separateThread==1) {   // separate
			fillingTreesNow = true;
			TreeBlockThread tLT = new TreeBlockThread(this, treeFillerTask, trees, howManyTrees, autoSave, file);
			fillerThreads.addElement(tLT); //Defunct: note!  already multiple threads remembered!
			tLT.suppressAsk = suppressAsk;
			tLT.start();
		}
		else if (separateThread == 0) {// same thread
			long s = System.currentTimeMillis();
			int before = trees.size();
			treeFillerTask.fillTreeBlock(trees, howManyTrees);
			if (trees.size()==before) {
				alert("Sorry, no trees were returned by " + treeFillerTask.getName());
				return null;
			}
			if (trees.getName()==null || "Untitled".equalsIgnoreCase(trees.getName()))
				trees.setName("Trees from " + treeFillerTask.getName());
			trees.addToFile(file, getProject(), this);

			doneQuery(treeFillerTask, taxa, trees, suppressAsk);
			if (!showTreeFiller){
				fireTreeFiller();
			}
			if (autoSave != null && autoSave.getValue()){
				FileCoordinator fCoord = getFileCoordinator();
				fCoord.writeFile(file);
			}
			resetAllMenuBars();
		}
		return null;
	}
	/*-----------------------------------------------------------------*/
	Object saveDirectTreeFileCoord(String commandName, String arguments, CommandChecker checker, boolean suppressAsk){
		//arguments that should be accepted: (1) tree source, (2) which taxa, (3)  file id, (4) name of tree block, (5) how many trees  [number of taxa block] [identification number of file in which the tree block should be stored] [name of tree block] [how many trees to make]
		Taxa taxa=null;

		if (getProject().getNumberTaxas()==1) 
			taxa = getProject().getTaxa(0);
		else {
			ListableVector taxas = getProject().getTaxas();
			taxa = (Taxa)ListDialog.queryList(containerOfModule(), "Select taxa", "Select taxa (for new trees block)",MesquiteString.helpString, taxas, 0);
		}
		if (taxa==null)
			return null;
		TreeSource treeSourceTask =  null;
		if (arguments == null)
			treeSourceTask = (TreeSource)hireEmployee(TreeSource.class, "Source of Trees for File");
		else
			treeSourceTask =  (TreeSource)hireNamedEmployee(TreeSource.class, arguments, null);

		if (treeSourceTask==null)
			return null;
		treeSourceTask.setUseMenubar(false);

		int howManyTrees = 0;
		if (!treeSourceTask.hasLimitedTrees(taxa)){
			if (treeSourceTask instanceof TreeSource)
				howManyTrees =((TreeSource)treeSourceTask).getNumberOfTrees(taxa);
			if (!MesquiteInteger.isCombinable(howManyTrees))
				howManyTrees = MesquiteInteger.queryInteger(containerOfModule(), "How many trees?", "How many trees?", 100, 1, 100000000);
			if (!MesquiteInteger.isCombinable(howManyTrees)) {
				return null;
			}
		}
		else
			howManyTrees = treeSourceTask.getNumberOfTrees(taxa);
		int separateThread = 0; 
		if (!MesquiteThread.isScripting() && treeSourceTask.permitSeparateThreadWhenFilling())
			separateThread= AlertDialog.query(containerOfModule(), "Separate Thread?", "Save tree file on separate thread? (Beware! If you use a separate thread, be careful not to reorder, delete, add or rename taxa while this calculation is in progress)","No", "Separate", "Cancel", 0, null);
		MainThread.incrementSuppressWaitWindow();
		MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "File in which to Save Trees", FileDialog.SAVE);   // Save File dialog box
		fdlg.setVisible(true);
		String tempFileName=fdlg.getFile();
		String tempDirectoryName=fdlg.getDirectory();
		MainThread.decrementSuppressWaitWindow();
		MesquiteFile file = MesquiteFile.newFile(tempDirectoryName, tempFileName);
		if (separateThread==1) {   // separate
			DirectTreeFileThread tLT = new DirectTreeFileThread(this, treeSourceTask, taxa, howManyTrees, file);
			fillerThreads.addElement(tLT);
			tLT.suppressAsk = suppressAsk;
			tLT.start();
		}
		else if (separateThread == 0) {// same thread
			if (!saveDirectTreeFile(treeSourceTask, taxa, howManyTrees, file))

				alert("Sorry, no trees were returned by " + treeSourceTask.getName());
			return null;
		}

		return null;
	}	

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("separateThreadFill".equalsIgnoreCase(tag))
			separateThreadFill.setValue(MesquiteBoolean.fromTrueFalseString(content));

		if ("autoSaveInference".equalsIgnoreCase(tag))
			autoSaveInference.setValue(MesquiteBoolean.fromTrueFalseString(content));
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "separateThreadFill", separateThreadFill);  
		StringUtil.appendXMLTag(buffer, 2, "autoSaveInference", autoSaveInference);  
		return buffer.toString();
	}
	int separateThreadQuery(String taskName, MesquiteBoolean autoSave, boolean isInference){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		String title = "Separate Thread?";
		if (isInference)
			title = "Separate Thread & Auto-Save?";
		ExtensibleDialog id = new ExtensibleDialog(containerOfModule(), title,buttonPressed);
		id.addLabel (taskName + " on separate thread?", Label.LEFT, true, false);
		id.addLargeTextLabel("Beware! If you use a separate thread, be careful not to reorder, delete, add or rename taxa while this calculation is in progress");
		String s = "This dialog box establishes basic running options for this calculation. ";
		s += "<h3>" + StringUtil.protectForXML("Separate Thread?") + "</h3>Mesquite can do multi-tasking (i.e., do multiple things at once) by placing different tasks on different computational \"threads\".";
		s += " There is one main thread that controls the user interface. You can place the calculation you are about to do on this thread, ";
		s += " or you can place it on a separate thread.  If you place it on the main thread you will not have control over the user interface, ";
		s += " and you thus cannot interact fully with Mesquite.  If you place it on a separate thread, you will be able to interact fully with Mesquite.";
		s += " The danger in placing it on a separate thread is that you might (while the calculations are proceeding) change your taxa in such a way";
		s += " that the taxa in your file will differ from those used in the running calculation and there will be an unrecoverable conflict. ";
		s += " Thus, placing the calculation on a separate thread is more convenient, put has potential risks if you are not careful. ";
		s += "<h3>" + StringUtil.protectForXML("Auto-save?") + "</h3>If you choose the option to auto-save,  Mesquite will save your file as soon as the calculation completes.";
		s += " This is of benefit should you be worried that something might happen (e.g., unexpected computer shutdown) before you have a chance to manually save the file.";
		id.appendToHelpString(s);
		int choice = 0;
		if (separateThreadFill.getValue())
			choice = 1;
		RadioButtons radio = id.addRadioButtons (new String[]{"NOT separate thread", "Separate Thread"}, choice);
		id.addBlankLine();
		if (isInference){
			id.addBlankLine();
			id.addLabel ("Save file automatically after trees are finished?", Label.LEFT, true, false);
			choice = 0;
			if (!autoSaveInference.getValue())
				choice = 1;
			RadioButtons radio2 = id.addRadioButtons (new String[]{"Auto-Save", "Don't Save"}, choice);


			id.addBlankLine();
			id.completeAndShowDialog("OK", null, null, "OK");
			id.dispose();
			if (autoSave != null){
				autoSave.setValue(radio2.getValue() == 0);
				autoSaveInference.setValue(autoSave.getValue());
			}
		}
		else {
			id.completeAndShowDialog("OK", null, null, "OK");
			id.dispose();
		}
		separateThreadFill.setValue(radio.getValue() == 1);   
		storePreferences();
		return radio.getValue();
	}
	/*.................................................................................................................*/
	boolean saveDirectTreeFile(TreeSource treeSourceTask, Taxa taxa, int howManyTrees, MesquiteFile file){
		String endLine = ";" + StringUtil.lineEnding();
		MainThread.incrementSuppressWaitWindow();
		ProgressIndicator progIndicator = null;
		boolean done = false;
		int treesWritten = 0;
		for (int i=0; i< howManyTrees && !done; i++){
			Tree t = treeSourceTask.getTree(taxa, i);
			if (i == 0) {
				if (t == null){
					fireEmployee(treeSourceTask);
					resetAllMenuBars();
					return false;
				}
				else {
					//=== Preparation, done only once first tree successfully found
					progIndicator = new ProgressIndicator(getProject(),getName(), "Saving trees", howManyTrees, true);
					if (progIndicator!=null){
						progIndicator.setButtonMode(ProgressIndicator.OFFER_CONTINUE);
						progIndicator.setOfferContinueMessageString("Are you sure you want to stop the tree saving?");
						progIndicator.start();
					}

					file.openWriting(true);
					file.write("#NEXUS");
					Date d = new Date(System.currentTimeMillis());
					String s = "";
					String loc = "";
					try {
						loc = " at " + java.net.InetAddress.getLocalHost();
					}
					catch (java.net.UnknownHostException e){
					}
					if (!MesquiteModule.author.hasDefaultSettings())
						loc += " (" + author.getName() + ")";
					file.writeLine("[written " + d.toString() + " by Mesquite " + s + " version " + getMesquiteVersion()  + getBuildVersion() + loc + "]"); 
					file.write("BEGIN TREES");
					file.write(endLine);
					if (MesquiteFile.okToWriteTitleOfNEXUSBlock(file, taxa)&& getProject().getNumberTaxas()>1){ //��� should have an isUntitled method??
						file.write("\tLINK Taxa = " + StringUtil.tokenize(taxa.getName()));
						file.write(endLine);
					}
					file.write("[! Trees from source: " + StringUtil.tokenize(treeSourceTask.getNameAndParameters()) + "]");
					file.write(StringUtil.lineEnding());
					file.write("\tTRANSLATE" + StringUtil.lineEnding());
					String tt = "";
					if (taxa!=null)
						for(int k=0; k<taxa.getNumTaxa(); k++) {
							if (k>0)
								tt += ","+ StringUtil.lineEnding();
							tt += "\t\t" + Taxon.toExternal(k) + "\t" + StringUtil.tokenize(taxa.getTaxonName(k)) ;
						}
					file.write( tt);
					file.write(endLine);
					//======== end Preparation
				}
			}
			else if (t == null)
				done = true;
			if (file == null)
				return false;
			if (t != null){
				file.write("\tTREE ");
				if (t instanceof MesquiteTree && !StringUtil.blank(((MesquiteTree)t).getAnnotation())) {
					String s = ((MesquiteTree)t).getAnnotation();
					s= StringUtil.replace(s, '\n', ' ');
					s=StringUtil.replace(s, '\r', ' ');
					file.write(" [!" + s + "] ");
				}

				file.write(StringUtil.tokenize(t.getName() )+ " = " +  t.writeTree(Tree.BY_NUMBERS) + StringUtil.lineEnding());
				treesWritten++;
			}
			if (progIndicator != null) {
				if (progIndicator.isAborted()) {
					progIndicator.goAway();
					done = true;
				}
				progIndicator.setText("Saved: tree " + (i+1) +  " of " + MesquiteInteger.toString(howManyTrees));
				progIndicator.setCurrentValue(i);
			}

		}
		if (file == null)
			return false;
		if (progIndicator != null)
			progIndicator.goAway();
		file.write("END;" + StringUtil.lineEnding()+ StringUtil.lineEnding());
		file.closeWriting();
		fireEmployee(treeSourceTask);
		discreetAlert( "Tree file saving is complete.  " + treesWritten + " trees written.");
		MainThread.decrementSuppressWaitWindow();

		resetAllMenuBars();
		return true;
	}
	/*.................................................................................................................*/
	MesquiteFile chooseFile( Taxa taxa){
		if (getProject().getNumberLinkedFiles()==1 || taxa == null)
			return getProject().getHomeFile();
		else if (MesquiteThread.isScripting())
			return taxa.getFile();
		else {
			Listable[] files = getProject().getFiles().getElementArray();
			if (files.length >1) {
				int count = 0;
				boolean taxaFound = false;
				for (int i=0; i<files.length; i++) {
					if (!taxaFound && files[i] == taxa.getFile())
						taxaFound = true;
					if (taxaFound)
						count++;
				}
				if (count!=files.length){
					Listable[] legalFiles = new Listable[count];
					count = 0;
					taxaFound = false;
					for (int i=0; i<files.length; i++) {
						if (!taxaFound && files[i] == taxa.getFile())
							taxaFound = true;

						if (taxaFound) {
							legalFiles[count] = files[i];
							count++;
						}
					}
					files = legalFiles;
				}

			}
			if (files.length == 1)
				return (MesquiteFile)files[0];
			return (MesquiteFile)ListDialog.queryList(containerOfModule(), "Select file", "Select file to which to add the new block of trees",MesquiteString.helpString, files, 0);
		}
	}
	/*.................................................................................................................*/

	void doneQuery(TreeBlockFiller fillTask, Taxa taxa, TreeVector trees, boolean suppressAsk){
		MesquiteModule fCoord = getFileCoordinator();
		MesquiteModule treeWindowCoord = null;
		if (fCoord!=null)
			treeWindowCoord = fCoord.findEmployeeWithName("Tree Window Coordinator");
		if (treeWindowCoord==null && fCoord!=null)
			treeWindowCoord = fCoord.findEmployeeWithName("#BasicTreeWindowCoord");

		if (treeWindowCoord==null){
			discreetAlert(MesquiteThread.isScripting(), "The trees are now ready [" + fillTask.getNameAndParameters() + "].");
		}
		else if (!MesquiteThread.isScripting() && (suppressAsk || AlertDialog.query(containerOfModule(), "Trees ready", "The trees are now ready [" + fillTask.getName() + "; name of tree block: \"" + trees.getName()+ "\"].  Would you like to open a tree window to display them?", "Yes", "No"))){
			//send script to tree window coord to makeTreeWindow with set of taxa and then set to stored trees and this tree vector
			int whichTreeBlock = getTreeBlockNumber(taxa, trees);
			String extraWindowCommands = fillTask.getExtraTreeWindowCommands(true);
			if (StringUtil.blank(extraWindowCommands))
				extraWindowCommands="";
			String commands = "makeTreeWindow " + getProject().getTaxaReferenceInternal(taxa) + "  #BasicTreeWindowMaker; tell It; setTreeSource  #StoredTrees;";
			commands += " tell It; setTaxa " + getProject().getTaxaReferenceInternal(taxa) + " ;  setTreeBlock " + TreeVector.toExternal(whichTreeBlock)  + "; endTell;  getWindow; tell It; setSize 400 300; " + extraWindowCommands + " endTell; showWindowForce; endTell; ";
			MesquiteInteger pos = new MesquiteInteger(0);
			Puppeteer p = new Puppeteer(this);
			CommandRecord prev = MesquiteThread.getCurrentCommandRecord();
			CommandRecord cRec = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(cRec);
			p.execute(treeWindowCoord, commands, pos, null, false, null, null);
			MesquiteThread.setCurrentCommandRecord(prev);


		}
	}
	/*.................................................................................................................*/
	public TreeVector getTreeVectorByID(int id){  //OK for doomed
		for (int j = 0; j< treesVector.size(); j++) {
			TreeVector trees = (TreeVector)treesVector.elementAt(j);
			if (trees!= null && trees.getID() ==id)
				return trees;
		}
		return null;
	}
	/*.................................................................................................................*/
	public ListableVector getTreeBlockVector(){
		return treesVector;
	}
	/*.................................................................................................................*/
	public TreeVector getTreeBlock(Taxa taxa, int i){  //OK for doomed
		if (treesVector==null)
			return null;
		int count = 0;
		for (int j = 0; j< treesVector.size(); j++) {
			TreeVector trees = (TreeVector)treesVector.elementAt(j);
			if ((taxa == null || taxa.equals(trees.getTaxa(), false)) && !trees.isDoomed()) { 
				if (count==i)
					return trees;
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public TreeVector getTreeBlockByID(long id){  //this uses the temporary run-time id of the tree vector
		if (treesVector==null)
			return null;
		for (int j = 0; j< treesVector.size(); j++) {
			TreeVector trees = (TreeVector)treesVector.elementAt(j);
			if (trees.getID() == id)
				return trees;
		}
		return null;
	}
	/*.................................................................................................................*/
	public TreeVector getTreeBlockByUniqueID(String uniqueID){  //this uses the temporary run-time id of the tree vector
		if (treesVector==null || uniqueID == null)
			return null;

		for (int j = 0; j< treesVector.size(); j++) {
			TreeVector trees = (TreeVector)treesVector.elementAt(j);
			if (uniqueID.equals(trees.getUniqueID()))
				return trees;
		}
		return null;
	}
	/*.................................................................................................................*/
	public TreeVector getTreeBlock(Taxa taxa, MesquiteFile file, int i){  //OK for doomed
		if (treesVector==null)
			return null;
		int count = 0;
		for (int j = 0; j< treesVector.size(); j++) {
			TreeVector trees = (TreeVector)treesVector.elementAt(j);
			if ((file==null || trees.getFile()==file) && !trees.isDoomed()  && (taxa == null || taxa.equals(trees.getTaxa(), false))){
				if (count==i)
					return trees;
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public int getTreeBlockNumber(Taxa taxa, MesquiteFile file, TreeVector trees){ //OK for doomed
		int count = 0;
		for (int j = 0; j< treesVector.size(); j++) {
			TreeVector t = (TreeVector)treesVector.elementAt(j);
			if ((file==null || t.getFile()==file) && (taxa == null || taxa.equals(t.getTaxa(), false)) && !t.isDoomed()) { 
				if (t == trees)
					return count;
				count++;
			}
		}
		return -1;
	}
	/*.................................................................................................................*/
	public int getTreeBlockNumber(Taxa taxa, TreeVector trees){ //OK for doomed
		int count = 0;
		for (int j = 0; j< treesVector.size(); j++) {
			TreeVector t = (TreeVector)treesVector.elementAt(j);
			if ((taxa == null || taxa.equals(t.getTaxa(), false)) && !t.isDoomed()) { 
				if (t == trees)
					return count;
				count++;
			}
		}
		return -1;
	}
	/*.................................................................................................................*/
	public int getTreeBlockNumber(TreeVector trees){//OK for doomed
		return getTreeBlockNumber(null, trees);
	}
	/*.................................................................................................................*/
	public int getNumberTreeBlocks(Taxa taxa){ //OK for doomed
		if (treesVector == null)
			return 0;
		int count = 0;
		for (int i = 0; i< treesVector.size(); i++) {
			TreeVector trees = (TreeVector)treesVector.elementAt(i);
			if (!trees.isDoomed() && (taxa == null || taxa.equals(trees.getTaxa(), false)))
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public int getNumberTreeBlocks(Taxa taxa, MesquiteFile file){ //OK for doomed
		if (treesVector == null)
			return 0;
		int count = 0;
		for (int i = 0; i< treesVector.size(); i++) {
			TreeVector trees = (TreeVector)treesVector.elementAt(i);
			if ((file==null || trees.getFile()==file) && !trees.isDoomed() && (taxa == null || taxa.equals(trees.getTaxa(), false)))
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public int getNumberTreeBlocks(){ //OK for doomed
		if (treesVector == null)
			return 0;
		int count = 0;
		for (int i = 0; i< treesVector.size(); i++) {
			TreeVector trees = (TreeVector)treesVector.elementAt(i);
			if (!trees.isDoomed())
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public TreeVector makeNewTreeBlock(Taxa taxa, String name, MesquiteFile f){
		return makeNewTreeBlock(taxa, name, false, f);
	}
	/*.................................................................................................................*/
	public TreeVector makeNewTreeBlock(Taxa taxa, String name, boolean writeWeights, MesquiteFile f){
		TreeVector trees = new TreeVector(taxa);
		trees.setName(name);
		trees.addToFile(f, getProject(), this); 
		trees.setWriteWeights(writeWeights);
		return trees;
	}
	/*.................................................................................................................*/
	public NexusBlock elementAdded(FileElement trees){
		if (trees ==null || !(trees instanceof TreeVector) || treesVector == null)
			return null;
		if (treesVector.indexOf(trees) <0) {
			treesVector.addElement(trees, true);
			for (int i=0; i<blockListeners.size(); i++){
				MesquiteListener ml = (MesquiteListener)blockListeners.elementAt(i);
				trees.addListener(ml);
				ml.changed(this, trees, new Notification(MesquiteListener.ELEMENT_CHANGED));
			}

			resetAllMenuBars();
		}
		NexusBlock nb = findNEXUSBlock(trees);
		if (nb==null) {
			TreeBlock t = new TreeBlock(trees.getFile(), this);
			t.setTreeBlock((TreeVector)trees);
			addNEXUSBlock(t);

			return t;
		}
		else return nb;
	}
	/*.................................................................................................................*/
	public void elementDisposed(FileElement e){
		if (e==null || !(e instanceof TreeVector))
			return;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object ma=getEmployeeVector().elementAt(i);
			if (ma instanceof ManagerAssistant)
				if (((ManagerAssistant)ma).showing(e)) {
					fireEmployee((ManagerAssistant)ma);
				}
		}
		NexusBlock nb = findNEXUSBlock(e);
		if (nb!=null) {
			removeNEXUSBlock(nb);
		}
		if (treesVector.indexOf(e)>=0){
			while(treesVector.indexOf(e)>=0) {
				treesVector.removeElement(e, true);
			}
		}
		else
			resetAllMenuBars();
	}
	/*.................................................................................................................*/
	public Taxa findTaxaMatchingTable(TreeVector trees, MesquiteProject proj, MesquiteFile file, Vector table) {
		ListableVector candidates = new ListableVector();
		for (int itx=0; itx< proj.getNumberTaxas(file); itx++) { //first check in this file
			Taxa tempTaxa = proj.getTaxa(file, itx);
			if (trees.tableMatchesTaxa(tempTaxa, table)) {
				candidates.addElement(tempTaxa, false);
			}
		}
		for (int itx=0; itx< proj.getNumberTaxas(); itx++) {//then in project as a whole
			Taxa tempTaxa = proj.getTaxa(itx);
			if (candidates.indexOf(tempTaxa)<0 && trees.tableMatchesTaxa(tempTaxa, table)) {
				candidates.addElement(tempTaxa, false);
			}
		}
		if (candidates.size() == 0)
			return null;
		if (candidates.size() == 1)
			return (Taxa)candidates.elementAt(0);
		Listable result = ListDialog.queryList(containerOfModule(), "Choose taxa block", "There is a tree block (" + trees.getName() + ") that does not specify the taxa block to which it pertains." + 
				" There is more than one taxa block with which it would be compatible.  Please choose its taxa block.", 
				"", candidates, 0);

		if (result == null)
			return (Taxa)candidates.elementAt(0);

		return (Taxa)result;
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new TreeBlockTest();}

	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
		boolean fuseTreeBlocks = false;
		int firstTree = 0;
		int lastTree = MesquiteInteger.infinite;
		int everyNth = 1;
		String fRA = parser.getFirstToken(fileReadingArguments);
		while (!StringUtil.blank(fRA)) {
			if (fRA.equalsIgnoreCase(StringUtil.argumentMarker + "fuseTreeBlocks"))
				fuseTreeBlocks = true;
			else if (fRA.equalsIgnoreCase(StringUtil.argumentMarker + "firstTree")) {
				fRA = parser.getNextToken();  // =
				fRA = parser.getNextToken();
				firstTree = MesquiteInteger.fromString(fRA);
			}
			else if (fRA.equalsIgnoreCase(StringUtil.argumentMarker + "lastTree")) {
				fRA = parser.getNextToken();  // =
				fRA = parser.getNextToken();
				lastTree = MesquiteInteger.fromString(fRA);
			}
			else if (fRA.equalsIgnoreCase(StringUtil.argumentMarker + "everyNth")) {
				fRA = parser.getNextToken();  // =
				fRA = parser.getNextToken();
				everyNth = MesquiteInteger.fromString(fRA);
			}
			fRA = parser.getNextToken();
		}
		Parser commandParser = new Parser();

		MesquiteInteger cPos = new MesquiteInteger(0);
		MesquiteString comment = new MesquiteString();
		String s;
		int treeNum=-1;
		boolean treeRead = false;
		Taxa taxa=null;
		if (file.getProject().getNumberTaxas()==1)
			taxa = file.getProject().getTaxa(0); //as default)

		TreeVector trees = new TreeVector( taxa);
		trees.setTaxa(taxa);
		if (getNumberTreeBlocks(taxa)>1)
			trees.setName("Tree block " + (getNumberTreeBlocks(taxa)+1) + " from \"" + file.getName() + "\"");
		else
			trees.setName("Trees from \"" + file.getName() + "\"");
		boolean nameSet = false;
		boolean translationTableRead = false;
		NexusBlock t =trees.addToFile(file, getProject(), this);
		while (!StringUtil.blank(s=block.getNextFileCommand(comment))) {
			String punc = ",";
			String commandName = parser.getFirstToken(s);
			if (commandName.equalsIgnoreCase("BEGIN") || commandName.equalsIgnoreCase("END")  || commandName.equalsIgnoreCase("ENDBLOCK")) {
			}
			//todo: here, allow figuring of taxa by first tree string and taxon names
			else if (commandName.equalsIgnoreCase("TRANSLATE")) {
				translationTableRead = true;
				Vector table = null;
				if (taxa == null)
					table = new Vector();
				String label =  parser.getNextToken();
				while (punc !=null && !punc.equalsIgnoreCase(";")) {
					String taxonName = parser.getNextToken();
					if (file.useStandardizedTaxonNames){
						if (taxa == null)
							taxa = file.getCurrentTaxa();
						if (taxa == null)
							taxa = getProject().getTaxa(0);
						String numS = taxonName.substring(1, taxonName.length());
						int it = MesquiteInteger.fromString(numS);
						if (MesquiteInteger.isCombinable(it))
							taxonName = taxa.getTaxonName(it);
					}
					if (taxa==null) 
						table.addElement(StringUtil.tokenize(taxonName) + " " + StringUtil.tokenize(label));
					else
						trees.setTranslationLabel(label, taxonName, false);
					punc =  parser.getNextToken(); 
					if (punc !=null && !punc.equals(";")) {
						label =  parser.getNextToken();
						if (";".equalsIgnoreCase(label))
							punc = label;  //to pop out of loop
					}
				}


				if (taxa==null) {
					int tI = parser.tokenIndexOfIgnoreCase(fileReadingArguments, "taxa");//DRM added this 8 April 2012 so that one can specify which taxa block it should belong to
					if (tI>=0){
						String ref = parser.getTokenNumber(fileReadingArguments, tI+2);
						taxa = getProject().getTaxa(ref);
					}

					if (taxa==null) 
						taxa = findTaxaMatchingTable(trees, getProject(), file, table);
					if (taxa!=null) {
						trees.setTaxa(taxa);
						trees.setTranslationTable(table);
					}
					else if (table.size()>0) {
						taxa = new Taxa(table.size());
						taxa.setName(getProject().getTaxas().getUniqueName("Untitled Block of Taxa"));
						taxa.addToFile(file, getProject(), findElementManager(Taxa.class));
						MesquiteInteger ppos = new MesquiteInteger();
						for (int it=0; it<taxa.getNumTaxa(); it++) {
							String taxonName = ParseUtil.getFirstToken((String)table.elementAt(it), ppos);
							taxa.setTaxonName(it,taxonName);
						}
						trees.setTaxa(taxa);
						trees.setTranslationTable(table);
						String st = "A block of trees has been read for which no corresponding block of taxa is available;  a new block of taxa has been created for it.";
						st += "  If you had expected that the trees would have applied to an existing block of taxa, it is possible that the taxa no longer correspond because of changes in names or in which taxa are included.";
						discreetAlert( st);
					}

				}
				else {
					if (table!=null)
						trees.setTranslationTable(table);
				}
				trees.checkTranslationTable();
			}
			else if (commandName.equalsIgnoreCase("TITLE")) {
				trees.setName(parser.getTokenNumber(2));
				nameSet = true;
			}
			else if (commandName.equalsIgnoreCase("ID")) {
				String id = parser.getTokenNumber(2);
				if (!StringUtil.blank(id))
					trees.setUniqueID(id);
			}
			else if (commandName.equalsIgnoreCase("LINK")) {
				if ("taxa".equalsIgnoreCase(parser.getTokenNumber(2))) {
					String taxaTitle = parser.getTokenNumber(4);
					taxa = getProject().getTaxa(file, taxaTitle);
					if (taxa == null)
						taxa = getProject().getTaxaLastFirst(taxaTitle);
					if (taxa == null) {
						if (getProject().getNumberTaxas(file)==1) //if translation table should search for match
							taxa = getProject().getTaxa(file, 0);
						else if (getProject().getNumberTaxas(file)==0 && getProject().getNumberTaxas()==1) //if translation table should search for match
							taxa = getProject().getTaxa(0);
						else
							discreetAlert( "LINK command in TREES block refers to taxa block, but taxa block not found");
					}
					trees.setTaxa(taxa);
					if (!nameSet)
						trees.setName("Trees block " + (getNumberTreeBlocks(taxa)+1) + " from " + file.getName());
				}
			}
			else { //if (taxa !=null || getProject().getNumberTaxas()>1) {
				int whichType = 0;
				if (commandName.equalsIgnoreCase("TREE")) 
					whichType = 1;
				else if (commandName.equalsIgnoreCase("UTREE")) 
					whichType =2;
				else if (commandName.equalsIgnoreCase("RTREE")) 
					whichType = 1;

				if (whichType > 0 && (!fuse || translationTableRead || (taxa!= null && taxa.getFile() == file))) {
					//if (fuseTaxaCharBlocks && !translationTableRead)
					treeNum++;
					if (treeNum >= firstTree && (!MesquiteInteger.isCombinable(lastTree) || treeNum<=lastTree) && (!MesquiteInteger.isCombinable(everyNth) || everyNth == 1 || (treeNum-firstTree) % everyNth == 0)) {
						boolean permitTaxaBlockEnlargement = false;
						if (taxa == null) { 
							TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
							taxa = taxaTask.makeNewTaxa("Taxa", 0, false);
							taxa.addToFile(file, getProject(), taxaTask);
							trees.setTaxa(taxa);
							permitTaxaBlockEnlargement = true;
							/*String st = "A block of trees has been read for which the corresponding block of taxa is not identified.  If you would like to attempt to read the block of trees as belonging to one of these taxa blocks, select the taxa.";
							st+= "  Command: " + s;
							taxa = getProject().chooseTaxa(containerOfModule(), st);
							if (taxa == null) {
								trees.dispose();
								return null;
							}
							else
								trees.setTaxa(taxa);
							 */
						}
						if (!translationTableRead && file.useStandardizedTaxonNames){
							for (int it = 0; it<taxa.getNumTaxa(); it++)
								trees.setTranslationLabel(Integer.toString(it+1), "t" + it, false);
							trees.checkTranslationTable();
						}
						String treeDescription;
						String treeName;
						treeName=parser.getNextToken();

						if (treeName.equals("*"))
							treeName=parser.getNextToken();
						parser.getNextToken(); //eat up "equals"
						treeDescription=s.substring(parser.getPosition(), s.length());

						MesquiteTree thisTree =new MesquiteTree(taxa);
						thisTree.setPermitTaxaBlockEnlargement(permitTaxaBlockEnlargement);
						String commentString = comment.getValue();

						if (commentString!=null && commentString.length()>1){
							if (commentString.charAt(0)=='!')
								thisTree.setAnnotation(commentString.substring(1, commentString.length()), false);
							else {
								int wpos = commentString.indexOf("&W");
								if (wpos <0)
									wpos = commentString.indexOf("&w");
								if (wpos>=0) {
									cPos.setValue(wpos+2);
									String num = ParseUtil.getToken(commentString, cPos);
									String slash = ParseUtil.getToken(commentString, cPos);
									String denom = ParseUtil.getToken(commentString, cPos);
									double w = 0;
									if (slash !=null && "/".equals(slash))
										w = 1.0*(MesquiteInteger.fromString(num))/(MesquiteInteger.fromString(denom));
									else
										w = MesquiteDouble.fromString(num);
									if (MesquiteDouble.isCombinable(w)) {
										MesquiteDouble d = new MesquiteDouble(w);
										d.setName(WEIGHT);
										thisTree.attachIfUniqueName(d);
									}
								}
							}
						}
						thisTree.setTreeVector(trees);
						trees.addElement(thisTree, false);
						treeRead = true;
						if (file.mrBayesReadingMode)
							thisTree.setReadingMrBayesConTree(true);
						thisTree.readTree(treeDescription);
						thisTree.setReadingMrBayesConTree(false);
						//thisTree.warnRetIfNeeded();
						thisTree.setName(treeName);
						if (whichType ==2) 
							thisTree.setRooted(false, false);
						if (treeNum>1 && treeNum % 100 == 0) 
							logln("   " + Integer.toString(treeNum) + " trees read ");

						if (treeNum>1 && treeNum % 1000 == 0) {
							Runtime rt = Runtime.getRuntime();
							rt.gc();
						}
					}	
				}
				else
					readUnrecognizedCommand(file, t, name, block, commandName, s, blockComments, comment);
			}
			/*else { 
				String st = "A block of trees has been read for which no corresponding block of taxa has been found, and no block of taxa could be created for it.";
				st += "  If you had expected that the trees would have applied to an existing block of taxa, it is possible that the taxa no longer correspond because of changes in names or in which taxa are included.";

				alert(st);
				trees.dispose();
				return null;
			}*/
		}
		if (treeRead){
			//assigning informative name if none or untitled
			if (trees!=null && (StringUtil.blank(trees.getName())|| "UNTITLED".equalsIgnoreCase(trees.getName()))) {
				trees.setName(treesVector.getUniqueName("Untitled Tree Block"));
			}
			if (trees != null && blockComments!=null && blockComments.length()>0)
				trees.setAnnotation(blockComments.toString(), false);
			if (getProject() != null)
				getProject().refreshProjectWindow();
			return t;
		}
		if (trees !=null)
			trees.dispose();
		discreetAlert( "No trees were read from the tree block.");
		getProject().refreshProjectWindow();
		return null;
	}


	public String getTreeBlock(TreeVector trees, NexusBlock tB){
		if (trees == null || trees.size()==0)
			return null;
		String endLine = ";" + StringUtil.lineEnding();
		StringBuffer block = new StringBuffer(5000);
		Taxa taxa = trees.getTaxa();
		block.append("BEGIN TREES");
		if (trees.getAnnotation()!=null) 
			block.append("[!" + StringUtil.tokenize(trees.getAnnotation()) + "]");
		block.append(endLine);
		if (!NexusBlock.suppressNEXUSTITLESANDLINKS){
			block.append("\tTitle " + StringUtil.tokenize(trees.getName()));
			block.append(endLine);
		}
		if (!NexusBlock.suppressNEXUSIDS){
			block.append("\tID " + StringUtil.tokenize(trees.getUniqueID()));
			block.append(endLine);
		}
		if (taxa!=null && (getProject().getNumberTaxas()>1 || !NexusBlock.suppressNEXUSTITLESANDLINKS)) {
			block.append("\tLINK Taxa = " + StringUtil.tokenize(taxa.getName()));
			block.append(endLine);
		}
		block.append("\tTRANSLATE" + StringUtil.lineEnding());
		String tt =trees.getTranslationTable();
		int writeMode = Tree.BY_TABLE;
		if (tt==null) {
			tt = "";
			if (taxa!=null)
				for(int i=0; i<taxa.getNumTaxa(); i++) {
					if (i>0)
						tt += ","+ StringUtil.lineEnding();
					tt += "\t\t" + Taxon.toExternal(i) + "\t" + StringUtil.tokenize(taxa.getTaxonName(i)) ;
				}
			writeMode = Tree.BY_NUMBERS;
		}
		block.append( tt);
		block.append(endLine);

		Enumeration e = trees.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			Tree t = (Tree)obj;

			block.append("\tTREE ");
			if (t instanceof MesquiteTree && !StringUtil.blank(((MesquiteTree)t).getAnnotation())) {
				String s = ((MesquiteTree)t).getAnnotation();
				s= StringUtil.replace(s, '\n', ' ');
				s=StringUtil.replace(s, '\r', ' ');
				block.append(" [!" + s + "] ");
			}

			Object weightObject = ((Attachable)t).getAttachment(WEIGHT);
			if(trees.getWriteWeights()&& weightObject!=null && weightObject instanceof MesquiteString){
				block.append(StringUtil.tokenize(t.getName()) + " = [&W " + ((MesquiteString)weightObject).getValue() + "] " + t.writeTree(writeMode) + StringUtil.lineEnding());
			}
			else {
				String ttt = t.writeTree(Tree.BY_TABLE);
				block.append(StringUtil.tokenize(t.getName() )+ " = " +  t.writeTree(writeMode) + StringUtil.lineEnding());
			}

		}
		if (tB != null) block.append(tB.getUnrecognizedCommands() + StringUtil.lineEnding());
		block.append("END");

		block.append(";" + StringUtil.lineEnding()+ StringUtil.lineEnding());
		return block.toString();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Manage TREES blocks"; //Name must be updated in Basic File Coord
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Tree Manager";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages tree blocks (including read/write TREES block in NEXUS file)." ;
	}

	public boolean isPrerelease(){
		return false;
	}
}
/* ======================================================================== */
abstract class FillerThread extends MesquiteThread {
	ManageTrees ownerModule;
	public FillerThread (ManageTrees ownerModule) {
		super();
		resetUIOnMe = false;
		this.ownerModule = ownerModule;
	}
	public void threadGoodbye(){
		ownerModule.fillerThreads.removeElement(this);
		super.threadGoodbye();
	}
	public abstract void stopFilling();
}

/* ======================================================================== */
class DirectTreeFileThread extends FillerThread {
	TreeSource treeSourceTask;
	Taxa taxa;
	MesquiteFile file;
	int howManyTrees;
	boolean suppressAsk = false;

	public DirectTreeFileThread (ManageTrees ownerModule, TreeSource treeSourceTask, Taxa taxa, int howManyTrees, MesquiteFile file) {
		super(ownerModule);
		this.treeSourceTask = treeSourceTask;
		this.howManyTrees = howManyTrees;
		this.taxa = taxa;
		this.file = file;
		setCurrent(1);
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		boolean sc;
		if (cr == null)
			sc = false;
		else
			sc = cr.recordIsScripting();
		setCommandRecord(new CommandRecord(sc));
	}

	public String getCurrentCommandName(){
		return "Making trees";
	}
	public String getCurrentCommandExplanation(){
		return null;
	}
	/*.............................................*/
	public void run() {
		long s = System.currentTimeMillis();
		try {
			ownerModule.saveDirectTreeFile(treeSourceTask, taxa, howManyTrees, file);
		}
		catch (Exception e){
			MesquiteFile.throwableToLog(this, e);
			ownerModule.alert("Sorry, there was a problem in making the tree block.  An Exception was thrown (class " + e.getClass() +"). For more details see Mesquite log file.");
		}
		catch (Error e){
			MesquiteFile.throwableToLog(this, e);
			ownerModule.alert("Sorry, there was a problem in making the tree block.  An Error was thrown (class " + e.getClass() +"). For more details see Mesquite log file.");
			throw e;
		}
		threadGoodbye();
	}
	public void stopFilling(){

	}
	/*.............................................*/
	public void dispose(){
		ownerModule = null;
		treeSourceTask=null;
		taxa = null;
		file = null;
	}

}
/* ======================================================================== */
class TreeBlockThread extends FillerThread {
	TreeBlockFiller fillTask;
	TreeVector trees;
	MesquiteFile file;
	int howManyTrees;
	boolean suppressAsk = false;
	CommandRecord comRec = null;
	MesquiteBoolean autoSave = null;
	boolean aborted = false;
	public TreeBlockThread (ManageTrees ownerModule, TreeBlockFiller fillTask, TreeVector trees, int howManyTrees, MesquiteBoolean autoSave, MesquiteFile file) {
		super(ownerModule);
		this.fillTask = fillTask;
		this.trees = trees;
		this.howManyTrees = howManyTrees;
		this.file = file;
		this.autoSave = autoSave;
		setCurrent(1);
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		boolean sc;
		if (cr == null)
			sc = false;
		else
			sc = cr.recordIsScripting();
		comRec = new CommandRecord(sc);
		setCommandRecord(comRec);

	}

	public String getCurrentCommandName(){
		return "Making trees";
	}
	public String getCurrentCommandExplanation(){
		return null;
	}
	/*.............................................*/
	public void run() {
		long s = System.currentTimeMillis();
		int before = trees.size();
		try {
			fillTask.fillTreeBlock(trees, howManyTrees);

			boolean okToSave = false;
			if (!ownerModule.isDoomed()){
				if (!aborted){
					if (trees.size()==before) {
						ownerModule.alert("Sorry, no trees were returned by " + fillTask.getName());
						ownerModule.fireTreeFiller();

					}
					else {
						trees.addToFile(file, ownerModule.getProject(), ownerModule);
						okToSave = true;
					}
					if (trees.size()!=before)
						ownerModule.doneQuery(fillTask, trees.getTaxa(), trees, suppressAsk);
				}
				ownerModule.fireTreeFiller();
				if (okToSave && autoSave != null && autoSave.getValue()){
					FileCoordinator fCoord = ownerModule.getFileCoordinator();
					fCoord.writeFile(file);
				}
			}
			ownerModule.resetAllMenuBars();
		}
		catch (Exception e){
			MesquiteFile.throwableToLog(this, e);
			ownerModule.alert("Sorry, there was a problem in making the tree block.  An Exception was thrown (class " + e.getClass() +"). For more details see Mesquite log file.");
		}
		catch (Error e){
			MesquiteFile.throwableToLog(this, e);
			ownerModule.alert("Sorry, there was a problem in making the tree block.  An Error was thrown (class " + e.getClass() +"). For more details see Mesquite log file.");
			throw e;
		}
		threadGoodbye();
	}
	public void stopFilling(){
		if (fillTask != null)
			fillTask.abortFilling();
		aborted = true;
	}
	/*.............................................*/
	public void dispose(){
		ownerModule = null;
		fillTask = null;
		trees = null;
		file = null;
	}

}
/* ======================================================================== */
class TreeBlockMonitorThread extends FillerThread {
	TreeBlockFiller fillTask;
	CommandRecord comRec = null;
	boolean aborted = true;
	String taxaIDString = null;

	public TreeBlockMonitorThread (ManageTrees ownerModule, String taxaID, TreeBlockFiller fillTask) {
		super(ownerModule);
		this.fillTask = fillTask;
		taxaIDString = taxaID;
		setCurrent(1);
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		boolean sc;
		if (cr == null)
			sc = false;
		else
			sc = cr.recordIsScripting();
		comRec = new CommandRecord(sc);
		setCommandRecord(comRec);

	}

	public String getCurrentCommandName(){
		return "Making trees";
	}
	public String getCurrentCommandExplanation(){
		return null;
	}
	/*.............................................*/
	public void run() {
		Reconnectable reconnectable = fillTask.getReconnectable();
		if (reconnectable != null){
			reconnectable.reconnectToRequester(new MesquiteCommand("treesReady", taxaIDString, ownerModule));
		}
		threadGoodbye();

	}
	public void stopFilling(){
		if (fillTask != null)
			fillTask.abortFilling();
		aborted = true;
	}
	/*.............................................*/
	public void dispose(){
		ownerModule = null;
		fillTask = null;
	}

}/*===============================================*/
class TreeBlock extends NexusBlock {
	TreeVector trees = null;
	public TreeBlock(MesquiteFile f, MesquiteModule mb){
		super(f, mb);
	}
	public void written() {
		trees.setDirty(false);
	}
	public boolean mustBeAfter(NexusBlock block){
		if (block==null)
			return false;
		if (trees!=null && block instanceof TaxaBlock) {
			return trees.getTaxa() == ((TaxaBlock)block).getTaxa();
		}
		return (block.getBlockName().equalsIgnoreCase("TAXA") || block.getBlockName().equalsIgnoreCase("CHARACTERS"));

	}
	public void dispose(){
		trees = null;
		super.dispose();
	}
	public String getBlockName(){
		return "TREES";
	}
	public boolean contains(FileElement e) {
		return e!=null && trees == e;
	}
	public void setTreeBlock(TreeVector trees) {
		this.trees = trees;
	}
	public TreeVector getTreeBlock() {
		return trees;
	}
	public String getName(){
		if (trees==null)
			return "empty tree block";
		else
			return "Tree block: " + trees.getName();
	}
	public String getNEXUSBlock(){
		if (trees==null)
			return null;
		else
			return ((ManageTrees)getManager()).getTreeBlock(trees, this);
	}
}

/* ======================================================================== */
class TreeBlockTest extends NexusBlockTest  {
	public TreeBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("TREES");
	}
}


