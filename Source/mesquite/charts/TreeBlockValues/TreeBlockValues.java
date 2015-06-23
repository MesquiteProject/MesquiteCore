/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.TreeBlockValues;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**=== Class TreeBlocksChart.  ===*/

public class TreeBlockValues extends FileAssistantCH {
	/*.................................................................................................................*/
	public String getName() {
		return "Tree Block Values";
	}
	public String getExplanation() {
		return "Makes a chart showing some value for each of many tree blocks.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTreeBlock.class, getName() + " needs a method to calculate values (such as average imbalance statistics, correlations,etc.) for a series of tree blocks.",
		"You can select this either when the chart starts up, or using the Values submenu of the Chart menu.  (You may request the chart itself by selecting the Tree Blocks item under New Bar & Line Chart in the Analysis menu)");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(TreeBlockSource.class, getName() + " needs a source of tree blocks.",
		"You can select the tree blocks to show either when the chart starts up, or using the Tree Block Source submenu of the Chart menu.  (You may request the chart itself by selecting the Tree Blocks item under New Bar & Line Chart in the Analysis menu)");
		EmployeeNeed e3 = registerEmployeeNeed(ItemsCharter.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");
	}
	/*.................................................................................................................*/
	public NumberForTreeBlock numberTask;
	private TreeBlockSource treeBlockSourceTask;
	private Taxa taxa;
	MesquiteString treeBlockSourceName;
	MesquiteString numberTaskName;
	MesquiteSubmenuSpec msNT;
	ChartWindow cWindow;
	ItemsCharter chartWindowTask;
	MesquiteBoolean live = new MesquiteBoolean(true);
	MesquiteCommand tbstC, ntC;
	static int numMade = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		chartWindowTask = (ItemsCharter)hireEmployee(ItemsCharter.class, null);
		if (chartWindowTask == null)
			return sorry(getName() + " couldn't start because no charting module was obtained.");

		makeMenu("Chart");


		//Tree block source %%%%%%%%
		treeBlockSourceName = new MesquiteString();
		tbstC =makeCommand("setTreeBlockSource",  this);
		if (numModulesAvailable(TreeBlockSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Block Source", tbstC, TreeBlockSource.class);
			mss.setSelected(treeBlockSourceName);
		}
		taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a chart of values for tree blocks?");
		if (taxa ==null)
			return sorry(getName() + " couldn't start because no taxa block was obtained.");
		taxa.addListener(this);

		treeBlockSourceTask= (TreeBlockSource)hireEmployee(TreeBlockSource.class, "Source of tree blocks");
		if (treeBlockSourceTask == null)
			return sorry(getName() + " couldn't start because no source of tree blocks was obtained.");
		treeBlockSourceTask.setHiringCommand(tbstC);
		treeBlockSourceName.setValue(treeBlockSourceTask.getName());
		treeBlockSourceTask.setPreferredTaxa(taxa);


		//values etc.  %%%%%%%%
		ntC =makeCommand("setCalculator",  this);
		msNT = addSubmenu(null, "Values", ntC, NumberForTreeBlock.class);

		numberTask=(NumberForTreeBlock)hireEmployee(NumberForTreeBlock.class, "Value to calculate for tree blocks");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module was obtained.");
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString(numberTask.getName());
		msNT.setSelected(numberTaskName);


		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow( cWindow);
		chartWindowTask.setTaxa(taxa);
		chartWindowTask.setNumberTask(numberTask);
		chartWindowTask.setItemsSource(treeBlockSourceTask);
		cWindow.setChartTitle("Tree Blocks Chart " + (++numMade));
		cWindow.resetTitle();
		if (!MesquiteThread.isScripting()){
			cWindow.setChartVisible();
			cWindow.setVisible(true);
			chartWindowTask.doCounts();
			cWindow.toFront();
		}

		resetContainingMenuBar();
		resetAllWindowsMenus();
		//window.setTitle("Trees: " + treeSourceTask.getSourceName());
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public void employeeQuit(MesquiteModule m){
		if (m==chartWindowTask)
			iQuit();
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
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suspendCalculations"); 
		temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa)); 
		temp.addLine("setTreeBlockSource ", treeBlockSourceTask); 
		temp.addLine("setCalculator ", numberTask); 
		temp.addLine("getCharter", chartWindowTask); 
		temp.addLine("setChartVisible"); 
		temp.addLine("resumeCalculations"); 
		temp.addLine("doCounts"); 
		temp.addLine("showWindow");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets block of taxa to use", "[block reference, number, or name]", commandName, "setTaxa")){
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				if (taxa!=null)
					taxa.removeListener(this);
				taxa = t;
				if (taxa!=null)
					taxa.addListener(this);
				if (taxa!=null && treeBlockSourceTask!=null)
					treeBlockSourceTask.setPreferredTaxa(taxa);
				return taxa;
			}
		} 
		else if (checker.compare(this.getClass(), "Requests that chart values be recalculated", null, commandName, "doCounts")){
			if (cWindow!=null)
				chartWindowTask.doCounts();
		}
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspendCalculations")){
			chartWindowTask.incrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resumeCalculations")){
			chartWindowTask.decrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the chart is \"live\" (that is, whether its calculatons are updated automatically when parameters or data change)", "[on or off]", commandName, "setLive")) {
			live.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Returns the chart drawing module", null, commandName, "getCharter")) {
			return chartWindowTask;
		}
		else if (checker.compare(this.getClass(), "Sets the chart to visible", null, commandName, "setChartVisible")) {
			if (cWindow!=null)
				cWindow.setChartVisible();

		}
		else if (checker.compare(getClass(), "Sets the module supplying tree blocks", "[name of module]", commandName, "setTreeBlockSource")) {
			TreeBlockSource temp =   (TreeBlockSource)replaceEmployee(TreeBlockSource.class, arguments, "Source of trees for chart", treeBlockSourceTask);
			if (temp!=null) {
				treeBlockSourceTask = temp;
				treeBlockSourceTask.setHiringCommand(tbstC);
				treeBlockSourceName.setValue(treeBlockSourceTask.getName());
				if (taxa!=null)
					treeBlockSourceTask.setPreferredTaxa(taxa);
				if (cWindow!=null){
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource(treeBlockSourceTask);
					if (!MesquiteThread.isScripting()){
						chartWindowTask.doCounts();
					}
				}
				return treeBlockSourceTask;
			}
			//treeValues -- resize matrix according to number of trees, or if infinite, to chosen number
		}
		else if (checker.compare(getClass(), "Sets the module calculating numbers for the tree blocks", "[name of module]", commandName, "setCalculator")) {
			NumberForTreeBlock temp =  (NumberForTreeBlock)replaceEmployee(NumberForTreeBlock.class, arguments, "Value to calculate for trees", numberTask);
			//((TreeBlockValuesWindow)window).setNumberTask(numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (cWindow!=null){
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource(treeBlockSourceTask);
					if (!MesquiteThread.isScripting()){
						chartWindowTask.doCounts();
					}
				}
				return numberTask;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (cWindow!=null && !MesquiteThread.isScripting() && live.getValue())
			if (Notification.getCode(notification) == MesquiteListener.PARTS_ADDED && source == treeBlockSourceTask) {
				int[] notifParam = notification.getParameters();
				if (notifParam!=null && notifParam.length==2)
					chartWindowTask.doCounts( notifParam[0]+1, notifParam[0]+notifParam[1], false);
				else
					chartWindowTask.doCounts();
			}
			else chartWindowTask.doCounts(); //perhaps put intemschart in charge liveness and pass here whether through ePC
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Tree Blocks";
	}

}


