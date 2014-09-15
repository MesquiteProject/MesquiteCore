/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.TaxaScattergram;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**=== Class TaxaScattergram.  ===*/

public class TaxaScattergram extends FileAssistantCS  {
	/*.................................................................................................................*/
	public String getName() {
		return "Taxa Scattergram";
	}
	public String getExplanation() {
		return "Makes a scatterplot displaying two values (such as proportion of gaps in a sequence, etc.) for a series of taxa.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTaxon.class,getName() + " needs methods to calculate the two values for the taxa.",
		"You can select this either when the chart starts up, or using the Values submenus of the Chart menu.  (You may request the chart itself by selecting the Taxa item under New Scattergram in the Analysis menu)");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(TaxonSource.class, getName() + " needs a source of taxa.",
		"You can select the taxa to show either when the chart starts up, or using the Taxon Source submenu of the Chart menu, if there is more than one source available.  (You may request the chart itself by selecting the Taxa item under New Scattergram in the Analysis menu)");
		EmployeeNeed e3 = registerEmployeeNeed(ItemsBiplotter.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");
	}
	/*.................................................................................................................*/
	public NumberForTaxon numberTaskX, numberTaskY;
	private TaxonSource taxonSourceTask;
	ItemsBiplotter chartWindowTask;
	public Taxa taxa;
	boolean separateAxes=false;
	MesquiteString numberTaskXName, numberTaskYName;
	MesquiteString taxonSourceName;
	ChartWindow cWindow;
	MesquiteCommand tstC, ntC, ntxC, ntyC;
	MesquiteMenuSpec mX;
	MesquiteMenuSpec mY;
	static int numMade = 0;
	MesquiteBoolean showNames;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		if (getProject().getNumberTaxas()==0)
			return sorry(getName() + " couldn't start because no blocks of taxa are available.");
		chartWindowTask = (ItemsBiplotter)hireEmployee(ItemsBiplotter.class, null);
		if (chartWindowTask == null)
			return sorry(getName() + " couldn't start because no charting module was obtained.");

		//Source of Taxa %%%%%%%%
		taxonSourceTask= (TaxonSource)hireEmployee(TaxonSource.class, "Source of taxa (Taxa scattergram)");
		if (taxonSourceTask == null)
			return sorry(getName() + " couldn't start because no source of taxa was obtained.");
		//todo: doesn't use taxonSource!!!!!!!
		tstC = makeCommand("setTaxonSource",  this);
		taxonSourceTask.setHiringCommand(tstC);
		taxonSourceName = new MesquiteString();
		taxonSourceName.setValue(taxonSourceTask.getName());
		if (numModulesAvailable(TaxonSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Taxon Source", tstC, TaxonSource.class);
			mss.setSelected(taxonSourceName);
		}
		makeMenu("Scattergram");


		//values etc.  %%%%%%%%
		ntC = makeCommand("setValues",  this);
		ntxC = makeCommand("setValuesX",  this);
		ntyC = makeCommand("setValuesY",  this);

		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow( cWindow);
		cWindow.setChartTitle("Taxa Scattergram " + (++numMade));
		cWindow.resetTitle();
		taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a scattergram?");
		if (taxa==null)
			return sorry(getName() + " couldn't start because taxa block not obtained.");
		taxa.addListener(this);
		numberTaskXName = new MesquiteString();
		numberTaskYName = new MesquiteString();
		showNames = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Show Taxon Names", MesquiteModule.makeCommand("showNamesToggle",  this), showNames);
		/* ---------------------------*/
		if (!MesquiteThread.isScripting()){
			String expl = "(For instance, the X and Y axes might show the same calculations but for different characters, or they may show two entirely different calculations.)";
			separateAxes = (numModulesAvailable(NumberForTaxonIncr.class)==0) || !AlertDialog.query(containerOfModule(), "Axes", "Choose same or different calculations for the two axes? " + expl, "Same", "Different");
			initMenus();
			if (!separateAxes){
				numberTaskX = (NumberForTaxonIncr)hireEmployee(NumberForTaxonIncr.class, "Values for axes");
				if (numberTaskX == null)
					return false;
				numberTaskXName.setValue(numberTaskX.getName());
				numberTaskX.setHiringCommand(ntC);
				numberTaskY = numberTaskX;
			}
			else {
				numberTaskX = (NumberForTaxon)hireEmployee(NumberForTaxon.class, "Values for X axis");
				if (numberTaskX == null)
					return false;
				numberTaskY = (NumberForTaxon)hireEmployee(NumberForTaxon.class, "Values for Y axis");
				if (numberTaskY == null)
					return false;
				numberTaskX.setMenuToUse(mX);
				numberTaskY.setMenuToUse(mY);
				numberTaskX.setHiringCommand(ntxC);
				numberTaskY.setHiringCommand(ntyC);
				numberTaskXName.setValue(numberTaskX.getName());
				numberTaskYName.setValue(numberTaskY.getName());
			}
			sendParametersToCharter();
			if (!MesquiteThread.isScripting()){
				chartWindowTask.doCounts();
				cWindow.setChartVisible();
				cWindow.setVisible(true);
			}
		}
		resetContainingMenuBar();
		resetAllWindowsMenus();
		/* ---------------------------*/

		return true;
	}
	private void initMenus(){
		if (!separateAxes){
			if (numModulesAvailable(NumberForTaxonIncr.class)>1) {
				MesquiteSubmenuSpec mss = addSubmenu(null, "Values", ntC, NumberForTaxonIncr.class);
				mss.setSelected(numberTaskXName);
			}
		}
		else {
			mX = addAuxiliaryMenu("X.Axis");
			mY = addAuxiliaryMenu("Y.Axis");
			if (numModulesAvailable(NumberForTaxon.class)>1) {
				MesquiteSubmenuSpec mss = addSubmenu(mX, "Values on X", ntxC, NumberForTaxon.class);
				mss.setSelected(numberTaskXName);
				mss = addSubmenu(mY, "Values on Y", ntyC, NumberForTaxon.class);
				mss.setSelected(numberTaskYName);
			}
		}
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
		if (m == chartWindowTask)
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
	void sendParametersToCharter(){
		chartWindowTask.setTaxa(taxa);
		chartWindowTask.setNumberTaskX(numberTaskX);
		chartWindowTask.setNumberTaskY(numberTaskY);
		chartWindowTask.setItemsSource(taxonSourceTask);
		chartWindowTask.setShowNames(showNames.getValue());
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setTaxonSource ", taxonSourceTask); 
		temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa)); 
		if (separateAxes){
			temp.addLine("axesDifferent"); 
			temp.addLine("setValuesX ", numberTaskX); 
			temp.addLine("setValuesY ", numberTaskY); 
		}
		else {
			temp.addLine("axesSame"); 
			temp.addLine("setValues ", numberTaskX); 
		}
		temp.addLine("showNamesToggle " + showNames.toOffOnString()); 
		temp.addLine("getCharter", chartWindowTask);
		temp.addLine("setChartVisible");
		temp.addLine("doCounts");
		temp.addLine("showWindow");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the block of taxa used", "[block reference, number, or name]", commandName, "setTaxa")) {
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				if (taxa!=null)
					taxa.removeListener(this);
				taxa = t;
				if (taxa!=null)
					taxa.addListener(this);
				return taxa;
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether taxon names to be shown or not", "[on or off]", commandName, "showNamesToggle")) {
			showNames.toggleValue(arguments);
			if (chartWindowTask!=null){
				chartWindowTask.setShowNames(showNames.getValue());
				if (!MesquiteThread.isScripting() && cWindow!=null)
					cWindow.recalcChart();
			}

		}
		else if (checker.compare(this.getClass(), "Sets the two axes as using different calculators", null, commandName, "axesDifferent")){
			separateAxes=true;
			initMenus();
		}
		else if (checker.compare(this.getClass(), "Sets the two axes as using the same calculator", null, commandName, "axesSame")){
			separateAxes=false;
			initMenus();
		}
		else if (checker.compare(this.getClass(), "Sets the module to calculate the number for taxa for both axes", "[name of module]", commandName, "setValues")) {
			NumberForTaxonIncr temp =  (NumberForTaxonIncr)replaceEmployee(NumberForTaxonIncr.class, arguments, "Values for axes", numberTaskX);
			if (temp!=null) {
				numberTaskX = temp;
				numberTaskX.setHiringCommand(ntC);
				numberTaskY = numberTaskX;
				numberTaskXName.setValue(numberTaskX.getName());
				if (cWindow!=null){
					sendParametersToCharter();
					if (!MesquiteThread.isScripting())
						chartWindowTask.doCounts();
				}
				return numberTaskX;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the module to calculate the number for taxa for the X axis", "[name of module]", commandName, "setValuesX")) {
			NumberForTaxon temp =  (NumberForTaxon)replaceEmployee(NumberForTaxon.class, arguments, "Values for X axis", numberTaskX);
			if (temp!=null) {
				numberTaskX = temp;
				numberTaskX.setHiringCommand(ntxC);
				numberTaskXName.setValue(numberTaskX.getName());
				if (separateAxes)
					numberTaskX.setMenuToUse(mX);
				if (cWindow!=null){
					sendParametersToCharter();
					if (!MesquiteThread.isScripting())
						chartWindowTask.doCounts();
				}
				return numberTaskX;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the module to calculate the number for taxa for the X axis", "[name of module]", commandName, "setValuesY")) {
			NumberForTaxon temp =  (NumberForTaxon)replaceEmployee(NumberForTaxon.class, arguments, "Values for Y axis", numberTaskY);
			if (temp!=null) {
				numberTaskY = temp;
				numberTaskY.setHiringCommand(ntyC);
				if (separateAxes)
					numberTaskY.setMenuToUse(mY);
				numberTaskYName.setValue(numberTaskY.getName());
				if (cWindow!=null){
					sendParametersToCharter();
					if (!MesquiteThread.isScripting())
						chartWindowTask.doCounts();
				}
				return numberTaskY;
			}
		}
		else if (checker.compare(this.getClass(), "Requests that chart calculations be redone", null, commandName, "doCounts")){
			if (cWindow!=null)
				chartWindowTask.doCounts();
		}
		else if (checker.compare(this.getClass(),  "Returns the chart drawing module", null, commandName, "getCharter")) {
			return chartWindowTask;
		}
		else if (checker.compare(this.getClass(), "Sets the chart as visible", null, commandName, "setChartVisible")) {
			if (cWindow!=null)
				cWindow.setChartVisible();
		}
		else if (checker.compare(this.getClass(), "Sets the module supplying taxa", "[name of module]", commandName, "setTaxonSource")) {
			TaxonSource temp =   (TaxonSource)replaceEmployee(TaxonSource.class, arguments, "Source of taxa for chart", taxonSourceTask);
			if (temp!=null) {
				taxonSourceTask = temp;
				taxonSourceTask.setHiringCommand(tstC);
				taxonSourceName.setValue(taxonSourceTask.getName());
				if (cWindow!=null){
					sendParametersToCharter();
					if (!MesquiteThread.isScripting())
						chartWindowTask.doCounts();
				}
				return taxonSourceTask;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		chartWindowTask.doCounts();
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Taxa";
	}

}


