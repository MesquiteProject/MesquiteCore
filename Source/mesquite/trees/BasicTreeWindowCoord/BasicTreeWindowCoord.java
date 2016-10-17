/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BasicTreeWindowCoord;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Coordinates the display of the basic Tree Windows (BasicTreeWindowMaker actually makes the window) */
public class BasicTreeWindowCoord extends FileInit {
	ListableVector treeWindows;
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeWindowMaker.class, "Tree windows display trees to the user.", 
				"You may request a tree window by selecting the Tree Window item under the Taxa&Trees menu");
		e.setAsEntryPoint("makeTreeWindow");
	}
	MesquiteMenuItemSpec catw = null;
	/*.................................................................................................................*/
	public String getName() {
		return "Tree Window Coordinator"; 
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates the creation of basic tree windows." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		MesquiteSubmenuSpec mss = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "New Tree Window");
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.treesMenu, mss, "With Trees from Source", makeCommand("makeTreeWindow",  this));
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.treesMenu, mss, "With Tree To Edit By Hand", makeCommand("editingTreeWindow",  this));
//		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "New Tree Window", makeCommand("makeTreeWindow",  this));
		treeWindows = new ListableVector();
		MesquiteSubmenuSpec mms = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "Current Tree Windows", makeCommand("showTreeWindow",  this));
		mms.setList(treeWindows);
		catw = getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "Close All Tree Windows", makeCommand("closeAllTreeWindows",  this));
		catw.setEnabled(false);

		return true;
	}

	String taxaRef(Taxa d, boolean internal){
		if (internal)
			return getProject().getTaxaReferenceInternal(d);
		else
			return getProject().getTaxaReferenceExternal(d);
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		if (m instanceof TreeWindowMaker) {
			for (int i=0; i<treeWindows.size(); i++){
				MesquiteWindow w = (MesquiteWindow)treeWindows.elementAt(i);
				if (w.getOwnerModule() == m || w.getOwnerModule() == null) {
					treeWindows.removeElement(w, false);
					if (catw != null) catw.setEnabled(treeWindows.size()>0);
					resetAllMenuBars();
					return;
				}
			}
			resetAllMenuBars();
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof TreeWindowMaker) {
				TreeWindowMaker dwm = (TreeWindowMaker)e;
				Taxa d = (Taxa)dwm.doCommand("getTaxa", null, CommandChecker.defaultChecker);
				if (d != null) {
					temp.addLine("makeTreeWindow " + taxaRef(d, false) + " ", dwm ); //TODO: must pass treeWindowTask
				}
			}
		}
		return temp;
	}
	/*.................................................................................................................*/
	public TreeWindowMaker showTreeWindow(Taxa taxa){
		if (taxa==null)
			return null;
		TreeWindowMaker treeWindowTask= (TreeWindowMaker)hireCompatibleEmployee(TreeWindowMaker.class, taxa, null);
		if (treeWindowTask !=null){
			treeWindowTask.doCommand("makeTreeWindow", getProject().getTaxaReferenceInternal(taxa), CommandChecker.defaultChecker);
			treeWindows.addElement(treeWindowTask.getModuleWindow(), false);
			if (catw != null) catw.setEnabled(treeWindows.size()>0);
			resetAllMenuBars();
			return treeWindowTask;
		}
		return null;
	}
	/*.................................................................................................................*/
	public TreeWindowMaker makeWindowShowingTrees(Taxa taxa, TreeVector trees){
		if (taxa==null)
			return null;
		TreeWindowMaker treeWindowTask = (TreeWindowMaker)hireNamedEmployee(TreeWindowMaker.class, "$ #BasicTreeWindowMaker edit", taxa);
		if (treeWindowTask !=null){
			boolean wasScrip = MesquiteThread.isScripting();
			CommandRecord rec = MesquiteThread.getCurrentCommandRecord();
			if (rec != null)
				rec.setScripting(true);
			treeWindowTask.doCommand("makeTreeWindow", getProject().getTaxaReferenceInternal(taxa), CommandChecker.defaultChecker);
			if (rec != null)
				rec.setScripting(wasScrip);
			treeWindows.addElement(treeWindowTask.getModuleWindow(), false);
			if (catw != null) catw.setEnabled(treeWindows.size()>0);
			TreesManager manager = (TreesManager)findElementManager(TreeVector.class);
			int whichBlock = manager.getTreeBlockNumber(trees.getTaxa(), trees);
			TreeSource ts = (TreeSource)treeWindowTask.getTreeSource();
			ts.doCommand("setTreeBlockInt", Integer.toString(whichBlock), CommandChecker.defaultChecker);
			resetAllMenuBars();
		}
		return treeWindowTask;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Requests that a tree window be made", "[number of taxa block]", commandName, "makeTreeWindow") || checker.compare(this.getClass(), "Requests that a tree window be made for hand editing", "[number of taxa block]", commandName, "editingTreeWindow")) {
			Taxa taxa = null;
			MesquiteFile file = null;
			if (getProject() == null)
				return null;
			if (getProject().getNumberTaxas()==0){
				discreetAlert("A taxa block must be created first before a tree window can be shown");
				return null;
			}
			if (checker != null)
				file = checker.getFile();
			if (!StringUtil.blank(arguments)) //rearranged to attempt to get some taxa to be used
				taxa =  getProject().getTaxa(file, parser.getFirstToken(arguments));
			if (taxa == null && !StringUtil.blank(arguments)) //rearranged to attempt to get some taxa to be used
				taxa =  getProject().getTaxa(parser.getFirstToken(arguments));
			if (taxa == null){
				int numTaxas = getProject().getNumberTaxas(file);
				//if only one taxa block, use it
				if (numTaxas<=0){
					return null;
				}
				else if (numTaxas==1){
					taxa =  getProject().getTaxa(file, 0);
				}
				else {
					taxa =  getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show trees in the tree window?");
					//else, query user
				}
			}
			if (taxa==null)
				return null;
			TreeWindowMaker treeWindowTask= null;
			if (commandName.equalsIgnoreCase("editingTreeWindow"))
				treeWindowTask = (TreeWindowMaker)hireNamedEmployee(TreeWindowMaker.class, "$ #BasicTreeWindowMaker edit", taxa);
			else
				treeWindowTask= (TreeWindowMaker)hireCompatibleEmployee(TreeWindowMaker.class, taxa, "Tree window style");

			if (treeWindowTask !=null){
				treeWindowTask.doCommand("makeTreeWindow", getProject().getTaxaReferenceInternal(taxa), checker);
				treeWindows.addElement(treeWindowTask.getModuleWindow(), false);
				if (catw != null) catw.setEnabled(treeWindows.size()>0);
				resetAllMenuBars();
				return treeWindowTask;
			}
		}
		else if (checker.compare(this.getClass(), "Closes all tree windows", null, commandName, "closeAllTreeWindows")) {
			for (int i = treeWindows.size()-1; i>=0; i--){
				MesquiteWindow win = (MesquiteWindow)treeWindows.elementAt(i);
				TreeWindowMaker t = (TreeWindowMaker)win.getOwnerModule();
				t.windowGoAway(win);
			}
			if (catw != null) catw.setEnabled(treeWindows.size()>0);
			resetAllMenuBars();

		}
		else if (checker.compare(this.getClass(), "Shows an existing tree window", "[number of tree window as employee of coordinator]", commandName, "showTreeWindow")) {
			pos.setValue(0);
			int which = MesquiteInteger.fromString(arguments, pos);
			if ((which == 0 || MesquiteInteger.isPositive(which)) && which<treeWindows.size()) {
				MesquiteWindow win = (MesquiteWindow)treeWindows.elementAt(which);
				win.show();
			}

		}
		else if (checker.compare(this.getClass(), "Returns module controlling i'th tree window", "[i]", commandName, "getTreeWindow")) {
			pos.setValue(0);
			int which = MesquiteInteger.fromString(arguments, pos);
			if ((which == 0 || MesquiteInteger.isPositive(which)) && which<treeWindows.size()) {
				MesquiteWindow win = (MesquiteWindow)treeWindows.elementAt(which);
				return win.getOwnerModule();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/**Returns command to hire employee if clonable*/
	public String getClonableEmployeeCommand(MesquiteModule employee){
		if (employee!=null && employee.getEmployer()==this) {
			if (employee.getHiredAs()==TreeWindowMaker.class) {
				Taxa d = (Taxa)employee.doCommand("getTaxa", null, CommandChecker.defaultChecker);
				if (d != null) {
					return ("makeTreeWindow " + taxaRef(d, true) + "  " + StringUtil.tokenize(employee.getName()) + ";");//quote
				}
			}
		}
		return null;
	}

}


