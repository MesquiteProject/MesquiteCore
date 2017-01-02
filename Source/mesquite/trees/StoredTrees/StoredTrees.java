/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.StoredTrees;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Supplies trees from tree blocks stored in the project.*/
public class StoredTrees extends TreeSource implements MesquiteListener {
	int currentTree=0;
	TreeVector currentTreeBlock = null;
	TreeVector lastUsedTreeBlock = null;
	TreesManager manager;
	Taxa preferredTaxa =null;

	int currentListNumber = MesquiteInteger.unassigned;
	MesquiteSubmenuSpec listSubmenu;
	MesquiteBoolean weightsEnabled, useWeights;
	MesquiteMenuItemSpec weightsItem;
	MesquiteFile currentSourceFile = null;
	MesquiteString blockName;
	ListableVector managerVectorOfTreeBlocks = null;
	long currentTreeBlockID = MesquiteLong.unassigned;
	boolean laxMode = false; //mode where default tree given without complaint, empty trees block prepared automatically on store
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition !=null && condition instanceof Taxa){
			preferredTaxa = (Taxa)condition;
		}
		weightsEnabled = new MesquiteBoolean(false);
		useWeights = new MesquiteBoolean(false);
		manager = (TreesManager)findElementManager(TreeVector.class);
		if (arguments != null && arguments.equalsIgnoreCase("laxMode"))
			laxMode = true;
		if (manager==null)
			return sorry(getName() + " couldn't start because no tree manager module was found.");
		if (!laxMode && manager.getNumberTreeBlocks(preferredTaxa)==0 && !MesquiteThread.isScripting()) {
			return sorry("No stored trees are available.");
		}
		managerVectorOfTreeBlocks = manager.getTreeBlockVector();
		managerVectorOfTreeBlocks.addListener(this);

		//can leave a hint in terms of an id of a treeblock to use
		String whichBlock = MesquiteThread.retrieveAndDeleteHint(this);
		long wB = MesquiteLong.fromString(whichBlock);
		if (MesquiteLong.isCombinable(wB)){
			currentTreeBlockID = wB;
		}
		listSubmenu = addSubmenu(null, "Tree Block (" + whatIsMyPurpose() + ")", makeCommand("setTreeBlockInt",  this), manager.getTreeBlockVector());
		blockName = new MesquiteString();
		listSubmenu.setSelected(blockName);
		return true;
	}
	public boolean permitSeparateThreadWhenFilling(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	 /**Returns info to show in info panel etc. for tree block or source of trees.*/
	public String getTreeSourceInfo(Taxa taxa){
		if (currentTreeBlock == null)
			return null;
		String s = getName();
		s+="\n" + currentTreeBlock.getAnnotation();
		return s;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (laxMode)
			temp.addLine("laxMode");
		if (preferredTaxa!=null && getProject() != null && getProject().getNumberTaxas()>1)
			temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(preferredTaxa));
		if (currentSourceFile!=null && currentSourceFile != file)
			temp.addLine("setTreeBlockInt " + currentListNumber); 
		else
			temp.addLine("setTreeBlock " + TreeVector.toExternal(currentListNumber)); 
		if (currentTreeBlock != null){  // this is done as a second command, so that files are written to be readable by old Mesquite, by having old numbering as back-up
			temp.addLine("setTreeBlockID " + StringUtil.tokenize(currentTreeBlock.getUniqueID())); 
		}
		temp.addLine("toggleUseWeights " + useWeights.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets which block of trees to use (for internal use; 0 based)", "[block number]", commandName, "setTreeBlockInt")) { //need separate from setTreeBlock since this is used internally with 0-based menu response
			int whichList = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (MesquiteInteger.isCombinable(whichList)) {
				currentTreeBlock = manager.getTreeBlock(preferredTaxa, whichList); //checker.getFile(), 
				if (currentTreeBlock ==null)
					return null;
				if (lastUsedTreeBlock !=null) 
					lastUsedTreeBlock.removeListener(this);
				blockName.setReferentID(Long.toString(currentTreeBlock.getID()));
				blockName.setValue(currentTreeBlock.getName());
				currentTreeBlock.addListener(this);
				lastUsedTreeBlock = currentTreeBlock;
				currentListNumber = whichList;
				currentTreeBlockID = currentTreeBlock.getID();
				currentSourceFile = currentTreeBlock.getFile();
				MesquiteTrunk.resetChecks(listSubmenu);
				parametersChanged();
				MesquiteTrunk.checkForResetCheckMenuItems();
				return currentTreeBlock;
			}
		}
		else if (checker.compare(this.getClass(),  "Sets which block of trees to use", "[block unique ID]", commandName, "setTreeBlockID")) {
			String uniqueID = parser.getFirstToken(arguments);
			if (!StringUtil.blank(uniqueID)) {
				currentTreeBlock = manager.getTreeBlockByUniqueID(uniqueID);
				if (currentTreeBlock ==null)
					return null;
				if (lastUsedTreeBlock !=null) 
					lastUsedTreeBlock.removeListener(this);
				blockName.setReferentID(Long.toString(currentTreeBlock.getID()));
				blockName.setValue(currentTreeBlock.getName());
				currentTreeBlock.addListener(this);
				currentTreeBlockID = currentTreeBlock.getID();
				currentSourceFile = currentTreeBlock.getFile();
				lastUsedTreeBlock = currentTreeBlock;
				currentListNumber = manager.getTreeBlockNumber(preferredTaxa, checker.getFile(), currentTreeBlock);
				MesquiteTrunk.resetChecks(listSubmenu);
				parametersChanged();
				return currentTreeBlock;
			}
		}
		else if (checker.compare(this.getClass(),  "Sets which block of trees to use", "[block number]", commandName, "setTreeBlock")) {
			int whichList = TreeVector.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
			if (MesquiteInteger.isCombinable(whichList)) {
				currentTreeBlock = manager.getTreeBlock(preferredTaxa, checker.getFile(), whichList);
				if (currentTreeBlock ==null)
					return null;
				if (lastUsedTreeBlock !=null) 
					lastUsedTreeBlock.removeListener(this);
				blockName.setReferentID(Long.toString(currentTreeBlock.getID()));
				blockName.setValue(currentTreeBlock.getName());
				currentTreeBlock.addListener(this);
				currentTreeBlockID = currentTreeBlock.getID();
				currentSourceFile = currentTreeBlock.getFile();
				lastUsedTreeBlock = currentTreeBlock;
				currentListNumber = whichList;
				MesquiteTrunk.resetChecks(listSubmenu);
				parametersChanged();
				return currentTreeBlock;
			}
		}
		else if (checker.compare(this.getClass(),  "Sets which block of trees to use", "[runtime ID]", commandName, "setTreeBlockByID")) {
			int whichList = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (MesquiteInteger.isCombinable(whichList)) {
				TreeVector tr = manager.getTreeBlockByID(whichList);
				if (tr == null || (preferredTaxa != null && tr.getTaxa() != preferredTaxa))
					return null;
				currentTreeBlock = tr;
				if (lastUsedTreeBlock !=null) 
					lastUsedTreeBlock.removeListener(this);
				blockName.setReferentID(Long.toString(currentTreeBlock.getID()));
				blockName.setValue(currentTreeBlock.getName());
				currentTreeBlock.addListener(this);
				currentTreeBlockID = currentTreeBlock.getID();
				currentSourceFile = currentTreeBlock.getFile();
				lastUsedTreeBlock = currentTreeBlock;
				currentListNumber = whichList;
				MesquiteTrunk.resetChecks(listSubmenu);
				parametersChanged();
				return currentTreeBlock;
			}
		}
		else if (checker.compare(this.getClass(),  "Returns current tree block", null, commandName, "getTreeBlock")) {
			if (currentTreeBlock == null)
				checkTreeBlock(preferredTaxa, false);
			return currentTreeBlock;
		}
		else if (checker.compare(this.getClass(),  "Sets to lax mode", null, commandName, "laxMode")) {
			laxMode = true;
		}
		else if (checker.compare(this.getClass(),  "Turns off lax mode", null, commandName, "laxOff")) {
			laxMode = false;
		}
		else if (checker.compare(this.getClass(), "Sets which block of taxa to use", "[block reference, number, or name]", commandName, "setTaxa")) { 
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				setPreferredTaxa(t);
				parametersChanged();
				return t;
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to use any available weights for the trees", "[on or off]", commandName, "toggleUseWeights")) {
			boolean current = useWeights.getValue();
			useWeights.toggleValue(parser.getFirstToken(arguments));
			if (current!=useWeights.getValue() && !MesquiteThread.isScripting())
				parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void classFieldChanged (Class c, String fieldName) {
		super.classFieldChanged(c, fieldName);
		if (c== Tree.class)
			parametersChanged();
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (currentTreeBlock !=null) {
			currentTreeBlock.removeListener(this);
		}
		if (preferredTaxa != null){
			preferredTaxa.removeListener(this);
		}
		if (managerVectorOfTreeBlocks!= null)
			managerVectorOfTreeBlocks.removeListener(this);

		super.endJob();
	}
	/*.................................................................................................................*/
	long previous = -1;
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (notification != null && notification.getNotificationNumber() == previous)
			return;
		if (notification != null)
			previous = notification.getNotificationNumber();
		if (doomed)
			return;

		if (code != MesquiteListener.ANNOTATION_CHANGED && code != MesquiteListener.ANNOTATION_DELETED && code != MesquiteListener.ANNOTATION_ADDED && !(obj instanceof TreeVector && code == MesquiteListener.SELECTION_CHANGED)) {
			if (obj == managerVectorOfTreeBlocks){
				if (code == MesquiteListener.PARTS_MOVED){
					if (MesquiteLong.isCombinable(currentTreeBlockID))
						currentListNumber = indexOfTreeBlockWithID(preferredTaxa, currentTreeBlockID);
				}
				else if (code == MesquiteListener.PARTS_DELETED){
					if (currentTreeBlock != null && managerVectorOfTreeBlocks.indexOf(currentTreeBlock)<0){
						int index = indexOfTreeBlockWithID(preferredTaxa, currentTreeBlockID);
						if (index>0)
							currentListNumber = index;
						else if (MesquiteLong.isCombinable(currentTreeBlockID)){
							
						//	discreetAlert( "The current tree block used by Stored Trees (for " + getEmployer().getName() + ") has apparently been deleted.  You might be asked to select another tree block, or this might force use of default trees, and also may yield error messages when rereading the file.");
							if (currentTreeBlock != null)
								currentTreeBlock.removeListener(this);
							currentTreeBlock = null;
							lastUsedTreeBlock = null;
							currentListNumber = MesquiteInteger.unassigned;
							currentTreeBlockID = MesquiteLong.unassigned;
							MesquiteTrunk.resetChecks(listSubmenu);
							checkTreeBlock(preferredTaxa, true);
							parametersChanged(new Notification(MesquiteListener.BLOCK_DELETED));
						}
					}
				}


			}
			else if (obj instanceof Taxa){
				boolean respond = (code==MesquiteListener.ITEMS_ADDED || code==MesquiteListener.PARTS_CHANGED || code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_DELETED || code==MesquiteListener.PARTS_MOVED);
				if (respond){
					if (notification != null)
						notification.setObjectClass(Taxa.class);
					parametersChanged(notification);
				}
			}
			else {
				if (obj instanceof TreeVector && ((TreeVector)obj).size()==0 && obj == currentTreeBlock) {
					discreetAlert( "The current tree block used by Stored Trees (for " + getEmployer().getName() + ") has no trees in it.  This might force use of default trees, and also may yield error messages when rereading the file.");
					currentTreeBlock.removeListener(this);
					currentTreeBlock = null;
					lastUsedTreeBlock = null;
					currentListNumber = MesquiteInteger.unassigned;
					currentTreeBlockID = MesquiteLong.unassigned;
					MesquiteTrunk.resetChecks(listSubmenu);
				}
				parametersChanged(notification);
			}
		}
		else if ((obj instanceof TreeVector && code == MesquiteListener.SELECTION_CHANGED)) {
			parametersChanged(notification);
		}

	}
	/*.................................................................................................................*/
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (preferredTaxa == obj) {
			preferredTaxa = null;
			if (currentTreeBlock!= null)
				currentTreeBlock.removeListener(this);
			currentTreeBlock = null;
			lastUsedTreeBlock = null;
			currentListNumber = MesquiteInteger.unassigned;
			setHiringCommand(null); //since there is no rehiring
			iQuit();
			//don't say parameters changed since employer asked for those taxa
		}
		else if (currentTreeBlock == obj) {
			currentTreeBlock.removeListener(this);
			currentTreeBlock = null;
			lastUsedTreeBlock = null;
			currentListNumber = MesquiteInteger.unassigned;
			MesquiteTrunk.resetChecks(listSubmenu);
			if (preferredTaxa != null && preferredTaxa.isDoomed()){
				preferredTaxa = null; //don't say parameters changed since employer asked for those taxa
			}
			else{
				checkTreeBlock(preferredTaxa, true);
				parametersChanged(); 
			}
		}
	}
	/*.................................................................................................................*/
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	/*.................................................................................................................*/
	/** queryies the user to choose a tree and returns an integer of the tree chosen*/
	public int queryUserChoose(Taxa taxa, String forMessage){
		int ic=MesquiteInteger.unassigned;
		int numTrees = getNumberOfTrees(taxa);
		if (currentTreeBlock == null)
			return ic;
		if (MesquiteInteger.isCombinable(numTrees)){
			String[] s = new String[numTrees];
			for (int i=0; i<numTrees; i++){
				Tree tree= (Tree)currentTreeBlock.elementAt(i);
				if (tree.getName()!=null && !tree.getName().equals(""))
					s[i] = tree.getName();
				else
					s[i]= "Tree # " + Integer.toString(MesquiteTree.toExternal(i));
			}
			return ListDialog.queryList(containerOfModule(), "Choose tree", "Choose tree " + forMessage, MesquiteString.helpString,s, 0);
		}
		else 
			return  MesquiteInteger.queryInteger(containerOfModule(), "Choose tree", "Number of tree " + forMessage, 1);

	}

	boolean firstInit = true;
	/*.................................................................................................................*/
	public void initialize(Taxa taxa) {
		checkTreeBlock(taxa, false);
	}
	/*.................................................................................................................*/
	public void resetMenu(Taxa taxa){
		boolean reset = false;
		if (listSubmenu!=null){
			if (!isInStartup()){
				if (firstInit){
					listSubmenu.setName("Tree Block (" + whatIsMyPurpose() + ")");
					reset = true;
				}
				firstInit = false;
			}
			Object obj = listSubmenu.getCompatibilityCheck();
			if (obj == null || obj!=taxa) {
				listSubmenu.setCompatibilityCheck(taxa);
				reset = true;
			}
		}
		if (reset)
			resetContainingMenuBar();
	}
	/*.................................................................................................................*/
	public void setPreferredTaxa(Taxa taxa) {
		if (taxa !=null && taxa.isDoomed())
			return;
		if (preferredTaxa!=taxa) {
			if (preferredTaxa !=null)
				preferredTaxa.removeListener(this);
			preferredTaxa = taxa;
			if (taxa == null)
				return;
			preferredTaxa.addListener(this);
			resetMenu(taxa);
		}
	}
	boolean first = true;

	int indexOfTreeBlockWithID(Taxa taxa, long treeBlockID){
		int nt = manager.getNumberTreeBlocks(taxa);
		for (int i=0; i< nt; i++){
			TreeVector tv =manager.getTreeBlock(taxa, i);
			if (tv.getID() == treeBlockID)
				return i;
		}
		return -1;
	}
	/*.................................................................................................................*/
	private int checkTreeBlock(Taxa taxa, boolean becauseOfDeletion){
		if (taxa == null) {
			if (!MesquiteThread.isScripting())
				logln("Taxa null in checkTreeBlock in Stored Trees");
			return -1;
		}
		if (manager == null)
			return -1;
		resetMenu(taxa);
		int nt = manager.getNumberTreeBlocks(taxa);
		if (MesquiteLong.isCombinable(currentTreeBlockID))
			currentListNumber = indexOfTreeBlockWithID(taxa, currentTreeBlockID);
		if ((!MesquiteInteger.isCombinable(currentListNumber) || currentListNumber>=nt || currentListNumber<0)) {
			if (MesquiteThread.isScripting())
				currentListNumber = 0;
			else if (nt<=1)
				currentListNumber = 0;
			else if (MesquiteLong.isCombinable(currentTreeBlockID)){
				for (int i=0; i< nt; i++){
					TreeVector tv =manager.getTreeBlock(taxa, i);
					if (tv.getID() == currentTreeBlockID)
						currentListNumber = i;
				}
			}
			else {
				if (becauseOfDeletion){
					if (!dearEmployerShouldIHandleThis(new Notification(MesquiteListener.BLOCK_DELETED))){
						currentListNumber = 0;
						MesquiteTrunk.resetChecks(listSubmenu);
						return -1;
				}
							
				}
				String[] list = new String[nt];
				for (int i=0; i< nt; i++){
					TreeVector tv =manager.getTreeBlock(taxa, i);
					list[i]=tv.getName();
				}
				currentListNumber = ListDialog.queryList(containerOfModule(), "Use which tree block?", "Use which tree block? \n(" + whatIsMyPurpose() + ")",MesquiteString.helpString, list, 0);
				if (!MesquiteInteger.isCombinable(currentListNumber))
					currentListNumber = 0;
			}
			MesquiteTrunk.resetChecks(listSubmenu);
		}
		int code = 0;
		if (lastUsedTreeBlock == null || lastUsedTreeBlock.getTaxa() == null || !lastUsedTreeBlock.getTaxa().equals(taxa)) {
			currentTreeBlock =  manager.getTreeBlock(taxa, currentListNumber);
			code = 1;
		}
		else
			currentTreeBlock = lastUsedTreeBlock;

		if (currentTreeBlock != null)
			currentTreeBlockID = currentTreeBlock.getID();

		if (blockName != null && currentTreeBlock != null){
		blockName.setReferentID(Long.toString(currentTreeBlock.getID()));
			blockName.setValue(currentTreeBlock.getName());
		}
		
		if (lastUsedTreeBlock!=currentTreeBlock) {
			if (lastUsedTreeBlock !=null) 
				lastUsedTreeBlock.removeListener(this);
			if (currentTreeBlock!=null)
				currentTreeBlock.addListener(this);
		}
		lastUsedTreeBlock = currentTreeBlock;
		if (currentTreeBlock == null) {
			currentTreeBlock = manager.getTreeBlock(taxa, 0);
			if (currentTreeBlock!=null)
				currentTreeBlock.addListener(this);
			lastUsedTreeBlock = currentTreeBlock;
		}
		if (currentTreeBlock == null) {
			
			
			if (getProject() != null && getProject().getNumberTaxas()==1 && !MesquiteThread.isScripting() && !laxMode)
				logln("No current tree block for taxa " + taxa.getName() + "(Module: Stored Trees)");
			if (!MesquiteThread.isScripting())
				iQuit();
			return -1;
		}
		currentSourceFile = currentTreeBlock.getFile();
		laxMode = false;
		return code;
	}
	public boolean showing(TreeVector v){
		return v == currentTreeBlock;
	}
	boolean warned = false;
 	public String getNotesAboutTrees(Taxa taxa){
		int code = checkTreeBlock(taxa, false);
		if (code <0 || currentTreeBlock == null) {
			if (laxMode)
				return "Tree being edited by hand.";
			else
				return null;
		}
		String s = "Tree block: " + currentTreeBlock.getName();
		String an = currentTreeBlock.getAnnotation();
		if (!StringUtil.blank(an))
			s += "\n\nNotes:\n" + an;
		return s;
	}
	/*.................................................................................................................*/
	public Tree getCurrentTree(Taxa taxa) {
		try {
			int code = checkTreeBlock(taxa, false);
			if (code <0) {
				if (laxMode)
					return getDefaultTree(taxa);
				else
					return null;
			}
			if (currentTreeBlock != null && currentTreeBlock.size()>0) {
				if (currentTree<currentTreeBlock.size()) {
					Tree t = (Tree)currentTreeBlock.elementAt(currentTree);
					if (t == null)
						return null;
					t.setFileIndex(currentTree);

					if (t instanceof MesquiteTree)
						((MesquiteTree)t).setAssignedNumber(currentTree);
					return t;
				}
				else {
					MesquiteMessage.warnUser("Tree #" + (currentTree+1) + " requested beyond number available (" + currentTreeBlock.size() + ") in tree block \"" + currentTreeBlock.getName() + "\"."); //in 1.0 returned first tree in block
					/*
					currentTree = 0;
	   				Tree t = (Tree)currentTreeBlock.elementAt(currentTree);
	   				if (t instanceof MesquiteTree)
	   					((MesquiteTree)t).setAssignedNumber(currentTree);
					 */
					return null;
				}
			}
			else {
				if (first) {
					if (taxa != null) {
						if (currentTreeBlock==null) {
							if (!MesquiteThread.isScripting()) {
								discreetAlert(warned, "No tree block available for taxa \"" + taxa.getName() + "\"; will use default tree");
								warned = true;
							}
						}
						else {
							MesquiteMessage.warnUser("Tree #" + (currentTree+1) + " requested beyond number (" + currentTreeBlock.size() + ") in tree block \"" + currentTreeBlock.getName() + "\"."); //in 1.0 returned default tree
						}
					}
					else
						MesquiteMessage.warnUser("No tree block available! [code " + code + "]");
					first = false;
				}
				//return taxa.getDefaultTree();
				return null;
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	/*.................................................................................................................*/
	public Tree getDefaultTree(Taxa taxa) {
		int numTaxa = taxa.getNumTaxa();
		MesquiteTree tree = new MesquiteTree(taxa);
		tree.setToDefaultSymmetricalTree(numTaxa, false);
		tree.setName("DEFAULT ARBITRARY TREE");
		return tree;
	}
	/*.................................................................................................................*/
	public Selectionable getSelectionable(){
		return currentTreeBlock;
	}
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int itree) {
		setPreferredTaxa(taxa);
		currentTree=itree;
		return getCurrentTree(taxa);
	}
	/*.................................................................................................................*/
	public void setEnableWeights(boolean enable){
		if (enable == weightsEnabled.getValue())
			return;
		weightsEnabled.setValue(enable);
		if (enable && weightsItem == null) {
			weightsItem = addCheckMenuItem(null, "Use Tree Weights", makeCommand("toggleUseWeights", this), useWeights);
			resetContainingMenuBar();
		}
		else {
			weightsItem.setEnabled(enable);
			MesquiteTrunk.resetMenuItemEnabling();
		}
	}
	/*.................................................................................................................*/
	public boolean itemsHaveWeights(Taxa taxa){
		if (!useWeights.getValue())
			return false;
		int code = checkTreeBlock(taxa, false);
		if (currentTreeBlock != null) {
			for (int itree =0; itree < currentTreeBlock.size(); itree++){
				Tree tree = currentTreeBlock.getTree(itree);
				if (tree !=null && tree instanceof Attachable){
					Vector at = ((Attachable)tree).getAttachments();
					if (at !=null) {
						for (int i =0; i < at.size(); i++){
							Object obj = at.elementAt(i);
							if (obj instanceof MesquiteDouble) {
								String name = ((Listable)obj).getName();
								if ("weight".equalsIgnoreCase(name)) 
									return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	public double getItemWeight(Taxa taxa, int ic){
		Tree tree = getTree(taxa, ic);
		if (tree != null) {
			if (tree instanceof Attachable){
				Vector at = ((Attachable)tree).getAttachments();
				if (at !=null) {
					for (int i =0; i < at.size(); i++){
						Object obj = at.elementAt(i);
						if (obj instanceof MesquiteDouble) {
							String name = ((Listable)obj).getName();
							if ("weight".equalsIgnoreCase(name)) 
								return ((MesquiteDouble)obj).getValue();
						}
					}
				}
			}
		}
		return MesquiteDouble.unassigned;
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		setPreferredTaxa(taxa);
		if (currentTreeBlock == null && laxMode)
			return 0;
		int code = checkTreeBlock(taxa, false);
		if (currentTreeBlock != null)
			return currentTreeBlock.size();
		else
			return 0; //just default tree
	}

	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		setPreferredTaxa(taxa);
		try {
			Tree tree;
			int code = checkTreeBlock(taxa, false);
			if (currentTreeBlock == null || itree>=currentTreeBlock.size())
				return "";
			if (currentTree<currentTreeBlock.size())
				tree= (Tree)currentTreeBlock.elementAt(itree);
			else
				tree = taxa.getDefaultTree();
			if (tree.getName()!=null && !tree.getName().equals(""))
				return "Tree \"" + tree.getName() + "\" from trees \"" + currentTreeBlock.getName() + "\" of file " + currentTreeBlock.getFileName() + "  [tree: " + tree + "]";
			else
				return "Tree # " + Integer.toString(MesquiteTree.toExternal(itree))  + " from trees \"" + currentTreeBlock.getName() + "\" of file " + currentTreeBlock.getFileName() + "  [tree: " + tree + "]";
		}
		catch (NullPointerException e) {
			return null;
		}
	}
	/*.................................................................................................................*/
	public String getCurrentTreeNameString(Taxa taxa) {
		setPreferredTaxa(taxa);
		Tree tree = getCurrentTree(taxa);
		if (tree.getName()!=null && !tree.getName().equals(""))
			return "Tree \"" + tree.getName() + "\" from file " + currentTreeBlock.getFileName();
		else
			return "Tree # " + Integer.toString(MesquiteTree.toExternal(currentTree))  + " from file " + currentTreeBlock.getFileName();
	}
 	 /**Returns name to show in windows etc. for tree block or source of trees.*/
 	public String getTreesDescriptiveString(Taxa taxa){
		setPreferredTaxa(taxa);
		if (getNumberOfTrees(taxa)==0)
			return "";
		String description = "";
		try {
			Tree tree;
			int code = checkTreeBlock(taxa, false);
			if (currentTreeBlock != null)
				return currentTreeBlock.getName();
		}
		catch (NullPointerException e) {
		}
		if (laxMode && currentTreeBlock == null)
			return "";
		return getNameForMenuItem();
 	}
	/*.................................................................................................................*/
	public String getName() {
		return "Stored Trees";
	}
	/*.................................................................................................................*/
	public String getNameAndParameters() {
		if (currentTreeBlock ==null)
			return "Stored Trees";

					
		return "Trees stored in block \"" + currentTreeBlock.getName() + "\"";
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies trees stored, for instance in a file.";
	}
	/*.................................................................................................................*/
	public String getParameters() {
		if (currentTreeBlock==null) {
			if (getProject()== null)
				return "";
			else
				return "Trees stored in " + getProject().getName();
		}
		return "Trees \"" + currentTreeBlock.getName() + "\" from file " + currentTreeBlock.getFileName();
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new STCompatibilityTest();
	}
}

class STCompatibilityTest extends CompatibilityTest{
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		return isCompatible(obj, project, prospectiveEmployer, null);
	}
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report){
		if (obj != null && !(obj instanceof Taxa))
			return true;
		else if (obj instanceof Taxa || obj == null) {
			TreesManager manager = null;
			if (prospectiveEmployer == null) {
				if (project!=null){
					MesquiteModule coord = project.getCoordinatorModule();
					if (coord!=null){
						manager = (TreesManager)coord.findElementManager(TreeVector.class);
					}
				}
			}	
			else
				manager = (TreesManager)prospectiveEmployer.findElementManager(TreeVector.class);
			if (manager==null) {
				return true;
			}
			boolean treesExist =  (manager.getNumberTreeBlocks((Taxa)obj)>0);
			if (!treesExist && report != null) {
				report.setValue("there are no stored trees in the data file or project for the requested taxa block");
			}
			return treesExist;
		}
		return true;
	}
}


