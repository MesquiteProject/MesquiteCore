/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.TreeValuesChart;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.FileAssistantCH;
import mesquite.lib.duties.ItemsCharter;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.duties.TreeSource;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ChartWindow;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
/**=== Class TreeValuesChart.  ===*/

public class TreeValuesChart extends FileAssistantCH  {
	/*.................................................................................................................*/
	public String getName() {
		return "Bar & Line Chart for Trees";
	}
	public String getExplanation() {
		return "Makes a chart displaying values (such as likelihoods, parsimony scores, imbalance statistics, correlations,etc.) for a series of trees.";
	}

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTree.class, getName() + " needs a method to calculate the values for the trees.",
		"You can select this either when the chart starts up, or using the Values submenu of the Chart menu.  (You may request the chart itself by selecting the Trees item under New Bar & Line Chart in the Analysis menu)");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(TreeSource.class, getName() + " needs a source of trees.",
		"You can select the trees to show either when the chart starts up, or using the Tree Source submenu of the Chart menu.  (You may request the chart itself by selecting the Trees item under New Bar & Line Chart in the Analysis menu)");
		e2.setPriority(1);
		EmployeeNeed e3 = registerEmployeeNeed(ItemsCharter.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");
	}
	/*.................................................................................................................*/
	public NumberForTree numberTask;
	private TreeSource treeSourceTask;
	ItemsCharter chartWindowTask;
	ChartWindow cWindow;
	private Taxa taxa;
	MesquiteString treeSourceName;
	MesquiteString numberTaskName;
	MesquiteSubmenuSpec msNT;
	MesquiteCommand tstC, ntC;
	static int numMade = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		chartWindowTask = (ItemsCharter)hireEmployee(ItemsCharter.class, null);
		if (chartWindowTask == null)
			return sorry(getName() + " couldn't start because no charting module obtained.");
		makeMenu("Chart");
		treeSourceName = new MesquiteString();
		tstC = makeCommand("setTreeSource",  this);
		ntC =makeCommand("setCalculator",  this);
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source", tstC, TreeSource.class);
			mss.setSelected(treeSourceName);
		}


		msNT = addSubmenu(null, "Values", ntC, NumberForTree.class);
		//is there a hint as to taxa
		String whichTaxa = MesquiteThread.retrieveAndDeleteHint(this);
		long wB = MesquiteLong.fromString(whichTaxa);
		if (MesquiteLong.isCombinable(wB)){
			taxa = getProject().getTaxaByID(wB);
		}

		if (taxa == null)
			taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a chart of values for trees?");
		if (taxa ==null)
			return sorry(getName() + " couldn't start because no block of taxa available.");
		taxa.addListener(this);

		//Tree source %%%%%%%%
		if (arguments == null){
			arguments = MesquiteThread.retrieveAndDeleteHint(this);
		}
		if (arguments != null)
			treeSourceTask = (TreeSource)hireNamedEmployee(TreeSource.class, arguments);
		if (treeSourceTask == null)
		treeSourceTask= (TreeSource)hireEmployee(TreeSource.class, "Source of trees (Trees chart)");
		if (treeSourceTask == null)
			return sorry(getName() + " couldn't start because no source of trees obtained.");
		treeSourceName.setValue(treeSourceTask.getName());
		treeSourceTask.setPreferredTaxa(taxa);
		treeSourceTask.setHiringCommand(tstC);


		//values etc.  %%%%%%%%
		if (MesquiteThread.isScripting())
			numberTask=(NumberForTree)hireNamedEmployee(NumberForTree.class, "#NumberOfTaxa");
		if (numberTask == null)
			numberTask=(NumberForTree)hireEmployee(NumberForTree.class, "Value to calculate for trees");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString(numberTask.getName());
		msNT.setSelected(numberTaskName);

		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow( cWindow);
		chartWindowTask.setTaxa(taxa);
		chartWindowTask.setNumberTask(numberTask);
		chartWindowTask.setItemsSource(treeSourceTask);
		cWindow.setChartTitle("Trees Chart " + (++numMade));
		cWindow.resetTitle();
		if (!MesquiteThread.isScripting()){
			cWindow.setChartVisible();
			cWindow.setVisible(true);
			chartWindowTask.doCounts();
			cWindow.toFront();
		}

		resetContainingMenuBar();
		resetAllWindowsMenus();
		//getModuleWindow().setTitle("Trees: " + treeSourceTask.getSourceName());
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public void employeeQuit(MesquiteModule m){
		if (m== chartWindowTask)
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
		temp.addLine("setTreeSource ", treeSourceTask); 
		temp.addLine("setCalculator ", numberTask); 
		temp.addLine("getCharter", chartWindowTask); 
		temp.addLine("setChartVisible"); 
		temp.addLine("doCounts"); 
		temp.addLine("resumeCalculations"); 
		temp.addLine("showWindow");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the block of taxa used", "[block reference, number, or name]", commandName, "setTaxa")){
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				if (taxa!=null)
					taxa.removeListener(this);
				taxa = t;
				if (taxa!=null)
					taxa.addListener(this);
				if (taxa!=null && treeSourceTask!=null)
					treeSourceTask.setPreferredTaxa(taxa);
				return taxa;
			}
		} 
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspendCalculations")){
			chartWindowTask.incrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resumeCalculations")){
			chartWindowTask.decrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Sets the chart to visible", null, commandName, "setChartVisible")) {
			if (cWindow!=null)
				cWindow.setChartVisible();

		}
		else if (checker.compare(this.getClass(), "Requests that chart calculations be redone", null, commandName, "doCounts")){
			if (cWindow!=null) {
				chartWindowTask.doCounts();
			}
		}
		else if (checker.compare(this.getClass(), "Returns the chart drawing module", null, commandName, "getCharter")) {
			return chartWindowTask;
		}
		else if (checker.compare(this.getClass(), "Sets the module supplying trees", "[name of module]", commandName, "setTreeSource")) {
			TreeSource temp =   (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees for chart", treeSourceTask);
			if (temp!=null) {
				treeSourceTask = temp;
				treeSourceTask.setHiringCommand(tstC);
				treeSourceName.setValue(treeSourceTask.getName());
				if (taxa!=null)
					treeSourceTask.setPreferredTaxa(taxa);
				if (cWindow!=null){
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource(treeSourceTask);
					if (!MesquiteThread.isScripting()){
						chartWindowTask.doCounts();
					}
				}
				return treeSourceTask;
			}
			//treeValues -- resize matrix according to number of trees, or if infinite, to chosen number
		}
		else if (checker.compare(this.getClass(), "Sets the module calculating values for the trees", "[name of module]", commandName, "setCalculator")) {
			NumberForTree temp =  (NumberForTree)replaceEmployee(NumberForTree.class, arguments, "Value to calculate for trees", numberTask);
			//((TreesChartWindow)getModuleWindow()).setNumberTask(numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (cWindow!=null){
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource(treeSourceTask);
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
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (cWindow!=null && !MesquiteThread.isScripting()) {
			if (Notification.getCode(notification) == MesquiteListener.ITEMS_ADDED && source == treeSourceTask) {
				int[] notifParam = notification.getParameters();
				if (notifParam!=null && notifParam.length==2) {
					// we now have just some trees added.  The first tree added as notifParam[0]+1, and notifParam[1] trees are added.


					chartWindowTask.doCounts( notifParam[0], MesquiteInteger.add(notifParam[0], notifParam[1]), false);
				}
				else
					chartWindowTask.doCounts();
			}
			else if (Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED) {
				chartWindowTask.doCounts();
			}

		}
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Trees";
	}
}


