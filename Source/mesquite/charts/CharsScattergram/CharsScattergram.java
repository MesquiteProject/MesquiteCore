/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.CharsScattergram;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**=== Class CharsScattergram.  ===*/

public class CharsScattergram extends FileAssistantCS  {
	/*.................................................................................................................*/
	public String getName() {
		return "Characters Scattergram";
	}
	public String getExplanation() {
		return "Makes a scatterplot displaying two values (such as likelihoods, parsimony scores, etc.) for a series of characters.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForCharacter.class, getName() + " needs methods to calculate the two values for the characters.",
		"You can select this either when the chart starts up, or using the Values submenus of the Chart menu.  (You may request the chart itself by selecting the Characters item under New Scattergram in the Analysis menu)");
		e.setPriority(2);
		EmployeeNeed e2 = registerEmployeeNeed(CharSourceCoordObed.class, getName() + " needs a source of characters.",
		"You can select the characters to show either when the chart starts up, or using the Source of Characters submenu of the Chart menu.  (You may request the chart itself by selecting the Characters item under New Scattergram in the Analysis menu)");
		e2.setPriority(2);
		EmployeeNeed e3 = registerEmployeeNeed(ItemsBiplotter.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");
	}
	/*.................................................................................................................*/
	public NumberForCharacter numberTaskX, numberTaskY, numberTaskZ;
	private CharSourceCoordObed charSourceTask;
	ItemsBiplotter chartWindowTask;
	public Taxa taxa;
	boolean separateAxes=false;
	public MesquiteBoolean showColors = new MesquiteBoolean(false);
	MesquiteString numberTaskXName, numberTaskYName, numberTaskZName;
	MesquiteString itemSourceName;
	MesquiteMenuItemSpec setColorsItem;
	ChartWindow cWindow;
	MesquiteCommand  ntxC, ntyC, ntC;
	MesquiteMenuSpec colorsMenu;
	static int numMade = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		makeMenu("Scattergram");
		colorsMenu = addAuxiliaryMenu("Colors");
		if (getProject().getNumberTaxas()==0)
			return sorry(getName() + " couldn't start because there are no blocks of taxa available");
		chartWindowTask = (ItemsBiplotter)hireEmployee(ItemsBiplotter.class, null);
		if (chartWindowTask == null)
			return sorry(getName() + " couldn't start because no charting module was obtained.");

		//Source of characters %%%%%%%%
		charSourceTask= (CharSourceCoordObed)hireEmployee(CharSourceCoordObed.class, "Source of characters (Characters scattergram)");
		if (charSourceTask==null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		itemSourceName = new MesquiteString();
		itemSourceName.setValue(charSourceTask.getName());


		//values etc.  %%%%%%%%
		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow(cWindow);
		cWindow.setChartTitle("Characters Scattergram " + (++numMade));
		cWindow.resetTitle();
		taxa = getProject().getTaxa(0); // as default;
		numberTaskXName = new MesquiteString();
		numberTaskYName = new MesquiteString();
		numberTaskZName = new MesquiteString();
		ntxC =makeCommand("setValuesX",  this);
		ntyC =makeCommand("setValuesY",  this);
		ntC =makeCommand("setValues",  this);
		/* ---------------------------*/
		if (!MesquiteThread.isScripting()){
			if (getProject().getNumberTaxas()>1 && !MesquiteThread.isScripting()) {
				Taxa t = (Taxa) ListDialog.queryList(containerOfModule(), "Taxa", "Choose set of taxa",MesquiteString.helpString,  getProject().getTaxas(), 0);
				if (t!=null) {
					taxa.removeListener(this);
					taxa = t;
					taxa.addListener(this);
				}
			}
			String expl = "(For instance, the X and Y axes might show the same calculations but for different trees, or they may show two entirely different calculations.)";
			separateAxes = (numModulesAvailable(NumberForCharacterIncr.class)==0) || !AlertDialog.query(containerOfModule(), "Axes", "Choose same or different calculations for the two axes? " + expl, "Same", "Different");
			if (!separateAxes){
				numberTaskX = (NumberForCharacter)hireEmployee(NumberForCharacterIncr.class, "Values for axes");
				if (numberTaskX == null)
					return sorry(getName() + " couldn't start because no calculator module was obtained.");
				if (numModulesAvailable(NumberForCharacterIncr.class)>1) {
					MesquiteSubmenuSpec mss = addSubmenu(null, "Values for axes", ntC, NumberForCharacterIncr.class);
					mss.setSelected(numberTaskXName);
				}
				numberTaskXName.setValue(numberTaskX.getName());
				numberTaskX.setHiringCommand(ntC);
				numberTaskY = numberTaskX;
			}
			else {
				numberTaskX = (NumberForCharacter)hireEmployee(NumberForCharacter.class, "Values for X axis");
				if (numberTaskX == null)
					return sorry(getName() + " couldn't start because no calculator module was obtained for the X axis.");
				numberTaskX.setHiringCommand(ntxC);
				numberTaskY = (NumberForCharacter)hireEmployee(NumberForCharacter.class, "Values for Y axis");
				if (numberTaskY == null)
					return sorry(getName() + " couldn't start because no calculator module was obtained for the Y axis.");
				numberTaskY.setHiringCommand(ntyC);
				if (numModulesAvailable(NumberForCharacter.class)>1) {
					MesquiteSubmenuSpec mss = addSubmenu(null, "Values on X", ntxC, NumberForCharacter.class);
					mss.setSelected(numberTaskXName);
					mss = addSubmenu(null, "Values on Y", ntyC, NumberForCharacter.class);
					mss.setSelected(numberTaskYName);
				}
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
		else 
			taxa.addListener(this);
		addCheckMenuItem(colorsMenu, "Color by third value", MesquiteModule.makeCommand("showColors",  this), showColors);
		resetMenus();
		resetContainingMenuBar();
		resetAllWindowsMenus();
		/* ---------------------------*/

		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	void resetMenus(){
		if (showColors.getValue()){
			if (setColorsItem == null) {
				setColorsItem = addSubmenu(colorsMenu, "Values for colors", MesquiteModule.makeCommand("setColorValues",  this), NumberForCharacter.class);
				((MesquiteSubmenuSpec)setColorsItem).setSelected(numberTaskZName);
			}

		}
		else {
			deleteMenuItem(setColorsItem);
			setColorsItem = null;
		}
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)obj == taxa) {
			iQuit();
		}
	}
	public void employeeQuit(MesquiteModule m){
		if (m == chartWindowTask || m== charSourceTask)
			iQuit();
	}
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
	}
	void sendParametersToCharter(){
		chartWindowTask.setTaxa(taxa);
		chartWindowTask.setNumberTaskX(numberTaskX);
		chartWindowTask.setNumberTaskY(numberTaskY);
		chartWindowTask.setNumberTaskZ(numberTaskZ); ///
		chartWindowTask.setItemsSource(charSourceTask);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getCharacterSource ", charSourceTask); 
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
		if (showColors.getValue() && numberTaskZ!=null) {
			temp.addLine("showColors " + showColors.toOffOnString());
			temp.addLine("setColorValues ", numberTaskZ);
		}
		temp.addLine("getCharter", chartWindowTask);
		temp.addLine("setChartVisible");
		temp.addLine("doCounts");
		temp.addLine("showWindow");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the block of taxa", "[block reference, number, or name]", commandName, "setTaxa")) {
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
		else if (checker.compare(this.getClass(), "Sets chart to use different calculators for x and y axes", null, commandName, "axesDifferent")){
			separateAxes=true;
			if (numModulesAvailable(NumberForCharacter.class)>1) {
				MesquiteSubmenuSpec mss = addSubmenu(null, "Values on X", ntxC, NumberForCharacter.class);
				mss.setSelected(numberTaskXName);
				mss = addSubmenu(null, "Values on Y", ntyC, NumberForCharacter.class);
				mss.setSelected(numberTaskYName);
			}
			//todo: this should shut off values-same menu items
		}
		else if (checker.compare(this.getClass(), "Sets chart to use same calculators for x and y axes", null, commandName, "axesSame")){
			separateAxes=false;
			if (numModulesAvailable(NumberForCharacterIncr.class)>1) {
				MesquiteSubmenuSpec mss = addSubmenu(null, "Values for axes", ntC, NumberForCharacterIncr.class);
				mss.setSelected(numberTaskXName);
			}
			//todo: this should shut off values-different menu items
		}
		else if (checker.compare(this.getClass(), "Colors the markers by a third number", "[on or off]", commandName, "showColors")) {
			boolean wasShow = showColors.getValue();
			showColors.toggleValue(parser.getFirstToken(arguments));
			if (showColors.getValue() && !wasShow){
				if (!MesquiteThread.isScripting()){
					NumberForCharacter temp = (NumberForCharacter)hireEmployee(NumberForCharacter.class, "Values by which to color spots in scattergram");
					if (temp!=null) {
						numberTaskZ = temp;
						numberTaskZName.setValue(numberTaskZ.getName());
						numberTaskZ.setMenuToUse(colorsMenu);
						if (cWindow!=null){
							sendParametersToCharter();
							if (!MesquiteThread.isScripting())
								chartWindowTask.doCounts();
						}
					}
				}
				resetMenus();
				resetContainingMenuBar();
				return numberTaskZ;
			}
			else if (!showColors.getValue()  && wasShow) {
				if (numberTaskZ!=null)
					fireEmployee(numberTaskZ);
				numberTaskZ = null;
				if (cWindow!=null){
					sendParametersToCharter();
					if (!MesquiteThread.isScripting())
						chartWindowTask.doCounts();
				}
			}
			resetMenus();
			resetContainingMenuBar();
		}
		else if (checker.compare(this.getClass(), "Sets the module calculating numbers for characters used for the colors of markers", "[name of module]", commandName, "setColorValues")) {
			NumberForCharacter temp =  (NumberForCharacter)replaceEmployee(NumberForCharacter.class, arguments, "Values by which to color spots in scattergram", numberTaskZ);
			if (temp!=null) {
				numberTaskZ = temp;
				numberTaskZName.setValue(numberTaskZ.getName());
				numberTaskZ.setMenuToUse(colorsMenu);
				if (cWindow!=null){
					sendParametersToCharter();
					if (!MesquiteThread.isScripting())
						chartWindowTask.doCounts();
				}
				return numberTaskZ;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the module calculating numbers for characters for the axes", "[name of module]", commandName, "setValues")) {
			NumberForCharacter temp =  (NumberForCharacter)replaceEmployee(NumberForCharacterIncr.class, arguments, "Values for axes", numberTaskX);
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
		else if (checker.compare(this.getClass(), "Sets the module calculating numbers for characters for the X axis", "[name of module]", commandName, "setValuesX")) {
			NumberForCharacter temp =  (NumberForCharacter)replaceEmployee(NumberForCharacter.class, arguments, "Values for X axis", numberTaskX);
			if (temp!=null) {
				numberTaskX = temp;
				numberTaskX.setHiringCommand(ntxC);
				numberTaskXName.setValue(numberTaskX.getName());
				if (cWindow!=null){
					sendParametersToCharter();
					if (!MesquiteThread.isScripting())
						chartWindowTask.doCounts();
				}
				return numberTaskX;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the module calculating numbers for characters for the Y axis", "[name of module]", commandName, "setValuesY")) {
			NumberForCharacter temp =  (NumberForCharacter)replaceEmployee(NumberForCharacter.class, arguments, "Values for Y axis", numberTaskY);
			if (temp!=null) {
				numberTaskY = temp;
				numberTaskY.setHiringCommand(ntyC);
				numberTaskYName.setValue(numberTaskY.getName());
				if (cWindow!=null){
					sendParametersToCharter();
					if (!MesquiteThread.isScripting())
						chartWindowTask.doCounts();
				}
				return numberTaskY;
			}
		}
		else if (checker.compare(this.getClass(), "Recalculate values for the chart", null, commandName, "doCounts")){
			if (cWindow!=null)
				chartWindowTask.doCounts();
		}
		else if (checker.compare(this.getClass(), "Returns the chart drawing module", null, commandName, "getCharter")) {
			return chartWindowTask;
		}
		else if (checker.compare(this.getClass(), "Sets the chart to visible", null, commandName, "setChartVisible")) {
			if (cWindow!=null)
				cWindow.setChartVisible();
		}
		else if (checker.compare(this.getClass(), "Returns the module serving to supply characters", "[name of module]", commandName, "getCharacterSource")) {
			return charSourceTask;
		}
		else if (checker.compare(this.getClass(), "Sets the module serving to supply characters", "[name of module]", commandName, "setCharacterSource")) { //temporary, for data files using old system without coordinators
			return charSourceTask.doCommand(commandName, arguments, checker);
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
		return "Characters";
	}

}


