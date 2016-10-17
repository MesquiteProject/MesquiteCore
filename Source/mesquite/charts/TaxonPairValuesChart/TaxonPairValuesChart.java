/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.TaxonPairValuesChart;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**=== Class TaxonPairValuesChart.  ===*/

public class TaxonPairValuesChart extends FileAssistantCH  {
	/*.................................................................................................................*/
	public String getName() {
		return "Taxon Pairs Histogram";
	}
	public String getExplanation() {
		return "Makes a chart showing some value for each of many pairs of taxa.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberFor2Taxa.class, getName() + " needs a method to calculate values (such as distance, etc.) for each of a series of taxon pairs.",
		"You can select this either when the chart starts up, or using the Values submenu of the Chart menu.  (You may request the chart itself by selecting the Taxon Pairs item under New Bar & Line Chart in the Analysis menu)");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(TaxonPairSource.class, getName() + " needs a source of taxon pairs.",
		"You can select the taxon pairs to show either when the chart starts up, or using the Taxon Pair Source submenu of the Chart menu, if there is more than one source available.  (You may request the chart itself by selecting the Taxon Pairs item under New Bar & Line Chart in the Analysis menu)");
		EmployeeNeed e3 = registerEmployeeNeed(ItemsCharter.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");
	}

	/*.................................................................................................................*/
	public NumberFor2Taxa numberTask;
	private TaxonPairSource taxonPairSourceTask;
	ItemsCharter chartWindowTask;
	ChartWindow cWindow;
	private Taxa taxa;
	MesquiteString taxonPairSourceName;
	MesquiteString numberTaskName;
	MesquiteSubmenuSpec msNT;
	int suspend = 0;
	MesquiteCommand tstC, ntC;
	static int numMade = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		chartWindowTask = (ItemsCharter)hireEmployee(ItemsCharter.class, null);
		if (chartWindowTask == null)
			return sorry(getName() + " couldn't start because no charting module was obtained.");
		taxonPairSourceName = new MesquiteString();
		tstC = makeCommand("setTaxonPairSource",  this);
		ntC = makeCommand("setCalculator",  this);
		if (numModulesAvailable(TaxonPairSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Taxon Pair Source", tstC, TaxonPairSource.class);
			mss.setSelected(taxonPairSourceName);
		}
		makeMenu("Chart");
		msNT = addSubmenu(null, "Values", ntC, NumberFor2Taxa.class);
		return checkInitialize();
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		if (m==chartWindowTask)
			iQuit();
	}
	/*.................................................................................................................*/
	private boolean checkInitialize(){

		taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a chart of taxon pair values?");
		if (taxa ==null)
			return false;
		taxa.addListener(this);
		numberTask=(NumberFor2Taxa)hireEmployee(NumberFor2Taxa.class, "Value to calculate for taxon pairs");
		if (numberTask == null)
			return false;
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString(numberTask.getName());
		msNT.setSelected(numberTaskName);

		taxonPairSourceTask= (TaxonPairSource)hireEmployee(TaxonPairSource.class, "Source of taxa (Taxon pair values chart)");
		if (taxonPairSourceTask == null)
			return false;
		taxonPairSourceTask.setHiringCommand(tstC);
		taxonPairSourceName.setValue(taxonPairSourceTask.getName());

		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow( cWindow);
		chartWindowTask.setTaxa(taxa);
		chartWindowTask.setNumberTask(numberTask);
		chartWindowTask.setItemsSource(taxonPairSourceTask);
		cWindow.setChartTitle("Taxon Pairs Chart " + (++numMade));
		cWindow.resetTitle();
		if (!MesquiteThread.isScripting()){
			cWindow.setChartVisible();
			cWindow.setVisible(true);
			chartWindowTask.doCounts();
			cWindow.toFront();
	}

		resetContainingMenuBar();
		resetAllWindowsMenus();
		//window.setTitle("Trees: " + taxonPairSourceTask.getSourceName());
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
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
		temp.addLine("setTaxonPairSource ", taxonPairSourceTask); 
		temp.addLine("setCalculator ", numberTask); 
		temp.addLine("getCharter", chartWindowTask); 
		temp.addLine("setChartVisible"); 
		temp.addLine("doCounts"); 
		temp.addLine("showWindow");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the chart visible", null, commandName, "setChartVisible")) {
			if (cWindow!=null)
				cWindow.setChartVisible();
		}
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspendCalculations")){
			suspend++;
		}
		else if (checker.compare(this.getClass(),  "Resumes calculations", null, commandName, "resumeCalculations")){
			suspend--;
		}
		else if (checker.compare(this.getClass(), "Requests that calculations are redone", null, commandName, "doCounts")){
			if (cWindow!=null)
				chartWindowTask.doCounts();
		}
		else if (checker.compare(this.getClass(), "Returns the chart drawing module", null, commandName, "getCharter")) {
			return chartWindowTask;
		}
		else if (checker.compare(this.getClass(), "Sets the source of taxon pairs", "[name of module]", commandName, "setTaxonPairSource")) {
			TaxonPairSource temp =   (TaxonPairSource)replaceEmployee(TaxonPairSource.class, arguments, "Source of taxa for chart", taxonPairSourceTask);
			if (temp!=null) {
				taxonPairSourceTask = temp;
				taxonPairSourceTask.setHiringCommand(tstC);
				taxonPairSourceName.setValue(taxonPairSourceTask.getName());
				if (cWindow!=null){
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource(taxonPairSourceTask);
					if (!MesquiteThread.isScripting()){
						chartWindowTask.doCounts();
					}
				}
				return taxonPairSourceTask;
			}
			//treeValues -- resize matrix according to number of trees, or if infinite, to chosen number
		}
		else if (checker.compare(this.getClass(), "Sets the module calculating the numbers for the taxon pairs", "[name of module]", commandName, "setCalculator")) {
			NumberFor2Taxa temp =  (NumberFor2Taxa)replaceEmployee(NumberFor2Taxa.class, arguments, "Value to calculate for taxon pairs", numberTask);
			//((TreesChartWindow)window).setNumberTask(numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (cWindow!=null){
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource(taxonPairSourceTask);
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
		if (cWindow!=null) {
			if (Notification.getCode(notification) == MesquiteListener.PARTS_ADDED && source == taxonPairSourceTask) {
				int[] notifParam = notification.getParameters();
				if (notifParam!=null && notifParam.length==2)
					chartWindowTask.doCounts( notifParam[0]+1, notifParam[0]+notifParam[1], false);
				else
					chartWindowTask.doCounts();
			}
			else chartWindowTask.doCounts();
		}
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Taxon Pairs";
	}

}


