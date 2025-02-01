/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NodeAssociatesList;
/*~~  */

import mesquite.lists.lib.*;
import mesquite.trees.NodeAssociatesZDisplayControl.NodeAssociatesZDisplayControl;
import mesquite.trees.lib.NodeAssociatesListAssistant;

import java.awt.Graphics;

import mesquite.lib.*;
import mesquite.lib.duties.TreeWindowMaker;
import mesquite.lib.table.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class NodeAssociatesList extends ListModule {

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeAssociatesListAssistant.class, "The List of Node Associates window can display columns showing information for each taxon group.",
				"You can request that columns be shown using the Columns menu of the List of Node Associates Window. ");
	}
	MesquiteTree tree;
	NodesAssociatesListWindow myWindow = null;
	NodeAssociatesZDisplayControl displayModule;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		findEmployerWithDuty(TreeWindowMaker.class).addMenuItem("List of Node/Branch Properties", MesquiteModule.makeCommand("showWindow",  this));
		return true;
	}

	/*.................................................................................................................*/
	public void setTree(MesquiteTree tree){
		this.tree = tree;
		if (myWindow != null)
			myWindow.setTree(tree);

	}
	/*.................................................................................................................*/
	public boolean showing(Object obj){
		return (getModuleWindow()!=null && obj == tree);
	}
	public void endJob(){
		super.endJob();
	}

	void changeNameInDisplayModule(String oldName, int kind, String newName){
		if (displayModule == null)
			displayModule = (NodeAssociatesZDisplayControl)findNearestColleagueWithDuty(NodeAssociatesZDisplayControl.class);
		if (displayModule != null)
			displayModule.changeName(oldName, kind, newName);  //change name in z

	}

	public void showListWindow(Object obj) {
		if (!(obj instanceof MesquiteTree)){
			return;
		}
		setTree((MesquiteTree)obj);
		displayModule = (NodeAssociatesZDisplayControl)findNearestColleagueWithDuty(NodeAssociatesZDisplayControl.class);
		showListWindow();
	}

	/*.................................................................................................................*/
	public void showListWindow(){
		if (myWindow != null){
			myWindow.show();
			myWindow.setVisible(true);
			myWindow.setPopAsTile(true);
			myWindow.popOut(true);
			return;
		}
		myWindow = new NodesAssociatesListWindow(this);
		setModuleWindow(myWindow);
		myWindow.getParentFrame().setPopoutWidth(460);
		myWindow.setPopAsTile(true);
		myWindow.popOut(true);
		makeMenu("List");
		if (!MesquiteThread.isScripting()){
			hireAssistant("#NodeAssociatesListValue");
			hireAssistant("#NodeAssociatesListShow");
			hireAssistant("#NodeAssociatesListKind");
			hireAssistant("#NodeAssociatesListBetween");
		}
		myWindow.setTree(tree);
		resetContainingMenuBar();
		resetAllWindowsMenus();
		//getModuleWindow().setVisible(true);
	}
	void hireAssistant(String name){
		NodeAssociatesListAssistant assistant = (NodeAssociatesListAssistant)hireNamedEmployee(NodeAssociatesListAssistant.class, StringUtil.tokenize(name));
		if (assistant!= null){
			myWindow.addListAssistant(assistant);
			assistant.setUseMenubar(false);
		}
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


	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows the window", "[]", commandName, "showWindow")) {
			showListWindow();
		}
		else if (checker.compare(this.getClass(), "Shows the window", "[]", commandName, "toggleWindow")) {
			if (myWindow == null || !myWindow.isVisible())
				showListWindow();
			else {
				myWindow.setVisible(false);
				myWindow.popIn();
				myWindow.hide();
			}

		}
		else if (checker.compare(this.getClass(), "Returns the window", "[]", commandName, "getWindow")) {
			return getModuleWindow();
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
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
		return NodeAssociatesListAssistant.class;
	}
	public String getItemTypeName(){
		return "Node/Branch Property";
	}
	public String getItemTypeNamePlural(){
		return "Node/Branch Properties";
	}
	/*.................................................................................................................*/
	public MesquiteInteger getNameKindOfRow(int row){
		if (myWindow != null && row>=0 && row<myWindow.associatesList.size()){
			ObjectContainer objContainer = (ObjectContainer)myWindow.associatesList.elementAt(row);
			Object obj = objContainer.getObject();
			String name = null;
			if (!(obj instanceof Tree) && obj instanceof Listable)
				name = ((Listable)obj).getName();
			else
				name = objContainer.getName();
			int kind = Associable.BUILTIN;
			if (objContainer.getName().equalsIgnoreCase("Branch length"))
				kind = Associable.BUILTIN;
			else if (name.equalsIgnoreCase("!color"))
				kind = Associable.OBJECTS;
			else if (objContainer.getName().equalsIgnoreCase("Node label"))
				kind = Associable.BUILTIN;
			else if (obj instanceof DoubleArray)
				kind = Associable.DOUBLES;
			else if (obj instanceof LongArray)
				kind = Associable.LONGS;
			else if (obj instanceof StringArray)
				kind = Associable.OBJECTS;
			else if (obj instanceof ObjectArray) 
				kind = Associable.OBJECTS;
			else if (obj instanceof Bits) 
				kind = Associable.OBJECTS;
			return new MesquiteInteger(name, kind);
		}
		return null;
	}
	public boolean associateIsBuiltIn(int row){
		if (myWindow != null && row>=0 && row<myWindow.associatesList.size()){
			ObjectContainer objContainer = (ObjectContainer)myWindow.associatesList.elementAt(row);
			boolean builtIn = false;
			if (objContainer.getName().equalsIgnoreCase("Branch length"))
				builtIn = true;
			else if (objContainer.getName().equalsIgnoreCase("Node label"))
				builtIn = true;
			if (row <2 && !builtIn && MesquiteTrunk.developmentMode)
				System.err.println("Row doesn't match built-in in " + getClass());
			return builtIn;
		}
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
		return true;  //Debugg.println
	}
	public boolean deleteRow(int row, boolean notify){
		return internalDeleteRow(row, true);
	}

	public boolean internalDeleteRow(int row, boolean notify){
		MesquiteInteger mi = getNameKindOfRow(row);

		if (mi.getValue() == Associable.BUILTIN){
			if (mi.getName().equalsIgnoreCase("Branch length"))
				tree.deassignAllBranchLengths(notify);
			else if (mi.getName().equalsIgnoreCase("Node label")){
				tree.removeAllInternalNodeLabels(tree.getRoot());
				if (notify)
					tree.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
			}

		}
		else {if (mi.getValue() == Associable.BITS)
			tree.removeAssociatedBits(NameReference.getNameReference(mi.getName()));
		else if (mi.getValue() == Associable.LONGS)
			tree.removeAssociatedLongs(NameReference.getNameReference(mi.getName()));
		else if (mi.getValue() == Associable.DOUBLES)
			tree.removeAssociatedDoubles(NameReference.getNameReference(mi.getName()));
		else if (mi.getValue() == Associable.OBJECTS)
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
			if (obj instanceof NodeAssociatesListAssistant) {
				((NodeAssociatesListAssistant)obj).cursorTouchBranch(tree, N);
			}
		}	
	}
	public void cursorEnterBranch(MesquiteTree tree, int N){
		for (int i=0; i<getNumberOfEmployees(); i++) {
			Object obj =  getEmployeeVector().elementAt(i);
			if (obj instanceof NodeAssociatesListAssistant) {
				((NodeAssociatesListAssistant)obj).cursorEnterBranch(tree, N);
			}
		}	
	}
	public void cursorExitBranch(MesquiteTree tree, int N){
		for (int i=0; i<getNumberOfEmployees(); i++) {
			Object obj =  getEmployeeVector().elementAt(i);
			if (obj instanceof NodeAssociatesListAssistant) {
				((NodeAssociatesListAssistant)obj).cursorExitBranch(tree, N);
			}
		}	
	}
	public void cursorMove(MesquiteTree tree){
		for (int i=0; i<getNumberOfEmployees(); i++) {
			Object obj =  getEmployeeVector().elementAt(i);
			if (obj instanceof NodeAssociatesListAssistant) {
				((NodeAssociatesListAssistant)obj).cursorMove(tree);
			}
		}	
	}

	/*.................................................................................................................*/
	public String getName() {
		return "List of Node/Branch Properties Maker";
	}
	public String getExplanation() {
		return "Makes windows listing information stored at the nodes of the tree." ;
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
	ListableVector associatesList = new ListableVector();
	NodeAssociatesList ownerModule;
	public NodesAssociatesListWindow (NodeAssociatesList ownerModule) {
		super(ownerModule);
		this.ownerModule = ownerModule; 
		MesquiteTable t = getTable();
		if (t!=null)
			t.setAutoEditable(false, false, false, false);
		setIcon(MesquiteModule.getRootImageDirectoryPath() + "showNodeAssoc.gif");
	}

	private void makeAssociatesList(){
		associatesList.removeAllElements(false);
		if (tree == null) 
			return;
		ObjectContainer branchLengths = new ObjectContainer("Branch length", tree);
		associatesList.addElement(branchLengths, false);
		ObjectContainer nodeLabels = new ObjectContainer("Node label", tree);
		associatesList.addElement(nodeLabels, false);
		int numBitsAssocs = tree.getNumberAssociatedBits();
		for (int i= 0; i<numBitsAssocs; i++){
			Bits bits = tree.getAssociatedBits(i);
			associatesList.addElement(new ObjectContainer(bits.getName(), bits), false);
		}
		int numDoubleAssocs = tree.getNumberAssociatedDoubles();
		for (int i= 0; i<numDoubleAssocs; i++){
			DoubleArray array = tree.getAssociatedDoubles(i);
			associatesList.addElement(new ObjectContainer(array.getName(), array), false);
		}
		int numLongAssocs = tree.getNumberAssociatedLongs();
		for (int i= 0; i<numLongAssocs; i++){
			LongArray array = tree.getAssociatedLongs(i);
			associatesList.addElement(new ObjectContainer(array.getName(), array), false);
		}
		int numObjectAssocs = tree.getNumberAssociatedObjects();
		for (int i= 0; i<numObjectAssocs; i++){
			ObjectArray array = tree.getAssociatedObjects(i);
			associatesList.addElement(new ObjectContainer(array.getName(), array), false);
		}
	}
	/*.................................................................................................................*/

	public void resetTitle(){
		setTitle("Node/Branch Properties"); 
	}
	/*.................................................................................................................*/
	public Object getCurrentObject(){
		return associatesList;
	}

	public void setTree(MesquiteTree tree){
		this.tree = tree;
		setCurrentObject(associatesList);
		makeAssociatesList();
		table.setNumRows(associatesList.size());
		table.repaintAll();

		for (int i=0; i< ownerModule.getNumberOfEmployees(); i++) {
			Object obj =  ownerModule.getEmployeeVector().elementAt(i);
			if (obj instanceof NodeAssociatesListAssistant) {
				((NodeAssociatesListAssistant)obj).setTree(tree);
			}
		}	
	}

	/*...............................................................................................................*/
	/** returns whether or not a row name of table is editable.*/
	public boolean isRowNameEditable(int row){
		return !ownerModule.associateIsBuiltIn(row); //ok for !color
	}
	public void setRowName(int row, String name){
		if (ownerModule.associateIsBuiltIn(row)){
		}
		else if (row>=0 && row<associatesList.size()){
			MesquiteInteger mi = ownerModule.getNameKindOfRow(row);
			ObjectContainer objContainer = (ObjectContainer)associatesList.elementAt(row);
			Object obj = objContainer.getObject();
			if (obj instanceof Nameable){
				Nameable nm = (Nameable)obj;
				ownerModule.changeNameInDisplayModule(mi.getName(), mi.getValue(), name);
				nm.setName(name);
				objContainer.setName(name);
				tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
			}
		}

	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "New assistant", "[]", commandName, "newAssistant")) {
			NodeAssociatesListAssistant assistant = (NodeAssociatesListAssistant)super.doCommand(commandName, arguments, checker);
			//		NodeAssociatesListAssistant assistant = (NodeAssociatesListAssistant)ownerModule.hireNamedEmployee(NodeAssociatesListAssistant.class, arguments);
			if (assistant!= null){
				assistant.setTree(tree);
			}

		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*...............................................................................................................*/

	public boolean interceptRowNameTouch(int row, int regionInCellH, int regionInCellV, int modifiers){
		/*TaxaGroup group = getTaxonGroup(row);
		if (group!=null){
			getTable().editRowNameCell(row);
		}*/
		return true;
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

}


