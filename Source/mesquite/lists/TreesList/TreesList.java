/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TreesList;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TreesList extends ListLVModule {
	/*.................................................................................................................*/
	public String getName() {
		return "Trees List";
	}
	public String getExplanation() {
		return "Makes windows listing trees and information about them." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeListAssistant.class, "The List of Trees window can display columns showing information for each tree.",
		"You can request that columns be shown using the List menu of the List of Trees Window. ");
		e.setEntryCommand("newAssistant");
		EmployeeNeed e2 = registerEmployeeNeed(TreeListUtility.class, "Utilities operating on trees can be used through the List of Trees window.",
		"You can request such a utility using the Utilities submenu of the List menu of the List of Trees Window. ");
	}

	/*.................................................................................................................*/
	Taxa taxa;
	TreeVector currentTreeBlock = null;
	TreesManager manager;
	MesquiteSubmenuSpec listSubmenu;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		manager = (TreesManager)findElementManager(TreeVector.class);
		if (manager==null)
			return sorry(getName() + " couldn't start because no tree manager module found.");
		if (manager.getNumberTreeBlocks()==0)
			return sorry(getName() + " couldn't start because no stored trees available.");
		hireAllEmployees(TreeListInit.class);
		return true;
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)obj == taxa) {
			iQuit();
		}
	}
	/*.................................................................................................................*/

	/** returns an explanation of the row.*/
	public String getAnnotation(int row) {
		if (currentTreeBlock !=null){
			if (row >=0 && row < currentTreeBlock.size()) {
				MesquiteTree t = (MesquiteTree)currentTreeBlock.getTree(row);
				if (t!=null) {
					return t.getAnnotation();
				}
			}
		}
		return null ;
	}
	/*.................................................................................................................*/
	public boolean showing(Object obj){
		if (obj instanceof TreeVector) {
			return (obj == currentTreeBlock && getModuleWindow()!=null);
		}
		return false;
	}

	public void showListWindow(Object obj){
		if (obj instanceof TreeVector) {
			currentTreeBlock = (TreeVector)obj;
		}
		else
			currentTreeBlock = manager.getTreeBlock(getProject().getTaxa(0), 0);
		if (currentTreeBlock==null)
			return;
		if (taxa!=null)
			taxa.removeListener(this);
		taxa =currentTreeBlock.getTaxa();
		if (taxa!=null)
			taxa.addListener(this);
		
		setModuleWindow( new TreesListWindow(this));
		((TreesListWindow)getModuleWindow()).setObject(currentTreeBlock);
		makeMenu("List");
		addMenuItem("Make New Tree...", makeCommand("newTree",  this));
		listSubmenu = addSubmenu(null, "Tree Block", makeCommand("setTreeBlockInt",  this), manager.getTreeBlockVector());
		listSubmenu.setCompatibilityCheck(taxa);

		MesquiteSubmenuSpec mss2 = addSubmenu(null, "Utilities", MesquiteModule.makeCommand("doUtility",  this));
		mss2.setList(TreeListUtility.class);
		//addMenuItem( "Save selected set...", makeCommand("saveSelectedRows", this));
		addMenuSeparator();
		if (!MesquiteThread.isScripting()){
			TreeListAssistant assistant;
			assistant = (TreeListAssistant)hireNamedEmployee(TreeListAssistant.class, "$ #NumForTreeList #NumberOfTaxa");
			if (assistant!= null){
				((ListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (TreeListAssistant)hireNamedEmployee(TreeListAssistant.class, "#TreeListRooted");
			if (assistant!= null){
				((ListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (TreeListAssistant)hireNamedEmployee(TreeListAssistant.class, "#TreeListPolys");
			if (assistant!= null){
				((ListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
				assistant = (TreeListAssistant)hireNamedEmployee(TreeListAssistant.class, "#TreeListPolyAssumption");
				if (assistant!= null){
					((ListWindow)getModuleWindow()).addListAssistant(assistant);
					assistant.setUseMenubar(false);
				}
			}
		}
		for (int i = 0; i<getEmployeeVector().size(); i++){
			Object mb = getEmployeeVector().elementAt(i);
			if (mb instanceof TreeListInit){
				((TreeListInit)mb).setTableAndTreeBlock(((TableWindow)getModuleWindow()).getTable(), currentTreeBlock);
			}
		}
		resetContainingMenuBar();
		resetAllWindowsMenus();
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
	public Object getMainObject(){
		return currentTreeBlock;
	}
	/* */
	public TreeVector getTreeBlock(){
		return currentTreeBlock;
	}
	public int getNumberOfRows(){
		if (currentTreeBlock==null)
			return 0;
		else
			return currentTreeBlock.size();
	}
	public Class getAssistantClass(){
		return TreeListAssistant.class;
	}
	public String getItemTypeName(){
		return "Tree";
	}
	public String getItemTypeNamePlural(){
		return "Trees";
	}
	public void classFieldChanged (Class c, String fieldName) {
		super.classFieldChanged(c, fieldName);
		if (c== Tree.class)
			forceRecalculations();
	}
	/*.................................................................................................................*/
	public boolean rowsDeletable(){
		return true;
	}
	public boolean deleteRow(int row, boolean notify){
		if (currentTreeBlock!=null) {
			Listable obj = currentTreeBlock.getTree(row);
			if (obj!=null) {
				currentTreeBlock.removeElement(obj, notify);
				return true;
			}
		}
		return false;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (getModuleWindow()==null || !getModuleWindow().isVisible())
			return null;
		Snapshot temp = new Snapshot();

		temp.addLine("setTreeBlock " + TreeVector.toExternal(manager.getTreeBlockNumber(taxa, currentTreeBlock))); 
		if (getModuleWindow()!=null)
			getModuleWindow().incorporateSnapshot(temp, file);
		temp.addLine("showWindow"); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets tree block to use (for internal use; 0 based)", "[number of tree block]", commandName, "setTreeBlockInt")) { //need separate from setTreeBlock since this is used internally with 0-based menu response
			int whichList = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (MesquiteInteger.isCombinable(whichList)) {
				currentTreeBlock = manager.getTreeBlock(taxa, whichList);
				if (taxa!=null)
					taxa.removeListener(this);
				taxa =currentTreeBlock.getTaxa();
				if (taxa!=null)
					taxa.addListener(this);
				listSubmenu.setCompatibilityCheck(taxa);
				((TreesListWindow)getModuleWindow()).setObject(currentTreeBlock);
				return currentTreeBlock;
			}
		}
		else if (checker.compare(this.getClass(), "Sets tree block to use", "[number of tree block]", commandName, "setTreeBlock")) {
			int whichList = TreeVector.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
			if (MesquiteInteger.isCombinable(whichList)) {
				TreeVector t  = manager.getTreeBlock(taxa, whichList);
				if (t==null)
					return null;
				currentTreeBlock = t;
				if (taxa!=null)
					taxa.removeListener(this);
				taxa =currentTreeBlock.getTaxa();
				if (taxa!=null)
					taxa.addListener(this);
				((TreesListWindow)getModuleWindow()).setObject(currentTreeBlock);
				return currentTreeBlock;
			}
		}
		else if (checker.compare(this.getClass(), "Instructs the user how to make a new tree", null, commandName, "newTree")) {
			alert("One way to create a new tree is to open a tree window, edit the tree, and choose Store Tree from the Tree menu"); 
		}
		else if (checker.compare(this.getClass(), "Hires utility module to operate on the trees", "[name of module]", commandName, "doUtility")) {
			if (currentTreeBlock !=null){
				TreeListUtility tda= (TreeListUtility)hireNamedEmployee(TreeListUtility.class, arguments);
				if (tda!=null) {
					boolean a = tda.operateOnTrees(currentTreeBlock);
					if (!tda.pleaseLeaveMeOn())
						fireEmployee(tda);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Returns taxa block used", null, commandName, "getTaxa")) {
			return taxa;
		}
		else if (checker.compare(this.getClass(), "Returns current tree block", null, commandName, "getTreeBlock")) {
			return currentTreeBlock;
		}
		else if (checker.compare(this.getClass(), "NOT USED YET", null, commandName, "saveSelectedRows")) {
			//((TreesListWindow)getModuleWindow()).saveSelectedRows();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		if (currentTreeBlock==null)
			return "";
		return "Tree block: "  + currentTreeBlock.getName();
	}

	/*.................................................................................................................*/
	/** Requests a window to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		//Debug.println("disposing of window");
		if (whichWindow == null)
			return;
		whichWindow.hide();
	}


}

/* ======================================================================== */
class TreesListWindow extends ListableVectorWindow implements MesquiteListener {
	private TreeVector currentTreeBlock;
	TreesList treesListModule=null;
	public TreesListWindow (TreesList ownerModule) {
		super(ownerModule);
		treesListModule = ownerModule;
		currentTreeBlock = ownerModule.currentTreeBlock; //SHOULDN'T BE CURRENT FOR TWIG SINCE SHOULD ALLOW MORE THAN ONE WINDOW
		if (currentTreeBlock!=null)
			currentTreeBlock.addListener(this); 
		getTable().setRowAssociable(currentTreeBlock);
		resetTitle();
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		if (treesListModule ==null)
			setTitle("Trees"); 
		else {
			TreeVector trees = treesListModule.getTreeBlock();
			if (trees==null)
				setTitle("Trees"); 
			else if (StringUtil.blank(trees.getName()) || "untitled".equalsIgnoreCase(trees.getName()))
				setTitle("Trees of taxa " + trees.getTaxa().getName()); //TODO: name?
			else 
				setTitle("Trees \"" + trees.getName() + "\""); 
		}
	}
	public Object getCurrentObject(){
		return currentTreeBlock;
	}
	
	public void setObject(Object o){
		super.setObject(o);
		TreeListInit assistant;
		Enumeration enumeration=ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof TreeListInit) {
				assistant =  (TreeListInit)obj;
				assistant.setTableAndTreeBlock(table, (TreeVector)o);
			}
		}	
	}
	/*.................................................................................................................*/
	public void processPostSwap(Associable assoc){
		if (assoc instanceof TreeVector) 
			((TreeVector)assoc).resetAssignedNumbers();
		super.processPostSwap(assoc);
	}
	/*.................................................................................................................*/
	public void setCurrentObject(Object obj){
		if (obj instanceof TreeVector) {
			if (currentTreeBlock!=null)
				currentTreeBlock.removeListener(this);
			currentTreeBlock = (TreeVector)obj;
			if (currentTreeBlock!=null)
				currentTreeBlock.addListener(this);
			getTable().setRowAssociable(currentTreeBlock);
			getTable().synchronizeRowSelection(currentTreeBlock);
			resetTitle();
		}
		super.setCurrentObject(obj);
	}
	public void setRowName(int row, String name){
		if (currentTreeBlock!=null){
			AdjustableTree tree = (AdjustableTree)currentTreeBlock.getTree(row);
			if (tree!=null)
				tree.setName(name);
		}
	}
	public String getRowName(int row){
		if (currentTreeBlock!=null) {
			Tree tree = currentTreeBlock.getTree(row);
			if (tree!=null)
				return tree.getName();
			else
				return null;
		}
		else
			return null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof TreeVector &&  (TreeVector)obj ==currentTreeBlock)
			ownerModule.windowGoAway(this);
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		UndoReference undoReference = Notification.getUndoReference(notification);
		int code = Notification.getCode(notification);
		if (obj instanceof TreeVector && (TreeVector)obj ==currentTreeBlock) {
			if (code==MesquiteListener.NAMES_CHANGED) {
				getTable().redrawRowNames();
			}
			else if (code==MesquiteListener.SELECTION_CHANGED) {
				getTable().synchronizeRowSelection(currentTreeBlock);
				getTable().repaintAll();
			}
			else if (code==MesquiteListener.ITEMS_ADDED ||code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_CHANGED || code==MesquiteListener.PARTS_MOVED) {
				getTable().setNumRows(currentTreeBlock.size());
				getTable().synchronizeRowSelection(currentTreeBlock);
				treesListModule.forceRecalculations();
				getTable().repaintAll();
				if (code==MesquiteListener.PARTS_MOVED)
					setUndoer(undoReference);
				else
					setUndoer();
			}
			else if (code==MesquiteListener.PARTS_DELETED) {
				getTable().setNumRows(currentTreeBlock.size());
				getTable().synchronizeRowSelection(currentTreeBlock);
				treesListModule.forceRecalculations();
				getTable().repaintAll();
			}
			else if (code!=MesquiteListener.ANNOTATION_CHANGED && code!=MesquiteListener.ANNOTATION_ADDED && code!=MesquiteListener.ANNOTATION_DELETED) {
				getTable().setNumRows(currentTreeBlock.size());
				getTable().synchronizeRowSelection(currentTreeBlock);
				getTable().repaintAll();
			}
		}
		else
			super.changed(caller, obj, notification);
	}

	public void dispose(){
		if (currentTreeBlock!=null)
			currentTreeBlock.removeListener(this);
		super.dispose();
	}

}


