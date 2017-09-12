/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BasicTreeWindowMaker;

/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import mesquite.categ.lib.CategDataEditorInitD;
import mesquite.lib.*;
import mesquite.lib.duties.*;

import java.awt.datatransfer.*;

import mesquite.tol.lib.TaxonOnWebServer;
import mesquite.trees.lib.TreeInfoExtraPanel;

/** Makes and manages a Tree Window for tree editing and visualization */
public class BasicTreeWindowMaker extends TreeWindowMaker implements CommandableOwner, TreeContext, TreeDisplayActive {

	public String getName() {
		return "Tree Window";
	}

	/** returns an explanation of what the module does. */
	public String getExplanation() {
		return "Makes a basic tree window, which contains a tool palette.  Hires assistants for the tree window (e.g., Trace Character).";
	}

	public void getEmployeeNeeds() { // This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeDisplayAssistantA.class, "A Tree Window displays trees, and can use various assistants to do analyses.", "You can request this under the <strong>Analysis</strong> menu of the <strong>Tree Window.</strong>");
		e.setPriority(2);
		e.setAsEntryPoint("newAssistant");
		EmployeeNeed e111 = registerEmployeeNeed(TreeDisplayAssistantAO.class, "A Tree Window displays trees, and can use various assistants to do analyses.", "You can request this under the <strong>Analysis (Other)</strong> submenu of the <strong>Tree Window.</strong>");
		e111.setPriority(2);
		e111.setAsEntryPoint("newAssistant");

		EmployeeNeed e2 = registerEmployeeNeed(TreeDisplayAssistantD.class, "A Tree Window displays trees, and can use various assistants to modify the display.", "You can request this under the <strong>Tree</strong> menu of the <strong>Tree Window.</strong>");
		e2.setAsEntryPoint("newAssistant");

		EmployeeNeed e3 = registerEmployeeNeed(TreeWindowAssistantA.class, "A Tree Window displays trees, and can use various assistants to do analyses.", "You can request this under the <strong>Analysis</strong> menu of the <strong>Tree Window.</strong>");
		e3.setAsEntryPoint("newWindowAssistant");

		EmployeeNeed e4 = registerEmployeeNeed(TreeWindowAssistantC.class, "A Tree Window displays trees, and can use various assistants to do analyses.", "You can request this under the <strong>New Chart for Tree submenu of the Analysis</strong> menu of the <strong>Tree Window. </strong>");
		e4.setAsEntryPoint("newWindowAssistant");

		EmployeeNeed e5 = registerEmployeeNeed(TreeWindowAssistantN.class, "A Tree Window displays trees, and can use various assistants.", "You can request this under the <strong>Tree</strong> menu of the <strong>Tree Window.</strong>");
		e5.setAsEntryPoint("newWindowAssistant");

		EmployeeNeed e6 = registerEmployeeNeed(TreeSource.class, "A Tree Window needs a source of trees.", "You can request the source of trees when the Tree Window starts, or later using the Tree Source submenu of the Tree menu of the Tree Window.");
		e6.setPriority(2);
		e6.setAsEntryPoint("setTreeSource");
		EmployeeNeed e20 = registerEmployeeNeed(TreeDisplayAssistantDI.class, "A Tree Window uses various assistants.", "This is activated automatically.");
		EmployeeNeed e7 = registerEmployeeNeed(TreeDisplayAssistantI.class, "A Tree Window uses various assistants.", "This is activated automatically.");
		EmployeeNeed e8 = registerEmployeeNeed(TreeWindowAssistantI.class, "A Tree Window uses various assistants.", "This is activated automatically.");
		EmployeeNeed e9 = registerEmployeeNeed(DrawTreeCoordinator.class, "A Tree Window displays a tree drawn in various possible styles.", "This is activated automatically.");
		e9.setPriority(2);
		EmployeeNeed e10 = registerEmployeeNeed(TreeAlterer.class, "Trees can be altered within the Tree Window.", "Tree altering methods are available in the Alter/Transform Tree submenu of the Tree menu of the Tree Window.<br>");
		e10.setAsEntryPoint("alterTree");
		e10.setPriority(3);
		EmployeeNeed e11 = registerEmployeeNeed(BranchLengthsAlterer.class, "The branch lengths of trees can be altered within the Tree Window.", "Methods to alter branch lengths are available in the Alter/Transform Branch Lengths submenu of the Tree menu of the Tree Window.<br>");
		e11.setAsEntryPoint("alterBranchLengths");
		e11.setPriority(3);

	}

	public void getSubfunctions() {
		registerSubfunction(new FunctionExplanation("Interchange branches", "(A tool of the Tree Window) Exchanges the position of two branches in a tree in a tree window", null, getPath() + "interchange.gif"));
		registerSubfunction(new FunctionExplanation("Collapse branch", "(A tool of the Tree Window) Destroys a branch, thus collapsing its daughter branches into a polytomy", null, getPath() + "collapse.gif"));
		registerSubfunction(new FunctionExplanation("Collapse all branches", "(A tool of the Tree Window) Destroys all internal branches in a clade, thus collapsing the entire clade to a polytomous bush", null, getPath() + "collapseall.gif"));
		registerSubfunction(new FunctionExplanation("Reroot at branch", "(A tool of the Tree Window) Reroots the tree along the branch touched", null, getPath() + "reroot.gif"));
		registerSubfunction(new FunctionExplanation("Prune clade", "(A tool of the Tree Window) Deletes the clade of the node touched.  Only the tree being operated on is affected (that is, the terminal taxa are not deleted from the data file)", "scissors delete", getPath() + "scissors.gif"));
		registerSubfunction(new FunctionExplanation("Ladderize clade", "(A tool of the Tree Window) Ladderizes a clade by rotating branches until largest of sister clades on right (or left, if the Option key is held down)", null, getPath() + "ladderize.gif"));
		super.getSubfunctions();
	}

	/* ................................................................................................................. */
	public DrawTreeCoordinator treeDrawCoordTask;

	public TreeSource treeSourceTask;

	public Vector contextListeners;

	Taxa taxa;

	static boolean warnUnsaved;

	boolean editMode = false;

	MesquiteBoolean printNameOnTree;

	BasicTreeWindow basicTreeWindow;

	MesquiteString treeSourceName;

	MagnifyExtra magnifyExtra;

	MesquiteString xmlPrefs = new MesquiteString();

	boolean useXORForBranchMoves = true;

	String xmlPrefsString = null;
	static {
		warnUnsaved = true;
	}

	/* ................................................................................................................... */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences(xmlPrefs);
		xmlPrefsString = xmlPrefs.getValue();
		makeMenu("Tree");
		if (condition != null && condition instanceof Taxa) {
			taxa = (Taxa) condition;
		}
		resetContainingMenuBar();
		if (MesquiteThread.isScripting() || (arguments == null || !arguments.equalsIgnoreCase("edit")))
			treeSourceTask = (TreeSource) hireCompatibleEmployee(TreeSource.class, condition, "Source of trees (Tree window)");
		else {
			treeSourceTask = (TreeSource) hireNamedEmployee(TreeSource.class, "$ #StoredTrees laxMode", taxa, false);
			editMode = true;
		}

		if (treeSourceTask == null)
			return sorry(getName() + " couldn't start because no source of trees was obtained.");
		treeSourceName = new MesquiteString(treeSourceTask.getName());
		// treeSourceTask.setHiringCommand(makeCommand("setTreeSource", this));
		defineMenus(false);
		if (MesquiteThread.isScripting() && getProject().developing)
			respondToWindowResize = false; // this prevents a lot of tree/window resizing when file being re-read

		contextListeners = new Vector();
		// setAutoSaveMacros(true);
		return true;
	}

	public Taxa getTaxa() {
		return taxa;
	}

	/* ................................................................................................................. */
	public DrawTreeCoordinator hireTreeDrawCoordTask() {
		treeDrawCoordTask = (DrawTreeCoordinator) hireEmployee(DrawTreeCoordinator.class, null);
		if (treeDrawCoordTask == null) {
			sorry(getName() + " couldn't start because no tree draw coordinating module was obtained.");
			return null;
		}
		treeDrawCoordTask.setToLastEmployee(true);
		hireAllEmployees(TreeDisplayAssistantI.class);
		hireAllEmployees(TreeDisplayAssistantDI.class);
		Enumeration enumeration = getEmployeeVector().elements();
		while (enumeration.hasMoreElements()) {
			Object obj = enumeration.nextElement();
			if (obj instanceof TreeDisplayAssistantDI) {
				TreeDisplayAssistantDI init = (TreeDisplayAssistantDI) obj;
				treeDrawCoordTask.requestGuestMenuPlacement(init);
			}
		}
		resetContainingMenuBar();
		return treeDrawCoordTask;
	}

	/*--------------------------------------*/
	public boolean isPrerelease() {
		return false;
	}

	boolean respondToWindowResize = true;

	public void fileReadIn(MesquiteFile f) {
		respondToWindowResize = true;
		if (basicTreeWindow != null) {
			basicTreeWindow.fileReadIn();
		}
	}

	public String getExpectedPath() {
		return getPath() + "recent";
	}

	/*--------------------------------------*/
	/* Menus defined in this method are visible to Mesquite's automatic documentation system <b>(overrides method of MesquiteModule)</b> */
	public void defineMenus(boolean accumulating) {
		if (accumulating || numModulesAvailable(TreeSource.class) > 1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source", makeCommand("setTreeSource", this));
			if (!accumulating) {
				mss.setSelected(treeSourceName);
				mss.setList(TreeSource.class);
				if (taxa != null)
					mss.setCompatibilityCheck(taxa);
			}
		}
	}

	public Commandable[] getCommandablesForAccumulation() {
		Commandable[] cs = new Commandable[1];
		cs[0] = new BasicTreeWindow();
		return cs;
	}

	/* ................................................................................................................. */
	boolean handlingQuitTreeSource = false;

	void disconnectFromTreeBlock(boolean fireCurrentTask) {
		if (basicTreeWindow == null)
			return;
		handlingQuitTreeSource = true;
		
		String d = basicTreeWindow.getTreeDescription();
		String n = "Tree recovered from: " + basicTreeWindow.getTreeNameAndDetails();
		if (fireCurrentTask)
			fireEmployee(treeSourceTask);
		TreeSource temp = (TreeSource) hireNamedEmployee(TreeSource.class, "$ #StoredTrees laxMode", taxa, false);
		if (temp != null) {
			treeSourceTask = temp;
			treeSourceName.setValue(treeSourceTask.getName());
			if (basicTreeWindow != null) {
				basicTreeWindow.setTreeSource(treeSourceTask);
				basicTreeWindow.showTree();
			}
			treeSourceTask.setPreferredTaxa(taxa);
			editMode = true;
			resetContainingMenuBar();
			resetAllWindowsMenus();
		}
		basicTreeWindow.setStoreTreeAsMenuItems(true);
		basicTreeWindow.resetForTreeSource(true, false, true, 0);
		basicTreeWindow.setTreeDescription(d, n);
		basicTreeWindow.treeEdited = false;
		basicTreeWindow.setHighlighted(false);
		handlingQuitTreeSource = false;
	}

	/* ................................................................................................................. */
	public void employeeQuit(MesquiteModule m) {
		if (m instanceof TreeSource && !handlingQuitTreeSource) {
			disconnectFromTreeBlock(false);
		}
		else if (basicTreeWindow != null)
			basicTreeWindow.contentsChanged();
	}

	/* ................................................................................................................. */
	public void processPreferencesFromFile(String[] prefs) {
		if (prefs != null && prefs.length > 0) {
			if (prefs[0].equals("warned")) {
				warnUnsaved = false;
			}
		}
	}

	/* ................................................................................................................. */

	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "warnUnsaved", warnUnsaved);
		if (basicTreeWindow != null) {
			String s = basicTreeWindow.preparePreferencesForXML();
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		return buffer.toString();
	}

	public void processSingleXMLPreference(String tag, String content) {
		if ("warnUnsaved".equalsIgnoreCase(tag))
			warnUnsaved = MesquiteBoolean.fromTrueFalseString(content);
	}

	public boolean treeIsEdited() {
		if (basicTreeWindow == null)
			return false;
		return basicTreeWindow.treeEdited;
	}

	/* ....................................................THIS SHOULD not normally be called within Mesquite. It is here for use of Mesquite as library, e.g. for R-java............................................................. */
	public void showTree(Tree tree) {
		if (basicTreeWindow == null)
			return;
		MesquiteBoolean editStatusToSet = new MesquiteBoolean();
		basicTreeWindow.setCloneOfTree(tree, true, editStatusToSet);
		basicTreeWindow.treeEdited = editStatusToSet.getValue();
	}

	/* ................................................................................................................. */
	/** return whether or not this module should have snapshot saved when saving a macro given the current snapshot mode. */
	public boolean satisfiesSnapshotMode() {
		return (MesquiteTrunk.snapshotMode == Snapshot.SNAPALL || MesquiteTrunk.snapshotMode == Snapshot.SNAPDISPLAYONLY);
	}

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		if (basicTreeWindow == null)
			return null;
		// if (MesquiteTrunk.snapshotMode==Snapshot.SNAPDISPLAYONLY)
		// return null;

		Snapshot temp = new Snapshot();
		Snapshot fromWindow = basicTreeWindow.getSnapshot(file);
		temp.addLine("suppressEPCResponse");

		if (MesquiteTrunk.snapshotMode != Snapshot.SNAPDISPLAYONLY) {
			if (!editMode)
				temp.addLine("setTreeSource ", treeSourceTask);
			else
				temp.addLine("setTreeSourceEditMode", treeSourceTask);

			temp.addLine("setAssignedID " + getPermanentIDString()); // for tree context
		}
		temp.addLine("getTreeWindow");
		temp.addLine("tell It");
		temp.incorporate(fromWindow, true);

		if (MesquiteTrunk.snapshotMode == Snapshot.SNAPDISPLAYONLY) {
			for (int i = 0; i < getNumberOfEmployees(); i++) {
				Object e = getEmployeeVector().elementAt(i);
				if (e instanceof TreeDisplayAssistantDI) {
					temp.addLine("\tnewAssistant ", ((MesquiteModule) e));
				}
			}
		}
		else {
			for (int i = 0; i < getNumberOfEmployees(); i++) {
				Object e = getEmployeeVector().elementAt(i);
				if (e instanceof TreeDisplayAssistantD || e instanceof TreeDisplayAssistantA || e instanceof TreeDisplayAssistantAO) {
					temp.addLine("\tnewAssistant ", ((MesquiteModule) e));
				}
			}
			for (int i = 0; i < getNumberOfEmployees(); i++) {
				Object e = getEmployeeVector().elementAt(i);
				if (e instanceof TreeWindowAssistantC || e instanceof TreeWindowAssistantN || e instanceof TreeWindowAssistantA) {
					if (((TreeWindowAssistant)e).rehireMeInSnapshot())   
						temp.addLine("\tnewWindowAssistant ", ((MesquiteModule) e));
				}
			}
		}
		temp.addLine("endTell");
		if (MesquiteTrunk.snapshotMode != Snapshot.SNAPDISPLAYONLY && editMode) {
			temp.addLine("setEditMode " + StringUtil.tokenize(basicTreeWindow.getTreeNameAndDetails()));
			temp.addLine("desuppressEPCResponseNORESET");
		}
		else
			temp.addLine("desuppressEPCResponse");

		return temp;
	}

	/*
	 * .................................................................................................................* public void getSnapshotForMacro(Snapshot temp) { temp.addLine("getTreeWindow"); temp.addLine("tell It"); // temp.incorporate(fromWindow, true); for (int i = 0; i<getNumberOfEmployees(); i++) { Object e=getEmployeeVector().elementAt(i); if (e instanceof TreeDisplayAssistantDI) { temp.addLine("\tnewAssistant " , ((MesquiteModule)e)); } } temp.addLine("endTell"); }
	 * /*.................................................................................................................
	 */
	/**
	 * Query module as to whether conditions are such that it will have to quit soon -- e.g. if its taxa block has been doomed. The tree window, data window, etc. override this to return true if their object is doomed. This is useful in case MesquiteListener disposing method is not called for an employer before one of its employees discovers that it needs to quit. If the employer is going to quit anyway,there is no use to use auto rehire for the quit employee.
	 */
	public boolean quittingConditions() {
		return (taxa.isDoomed());
	}

	boolean suppressEPCResponse = false;

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the block of taxa associated with this tree window", null, commandName, "getTaxa")) {
			return taxa;
		}
		else if (checker.compare(this.getClass(), "Displays the tree window", null, commandName, "show")) {
			displayTreeWindow();
		}
		else if (checker.compare(this.getClass(), "Suppresses responding to parameters changed.", null, commandName, "suppressEPCResponse")) {
			suppressEPCResponse = true;
		}
		else if (checker.compare(this.getClass(), "Suppresses responding to parameters changed.", null, commandName, "desuppressEPCResponse")) {
			suppressEPCResponse = false;
			if (basicTreeWindow != null)
				basicTreeWindow.resetForTreeSource(false, false, false, MesquiteInteger.inapplicable);
		}
		else if (checker.compare(this.getClass(), "Suppresses responding to parameters changed.", null, commandName, "desuppressEPCResponseNORESET")) {
			suppressEPCResponse = false;
		}
		else if (checker.compare(this.getClass(), "Sets the tree in the window to the description passed", "[standard tree description]", commandName, "setTree")) { // added 10 Jan 02 for use in Send Script dialog
			if (basicTreeWindow != null) {
				basicTreeWindow.doCommand(commandName, arguments, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Sets the mode to that of With Trees To Edit By Hand", null, commandName, "setEditMode")) {
			String mssage = parser.getFirstToken(arguments);
			editMode = true;
			basicTreeWindow.treeEdited = false;
			basicTreeWindow.forceRenameTree(mssage);
			basicTreeWindow.originalTree = null;
			basicTreeWindow.setHighlighted(false);
		}
		else if (checker.compare(this.getClass(), "Sets the source of trees for edit mode", null, commandName, "setTreeSourceEditMode")) {
			if (basicTreeWindow != null)
				basicTreeWindow.hideTree();
			TreeSource temp = (TreeSource) replaceEmployee(TreeSource.class, "$ #StoredTrees laxMode", "Source of trees", treeSourceTask);
			if (temp != null) {
				treeSourceTask = temp;
				treeSourceName.setValue(treeSourceTask.getName());
				if (basicTreeWindow != null) {
					basicTreeWindow.setTreeSource(treeSourceTask);
					basicTreeWindow.showTree();
					basicTreeWindow.resetForTreeSource(true, false, true, 0);
				}
				treeSourceTask.setPreferredTaxa(taxa);

				resetContainingMenuBar();
				resetAllWindowsMenus();
				return treeSourceTask;
			}
			else if (basicTreeWindow != null)
				basicTreeWindow.showTree();
		}
		else if (checker.compare(this.getClass(), "Sets the source of trees", "[name of tree source module]", commandName, "setTreeSource")) {
			if (basicTreeWindow != null)
				basicTreeWindow.hideTree();
			TreeSource temp = (TreeSource) replaceEmployee(TreeSource.class, arguments, "Source of trees", treeSourceTask);
			if (temp != null) {
				if (treeSourceTask != null)
					treeSourceTask.doCommand("laxOff", null, checker);
				editMode = false;
				if (basicTreeWindow != null)
					basicTreeWindow.editedByHand = false;
				treeSourceTask = temp;
				// treeSourceTask.setHiringCommand(makeCommand("setTreeSource", this));
				treeSourceName.setValue(treeSourceTask.getName());
				if (basicTreeWindow != null) {
					basicTreeWindow.setTreeSource(treeSourceTask);
					basicTreeWindow.showTree();
				}
				treeSourceTask.setPreferredTaxa(taxa);

				resetContainingMenuBar();
				resetAllWindowsMenus();
				return treeSourceTask;
			}
			else if (basicTreeWindow != null)
				basicTreeWindow.showTree();
		}
		else if (checker.compare(this.getClass(), "Constructs a tree window referring to a block of taxa, or returns the existing window if this module has already made one.", "[number of block of taxa, 0 based]", commandName, "makeTreeWindow")) {
			if (basicTreeWindow != null)
				return basicTreeWindow;
			String tRef = parser.getFirstToken(arguments);

			int setNumber = MesquiteInteger.fromString(tRef);
			Taxa taxa = null;
			if (!MesquiteInteger.isCombinable(setNumber)) {
				taxa = getProject().getTaxaLastFirst(tRef);
			}
			else
				taxa = getProject().getTaxa(checker.getFile(), setNumber);
			if (taxa != null) {
				makeTreeWindow(taxa);
				return basicTreeWindow;
			}
		}
		else if (checker.compare(this.getClass(), "Returns the tree draw coordinating module", null, commandName, "getTreeDrawCoordinator")) {
			return treeDrawCoordTask;
		}
		else if (checker.compare(this.getClass(), "Returns the tree window", null, commandName, "getTreeWindow")) {
			return basicTreeWindow;
		}
		else if (checker.compare(this.getClass(), "Returns the tree", null, commandName, "getTree")) {
			return getTree();
		}
		else if (checker.compare(this.getClass(), "Sets the tree as having been edited (thus showing it as \"untitled\"", null, commandName, "treeEdited")) {
			basicTreeWindow.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Hires a tree display assistant module", "[name of assistant module]", commandName, "newAssistant")) {
			return basicTreeWindow.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Hires a tree window assistant module", "[name of assistant module]", commandName, "newWindowAssistant")) {
			return basicTreeWindow.doCommand(commandName, arguments, checker);
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	/* ................................................................................................................. */
	public void employeeOutputInvalid(MesquiteModule employee, MesquiteModule source) {
		if (basicTreeWindow == null)
			return;
		basicTreeWindow.contentsChanged();
		basicTreeWindow.treeDisplay.pleaseUpdate(false);

	}

	/* ................................................................................................................. */
	/** Generated by an employee calling its dearEmployerShouldIHandleThis method. The MesquiteModule should respond false if the employer wants to handle it itself. */
	public boolean employeeRequestingIndependenceOfAction(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee instanceof TreeSource) {
			int code = Notification.getCode(notification);
			if (code == MesquiteListener.BLOCK_DELETED && (employee.nameMatches("StoredTrees") || (employee.nameMatches("ConsensusTree"))))
				return false;
		}
		return true;
	}

	/* ................................................................................................................. */
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (basicTreeWindow == null)
			return;
		if (employee instanceof DrawTreeCoordinator) {
			if (source instanceof DrawNamesTreeDisplay && Notification.getCode(notification) == TreeDisplay.FONTSIZECHANGED) {
				basicTreeWindow.sizeDisplay();
				basicTreeWindow.treeDisplay.pleaseUpdate(true);
				return;
			}
			if (basicTreeWindow.drawSizeSubmenu != null) {
				if (basicTreeWindow.drawSizeSubmenu.isEnabled() != !treeDrawCoordTask.hasPreferredSize()) {
					basicTreeWindow.drawSizeSubmenu.setEnabled(!treeDrawCoordTask.hasPreferredSize());
					basicTreeWindow.sizeDisplay();
					basicTreeWindow.treeDisplay.pleaseUpdate(true);
					return;
				}
			}
			Dimension d = treeDrawCoordTask.getPreferredSize();
			if (d == null && basicTreeWindow.oldPreferred == null)
				return;
			if ((d == null && basicTreeWindow.oldPreferred != null) || (basicTreeWindow.oldPreferred == null && d != null) || (d.width != basicTreeWindow.oldPreferred.width && d.height != basicTreeWindow.oldPreferred.height)) {
				// if (basicTreeWindow.drawingSizeMode != BasicTreeWindow.FIXEDSIZE){
				// basicTreeWindow.setSuggestedSize(false, true);
				basicTreeWindow.sizeDisplay();
				basicTreeWindow.treeDisplay.pleaseUpdate(true);
				// }
			}

		}
		else if (employee instanceof TreeSource) {
			if (suppressEPCResponse)
				return;
			int code = Notification.getCode(notification);
			if (code == MesquiteListener.BLOCK_DELETED && (employee.nameMatches("StoredTrees") || (employee.nameMatches("ConsensusTree")))) {
				disconnectFromTreeBlock(true);
			}
			else if (code == MesquiteListener.ITEMS_ADDED) {
				if (basicTreeWindow.usingDefaultTree) {
					basicTreeWindow.resetForTreeSource(false, false, false, Notification.getCode(notification));
					basicTreeWindow.contentsChanged();
				}
				else
					basicTreeWindow.numTreesChanged();
			}
			else if (code == MesquiteListener.NUM_ITEMS_CHANGED) {
				basicTreeWindow.numTreesChanged();
			}
			else if (!(notification != null && notification.getObjectClass() == Taxa.class)) { // if notification came from TAxa changes, don't respond, as that will be handled otherwise
				if (code != MesquiteListener.SELECTION_CHANGED) {
					if (basicTreeWindow.originalTree != null && basicTreeWindow.originalTree instanceof MesquiteTree && basicTreeWindow.taxa != null)
						basicTreeWindow.taxa.removeListener((MesquiteTree) basicTreeWindow.originalTree);
					basicTreeWindow.originalTree = null; // otree
					editMode = false;
					basicTreeWindow.resetForTreeSource(false, false, MesquiteThread.isDuringNotification(), Notification.getCode(notification)); // if switching between tree blocks, should reset to zero! If storing tree in tree block, shouldn't!
					basicTreeWindow.contentsChanged();
				}
			}
		}
		else {
			if (suppressEPCResponse)
				return;
			if (basicTreeWindow.treeDisplay != null) {
				basicTreeWindow.contentsChanged();
				TreeDisplayExtra[] ee = basicTreeWindow.treeDisplay.getMyExtras(employee);
				if (ee != null)
					for (int i = 0; i < ee.length; i++)
						ee[i].setTree(basicTreeWindow.treeDisplay.getTree()); // done to force recalculations
				basicTreeWindow.treeDisplay.pleaseUpdate(false);
			}
		}

	}

	/* ................................................................................................................. */
	public TreeDisplay getTreeDisplay() {
		return basicTreeWindow.treeDisplay;
	}

	/* ................................................................................................................. */
	/** because TreeContext */
	public Tree getTree() {
		if (basicTreeWindow == null)
			return null;
		else
			return basicTreeWindow.tree;
	}

	/* ................................................................................................................. */
	/** because TreeContext */
	public String getContextName() {
		if (basicTreeWindow == null)
			return "Tree Window";
		return basicTreeWindow.getTitle();
	}

	/* ................................................................................................................. */
	/** because TreeContext */
	public void addTreeContextListener(TreeContextListener listener) {
		if (listener != null && contextListeners.indexOf(listener) < 0)
			contextListeners.addElement(listener);
	}

	/* ................................................................................................................. */
	/** because TreeContext */
	public void removeTreeContextListener(TreeContextListener listener) {
		contextListeners.removeElement(listener);
	}

	/* ................................................................................................................. */
	/** because TreeContext */
	public MesquiteModule getTreeSource() {
		return treeSourceTask;
	}

	/* ................................................................................................................. */
	private void makeTreeWindow(Taxa taxa) {
		incrementMenuResetSuppression();
		this.taxa = taxa;
		treeSourceTask.setPreferredTaxa(taxa);
		BasicTreeWindow btw = new BasicTreeWindow(this, treeSourceTask, taxa, xmlPrefsString, editMode);
		setModuleWindow(btw);
		basicTreeWindow = (BasicTreeWindow) getModuleWindow();
		Enumeration e = getEmployeeVector().elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TreeDisplayAssistantI || obj instanceof TreeDisplayAssistantDI) {
				TreeDisplayAssistant tca = (TreeDisplayAssistant) obj;
				basicTreeWindow.addAssistant(tca);
			}
		}
		btw.sizeDisplay();
		MesquiteMenuSpec aux = addAuxiliaryMenu("Analysis:Tree");
		MesquiteCommand mC = makeCommand("newWindowAssistant", basicTreeWindow);
		MesquiteSubmenuSpec mms = addSubmenu(aux, "New Chart for Tree", mC);
		mms.setZone(0);
		mms.setList(TreeWindowAssistantC.class);
		addMenuItem(aux, "-", null);
		addModuleMenuItems(aux, makeCommand("newAssistant", basicTreeWindow), TreeDisplayAssistantA.class);
		addModuleMenuItems(aux, makeCommand("newWindowAssistant", basicTreeWindow), TreeWindowAssistantA.class);
		addMenuItem(aux, "-", null);
		MesquiteSubmenuSpec mmsO = addSubmenu(aux, "Other Analyses with Tree", makeCommand("newWindowAssistant", basicTreeWindow));
		mmsO.setList(TreeWindowAssistantOA.class);
		addMenuItem(aux, "-", null);
		MesquiteSubmenuSpec mmis = addSubmenu(aux, "Visual Tree Analysis", makeCommand("setTreeDrawer", treeDrawCoordTask));
		mmis.setList(AnalyticalDrawTree.class);
		// addMenuItem(aux, "-", null);
		// addMenuItem(null, "Force Repaint", makeCommand("forceRepaint", basicTreeWindow));

		if (!MesquiteThread.isScripting()) {
			displayTreeWindow();
		}
		decrementMenuResetSuppression();
	}

	void displayTreeWindow() {
		if (getModuleWindow() == null)
			return;
		getModuleWindow().setVisible(true);
		basicTreeWindow.showTree();
		getModuleWindow().toFront();
	}

	public Tree goToTreeNumber(int index) {
		if (basicTreeWindow == null)
			return null;
		return basicTreeWindow.goToTreeNumber(index, true);
	}

	/* ................................................................................................................. */
	public void windowGoAway(MesquiteWindow whichWindow) {
		iQuit();
	}

	public void endJob() {
		if (contextListeners != null) {
			Enumeration e = contextListeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeContextListener) {
					TreeContextListener tce = (TreeContextListener) obj;
					tce.disposing(this);
				}
			}

			contextListeners.removeAllElements();
		}
		treeDrawCoordTask = null;
		treeSourceTask = null;
		basicTreeWindow = null;

		super.endJob();
	}

	/* ................................................................................................................. */
	public String getParameters() {
		String s = "";
		;
		if (getModuleWindow() != null) {
			if (((BasicTreeWindow) getModuleWindow()).treeEdited)
				s += "Tree shown is not directly from source, but rather is an edited tree. ";
			if (((BasicTreeWindow) getModuleWindow()).getTaxa() != null)
				s += "Taxa: " + ((BasicTreeWindow) getModuleWindow()).getTaxa().getName();
		}
		return s;
	}

	/* ................................................................................................................. */
	public boolean isSubstantive() {
		return true;
	}
	/* ................................................................................................................. */
	boolean downInTree = false;

	public boolean mouseDownInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		if (x > 2000000 || y > 2000000)
			return true; // here because of bug in Mac OS X 10.0.4
		if (!treeDisplay.getTree().isLocked()) {
			downInTree = true;
			Shape c = g.getClip();
			g.setClip(0, 0, 99999, 99999);
			if (basicTreeWindow.isFauxScrollPane())
				g.setClip(basicTreeWindow.getTreeViewport());

			boolean st = basicTreeWindow.ScanTouch(g, x, y, modifiers);
			g.setClip(c);
			return st;
		}
		return false;
	}

	/* ................................................................................................................. */
	public boolean mouseUpInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {

		if (x > 2000000 || y > 2000000) {
			downInTree = false;
			return true; // here because of bug in Mac OS X 10.0.4
		}
		if (!treeDisplay.getTree().isLocked()) {
			if (!downInTree)
				return false;
			Shape c = g.getClip();
			g.setClip(0, 0, 99999, 99999);
			if (basicTreeWindow.isFauxScrollPane())
				g.setClip(basicTreeWindow.getTreeViewport());
			basicTreeWindow.ScanDrop(g, x, y, modifiers);
			g.setClip(c);
			basicTreeWindow.branchFrom = 0;
		}
		downInTree = false;
		return true;
	}

	/* ................................................................................................................. */
	public boolean mouseMoveInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		if (x > 2000000 || y > 2000000)
			return true; // here because of bug in Mac OS X 10.0.4
		if (!treeDisplay.getTree().isLocked()) {
			Shape c = g.getClip();
			g.setClip(0, 0, 99999, 99999);
			if (basicTreeWindow.isFauxScrollPane()) {
				g.setClip(basicTreeWindow.getTreeViewport());
			}
			// g.setClip(0,0,99999, 99999);
			basicTreeWindow.ScanFlash(g, x, y, modifiers);
			g.setClip(c);
		}
		// basicTreeWindow.setExplanation(basicTreeWindow.baseExplanation, false);
		return true;
	}

	/* ................................................................................................................. */
	public boolean mouseDragInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		if (x > 2000000 || y > 2000000)
			return true; // here because of bug in Mac OS X 10.0.4
		if (!treeDisplay.getTree().isLocked()) {
			if (!downInTree)
				return false;
			Shape c = g.getClip();
			g.setClip(0, 0, 99999, 99999);
			if (basicTreeWindow.isFauxScrollPane())
				g.setClip(basicTreeWindow.getTreeViewport());
			// g.setClip(0,0,99999, 99999);
			basicTreeWindow.ScanDrag(g, x, y, modifiers);
			g.setClip(c);
		}
		return true;
	}

	/* ................................................................................................................. */
	/** Returns command to hire employee if clonable */
	public String getClonableEmployeeCommand(MesquiteModule employee) {
		if (employee != null && employee.getEmployer() == this) {
			if (employee.getHiredAs() == TreeWindowAssistant.class)
				return ("newWindowAssistant " + StringUtil.tokenize(employee.getName()) + ";");// quote
			if (employee.getHiredAs() == TreeDisplayAssistant.class)
				return ("newAssistant " + StringUtil.tokenize(employee.getName()) + ";");// quote
		}
		return null;
	}

	public void transposeField() {
		if (basicTreeWindow == null)
			return;
		int w = basicTreeWindow.totalTreeFieldWidth;
		basicTreeWindow.totalTreeFieldWidth = basicTreeWindow.totalTreeFieldHeight;
		basicTreeWindow.totalTreeFieldHeight = w;
		basicTreeWindow.sizeDisplay();
	}

	/*
	 * .................................................................................................................* public CompatibilityTest getCompatibilityTest() { return new BTWCompatibilityTest(); } /*.................................................................................................................
	 */
	public boolean getUseXORForBranchMoves() {
		return useXORForBranchMoves;
	}

	public void setUseXORForBranchMoves(boolean useXORForBranchMoves) {
		this.useXORForBranchMoves = useXORForBranchMoves;
	}
}

/*
 * class BTWCompatibilityTest extends CompatibilityTest{ //should find out if available matrices of chosen sort public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){ return true; } }
 * 
 * /* ========================================================================
 */
class BasicTreeWindow extends MesquiteWindow implements Fittable, MesquiteListener, AdjustmentListener, XMLPreferencesProcessor {
	MesquiteTree tree;
	MesquiteTree oldTree = null; // used just to determine if version number of current tree matches; not used otherwise
	MesquiteTree undoTree = null; // listens to taxa directly
	MesquiteTree previousTree = null; // listens to taxa directly
	Tree previousEditedTree; // listens to taxa directly
	Tree originalTree;
	TreeVector recentEditedTrees;
	int maxRecentEditedTrees = 20;
	TreeDisplay treeDisplay;
	Taxa taxa;
	DrawTreeCoordinator treeDrawCoordTask;
	boolean treeEdited = false;
	boolean editedByHand = false;
	// private Rectangle treeRect;
	// MesquiteScrollbar hScroll, vScroll;
	TreeScrollPane treePane;
	Adjustable hScroll, vScroll;
	
	int scanLineThickness = 3;
	boolean usingPane = false;
	TreeSource treeSourceTask;
	boolean warningGivenForTreeSource = false;
	static final int scrollWidth = 16;
	static final int initalWindowHeight = 400;
	static final int initalWindowWidth = 520;
	static final int baseMessageWidth = 256;
	int messageWidth = 256;
	int totalTreeFieldWidth = 800;
	int totalTreeFieldHeight = 800;
	TreeWindowPalette palette;
	MessagePanel messagePanel;
	/* New code added Feb.07 oliver */// TODO: delete new code comments
	ControlStrip controlStrip;
	/* End new code added Feb.07 oliver */
	boolean canUndo = true;
	MesquiteCommand setTreeNumberCommand;
	TreeTool currentTreeTool;
	MesquiteBoolean ladderizeAfterReroot = new MesquiteBoolean(true);
	MesquiteCMenuItemSpec toggleRerootLadderizeMSpec = null;
	MesquiteCheckMenuItem toggleRerootLadderizeMenuItem = null;
	// boolean initiating = true;
	// MesquiteBoolean sizeToFit;
	// MesquiteBoolean useSuggestedSize;
	TreeTool toolMAG;
	TreeTool rerootTool;
	int drawingSizeMode;
	final static int AUTOSIZE = 0;
	final static int SCALETOFIT = 1;
	final static int FIXEDSIZE = 2;
	StringArray sizeModes;
	MesquiteString sizeModeName;
	MesquiteBoolean infoPanelOn;
	MesquiteBoolean floatLegends;
	MesquiteBoolean textVersionDrawOnTree;
	BasicTreeWindowMaker windowModule;
	MesquiteInteger dropHighlightedBranch = new MesquiteInteger(0);
	MesquiteInteger highlightedBranch = null;
	int branchFrom;
	int highlightedTaxon = -1;
	int taxonTouched = -1;
	int xFrom, yFrom, xTo, yTo, fieldTouchX, fieldTouchY, lastFieldDragX, lastFieldDragY;
	static final int CONTINUE_WITH_EDITED = 1;
	static final int CHANGE_TREE_WITH_SOURCE = 0;
	static final int ASK_USER_TREE_CHANGE = 2;
	static int editedTreeMODE = ASK_USER_TREE_CHANGE;
	boolean usingDefaultTree = false;
	private int currentTreeNumber = 0;
	// MesquiteMenuItemSpec sizeItem;
	static int numWindows = 0;
	private int windowNum = 0;
	MagnifyExtra magnifyExtra;
	// int setPosX = MesquiteInteger.unassigned;
	// int setPosY = MesquiteInteger.unassigned;
	long treeVersion = 0;
	MesquiteCommand undoCommand, copyCommand, pasteCommand;
	MesquiteString baseExplanation;
	TreeReference treeReference = new TreeReference();
	// MesquiteString currentTreeFootnote;
	boolean baseExplanationUsed = false;
	boolean treeAnnotationShown = false;
	MesquiteMenuItemSpec storeTreeMenuItem, storeTreeAsMenuItem, storeTreeAsOtherMenuItem, recoverEditedMenuItem;
	MesquiteMenuItemSpec floatLegendsItem;
	int oldH = 0;
	int oldV = 0;
	boolean showTreeListOnSave = false; // have preference for this?
	Dimension oldPreferred = null;
	TreeInfoPanel treeInfoPanel;
	boolean treeInfoPanelEverShown = false;
	MesquiteSubmenuSpec drawSizeSubmenu;
	/*
	 * When lockStoredTrees is true (MacClade mode), editing a stored tree causes the tree to be treated as unsaved, and for the new tree to be saved, Store Tree or Replace Stored Tree must be called. When false, editing a stored tree changes the original in storage.
	 */
	// boolean lockStoredTrees = false;
	// LockPanel lockPanel;

	public BasicTreeWindow() {
	}

	public BasicTreeWindow(BasicTreeWindowMaker ownerModule, TreeSource tsT, Taxa taxa, String xmlPrefsString, boolean editMode) {
		super(ownerModule, true); // INFOBAR
		windowModule = ownerModule;
		// this.ownerModule = ownerModule;
		setWindowSize(initalWindowWidth, initalWindowHeight);
		ownerModule.setModuleWindow(this);
		baseExplanation = new MesquiteString("");

		numWindows++;
		windowNum = numWindows;
		this.taxa = taxa;

		// 1
		treeDrawCoordTask = windowModule.hireTreeDrawCoordTask(); // do this here to ensure that any modules hired by the task have a window into which to put things
		if (treeDrawCoordTask == null)
			return;
		setIcon(MesquiteModule.getRootImageDirectoryPath() + "windowIcons/tree.gif");

		recentEditedTrees = new TreeVector(taxa);
		this.ownerModule = ownerModule;
		this.treeSourceTask = tsT;
		// useSuggestedSize = new MesquiteBoolean(true);
		infoPanelOn = new MesquiteBoolean(false);
		// sizeToFit = new MesquiteBoolean(true);
		floatLegends = new MesquiteBoolean(true);
		ownerModule.printNameOnTree = new MesquiteBoolean(true);
		textVersionDrawOnTree = new MesquiteBoolean(false);
		controlStrip = new ControlStrip((BasicTreeWindowMaker) ownerModule);
		MesquiteButton listButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("showTaxaList", this), null, true, MesquiteModule.getRootImageDirectoryPath() + "listT.gif", 12, 16);
		listButton.setShowBackground(false);
		listButton.setUseWaitThread(false);
		listButton.setButtonExplanation("Show List of Taxa window");
		controlStrip.addButton(listButton);
		MesquiteButton infoButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("toggleInfoPanel", this), null, true, MesquiteModule.getRootImageDirectoryPath() + "showInfo.gif", 12, 16);
		// infoBar.addExtraButton(MesquiteModule.getRootImageDirectoryPath() + "showInfo.gif", MesquiteModule.makeCommand("toggleInfoPanel", this));
		infoButton.setUseWaitThread(false);
		infoButton.setShowBackground(false);
		infoButton.setButtonExplanation("Show Tree Info Panel");
		controlStrip.addButton(infoButton);
		addToWindow(controlStrip);
		ownerModule.addCheckMenuItem(null, "Show Tree Info Panel", ownerModule.makeCommand("toggleInfoPanel", this), infoPanelOn);
		treeDrawCoordTask.addCheckMenuItem(null, "Add Name to Printed Tree", ownerModule.makeCommand("togglePrintName", this), ownerModule.printNameOnTree);
		tree = null;
		if (originalTree != null && originalTree instanceof MesquiteTree)
			taxa.removeListener((MesquiteTree) originalTree);
		Tree tempTree = null;
		if (!editMode) {
			tempTree = treeSourceTask.getTree(taxa, 0);
			// in case treeSourceTask quits at this point, will reset tree source task
			if (tempTree == null || treeSourceTask != ownerModule.treeSourceTask) {
				treeSourceTask = ownerModule.treeSourceTask;
				tempTree = treeSourceTask.getTree(taxa, 0);
			}
			originalTree = tempTree; // otree
		}
		else {
			tempTree = taxa.getDefaultDichotomousTree(null);
			usingDefaultTree = true;
		}

		if (originalTree != null && originalTree instanceof MesquiteTree)
			taxa.addListener((MesquiteTree) originalTree);
		if (tempTree == null) {
			tree = taxa.getDefaultTree();
			usingDefaultTree = true;
			ownerModule.discreetAlert(MesquiteThread.isScripting() || warningGivenForTreeSource, "Tree source \"" + treeSourceTask.getName() + "\" is not supplying a tree; a default tree may be shown in Tree Window (c).");
			if (!MesquiteThread.isScripting())
				warningGivenForTreeSource = true;
		}
		else
			tree = tempTree.cloneTree();
		// currentTreeFootnote = new MesquiteString(tree.getAnnotation());
		// currentTreeFootnote.addListener(this);
		treeAnnotationShown = true; // so that the base Explanation can refer to the annotation
		treeVersion = tree.getVersionNumber();
		oldTree = tree;
		if (undoTree != null)
			taxa.removeListener(undoTree);
		if (previousTree != null)
			taxa.removeListener(previousTree);
		undoTree = tree.cloneTree();
		previousTree = tree.cloneTree();
		taxa.addListenerHighPriority(undoTree);
		taxa.addListenerHighPriority(previousTree);
		hookCurrentTree();
		if (taxa != null) {
			taxa.addListener(this);
		}
		messagePanel = new MessagePanel(ownerModule);
		addToWindow(messagePanel);
		messagePanel.setVisible(true);
		treeDisplay = treeDrawCoordTask.createOneTreeDisplay(taxa, this);

		treeDisplay.textVersionDrawOnTree = textVersionDrawOnTree.getValue();

		treeDisplay.setLocation(0, 0);
		/*
		 * treeRect = treeDisplay.getVisRect(); hScroll = new TWScroll(this, Scrollbar.HORIZONTAL, 0, 2, 0, 0); vScroll = new TWScroll(this, Scrollbar.VERTICAL, 0, 2, 0, 0); treeDisplay.translatePoint = new Point(0,0); addToWindow(hScroll); addToWindow(vScroll);
		 */
		treePane = new TreeScrollPane(this);
		hScroll = treePane.getHAdjustable();
		vScroll = treePane.getVAdjustable();
		hScroll.addAdjustmentListener(this);
		vScroll.addAdjustmentListener(this);
		MesquiteWindow.addKeyListener(this, treePane);

		addToWindow(treeDisplay);
		highlightedBranch = treeDisplay.getHighlightedBranchMI();
		palette.setUpBirdsEye();
		// setSuggestedSize(false, false);

		storeTreeMenuItem = ownerModule.addMenuItem("Store Tree", ownerModule.makeCommand("storeTree", this));
		storeTreeAsMenuItem = ownerModule.addMenuItem("Store Tree As...", ownerModule.makeCommand("storeTreeAs", this));
		storeTreeAsOtherMenuItem = ownerModule.addMenuItem("Store Tree In Tree Block As...", ownerModule.makeCommand("storeTreeAsOther", this));
		setStoreTreeAsMenuItems(editMode);

		ownerModule.addMenuSeparator();
		recoverEditedMenuItem = ownerModule.addMenuItem("Recover Last Edited Tree", ownerModule.makeCommand("recoverLastEditedTree", this));
		ownerModule.addMenuItem("Edited Tree Handling Options...", ownerModule.makeCommand("queryEditedTreeMode", this));
		ownerModule.addMenuSeparator();

		ownerModule.addMenuItem("Choose Tree...", ownerModule.makeCommand("chooseTree", this));
		MesquiteMenuItemSpec mm = ownerModule.addMenuItem("Next Tree", ownerModule.makeCommand("nextTree", this)); // DOES THIS WORK??
		mm.setShortcut(KeyEvent.VK_UP); // right
		mm = ownerModule.addMenuItem("Previous Tree", ownerModule.makeCommand("previousTree", this));
		mm.setShortcut(KeyEvent.VK_DOWN); // right
		ownerModule.addMenuItem("Step Through Trees...", ownerModule.makeCommand("stepThroughTrees", this));
		ownerModule.addMenuSeparator();
		ownerModule.addSubmenu(null, "Alter/Transform Tree", ownerModule.makeCommand("alterTree", this), TreeAlterer.class);
		ownerModule.addSubmenu(null, "Alter/Transform Branch Lengths", ownerModule.makeCommand("alterBranchLengths", this), BranchLengthsAlterer.class);
		ownerModule.addMenuItem("Cut Selected Taxa", ownerModule.makeCommand("cutSelectedTaxa", this));
		ownerModule.addMenuSeparator();
		ownerModule.addModuleMenuItems(null, ownerModule.makeCommand("newWindowAssistant", this), TreeWindowAssistantN.class);
		ownerModule.addModuleMenuItems(null, ownerModule.makeCommand("newAssistant", this), TreeDisplayAssistantD.class);
		ownerModule.addMenuSeparator();

		MesquiteSubmenuSpec mBringToFrontAsst = ownerModule.addSubmenu(null, "Bring To Front");
		mBringToFrontAsst.setList(treeDisplay.getExtras());
		mBringToFrontAsst.setListableFilter(TreeDisplayDrawnExtra.class);
		mBringToFrontAsst.setCommand(ownerModule.makeCommand("bringToFront", this));
		MesquiteSubmenuSpec mSaveMacroAsst = ownerModule.addSubmenu(null, "Save Tree Analysis as Macro");
		mSaveMacroAsst.setList(ownerModule.getEmployeeVector());
		mSaveMacroAsst.setListableFilter(TreeDisplayAssistantAD.class);
		mSaveMacroAsst.setCommand(ownerModule.makeCommand("saveMacroDisplayAssistant", this));

		MesquiteSubmenuSpec mCloseAsst = ownerModule.addSubmenu(null, "Close/Remove");
		mCloseAsst.setList(ownerModule.getEmployeeVector());
		mCloseAsst.setListableFilter(TreeDisplayAssistantAD.class);
		mCloseAsst.setCommand(ownerModule.makeCommand("closeDisplayAssistant", this));

		treeDrawCoordTask.addMenuSeparator();

		drawingSizeMode = AUTOSIZE;
		if (treeDrawCoordTask.hasPreferredSize())
			sizeModes = new StringArray(1);
		else
			sizeModes = new StringArray(3);
		sizeModes.setValue(AUTOSIZE, "Size Automatically"); // the strings passed will be the menu item labels
		sizeModes.setValue(SCALETOFIT, "Scale to Fit");
		sizeModes.setValue(FIXEDSIZE, "Fixed Drawing Size...");

		sizeModeName = new MesquiteString(sizeModes.getValue(drawingSizeMode)); // this helps the menu keep track of checkmenuitems
		drawSizeSubmenu = treeDrawCoordTask.addSubmenu(null, "Drawing Size", MesquiteModule.makeCommand("setDrawingSizeMode", this), sizeModes);
		drawSizeSubmenu.setSelected(sizeModeName);
		drawSizeSubmenu.setEnabled(!treeDrawCoordTask.hasPreferredSize());
		floatLegendsItem = treeDrawCoordTask.addCheckMenuItem(null, "Float Legends", ownerModule.makeCommand("toggleLegendFloat", this), floatLegends);
		treeDrawCoordTask.addMenuItem("Legends To Default Positions", ownerModule.makeCommand("legendsToHome", this));
		treeDrawCoordTask.addCheckMenuItem(null, "Text Extras On Trees", ownerModule.makeCommand("toggleTextOnTree", this), textVersionDrawOnTree);
		treeDrawCoordTask.addMenuSeparator();

		undoCommand = MesquiteModule.makeCommand("undo", this);
		copyCommand = MesquiteModule.makeCommand("copyTree", this);
		pasteCommand = MesquiteModule.makeCommand("paste", this);

		treeDisplay.setTaxonNameBuffer(30);

		/*
		 * lockPanel=new LockPanel(this); addToWindow(lockPanel); lockPanel.setLocation(0, getHeight()-scrollWidth); lockPanel.setVisible(true);
		 */

		setBackground(Color.white);
		/* Edited Feb.06 was (see commented out line below) oliver */// TODO: Delete new code comments
		// messagePanel.setLocation(0, getHeight()-scrollWidth);
		messagePanel.setLocation(25, getHeight() - scrollWidth);
		controlStrip.setLocation(0, getHeight() - scrollWidth);
		/* End edited Feb.06 oliver */

		TreeTool tool1 = new TreeTool(this, "arrow", MesquiteModule.getRootImageDirectoryPath(), "arrow.gif", 4, 2, "Move branch", "This tool is used for rearranging the tree by moving branches.  By clicking on one branch, then holding the mouse button down, one can drag the branch and drop it on another branch.  If a legal move, the first branch will be cut and grafted on the second branch.  A move is illegal if the first branch is a descendant of the second branch.");
		tool1.setTransferredCommand(MesquiteModule.makeCommand("moveBranch", this));
		// tool1.setTouchedTaxonCommand(MesquiteModule.makeCommand("touchTaxon", this)); //cut as of 1.02
		currentTreeTool = tool1;
		addTool(tool1);
		setCurrentTool(currentTreeTool);
		currentTreeTool.setInUse(true);
		currentTreeTool.setIsArrowTool(true);

		toolMAG = new TreeTool(this, "zoom", ownerModule.getPath(), "zoom.gif", 4, 4, "Zoom image", "This tool magnifies (or shrinks, if Option/Alt is held down) the image of the whole tree.  It is enabled only if the Drawing Size is set to Size Automatically");
		toolMAG.setTouchedCommand(MesquiteModule.makeCommand("zoomBT", this));
		toolMAG.setTouchedFieldCommand(MesquiteModule.makeCommand("zoom", this));
		toolMAG.setTouchedTaxonCommand(MesquiteModule.makeCommand("zoomBT", this));
		toolMAG.setOptionImageFileName("zoomOut.gif", 4, 4);
		addTool(toolMAG);
		toolMAG.setEnabled(drawingSizeMode == AUTOSIZE);
		toolMAG.setOptionsCommand(new MesquiteCommand("zoomOptions", this));
		/**/

		TreeTool tool2 = new TreeTool(this, "interchange", ownerModule.getPath(), "interchange.gif", 7, 13, "Interchange branches", "This tool is used as is the Move Branch (arrow) tool, except that the result is to interchange the locations of the two branches.  It can be used to rotate a node.");
		tool2.setTransferredCommand(MesquiteModule.makeCommand("exchangeBranches", this));
		addTool(tool2);

		TreeTool tool3 = new TreeTool(this, "collapse", ownerModule.getPath(), "collapse.gif", 7, 12, "Collapse branch", "This tool destroys a branch, thus collapsing its daughter branches into a polytomy.  It cannot be used on terminal branches.");
		tool3.setTouchedCommand(MesquiteModule.makeCommand("collapseBranch", this));
		addTool(tool3);

		TreeTool tool4 = new TreeTool(this, "collapseall", ownerModule.getPath(), "collapseall.gif", 8, 13, "Collapse all branches", "This tool destroys all internal branches in a clade, thus collapsing the entire clade to a polytomous bush.");
		tool4.setTouchedCommand(MesquiteModule.makeCommand("collapseAll", this));
		tool4.setOptionImageFileName("collapseAllBelow.gif", 14, 4);
		addTool(tool4);

		rerootTool = new TreeTool(this, "reroot", ownerModule.getPath(), "reroot.gif", 3, 4, "Reroot at branch", "This tool reroots the tree along the branch touched.");
		rerootTool.setTouchedCommand(MesquiteModule.makeCommand("rootAlongBranch", this));
		addTool(rerootTool);
		rerootTool.setOptionsCommand(new MesquiteCommand("rerootOptions", this));
		toggleRerootLadderizeMSpec = ownerModule.addCheckMenuItem(null, "Ladderize After Reroot", MesquiteModule.makeCommand("toggleRerootLadderize", this), ladderizeAfterReroot);
		toggleRerootLadderizeMenuItem = new MesquiteCheckMenuItem(toggleRerootLadderizeMSpec);

		TreeTool tool5 = new TreeTool(this, "scissors", ownerModule.getPath(), "scissors.gif", 6, 5, "Prune clade", "This tool deletes the clade of the node touched.  Only the tree being operated on is affected (that is, the terminal taxa are not deleted from the data file).");
		tool5.setTouchedCommand(MesquiteModule.makeCommand("cutClade", this));
		addTool(tool5);

		TreeTool tool7 = new TreeTool(this, "ladderize", ownerModule.getPath(), "ladderize.gif", 3, 13, "Ladderize clade", "This tool ladderizes a clade by rotating branches until largest of sister clades on right (except if option key is held down, in which case, left).");
		tool7.setTouchedCommand(MesquiteModule.makeCommand("ladderize", this));
		tool7.setTouchedTaxonCommand(MesquiteModule.makeCommand("focalLadderize", this));
		tool7.setOptionImageFileName("reverseLadderize.gif", 13, 13);
		addTool(tool7);

		TreeTool tool8 = new TreeTool(this, "triangle", ownerModule.getPath(), "triangle.gif", 7, 13, "Draw clade as triangle", "This tool designates the clade of the node touched to be drawn in compact form, for instance as a triangle.  This affects only drawing; it does not affect calculations, as the clade (with its phylogenetic structure) is still present in the tree.  This can be used for large trees to hide temporarily details within some clades.");
		tool8.setTouchedCommand(MesquiteModule.makeCommand("drawAsTriangle", this));
		addTool(tool8);
		TreeTool tool9 = new TreeTool(this, "magnify", ownerModule.getPath(), "magnify.gif", 4, 4, "Magnify Clade", "This tool fills the tree window with the clade of the node touched.  The rest of the tree is present, but not shown.  This can be use for large trees to focus on individual clades");
		tool9.setTransferredCommand(MesquiteModule.makeCommand("magnifyClade", this));
		addTool(tool9);
		magnifyExtra = new MagnifyExtra(ownerModule, treeDisplay, tool9);
		ownerModule.magnifyExtra = magnifyExtra;
		treeDisplay.addExtra(magnifyExtra);
		treeDisplay.setTree(tree);
		treeDisplay.setVisible(true);
		treeInfoPanel = new TreeInfoPanel(this);
		TreeInfoPanelAssistant valuesAssistant = (TreeInfoPanelAssistant) ownerModule.hireNamedEmployee(TreeInfoPanelAssistant.class, "#TreeInfoValues");
		treeInfoPanel.addExtraPanel(valuesAssistant.getPanel(treeInfoPanel));

		XMLUtil.readXMLPreferences(ownerModule, this, xmlPrefsString);

		setTreeName(tree);
		sizeDisplay();
		ownerModule.hireAllEmployees(TreeWindowAssistantI.class);
		Enumeration em = ownerModule.getEmployeeVector().elements();
		if (tree != null)
			while (em.hasMoreElements()) {
				Object obj = em.nextElement();
				MesquiteModule mb = (MesquiteModule) obj;
				if (mb instanceof TreeWindowAssistant) {
					((TreeWindowAssistant) mb).setTree(tree);
				}
			}
		setShowExplanation(true);
		setShowAnnotation(true);
		baseExplanation.setValue("This is a tree window.  In it you can view trees from various tree sources, edit trees, and store trees.");
		setExplanation(baseExplanation, true);
		resetTitle();
		// initiating = false;

	}

	public void requestFocus(){
		treePane.requestFocus();
	}

	void setStoreTreeAsMenuItems(boolean editMode) {
		if (treeSourceTask.nameMatches("StoredTrees")) {
			if (editMode) {
				storeTreeAsMenuItem.setName("Store Tree As...");
				storeTreeAsMenuItem.setEnabled(false);
				storeTreeAsOtherMenuItem.setName("Store Tree In Tree Block As...");
				storeTreeAsOtherMenuItem.setEnabled(true);
			}
			else {
				storeTreeAsMenuItem.setName("Store Tree In Current Block As...");
				storeTreeAsMenuItem.setEnabled(true);
				storeTreeAsOtherMenuItem.setName("Store Tree In Other Block As...");
				storeTreeAsOtherMenuItem.setEnabled(true);
			}
		}
		else {
			storeTreeAsMenuItem.setName("Store Tree As...");
			storeTreeAsMenuItem.setEnabled(false);
			storeTreeAsOtherMenuItem.setName("Store Copy of Tree As...");
			storeTreeAsOtherMenuItem.setEnabled(true);
		}
		storeTreeMenuItem.setEnabled(!treeSourceLocked());
	}

	/* ................................................................................................................. */
	public void processSingleXMLPreference(String tag, String content) {
		if ("toggleRerootLadderize".equalsIgnoreCase(tag))
			ladderizeAfterReroot.setValue(MesquiteBoolean.fromTrueFalseString(content));
		else if ("editedTreeRetainMode".equalsIgnoreCase(tag)) {
			int x = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(x))
				editedTreeMODE = x;
		}
	}

	/* ................................................................................................................. */
	public void processSingleXMLPreference(String tag, String flavor, String content) {
	}

	/* ................................................................................................................. */
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "toggleRerootLadderize", ladderizeAfterReroot);
		StringUtil.appendXMLTag(buffer, 2, "editedTreeRetainMode", editedTreeMODE);
		return buffer.toString();
	}

	/* ................................................................................................................. */
	void setTreeInfoPanel(boolean show) {
		infoPanelOn.setValue(show);
		if (show) {
			treeInfoPanelEverShown = true;
			addSidePanel(treeInfoPanel, TreeInfoPanel.width);
			treeInfoPanel.setVisible(true);
			treeInfoPanel.setTree(tree);
			String title = "Tree Information";
			treeInfoPanel.repaint();
		}
		else {
			if (treeInfoPanel != null)
				removeSidePanel(treeInfoPanel);
		}
	}

	void treeInfoPanelGoAway() {
		setTreeInfoPanel(false);
	}

	/* ................................................................................................................. */
	public String searchData(String s, MesquiteString commandResult) {
		if (StringUtil.blank(s) || taxa == null)
			return "<h2>Nothing to search for (searched: \"" + s + "\")</h2>";
		if (commandResult != null)
			commandResult.setValue((String) null);
		String listData = taxa.searchData(s, commandResult);

		if (!StringUtil.blank(listData))
			return "<h2>Matches to search string: \"" + s + "\"</h2>" + listData;
		else
			return "<h2>No matches found (searched: \"" + s + "\")</h2>";
	}

	/* ................................................................................................................. */
	private void makeTaxonVisible(int i) {
		if (drawingSizeMode == SCALETOFIT)
			return;
		if (usingPane) {
			TreeDrawing td = treeDisplay.getTreeDrawing();
			int x = (int)td.x[treeDisplay.getTree().nodeOfTaxonNumber(i)];    //integer node loc approximation
			int y = (int)td.y[treeDisplay.getTree().nodeOfTaxonNumber(i)];    //integer node loc approximation
			int w = getTreePaneWidth();
			int h = getTreePaneHeight();
			setOrigin(x - w / 2, y - h / 2, true);
			treeDisplay.pleaseUpdate(true);
			sizeDisplay();
		}
	}

	/* ................................................................................................................. */
	protected ToolPalette makeToolPalette() {
		setTreeNumberCommand = ownerModule.makeCommand("setTreeNumber", this);
		palette = new TreeWindowPalette((BasicTreeWindowMaker) ownerModule, this);
		palette.setFirstToolHeight(76);
		return palette;
	}

	/* ................................................................................................................. */
	/**
	 * When called the window will determine its own title. MesquiteWindows need to be self-titling so that when things change (names of files, tree blocks, etc.) they can reset their titles properly
	 */
	public void resetTitle() {
		String t;
		if (treeSourceTask == null)
			t = "Tree Window " + windowNum + " for taxa \"" + taxa.getName() + "\"";
		else
			t = treeSourceTask.getTreesDescriptiveString(taxa);
		if (StringUtil.blank(t)) {
			if (treeEdited || editedByHand)
				t = "Edited Tree";
			else
				t = "Tree";
		}
		setTitle(t);
	}

	public MesquiteCommand getUndoCommand() {
		return undoCommand;
	}

	public MesquiteCommand getCopySpecialCommand() {
		return copyCommand;
	}

	public String getCopySpecialName() {
		return "Copy Tree";
	}

	public MesquiteCommand getPasteSpecialCommand() {
		return pasteCommand;
	}

	public String getPasteSpecialName() {
		return "Paste Tree";
	}

	/* @@@@@@@@@@@@@@@@@@@@@@========= pane/scrolling ==========@@@@@@@@@@@@@@@@@@@@@ */
	private void toggleLegendFloat() { // called only when recently changed
		if (false && usingPane) { // turning off float; adjust offsets
			// get bounds on visible part of treeDisplay
			Point pt = getTreeScrollPoint();
			Dimension dim = getTreeViewportSize();
			// cycle through all components getting those that are Legends
			Component[] cc = treeDisplay.getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++) {
					if (cc[i] instanceof Legend) {
						Legend legend = (Legend) cc[i];
						Rectangle rect = legend.getBounds();

						// int deltaBaseX = (treeDisplay.getBounds().width + hScroll.getValue()) - (treePane.getWidth());
						int deltaBaseY = (treeDisplay.getBounds().height + vScroll.getValue()) - (treePane.getHeight());
						if (!floatLegends.getValue()) {
							// legend.setOffsetX(legend.getOffsetX()-deltaBaseX);
							legend.setOffsetY(legend.getOffsetY() - deltaBaseY);
						}
						else {
							// legend.setOffsetX(legend.getOffsetX()+deltaBaseX);
							legend.setOffsetY(legend.getOffsetY() + deltaBaseY);
						}
					}
				}
		}
		checkPanelPositionsLegal();
	}

	private void OLDtoggleLegendFloat() { // called only when recently changed
		if (!usingPane || drawingSizeMode == SCALETOFIT || floatLegends.getValue()) { // legends can't float
			checkPanelPositionsLegal();
		}
		else { // undoing floating
			// get bounds on visible part of treeDisplay
			Point pt = getTreeScrollPoint();
			Dimension dim = getTreeViewportSize();

			// cycle through all components getting those that are Legends
			Component[] cc = treeDisplay.getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++) {
					if (cc[i] instanceof Legend) {
						// use getOffsetX(); for current and
						// adjustLocation
						Legend legend = (Legend) cc[i];
						Rectangle rect = legend.getBounds();
						legend.setConstrainingRectangle(treeDisplay.getBounds());

						// switching from float to non-float.
						if (rect.x - pt.x > dim.width / 2) {
							// move legend to right border
							// legend.setOffsetX(treeDisplay.getBounds().width - dim.width + legend.getOffsetX());
							legend.setOffsetX(-treeDisplay.getBounds().width - treeDisplay.getBounds().x + dim.width - rect.width);
						}
						else {
							// move legend to left border
							legend.setOffsetX(-treeDisplay.getBounds().width - treeDisplay.getBounds().x);
							// legend.setOffsetX(legend.getOffsetX() - pt.x);
						}
						if (rect.y - pt.y > dim.height / 2) {
							// move legend to bottom border
							// legend.setOffsetY(treeDisplay.getBounds().height - dim.height + legend.getOffsetY());
							legend.setOffsetY(-treeDisplay.getBounds().height - treeDisplay.getBounds().y + dim.height - rect.height);
						}
						else {
							// move legend to top border
							// legend.setOffsetY(legend.getOffsetY() - pt.y);
							legend.setOffsetY(-treeDisplay.getBounds().height - treeDisplay.getBounds().y);
						}
						legend.adjustLocation();
					}
				}
			checkPanelPositionsLegal();
		}
	}

	public void checkPanelPositionsLegal() {
		if (treeDisplay == null)
			return;
		try {
			// if (true || !usingPane || drawingSizeMode == SCALETOFIT || !floatLegends.getValue()) { //legends can't float or aren't floating
			Component[] cc = treeDisplay.getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++) {
					if (cc[i] instanceof Legend) { // make sure legends are in bounds
						// adjustLocation
						Legend legend = (Legend) cc[i];
						if (usingPane && floatLegends.getValue()) {
							legend.setConstrainingRectangle(treeDisplay.getBounds());
						}
						else {
							legend.setConstrainingRectangle(treeDisplay.getBounds()); // treeDisplay.getBounds()
						}
						legend.adjustLocation();
					}
				}
			// }
		} catch (Exception ex) {
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource() == hScroll)
			scrollTouched(Scrollbar.HORIZONTAL, hScroll.getValue());
		else
			scrollTouched(Scrollbar.VERTICAL, vScroll.getValue());
	}

	/* ................................................................................................................. */
	void scrollTouched(int orientation, int value) {
		if (usingPane)
			resetLegends();
		/*
		 * if (usingPane){ if (orientation == Scrollbar.HORIZONTAL){ treeRect.x = value; } else { treeRect.y = value; }
		 * 
		 * setOrigin(treeRect.x, treeRect.y); //sizeDisplay(); treeDisplay.pleaseUpdate(false);
		 * 
		 * }
		 */
		checkPanelPositionsLegal();
	}

	/* ................................................................................................................. */
	void resetLegends() {

		if (usingPane && drawingSizeMode != SCALETOFIT && treeDisplay != null && floatLegends.getValue() && hScroll != null) {
			int changeH = hScroll.getValue() - oldH;
			int changeV = vScroll.getValue() - oldV;
			if (changeH != 0 || changeV != 0) {
				Component[] cc = treeDisplay.getComponents();
				if (cc != null && cc.length > 0)
					for (int i = 0; i < cc.length; i++) {
						if (cc[i] instanceof Legend) {
							Legend legend = (Legend) cc[i];
							legend.setOffsetX(legend.getOffsetX() + changeH);
							legend.setOffsetY(legend.getOffsetY() + changeV);
							legend.adjustLocation();
						}
					}

			}

			oldH = hScroll.getValue();
			oldV = vScroll.getValue();
		}
		else
			checkPanelPositionsLegal();
	}

	boolean isFauxScrollPane() {
		if (usingPane)
			return treePane.isFauxScrollPane();
		return false;
	}

	Dimension getTreeViewportSize() {
		if (usingPane)
			return treePane.getViewportSize();
		// SCROLLPANEreturn treePane.getViewportSize();
		else
			return new Dimension(treeDisplay.getWidth(), treeDisplay.getHeight());
	}

	Rectangle getTreeViewport() {
		if (usingPane && hScroll != null) {
			if (!isFauxScrollPane() && MesquiteTrunk.isMacOSX())
				return new Rectangle(0, 0, treeDisplay.getWidth(), treeDisplay.getHeight());
			hScroll = treePane.getHAdjustable();
			vScroll = treePane.getVAdjustable();
			return new Rectangle(hScroll.getValue(), vScroll.getValue(), treePane.getContentsWidth(), treePane.getContentsHeight());
			// SCROLLPANEreturn treePane.getViewportSize();
		}
		else
			return new Rectangle(0, 0, treeDisplay.getWidth(), treeDisplay.getHeight());
	}

	Point getTreeScrollPoint() {
		if (usingPane)
			return treePane.getScrollPosition();
		// SCROLLPANEreturn treePane.getScrollPosition();
		else
			return new Point(0, 0);
	}

	int getTreePaneWidth() {
		if (usingPane)
			return treePane.getBounds().width;
		else
			return treeDisplay.getBounds().width;

	}

	int getTreePaneHeight() {
		if (usingPane)
			return treePane.getBounds().height;
		else
			return treeDisplay.getBounds().height;
	}

	/* ................................................................................................................. */
	public void togglePane(boolean paneOn, boolean resetOrigin) {

		if (paneOn) {
			if (!usingPane) {
				// SCROLLPANEif (!initiating)
				// SCROLLPANE removeFromWindow(treeDisplay);
				// SCROLLPANEif (treePane != null)
				// SCROLLPANE removeFromWindow(treePane);
				messageWidth = baseMessageWidth;
				if (palette != null && palette.birdsEyeBox != null)
					palette.birdsEyeBox.setVisible(true);
				removeFromWindow(treeDisplay);
				addToWindow(treePane);
				treePane.setLocation(0, 0);
				treePane.addTreeDisplay(treeDisplay);
				usingPane = true;

				hScroll = treePane.getHAdjustable();
				vScroll = treePane.getVAdjustable();

				hScroll.setUnitIncrement(10);
				hScroll.setBlockIncrement((getWidth() - scrollWidth) / 10);
				/*
				 * hScroll.setBounds(0, getHeight()-scrollWidth-scrollWidth, getWidth()-scrollWidth, scrollWidth); vScroll.setBounds(getWidth()-scrollWidth, 0, scrollWidth, getHeight()-scrollWidth-scrollWidth);
				 */
				vScroll.setBlockIncrement((getHeight() - scrollWidth) / 10);
				vScroll.setUnitIncrement(10);
				resetScrolls();
				oldH = hScroll.getValue();
				oldV = vScroll.getValue();
				/*
				 * vScroll.doLayout(); hScroll.doLayout(); hScroll.setVisible(true); vScroll.setVisible(true);
				 */
				// treePane.setSize(getWidth(), getHeight());
				treePane.setSize(getWidth(), getHeight() - scrollWidth);
				// SCROLLPANEaddToWindow(treePane);
				// sizeDisplay();
				// if (MesquiteInteger.isCombinable(setPosX))
				// treePane.setScrollPosition(-setPosX, -setPosY);
				// else treePane.setScrollPosition(0, 0);
				if (palette != null)
					palette.setFieldSize(totalTreeFieldWidth, totalTreeFieldHeight);
				// sizeItem.setEnabled(true);
				floatLegendsItem.setEnabled(true);
				// if (!initiating) {
				ownerModule.resetContainingMenuBar();
				// treePane.setVisible(true);
				treeDisplay.pleaseUpdate(true);
				messagePanel.repaint();
				// lockPanel.repaint();
				// }
				setOrigin(0, 0, true);
				// initiating = false;
				treePane.doLayout();
			}
		}
		else {
			if (usingPane) {
				usingPane = false;
				removeFromWindow(treePane);
				addToWindow(treeDisplay);
				// if (!initiating)
				// removeFromWindow(treePane);
				/*
				 * hScroll.setVisible(false); vScroll.setVisible(false); hScroll.setLocation(getWidth(), getHeight()); //off screen vScroll.setLocation(getWidth(), getHeight()); //off screen
				 */
				// treePane = null;
				// SCROLLPANEtreeDisplay.setLocation(0, 0);
				// SCROLLPANEaddToWindow(treeDisplay);
				palette.birdsEyeBox.setVisible(false);
				// if (sizeItem!=null)
				// sizeItem.setEnabled(false);
				if (floatLegendsItem != null)
					floatLegendsItem.setEnabled(false);
				// if (!initiating) {
				ownerModule.resetContainingMenuBar();
				treeDisplay.pleaseUpdate(true);
				messagePanel.repaint();
				/* New code added Feb.07 oliver */// TODO: Delete new code comments
				controlStrip.repaint();
				treeDisplay.setLocation(0, 0);
				/* End new code added Feb.07 oliver */
				// lockPanel.repaint();
				// }
				// initiating = false;
			}
			if (resetOrigin)
				setOrigin(0, 0, true);
		}
		treeDisplay.setVisRect(getTreeViewport());
	}

	public void setOrigin(int x, int y, boolean setScrolls) {
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (usingPane) {
			treeDisplay.setLocation(-x, -y);
			if (setScrolls) {
				hScroll.setValue(x);
				vScroll.setValue(y);

			}
			treeDisplay.setVisRect(getTreeViewport());
		}

		resetLegends();
	}

	public void resetScrolls() {
		if (hScroll.getValue() < 0)
			hScroll.setValue(0);
		if (vScroll.getValue() < 0) {
			vScroll.setValue(0);
		}
	}

	public int getOriginX() {
		if (!usingPane)
			return 0;
		Point p = getTreeScrollPoint();
		return p.x;
		// Adjustable h = treePane.getHAdjustable();
		// return -(int)( ((1.0*(h.getValue()- h.getMinimum()))/(h.getMaximum()-h.getMinimum()))*treeDisplay.getBounds().width);
	}

	public int getOriginY() {
		if (!usingPane)
			return 0;

		Point p = getTreeScrollPoint();
		return p.y;
		// Adjustable v = treePane.getVAdjustable();
		// return -(int)( ((1.0*(v.getValue()- v.getMinimum()))/(v.getMaximum()-v.getMinimum()))*treeDisplay.getBounds().height);
	}

	/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ pane/scrolling @@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
	/* ................................................................................................................. */
	/**
	 * Called in some circumstances (not all) when a component is added to a container in the window. Currently used so that the Tree window knows that a component has been added to the TreeDisplay.
	 */
	public void componentAdded(Container cont, Component comp) {
		checkPanelPositionsLegal();
	}

	public void setVisible(boolean v) {
		super.setVisible(v);
		if (v && treeDisplay != null) {
			treeDisplay.redoCalculations(89);
			treeDisplay.repaint(true);
		}

	}
	void fileReadIn(){
		sizeDisplay();
		if (tree != null)
			setTreeName(tree);
	}
	/* ................................................................................................................. */
	int countSizes = 0;

	void sizeDisplay() {
		if (palette == null || treeDisplay == null || messagePanel == null)
			return;
		if (treeDrawCoordTask.hasPreferredSize()) {
			drawingSizeMode = AUTOSIZE;
		}
		if (drawSizeSubmenu != null)
			drawSizeSubmenu.setEnabled(!treeDrawCoordTask.hasPreferredSize());
		treeDisplay.autoStretchIfNeeded = true; // this is always true at moment; delete?
		palette.setFieldSize(totalTreeFieldWidth, totalTreeFieldHeight);
		if (drawingSizeMode == SCALETOFIT) {
			togglePane(false, true);
			treeDisplay.setSize(getWidth(), getHeight() - scrollWidth);
			treeDisplay.setFieldSize(getWidth(), getHeight() - scrollWidth);
			treeDisplay.autoStretchIfNeeded = true;
			scale = 0;
			treeDisplay.redoCalculations(8813);
		}
		else if (drawingSizeMode == AUTOSIZE) {
			int w = getWidth() - scrollWidth;
			int h = getHeight() - scrollWidth - scrollWidth;
			Dimension s = treeDrawCoordTask.getPreferredSize();
			if (s != null) {
				togglePane(true, false);
				// treeDisplay.setSize(w,h);
				if (treePane != null)
					treePane.setSize(getWidth(), getHeight() - scrollWidth);
				treeDisplay.setSize(s.width, s.height);
				treeDisplay.setFieldSize(s.width, s.height);
				treeDisplay.redoCalculations(88173);
			}
			else {

				if (taxa == null) {
					treeDisplay.setSize(w, h);

					treeDisplay.setFieldSize(w, h);
					treeDisplay.redoCalculations(28813);
					togglePane(false, false);
				}
				else {
					int basicMinSpacing = 12;
					Graphics g = treeDisplay.getGraphics();
					if (g != null) {
						FontMetrics fm = g.getFontMetrics(treeDisplay.getTaxonNamesFont());
						if (fm != null) {
							basicMinSpacing = fm.getMaxAscent() + fm.getMaxDescent();
						}
					}
					boolean canFit = true;
					Tree tree = treeDisplay.getTree();
					int numTaxa = 0;
					if (tree != null)
						numTaxa = tree.numberOfTerminalsInClade(tree.getRoot());
					else
						numTaxa = taxa.getNumTaxa();
					// canFit = numTaxa<50;

					// if (!canFit){
					if (treeDisplay.getOrientation() == TreeDisplay.UP || treeDisplay.getOrientation() == TreeDisplay.DOWN)
						canFit = numTaxa * basicMinSpacing < w;
					else if (treeDisplay.getOrientation() == TreeDisplay.RIGHT || treeDisplay.getOrientation() == TreeDisplay.LEFT)
						canFit = numTaxa * basicMinSpacing < h;
					else
						canFit = numTaxa * 6 < (w + h) / 2;
					// }

					if (canFit && scale <= 0) {
						treeDisplay.setSize(w, h);

						treeDisplay.setFieldSize(w, h);
						treeDisplay.redoCalculations(8813);
						togglePane(false, false);
					}
					else {
						treeDisplay.autoStretchIfNeeded = true;
						if (treeDisplay.getOrientation() == TreeDisplay.UP || treeDisplay.getOrientation() == TreeDisplay.DOWN) {
							totalTreeFieldWidth = numTaxa * basicMinSpacing;
							totalTreeFieldHeight = h - scrollWidth - 4;
						}
						else if (treeDisplay.getOrientation() == TreeDisplay.RIGHT || treeDisplay.getOrientation() == TreeDisplay.LEFT) {
							totalTreeFieldWidth = w - scrollWidth - 4;
							totalTreeFieldHeight = numTaxa * basicMinSpacing;
						}
						else {
							totalTreeFieldWidth = numTaxa * 8;
							totalTreeFieldHeight = numTaxa * 8;
						}
						if (scale > 0) {
							for (int i = 0; i < scale; i++) {
								totalTreeFieldWidth *= 2;
								totalTreeFieldHeight *= 2;
							}
						}
						else if (scale < 0) {
							for (int i = 0; i > scale; i--) {
								totalTreeFieldWidth /= 2;
								totalTreeFieldHeight /= 2;
							}
						}
						// treeDisplay.setSize(w, h);
						treeDisplay.setSize(totalTreeFieldWidth, totalTreeFieldHeight);
						treeDisplay.setFieldSize(totalTreeFieldWidth, totalTreeFieldHeight);
						treeDisplay.redoCalculations(881153);
						togglePane(true, false);
						treePane.setSize(getWidth(), getHeight() - scrollWidth);
						// toggleLegendFloat();
					}
				}
			}
		}
		else { // fixed size
			int useWidth = MesquiteInteger.maximum(totalTreeFieldWidth, getWidth());
			int useHeight = MesquiteInteger.maximum(totalTreeFieldHeight, getHeight() - scrollWidth);
			// treeDisplay.setFieldSize(totalTreeFieldWidth, totalTreeFieldHeight);
			treeDisplay.setFieldSize(totalTreeFieldWidth, totalTreeFieldHeight);
			treeDisplay.setSize(useWidth, useHeight);
			treeDisplay.redoCalculations(88);
			togglePane(true, false);
			treePane.setSize(getWidth(), getHeight() - scrollWidth);
		}
		int CONTROLWIDTH = 42;
		messageWidth = getWidth() - CONTROLWIDTH;
		if (usingPane) {
			// treeDisplay.setSize(getWidth()-scrollWidth, getHeight()-scrollWidth-scrollWidth);
			// hScroll.setBounds(0, getHeight()-scrollWidth-scrollWidth+1, getWidth()-scrollWidth, scrollWidth);
			// vScroll.setBounds(getWidth()-scrollWidth, 0, scrollWidth, getHeight()-scrollWidth-scrollWidth);
			// hScroll.setValue(treePane.x);
			treePane.setHMinMax(0, treeDisplay.getFieldWidth() - treePane.getWidth() - scrollWidth);

			int increment = treePane.getHeight() / 10;
			if (increment>treeDisplay.getFieldHeight())
				increment = treeDisplay.getFieldHeight();
			vScroll.setBlockIncrement(increment);
			increment = treePane.getWidth() / 10;
			if (increment>treeDisplay.getFieldWidth())
				increment = treeDisplay.getFieldWidth();
			hScroll.setBlockIncrement(increment);
			vScroll.setUnitIncrement(10);
			hScroll.setUnitIncrement(10);
			resetScrolls();
			treePane.setVMinMax(0, treeDisplay.getFieldHeight() - treePane.getHeight() - scrollWidth - scrollWidth);
			treePane.doLayout();
			// vScroll.doLayout();
			// hScroll.doLayout();
			// setOrigin(treeRect.x, treeRect.y);
		}
		else {
			treeDisplay.setSize(getWidth(), getHeight() - scrollWidth);
			setOrigin(0, 0, true);
			// hScroll.setLocation(getWidth(), getHeight()); //off screen
			// vScroll.setLocation(getWidth(), getHeight()); //off screen
		}
		treeDisplay.setVisRect(getTreeViewport());
		messagePanel.setLocation(CONTROLWIDTH, getHeight() - scrollWidth);
		controlStrip.setLocation(0, getHeight() - scrollWidth);
		controlStrip.setSize(CONTROLWIDTH, scrollWidth);
		messagePanel.setSize(messageWidth, scrollWidth);
		checkPanelPositionsLegal();
		treeDisplay.forceRepaint();

	}

	/* ................................................................................................................. */
	public Taxa getTaxa() {
		return taxa;
	}

	/* ................................................................................................................. */
	public void hideTree() {
		treeDisplay.setVisible(false);
	}

	/* ................................................................................................................. */
	public void showTree() {
		treeDisplay.setVisible(true);
	}

	/* ................................................................................................................. */
	/* ................................................................................................................. */
	public String getTreeDescription() {
		if (tree == null)
			return null;
		return tree.writeTree(Tree.BY_NAMES, false);
	}

	public void setTreeDescription(String s) {
		setTreeDescription(s, null);
	}

	public void setTreeDescription(String s, String name) {
		originalTree = null;
		Tree tr = setTree(s, name);

		if (tr != null) {
			treeEdited(true);
		}
	}

	/* ................................................................................................................. */
	public void setTreeSource(TreeSource tsTask) {
		boolean setToZero = tsTask != treeSourceTask;
		treeSourceTask = tsTask;
		setStoreTreeAsMenuItems(windowModule.editMode);

		resetTitle();
		if (!MesquiteThread.isScripting()) {
			resetForTreeSource(setToZero, true, false, MesquiteInteger.inapplicable);
			contentsChanged();
			treeDisplay.repaint();
		}
		else {
			resetForTreeSource(setToZero, true, false, MesquiteInteger.inapplicable);
		}
	}

	private void zapPreviousEdited(boolean resetEnabling) {
		boolean wasEnabled = false;
		if (resetEnabling) {
			if (recoverEditedMenuItem != null)
				wasEnabled = recoverEditedMenuItem.isEnabled();
		}
		if (previousEditedTree != null)
			previousEditedTree.dispose();
		if (previousEditedTree != null && previousEditedTree instanceof MesquiteTree)
			taxa.removeListener((MesquiteTree) previousEditedTree);
		previousEditedTree = null;
		if (recoverEditedMenuItem != null)
			recoverEditedMenuItem.setEnabled(false);
		if (resetEnabling) {
			if (recoverEditedMenuItem != null && wasEnabled != recoverEditedMenuItem.isEnabled())
				MesquiteTrunk.resetMenuItemEnabling();
		}
	}

	/* ................................................................................................................. */
	private void queryEditedTreeRetentionPreference() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(this, "Retain Edited Tree?", buttonPressed);
		queryDialog.addLabel("Retain Edited Tree?");
		queryDialog.addLargeOrSmallTextLabel("If you have edited a tree in the tree window, then you ask for a change in the tree source, do you want retain the edited tree, or go immediately " + " to the new tree implied by the change in the tree source?  Choose an option:");
		RadioButtons alignRadios = queryDialog.addRadioButtons(new String[] { "Switch to new tree, but remember edited tree for later recovery", "Continue to show edited tree", "Ask each time whether to continue with the edited tree, or switch to the new tree" }, editedTreeMODE);

		queryDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue() == 0) {
			int oldMode = editedTreeMODE;
			editedTreeMODE = alignRadios.getValue();
			if (oldMode != editedTreeMODE)
				ownerModule.storePreferences();
		}
		queryDialog.dispose();
	}

	private boolean askAboutRetainingEditedTree() { // return true if to retain
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(this, "Retain Edited Tree?", buttonPressed);
		queryDialog.addLabel("Retain Edited Tree?");
		queryDialog
		.addLargeOrSmallTextLabel("The tree in the window \"" + getTitle() + "\" has been edited but not saved.  " + "Do you want to go to the new tree, or do you want to continue to show the edited tree in the window?\n\nIf you continue to show the edited tree, remember that the tree " + "shown might not come from the source of trees currently used by the window.");

		queryDialog
		.appendToHelpString("If you choose to go to the new tree, you may be able to recover the edited tree by selecting Recover Last Edited Tree in the Tree menu. " + "\n\nIf you choose to continue showing the edited tree, you can store the edited tree by selecting Store Tree As from the Tree menu." + " To see a tree that belongs to the source of trees used by the window, hit the Enter arrow of the " + "Tree scroll in the upper left of the tree window.");
		Checkbox dontAsk = queryDialog.addCheckBox("Don't ask again. (To change preference, choose Edited Tree Handling Options from the Tree menu.)", false);
		queryDialog.completeAndShowDialog("Switch to new tree", "Continue showing edited tree", null, (String) null);
		if (dontAsk.getState()) {
			int oldMode = editedTreeMODE;
			if (buttonPressed.getValue() == 0) // switch to new tree
				editedTreeMODE = CHANGE_TREE_WITH_SOURCE;
			else
				editedTreeMODE = CONTINUE_WITH_EDITED;
			if (oldMode != editedTreeMODE)
				ownerModule.storePreferences();
		}

		queryDialog.dispose();

		return buttonPressed.getValue() != 0;
		/*
		 * return !AlertDialog.query(this, "Retain edited tree?", "The tree in the window has been edited but not saved.  " + "Do you want to go to the new tree, or do you want to continue to show the edited tree in the window?\n\nIf you continue, remember that it " + "does not come from the source of trees currently used by the window.  " + "To see the first tree that does belong to the source, hit the Enter arrow of the " + "Tree scroll in the upper left of the tree window. To store your edited tree first, ask to continue and then choose Store Tree As from the Tree menu."
		 * + " (To change whether or not Mesquite asks you about this each time, choose Edited Tree Handling Options from the Tree menu.)", "Switch to new tree", "Continue showing edited tree", 0);
		 */
	}

	/*
	 * Three modes: verbose: if the tree source changes, or its parameters change, and there is an edited tree, then user is pestered to retain edited tree or not UNLESS change was trees added or deleted, in which case the edited tree is retained quiet retain (old style): continue to show edited tree even though it may not match source quiet shift: always discard edited tree, but
	 * 
	 * static final int CONTINUE_WITH_EDITED = 1; static final int CHANGE_TREE_WITH_SOURCE = 0; static final int ASK_USER_TREE_CHANGE = 2; static int editedTreeMODE = CHANGE_TREE_WITH_SOURCE;
	 */
	/* ................................................................................................................. */

	void resetForTreeSource(boolean setToZero, boolean firstTimeTreeSource, boolean retainEditedRegardless, int notificationCode) {
		if (disposing)
			return;
		windowModule.editMode = false;   //if you get here, you can't be in editMode.  
		resetTitle();
		if (firstTimeTreeSource)
			warningGivenForTreeSource = false;
		MesquiteTree editedTree = null;
		if (taxa != null && taxa.isDoomed()) {
			ownerModule.iQuit();
			return;
		}
		if (tree != null) {
			boolean retainTree = false;
			if (retainEditedRegardless || MesquiteThread.isScripting() || editedTreeMODE == CONTINUE_WITH_EDITED || notificationCode == MesquiteListener.ITEMS_ADDED || notificationCode == MesquiteListener.PARTS_ADDED || notificationCode == MesquiteListener.PARTS_DELETED || notificationCode == MesquiteListener.PARTS_MOVED)
				retainTree = true;
			else if (editedTreeMODE == ASK_USER_TREE_CHANGE && treeEdited && editedByHand)
				retainTree = askAboutRetainingEditedTree();
			else
				retainTree = false;

			boolean wasEnabled = false;
			if (recoverEditedMenuItem != null)
				wasEnabled = recoverEditedMenuItem.isEnabled();
			if (treeEdited) {
				if (retainTree) {
					editedTree = tree; // if this is done, and the tree is unot disposed, then it will be remembered
					zapPreviousEdited(false);
				}
				else {
					if (previousEditedTree != null && previousEditedTree instanceof MesquiteTree)
						taxa.removeListener((MesquiteTree) previousEditedTree);
					previousEditedTree = tree.cloneTree();
					if (previousEditedTree instanceof MesquiteTree)
						taxa.addListener((MesquiteTree) previousEditedTree);
					if (recoverEditedMenuItem != null)
						recoverEditedMenuItem.setEnabled(true);
				}
			}
			else {
				zapPreviousEdited(false);
			}
			if (recoverEditedMenuItem != null && wasEnabled != recoverEditedMenuItem.isEnabled())
				MesquiteTrunk.resetMenuItemEnabling();
			unhookPreviousTree();
			if (!retainTree)
				tree.dispose();

		}
		treeAnnotationShown = false;
		// tree=null; //done to catch spurious redraws
		treeVersion = 0;
		treeDisplay.setTree(null); // done to catch spurious redraws
		int numTrees = treeSourceTask.getNumberOfTrees(taxa);
		if (numTrees == 0) {
			currentTreeNumber = 0;
			palette.paletteScroll.setCurrentValue(0);
			palette.paletteScroll.setMinimumValue(0);
			palette.paletteScroll.setMaximumValue(0);
			palette.paletteScroll.setEnableEnter(false);
			palette.paletteScroll.setEnterLock(true);
		}
		else {
			palette.paletteScroll.setEnterLock(false);
			palette.paletteScroll.setEnableEnter(true);
			palette.paletteScroll.setMinimumValue(MesquiteTree.toExternal(0));
			palette.paletteScroll.setMaximumValue(MesquiteTree.toExternal(numTrees - 1));
			if (setToZero || currentTreeNumber >= numTrees)
				goToTreeNumber(0, false);
			else
				goToTreeNumber(currentTreeNumber, false);
		}

		if (editedTree != null) {
			// originalTree = null;
			setTree(editedTree);
			setTreeName(editedTree);
			if (firstTimeTreeSource) {
				if (originalTree != null && originalTree instanceof MesquiteTree)
					taxa.removeListener((MesquiteTree) originalTree);
				originalTree = null; // otree
			}
			treeEdited(false);
		}

		storeTreeMenuItem.setEnabled(!treeSourceLocked());
		MesquiteTrunk.resetMenuItemEnabling();
		// resetLockImage();
		checkPanelPositionsLegal();
		resetBaseExplanation();
	}

	public void numTreesChanged() {
		if (taxa != null && taxa.isDoomed()) {
			ownerModule.iQuit();
			return;
		}
		palette.paletteScroll.setMinimumValue(MesquiteTree.toExternal(0));
		int numTrees = treeSourceTask.getNumberOfTrees(taxa);
		palette.paletteScroll.setMaximumValue(MesquiteTree.toExternal(numTrees - 1));
		if (currentTreeNumber >= numTrees && MesquiteInteger.isCombinable(numTrees)) {
			currentTreeNumber = numTrees - 1;
			goToTreeNumber(currentTreeNumber, true);
		}
		checkPanelPositionsLegal();
		resetBaseExplanation();
	}

	/* ................................................................................................................. */
	public void addAssistant(TreeDisplayAssistant tda) {
		tda.setEmployeesInStartup(true); // normally used only within EmployerEmployee, this helps assistants know they are still in startup phase
		treeDrawCoordTask.addAssistantTask(tda);

		TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplay);

		if (tce == null)
			return;
		if (tree != null)
			tce.setTree(tree);
		treeDisplay.addExtra(tce);
		checkPanelPositionsLegal();
		treeDisplay.pleaseUpdate(false);
		if (getMode() > 0)
			updateTextPage();
		tda.setEmployeesInStartup(false);
	}

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.incorporate(super.getSnapshot(file), false);

		if (MesquiteTrunk.snapshotMode != Snapshot.SNAPDISPLAYONLY) {
			temp.addLine("getTreeDrawCoordinator", treeDrawCoordTask);
			temp.addLine("setTreeNumber " + (MesquiteTree.toExternal(currentTreeNumber)));
			if ((treeEdited || windowModule.editMode) && tree != null) {
				temp.addLine("setTree " + StringUtil.tokenize(tree.writeTree(Tree.BY_NUMBERS, false)));
				if (!StringUtil.blank(tree.getAnnotation()))
					temp.addLine("setTreeAnnotation " + StringUtil.tokenize(tree.getAnnotation()));

				if (file != null && BasicTreeWindowMaker.warnUnsaved) {
					BasicTreeWindowMaker.warnUnsaved = false;
					ownerModule.storePreferences();
					String s = "The tree in a tree window has been edited, and is a temporary tree associated with the window.  ";
					s += "The fact that it is a temporary tree is indicated by the tree window\'s bar toward the bottom that indicates the tree name with a black diamond.  ";
					s += "If you haven\'t stored it in the file using Store Tree from the Tree menu, then this temporary tree won't be stored in a public part of the file, ";
					s += "and other programs won't be able to see the tree.\nIf you want other programs to see a temporary tree in a tree window, use Store Tree then resave the file.";
					ownerModule.alert(s);
				}
			}
		}
		if (drawingSizeMode != FIXEDSIZE) {
			temp.addLine("setDrawingSizeMode " + drawingSizeMode);
			temp.addLine("toggleLegendFloat " + floatLegends.toOffOnString());
		}
		else if (drawingSizeMode == FIXEDSIZE) {
			temp.addLine("setDrawingSizeMode " + drawingSizeMode + " " + totalTreeFieldWidth + " " + totalTreeFieldHeight + "  " + getOriginX() + " " + getOriginY());
			temp.addLine("toggleLegendFloat " + floatLegends.toOffOnString());
		}
		if (drawingSizeMode == AUTOSIZE) {
			temp.addLine("scale " + scale);
		}

		temp.addLine("toggleTextOnTree " + textVersionDrawOnTree.toOffOnString());
		if (treeInfoPanelEverShown) {
			if (treeInfoPanel != null) {
				temp.addLine("getInfoPanel");
				temp.addLine("tell It");
				temp.incorporate(treeInfoPanel.getSnapshot(file), true);
				temp.addLine("endTell");
			}
			temp.addLine("toggleInfoPanel " + infoPanelOn.toOffOnString());
		}
		temp.addLine("togglePrintName " + windowModule.printNameOnTree.toOffOnString());
		temp.addLine("showWindow");
		return temp;
	}

	/** Returns menu location for item to bring the window to the for (0 = custom or don't show; 1 = system area of Windows menu; 2 = after system area of Windows menu) */
	public int getShowMenuLocation() {
		return 0;
	}

	/* ................................................................................................................. */
	public String getPrintMenuItem() {
		return "Print Tree...";
	}

	/* ................................................................................................................. */
	public String getPrintToFitMenuItemName() {
		return "Print Tree To Fit Page...";
	}

	/* ................................................................................................................. */
	/**
	 * @author Peter Midford
	 */
	public void windowToPDF(MesquitePDFFile pdfFile, int fitToPage) {
		if (pdfFile != null) {
			if (infoBar.getMode() > 0)
				super.windowToPDF(pdfFile, fitToPage);
			else {
				Dimension oldTreeDisplaySize = treeDisplay.getSize();
				treeDisplay.setPrintingInProcess(true);
				treeDisplay.setSize(treeDisplay.getFieldWidth(), treeDisplay.getFieldHeight());
				Rectangle r = treeDisplay.getVisRect();
				treeDisplay.setVisRect(null);

				Graphics g2 = pdfFile.getPDFGraphicsForComponent(treeDisplay, null);
				treeDisplay.printAll(g2);
				treeDisplay.setSize(oldTreeDisplaySize.width, oldTreeDisplaySize.height);
				treeDisplay.setVisRect(r);
				treeDisplay.setPrintingInProcess(false);
				treeDisplay.repaintAll();
				pdfFile.end();
			}
		}
	}

	/* ................................................................................................................. */
	/**
	 * @author Peter Midford
	 */
	public String getPrintToPDFMenuItemName() {
		return "Save Tree as PDF...";
	}

	protected void pdfWindow(int fitToPage) {
		super.pdfWindow(fitToPage);
	}

	/* ................................................................................................................. */
	public void printWindow(MesquitePrintJob pjob) {
		if (pjob != null) {
			if (infoBar.getMode() > 0)
				super.printWindow(pjob);
			else
				pjob.printComponent(treeDisplay, null, currentFont);
		}
	}

	/* ................................................................................................................. */
	public void setCurrentTool(MesquiteTool tool) {
		if (tool != null && !tool.getEnabled())
			return;
		if (tool instanceof TreeTool)
			currentTreeTool = (TreeTool) tool;
		super.setCurrentTool(tool);
	}

	/* ............................................................................................................... */
	protected void setContentsCursor(Cursor c) {
		if (c == null)
			MesquiteMessage.printStackTrace("Error: cursor of tree window null");
		else if (treeDisplay != null)
			treeDisplay.setCursor(c);
	}

	/* ................................................................................................................. */
	MesquiteInteger pos = new MesquiteInteger();

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires a tree display assistant module", "[name of assistant module]", commandName, "newAssistant")) {
			ownerModule.incrementMenuResetSuppression();
			TreeDisplayAssistant tda = (TreeDisplayAssistant) ownerModule.hireNamedEmployee(TreeDisplayAssistant.class, arguments);
			if (tda != null) {
				addAssistant(tda);
				if (!MesquiteThread.isScripting())
					ownerModule.resetContainingMenuBar();
			}
			ownerModule.decrementMenuResetSuppression();
			return tda;
		}
		else if (checker.compare(this.getClass(), "Returns the tree info panel", null, commandName, "getInfoPanel")) {
			return treeInfoPanel;
		}
		else if (checker.compare(this.getClass(), "Returns analyses at nodes as a table", null, commandName, "getAsTable")) {
			if (treeDisplay == null)
				return null;
			String s = treeDisplay.getTableVersion();
			return s;
		}
		else if (checker.compare(this.getClass(), "Shows analyses at nodes as a table", null, commandName, "showAsTable")) {
			if (treeDisplay == null)
				return null;
			String s = treeDisplay.getTableVersion();
			ownerModule.logln(s);
		}
		else if (checker.compare(this.getClass(), "Shows taxon", "[id of taxa block][number of taxon]", commandName, "showTaxon")) {
			pos.setValue(0);
			long whichTaxaBlock = MesquiteInteger.fromString(arguments, pos);
			if (whichTaxaBlock != taxa.getID())
				return null;
			int which = MesquiteInteger.fromString(arguments, pos);
			if (which >= 0 && which < taxa.getNumTaxa()) {
				makeTaxonVisible(which);
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Selects taxon", "[number of taxon]", commandName, "selectTaxon")) {
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			if (which >= 0 && which < taxa.getNumTaxa()) {
				taxa.setSelected(which, !taxa.getSelected(which));
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				makeTaxonVisible(which);
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Puts legends in default position", null, commandName, "legendsToHome")) {
			Component[] cc = treeDisplay.getComponents();
			if (cc != null && cc.length > 0)
				for (int i = 0; i < cc.length; i++) {
					if (cc[i] instanceof Legend) {
						Legend legend = (Legend) cc[i];
						legend.setOffsetX(0);
						legend.setOffsetY(0);
						legend.adjustLocation();
					}
				}

			return null;
		}
		else if (checker.compare(this.getClass(), "Returns the tree draw coordinating module", null, commandName, "getTreeDrawCoordinator")) {
			return treeDrawCoordTask;
		}
		else if (checker.compare(this.getClass(), "Forces a repaint", null, commandName, "forceRepaint")) {
			treeDisplay.redoCalculations(33);
			treeDisplay.forceRepaint();
		}
		else if (checker.compare(this.getClass(), "Hires a tree window assistant module", "[name of assistant module]", commandName, "newWindowAssistant")) {
			TreeWindowAssistant tda = (TreeWindowAssistant) ownerModule.hireNamedEmployee(TreeWindowAssistant.class, arguments);
			if (tda != null && tree != null)
				tda.setTree(tree);
			return tda;
		}
		else if (checker.compare(this.getClass(), "Returns the tree", null, commandName, "getTree")) {
			return tree;
		}
		else if (checker.compare(this.getClass(), "Brings the graphics of an assistant module to the front", "[number of assistant module among TreeDisplayDrawnExtra owners]", commandName, "bringToFront")) {
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(which))
				return null;
			ListableVector ev = treeDisplay.getExtras();
			int count = 0;
			for (int i = 0; i < ev.size(); i++) {
				TreeDisplayExtra extra = (TreeDisplayExtra) ev.elementAt(i);
				if (extra != null && extra instanceof TreeDisplayDrawnExtra) {
					if (count == which) {
						TreeDisplayExtra found = extra;
						ownerModule.moveEmployeeToFront(extra.getOwnerModule());
						treeDisplay.moveExtraToFront(extra);
						ownerModule.resetContainingMenuBar();
						return null;
					}
					count++;
				}
			}
		}
		else if (checker.compare(this.getClass(), "Closes an assistant module", "[number of assistant module]", commandName, "closeDisplayAssistant")) {
			EmployeeVector ev = ownerModule.getEmployeeVector();
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(which))
				return null;
			int count = 0;
			for (int i = 0; i < ev.size(); i++) {
				MesquiteModule mb = (MesquiteModule) ev.elementAt(i);
				if (mb != null && mb instanceof TreeDisplayAssistantAD) {
					if (count == which) {
						ownerModule.fireEmployee(mb);
						return null;
					}
					count++;
				}
			}
		}
		else if (checker.compare(this.getClass(), "Saves a macro to redo the analysis of an assistant module", "[number of assistant module]", commandName, "saveMacroDisplayAssistant")) {
			EmployeeVector ev = ownerModule.getEmployeeVector();
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(which))
				return null;
			int count = 0;
			for (int i = 0; i < ev.size(); i++) {
				MesquiteModule mb = (MesquiteModule) ev.elementAt(i);
				if (mb != null && mb instanceof TreeDisplayAssistantAD) {
					if (count == which) {
						String recipe = "newAssistant #" + mb.getClass().getName() + ";" + StringUtil.lineEnding() + "tell It;" + StringUtil.lineEnding();
						recipe += Snapshot.getSnapshotCommands(mb, null, "");
						recipe += "endTell;" + StringUtil.lineEnding();
						MesquiteMacro.saveMacro(ownerModule, "Macro to start " + mb.getNameForMenuItem(), 0, recipe);
						return null;
					}
					count++;
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the tree as having been edited (e.g. so that it can be treated as \"untitled\")", null, commandName, "treeEdited")) {
			treeEdited(true);
			treeChanged(true);
			setTreeName(tree);
			setExplanation(baseExplanation, true);
			if (getMode() > 0)
				updateTextPage();
		}
		/*
		 * else if (checker.compare(this.getClass(), "Sets the tree to that described by the string passed, and presented in a standard rotation.", null, commandName, "setStandardizedTree")) { Tree t = setTree(ParseUtil.getFirstToken(arguments, pos)); if (t instanceof MesquiteTree) ((MesquiteTree)t).standardize(t.getRoot(),false); if (t!=null){ treeEdited(true); setTreeName(); return t; } }
		 */
		else if (checker.compare(this.getClass(), "Sets the tree to that described by the string passed", "[Parenthesis notation string of tree]", commandName, "queryEditedTreeMode")) {
			queryEditedTreeRetentionPreference();
		}
		else if (checker.compare(this.getClass(), "Sets the tree to that described by the string passed", "[Parenthesis notation string of tree]", commandName, "setTree")) {
			String descr = ParseUtil.getFirstToken(arguments, pos);

			Tree t = setTree(descr); // process the tree fully, including [%color = 4]
			if (t != null) {
				treeEdited(true);
				setTreeName(t);
				return t;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the tree to a recently edited tree", "[number of edited tree]", commandName, "showRecentEdited")) {
			int r = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(r) && r < recentEditedTrees.size()) {
				MesquiteBoolean editStatusToSet = new MesquiteBoolean();
				Tree t = setCloneOfTree(recentEditedTrees.getTree(r), false, editStatusToSet);
				treeEdited(editStatusToSet.getValue());
				setTreeName(t);
			}
		}
		else if (checker.compare(this.getClass(), "Recovers the last edited tree", null, commandName, "recoverLastEditedTree")) {
			if (previousEditedTree != null) {
				MesquiteBoolean editStatusToSet = new MesquiteBoolean();
				Tree t = setCloneOfTree(previousEditedTree, false, editStatusToSet);
				treeEdited(editStatusToSet.getValue());
				setTreeName(t);
			}
		}

		else if (checker.compare(this.getClass(), "Shows the window", null, commandName, "showWindow")) {
			sizeDisplay();
			setVisible(true);
			// if (useSuggestedSize.getValue())
			// setSuggestedSize(false, true);

		}
		else if (checker.compare(this.getClass(), "Sets the annotation for the current tree", "[Annotation for tree in window]", commandName, "setTreeAnnotation")) {
			String note = ParseUtil.getFirstToken(arguments, pos);
			if (tree != null && treeEdited) {
				tree.setAnnotation(note, true);
				showTreeAnnotation();
			}

		}
		else if (checker.compare(this.getClass(), "Returns the number of trees", null, commandName, "getNumTrees")) {
			return new MesquiteInteger(treeSourceTask.getNumberOfTrees(taxa));
		}
		else if (checker.compare(this.getClass(), "Returns the current tree number", null, commandName, "getTreeNumber")) {
			return new MesquiteInteger(currentTreeNumber);
		}
		else if (checker.compare(this.getClass(), "Sets the tree to be the i'th one from the current tree source", "[number of tree to be shown]", commandName, "setTreeNumber")) {
			return goToTreeNumber(MesquiteTree.toInternal(MesquiteInteger.fromFirstToken(arguments, pos)), true);
		}
		else if (checker.compare(this.getClass(), "Present a dialog box to choose a tree from the current tree source", null, commandName, "chooseTree")) {
			int ic = treeSourceTask.queryUserChoose(taxa, "for tree window");
			if (MesquiteInteger.isCombinable(ic)) {
				return goToTreeNumber(ic, true);
			}

		}
		else if (checker.compare(this.getClass(), "Stores the current tree as a new stored tree in a tree block", null, commandName, "storeTreeAs") || checker
				.compare(this.getClass(), "Stores the current tree as a new stored tree in another tree block", null, commandName, "storeTreeAsOther")) {
			boolean inOther = commandName.equalsIgnoreCase("storeTreeAsOther");
			MesquiteTree tree = treeDisplay.getTree().cloneTree();

			// if treeSourceTask is Stored Trees, and tree is saved into same block as being shown, then should change current tree to that one and jump to there???

			String s = ParseUtil.getFirstToken(arguments, new MesquiteInteger(0));

			if (StringUtil.blank(s))
				s = MesquiteString.queryString(this, "Store Tree As", "Name of tree: ", tree.getName());
			if (s != null) {
				tree.setName(s);
				TreeVector trees = null;
				int numTreeBlocks = ownerModule.getProject().getNumberTreeVectors();
				if (!inOther && originalTree != null && ((MesquiteTree) originalTree).getTreeVector() != null) {
					trees = ownerModule.getProject().storeTree(this, ((MesquiteTree) originalTree).getTreeVector(), tree, true);
				}
				else
					trees = ownerModule.getProject().storeTree(this, null, tree, true);
				int numTreeBlocksAfter = ownerModule.getProject().getNumberTreeVectors();
				if (numTreeBlocksAfter>numTreeBlocks)
					MesquiteModule.resetAllMenuBars();
				if (trees != null) {
					TreesManager manager = (TreesManager) ownerModule.findElementManager(TreeVector.class);
					if (manager != null) {
						int listNum = manager.getTreeBlockNumber(trees);
						if (showTreeListOnSave)
							manager.doCommand("showTrees", Integer.toString(listNum), checker);
						showTreeListOnSave = false;

						if (treeSourceTask != null && "Stored Trees".equalsIgnoreCase(treeSourceTask.getName())) {
							boolean oldSuppress = ((BasicTreeWindowMaker) ownerModule).suppressEPCResponse;
							((BasicTreeWindowMaker) ownerModule).suppressEPCResponse = true;
							TreeVector v = (TreeVector) treeSourceTask.doCommand("setTreeBlockByID", "" + trees.getID(), CommandChecker.defaultChecker);
							((BasicTreeWindowMaker) ownerModule).suppressEPCResponse = oldSuppress;
							if (v == trees) {
								resetForTreeSource(false, false, false, MesquiteInteger.inapplicable);
								goToTreeNumber(trees.size() - 1, true);
							}
							if (windowModule.editMode) {
								treeSourceTask.doCommand("laxOff", null, checker);
								windowModule.editMode = false;
								editedByHand = false;
							}
							resetBaseExplanation();
							resetTitle();
						}
						setStoreTreeAsMenuItems(windowModule.editMode);
						numTreesChanged();
						return trees;
					}
				}

			}
		}
		else if (checker.compare(this.getClass(), "Stores the current tree back into the original stored tree from which it came", null, commandName, "storeTree")) {
			// Tree tree = basicTreeWindow.tree; //should get this straight from tree window!!!!!!!
			if (treeSourceLocked())
				return null;
			if (originalTree != null && treeEdited) {
				((MesquiteTree) originalTree).setToClone(tree);
				if (originalTree != null)
					((MesquiteTree) originalTree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
				if (originalTree != null && ((MesquiteTree) originalTree).getTreeVector() != null)
					((MesquiteTree) originalTree).getTreeVector().notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
				treeEdited = false;
				zapPreviousEdited(true);
				editedByHand = false;
				messagePanel.setHighlighted(false);
				if (treeInfoPanel != null)
					treeInfoPanel.setHighlighted(!treeSourceLocked());
				resetBaseExplanation();
				resetTitle();
			}
			return originalTree;
		}
		else if (checker.compare(this.getClass(), "Gets tree vector being shown.", null, commandName, "getTreeVector")) {
			if (originalTree != null && ((MesquiteTree) originalTree).getTreeVector() != null)
				return ((MesquiteTree) originalTree).getTreeVector();

		}
		else if (checker.compare(this.getClass(), "Goes to the next tree in the tree source.  THIS RUNS ON GUI THREAD.", null, commandName, "nextTree")) {
			palette.paletteScroll.increment();
		}
		else if (checker.compare(this.getClass(), "Goes to the previous tree in the tree source.  THIS RUNS ON GUI THREAD.", null, commandName, "previousTree")) {
			palette.paletteScroll.decrement();
		}
		else if (checker.compare(this.getClass(), "Steps through the trees.", null, commandName, "stepThroughTrees")) {
			stepThroughTrees();
		}
		else if (checker.compare(this.getClass(), "Goes to the next tree in the tree source.", null, commandName, "goToNextTree")) {
			incrementTreeNumber();
		}
		else if (checker.compare(this.getClass(), "Undoes the previous tree change", null, commandName, "undo")) {
			if (undoTree != null && canUndo && undoTree.upToDateWithTaxa()) {
				MesquiteBoolean editStatusToSet = new MesquiteBoolean();
				setCloneOfTree(undoTree, false, editStatusToSet);
				treeEdited(editStatusToSet.getValue());
				return tree;
			}
		}
		else if (checker.compare(this.getClass(), "Copies a description of the tree", null, commandName, "copyTree")) {
			if (tree != null) {
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(tree.writeTree(Tree.BY_NAMES, false));
				clip.setContents(ss, ss);
			}
		}
		else if (checker.compare(this.getClass(), "Pastes a description of the tree", null, commandName, "Paste")) {
			if (tree != null) {
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable t = clip.getContents(this);
				try {
					String s = (String) t.getTransferData(DataFlavor.stringFlavor);
					Tree tr = setTree(s);

					if (tr != null) {
						treeEdited(true);
						return tr;
					}

				} catch (Exception e) {
				}
			}
		}
		else if (checker.compare(this.getClass(), "Hires a module to alter or transform branch lengths", "[name of module]", commandName, "alterBranchLengths")) {
			BranchLengthsAlterer ble = (BranchLengthsAlterer) ownerModule.hireNamedEmployee(BranchLengthsAlterer.class, arguments);
			if (ble != null) {
				boolean success = ble.transformTree(tree, null, true);
				if (success) {
					if (treeSourceLocked())
						tree.setName("Untitled Tree");
					treeEdited(false);
				}
				ownerModule.fireEmployee(ble); // todo: for branch length estimators, might be good to keep it around, and remembering it if user wants to change parameters
			}
		}
		else if (checker.compare(this.getClass(), "Hires a module to alter the tree", "[name of module]", commandName, "alterTree")) {
			TreeAlterer ble = (TreeAlterer) ownerModule.hireNamedEmployee(TreeAlterer.class, arguments);
			if (ble != null) {
				boolean success = ble.transformTree(tree, null, true);
				if (success) {
					if (treeSourceLocked())
						tree.setName("Untitled Tree");
					treeEdited(false);
					treeDisplay.pleaseUpdate(true);
				}
				ownerModule.fireEmployee(ble);
			}
		}
		else if (checker.compare(this.getClass(), "Toggles whether the info panel is on", null, commandName, "toggleInfoPanel")) {
			infoPanelOn.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			setTreeInfoPanel(infoPanelOn.getValue());
		}
		else if (checker.compare(this.getClass(), "Toggles whether to add the name of the tree when printing", null, commandName, "togglePrintName")) {
			windowModule.printNameOnTree.toggleValue(ParseUtil.getFirstToken(arguments, pos));
		}
		else if (checker.compare(this.getClass(), "(For old scripts only) Requests that the tree is drawn to a default (suggested) size", null, commandName, "useSuggestedSize")) {
			MesquiteBoolean useSuggestedSize = new MesquiteBoolean(drawingSizeMode == AUTOSIZE);
			useSuggestedSize.toggleValue(ParseUtil.getFirstToken(arguments, pos));

			if (useSuggestedSize.getValue()) {
				drawingSizeMode = AUTOSIZE;
			}
			else
				drawingSizeMode = SCALETOFIT;

			sizeModeName.setValue(sizeModes.getValue(drawingSizeMode)); // so that menu item knows to become checked
			if (!MesquiteThread.isScripting()) {
				sizeDisplay();
				treeDisplay.pleaseUpdate(false);
			}
		}
		else if (checker.compare(this.getClass(), "(For old scripts only) Sets whether or not the tree is drawn so as to fit within the window, or so as to fit within a scrollable pane", "[on or off to indicate whether constrained to window]", commandName, "toggleSizeToFit")) {
			MesquiteBoolean sizeToFit = new MesquiteBoolean(drawingSizeMode == SCALETOFIT);

			pos.setValue(0);
			sizeToFit.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			if (sizeToFit.getValue()) {
				drawingSizeMode = SCALETOFIT;
				toolMAG.setEnabled(false);
			}
			else {
				drawingSizeMode = AUTOSIZE;
				toolMAG.setEnabled(true);
			}
			sizeModeName.setValue(sizeModes.getValue(drawingSizeMode)); // so that menu item knows to become checked
			if (!MesquiteThread.isScripting()) {
				sizeDisplay();
				treeDisplay.pleaseUpdate(false);
			}
		}
		else if (checker.compare(this.getClass(), "(For old scripts only) Sets the size of the drawing pane area (and implicitly sets to fixed size)", "[width in pixels of drawing area] [height in pixels of drawing area]", commandName, "sizeDrawing")) {
			pos.setValue(0);
			int w = MesquiteInteger.fromString(arguments, pos);
			int h = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(h) || MesquiteInteger.isCombinable(w)) {
				drawingSizeMode = FIXEDSIZE;
				sizeModeName.setValue(sizeModes.getValue(drawingSizeMode)); // so that menu item knows to become checked
				toolMAG.setEnabled(false);
				boolean resize = false;
				if (MesquiteInteger.isCombinable(h) && h > 10) {
					totalTreeFieldHeight = h;
					resize = true;
				}
				if (MesquiteInteger.isCombinable(w) && w > 10) {
					totalTreeFieldWidth = w;
					resize = true;
				}
				if (resize) {
					sizeDisplay();
				}
			}
		}
		else if (checker.compare(this.getClass(), "(For old scripts only) Sets the origin for the scrolling area", "[origin x] [origin y]", commandName, "setOrigin")) {
			pos.setValue(0);
			int horiz = MesquiteInteger.fromString(arguments, pos);
			int vert = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(horiz) && MesquiteInteger.isCombinable(vert)) {
				setOrigin(horiz, vert, true);
			}
		}
		else if (checker.compare(this.getClass(), "Sets zoom scale", "[log base 2 of scale]", commandName, "scale")) {
			pos.setValue(0);
			int s = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(s)) {
				scale = s;

			}
		}
		else if (checker.compare(this.getClass(), "Sets the drawing size mode", "[mode] [width in pixels of drawing area] [height in pixels of drawing area] [x origin] [y origin]", commandName, "setDrawingSizeMode")) {
			pos.setValue(0);
			int mode = MesquiteInteger.fromString(arguments, pos); // sets the mode

			if (!MesquiteInteger.isCombinable(mode)) { // || drawingSizeMode == mode
				Parser parser = new Parser();
				String name = parser.getFirstToken(arguments); // get argument passed of option chosen
				mode = sizeModes.indexOf(name); // see if the option is recognized by its name
				if (mode < 0) {
					sizeModeName.setValue(sizeModes.getValue(drawingSizeMode)); // so that menu item knows to become checked
					return null;
				}
			}
			drawingSizeMode = mode;
			sizeModeName.setValue(sizeModes.getValue(mode)); // so that menu item knows to become checked
			MesquiteTrunk.resetCheckMenuItems();
			toolMAG.setEnabled(drawingSizeMode == AUTOSIZE);
			if (drawingSizeMode != AUTOSIZE)
				scale = 0;
			if (drawingSizeMode == FIXEDSIZE) {
				int w = MesquiteInteger.fromString(arguments, pos);
				int h = MesquiteInteger.fromString(arguments, pos);
				if (MesquiteInteger.isCombinable(h) || MesquiteInteger.isCombinable(w)) {
					boolean resize = false;
					if (MesquiteInteger.isCombinable(h) && h > 10) {
						totalTreeFieldHeight = h;
						resize = true;
					}
					if (MesquiteInteger.isCombinable(w) && w > 10) {
						totalTreeFieldWidth = w;
						resize = true;
					}
					if (resize && !MesquiteThread.isScripting()) {
						sizeDisplay();
					}
					int horiz = MesquiteInteger.fromString(arguments, pos);
					int vert = MesquiteInteger.fromString(arguments, pos);
					if (MesquiteInteger.isCombinable(horiz) && MesquiteInteger.isCombinable(vert)) {
						setOrigin(horiz, vert, true);
					}
				}
				else {
					MesquiteBoolean answer = new MesquiteBoolean(false);
					MesquiteInteger newWidth = new MesquiteInteger(totalTreeFieldWidth);
					MesquiteInteger newHeight = new MesquiteInteger(totalTreeFieldHeight);
					MesquiteInteger.queryTwoIntegers(ownerModule.containerOfModule(), "Size of tree drawing", "Width (Pixels)", "Height (Pixels)", answer, newWidth, newHeight, 10, MesquiteInteger.unassigned, 10, MesquiteInteger.unassigned, "Enter the width and height of the tree drawing.  These values must be at least 10 pixels each.");
					if (answer.getValue() && newWidth.getValue() > 10 && newHeight.getValue() > 10) {
						totalTreeFieldWidth = newWidth.getValue();
						totalTreeFieldHeight = newHeight.getValue();
						if (!MesquiteThread.isScripting())
							sizeDisplay();
					}
				}
			}
			else if (!MesquiteThread.isScripting())
				sizeDisplay();
		}
		else if (checker.compare(this.getClass(), "When Size to Window is false, brings legends into view", null, commandName, "toggleLegendFloat")) {
			boolean current = floatLegends.getValue();
			pos.setValue(0);
			floatLegends.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			if (current != floatLegends.getValue())
				toggleLegendFloat();
			else
				checkPanelPositionsLegal();

		}
		else if (checker.compare(this.getClass(), "In Text version of window, controls whether extras like trace character show their information directly on a tree or as a list of nodes", null, commandName, "toggleTextOnTree")) {
			boolean current = textVersionDrawOnTree.getValue();
			pos.setValue(0);
			textVersionDrawOnTree.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			if (current != textVersionDrawOnTree.getValue()) {
				treeDisplay.textVersionDrawOnTree = textVersionDrawOnTree.getValue();
				if (getMode() > 0)
					updateTextPage();
			}

		}
		else if (checker.compare(this.getClass(), "Sets size of tree window", "[width in pixels of window] [height in pixels of window]", commandName, "setSize")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int width = MesquiteInteger.fromString(arguments, io);
			int height = MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(width) && MesquiteInteger.isCombinable(height)) {
				fromScriptCommand = true; // this is needed to counteract difficulties with popping in/out and size setting in window constructors

				setWindowSize(width, height);
				fromScriptCommand = false;
				if (!MesquiteThread.isScripting()) {
					sizeDisplay();
					treeDisplay.redoCalculations(355);
					treeDisplay.forceRepaint();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the current tool", "[name of tool]", commandName, "setTool")) {
			ToolPalette palette = getPalette();
			if (palette == null)
				return null;
			currentTreeTool = (TreeTool) palette.getToolWithName(arguments);
			setCurrentTool(currentTreeTool);
			setExplanation(currentTreeTool.getDescription());
		}
		else if (checker.compare(this.getClass(), "Root current tree along branch", "[branch number]", commandName, "rootAlongBranch")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int atBranch = MesquiteInteger.fromString(arguments, io);
			if (atBranch > 0 && tree.reroot(atBranch, tree.getRoot(), true)) {
				treeEdited(false);
				if (ladderizeAfterReroot.getValue())
					tree.standardize(tree.getRoot(), true, true);
			}

		}
		else if (checker.compare(this.getClass(), "Move one branch onto another", "[branch being moved] [branch onto which first will be attached]", commandName, "moveBranch")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int branchFrom = MesquiteInteger.fromString(arguments, io);
			int branchTo = MesquiteInteger.fromString(arguments, io);

			if (branchFrom > 0 && branchTo > 0){
				int fromMother = tree.motherOfNode(branchFrom);  //new in 3. 1: finally, allowing interchange
				int toMother = tree.motherOfNode(branchTo);
				if (fromMother == toMother && toMother>0 && (arguments.indexOf("option") >= 0 || !tree.nodeIsPolytomous(fromMother))){
					if (tree.interchangeBranches(branchFrom, branchTo, false, true))
						treeEdited(false);
				}
				else if (tree.moveBranch(branchFrom, branchTo, true))
					treeEdited(false);
			}

		}
		/*
		 * cut out as of 1.02; shifted to SelectTaxaInClade else if (checker.compare(this.getClass(), "Touch a taxon in the tree window", "[taxon touched] [branch onto which first will be attached]", commandName, "touchTaxon")) { MesquiteInteger io = new MesquiteInteger(0); int touched= MesquiteInteger.fromString(arguments, io); if (touched >=0 && MesquiteInteger.isCombinable(touched)) { if (arguments.indexOf("shift") >=0) { taxa.setSelected(touched, !taxa.getSelected(touched)); //reverse selection of touched taxon taxa.notifyListeners(this, new
		 * Notification(MesquiteListener.SELECTION_CHANGED)); } else { taxa.deselectAll(); taxa.setSelected(touched, true); taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED)); } }
		 * 
		 * 
		 * }
		 */
		else if (checker.compare(this.getClass(), "Exchange two branches", "[first branch number] [second branch number]", commandName, "exchangeBranches")) {
			pos.setValue(0);
			int branchFrom = MesquiteInteger.fromString(arguments, pos);
			int branchTo = MesquiteInteger.fromString(arguments, pos);
			boolean preserveHeights = (arguments.indexOf("option") >= 0);
			if (branchFrom > 0 && branchTo > 0 && (tree.interchangeBranches(branchFrom, branchTo, preserveHeights, true)))
				treeEdited(false);

		}
		else if (checker.compare(this.getClass(), "Magnify clade descendant from node", "[node number]", commandName, "magnifyClade")) {
			int branchFound = MesquiteInteger.fromFirstToken(arguments, pos);

			if (branchFound > 0 && tree.nodeIsInternal(branchFound)) {
				if (branchFound == treeDisplay.getTreeDrawing().getDrawnRoot())
					treeDisplay.getTreeDrawing().setDrawnRoot(-1);
				else
					treeDisplay.getTreeDrawing().setDrawnRoot(branchFound);
				treeDisplay.pleaseUpdate(true);
				messagePanel.repaint();
			}
		}
		else if (checker.compare(this.getClass(), "Zoom image", "[branch][x][y]", commandName, "zoomBT")) {
			int branchFound = MesquiteInteger.fromFirstToken(arguments, pos);
			int x = MesquiteInteger.fromString(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			boolean zoomIn = (arguments.indexOf("option") < 0);
			zoom(x, y, zoomIn);
		}
		else if (checker.compare(this.getClass(), "Zoom image", "[x][y]", commandName, "zoom")) {
			int x = MesquiteInteger.fromFirstToken(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			boolean zoomIn = (arguments.indexOf("option") < 0);
			zoom(x, y, zoomIn);
		}
		else if (checker.compare(this.getClass(), "Resets zoom scale to normal", null, commandName, "zoomReset")) {
			scale = 0;
			setOrigin(0, 0, true);
			sizeDisplay();
		}
		else if (checker.compare(this.getClass(), "Toggles whether or not to ladderize after rerooting", null, commandName, "toggleRerootLadderize")) {
			pos.setValue(0);
			ladderizeAfterReroot.toggleValue(ParseUtil.getFirstToken(arguments, pos));
		}
		else if (checker.compare(this.getClass(), "Show popup for options for zoom", "[x][y]", commandName, "zoomOptions")) {
			MesquiteButton button = toolMAG.getButton();
			if (button != null) {
				MesquiteInteger io = new MesquiteInteger(0);
				int x = MesquiteInteger.fromFirstToken(arguments, pos);
				int y = MesquiteInteger.fromString(arguments, pos);
				MesquitePopup popup = new MesquitePopup(button);
				MesquiteMenuItem mItem = new MesquiteMenuItem("Reset Scale to Normal", ownerModule, new MesquiteCommand("zoomReset", this));
				popup.add(mItem);
				popup.showPopup(x, y + 6);
			}

		}
		else if (checker.compare(this.getClass(), "Show popup for options for reroot", "[x][y]", commandName, "rerootOptions")) {
			MesquiteButton button = rerootTool.getButton();
			if (button != null) {
				MesquiteInteger io = new MesquiteInteger(0);
				int x = MesquiteInteger.fromFirstToken(arguments, pos);
				int y = MesquiteInteger.fromString(arguments, pos);
				MesquitePopup popup = new MesquitePopup(button);
				popup.add(toggleRerootLadderizeMenuItem);
				popup.showPopup(x, y + 6);
			}

		}
		else if (checker.compare(this.getClass(), "Ladderizes the clade", "[branch number]", commandName, "ladderize")) {
			Parser parser = new Parser();
			String s = parser.getFirstToken(arguments);
			int branchFound = MesquiteInteger.fromString(s);
			if (s.equalsIgnoreCase("root"))
				branchFound = tree.getRoot();
			else
				branchFound = MesquiteInteger.fromString(s);
			if (branchFound > 0) {
				boolean direction = true;
				if (arguments.indexOf("option") >= 0)
					direction = false;
				if (tree.standardize(branchFound, direction, true)) {
					treeEdited(false);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Ladderizes the clade with focal taxon at left or right", "[taoxn number]", commandName, "focalLadderize")) {
			int taxonFound = MesquiteInteger.fromFirstToken(arguments, pos);
			if (taxonFound >= 0) {
				boolean direction = true;
				if (arguments.indexOf("option") >= 0)
					direction = false;
				if (tree.focalStandardize(taxonFound, direction, true)) {
					treeEdited(false);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Collapse branch to yield polytomy", "[branch number]", commandName, "collapseBranch")) {
			int branchFound = MesquiteInteger.fromFirstToken(arguments, pos);
			if (branchFound > 0 && (tree.collapseBranch(branchFound, true)))
				treeEdited(false);
		}
		else if (checker.compare(this.getClass(), "Collapse all internal branches in clade descendant from node", "[node number]", commandName, "collapseAll")) {
			int branchFound = MesquiteInteger.fromFirstToken(arguments, pos);
			if (branchFound > 0) {
				boolean below = false;
				if (arguments.indexOf("option") >= 0)
					below = true;
				if (tree.collapseAllBranches(branchFound, below, true))
					treeEdited(false);
			}
		}
		else if (checker.compare(this.getClass(), "Draw clade descendant from node compactly as a triangle", "[node number]", commandName, "drawAsTriangle")) {
			int branchFound = MesquiteInteger.fromFirstToken(arguments, pos);
			boolean isRoot = tree.getRoot() == branchFound;
			if (branchFound > 0 && tree.nodeIsInternal(branchFound)) {
				NameReference triangleNameRef = NameReference.getNameReference("triangled");
				if (isRoot) {
					if (tree.getWhichAssociatedBits(triangleNameRef) != null && tree.getAssociatedBit(triangleNameRef, branchFound)) {
						tree.setAssociatedBit(triangleNameRef, branchFound, false);
						treeDisplay.pleaseUpdate(true);
					}
				}
				else {
					if (tree.getWhichAssociatedBits(triangleNameRef) == null)
						tree.makeAssociatedBits("triangled");

					tree.setAssociatedBit(triangleNameRef, branchFound, !(tree.getAssociatedBit(triangleNameRef, branchFound)));
					treeDisplay.pleaseUpdate(true);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Cut clade descendant from node", "[node number]", commandName, "cutClade")) {
			int branchFound = MesquiteInteger.fromFirstToken(arguments, pos);
			if (branchFound > 0) {
				if (tree.deleteClade(branchFound, true)) {
					treeEdited(false);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Cuts selected taxa from the tree", null, commandName, "cutSelectedTaxa")) {
			boolean changed = false;
			for (int i = 0; i < taxa.getNumTaxa(); i++) {
				int node = tree.nodeOfTaxonNumber(i);
				if (tree.nodeExists(node) && taxa.getSelected(i)) {
					tree.deleteClade(node, false);
					changed = true;
				}
			}
			if (changed) {
				tree.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
				treeEdited(false);
			}
		}
		else if (checker.compare(this.getClass(), "Shows the list of taxa", null, commandName, "showTaxaList")) {
			tree.getTaxa().showMe();
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	void stepThroughTrees() {
		int numW = numberWritable();
		if (numW == 0) {
			ownerModule
			.discreetAlert("Step Through Trees summarizes calculations over a series of trees, but you currently have no calculations active that can be summarized.  \"Values\" in the tree info panel, \"Values for Current Tree\", \"Trace Character History\" and some other calculations can be summarized. (Not all calculations have been enabled for summarizing.)");
			return;
		}
		if (treeSourceTask == null)
			return;
		// if tree is edited, save it
		String returnTree = null;
		int iCT = currentTreeNumber;
		if (treeEdited && tree != null) {
			returnTree = tree.writeTree(Tree.BY_NUMBERS, false);
		}
		Listable[] list = null;
		// ask to choose results if multiple writables; if only 1 explain that trees will be cycled.
		if (numW > 1) {
			ListableVector lv = new ListableVector();
			for (int i = 0; i < ownerModule.getNumberOfEmployees(); i++) {
				Object e = ownerModule.getEmployeeVector().elementAt(i);
				if (e instanceof TreeWDIAssistant) {
					TreeWDIAssistant ta = (TreeWDIAssistant) e;
					if (ta.suppliesWritableResults()) {
						ObjectContainer oc = new ObjectContainer();
						oc.setObject(ta);
						oc.setName(ta.nameForWritableResults());
						lv.addElement(oc, false);

					}
				}
			}

			list = ListDialog.queryListMultiple(this, "Whose results to save?", "You have asked to step through the trees, storing the results of calculations in a text file.  Whose results should be saved in this way?  Choose one or more.", null, lv, null);

		}
		else {
			list = new Listable[1];
			for (int i = 0; i < ownerModule.getNumberOfEmployees(); i++) {
				Object e = ownerModule.getEmployeeVector().elementAt(i);
				if (e instanceof TreeWDIAssistant) {
					TreeWDIAssistant ta = (TreeWDIAssistant) e;
					if (ta.suppliesWritableResults()) {
						ObjectContainer oc = new ObjectContainer();
						oc.setObject(ta);
						list[0] = oc;
						break;

					}
				}
			}
			ownerModule.discreetAlert("You have asked to step through the trees, storing the results of calculations in a text file.  The results that will be stored are from " + list[0].getName());
		}
		if (list == null)
			return;
		int numTrees = treeSourceTask.getNumberOfTrees(taxa);
		// if infinite number of trees, ask how many
		if (MesquiteInteger.isInfinite(numTrees)) {
			numTrees = MesquiteInteger.queryInteger(this, "How many trees?", "The tree source for this tree window has unlimited trees.  How many trees do you want to step through?", 100);
			if (!MesquiteInteger.isCombinable(numTrees))
				return;
		}
		// choose output file
		MesquiteFileDialog fdlg = new MesquiteFileDialog(this, "Output File for Step Through Trees", FileDialog.SAVE);
		fdlg.setBackground(ColorTheme.getInterfaceBackground());
		fdlg.setVisible(true);
		String fileName = fdlg.getFile();
		String directory = fdlg.getDirectory();
		// fdlg.dispose();
		if (StringUtil.blank(fileName) || StringUtil.blank(directory))
			return;
		MesquiteFile.putFileContents(MesquiteFile.composePath(directory, fileName), "Output from Step Through Trees" + StringUtil.lineEnding(), false);

		// write heading
		for (int i = 0; i < list.length; i++) {
			ObjectContainer econtainer = (ObjectContainer) list[i];

			Object e = econtainer.getObject();
			if (e instanceof TreeWDIAssistant) {
				TreeWDIAssistant ta = (TreeWDIAssistant) e;
				if (ta.suppliesWritableResults()) {
					Object tH = ta.getResultsHeading();
					String s = "";
					if (tH != null) {
						if (tH instanceof String)
							s = (String) tH;
						else if (tH instanceof MesquiteNumber) {
							MesquiteNumber mn = (MesquiteNumber) tH;
							s = mn.toStringNames();
						}
						else
							s = tH.toString();
					}
					MesquiteFile.appendFileContents(MesquiteFile.composePath(directory, fileName), "\t" + s, false);

				}
			}
		}
		MesquiteFile.appendFileContents(MesquiteFile.composePath(directory, fileName), StringUtil.lineEnding(), false);
		for (int iTree = 0; iTree < numTrees; iTree++) {
			goToTreeNumber(iTree, false);
			MesquiteFile.appendFileContents(MesquiteFile.composePath(directory, fileName), "Tree " + (iTree + 1), false);
			for (int i = 0; i < list.length; i++) {
				ObjectContainer econtainer = (ObjectContainer) list[i];

				Object e = econtainer.getObject();
				if (e instanceof TreeWDIAssistant) {
					TreeWDIAssistant ta = (TreeWDIAssistant) e;
					if (ta.suppliesWritableResults()) {
						Object tO = ta.getWritableResults();
						String s = "";
						if (tO != null) {
							if (tO instanceof String)
								s = (String) tO;
							else if (tO instanceof MesquiteNumber) {
								MesquiteNumber mn = (MesquiteNumber) tO;
								s = mn.toStringWithDetails();
							}
							else if (tO instanceof MesquiteNumber[]) {
								MesquiteNumber[] mn = (MesquiteNumber[]) tO;
								for (int k = 0; k < mn.length; k++) {
									if (k != 0)
										s += "\t";
									s += mn[k].toStringWithDetails();
								}
							}
							else
								s = tO.toString();
						}
						MesquiteFile.appendFileContents(MesquiteFile.composePath(directory, fileName), "\t" + s, false);

					}
				}
			}
			MesquiteFile.appendFileContents(MesquiteFile.composePath(directory, fileName), StringUtil.lineEnding(), false);
			if (!MesquiteInteger.isCombinable(numTrees))
				numTrees = treeSourceTask.getNumberOfTrees(taxa);
		}
		// return tree to former state
		goToTreeNumber(iCT, false);
		if (returnTree != null) {
			setTree(returnTree);
			treeEdited(false);
			treeChanged(true);
		}
		treeDisplay.redoCalculations(6519);
		treeDisplay.repaint();

	}

	int numberWritable() {
		int count = 0;
		for (int i = 0; i < ownerModule.getNumberOfEmployees(); i++) {
			Object e = ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof TreeWDIAssistant) {
				TreeWDIAssistant ta = (TreeWDIAssistant) e;
				if (ta.suppliesWritableResults()) {
					count++;
				}
			}
		}
		return count;
	}

	/* ................................................................................................................. */
	int scale = 0;

	void zoom(int x, int y, boolean zoomIn) {
		int oX = x;// point touched in old coordinates
		int oY = y;
		if (zoomIn) {
			scale++;
			oX *= 2; // point touched in new coordinates
			oY *= 2;
		}
		else {
			scale--;
			oX /= 2; // point touched in new coordinates
			oY /= 2;
		}

		// new origin should center point touched in new coordinates
		int setX = oX - treeDisplay.getWidth() / 2;
		int setY = oY - treeDisplay.getHeight() / 2;

		setOrigin(setX, setY, true);
		sizeDisplay();

	}

	void setHighlighted(boolean edited) {
		messagePanel.setHighlighted(edited);
	}

	/* ................................................................................................................. */
	public void treeEdited(boolean rememberEditedTree) {
		boolean wasEdited = treeEdited;
		if (!MesquiteThread.isScripting())
			editedByHand = true;
		treeEdited = true;
		zapPreviousEdited(true);
		if (wasEdited && treeSourceLocked()) {
			showTreeAnnotation();
		}
		if (rememberEditedTree) {

			recentEditedTrees.addElement(tree.cloneTree(), false);
			if (recentEditedTrees.size() >= maxRecentEditedTrees)
				recentEditedTrees.removeElementAt(0, false);
			palette.recentButton.repaint();
		}
		if (!treeSourceLocked() && originalTree != null) {
			tree.setName(originalTree.getName());
		}
		messagePanel.setHighlighted(true);
		if (treeInfoPanel != null)
			treeInfoPanel.setHighlighted(!treeSourceLocked());
		palette.paletteScroll.setEnableEnter(true);
		if (tree != null)
			treeVersion = tree.getVersionNumber();
		// resetLockImage();
		highlightedBranch.setValue(0);
		dropHighlightedBranch.setValue(0);
		branchFrom = 0;
		storeTreeMenuItem.setEnabled(!treeSourceLocked());
		MesquiteTrunk.resetMenuItemEnabling();
		checkPanelPositionsLegal();
		resetBaseExplanation();
	}

	/*
	 * void lockTouched(){ if (!treeSourceTask.nameMatches("StoredTrees") || !(originalTree instanceof MesquiteTree) ) { lockStoredTrees = true; ownerModule.alert("You cannot unlock the trees, because trees are being supplied by \"" + treeSourceTask.getName() + "\" and are not editable Stored Trees.  Because the trees are locked, they can be viewed but not modified.  Attempts to modify the tree will yield an unsaved temporary tree, which can be stored permanently using the Store Tree menu item."); } else if (lockStoredTrees) { lockStoredTrees = false; //Need to do a save as
	 * ownerModule.alert("The stored trees are now unlocked for editing.  Any editing of the tree being browsed will change the original copy stored in the file."); } else { lockStoredTrees = true; ownerModule.alert("The stored trees are now locked.  Attempts to modify the tree being browsed will yield an unsaved temporary tree, which can be stored permanently using the Store Tree menu item."); } resetLockImage(); } void resetLockImage(){ if (!treeSourceTask.nameMatches("StoredTrees")) lockPanel.setLockState(0); else if (!(originalTree instanceof MesquiteTree) ||
	 * lockStoredTrees) lockPanel.setLockState(1); else lockPanel.setLockState(2);
	 * 
	 * }
	 */
	boolean treeSourceLocked() {
		return (originalTree == null || !(originalTree instanceof MesquiteTree) || !(treeSourceTask.nameMatches("StoredTrees") && treeSourceTask.getNumberOfTrees(taxa) > 0)); // lockStoredTrees ||
	}

	/* ................................................................................................................. */
	public Tree incrementTreeNumber() {
		int numTrees = treeSourceTask.getNumberOfTrees(taxa);
		if (currentTreeNumber + 1 >= numTrees)
			return null;
		return goToTreeNumber(currentTreeNumber + 1, true);
	}

	/* ................................................................................................................. */
	public Tree goToTreeNumber(int index, boolean rememberEdited) {
		if (disposing)
			return null;
		currentTreeNumber = index;
		if (MesquiteThread.isScripting() && ((BasicTreeWindowMaker) ownerModule).suppressEPCResponse)
			return null;
		if (rememberEdited) {
			boolean wasEnabled = false;
			if (recoverEditedMenuItem != null)
				wasEnabled = recoverEditedMenuItem.isEnabled();
			if (treeEdited) {
				if (previousEditedTree != null && previousEditedTree instanceof MesquiteTree)
					taxa.removeListener((MesquiteTree) previousEditedTree);
				previousEditedTree = tree.cloneTree();
				if (previousEditedTree instanceof MesquiteTree)
					taxa.addListener((MesquiteTree) previousEditedTree);
				if (recoverEditedMenuItem != null)
					recoverEditedMenuItem.setEnabled(true);
			}
			if (recoverEditedMenuItem != null && wasEnabled != recoverEditedMenuItem.isEnabled())
				MesquiteTrunk.resetMenuItemEnabling();
		}

		Tree treeT = treeSourceTask.getTree(taxa, index);
		if (disposing)
			return null;
		if (treeT == null) { // source may have not known how many trees; ask if it would revise its current number of trees
			int numTrees = treeSourceTask.getNumberOfTrees(taxa);
			if (currentTreeNumber >= numTrees && MesquiteInteger.isCombinable(numTrees)) {
				currentTreeNumber = numTrees - 1;
				treeT = treeSourceTask.getTree(taxa, currentTreeNumber);
				palette.paletteScroll.setMaximumValue(MesquiteTree.toExternal(numTrees - 1));

			}
		}
		if (disposing)
			return null;
		MesquiteBoolean editStatusToSet = new MesquiteBoolean();
		Tree t = setCloneOfTree(treeT, true, editStatusToSet);
		treeEdited = editStatusToSet.getValue();
		editedByHand = false;
		setTreeName(t);
		messagePanel.setHighlighted(treeEdited);
		if (treeInfoPanel != null)
			treeInfoPanel.setHighlighted(!treeSourceLocked());

		palette.paletteScroll.setCurrentValue(MesquiteTree.toExternal(currentTreeNumber));

		// resetLockImage();
		storeTreeMenuItem.setEnabled(!treeSourceLocked());
		MesquiteTrunk.resetMenuItemEnabling();
		treeAnnotationShown = true;
		resetBaseExplanation();
		checkPanelPositionsLegal();

		return t;
	}

	/* ................................................................................................................. */
	public String getTextContents() {
		if (treeDisplay == null)
			return "";
		String s = "Tree window";
		if (taxa != null)
			s += " for taxa \"" + taxa.getName() + "\"\n";
		s += "\n";
		if (treeSourceTask != null)
			s += "----------------\nShowing " + treeSourceTask.getNotesAboutTrees(taxa) + "\n----------------\n";
		if (originalTree != null)
			s += "\nOriginal Tree:  " + originalTree;

		s += "\n\n" + treeDisplay.getTextVersion();
		return s;
	}

	/* ................................................................................................................. */
	public void paintContents(Graphics g) {
		if (treeDisplay == null) {
			MesquiteMessage.warnProgrammer("Oh no, tree display is null");
		}
		else {
			// ^^^ sizeDisplay();
			treeDisplay.repaint();
			checkPanelPositionsLegal();
			palette.repaintBirdsEye();
			g.setColor(Color.black);
		}
	}

	// for java 1.1 printing
	public Object fit(Dimension dim) {
		/*
		 * int w; int h; int currentWidth = treeDisplay.getFieldWidth(); int currentHeight = treeDisplay.getFieldHeight(); if (currentHeight == 0 || currentWidth == 0) { w = dim.width; h = dim.height; } else if (((double)dim.width)/currentWidth > ((double)dim.height)/currentHeight) { w = (int)(((double)dim.height)/currentHeight * currentWidth); h = dim.height; } else { w = dim.width; h = (int)(((double)dim.width)/currentWidth * currentHeight); }
		 */
		Dimension d = new Dimension(getOriginX(), getOriginY());
		treeDisplay.setFieldSize(dim.width, dim.height);
		setOrigin(0, 0, true);
		treeDisplay.getTreeDrawing().recalculatePositions(treeDisplay.getTree()); // to force node locs recalc
		return d;
	}

	public void unfit(Object o) {
		int oX = 0;
		int oY = 0;
		if (o instanceof Dimension) {
			oX = ((Dimension) o).width;
			oY = ((Dimension) o).height;
		}
		setOrigin(oX, oY, true);
		sizeDisplay();
	}

	/* _________________________________________________ */

	public void InvertTaxon(Graphics g, int M) {

		if (findTaxon(treeDisplay.getMouseX(), treeDisplay.getMouseY()) == M) { // still in taxon
			if (windowModule.getUseXORForBranchMoves() && false) {
				g.setColor(Color.black);
				if (GraphicsUtil.useXORMode(g, true)) {
					g.setXORMode(Color.white);
					try {
						treeDisplay.fillTaxon(g, M);
					} catch (InternalError e) { // workaround to bug in Windows Java 1.7_45
					}
					g.setPaintMode();
					g.setColor(Color.black);
				}
			}
			else {
				// g.setColor(Color.black);
				// if (GraphicsUtil.useXORMode(g, true)) {
				// g.setXORMode(Color.white);
				try {
					treeDisplay.fillTaxon(g, M);
				} catch (InternalError e) { // workaround to bug in Windows Java 1.7_45
				}
				// g.setPaintMode();
				g.setColor(Color.black);
				// }
			}
			highlightedTaxon = M;
		}
		Tree t = treeDisplay.getTree();
		if (t != null && t.getTaxa() != null) {
			if (t.getTaxa().getAnnotation(M) != null)
				setAnnotation(t.getTaxa().getAnnotation(M), "Footnote above refers to taxon \"" + t.getTaxa().getTaxonName(M) + "\"");
			else {
				setExplanation("Taxon: " + t.getTaxa().getTaxonName(M));
				setAnnotation("", null);
			}
			treeAnnotationShown = false; // so that the base Explanation can know whether to refer to the annotation
		}
	}

	/* _________________________________________________ */
	public void RevertTaxon(Graphics g, int M) {
		if (highlightedTaxon >= 0) {
			g.setColor(Color.black);

			if (windowModule.getUseXORForBranchMoves() && false) {

				if (GraphicsUtil.useXORMode(g, true)) {
					g.setXORMode(Color.white);
					try {
						treeDisplay.redrawTaxa(g, highlightedTaxon);
					} catch (InternalError e) { // workaround to bug in Windows Java 1.7_45
					}
					g.setPaintMode();
					treeDisplay.repaint(); // TODO: just redraw the taxon
				}
			}
			else {

				// if (GraphicsUtil.useXORMode(g, true)) {
				// g.setXORMode(Color.white);
				try {
					// treeDisplay.redrawTaxa(g, highlightedTaxon);
				} catch (InternalError e) { // workaround to bug in Windows Java 1.7_45
				}
				// g.setPaintMode();
				treeDisplay.repaint(); // TODO: just redraw the taxon
				// }
			}

			highlightedTaxon = -1;
			g.setColor(Color.black);
		}
		showTreeAnnotation();

	}

	/* _________________________________________________ */
	private int findBranch(int x, int y, MesquiteDouble fraction) {
		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot(); // TODO: remember drawnRoot!!!
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		return treeDisplay.getTreeDrawing().findBranch(tree, drawnRoot, x, y, fraction); // check that still in branch
	}

	private int findTaxon(int x, int y) {
		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot(); // TODO: remember drawnRoot!!!
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		return treeDrawCoordTask.getNamesTask().findTaxon(tree, drawnRoot, x, y);
	}

	/* _________________________________________________ */
	NameReference branchNotesRef = NameReference.getNameReference("note");

	private int countinvert = 0;

	public void InvertBranchOld(Graphics g, int N, MesquiteInteger highlight) {
		InvertBranchOld(g, N, highlight, true);
	}

	public void InvertBranchOld(Graphics g, int N, MesquiteInteger highlight, boolean onlyIfStillInBranch) {
		Tree t = treeDisplay.getTree();
		if (t != null) {
			MesquiteDouble fraction = new MesquiteDouble();
			if (!onlyIfStillInBranch || findBranch(treeDisplay.getMouseX(), treeDisplay.getMouseY(), fraction) == N) { // still in N
				TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
				highlight.setValue(N); // sets the highlighed branch
				if (treeDrawing != null && !treeDisplay.repaintPending()) {
					treeDrawing.fillBranchInverted(t, N, g);
				}
				// colorInvertBranch(t,N,g);
				showBranchExplanation(N);
			}
		}
	}

	/* _________________________________________________ */
	public void HighlightBranch(Graphics g, int N, MesquiteInteger highlight) {
		if (windowModule.getUseXORForBranchMoves())
			InvertBranchOld(g, N, highlight);
		else
			HighlightBranch(g, N, highlight, true);
	}

	public void HighlightBranch(Graphics g, int N, MesquiteInteger highlight, boolean onlyIfStillInBranch) {
		Tree t = treeDisplay.getTree();
		if (t != null) {
			MesquiteDouble fraction = new MesquiteDouble();
			if (!onlyIfStillInBranch || findBranch(treeDisplay.getMouseX(), treeDisplay.getMouseY(), fraction) == N) { // still in N
				TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
				highlight.setValue(N); // sets the highlighed branch
				if (treeDrawing != null && !treeDisplay.repaintPending()) {
					treeDrawing.highlightBranch(t, N, g);
				}
				// colorInvertBranch(t,N,g);
				showBranchExplanation(N);
			}
		}
	}

	/* _________________________________________________ */
	private void showBranchExplanation(int node) {
		String s = "";
		if (tree.numberOfParentsOfNode(node) > 1)
			s = " (Node represents reticulation; has " + tree.numberOfParentsOfNode(node) + " immediate ancestors)";// added 4 feb 02
		if (tree.nodeIsTerminal(node)) {
			Taxon t = tree.getTaxa().getTaxon(tree.taxonNumberOfNode(node));
			if (t != null)
				setExplanation("Taxon: " + t.getName() + s);
			else
				setExplanation("Unknown taxon (the tree description may have been malformed or with undefined taxa)" + s);
		}
		else {
			Taxon t1 = tree.getTaxa().getTaxon(tree.taxonNumberOfNode(tree.leftmostTerminalOfNode(node)));
			Taxon t2 = tree.getTaxa().getTaxon(tree.taxonNumberOfNode(tree.rightmostTerminalOfNode(node)));
			String n1, n2;
			if (t1 == null)
				n1 = "Unknown taxon";
			else
				n1 = t1.getName();
			if (t2 == null)
				n2 = "Unknown taxon";
			else
				n2 = t2.getName();
			setExplanation(" " + tree.numberOfTerminalsInClade(node) + " taxa in clade (" + n1 + " to " + n2 + ").  Node is number " + node + ". " + s);
		}
	}

	/* _________________________________________________ */
	public void RevertBranchOld(Graphics g, MesquiteInteger highlight) {
		// treeDisplay.deletePendingMoveDrag();

		int wasHighlighted = highlight.getValue();
		highlight.setValue(0);
		if (wasHighlighted > 0 && !treeDisplay.repaintPending()) {
			treeDisplay.getTreeDrawing().fillBranchInverted(treeDisplay.getTree(), wasHighlighted, g);
			// treeDisplay.getTreeDrawing().fillBranchInverted(t, N, g);
		}
		showTreeAnnotation();
		// setAnnotation("", null);
		// treeAnnotationShown = false; //so that the base Explanation can know whether to refer to the annotation
	}

	/* _________________________________________________ */
	public void UnhighlightBranchNew(Graphics g, MesquiteInteger highlight) {
		// treeDisplay.deletePendingMoveDrag();

		int wasHighlighted = highlight.getValue();
		highlight.setValue(0);
		if (wasHighlighted > 0 && !treeDisplay.repaintPending()) {
			// treeDisplay.repaint(); // need to do it this way as XOR mode no good
			treeDisplay.getTreeDrawing().unhighlightBranch(treeDisplay.getTree(), wasHighlighted, g);
			// treeDisplay.getTreeDrawing().fillBranchInverted(t, N, g);
		}
		showTreeAnnotation();
		// setAnnotation("", null);
		// treeAnnotationShown = false; //so that the base Explanation can know whether to refer to the annotation
	}

	/* _________________________________________________ */
	public void UnhighlightBranch(Graphics g, MesquiteInteger highlight) {
		if (windowModule.getUseXORForBranchMoves())
			RevertBranchOld(g, highlight);
		else
			UnhighlightBranchNew(g, highlight);
	}

	/* _________________________________________________ */
	public void ScanFlash(Graphics g, int x, int y, int modifiers) {
		if (treeDisplay == null || tree == null || treeDrawCoordTask == null || treeDrawCoordTask.getNamesTask() == null || treeDisplay.getTreeDrawing() == null)
			return;
		if (treeDisplay.getInvalid())
			return;
		MesquiteDouble fraction = new MesquiteDouble();
		int branchFound = findBranch(x, y, fraction);

		/*
		 * if (fraction.isCombinable()) if (treeDisplay.getTreeDrawing().isAtNode(fraction)) System.out.println("in node " + branchFound + ", fraction: " + fraction.getValue()); else System.out.println("  " + fraction.getValue() + "   " + branchFound);
		 */
		if (highlightedBranch.getValue() != 0) { // we are already in a branch
			int wasHighlighted = highlightedBranch.getValue();
			if (branchFound == 0) {
				UnhighlightBranch(g, highlightedBranch);
				notifyExtrasOfBranchExit(g, wasHighlighted);

				setTreeName(tree);
				setExplanation(baseExplanation, false);
			}
			else if (branchFound != highlightedBranch.getValue()) {
				UnhighlightBranch(g, highlightedBranch);
				HighlightBranch(g, branchFound, highlightedBranch);
				notifyExtrasOfBranchExit(g, wasHighlighted);
				notifyExtrasOfBranchEnter(g, branchFound);
			}
		}
		else if (branchFound != 0) { // we weren't in a branch, but now we found one
			HighlightBranch(g, branchFound, highlightedBranch);
			notifyExtrasOfBranchEnter(g, branchFound);
			if (tree.nodeIsTerminal(branchFound)) {
				Taxon t = tree.getTaxa().getTaxon(tree.taxonNumberOfNode(branchFound));
				if (t != null)
					setExplanation("Taxon: " + t.getName());
				else
					setExplanation("Unknown taxon (the tree description may have been malformed or with undefined taxa)");
			}
			else {
				Taxon t1 = tree.getTaxa().getTaxon(tree.taxonNumberOfNode(tree.leftmostTerminalOfNode(branchFound)));
				Taxon t2 = tree.getTaxa().getTaxon(tree.taxonNumberOfNode(tree.rightmostTerminalOfNode(branchFound)));
				String n1, n2;
				if (t1 == null)
					n1 = "Unknown taxon";
				else
					n1 = t1.getName();
				if (t2 == null)
					n2 = "Unknown taxon";
				else
					n2 = t2.getName();
				setExplanation(" " + tree.numberOfTerminalsInClade(branchFound) + " taxa in clade (" + n1 + " to " + n2 + ").  Node is number " + branchFound + ". ");
			}
		}
		else {
			int nameFound = findTaxon(x, y);
			if (highlightedTaxon >= 0) {
				if (nameFound == -1) {
					int wasHighlighted = highlightedTaxon;
					RevertTaxon(g, highlightedTaxon);
					notifyExtrasOfTaxonExit(g, wasHighlighted);
					setExplanation(baseExplanation, false);
				}
				else if (nameFound != highlightedTaxon) {
					int wasHighlighted = highlightedTaxon;
					RevertTaxon(g, highlightedTaxon);
					InvertTaxon(g, nameFound);
					notifyExtrasOfTaxonExit(g, wasHighlighted);
					notifyExtrasOfTaxonEnter(g, nameFound);
					setExplanation(" Taxon: " + taxa.getTaxonName(nameFound));
				}
			}
			else if (nameFound != -1) {
				InvertTaxon(g, nameFound);
				notifyExtrasOfTaxonEnter(g, nameFound);
				setExplanation(" Taxon: " + taxa.getTaxonName(nameFound));
			}
			else {
				currentTreeTool.moved(x, y, tree, modifiers);
				notifyExtrasOfCursorMove(g, x, y);
				// notify extras?
			}
		}

	}

	/* _________________________________________________ */
	public void drawBranchTouchSpot(Graphics g, int x, int y) {
		int spotSize = 8;
		Color oldColor = g.getColor();
		g.setColor(Color.yellow);
		GraphicsUtil.fillOval(g, x - spotSize / 2, y - spotSize / 2, spotSize, spotSize, false);
		g.setColor(Color.black);
		GraphicsUtil.drawOval(g, x - spotSize / 2, y - spotSize / 2, spotSize, spotSize);
		g.setColor(oldColor);
	}
	/* _________________________________________________ */
	/* Historical note: this method's name begins with an upper case letter because it descends directly
	from Pascal source code for MacClade 1 (1986), under the name ScanPick */
	public boolean ScanTouch(Graphics g, int x, int y, int modifiers) {
		if (treeDisplay == null || tree == null || treeDrawCoordTask == null || treeDrawCoordTask.getNamesTask() == null || treeDisplay.getTreeDrawing() == null)
			return false;
		if (treeDisplay.getInvalid())
			return false;
		xFrom = -1;
		yFrom = -1;
		xTo = -1;
		yTo = -1;
		fieldTouchX = -1;
		fieldTouchY = -1;
		lastFieldDragX = -1;
		lastFieldDragY = -1;
		taxonTouched = -1;
		MesquiteDouble fraction = new MesquiteDouble();
		int branchFound = findBranch(x, y, fraction);
		if (branchFound != 0) { // in a branch
			branchFrom = branchFound;
			if (currentTreeTool.informTransfer()) {
				// branchFrom=branchFound;
				xFrom = x;
				yFrom = y;
				xTo = x;
				yTo = y;
				if (GraphicsUtil.useXORMode(g, true)) {
					if (windowModule.getUseXORForBranchMoves()) {
						GraphicsUtil.drawXORLine(g, xFrom, yFrom, xTo, yTo, scanLineThickness, Color.lightGray);
					}
					else
						drawBranchTouchSpot(g, xFrom, yFrom);
				}
			}
			else {
				if (highlightedBranch.getValue() != 0) {
					notifyExtrasOfBranchExit(g, highlightedBranch.getValue());
					UnhighlightBranch(g, highlightedBranch);
				}
				currentTreeTool.branchTouched(branchFound, x, y, tree, modifiers);
				// branchFrom = 0;
			}
			notifyExtrasOfBranchTouch(g, branchFound);
			return true;
		}
		else { // not in a branch
			int nameFound = findTaxon(x, y);
			if (nameFound != -1) { // it is in a taxon
				currentTreeTool.taxonTouched(nameFound, tree, modifiers);
				taxonTouched = nameFound;
				notifyExtrasOfTaxonTouch(g, nameFound);
				if (highlightedTaxon >= 0)
					RevertTaxon(g, highlightedTaxon);
				return true;
			}
			else { // not in a taxon
				if (currentTreeTool.isArrowTool()) {
					fieldTouchX = x;
					fieldTouchY = y;
					lastFieldDragX = x;
					lastFieldDragY = y;
					if (!windowModule.getUseXORForBranchMoves()) {
						GraphicsUtil.drawCross(g, fieldTouchX, fieldTouchY, 10);
						treeDisplay.setCrossDrawn(true);
					}
				}

				boolean fieldTouchAccepted = currentTreeTool.fieldTouched(x, y, tree, modifiers);
				// notify extras?
				return fieldTouchAccepted;
			}
		}
		// return false;
	}
	/* _________________________________________________ */
	/* Historical note: this method's name begins with an upper case letter because it descends directly
	from Pascal source code for MacClade 1 (1986) */
	public void ScanDrop(Graphics g, int x, int y, int modifiers) {
		if (treeDisplay == null || tree == null || treeDisplay.getTreeDrawing() == null)
			return;
		if (treeDisplay.getInvalid())
			return;
		if (currentTreeTool.isArrowTool() && fieldTouchX >= 0 && fieldTouchY >= 0) {
			g.setColor(Color.blue);
			if (GraphicsUtil.useXORMode(g, false) && windowModule.getUseXORForBranchMoves()) {
				g.setXORMode(Color.white); // for some reason color doesn't matter in MacOS, but does in Win95
				GraphicsUtil.drawRect(g, fieldTouchX, fieldTouchY, lastFieldDragX - fieldTouchX, lastFieldDragY - fieldTouchY);
			}
			highlightedNodes = null;
			highlightedTaxa = null;

			if (!dragSelect(modifiers, fieldTouchX, fieldTouchY, x - fieldTouchX, y - fieldTouchY)) {
				if (taxa.anySelected()) {
					taxa.deselectAll();
					taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
				if (tree.anySelected()) {
					tree.deselectAll();
					tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
				if (Math.abs(x - fieldTouchX) < 4 && Math.abs(y - fieldTouchY) < 4)
					notifyExtrasOfFieldTouch(g, x, y, modifiers);

			}

			fieldTouchX = -1;
			fieldTouchY = -1;

			lastFieldDragX = -1;
			lastFieldDragY = -1;
			if (treeDisplay.isCrossDrawn())
				treeDisplay.update(g);
			return;
		}
		if ((!treeDisplay.getTree().isLocked()) && (branchFrom != 0)) {
			MesquiteDouble fraction = new MesquiteDouble();
			if (currentTreeTool.informTransfer()) {
				int branchTo = findBranch(x, y, fraction);
				if (branchTo != 0 && !windowModule.getUseXORForBranchMoves()) {
					GraphicsUtil.drawArrow((Graphics2D) g, xFrom, yFrom, xTo, yTo, 2); // only if drawn
					drawBranchTouchSpot(g, xFrom, yFrom);
					MesquiteThread.pauseForSeconds(0.5);
				}

			}
			if (highlightedBranch.getValue() != 0) {
				notifyExtrasOfBranchExit(g, highlightedBranch.getValue());
				UnhighlightBranch(g, highlightedBranch);
			}
			else if (highlightedTaxon >= 0)
				RevertTaxon(g, highlightedTaxon);
			g.setColor(Color.black);
			if (GraphicsUtil.useXORMode(g, false) && windowModule.getUseXORForBranchMoves()) {
				GraphicsUtil.drawXORLine(g, xFrom, yFrom, xTo, yTo, scanLineThickness, Color.lightGray);
			}

			if (currentTreeTool.informTransfer()) {
				int branchTo = findBranch(x, y, fraction);
				if (branchTo != 0) {
					if (branchTo == branchFrom && currentTreeTool.isArrowTool()) {
						selectBranch(modifiers, branchTo);
					}
					else {
						currentTreeTool.branchTransferred(branchFrom, branchTo, tree, modifiers);
					}
				}
			}
			else if (currentTreeTool.informDrop()) {
				// g.drawLine(xFrom,yFrom,xTo,yTo); //only if drawn
				currentTreeTool.branchDropped(branchFrom, x, y, tree, modifiers);
			}
			branchFrom = 0;
		}
		else {
			int nameFound = findTaxon(x, y);
			if (nameFound != -1) {
				if (taxonTouched == nameFound && currentTreeTool.isArrowTool())
					selectTaxon(modifiers, taxonTouched);
				currentTreeTool.taxonMouseUp(nameFound, x, y, tree, modifiers);
				notifyExtrasOfTaxonTouch(g, nameFound);
				if (highlightedTaxon >= 0)
					RevertTaxon(g, highlightedTaxon);
				taxonTouched = -1;
				return;
			}
			else
				currentTreeTool.fieldMouseUp(x, y, tree, modifiers);
			// notify extras?
		}
		taxonTouched = -1;
	}

	/* _________________________________________________ */
	/* Historical note: this method's name begins with an upper case letter because it descends directly
	from Pascal source code for MacClade 1 (1986) */
	public void ScanDrag(Graphics g, int x, int y, int modifiers) {
		if (treeDisplay == null || tree == null)
			return;
		if (treeDisplay.getInvalid())
			return;
		if (currentTreeTool.isArrowTool() && fieldTouchX >= 0 && fieldTouchY >= 0) {
			g.setColor(Color.blue);
			if (GraphicsUtil.useXORMode(g, false) && windowModule.getUseXORForBranchMoves()) {
				g.setXORMode(Color.white); // for some reason color doesn't matter in MacOS, but does in Win95
				GraphicsUtil.drawRect(g, fieldTouchX, fieldTouchY, lastFieldDragX - fieldTouchX, lastFieldDragY - fieldTouchY);
				GraphicsUtil.drawRect(g, fieldTouchX, fieldTouchY, x - fieldTouchX, y - fieldTouchY);
				// g.drawRect(fieldTouchX,fieldTouchY,lastFieldDragX-fieldTouchX,lastFieldDragY-fieldTouchY);
				// g.drawRect(fieldTouchX,fieldTouchY,x-fieldTouchX,y-fieldTouchY);
			}
			else
				dragHighlight(g, modifiers, fieldTouchX, fieldTouchY, x - fieldTouchX, y - fieldTouchY);
			lastFieldDragX = x;
			lastFieldDragY = y;
			return;
		}
		MesquiteDouble fraction = new MesquiteDouble();
		int branchFound = findBranch(x, y, fraction);
		if (branchFound > 0) {
			if (branchFound != dropHighlightedBranch.getValue()) {
				UnhighlightBranch(g, dropHighlightedBranch);
				HighlightBranch(g, branchFound, dropHighlightedBranch);
			}
		}
		else if (dropHighlightedBranch.getValue() > 0)
			UnhighlightBranch(g, dropHighlightedBranch);

		if (branchFrom != 0) {
			if (currentTreeTool.informTransfer()) {
				g.setColor(Color.black);
				if (GraphicsUtil.useXORMode(g, false) && windowModule.getUseXORForBranchMoves()) {
					GraphicsUtil.drawXORLine(g, xFrom, yFrom, xTo, yTo, scanLineThickness, Color.lightGray);
				}
				xTo = x;
				yTo = y;
				if (GraphicsUtil.useXORMode(g, false) && windowModule.getUseXORForBranchMoves())
					GraphicsUtil.drawXORLine(g, xFrom, yFrom, xTo, yTo, scanLineThickness, Color.lightGray);
			}
			else if (currentTreeTool.informDrag()) {
				currentTreeTool.branchDragged(branchFrom, x, y, tree, modifiers);
			}
		}
	}

	public void findContained(Tree tree, int N, int x, int y, int w, int h, Vector nodes, Vector taxons) {
		TreeDrawing drawing = treeDisplay.getTreeDrawing();
		int dX = (int)drawing.x[N];   //integer node loc approximation
		int dY = (int)drawing.y[N];   //integer node loc approximation

		if (dX >= x && dX <= x + w && dY >= y && dY <= y + h)
			nodes.addElement(new MesquiteInteger(N));

		if (tree.nodeIsTerminal(N) && drawing.namePolys != null) {
			Polygon term = drawing.namePolys[tree.taxonNumberOfNode(N)];
			if (term != null && term.intersects(x, y, w, h))
				taxons.addElement(new MesquiteInteger(tree.taxonNumberOfNode(N)));
		}
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			findContained(tree, d, x, y, w, h, nodes, taxons);
		}

	}

	public boolean shrinkWrapNodes(MesquiteTree tree, int N, boolean selBelow) {
		int pathsSelAbove = 0;
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			boolean selAbove = shrinkWrapNodes(tree, d, selBelow || tree.getSelected(N));
			if (selAbove)
				pathsSelAbove++;
		}
		if (pathsSelAbove > 1 || tree.getSelected(N))
			tree.selectAllInClade(N);
		return pathsSelAbove > 0 || tree.getSelected(N);
	}

	/*-----------------------------------------*/
	/** SelectsAllNodes in the clade */
	public void selectAllTaxaInClade(Tree tree, int node) {
		if (tree.nodeIsTerminal(node))
			taxa.setSelected(tree.taxonNumberOfNode(node), true);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			selectAllTaxaInClade(tree, d);
		}
	}

	public boolean shrinkWrapTaxa(MesquiteTree tree, int N) {
		int pathsSelAbove = 0;
		boolean thisIsSelected = tree.nodeIsTerminal(N) && tree.getTaxa().getSelected(tree.taxonNumberOfNode(N));
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			boolean selAbove = shrinkWrapTaxa(tree, d);
			if (selAbove)
				pathsSelAbove++;
		}
		if (pathsSelAbove > 1)
			selectAllTaxaInClade(tree, N);
		return pathsSelAbove > 0 || thisIsSelected;
	}

	/*-----------------------------------------*/
	Vector highlightedNodes = new Vector();

	Vector highlightedTaxa = new Vector();

	private Vector vectorElementsInFirstButNotSecond(Vector firstVector, Vector secondVector) {
		if (secondVector == null)
			return firstVector;
		if (firstVector == null)
			return null;
		Vector vector = new Vector();
		for (int i = 0; i < firstVector.size(); i++) {
			boolean found = false;
			for (int j = 0; j < secondVector.size() && !found; j++) {
				if (((MesquiteInteger) firstVector.elementAt(i)).getValue() == ((MesquiteInteger) secondVector.elementAt(j)).getValue()) {
					found = true;
				}
			}
			if (!found)
				vector.add(firstVector.elementAt(i));
		}
		return vector;
	}

	private void dragHighlight(Graphics g, int modifiers, int x, int y, int w, int h) {
		Vector nodes = new Vector();
		Vector taxons = new Vector();

		if (w < 0) {
			int nx = x + w;
			x = nx;
			w = -w;
		}
		if (h < 0) {
			int ny = y + h;
			y = ny;
			h = -h;
		}
		findContained(tree, tree.getRoot(), x, y, w, h, nodes, taxons);

		Vector nodesToUnselect = vectorElementsInFirstButNotSecond(highlightedNodes, nodes);
		Vector taxaToUnselect = vectorElementsInFirstButNotSecond(highlightedTaxa, taxons);

		Vector nodesToSelect = null;
		Vector taxaToSelect = null;

		if ((nodesToUnselect != null && nodesToUnselect.size() > 0) || (taxaToUnselect != null && taxaToUnselect.size() > 0)) {
			treeDisplay.update(g);
			if (!windowModule.getUseXORForBranchMoves()) {
				GraphicsUtil.drawCross(g, fieldTouchX, fieldTouchY, 10);
				treeDisplay.setCrossDrawn(true);
			}
			nodesToSelect = nodes;
			taxaToSelect = taxons;
		}
		else {
			nodesToSelect = vectorElementsInFirstButNotSecond(nodes, highlightedNodes);
			taxaToSelect = vectorElementsInFirstButNotSecond(taxons, highlightedTaxa);
		}
		if (nodesToSelect.size() > 0) {
			for (int i = 0; i < nodesToSelect.size(); i++) {
				MesquiteInteger mi = (MesquiteInteger) nodesToSelect.elementAt(i);
				HighlightBranch(g, mi.getValue(), highlightedBranch, false);
			}
		}
		if (taxaToSelect.size() > 0) {
			for (int i = 0; i < taxaToSelect.size(); i++) {
				MesquiteInteger mi = (MesquiteInteger) taxaToSelect.elementAt(i);
				try {
					treeDisplay.fillTaxon(g, mi.getValue());
				} catch (InternalError e) { // workaround to bug in Windows Java 1.7_45
				}
			}
		}
		highlightedNodes = new Vector(nodes);
		highlightedTaxa = new Vector(taxons);
	}

	/*-----------------------------------------*/

	private boolean dragSelect(int modifiers, int x, int y, int w, int h) {
		Vector nodes = new Vector();
		Vector taxons = new Vector();
		if (w < 0) {
			int nx = x + w;
			x = nx;
			w = -w;
		}
		if (h < 0) {
			int ny = y + h;
			y = ny;
			h = -h;
		}
		boolean selectionChanged = false;
		boolean shiftDown = MesquiteEvent.shiftKeyDown(modifiers);
		boolean commandDown = MesquiteEvent.commandOrControlKeyDown(modifiers);
		findContained(tree, tree.getRoot(), x, y, w, h, nodes, taxons);
		if (nodes.size() > 0) {
			if (!shiftDown && !commandDown)
				tree.deselectAll();
			for (int i = 0; i < nodes.size(); i++) {
				MesquiteInteger mi = (MesquiteInteger) nodes.elementAt(i);
				if (commandDown)
					tree.setSelected(mi.getValue(), !tree.getSelected(mi.getValue()));
				else
					tree.setSelected(mi.getValue(), true);
			}
			if (shiftDown)
				shrinkWrapNodes(tree, tree.getRoot(), false);
			tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			selectionChanged = true;
		}
		if (taxons.size() > 0) {
			if (!shiftDown && !commandDown)
				taxa.deselectAll();
			for (int i = 0; i < taxons.size(); i++) {
				MesquiteInteger mi = (MesquiteInteger) taxons.elementAt(i);
				if (commandDown)
					taxa.setSelected(mi.getValue(), !taxa.getSelected(mi.getValue()));
				else
					taxa.setSelected(mi.getValue(), true);
			}
			if (shiftDown)
				shrinkWrapTaxa(tree, tree.getRoot());
			taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			selectionChanged = true;
		}
		return selectionChanged;
	}

	private void selectBranch(int modifiers, int node) {
		boolean shiftDown = MesquiteEvent.shiftKeyDown(modifiers);
		boolean commandDown = MesquiteEvent.commandOrControlKeyDown(modifiers);
		if (!shiftDown && !commandDown)
			tree.deselectAll();
		if (commandDown)
			tree.setSelected(node, !tree.getSelected(node));
		else
			tree.setSelected(node, true);
		if (shiftDown)
			shrinkWrapNodes(tree, tree.getRoot(), false);
		tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
	}

	private void selectTaxon(int modifiers, int taxon) {
		boolean shiftDown = MesquiteEvent.shiftKeyDown(modifiers);
		boolean commandDown = MesquiteEvent.commandOrControlKeyDown(modifiers);
		if (!shiftDown && !commandDown)
			taxa.deselectAll();
		if (commandDown)
			taxa.setSelected(taxon, !taxa.getSelected(taxon));
		else
			taxa.setSelected(taxon, true);
		if (shiftDown)
			shrinkWrapTaxa(tree, tree.getRoot());
		taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
	}

	/* ................................................................................................ */
	public void notifyExtrasOfCursorMove(Graphics g, int x, int y) {
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					tce.cursorMove(tree, x, y, g);
				}
			}
		}
	}

	/* ................................................................................................ */
	public void notifyExtrasOfTaxonEnter(Graphics g, int M) {
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					tce.cursorEnterTaxon(tree, M, g);
				}
			}
		}
		if (treeInfoPanel != null && infoPanelOn.getValue())
			treeInfoPanel.taxonEnter(M);
	}

	/* ................................................................................................ */
	public void notifyExtrasOfTaxonExit(Graphics g, int M) {
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					tce.cursorExitTaxon(tree, M, g);
				}
			}
		}
		if (treeInfoPanel != null && infoPanelOn.getValue())
			treeInfoPanel.taxonExit(M);
	}

	/* ................................................................................................ */
	public void notifyExtrasOfTaxonTouch(Graphics g, int M) {
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					tce.cursorTouchTaxon(tree, M, g);
				}
			}
		}
		if (treeInfoPanel != null && infoPanelOn.getValue())
			treeInfoPanel.taxonTouch(M);
	}

	/* ................................................................................................ */
	public void notifyExtrasOfBranchEnter(Graphics g, int N) {
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					tce.cursorEnterBranch(tree, N, g);
				}
			}
		}
		if (treeInfoPanel != null && infoPanelOn.getValue())
			treeInfoPanel.branchEnter(N);
	}

	/* ................................................................................................ */
	public void notifyExtrasOfBranchExit(Graphics g, int N) {
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					tce.cursorExitBranch(tree, N, g);
				}
			}
		}
		if (treeInfoPanel != null && infoPanelOn.getValue())
			treeInfoPanel.branchExit(N);
	}

	/* ................................................................................................ */
	public void notifyExtrasOfBranchTouch(Graphics g, int N) {
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					tce.cursorTouchBranch(tree, N, g);
				}
			}
		}
		if (treeInfoPanel != null && infoPanelOn.getValue())
			treeInfoPanel.branchTouch(N);
	}

	/* ................................................................................................ */
	public void notifyExtrasOfFieldTouch(Graphics g, int x, int y, int modifiers) {
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					tce.cursorTouchField(tree, g, x, y, modifiers);
				}
			}
		}
	}

	/* ................................................................................................................. */
	private void treeChanged(boolean notifyContextListeners) {
		if (tree == null || previousTree == null || undoTree == null)
			return;

		if (tree.upToDateWithTaxa() && previousTree.upToDateWithTaxa()) {
			canUndo = true;
		}
		undoTree.setToClone(previousTree);

		undoTree.setName("Untitled Tree");

		previousTree.setToClone(tree);
		if (tree != oldTree) {
			treeVersion = tree.getVersionNumber();
			oldTree = tree;
		}
		else if (treeVersion != tree.getVersionNumber()) {
			treeEdited(false);
		}
		if (treeDisplay != null) {
			treeDisplay.setTree(tree);

			treeDisplay.getTreeDrawing().setDrawnRoot(tree.getRoot());
			// rooted.setValue(tree.getRooted());
			setTreeName(tree);
			setExplanation(baseExplanation, false);
		}
		else
			ownerModule.alert("Tree display null in treeChanged");
		if (treeInfoPanel != null)
			treeInfoPanel.setTree(tree);
		if (notifyContextListeners && windowModule.contextListeners != null) {
			Enumeration e = windowModule.contextListeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeContextListener) {
					TreeContextListener tce = (TreeContextListener) obj;
					tce.treeChanged(tree);
				}
			}
		}
		if (treeDisplay != null) {
			treeDisplay.setTreeAllExtras(tree);
			treeDisplay.pleaseUpdate(true);
			if (getMode() > 0)
				updateTextPage();
		}

		Enumeration em = ownerModule.getEmployeeVector().elements();
		while (em.hasMoreElements()) {
			Object obj = em.nextElement();
			MesquiteModule mb = (MesquiteModule) obj;
			if (mb instanceof TreeWindowAssistant) {
				((TreeWindowAssistant) mb).setTree(tree);
			}
		}
		storeTreeMenuItem.setEnabled(!treeSourceLocked());
		MesquiteTrunk.resetMenuItemEnabling();
		showTreeAnnotation();
		sizeDisplay();
		contentsChanged();
	}

	/* ................................................................................................................. */
	void showTreeAnnotation() {
		Tree t = annotatableTree();
		if (t != null) {
			setDefaultAnnotatable((Annotatable) t);
			setAnnotation((Annotatable) t);
			treeAnnotationShown = true; // so that the base Explanation can refer to the annotation
			resetCursor();
		}
	}

	/* ................................................................................................................. */
	/** passes which object is being disposed (from MesquiteListener interface) */
	public void disposing(Object obj) {
		if (obj instanceof Taxa && (Taxa) obj == taxa) {
			ownerModule.iQuit();
		}
	}

	/* ................................................................................................................. */
	/** passes which object is being disposed (from MesquiteListener interface) */
	public boolean okToDispose(Object obj, int queryUser) {
		return true; // TODO: respond
	}

	/* ................................................................................................................. */
	/** passes which object changed (from MesquiteListener interface) */
	public void changed(Object caller, Object obj, Notification notification) {
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof Taxa && (Taxa) obj == taxa) {
			if (code == MesquiteListener.PARTS_CHANGED || code == MesquiteListener.PARTS_ADDED || code == MesquiteListener.PARTS_MOVED || code == MesquiteListener.PARTS_DELETED) {
				canUndo = false;
			}
			else if (code == MesquiteListener.NAMES_CHANGED || code == MesquiteListener.SELECTION_CHANGED)
				treeDisplay.pleaseUpdate(true);
			if (getMode() > 0)
				updateTextPage();
		}
		else if (obj instanceof Tree && (Tree) obj == tree) { // && code!=MesquiteListener.ANNOTATION_CHANGED){
			treeEdited(true);
			treeChanged(code != MesquiteListener.ANNOTATION_CHANGED && code != MesquiteListener.ANNOTATION_DELETED && code != MesquiteListener.ANNOTATION_ADDED);
		}
		/*
		 * else if (obj instanceof Tree && (Tree)obj == tree && code==MesquiteListener.ANNOTATION_CHANGED){ treeEdited(); setTreeName(); setExplanation(baseExplanation, true); if (getMode()>0) updateTextPage(); }
		 */
		/*
		 * else if (obj == currentTreeFootnote){ //footnoteToTreeAnnotation(); }
		 */
		super.changed(caller, obj, notification);
	}

	/* ................................................................................................................. */

	void resetBaseExplanation() {
		if (ownerModule == null || ownerModule.isDoomed())
			return;
		String td = "";
		String td2 = "";
		if (tree != null) {
			if (treeAnnotationShown && !StringUtil.blank(tree.getAnnotation()))
				td = "Footnote above refers to the current tree. \nTree has " + tree.numberOfTerminalsInClade(tree.getRoot()) + " terminal taxa.";
			else
				td = "The current tree has " + tree.numberOfTerminalsInClade(tree.getRoot()) + " terminal taxa.";

			if (tree.hasPolytomies(tree.getRoot())) {
				if (tree.getPolytomiesAssumption() == 0)
					td += " Polytomies in this tree are assumed hard.";
				else if (tree.getPolytomiesAssumption() == 1)
					td += " Polytomies in this tree are assumed soft.";
			}
			if (!tree.getRooted())
				td += " This tree is unrooted.";
			if (tree.anySelectedInClade(tree.getRoot()))
				td += " " + tree.numberSelectedInClade(tree.getRoot()) + " branch(es) selected.";
			td += "\n";
		}
		if (ownerModule.getProject().getNumberTaxas() > 1 && tree != null)
			td += "Tree window shows trees for taxa \"" + tree.getTaxa().getName() + "\"\n";
		if (treeEdited)
			baseExplanation.setValue(td + "Editing Mode.  " + td2 + "When in browsing mode, the trees shown are from " + treeSourceTask.getName() + " [" + treeSourceTask.getParameters() + "]");
		else
			baseExplanation.setValue(td + "Browsing Mode.  " + td2 + "The trees shown are from " + treeSourceTask.getName() + " [" + treeSourceTask.getParameters() + "]");
		setExplanation(baseExplanation, true);
	}

	/* ................................................................................................................. */
	public void setExplanation(String exp) {
		baseExplanationUsed = false;
		super.setExplanation(exp);
	}

	/* ................................................................................................................. */
	void setExplanation(MesquiteString exp, boolean setEvenIfAlreadyBase) {
		if (ownerModule == null || ownerModule.isDoomed())
			return;
		if (exp == null)
			return;
		if (exp != baseExplanation || !(baseExplanationUsed && !setEvenIfAlreadyBase)) {
			baseExplanationUsed = true;
			super.setExplanation(exp.toString());
		}
	}

	/* ................................................................................................................. */
	private Tree annotatableTree() {
		Tree t;
		if (treeEdited) {
			t = tree;
			if (t != null && t instanceof Annotatable && (t != originalTree || treeSourceTask.nameMatches("StoredTrees")))
				return t;
		}
		else {
			t = originalTree;
			if (t != null && t instanceof Annotatable && treeSourceTask.nameMatches("StoredTrees"))
				return t;
		}
		return null;
	}

	/* ................................................................................................................. */
	private void hookCurrentTree() {
		tree.addListener(this);
		tree.getTaxa().addListenerHighPriority(tree); // just in case tree isn't part of TreeVector that will notify it; added 14 Feb 02
		showTreeAnnotation();
	}

	/* ................................................................................................................. */
	private void unhookPreviousTree() {
		tree.removeListener(this);
		tree.getTaxa().removeListener(tree); // just in case tree isn't part of TreeVector that will notify it; added 14 Feb 02
	}

	String getTreeNameAndDetails() {
		return messagePanel.getFullMessage();
	}

	/* ................................................................................................................. */
	public void setTreeName(Tree t) {
		String treename;
		if (ownerModule == null || ownerModule.isDoomed())
			return;
		if (treeEdited && treeSourceLocked()) {
			if (t != null) {
				treename = tree.getName();
			}
			else
				treename = "Untitled Tree";
		}
		else if (usingDefaultTree)
			treename = "DEFAULT TREE SHOWN BECAUSE TREE SOURCE NOT SUPPLYING TREE";
		else {
			if (t != null && t.hasName())
				treename = t.getName();
			else {
				treename = treeSourceTask.getTreeNameString(taxa, MesquiteTree.toInternal(palette.paletteScroll.getCurrentValue()));
			}
		}
		messagePanel.setMessage(treename);
		
		if (treeInfoPanel != null)
			treeInfoPanel.setTreeAndSourceName(treename, treeSourceTask.getName());

	}

	/* ................................................................................................................. */
	public Tree setTree(String TreeDescription) {
		return setTree(TreeDescription, null);
	}

	/* ................................................................................................................. */
	Tree setTree(String TreeDescription, String name) {
		if (ownerModule == null || ownerModule.isDoomed())
			return null;
		if (taxa != null && taxa.isDoomed()) {
			ownerModule.iQuit();
			return null;
		}
		if (treeDisplay != null) {
			if (taxa == null) {
				Tree displayTree = treeDisplay.getTree();
				if (displayTree == null)
					displayTree = tree;
				if (displayTree == null)
					return null;
				Taxa newTaxa = displayTree.getTaxa();
				taxa = newTaxa;
			}
			if (taxa == null)
				return null;
			MesquiteTree t = new MesquiteTree(taxa);
			if (!t.readTree(TreeDescription)) {
				ownerModule.discreetAlert("That tree description is invalid (" + TreeDescription + ")");
				return null;
			}
			// t.warnRetIfNeeded();
			if (tree != null) {
				unhookPreviousTree();
			}

			treeDisplay.setTree(t);
			if (tree != null)
				tree.dispose();
			tree = t;
			if (name == null)
				name = "Untitled Tree";
			tree.setName(name);
			treeEdited = true;
			zapPreviousEdited(true);
			hookCurrentTree();
			treeChanged(true);
			palette.paletteScroll.setEnableEnter(true);
			return tree;
		}
		return null;
	}

	void forceRenameTree(String n) {
		if (ownerModule == null || ownerModule.isDoomed())
			return;
		if (taxa != null && taxa.isDoomed()) {
			ownerModule.iQuit();
			return;
		}
		tree.setName(n);
		setTreeName(tree);
	}

	/* ................................................................................................................. */
	Tree setCloneOfTree(Tree treeToClone, boolean resetOriginal, MesquiteBoolean editStatusToSet) { // displays copy for editing
		if (taxa != null && taxa.isDoomed()) {
			ownerModule.iQuit();
			return null;
		}
		usingDefaultTree = false;
		originalTree = null;
		if (treeDisplay != null) {
			if (treeToClone == null) {
				if (!treeEdited) {
					if (tree != null) {
						unhookPreviousTree();
						tree.dispose();
					}
					if (windowModule.editMode)
						treeToClone = taxa.getDefaultDichotomousTree(null);
					else
						treeToClone = taxa.getDefaultTree();
					usingDefaultTree = true;
					resetOriginal = true;
					ownerModule.discreetAlert(MesquiteThread.isScripting() || warningGivenForTreeSource, "Tree source \"" + treeSourceTask.getName() + "\" is not supplying a tree; a default tree may be shown in Tree Window (b).");
					if (!MesquiteThread.isScripting())
						warningGivenForTreeSource = true;
					editStatusToSet.setValue(false);
				}
				else {
					treeToClone = tree;
					resetOriginal = true;
					editStatusToSet.setValue(true);
				}
			}
			else
				editStatusToSet.setValue(false);

			if (treeToClone != null) {
				if (tree != null) {
					unhookPreviousTree();
					tree.dispose();
				}
				if (resetOriginal) { // && !treeEdited){
					if (originalTree != null && originalTree instanceof MesquiteTree)
						taxa.removeListener((MesquiteTree) originalTree);
					this.originalTree = treeToClone; // otree
					if (originalTree != null && originalTree instanceof MesquiteTree)
						taxa.addListener((MesquiteTree) originalTree);
				}
				tree = treeToClone.cloneTree();
				setTreeName(tree);
			}
			hookCurrentTree();
			treeChanged(true);
			return tree;
		}
		else if (ownerModule != null)
			ownerModule.alert("Tree display null in setCloneOfTree");
		return null;
	}

	/* ................................................................................................................. */
	public Tree setTree(MesquiteTree originalTree) { // displays tree; only used in resetForTreeSource for editedTree
		if (taxa != null && taxa.isDoomed()) {
			ownerModule.iQuit();
			return null;
		}
		if (treeDisplay != null) {
			if (tree != null) {
				unhookPreviousTree();
				tree.dispose();
			}
			if (originalTree == null) {
				if (windowModule.editMode)
					tree = taxa.getDefaultDichotomousTree(null);
				else
					tree = taxa.getDefaultTree();
				setTreeName(tree);
				usingDefaultTree = true;
				ownerModule.discreetAlert(MesquiteThread.isScripting() || warningGivenForTreeSource, "Tree source \"" + treeSourceTask.getName() + "\" is not supplying a tree; a default tree may be shown in Tree Window (a).");
				if (!MesquiteThread.isScripting())
					warningGivenForTreeSource = true;
			}
			else {
				usingDefaultTree = false;
				tree = originalTree;
				setTreeName(tree);
			}
			hookCurrentTree();
			treeChanged(true);
			return tree;
		}
		else
			ownerModule.alert("Tree display null in showTree");
		return null;
	}

	/* ................................................................................................................. */
	public void windowResized() {
		super.windowResized();
		if (!MesquiteThread.isScripting() || (windowModule != null && windowModule.respondToWindowResize))
			sizeDisplay();
	}

	/* ................................................................................................................. */
	public void dispose() {
		disposing = true;
		waitUntilDisposable();

		if (taxa != null) {
			if (originalTree != null && originalTree instanceof MesquiteTree)
				taxa.removeListener((MesquiteTree) originalTree);
			if (previousEditedTree != null && previousEditedTree instanceof MesquiteTree)
				taxa.removeListener((MesquiteTree) previousEditedTree);
			if (previousTree != null)
				taxa.removeListener(previousTree);
			if (undoTree != null)
				taxa.removeListener(undoTree);
			if (oldTree != null)
				taxa.removeListener(oldTree);
			if (recentEditedTrees != null) {
				taxa.removeListener(recentEditedTrees);
				recentEditedTrees.dispose();
			}
		}

		if (tree != null) {
			unhookPreviousTree();
			tree.dispose();
		}
		if (taxa != null)
			taxa.removeListener(this);
		if (magnifyExtra != null)
			magnifyExtra.dispose();
		tree = null;
		if (palette != null)
			palette.dispose();
		windowModule = null;
		if (currentTreeTool != null) {
			currentTreeTool.dispose();
			currentTreeTool = null;
		}
		treeDrawCoordTask = null;
		treeSourceTask = null;
		try {
			if (treeDisplay != null) {
				removeFromWindow(treeDisplay);
				treeDisplay.dispose();
			}
			treeDisplay = null;
			if (setTreeNumberCommand != null)
				setTreeNumberCommand.setOwner(null);
			setTreeNumberCommand = null;
			// if (rooted!=null)
			// rooted.releaseMenuItem();
		} catch (NullPointerException e) {
		}
		super.dispose();
	}
}

/* ======================================================================== *
class REALTreeScrollPane extends ScrollPane implements AdjustmentListener, MouseWheelListener { // REALTreeScrollPane
	BasicTreeWindow window;

	Component treeDisplay;

	public REALTreeScrollPane(BasicTreeWindow window) {
		super();
		addMouseWheelListener(this);

		this.window = window;
	}

	public void addTreeDisplay(Component c) {
		addImpl(c, null, 0);
		treeDisplay = c;
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		Adjustable hScroll = getHAdjustable();
		Adjustable vScroll = getVAdjustable();

		window.setOrigin(hScroll.getValue(), vScroll.getValue(), false);
	}

	public boolean isFauxScrollPane() {
		return false;
	}

	public void setHMinMax(int min, int max) {
		// ignore for real scrollpane
	}

	public void setVMinMax(int min, int max) {
		// ignore for real scrollpane
	}

	public Dimension getViewportSize() {
		return new Dimension(getContentsWidth(), getContentsHeight());
	}

	public int getContentsWidth() {
		return getWidth() - window.scrollWidth;
	}

	public int getContentsHeight() {
		return getHeight() - window.scrollWidth;
	}

	public void setSize(int w, int h) {
		super.setSize(w, h);
		// doLayout();
		window.checkPanelPositionsLegal();

	}

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		// doLayout();
		window.checkPanelPositionsLegal();

	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int amount = e.getScrollAmount() * 2;
		boolean blockScroll = e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL;
		boolean vert = !e.isShiftDown();
		boolean upleft = e.getWheelRotation() < 0;
		Adjustable hScroll = getHAdjustable();
		Adjustable vScroll = getHAdjustable();
		if (vert) {
			if (blockScroll)
				amount = vScroll.getBlockIncrement();
			else
				amount = vScroll.getUnitIncrement() * amount;
			if (upleft) {
				amount = -amount;
				if (vScroll.getValue() == 0)
					amount = 0;
			}
			if (amount != 0) {
				vScroll.setValue(vScroll.getValue() + amount);
				window.sizeDisplay();
			}
		}
		else {
			if (blockScroll)
				amount = hScroll.getBlockIncrement();
			if (upleft) {
				amount = -amount;
				if (hScroll.getValue() == 0)
					amount = 0;
			}
			hScroll.setValue(hScroll.getValue() + amount);
			window.sizeDisplay();
		}

	}

}

/* ======================================================================== */
/*
 * this is an attempt to get around the bug in OS X java 1.4+ in which ScrollPane scrollbars don't return their position and don't notify of adjustments. This faux-ScrollPane works reasonably well but is slower and has some graphical artifacts
 */

class TreeScrollPane extends Panel implements MouseWheelListener, KeyListener { // HANDMADETreeScrollPane
	BasicTreeWindow window;

	TWScroll hScroll, vScroll;

	Panel port;

	Component treeDisplay;

	public TreeScrollPane(BasicTreeWindow window) {
		super();
		hScroll = new TWScroll(this, Scrollbar.HORIZONTAL, 0, 2, 0, 0);
		vScroll = new TWScroll(this, Scrollbar.VERTICAL, 0, 2, 0, 0);
		setLayout(new BorderLayout());
		add(hScroll, BorderLayout.SOUTH);
		add(vScroll, BorderLayout.EAST);
		add(port = new Panel(), BorderLayout.CENTER);
		addMouseWheelListener(this);
		//addKeyListener(this);
		port.setLayout(null);
		doLayout();

		this.window = window;
	}

	public void addTreeDisplay(Component c) {
		port.add(c);
		treeDisplay = c;
	}

	public boolean isFauxScrollPane() {
		return true;
	}

	public Adjustable getHAdjustable() {
		return hScroll;
	}

	public Adjustable getVAdjustable() {
		return vScroll;
	}

	public void setHMinMax(int min, int max) {
		if (min < 0)
			min = 0;
		if (max < min)
			max = min;
		boolean touch = hScroll.setMinimumWithResetWarning(min);
		touch = hScroll.setMaximumWithResetWarning(max) || touch;
		if (touch)
			scrollTouched(null, 0);
		hScroll.setVisible(min != max);

		if (min == max)
			treeDisplay.setLocation(0, treeDisplay.getLocation().y);

	}

	public void setVMinMax(int min, int max) {
		if (min < 0)
			min = 0;
		if (max < min)
			max = min;
		boolean touch = vScroll.setMinimumWithResetWarning(min);
		touch = vScroll.setMaximumWithResetWarning(max) || touch;
		if (touch)
			scrollTouched(null, 0);

		vScroll.setVisible(min != max);

		if (min == max)
			treeDisplay.setLocation(treeDisplay.getLocation().x, 0);
	}

	public void scrollTouched(TWScroll scroll, int value) {
		window.setOrigin(hScroll.getValue(), vScroll.getValue(), false);
		constrainTreeDisplay();
	}

	public Dimension getViewportSize() {
		return new Dimension(port.getWidth(), port.getHeight());
	}

	public int getContentsWidth() {
		return port.getWidth();
	}

	public int getContentsHeight() {
		return port.getHeight();
	}

	public Point getScrollPosition() {
		return new Point(hScroll.getValue(), vScroll.getValue());
	}

	public void constrainTreeDisplay() { // post 2. 6 needed at least for OS X as workaround for JVM failure to respect bounds of containing panel
		if (treeDisplay == null)
			return;
		int w = port.getWidth();
		int h = port.getHeight();
		int wReduce = 0;
		int hReduce = 0;

		if (treeDisplay.getWidth() + treeDisplay.getX() != w)
			wReduce = treeDisplay.getWidth() + treeDisplay.getX() - w;
		if (treeDisplay.getHeight() + treeDisplay.getY() != h)
			hReduce = treeDisplay.getHeight() + treeDisplay.getY() - h;
		if (wReduce != 0 || hReduce != 0)
			treeDisplay.setSize(treeDisplay.getWidth() - wReduce, treeDisplay.getHeight() - hReduce);
	}

	public void setSize(int w, int h) {
		super.setSize(w, h);
		doLayout();
		constrainTreeDisplay();
		window.checkPanelPositionsLegal();

	}

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		doLayout();
		constrainTreeDisplay();
		window.checkPanelPositionsLegal();

	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int amount = e.getScrollAmount() * 2;
		boolean blockScroll = e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL;
		boolean vert = !e.isShiftDown();
		boolean upleft = e.getWheelRotation() < 0;
		if (vert) {
			if (blockScroll)
				amount = vScroll.getBlockIncrement();
			else
				amount = vScroll.getUnitIncrement() * amount;
			if (upleft) {
				amount = -amount;
				if (vScroll.getValue() == 0)
					amount = 0;
			}
			if (amount != 0) {
				vScroll.setValue(vScroll.getValue() + amount);
				window.sizeDisplay();
			}
		}
		else {
			if (blockScroll)
				amount = hScroll.getBlockIncrement();
			if (upleft) {
				amount = -amount;
				if (hScroll.getValue() == 0)
					amount = 0;
			}
			hScroll.setValue(hScroll.getValue() + amount);
			window.sizeDisplay();
		}

	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyPressed(KeyEvent e) {
	    int keyCode = e.getKeyCode();
	    int amount = 0;
	    int scale = 1;
	    if (e.isAltDown())
	    	scale=5;

	    switch( keyCode ) { 
	        case KeyEvent.VK_UP:
				amount = vScroll.getBlockIncrement();
				vScroll.setValue(vScroll.getValue() - amount*scale);
				window.sizeDisplay();
	            break;
	        case KeyEvent.VK_DOWN:
				amount = vScroll.getBlockIncrement();
				vScroll.setValue(vScroll.getValue() + amount*scale);
				window.sizeDisplay();
	            break;
	        case KeyEvent.VK_LEFT:
				amount = hScroll.getBlockIncrement();
				hScroll.setValue(hScroll.getValue() - amount*scale);
				window.sizeDisplay();
	            break;
	        case KeyEvent.VK_RIGHT :
				amount = hScroll.getBlockIncrement();
				hScroll.setValue(hScroll.getValue() + amount*scale);
				window.sizeDisplay();
	            break;
	     }
	} 
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}

/* ======================================================================== */
class TWScroll extends MesquiteScrollbar {
	TreeScrollPane tsp;

	int orientation;

	public TWScroll(TreeScrollPane tsp, int orientation, int value, int visible, int min, int max) {
		super(orientation, value, visible, min, max);

		this.orientation = orientation;
		this.tsp = tsp;
	}

	public void setValue(int v) {
		super.setValue(v);
	}

	public boolean setMinimumWithResetWarning(int m) {
		boolean resetNeeded = false;
		if (getValue() < m) {
			setValue(m);
			resetNeeded = true;
		}
		super.setMinimum(m);
		return resetNeeded;
	}

	public boolean setMaximumWithResetWarning(int m) {
		boolean resetNeeded = false;
		if (getValue() > m) {
			setValue(m);
			resetNeeded = true;
		}
		super.setMaximum(m);
		return resetNeeded;
	}

	public void scrollTouched() {
		int currentValue = getValue();
		tsp.scrollTouched(this, currentValue);
	}

	public boolean processDuringAdjustment() {
		return true;
	}

	public void print(Graphics g) {
	}
}

/*
 * ======================================================================== * class LockPanel extends MousePanel { Image lockClosed, lockOpen, lockImage; BasicTreeWindow window; int state; public LockPanel(BasicTreeWindow window){ super(); this.window = window; lockClosed = MesquiteImage.getImage(MesquiteModule.getRootPath() + "images/lockClosed.gif"); lockOpen = MesquiteImage.getImage(MesquiteModule.getRootPath() + "images/lockOpen.gif"); setLockState(0); } public void setLockState(int state){ this.state = state; if (state == 0) lockImage = lockClosed; else if (state ==1)
 * lockImage = lockClosed; else if (state == 2) lockImage = lockOpen; }
 * 
 * public void paint(Graphics g) { //^^^ if (MesquiteWindow.checkDoomed(this)) return; g.drawImage(lockImage, 0, -1, this); g.drawLine(0, getBounds().height-1, getBounds().width, getBounds().height-1);
 * 
 * MesquiteWindow.uncheckDoomed(this); } public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) { window.lockTouched(); repaint(); } }
 */

/* ======================================================================== */
class MessagePanel extends Panel {
	String treeMessage = "";

	String treeSourceAddendum = "";

	BasicTreeWindowMaker ownerModule;

	boolean showDiamond = false;

	boolean indicateModified;

	String modifiedString = "";

	Polygon poly;

	int left = 4;

	int top = 4;

	int s = 8;

	public MessagePanel(BasicTreeWindowMaker ownerModule) { // in future pass general MesquiteWindow
		super();
		treeMessage = "";
		poly = new Polygon();
		poly.xpoints = new int[4];
		poly.ypoints = new int[4];
		poly.npoints = 0;
		poly.addPoint(left, top + s / 2);
		poly.addPoint(left + s / 2, top);
		poly.addPoint(left + s, top + s / 2);
		poly.addPoint(left + s / 2, top + s);
		poly.npoints = 4;
		this.ownerModule = ownerModule;
		setBackground(ColorTheme.getInterfaceElement());

	}

	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.drawRect(0, 0, getBounds().width - 1, getBounds().height - 1);
		if (showDiamond) {
			g.fillPolygon(poly);
			if (treeMessage != null)
				g.drawString(modifiedString + treeMessage + treeSourceAddendum, left + s + 4, 12);
		}
		else if (treeMessage != null)
			g.drawString(modifiedString + treeMessage + treeSourceAddendum, 4, 12);
		MesquiteWindow.uncheckDoomed(this);
	}

	public void setMessage(String s) {
		treeMessage = s;
		repaint();
	}

	public String getFullMessage() {
		return modifiedString + treeMessage + treeSourceAddendum;
	}

	void setHighlighted(boolean edited) {
		this.showDiamond = edited;
		if (edited) {
			if (!MesquiteThread.isScripting())
				modifiedString = "Edited, based on ";
			else
				modifiedString = "Modified, based on ";
		}
		else
			modifiedString = "";
		if (edited)
			setBackground(ColorDistribution.lightGreen);
		else
			setBackground(ColorTheme.getInterfaceElement());

		if (treeMessage != null)
			ownerModule.magnifyExtra.name = modifiedString + treeMessage;
		else
			ownerModule.magnifyExtra.name = null;
		if (ownerModule.treeSourceTask != null && !edited) {
					
			String s = ownerModule.treeSourceTask.getNameAndParameters();
			if (!StringUtil.blank(s))
				treeSourceAddendum = "   [" + s + "]";
		}
		else
			treeSourceAddendum = "";
		repaint();
	}
}

/* ======================================================================== */
/* New code added Feb.07 oliver */// TODO: delete new code comments
/** A panel at the bottom of a tree window that can be used for buttons & messages. */
class ControlStrip extends Panel {
	BasicTreeWindowMaker ownerModule;

	Vector buttons;

	int left = 4;

	int top = 4;

	int s = 8;

	public ControlStrip(BasicTreeWindowMaker ownerModule) { // in future pass general MesquiteWindow
		super();
		setLayout(null);
		buttons = new Vector();
		this.ownerModule = ownerModule;
		setBackground(ColorTheme.getInterfaceElement());
	}

	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.drawRect(0, 0, getBounds().width - 1, getBounds().height - 1);
		MesquiteWindow.uncheckDoomed(this);
	}

	public void addButton(MesquiteButton s) {
		if (buttons.indexOf(s) < 0) {
			buttons.addElement(s);
			add(s);
		}
		s.setVisible(true);
		resetLocs();
		repaint();
	}

	public void removeButton(MesquiteButton s) {
		buttons.removeElement(s);
		remove(s);
		s.setVisible(false);
		resetLocs();
		repaint();
	}

	private void resetLocs() {
		int x = 4;
		for (int i = 0; i < buttons.size(); i++) {
			MesquiteButton b = (MesquiteButton) buttons.elementAt(i);
			b.setLocation(x, 2);
			x += 20;
		}
	}
}

/* End new code added Feb.07 oliver */
/* ======================================================================== */
class RecentButton extends MousePanel {
	BasicTreeWindow window;

	static int width = 9; // 40;

	static int height = 36;

	static Image[] recentImage;

	static int numImages = 11;

	public RecentButton(BasicTreeWindow w) {
		this.window = w;
		// setBackground(ColorDistribution.lightBlue);
		setSize(width, height);
		if (recentImage == null) {
			recentImage = new Image[numImages];
			for (int i = 0; i < numImages; i++)
				recentImage[i] = MesquiteImage.getImage(window.getOwnerModule().getPath() + "recent" + MesquiteFile.fileSeparator + "recent" + (i) + ".gif");
		}
	}

	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		MesquitePopup popup = new MesquitePopup(this);
		popup.addItem("Recent Edited Trees", (MesquiteCommand) null, null);
		popup.addItem("-", (MesquiteCommand) null, null);
		if (window.recentEditedTrees.size() == 0)
			popup.addItem("None Available", (MesquiteCommand) null, null);
		else {
			popup.addItem("(Newest)", (MesquiteCommand) null, null);
			for (int i = window.recentEditedTrees.size() - 1; i >= 0; i--) {
				Tree t = window.recentEditedTrees.getTree(i);
				popup.addItem(Integer.toString(i + 1) + "  " + t.getName(), MesquiteModule.makeCommand("showRecentEdited", window), Integer.toString(i));
			}
			popup.addItem("(Oldest)", (MesquiteCommand) null, null);
		}
		popup.showPopup(x + 4, y + 6);
	}

	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		int i = window.recentEditedTrees.size();
		if (i >= numImages)
			i = numImages - 1;
		g.drawImage(recentImage[i], 0, 0, this);
		MesquiteWindow.uncheckDoomed(this);
	}

}

/* ======================================================================== */
class TreeWindowPalette extends ToolPalette {
	BasicTreeWindow treeWindow;

	MiniScroll paletteScroll;

	RecentButton recentButton;

	public BirdsEyePanel birdsEyeBox;

	BirdsEyeExtra birdsEyeExtra;

	int fieldWidth, fieldHeight;

	int scrollHeight = 20;

	public TreeWindowPalette(BasicTreeWindowMaker ownerModule, BasicTreeWindow containingWindow) { // in future pass general MesquiteWindow
		super(ownerModule, containingWindow, 2);
		treeWindow = containingWindow;
		Taxa taxa = treeWindow.taxa;
		if (taxa == null)
			taxa = ownerModule.taxa;
		add(paletteScroll = new MiniScroll(treeWindow.setTreeNumberCommand, true, 1, 1, MesquiteTree.toExternal(ownerModule.treeSourceTask.getNumberOfTrees(taxa) - 1), "tree"));
		paletteScroll.setBackground(Color.green);
		paletteScroll.setLocation((getWidth() - paletteScroll.totalWidth + RecentButton.width) / 2, scrollHeight);
		paletteScroll.setVisible(true);
		add(recentButton = new RecentButton(containingWindow));
		recentButton.setLocation(paletteScroll.getBounds().x - 12, paletteScroll.getBounds().y);

		recentButton.setVisible(true);
		setFieldSize(treeWindow.totalTreeFieldWidth, treeWindow.totalTreeFieldHeight);
	}

	/* ................................................................................................................. */
	public void setCurrentTool(MesquiteTool tool) {
		if (tool != null && !tool.getEnabled())
			return;
		if (tool instanceof TreeTool)
			treeWindow.currentTreeTool = (TreeTool) tool;
		super.setCurrentTool(tool);
		treeWindow.requestFocus();
	}

	void setUpBirdsEye() {
		birdsEyeBox = new BirdsEyePanel(treeWindow, treeWindow.treeDisplay.getTreeDrawing());
		add(birdsEyeBox);
		if (treeWindow.usingPane)
			birdsEyeBox.setVisible(true);
		else
			birdsEyeBox.setVisible(false);
		birdsEyeExtra = new BirdsEyeExtra(ownerModule, treeWindow.treeDisplay, treeWindow);
		treeWindow.treeDisplay.addExtra(birdsEyeExtra); // TODO: need to remove later?
	}

	public int minWidth() {
		int w = super.minWidth();
		int w2 = paletteScroll.getBounds().width + recentButton.width + 20;
		if (w < w2)
			return w2;
		return w;
	}

	public void setFieldSize(int width, int height) {
		if (treeWindow == null || birdsEyeBox == null)
			return;
		paletteScroll.setLocation((getWidth() - paletteScroll.totalWidth + RecentButton.width) / 2, scrollHeight);

		recentButton.setLocation(paletteScroll.getBounds().x - 12, paletteScroll.getBounds().y);

		if (width != 0 && height != 0) {
			int birdWidth = getWidth() - 8;
			int birdHeight = height * birdWidth / width;
			int deepest = getDeepestButton();
			if (deepest > getBounds().height - 8 - birdHeight) { // need to shrink birdsEyeBox to fit
				birdHeight = getBounds().height - 8 - deepest;
				birdWidth = width * birdHeight / height;
			}
			birdsEyeBox.setBounds(4, getBounds().height - 8 - birdHeight, birdWidth, birdHeight);
		}
	}

	public void paint(Graphics g) { // ^^^
		if (MesquiteWindow.checkDoomed(this))
			return;
		super.paint(g);

		g.setColor(Color.black);
		g.drawString("Tree #", paletteScroll.getBounds().x, paletteScroll.getBounds().y - 3);
		MesquiteWindow.uncheckDoomed(this);

	}

	public void repaintBirdsEye() {
		birdsEyeBox.repaint();
	}

	public void dispose() {
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w != null)
			w.waitUntilDisposable();
		birdsEyeExtra.dispose();
		birdsEyeBox.dispose();
		treeWindow = null;
		remove(paletteScroll);
		remove(birdsEyeBox);
		super.dispose();
	}

}

/* =========================================== */
/* =========================================== */

class BasicTreeStatisticsPanel extends TreeInfoExtraPanel {
	StringInABox statsBox;

	String treeStats = null;

	int neededHeight = 20;

	public BasicTreeStatisticsPanel(ClosablePanelContainer container) {
		super(container, "Basic Tree Stats");
		statsBox = new StringInABox("", null, 50);
		setOpen(true);
	}

	public void setTree(Tree tree) {
		super.setTree(tree);
		// number of terminal taxa
		// any polytomies
		// zero length branches (but only if present)
		// total path length
		// branch lengths absent
		adjustMessage();
		container.requestHeightChange(this);
		repaint();
	}

	public void setNode(int node) {
		super.setNode(node);
		repaint();
	}

	void adjustMessage() {
		if (tree == null)
			treeStats = "no tree";
		else {
			int numTerms = tree.numberOfTerminalsInClade(tree.getRoot());
			int numTermsTotal = tree.getTaxa().getNumTaxa();
			if (numTermsTotal == numTerms)
				treeStats = "All " + numTerms + " taxa included.\n";
			else
				treeStats = "" + numTerms + " of " + numTermsTotal + " taxa included.\n";
			if (tree.hasPolytomies(tree.getRoot()))
				treeStats += "Has polytomies.\n";
			if (!tree.hasBranchLengths())
				treeStats += "No branch lengths specified.\n";
			else {

			}
		}
	}

	public int getRequestedHeight(int width) {
		if (!isOpen())
			return MINHEIGHT;
		statsBox.setFont(getFont());
		statsBox.setString(treeStats);
		statsBox.setWidth(width - 4);
		neededHeight = statsBox.getHeight();
		return neededHeight + MINHEIGHT;
	}

	public void paint(Graphics g) {
		super.paint(g);
		// g.drawString("hello", 8, MINHEIGHT+20);
		statsBox.setWidth(getBounds().width - 4);
		statsBox.setFont(g.getFont());
		statsBox.setString(treeStats);
		statsBox.draw(g, 4, MINHEIGHT);
	}
}/* =========================================== */

class TreeSourceInfoPanel extends TreeInfoExtraPanel {
	StringInABox statsBox;

	String treeSourceInfo = null;

	int neededHeight = 20;

	BasicTreeWindow w;

	public TreeSourceInfoPanel(ClosablePanelContainer container, BasicTreeWindow w) {
		super(container, "Tree Source");
		this.w = w;
		statsBox = new StringInABox("", null, 50);
		setOpen(true);
	}

	public void setTree(Tree tree) {
		super.setTree(tree);
		adjustMessage();
		container.requestHeightChange(this);
		repaint();
	}

	void adjustMessage() {
		if (tree == null)
			treeSourceInfo = "";
		else {
			TreeSource treeSourceTask = w.treeSourceTask;
			treeSourceInfo = treeSourceTask.getTreeSourceInfo(tree.getTaxa());
		}
	}

	public int getRequestedHeight(int width) {
		if (!isOpen())
			return MINHEIGHT;
		statsBox.setFont(getFont());
		statsBox.setString(treeSourceInfo);
		statsBox.setWidth(width - 4);
		neededHeight = statsBox.getHeight();
		return neededHeight + MINHEIGHT;
	}

	public void paint(Graphics g) {
		super.paint(g);
		// g.drawString("hello", 8, MINHEIGHT+20);
		statsBox.setWidth(getBounds().width - 4);
		statsBox.setFont(g.getFont());
		statsBox.setString(treeSourceInfo);
		statsBox.draw(g, 4, MINHEIGHT);
	}
}

/* =========================================== */
class BranchInfoPanel extends TreeInfoExtraPanel {
	StringInABox statsBox;

	String message = null;

	int neededHeight = 20;

	String attachmentsMessage = "";

	Image query;

	public BranchInfoPanel(ClosablePanelContainer container) {
		super(container, "Branch/Node Info");
		query = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "queryGray.gif");
		statsBox = new StringInABox("", null, 50);
	}

	public void setTree(Tree tree) {
		super.setTree(tree);
		adjustMessage();
		container.requestHeightChange(this);
		repaint();
	}

	public void setNode(int node) {
		super.setNode(node);
		adjustMessage();
		container.requestHeightChange(this);
		repaint();
	}

	private void adjustMessage() {
		attachmentsMessage = "";
		if (tree == null)
			message = "no tree";
		else {
			message = "";
			if (tree.nodeExists(node)) {
				if (!tree.branchLengthUnassigned(node))
					attachmentsMessage += "Branch length: " + MesquiteDouble.toString(tree.getBranchLength(node)) + "\n";

				for (int i = 0; i < tree.getNumberAssociatedDoubles(); i++) {
					DoubleArray d = tree.getAssociatedDoubles(i);
					NameReference nr = d.getNameReference();
					attachmentsMessage += nr.getValue();
					attachmentsMessage += ": " + MesquiteDouble.toString(d.getValue(node));
					attachmentsMessage += "\n";

				}
				for (int i = 0; i < tree.getNumberAssociatedLongs(); i++) {
					LongArray d = tree.getAssociatedLongs(i);
					NameReference nr = d.getNameReference();
					attachmentsMessage += nr.getValue();
					attachmentsMessage += ": " + MesquiteLong.toString(d.getValue(node));
					attachmentsMessage += "\n";
				}
				for (int i = 0; i < tree.getNumberAssociatedObjects(); i++) {
					ObjectArray d = tree.getAssociatedObjects(i);
					NameReference nr = d.getNameReference();
					if (d.getValue(node) instanceof String) {
						attachmentsMessage += nr.getValue();
						attachmentsMessage += ": " + (d.getValue(node));
						attachmentsMessage += "\n";
					}
				}
				if (StringUtil.blank(attachmentsMessage)) {
					message = "\n ";
				}
				message += attachmentsMessage;
			}
		}
	}

	public int getRequestedHeight(int width) {
		if (!isOpen())
			return MINHEIGHT;
		statsBox.setFont(getFont());
		statsBox.setString(message);
		statsBox.setWidth(width - 4);
		neededHeight = statsBox.getHeight();
		return neededHeight + MINHEIGHT;
	}

	public void paint(Graphics g) {
		super.paint(g);
		super.paint(g);
		g.drawImage(query, getWidth() - 20, 4, this);
		statsBox.setWidth(getBounds().width - 4);
		statsBox.setFont(g.getFont());
		statsBox.setString(message);
		statsBox.draw(g, 4, MINHEIGHT);
	}

	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (y < MINHEIGHT && (x > getWidth() - 20)) {
			MesquiteTrunk.mesquiteTrunk.alert("Attachments:  Attachments to the tree or its nodes are pieces of information like branch lengths, lineage widths, assigned colors, and so on. Move cursor over nodes to see information attached there."); // query button hit
		}
		else
			super.mouseUp(modifiers, x, y, tool);
	}
}

/* =========================================== */

/* ======================================================================== */
class TreeInfoPanel extends MousePanel implements ClosablePanelContainer {
	static final int width = 200;

	// static final int height = 66;
	String title = null;

	String explanation = null;

	StringInABox titleBox, explanationBox;

	int[] locs;

	BasicTreeWindow w;

	Image goaway;

	Tree tree;

	Vector extras = new Vector();

	Font titleFont;

	String treeName, sourceName;

	boolean storedTrees;

	Image add = null;

	BasicTreeStatisticsPanel btsp;

	BranchInfoPanel ap;

	TreeSourceInfoPanel tsp;

	public TreeInfoPanel(BasicTreeWindow w) {
		super();
		this.w = w;
		add = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "addGray.gif");
		setLayout(null);
		addExtraPanel(btsp = new BasicTreeStatisticsPanel(this));
		addExtraPanel(ap = new BranchInfoPanel(this));
		addExtraPanel(tsp = new TreeSourceInfoPanel(this, w));
		setBackground(ColorDistribution.veryLightGray);
		setFont(new Font("SansSerif", Font.PLAIN, 12));
		titleFont = new Font("SansSerif", Font.BOLD, 12);
		titleBox = new StringInABox("", null, width);
		explanationBox = new StringInABox("", null, width);
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "minimizeTransparent.gif");
	}

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("btspOpen " + btsp.isOpen());
		temp.addLine("apOpen " + ap.isOpen());
		return temp;
	}

	public void employeeQuit(MesquiteModule m) {
		if (m == null)
			return;
		// zap values panel line

	}

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets attachment panel open", null, commandName, "apOpen")) {
			ap.setOpen(arguments == null || arguments.equalsIgnoreCase("true"));
		}
		else if (checker.compare(this.getClass(), "Sets the basic statistics panel open", null, commandName, "btspOpen")) {
			btsp.setOpen(arguments == null || arguments.equalsIgnoreCase("true"));
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	public ClosablePanel getPrecedingPanel(ClosablePanel panel) {
		int i = extras.indexOf(panel);
		if (i > 0)
			return (ClosablePanel) extras.elementAt(i - 1);
		return null;
	}

	void addExtraPanel(TreeInfoExtraPanel p) {
		extras.addElement(p);
		add(p);
		resetSizes(getWidth(), getHeight());
		p.setVisible(true);
		if (tree != null)
			p.setTree(tree);
	}

	void setTree(Tree tree) {
		if (tree == null)
			return;
		this.tree = tree;
		title = "Tree: " + tree.getName();
		for (int i = 0; i < extras.size(); i++) {
			TreeInfoExtraPanel panel = ((TreeInfoExtraPanel) extras.elementAt(i));
			if (tree != null)
				panel.setTree(tree);
		}
		setHighlighted(storedTrees);
	}

	void taxonEnter(int it) {
		for (int i = 0; i < extras.size(); i++) {
			TreeInfoExtraPanel panel = ((TreeInfoExtraPanel) extras.elementAt(i));
			panel.taxonEnter(it);
		}
	}

	void taxonExit(int it) {
		for (int i = 0; i < extras.size(); i++) {
			TreeInfoExtraPanel panel = ((TreeInfoExtraPanel) extras.elementAt(i));
			panel.taxonExit(it);
		}
	}

	void taxonTouch(int it) {
		for (int i = 0; i < extras.size(); i++) {
			TreeInfoExtraPanel panel = ((TreeInfoExtraPanel) extras.elementAt(i));
			panel.taxonTouch(it);
		}
	}

	void branchEnter(int node) {
		for (int i = 0; i < extras.size(); i++) {
			TreeInfoExtraPanel panel = ((TreeInfoExtraPanel) extras.elementAt(i));
			panel.branchEnter(node);
		}
	}

	void branchExit(int node) {
		for (int i = 0; i < extras.size(); i++) {
			TreeInfoExtraPanel panel = ((TreeInfoExtraPanel) extras.elementAt(i));
			panel.branchExit(node);
		}
	}

	void branchTouch(int node) {
		for (int i = 0; i < extras.size(); i++) {
			TreeInfoExtraPanel panel = ((TreeInfoExtraPanel) extras.elementAt(i));
			panel.branchTouch(node);
		}
	}

	void setTreeAndSourceName(String treeName, String sourceName) {
		this.treeName = treeName;
		this.sourceName = sourceName;
		setHighlighted(storedTrees);
	}

	public void setHighlighted(boolean storedTrees) {
		this.storedTrees = storedTrees;
		boolean edited = w.treeEdited;
		if (edited && storedTrees)
			title = "Tree: Edited, based on " + treeName;
		else if (edited)
			title = "Tree: Edited, based on " + treeName;
		else
			title = "Tree: " + treeName;
		if (edited) {
			if (storedTrees)
				explanation = "Tree is modified from one of the stored trees ";
			else
				explanation = "Tree has been edited; it is not directly from the tree source.";
		}
		else {
			if (storedTrees)
				explanation = "Tree is stored in the project.";
			else
				explanation = "Tree is not stored in the project.  The tree is from: " + sourceName;
		}
		resetSizes(getWidth(), getHeight());
		repaint();
	}

	public void requestHeightChange(ClosablePanel panel) {
		resetSizes(getWidth(), getHeight());
		repaint();
	}

	public void paint(Graphics g) {
		Color c = g.getColor();

		int vertical = 2;
		if (title != null) {
			g.setColor(ColorTheme.getInterfaceElement());
			g.fillRect(0, 0, getBounds().width, titleBox.getHeight() + 8);
			g.setColor(ColorTheme.getInterfaceTextContrast());
			titleBox.draw(g, 20, vertical);
			g.setColor(Color.black);
			vertical += 8 + titleBox.getHeight();
		}

		g.drawImage(goaway, 2, 2, this);
		g.drawLine(0, vertical - 4, getBounds().width, vertical - 4);
		if (explanation != null) {
			g.setColor(Color.white);
			g.fillRect(0, vertical, getBounds().width, 8 + explanationBox.getHeight());
			g.setColor(Color.black);
			explanationBox.draw(g, 4, vertical);
			vertical += 8 + explanationBox.getHeight();
		}
		g.setColor(Color.darkGray);
		g.fillRect(0, totalVertical, getBounds().width, 2);
		// WHEN CALCULATIONS CAN BE ADDED
		// g.drawImage(add, 2, totalVertical+4, this);
		g.setColor(c);

	}

	int totalVertical = 0;

	/* to be used by subclasses to tell that panel touched */
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (x < 30 && y > totalVertical + 4 && y < totalVertical + 20) {
			// WHEN CALCULATIONS CAN BE ADDED
			// MesquiteTrunk.mesquiteTrunk.alert("Sorry, doesn't do anything yet");
		}
	}

	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (x < 16 && y < 16)
			w.treeInfoPanelGoAway();

	}

	void resetSizes(int w, int h) {
		int vertical = 2;
		if (title != null) {
			titleBox.setWidth(w - 20);
			titleBox.setFont(titleFont);
			titleBox.setString(title);
			vertical += 8 + titleBox.getHeight();
		}
		if (explanation != null) {
			explanationBox.setWidth(w - 4);
			explanationBox.setFont(getFont());
			explanationBox.setString(explanation);
			vertical += 8 + explanationBox.getHeight();
		}
		for (int i = 0; i < extras.size(); i++) {
			ClosablePanel panel = ((ClosablePanel) extras.elementAt(i));
			int requestedlHeight = panel.getRequestedHeight(w);

			panel.setBounds(0, vertical, w, requestedlHeight);
			vertical += requestedlHeight;
		}
		totalVertical = vertical;
	}

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		resetSizes(w, h);
	}

	public void setSize(int w, int h) {
		super.setSize(w, h);
		resetSizes(w, h);
	}
}

/* ======================================================================== */
/* scrollbar for tree */
class TreeScroll extends MesquiteScrollbar {
	BasicTreeWindow basicTreeWindow;

	public TreeScroll(BasicTreeWindow basicTreeWindow, int orientation, int value, int visible, int min, int max) {
		super(orientation, value, visible, min, max);
		this.basicTreeWindow = basicTreeWindow;
	}

	public void scrollTouched() {
		int currentValue = getValue();
	}

	public void dispose() {
		basicTreeWindow = null;
		// super.dispose();
	}
}

/* ======================================================================== */
class BirdsEyeExtra extends TreeDisplayExtra {
	BasicTreeWindow treeWindow;

	public BirdsEyeExtra(MesquiteModule ownerModule, TreeDisplay treeDisplay, BasicTreeWindow treeWindow) {
		super(ownerModule, treeDisplay);
		this.treeWindow = treeWindow;
	}

	/* ................................................................................................................. */
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (treeWindow.usingPane && treeWindow.palette != null && treeWindow.palette.birdsEyeBox != null)
			treeWindow.palette.birdsEyeBox.repaint();
	}

	public void setTree(Tree tree) {
	}

	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

	public void dispose() {
		treeWindow = null;
		super.dispose();
	}
}

/* ======================================================================== */
class MagnifyExtra extends TreeDisplayExtra {
	Image image;

	BasicTreeWindowMaker om;

	String name = "";

	public MagnifyExtra(BasicTreeWindowMaker ownerModule, TreeDisplay treeDisplay, TreeTool tool) {
		super(ownerModule, treeDisplay);
		om = ownerModule;
		image = MesquiteImage.getImage(tool.getImagePath());
	}

	/* ................................................................................................................. */
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (drawnRoot != tree.getRoot()) {
			TreeDrawing td = treeDisplay.getTreeDrawing();
			g.drawImage(image, (int)td.x[drawnRoot], (int)td.y[drawnRoot], treeDisplay);
		}
	}

	public void setTree(Tree tree) {
	}

	/** Returns any strings to be appended to taxon name. */
	public String getTaxonStringAddition(Taxon taxon) {
		Taxa taxa = taxon.getTaxa();
		int which = taxa.whichTaxonNumber(taxon);
		String s = taxa.getAnnotation(which);
		if (!StringUtil.blank(s))
			return "*";
		return null;
	}

	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {

		if (name != null && om.printNameOnTree.getValue())
			g.drawString(name, 50, treeDisplay.getBounds().height - 20);
	}
}

/* ======================================================================== */
class BirdsEyePanel extends MesquitePanel {
	TreeDrawing treeDrawing;

	BasicTreeWindow treeWindow;

	int offsetX = 0;

	int offsetY = 0;

	int origTouchX, origTouchY, dragOffsetX, dragOffsetY;

	int rootX, rootY;

	Rectangle vis;

	public BirdsEyePanel(BasicTreeWindow treeWindow, TreeDrawing treeDrawing) {
		this.treeDrawing = treeDrawing;
		origTouchX = origTouchY = -1;
		this.treeWindow = treeWindow;
		setBackground(ColorTheme.getInterfaceBackgroundPale());
		vis = new Rectangle();
	}

	public void dispose() {
		treeWindow = null;
		treeDrawing = null;
		// super.dispose();
	}

	public void sketchTree(Tree tree, int N, Graphics g) {
		int nodeX = getTransformedX(N);
		int nodeY = getTransformedY(N);
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			int daughterX = getTransformedX(d);
			int daughterY = getTransformedY(d);
			g.drawLine(nodeX, nodeY, daughterX, daughterY);
			sketchTree(tree, d, g);
		}

	}

	public void setFieldSize(int width, int height) {

	}

	private int getTransformedX(int node) {
		return (int)(getBounds().width * treeDrawing.x[node] / (treeWindow.treeDisplay.getFieldWidth()));    //integer node loc approximation
	}

	private int getTransformedY(int node) {
		return (int)(getBounds().height * treeDrawing.y[node] / (treeWindow.treeDisplay.getFieldHeight()));    //integer node loc approximation
	}

	public void paint(Graphics g) {
		if (treeWindow == null || treeWindow.treeDisplay == null)
			return;
		if (MesquiteWindow.checkDoomed(this))
			return;
		Tree tree = treeWindow.treeDisplay.getTree();
		if (tree == null) {
			MesquiteWindow.uncheckDoomed(this);
			return;
		}
		treeDrawing = treeWindow.treeDisplay.getTreeDrawing();
		int drawnRoot = treeDrawing.getDrawnRoot();
		if (tree != null && !tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		g.setColor(Color.white);
		rootX = getTransformedX(drawnRoot);
		rootY = getTransformedY(drawnRoot);
		recalcVis();
		/*
		 * int oX = -treeWindow.getOriginX(); int oY = -treeWindow.getOriginY(); vis.x=getBounds().width *oX /(treeWindow.treeDisplay.getFieldWidth()); vis.y=getBounds().height *oY /(treeWindow.treeDisplay.getFieldHeight()); if (treeWindow.treePane==null) { vis.width = getBounds().width*treeWindow.treeDisplay.getBounds().width/(treeWindow.treeDisplay.getFieldWidth()); vis.height = getBounds().height*treeWindow.treeDisplay.getBounds().height/(treeWindow.treeDisplay.getFieldHeight()); } else { vis.width =
		 * getBounds().width*treeWindow.treePane.getBounds().width/(treeWindow.treeDisplay.getFieldWidth()); vis.height = getBounds().height*treeWindow.treePane.getBounds().height/(treeWindow.treeDisplay.getFieldHeight()); }
		 */
		g.fillRoundRect(vis.x, vis.y, vis.width, vis.height, 8, 8);
		g.setColor(Color.gray);
		sketchTree(tree, drawnRoot, g);
		g.setColor(Color.black);
		g.drawRoundRect(vis.x, vis.y, vis.width, vis.height, 8, 8);
		g.drawRect(0, 0, getBounds().width - 1, getBounds().height - 1);
		g.drawRect(1, 1, getBounds().width - 3, getBounds().height - 3);
		MesquiteWindow.uncheckDoomed(this);
	}

	boolean recalcVis() {
		Rectangle oldV = new Rectangle(vis);
		int oX = treeWindow.getOriginX();
		int oY = treeWindow.getOriginY();
		vis.x = getBounds().width * oX / (treeWindow.treeDisplay.getFieldWidth());
		vis.y = getBounds().height * oY / (treeWindow.treeDisplay.getFieldHeight());
		vis.width = getBounds().width * treeWindow.getTreePaneWidth() / (treeWindow.treeDisplay.getFieldWidth());
		vis.height = getBounds().height * treeWindow.getTreePaneHeight() / (treeWindow.treeDisplay.getFieldHeight());
		vis.width = getBounds().width * treeWindow.getTreePaneWidth() / (treeWindow.treeDisplay.getFieldWidth());
		vis.height = getBounds().height * treeWindow.getTreePaneHeight() / (treeWindow.treeDisplay.getFieldHeight());
		return (oldV.x != vis.x || oldV.y != vis.y || oldV.width != vis.width || oldV.height != vis.height);
	}

	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		if (origTouchX < 0 || origTouchY < 0)
			return;
		if (MesquiteWindow.checkDoomed(this))
			return;
		Graphics g = null;
		if (GraphicsUtil.useXORMode(null, false)) {
			g = getGraphics();
			g.setXORMode(Color.white);
			g.setColor(Color.green);
			g.drawRoundRect(vis.x + dragOffsetX, vis.y + dragOffsetY, vis.width, vis.height, 8, 8);
		}
		dragOffsetX = x - origTouchX;
		dragOffsetY = y - origTouchY;
		if (GraphicsUtil.useXORMode(null, false)) {
			g.drawRoundRect(vis.x + dragOffsetX, vis.y + dragOffsetY, vis.width, vis.height, 8, 8);
			g.dispose();
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		origTouchX = x;
		origTouchY = y;
		dragOffsetX = 0;
		dragOffsetY = 0;

		boolean changed = recalcVis();
		if (changed) {
			Graphics g = getGraphics();
			update(g);
			g.dispose();
		}
		if (GraphicsUtil.useXORMode(null, false)) {
			Graphics g = getGraphics();
			g.setXORMode(Color.white);
			g.setColor(Color.green);
			g.drawRoundRect(vis.x, vis.y, vis.width, vis.height, 8, 8);
			g.dispose();
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (origTouchX < 0 || origTouchY < 0)
			return;
		if (MesquiteWindow.checkDoomed(this))
			return;
		offsetX = offsetX + dragOffsetX;
		offsetY = offsetY + dragOffsetY;
		if (GraphicsUtil.useXORMode(null, false)) {
			Graphics g = getGraphics();
			g.setXORMode(Color.white);
			g.setColor(Color.green);
			g.drawRoundRect(vis.x + dragOffsetX, vis.y + dragOffsetY, vis.width, vis.height, 8, 8);
			g.setPaintMode();
			g.dispose();
		}
		int oX = (vis.x + dragOffsetX) * (treeWindow.treeDisplay.getFieldWidth()) / (getBounds().width);
		int oY = (vis.y + dragOffsetY) * (treeWindow.treeDisplay.getFieldHeight()) / (getBounds().height);
		if (oX < 0)
			oX = 0;
		if (oY < 0)
			oY = 0;
		treeWindow.setOrigin(oX, oY, true);

		treeWindow.treeDisplay.pleaseUpdate(true);
		treeWindow.sizeDisplay();
		origTouchX = -1;
		origTouchY = -1;

		repaint();
		MesquiteWindow.uncheckDoomed(this);
	}
}

/* ======================================================================== */
class TreeWindowSelectionRectangle {
	Rectangle selectionRect;

	public TreeWindowSelectionRectangle(Graphics2D g2, int x, int y, int w, int h) {
		this.selectionRect = new Rectangle(x, y, w, h);
	}

	public static Area createAreaFromRectangle(Rectangle rect) {

		MesquitePath2DFloat path = new MesquitePath2DFloat();
		if (path.OK()) {
			path.moveTo(rect.x, rect.y);
			path.lineTo(rect.x + rect.width, rect.y);
			path.lineTo(rect.x + rect.width, rect.y + rect.height);
			path.lineTo(rect.x, rect.y + rect.height);
			path.lineTo(rect.x, rect.y);
			path.closePath();
			return path.getArea();
		}
		else {
			return null;
		}

	}

	public void drawSelectionDifference(Graphics2D g2, Component comp, int x, int y, int w, int h) {
		Rectangle newRect = new Rectangle(x, y, w, h);
		GraphicsUtil.fixRectangle(newRect);
		Area newArea = createAreaFromRectangle(newRect);
		Area differenceArea = createAreaFromRectangle(selectionRect);

		if (differenceArea != null) {
			differenceArea.exclusiveOr(newArea);
			Shape oldClip = g2.getClip();
			g2.setClip(differenceArea);
			if (selectionRect.contains(newRect)) { // original rect is bigger
				comp.repaint(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
				GraphicsUtil.fillTransparentSelectionRectangle(g2, x, y, w, h);
			}
			else if (newRect.contains(selectionRect)) { // new rect is bigger
				GraphicsUtil.fillTransparentSelectionArea(g2, differenceArea);
			}
			g2.setClip(oldClip);
		}

		selectionRect.setRect(newRect);

	}
}
