/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.lib;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/* ======================================================================== */
public abstract class SimpleTreeWindowMaker extends TWindowMaker implements TreeContext, TreeDisplayActive {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(DrawTreeCoordinator.class,  getName() + " needs a module to draw trees.",
		"The drawing coordinator is arranged automatically");
	}
	public DrawTreeCoordinator treeDrawCoordTask;
	SimpleTreeWindow simpleTreeWindow;
	public Vector contextListeners;

	/*.................................................................................................................*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName) {
		treeDrawCoordTask= (DrawTreeCoordinator)hireCompatibleEmployee(DrawTreeCoordinator.class, new MesquiteBoolean(false), null);
		if (treeDrawCoordTask == null)
			return sorry(getName() + " couldn't start because no tree draw coordinator module found");
		makeMenu(getMenuName());
		simpleTreeWindow= makeTreeWindow( this, treeDrawCoordTask);
		contextListeners = new Vector();

		setModuleWindow(simpleTreeWindow);
		hireAllEmployees(TreeDisplayAssistantDI.class);
		Enumeration e = getEmployeeVector().elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TreeDisplayAssistantI || obj instanceof TreeDisplayAssistantDI) {
				TreeDisplayAssistant tca = (TreeDisplayAssistant)obj;
				simpleTreeWindow.addAssistant(tca);
			}
		}
		resetContainingMenuBar();
		resetAllWindowsMenus();
		simpleTreeWindow.sizeDisplays();
		return true;
	}
	protected abstract SimpleTreeWindow makeTreeWindow(SimpleTreeWindowMaker stwm, DrawTreeCoordinator dtwc);
	protected abstract String getMenuName();
	protected String getDefaultExplanation(){
		return null;
	}
	public void employeeQuit(MesquiteModule m){
		if (m==treeDrawCoordTask)
			iQuit();
	}
	public void endJob(){
		if (contextListeners!=null){
			Enumeration e = contextListeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeContextListener) {
					TreeContextListener tce = (TreeContextListener)obj;
					tce.disposing(this);
				}
			}

			contextListeners.removeAllElements();
		}
		treeDrawCoordTask=null;
		simpleTreeWindow=null;
		super.endJob();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (source instanceof DrawTreeCoordinator){ //ignores since this should have directly called to update tree display
		}
		else super.employeeParametersChanged(employee, source, notification);
	}
	public boolean isSubstantive(){
		return false;
	}
	boolean first = true;
	public  void setWindowVisible(boolean vis){
		if (simpleTreeWindow == null)
			return;
		if (first){
			simpleTreeWindow.setVisible(vis);
			simpleTreeWindow.toFront();
			first = false;
		}
		else if(!simpleTreeWindow.isVisible())
			simpleTreeWindow.setVisible(vis);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		simpleTreeWindow.setTree(tree, false);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree, boolean suppressDrawing) {
		simpleTreeWindow.setTree(tree, suppressDrawing);
	}
	/*.................................................................................................................*/
	public void  suppressDrawing(boolean suppress){
		simpleTreeWindow.getTreeDisplay().suppressDrawing(suppress);
		if (!suppress)
			simpleTreeWindow.getTreeDisplay().repaint();
	}

	public Tree getTree (){
		if (simpleTreeWindow == null)
			return null;
		return simpleTreeWindow.getTree();
	}
	/*.................................................................................................................*/
	/** because TreeContext */
	public String getContextName(){
		if (simpleTreeWindow==null)
			return "Tree Window";
		return simpleTreeWindow.getTitle();
	}
	/*.................................................................................................................*/
	/** because TreeContext */
	public void addTreeContextListener (TreeContextListener listener){
		if (listener!=null && contextListeners.indexOf(listener)<0)
			contextListeners.addElement(listener);
	}
	/*.................................................................................................................*/
	/** because TreeContext */
	public void removeTreeContextListener (TreeContextListener listener){
		contextListeners.removeElement(listener);
	}
	public MesquiteModule getTreeSource (){  //returns source of trees for context so that source can avoid using itself as context for modifications (e.g. SourceModifiedTree)
		return this;
	}
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		whichWindow.hide();
		//whichWindow.dispose();
		//iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (simpleTreeWindow ==null || !simpleTreeWindow.isVisible())
			return null;

		Snapshot fromWindow = simpleTreeWindow.getSnapshot(file);
		if (fromWindow == null || fromWindow.getNumLines() ==0)
			return null;
		Snapshot sn = new Snapshot();
		sn.addLine("getWindow");
		sn.addLine("tell It");
		sn.incorporate(fromWindow, true);
		sn.addLine("endTell");
		sn.addLine("getTreeDrawCoordinator", treeDrawCoordTask);
		sn.addLine("showWindow");

		return sn;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns module coordinating tree drawing", null, commandName, "getTreeDrawCoordinator")) {
			return treeDrawCoordTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/
	public boolean mouseDownInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		if (isDoomed())
				return false;
		if (!treeDisplay.getTree().isLocked())
			return simpleTreeWindow.ScanTouch(treeDisplay, g, x, y, modifiers);
		return false;
	}

	/*.................................................................................................................*/
	public boolean mouseUpInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		return true;
	}

	/*.................................................................................................................*/
	public boolean mouseMoveInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		if (isDoomed())
				return false;
		if (!treeDisplay.getTree().isLocked())
			simpleTreeWindow.ScanFlash(treeDisplay, g, x, y, modifiers);
		return true;
	}
	/*.................................................................................................................*/
	public boolean mouseDragInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 201;  
	}

}



