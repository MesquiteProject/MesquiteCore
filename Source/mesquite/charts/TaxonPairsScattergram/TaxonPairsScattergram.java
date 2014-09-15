/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charts.TaxonPairsScattergram;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
/* TODO:
 *   - how can one change which distance measure to use if distance from character matrix chosen?
 *   - how should selection work?
 */
/* ======================================================================== */
/**=== Class TaxonPairScattergram.  ===*/

public class TaxonPairsScattergram extends FileAssistantCS  {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberFor2Taxa.class, getName() + " needs methods to calculate two values (such as distance, etc.) for a series of taxon pairs.",
		"You can select this either when the chart starts up, or using the Values submenus of the Chart menu.  (You may request the chart itself by selecting the Taxon Pairs item under New Scattergram in the Analysis menu)");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(TaxonPairSource.class, getName() + " needs a source of taxon pairs.",
		"You can select the taxon pairs to show either when the chart starts up, or using the Taxon Source submenu of the Chart menu, if there is more than one source available.  (You may request the chart itself by selecting the Taxon Pairs item under New Scattergram in the Analysis menu)");
		EmployeeNeed e3 = registerEmployeeNeed(ItemsBiplotter.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");
	}
		/*.................................................................................................................*/
	public NumberFor2Taxa numberTaskX, numberTaskY;
	private TaxonPairSource taxonPairSourceTask;
	ItemsBiplotter chartWindowTask;
	public Taxa taxa;
	boolean separateAxes=false;
	MesquiteString numberTaskXName, numberTaskYName;
	MesquiteString taxonPairSourceName;
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
		taxonPairSourceTask= (TaxonPairSource)hireEmployee(TaxonPairSource.class, "Source of taxa (Taxa scattergram)");
		if (taxonPairSourceTask == null)
			return sorry(getName() + " couldn't start because no source of taxa was obtained.");
			//todo: doesn't use TaxonPairSource!!!!!!!
		tstC = makeCommand("setTaxonPairSource",  this);
		taxonPairSourceTask.setHiringCommand(tstC);
		taxonPairSourceName = new MesquiteString();
		taxonPairSourceName.setValue(taxonPairSourceTask.getName());
		if (numModulesAvailable(TaxonPairSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Taxon Pair Source", tstC, TaxonPairSource.class);
			mss.setSelected(taxonPairSourceName);
		}
		makeMenu("Scattergram");

		

		//values etc.  %%%%%%%%
 		ntC = makeCommand("setValues",  this);
 		ntxC = makeCommand("setValuesX",  this);
 		ntyC = makeCommand("setValuesY",  this);
			
 		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow( cWindow);
		cWindow.setChartTitle("Taxon Pair Scattergram " + (++numMade));
		cWindow.resetTitle();
	 	taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a scattergram?");
	 	if (taxa==null)
	 		return sorry(getName() + " couldn't start because taxa block not obtained.");
	 	taxa.addListener(this);
 		numberTaskXName = new MesquiteString();
 		numberTaskYName = new MesquiteString();
		showNames = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Show Taxon Pair Names", MesquiteModule.makeCommand("showNamesToggle",  this), showNames);
		/* ---------------------------*/
		if (!MesquiteThread.isScripting()){
			String expl = "(For instance, the X and Y axes might show the same calculations but for different characters, or they may show two entirely different calculations.)";
			separateAxes = (numModulesAvailable(NumberFor2TaxaIncr.class)==0) || !AlertDialog.query(containerOfModule(), "Axes", "Choose same or different calculations for the two axes? " + expl, "Same", "Different");
			initMenus();
			if (!separateAxes){
				numberTaskX = (NumberFor2TaxaIncr)hireEmployee(NumberFor2TaxaIncr.class, "Values for axes");
				if (numberTaskX == null)
					return false;
		 		numberTaskXName.setValue(numberTaskX.getName());
				numberTaskX.setHiringCommand(ntC);
				numberTaskY = numberTaskX;
			}
			else {
				numberTaskX = (NumberFor2Taxa)hireEmployee(NumberFor2Taxa.class, "Values for X axis");
				if (numberTaskX == null)
					return false;
				numberTaskY = (NumberFor2Taxa)hireEmployee(NumberFor2Taxa.class, "Values for Y axis");
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
				if (numModulesAvailable(NumberFor2TaxaIncr.class)>1) {
					MesquiteSubmenuSpec mss = addSubmenu(null, "Values", ntC, NumberFor2TaxaIncr.class);
			 		if (numberTaskX!=null)
			 			numberTaskXName.setValue(numberTaskX.getName());
		 			mss.setSelected(numberTaskXName);
				}
			}
			else {
				mX = addAuxiliaryMenu("X.Axis");
				mY = addAuxiliaryMenu("Y.Axis");
				if (numModulesAvailable(NumberFor2Taxa.class)>1) {
					MesquiteSubmenuSpec mss = addSubmenu(mX, "Values on X", ntxC, NumberFor2Taxa.class);
			 		if (numberTaskX!=null)
			 			numberTaskXName.setValue(numberTaskX.getName());
			 		mss.setSelected(numberTaskXName);
					mss = addSubmenu(mY, "Values on Y", ntyC, NumberFor2Taxa.class);
			 		if (numberTaskY!=null)
			 			numberTaskYName.setValue(numberTaskY.getName());
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
		chartWindowTask.setItemsSource(taxonPairSourceTask);
		chartWindowTask.setShowNames(showNames.getValue());
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
 	 	temp.addLine("setTaxonPairSource ", taxonPairSourceTask); 
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
    		 else if (checker.compare(this.getClass(), "Sets whether taxon pair names to be shown or not", "[on or off]", commandName, "showNamesToggle")) {
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
    			 NumberFor2TaxaIncr temp =  (NumberFor2TaxaIncr)replaceEmployee(NumberFor2TaxaIncr.class, arguments, "Values for axes", numberTaskX);
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
    	 		NumberFor2Taxa temp =  (NumberFor2Taxa)replaceEmployee(NumberFor2Taxa.class, arguments, "Values for X axis", numberTaskX);
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
    	 		NumberFor2Taxa temp =  (NumberFor2Taxa)replaceEmployee(NumberFor2Taxa.class, arguments, "Values for Y axis", numberTaskY);
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
    	 	else if (checker.compare(this.getClass(), "Sets the module supplying taxa", "[name of module]", commandName, "setTaxonPairSource")) {
    	 		TaxonPairSource temp =   (TaxonPairSource)replaceEmployee(TaxonPairSource.class, arguments, "Source of taxa for chart", taxonPairSourceTask);
 			if (temp!=null) {
 				taxonPairSourceTask = temp;
				taxonPairSourceTask.setHiringCommand(tstC);
				taxonPairSourceName.setValue(taxonPairSourceTask.getName());
				if (cWindow!=null){
					sendParametersToCharter();
					if (!MesquiteThread.isScripting())
						chartWindowTask.doCounts();
 				}
	 			return taxonPairSourceTask;
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
    	 public String getName() {
		return "Taxon Pairs Scattergram";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Taxon Pairs";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Makes a chart comparing two values for each of many pairs of taxa.";
   	 }
   	 
}


