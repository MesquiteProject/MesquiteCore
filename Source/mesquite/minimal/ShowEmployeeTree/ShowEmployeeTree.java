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
package mesquite.minimal.ShowEmployeeTree;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Displays a window showing the employee tree of a module. */
public class ShowEmployeeTree extends EmployeeTree {
	public String getName() {
		return "Show Employee Tree";
	}
	public String getExplanation() {
		return "Shows the window listing the tree of employees of the module of a given window.";
	}
	/*.................................................................................................................*/
	BrowseHierarchy drawTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		drawTask= (BrowseHierarchy)hireEmployee(BrowseHierarchy.class, null);
		if (drawTask == null)
			return sorry(getName() + " couldn't start because no Browse Hierarchy module obtained.");
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public  HPanel showEmployeeTreeWindow(MesquiteModule module){
		if (getModuleWindow()== null)
			setModuleWindow(new EmployeeTreeWindow(this, module, drawTask));
		resetContainingMenuBar();
		resetAllWindowsMenus();
		getModuleWindow().setVisible(true);
		getModuleWindow().contentsChanged();
		getModuleWindow().toFront();
		return ((EmployeeTreeWindow)getModuleWindow()).getHPanel();
	}
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
	public void refreshBrowser(Class c){
		if (c == MesquiteModule.class && getModuleWindow()!=null)
			((EmployeeTreeWindow)getModuleWindow()).renew();
		super.refreshBrowser(c);
	}
}

//TODO: in response to hiring changes, update the browser
/*======================================================================== */
class EmployeeTreeWindow extends MesquiteWindow {
	HPanel  browser;
	MesquiteModule focalModule, ownerModule;
	public EmployeeTreeWindow (MesquiteModule ownerModule, MesquiteModule focalModule, BrowseHierarchy drawTask) {
		super(ownerModule, false); //infobar
		setWindowSize(300,300);
		this.focalModule = focalModule;
		this.ownerModule = ownerModule;
		setFont(new Font ("SanSerif", Font.PLAIN, 10));

		getGraphicsArea().setLayout(new BorderLayout());
		//getGraphicsArea().setBackground(Color.cyan);
		browser = drawTask.makeHierarchyPanel();
		browser.setTitle("Employees of \"" + focalModule.getName() + "\"");
		addToWindow(browser);
		browser.setSize(getWidth(), getHeight());
		browser.setVisible(true);
		browser.setBackground(ColorDistribution.lightYellow);
		browser.setRootNode(focalModule);
		setShowExplanation(true);
		setShowAnnotation(true);
		resetTitle();
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Employees of \"" + focalModule.getName() + "\""); 
	}
	public void renew() {
		browser.renew();
	}
	public HPanel getHPanel(){
		return browser;
	}
}


