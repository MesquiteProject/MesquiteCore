/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BranchPropertiesList;
/*~~  */

import java.awt.Checkbox;

import mesquite.lib.Annotatable;
import mesquite.lib.Associable;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleArray;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.LongArray;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NameReference;
import mesquite.lib.Notification;
import mesquite.lib.PropertyRecord;
import mesquite.lib.Snapshot;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.TreeWindowMaker;
import mesquite.lib.table.EditorPanel;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.BranchProperty;
import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.SingleLineTextField;
import mesquite.lists.lib.ListModule;
import mesquite.lists.lib.ListWindow;
import mesquite.trees.BranchPropertyDisplayControl.BranchPropertyDisplayControl;
import mesquite.trees.lib.BranchPropertiesListAssistant;

/* ======================================================================== */
public class BranchPropertiesList extends ListModule implements Annotatable {

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(BranchPropertiesListAssistant.class, "The List of Node Associates window can display columns showing information for each taxon group.",
				"You can request that columns be shown using the Columns menu of the List of Node Associates Window. ");
	}
	MesquiteTree tree;
	NodesAssociatesListWindow myWindow = null;
	BranchPropertyDisplayControl displayModule;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		MesquiteModule twMB = findEmployerWithDuty(TreeWindowMaker.class);
		if (twMB!= null)
			twMB.addMenuItem("List of Branch/Node Properties", MesquiteModule.makeCommand("showWindow",  this));
		return true;
	}

	/*.................................................................................................................*/
	public void setTree(MesquiteTree tree){
		this.tree = tree;
		if (myWindow != null)
			myWindow.setTree(tree);
		MesquiteModule twMB = findEmployerWithDuty(TreeWindowMaker.class);
		if (twMB!= null && treeWindowMenuItem!= null){
			MesquiteWindow treeWindow = twMB.getModuleWindow(); 
			treeWindowMenuItem.setName("Tree Window: " + treeWindow.getTitle());
			resetContainingMenuBar();
		}
	}

	public String getAnnotation(){
		String annot = "Properties of branches/nodes in tree window";
		TreeWindowMaker twm = (TreeWindowMaker)findEmployerWithDuty(TreeWindowMaker.class);
		if (twm != null)
			annot += " \"" + twm.getModuleWindow().getTitle() + "\"";
		return annot + ".";
	}
	public void setAnnotation(String note, boolean notify){
	}
	/*.................................................................................................................*/
	public boolean showing(Object obj){
		return (getModuleWindow()!=null && obj == tree);
	}
	public void endJob(){
		super.endJob();
	}


	public void showListWindow(Object obj) {
		if (!(obj instanceof MesquiteTree)){
			return;
		}
		setTree((MesquiteTree)obj);
		displayModule = (BranchPropertyDisplayControl)findNearestColleagueWithDuty(BranchPropertyDisplayControl.class);
		showListWindow(false);
	}
	MesquiteMenuItemSpec treeWindowMenuItem = null;
	/*.................................................................................................................*/
	public void showListWindow(boolean hireAssistantsRegardless){
		if (myWindow != null){
			myWindow.show();
			myWindow.setVisible(true);
			if (!MesquiteThread.isScripting()){
				myWindow.setPopAsTile(true);
				myWindow.popOut(true);
			}
			return;
		}
		MesquiteModule twMB = findEmployerWithDuty(TreeWindowMaker.class);
		if (twMB!= null){
			MesquiteWindow treeWindow = twMB.getModuleWindow(); 
			treeWindowMenuItem = addMenuItem("Tree Window: " + StringUtil.shrinkInMiddle(treeWindow.getTitle(), 50), new MesquiteCommand("showTreeWindow", this));
			addMenuSeparator();
		}
		addMenuItem("Add Property to Tree...", new MesquiteCommand("addProperty", this));
		MesquiteSubmenuSpec mss = addSubmenu(null, "Convert");
		addItemToSubmenu(null, mss, "Branch Length to impliedHeight", new MesquiteCommand("branchLengthToImpliedHeight", this));
		addItemToSubmenu(null, mss, "Height to implied Branch Length", new MesquiteCommand("heightToBranchLength", this));
		addItemToSubmenu(null, mss, "Length to built-in Branch Length", new MesquiteCommand("lengthToBranchLength", this));
		addMenuSeparator();
		addItemToSubmenu(null, mss, "Node Label to Text Property...", makeCommand("nodeLabelToProperty", this));
		addItemToSubmenu(null, mss, "Property to Node Label...", makeCommand("propertyToNodeLabel", this));
		addMenuSeparator();
		addItemToSubmenu(null, mss, "Property to New Text Property...", makeCommand("propertyToTextProperty", this));
		addItemToSubmenu(null, mss, "Text Property to New Decimal Numbers Property...", makeCommand("textPropertyToNumbers", this));

		myWindow = new NodesAssociatesListWindow(this);
		setModuleWindow(myWindow);
		myWindow.getParentFrame().requestPopoutWidth(460);
		if (!MesquiteThread.isScripting()){
			myWindow.setPopAsTile(true);
			myWindow.popOut(true);
		}
		makeMenu("List");
		if (hireAssistantsRegardless || !MesquiteThread.isScripting()){
			hireAssistant("#BranchPropertiesListValue");
			hireAssistant("#BranchPropertiesListShow");
			hireAssistant("#BranchPropertiesListKind");
			hireAssistant("#BranchPropertiesListBetween");
		}
		myWindow.setTree(tree);	
		resetContainingMenuBar();
		resetAllWindowsMenus();
	}
	void hireAssistant(String name){
		BranchPropertiesListAssistant assistant = (BranchPropertiesListAssistant)hireNamedEmployee(BranchPropertiesListAssistant.class, StringUtil.tokenize(name));
		if (assistant!= null){
			myWindow.addListAssistant(assistant);
			assistant.setUseMenubar(false);
		}
	}
	public boolean suppressMenuAncestors(){
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (getModuleWindow()==null || !getModuleWindow().isVisible())
			return null;
		Snapshot temp = super.getSnapshot(file);
		//if (getModuleWindow()!=null && !getModuleWindow().isVisible())
		if (getModuleWindow()!=null){
			temp.addLine("showWindow"); 
			getModuleWindow().incorporateSnapshot(temp, file);
		}
		return temp;
	}

	void subtractAlreadyAccountedFor(ListableVector v){
		PropertyRecord.subtractIfInList(v, new BranchProperty(MesquiteTree.nodeLabelName, Associable.BUILTIN));
		PropertyRecord.subtractIfInList(v, new BranchProperty("nodelabel", Associable.STRINGS));
		PropertyRecord.subtractIfInList(v, new BranchProperty(MesquiteTree.branchLengthName, Associable.BUILTIN));
		PropertyRecord.subtractIfInList(v, new BranchProperty("branchlength", Associable.DOUBLES));
		//now removing those from tree
		DisplayableBranchProperty[] ps = myWindow.getPropertiesInTree();
		if (ps != null)
			for (int i = 0; i<ps.length; i++)
				PropertyRecord.subtractIfInList(v, ps[i]);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows the window", "[]", commandName, "showWindow")) {
			showListWindow(false);
		}
		else if (checker.compare(this.getClass(), "Shows the window", "[]", commandName, "showWindowWithAssistants")) {
			showListWindow(true);
		}
		else if (checker.compare(this.getClass(), "Shows the window", "[]", commandName, "toggleWindow")) {
			if (myWindow == null || !myWindow.isVisible())
				showListWindow(false);
			else {
				myWindow.setVisible(false);
				myWindow.popIn();
				myWindow.hide();
			}

		}
		else if (checker.compare(this.getClass(), "Returns the window", "[]", commandName, "showTreeWindow")) {
			MesquiteModule twMB = findEmployerWithDuty(TreeWindowMaker.class);
			if (twMB!= null){
				MesquiteWindow treeWindow = twMB.getModuleWindow(); 
				treeWindow.toFront();
			}
		}
		else if (checker.compare(this.getClass(), "Returns the window", "[]", commandName, "getWindow")) {
			return getModuleWindow();
		}
		else if (checker.compare(this.getClass(), "Converts length to built-in branch length", "[]", commandName, "lengthToBranchLength")) {
			NameReference lengthNR = NameReference.getNameReference("length");
			if (tree.getAssociatedDoubles(lengthNR) != null){
				lengthToBuiltInBranchLength(tree, tree.getRoot(), lengthNR);
				tree.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
			}

		}
		else if (checker.compare(this.getClass(), "Converts height to implied built-in branch length", "[]", commandName, "heightToBranchLength")) {
			NameReference heightNR = NameReference.getNameReference("height");
			if (tree.getAssociatedDoubles(heightNR) != null){
				heightToBuiltInBranchLength(tree, tree.getRoot(), heightNR);
				tree.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
			}

		}
		else if (checker.compare(this.getClass(), "Converts built-in branch length to attached property called impliedHeight", "[]", commandName, "branchLengthToImpliedHeight")) {
			if (tree.hasBranchLengths()){
				NameReference impliedHeightNR = tree.makeAssociatedDoubles("impliedHeight");
				branchLengthToImpliedHeight(tree, tree.getRoot(), tree.tallestPathAboveNode(tree.getRoot()), impliedHeightNR);
				tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
			}
		}
		else	if (checker.compare(this.getClass(), "Transforms the node label to a text property", null, commandName, "nodeLabelToProperty")) {
			nodeLabelToTextProperty();
		}
		else	if (checker.compare(this.getClass(), "Transforms the property to node labels", null, commandName, "propertyToNodeLabel")) {
			propertyToNodeLabel();
		}
		else	if (checker.compare(this.getClass(), "Transforms the property to a new text property", null, commandName, "propertyToTextProperty")) {
			propertyToTextProperty();
		}
		else	if (checker.compare(this.getClass(), "Transforms the text property to a new decimal numbers property", null, commandName, "textPropertyToNumbers")) {
			textPropertyToNumbers();
		}
		else if (checker.compare(this.getClass(), "Adds existing property", "[]", commandName, "addProperty")) {

			//The storage points for branch properties are:
			// BranchProperty.branchPropertiesSettingsVector: static, records settings in Mesquite_Folder/settings/trees/BranchPropertiesInit regarding branch properties (e.g. default kinds, betweenness)
			// DisplayableBranchProperty.branchPropertyDisplayPreferences: static, records the display preferences of branch properties
			// MesquiteProject.knownBranchProperties: instance, the properties known by the project. For interface; not saved to file.
			// The module BranchPropertiesInit is the primary manager

			ListableVector propertiesToAdd = new ListableVector();
			MesquiteString newProperty = new MesquiteString("New Property...", "");
			propertiesToAdd.addElement(newProperty, false);
			propertiesToAdd.addElement(new MesquiteString("   ", ""), false);
			//All as options known properties, except if they are already in the tree!
			ListableVector kTP =  getProject().knownBranchProperties.clone();
			subtractAlreadyAccountedFor(kTP);
			if (kTP.size()>0){
				propertiesToAdd.addElement(new MesquiteString(" — Properties in current file — ", ""), false);
				propertiesToAdd.addElements(kTP, false);
				propertiesToAdd.addElement(new MesquiteString("   ", ""), false);
			}
			kTP =  DisplayableBranchProperty.branchPropertyDisplayPreferences.clone();
			if (kTP.size() > 0){
				propertiesToAdd.addElement(new MesquiteString(" — Other known properties — ", ""), false);
				PropertyRecord.addIfNotInList(propertiesToAdd, kTP);
				PropertyRecord.addIfNotInList(propertiesToAdd, BranchProperty.branchPropertiesSettingsVector);
				//Now, exclude node label, nodelabel, branch length, branchlength
			}
			PropertyRecord.subtractIfInList(propertiesToAdd, new BranchProperty(MesquiteTree.nodeLabelName, Associable.BUILTIN));
			PropertyRecord.subtractIfInList(propertiesToAdd, new BranchProperty("nodelabel", Associable.STRINGS));
			PropertyRecord.subtractIfInList(propertiesToAdd, new BranchProperty(MesquiteTree.branchLengthName, Associable.BUILTIN));
			PropertyRecord.subtractIfInList(propertiesToAdd, new BranchProperty("branchlength", Associable.DOUBLES));
			//now removing those from tree
			DisplayableBranchProperty[] ps = myWindow.getPropertiesInTree();
			if (ps != null)
				for (int i = 0; i<ps.length; i++)
					PropertyRecord.subtractIfInList(propertiesToAdd, ps[i]);



			Listable chosen = ListDialog.queryList(containerOfModule(), "Property to add to branches/nodes", "What property do you want to add to the branches/nodes of the tree?", null, propertiesToAdd, -1);
			if (chosen == newProperty){
				String[] kinds = new String[]{ "Decimal number", "Integer number", "String of text", "Boolean (true/false)"};
				MesquiteInteger selectedInDialog = new MesquiteInteger(0);
				ListDialog dialog = new ListDialog(containerOfModule(), "New Property for branches/nodes", "What kind of property?", false,BranchProperty.branchNodeExplanation, kinds, 8, selectedInDialog, "OK", null, false, true);
				SingleLineTextField nameF = dialog.addTextField("Name of Property:", "", 30);
				Checkbox cb = dialog.addCheckBox("Pertains to polarized node (rather than branch between nodes); see Help (?).", false);
				dialog.addLargeOrSmallTextLabel("You can edit the values at branches/nodes by right-clicking on the branch with the arrow tool, or by clicking with the branch information tool (\"?\").");
				dialog.completeAndShowDialog(true);
				if (dialog.buttonPressed.getValue() == 0)  {
					int result = selectedInDialog.getValue();
					String name = nameF.getText();
					if (StringUtil.blank(name))
						name = "Untitled";
					if (result>=0){
						int kind = -1;
						if (result == 0)
							kind = Associable.DOUBLES;
						else if (result == 1)
							kind = Associable.LONGS;
						else if (result == 2)
							kind = Associable.STRINGS;
						else if (result == 3)
							kind = Associable.BITS;
						NameReference nameRef = NameReference.getNameReference(name);
						//MAKE SURE DOESN't CONFLICT WITH DEFAULT/built in etc. 
						if (PropertyRecord.inListButOtherKind(getProject().knownBranchProperties, nameRef, kind) || PropertyRecord.inListButOtherKind(DisplayableBranchProperty.branchPropertyDisplayPreferences, nameRef, kind) || PropertyRecord.inListButOtherKind(BranchProperty.branchPropertiesSettingsVector, nameRef, kind)){
							alert("You have given a name already in use. Try again, using another name, or, if you want to select an already known property, select it directly from the list");
							return null;
						}
						myWindow.makeNewProperty(kind, name, !cb.getState());
						//REMEMBER IN PREFS BETWEENNESS = !cb.getState()
					}
				}
				dialog.dispose();
			}
			else if (chosen instanceof BranchProperty) {
				myWindow.addProperty((BranchProperty)chosen);
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public void heightToBuiltInBranchLength(MesquiteTree tree, int node, NameReference nr){
		if (node == tree.getRoot()){
			tree.setBranchLength(node, MesquiteDouble.unassigned, false);
		}
		else {
			double nodeHeight= tree.getAssociatedDouble(nr, node);
			double motherHeight= tree.getAssociatedDouble(nr, tree.motherOfNode(node));
			if (MesquiteDouble.isCombinable(nodeHeight) && MesquiteDouble.isCombinable(motherHeight))
				tree.setBranchLength(node, motherHeight-nodeHeight, false);
			else
				tree.setBranchLength(node, MesquiteDouble.unassigned, false);
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			heightToBuiltInBranchLength(tree, d, nr);
	}
	public void lengthToBuiltInBranchLength(MesquiteTree tree, int node, NameReference nr){
		tree.setBranchLength(node, tree.getAssociatedDouble(nr, node), false);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			lengthToBuiltInBranchLength(tree, d, nr);
	}
	public void branchLengthToImpliedHeight(MesquiteTree tree, int node, double depth, NameReference nr){
		tree.setAssociatedDouble(nr, node, depth);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			branchLengthToImpliedHeight(tree, d, depth - tree.getBranchLength(d, 0), nr);
	}

	double fromString(String s){
		double d = MesquiteDouble.fromString(s);
		if (!MesquiteDouble.isCombinable(d) && d != MesquiteDouble.unassigned)
			d = MesquiteDouble.unassigned;
		return d;
	}
	/*.................................................................................................................*/
	void propertyToNodeLabel(){
		DisplayableBranchProperty[] properties = tree.getPropertyRecords();
		Listable listable = ListDialog.queryList(containerOfModule(), "Convert property to node labels", "Which property to convert to node labels?", null, properties, 0);
		if (listable==null)
			return;
		DisplayableBranchProperty property = (DisplayableBranchProperty)listable;
		for (int node = 0; node<tree.getNumNodeSpaces(); node++) {
			if (tree.nodeExists(node)){
				String s = property.getStringAtNode( tree, node, false, true, true);
				tree.setNodeLabel(s, node);
			}
		}
		tree.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
		parametersChanged();
	}
	/*.................................................................................................................*/
	void propertyToTextProperty(){
		DisplayableBranchProperty[] properties = tree.getPropertyRecords();
		Listable listable = ListDialog.queryList(containerOfModule(), "Convert property to text property", "Which property to convert to text?", null, properties, 0);
		if (listable==null)
			return;
		NameReference nRef = tree.makeAssociatedStrings(listable.getName() + ".text");
		DisplayableBranchProperty property = (DisplayableBranchProperty)listable;
		for (int node = 0; node<tree.getNumNodeSpaces(); node++) {
			if (tree.nodeExists(node)){
				String s = property.getStringAtNode( tree, node, false, true, true);
				tree.setAssociatedString(nRef, node, s);
			}
		}
		tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		parametersChanged();
	}
	/*.................................................................................................................*/
	void textPropertyToNumbers(){
		ListableVector associates = tree.getAssociatesOfKind(Associable.STRINGS); // extract out text ones
		Listable listable = ListDialog.queryList(containerOfModule(), "Convert text property to decimal number", "Which property to convert to decimal numbers?", null, associates, 0);
		if (listable==null)
			return;
		NameReference nRef = tree.makeAssociatedDoubles(listable.getName() + ".numbers");
		DisplayableBranchProperty property = new DisplayableBranchProperty(listable.getName(), Associable.STRINGS);
		for (int node = 0; node<tree.getNumNodeSpaces(); node++) {
			if (tree.nodeExists(node)){
				String s = property.getStringAtNode( tree, node, false, true, true);
				double d = MesquiteDouble.fromString(s);
				tree.setAssociatedDouble(nRef, node, d);
			}
		}
		tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		parametersChanged();
	}
	/*.................................................................................................................*/
	void nodeLabelToTextProperty(){
		NameReference nRef = NameReference.getNameReference("Node label.text");
		for (int node = 0; node<tree.getNumNodeSpaces(); node++) {
			if (tree.nodeExists(node)){
				String s = tree.getNodeLabel(node);
				tree.setAssociatedString(nRef, node, s);
			}
		}
		tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		parametersChanged();
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
	public Object getMainObject(){
		return tree;
	}
	/*.................................................................................................................*/
	/** Requests a getModuleWindow() to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.popIn();
		whichWindow.hide();
	}

	public int getNumberOfRows(){
		if (myWindow == null || myWindow.associatesList == null)
			return 0;
		return myWindow.associatesList.size();

	}
	public Class getAssistantClass(){
		return BranchPropertiesListAssistant.class;
	}
	public String getItemTypeName(){
		return "Branch/Node Property";
	}
	public String getItemTypeNamePlural(){
		return "Branch/Node Properties";
	}
	/*.................................................................................................................*/
	public DisplayableBranchProperty getPropertyAtRow(int row){
		if (myWindow != null && row>=0 && row<myWindow.associatesList.size()){
			return (DisplayableBranchProperty)myWindow.associatesList.elementAt(row);
		}
		return null;
	}
	public boolean associateIsBuiltIn(int row){
		DisplayableBranchProperty p = getPropertyAtRow(row);
		if (p != null)
			return p.kind == Associable.BUILTIN;
		/*
		if (myWindow != null && row>=0 && row<myWindow.associatesList.size()){
			ObjectContainer objContainer = (ObjectContainer)myWindow.associatesList.elementAt(row);
			boolean builtIn = false;
			if (objContainer.getName().equalsIgnoreCase(MesquiteTree.branchLengthName))
				builtIn = true;
			else if (objContainer.getName().equalsIgnoreCase(MesquiteTree.nodeLabelName))
				builtIn = true;
			if (row <2 && !builtIn && MesquiteTrunk.developmentMode)
				System.err.println("Row doesn't match built-in in " + getClass());
			return builtIn;
		}*/
		return false;
	}
	/*.................................................................................................................*/
	public boolean columnsMovable(){
		return true;
	}
	public boolean rowsMovable(){
		return false;
	}
	public boolean rowsDeletable(){
		return true; 
	}
	public boolean deleteRow(int row, boolean notify){
		return internalDeleteRow(row, true);
	}

	public boolean internalDeleteRow(int row, boolean notify){
		DisplayableBranchProperty mi = getPropertyAtRow(row);

		if (mi.kind == Associable.BUILTIN){
			if (mi.getName().equalsIgnoreCase(MesquiteTree.branchLengthName))
				tree.deassignAllBranchLengths(notify);
			else if (mi.getName().equalsIgnoreCase(MesquiteTree.nodeLabelName)){
				tree.removeAllInternalNodeLabels(tree.getRoot());
				if (notify)
					tree.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
			}

		}
		else {if (mi.kind == Associable.BITS)
			tree.removeAssociatedBits(NameReference.getNameReference(mi.getName()));
		else if (mi.kind == Associable.LONGS)
			tree.removeAssociatedLongs(NameReference.getNameReference(mi.getName()));
		else if (mi.kind == Associable.DOUBLES)
			tree.removeAssociatedDoubles(NameReference.getNameReference(mi.getName()));
		else if (mi.kind == Associable.STRINGS)
			tree.removeAssociatedStrings(NameReference.getNameReference(mi.getName()));
		else if (mi.kind == Associable.OBJECTS)
			tree.removeAssociatedObjects(NameReference.getNameReference(mi.getName()));
		if (notify)
			tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		}
		myWindow.setTree(tree); //easy way to reset table!
		return true;
	}
	/*.................................................................................................................*/
	public boolean rowDeletable(int row){
		return !associateIsBuiltIn(row); //ok for !color
	}

	/*.................................................................................................................*/

	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ 
		return null;
	}
	/*.................................................................................................................*/

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){
	}
	/** returns a String of explanation for a row*
	public String getExplanation(int row){
		return null;
	} */
	public void cursorTouchBranch(MesquiteTree tree, int N){
		for (int i=0; i<getNumberOfEmployees(); i++) {
			Object obj =  getEmployeeVector().elementAt(i);
			if (obj instanceof BranchPropertiesListAssistant) {
				((BranchPropertiesListAssistant)obj).cursorTouchBranch(tree, N);
			}
		}	
	}
	public void cursorEnterBranch(MesquiteTree tree, int N){
		for (int i=0; i<getNumberOfEmployees(); i++) {
			Object obj =  getEmployeeVector().elementAt(i);
			if (obj instanceof BranchPropertiesListAssistant) {
				((BranchPropertiesListAssistant)obj).cursorEnterBranch(tree, N);
			}
		}	
	}
	public void cursorExitBranch(MesquiteTree tree, int N){
		for (int i=0; i<getNumberOfEmployees(); i++) {
			Object obj =  getEmployeeVector().elementAt(i);
			if (obj instanceof BranchPropertiesListAssistant) {
				((BranchPropertiesListAssistant)obj).cursorExitBranch(tree, N);
			}
		}	
	}
	public void cursorMove(MesquiteTree tree){
		for (int i=0; i<getNumberOfEmployees(); i++) {
			Object obj =  getEmployeeVector().elementAt(i);
			if (obj instanceof BranchPropertiesListAssistant) {
				((BranchPropertiesListAssistant)obj).cursorMove(tree);
			}
		}	
	}

	/*.................................................................................................................*/
	public String getName() {
		return "List of Branch/Node Properties Maker";
	}
	public String getExplanation() {
		return "Makes windows listing information stored at the branches or nodes of the tree." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}

}

/* ======================================================================== */
class NodesAssociatesListWindow extends ListWindow implements MesquiteListener {
	MesquiteTree tree;
	ListableVector associatesList ;
	BranchPropertiesList ownerModule;
	public NodesAssociatesListWindow (BranchPropertiesList ownerModule) {
		super(ownerModule);
		this.ownerModule = ownerModule; 
		associatesList = new ListableVector();
		MesquiteTable t = getTable();
		if (t!=null)
			t.setAutoEditable(false, false, false, false);
		setIcon(MesquiteModule.getRootImageDirectoryPath() + "showNodeAssoc.gif");
		setDefaultAnnotatable(ownerModule);
	}
	/**/
	private void makeAssociatesList(){
		associatesList.removeAllElements(false);
		if (tree == null) 
			return;
		TreeWindowMaker twMB = (TreeWindowMaker)ownerModule.findEmployerWithDuty(TreeWindowMaker.class);
		ListableVector mainPropertiesList = twMB.getBranchPropertiesList();
		for (int i = 0; i<mainPropertiesList.size(); i++){
			DisplayableBranchProperty property = (DisplayableBranchProperty)mainPropertiesList.elementAt(i);
			if (property.inCurrentTree)
				associatesList.addElement(property, false);
		}
	}

	DisplayableBranchProperty[] getPropertiesInTree(){
		if (tree == null)
			return null;
		return tree.getPropertyRecords();
	}
	void addProperty(BranchProperty property){
		if (property == null || tree == null)
			return;
		tree.addProperty(property, true);
		resetAssociatesList();
	}

	void checkNodeOrientedProperties(boolean betweenness){
		if (!betweenness){ // is node oriented; need to stamp original root if first time
			int oRD = tree.getOriginalRootDaughter();
			if (tree.nodeExists(oRD) && tree.motherOfNode(oRD)!= tree.getRoot()){ // there had been an original root, but the current root is different!
				ownerModule.discreetAlert("This tree has node properties that are sensitive to rerooting, but it has been rerooted already. The property you have just added is also sensitive. "
						+"Beware of any subsequent rerootings; the property might be misinterpreted as belonging to the wrong node."); 
				return;
			}
			tree.stampRootIfNodeOrientedProperties();
		}
	}
	void makeNewProperty(int kind, String name, boolean betweenness){
		if (kind <0 || tree == null)
			return;
		NameReference nr = NameReference.getNameReference(name);
		if (kind == Associable.BITS){
			String candidateName = name;
			int nameCount = 2;
			while (tree.getAssociatedBits(NameReference.getNameReference(candidateName)) != null)
				candidateName = name + (nameCount++);
			NameReference nameRef = tree.makeAssociatedBits(candidateName);
			Bits bits = tree.getAssociatedBits(nameRef);
			bits.setBetweenness(betweenness);
			checkNodeOrientedProperties(betweenness);
			tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		}
		else if (kind == Associable.DOUBLES){
			String candidateName = name;
			int nameCount = 2;
			while (tree.getAssociatedDoubles(NameReference.getNameReference(candidateName)) != null)
				candidateName = name + (nameCount++);
			NameReference nameRef = tree.makeAssociatedDoubles(candidateName);
			DoubleArray ds = tree.getAssociatedDoubles(nameRef);
			ds.setBetweenness(betweenness);
			checkNodeOrientedProperties(betweenness);
			tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		}
		else if (kind == Associable.LONGS){
			String candidateName = name;
			int nameCount = 2;
			while (tree.getAssociatedLongs(NameReference.getNameReference(candidateName)) != null)
				candidateName = name + (nameCount++);
			NameReference nameRef = tree.makeAssociatedLongs(candidateName);
			LongArray ls = tree.getAssociatedLongs(nameRef);
			ls.setBetweenness(betweenness);
			checkNodeOrientedProperties(betweenness);
			tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		}
		else if (kind == Associable.STRINGS){
			String candidateName = name;
			int nameCount = 2;
			while (tree.getAssociatedStrings(NameReference.getNameReference(candidateName)) != null)
				candidateName = name + (nameCount++);
			NameReference nameRef = tree.makeAssociatedStrings(candidateName);
			StringArray sa = tree.getAssociatedStrings(nameRef);
			sa.setBetweenness(betweenness);
			checkNodeOrientedProperties(betweenness);
			tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		}
		resetAssociatesList();
	}
	/*.................................................................................................................*/

	public void resetTitle(){
		setTitle("Branch/Node Properties"); 
	}
	/*.................................................................................................................*/
	public Object getCurrentObject(){
		return associatesList;
	}

	/*.................................................................................................................*/
	void resetAssociatesList(){
		makeAssociatesList();
		table.setNumRows(associatesList.size());
		table.repaintAll();
	}
	/*.................................................................................................................*/
	public void setTree(MesquiteTree tree){
		this.tree = tree;
		setCurrentObject(associatesList);
		resetAssociatesList();
		for (int i=0; i< ownerModule.getNumberOfEmployees(); i++) {
			Object obj =  ownerModule.getEmployeeVector().elementAt(i);
			if (obj instanceof BranchPropertiesListAssistant) {
				((BranchPropertiesListAssistant)obj).setTree(tree);
			}
		}	
	}

	/*...............................................................................................................*/
	/** returns whether or not a row name of table is editable.*/
	public boolean isRowNameEditable(int row){
		return !ownerModule.associateIsBuiltIn(row); //ok for !color
	}
	/*.................................................................................................................*/
	public void setRowName(int row, String name, boolean update){
		if (ownerModule.associateIsBuiltIn(row)){
		}
		else if (row>=0 && row<associatesList.size()){
			DisplayableBranchProperty mi = ownerModule.getPropertyAtRow(row);
			if (mi != null)
				tree.renameAssociated(mi, name, true);
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "New assistant", "[]", commandName, "newAssistant")) {
			BranchPropertiesListAssistant assistant = (BranchPropertiesListAssistant)super.doCommand(commandName, arguments, checker);
			if (assistant!= null){
				assistant.setTree(tree);
			}
			return assistant;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/*...............................................................................................................*/

	public boolean interceptRowNameTouch(int row, EditorPanel editorPanel, int x, int y, int modifiers){
		return false;
	}
	public String getRowName(int row){
		if (associatesList!=null){
			if (row<0 || row >= associatesList.size())
				return null;
			return ((Listable)associatesList.elementAt(row)).getName();
		}
		else
			return null;
	}
	public String getRowNameForSorting(int row){
		return getRowName(row);
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}

}


