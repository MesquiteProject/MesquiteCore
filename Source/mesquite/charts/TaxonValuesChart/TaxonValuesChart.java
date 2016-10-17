/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.TaxonValuesChart;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**=== Class TaxonValuesChart.  ===*/

public class TaxonValuesChart extends FileAssistantCH  {
	/*.................................................................................................................*/
	public String getName() {
		return "Bar & Line Chart for Taxa";
	}
	public String getExplanation() {
		return "Makes a chart displaying values (such as proportion of gaps in a sequence, etc.) for each of a series of taxa.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTaxon.class, getName() + " needs a method to calculate the values for the taxa.",
		"You can select this either when the chart starts up, or using the Values submenu of the Chart menu.  (You may request the chart itself by selecting the Taxa item under New Bar & Line Chart in the Analysis menu)");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(TaxonSource.class, getName() + " needs a source of taxa.",
		"You can select the taxa to show either when the chart starts up, or using the Taxon Source submenu of the Chart menu, if there is more than one source available.  (You may request the chart itself by selecting the Taxa item under New Bar & Line Chart in the Analysis menu)");
		EmployeeNeed e3 = registerEmployeeNeed(ItemsCharter.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");
	}
	/*.................................................................................................................*/
	public NumberForTaxon numberTask;
	private TaxonSource taxonSourceTask;
	ItemsCharter chartWindowTask;
	ChartWindow cWindow;
	private Taxa taxa;
	MesquiteString taxonSourceName;
	MesquiteString numberTaskName;
	MesquiteSubmenuSpec msNT;
	int suspend = 0;
	MesquiteCommand tstC, ntC;
	static int numMade = 0;
	long tbID = -1;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		chartWindowTask = (ItemsCharter)hireEmployee(ItemsCharter.class, null);
		if (chartWindowTask == null)
			return sorry(getName() + " couldn't start because no charting module was obtained.");
		taxonSourceName = new MesquiteString();
		tstC = makeCommand("setTaxonSource",  this);
		ntC = makeCommand("setCalculator",  this);
		if (numModulesAvailable(TaxonSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Taxon Source", tstC, TaxonSource.class);
			mss.setSelected(taxonSourceName);
		}
		//can leave a hint in terms of an id of a treeblock to use
		String whichBlock = MesquiteThread.retrieveAndDeleteHint(this);
		long wB = MesquiteLong.fromString(whichBlock);
		if (MesquiteLong.isCombinable(wB)){
			tbID = wB;
		}
		makeMenu("Chart");
		msNT = addSubmenu(null, "Values", ntC, NumberForTaxon.class);
		return checkInitialize();
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
	private boolean checkInitialize(){
		if (tbID>=0){
			taxa = getProject().getTaxaByID(tbID);
			tbID = -1;
		}
		if (taxa == null)
			taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a chart of taxon values?");
		if (taxa ==null)
			return false;
		taxa.addListener(this);
		numberTask=(NumberForTaxon)hireEmployee(NumberForTaxon.class, "Value to calculate for taxa");
		if (numberTask == null)
			return false;
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString(numberTask.getName());
		msNT.setSelected(numberTaskName);

		taxonSourceTask= (TaxonSource)hireEmployee(TaxonSource.class, "Source of taxa (Taxon values chart)");
		if (taxonSourceTask == null)
			return false;
		taxonSourceTask.setHiringCommand(tstC);
		taxonSourceName.setValue(taxonSourceTask.getName());

		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow( cWindow);
		chartWindowTask.setTaxa(taxa);
		chartWindowTask.setNumberTask(numberTask);
		chartWindowTask.setItemsSource(taxonSourceTask);
		cWindow.setChartTitle("Taxa Chart " + (++numMade));
		cWindow.resetTitle();
		if (!MesquiteThread.isScripting()){
			cWindow.setChartVisible();
			cWindow.setVisible(true);
			chartWindowTask.doCounts();
			cWindow.toFront();
		}

		resetContainingMenuBar();
		resetAllWindowsMenus();
		//window.setTitle("Trees: " + taxonSourceTask.getSourceName());
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
		temp.addLine("setTaxonSource ", taxonSourceTask); 
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
		else if (checker.compare(this.getClass(), "Sets the source of taxa", "[name of module]", commandName, "setTaxonSource")) {
			TaxonSource temp =   (TaxonSource)replaceEmployee(TaxonSource.class, arguments, "Source of taxa for chart", taxonSourceTask);
			if (temp!=null) {
				taxonSourceTask = temp;
				taxonSourceTask.setHiringCommand(tstC);
				taxonSourceName.setValue(taxonSourceTask.getName());
				if (cWindow!=null){
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource(taxonSourceTask);
					if (!MesquiteThread.isScripting()){
						chartWindowTask.doCounts();
					}
				}
				return taxonSourceTask;
			}
			//treeValues -- resize matrix according to number of trees, or if infinite, to chosen number
		}
		else if (checker.compare(this.getClass(), "Sets the module calculating the numbers for the taxa", "[name of module]", commandName, "setCalculator")) {
			NumberForTaxon temp =  (NumberForTaxon)replaceEmployee(NumberForTaxon.class, arguments, "Value to calculate for taxa", numberTask);
			//((TreesChartWindow)window).setNumberTask(numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (cWindow!=null){
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource(taxonSourceTask);
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
			if (Notification.getCode(notification) == MesquiteListener.PARTS_ADDED && source == taxonSourceTask) {
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
		return "Taxa";
	}

}


