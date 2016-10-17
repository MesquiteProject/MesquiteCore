/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.CharacterValuesChart;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**=== Class CharacterValuesChart.  ===*/

public class CharacterValuesChart extends FileAssistantCH  {
	/*.................................................................................................................*/
	public String getName() {
		return "Bar & Line Chart for Characters";
	}
	public String getExplanation() {
		return "Shows chart displaying values (such as likelihoods, parameter values, parsimony counts, etc.) for a series of characters." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForCharacter.class, getName() + " needs a method to calculate the values for the characters.",
		"You can select this either when the chart starts up, or using the Values submenu of the Chart menu.  (You may request the chart itself by selecting the Characters item under Bar & Line Charts in the Analysis menu)");
		e.setPriority(2);
		EmployeeNeed e2 = registerEmployeeNeed(CharSourceCoordObed.class, getName() + " needs a source of characters.",
		"You can select the characters to show either when the chart starts up, or later using the Source of Characters submenu of the Chart menu.  (You may request the chart itself by selecting the Characters item under Bar & Line Charts in the Analysis menu)");
		e2.setPriority(2);
		EmployeeNeed e3 = registerEmployeeNeed(ItemsCharter.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");

	}
	/*.................................................................................................................*/
	NumberForCharacter numberTask;
	CharSourceCoordObed characterSourceTask;
	ItemsCharter chartWindowTask;
	private CharacterDistribution charStates;
	private Taxa oldTaxa, taxa;
	private CharacterData data = null;
	ChartWindow cWindow;
	MesquiteCommand ntC;
	static int numMade =0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//is there a hint as to taxa
		String whichTaxa = MesquiteThread.retrieveAndDeleteHint(this);
		long wB = MesquiteLong.fromString(whichTaxa);
		if (MesquiteLong.isCombinable(wB)){
			taxa = getProject().getTaxaByID(wB);
		}

		if (taxa == null)
		taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a chart of values for characters?");
		if (taxa ==null)
			return sorry(getName() + " couldn't start because no block of taxa available.");
		taxa.addListener(this);
		chartWindowTask = (ItemsCharter)hireEmployee(ItemsCharter.class, null);
		if (chartWindowTask == null)
			return sorry(getName() + " couldn't start because no chart window module was obtained");

		//character source %%%%%%%%
		characterSourceTask = (CharSourceCoordObed)hireEmployee(CharSourceCoordObed.class, "Source of characters (for Character Values chart)");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		CharacterDistribution item = (CharacterDistribution)characterSourceTask.getItem(taxa, 0);
		Class stateClass = null;
		if (item != null)
			stateClass = item.getStateClass();
		//values etc. %%%%%%%%
		numberTask=(NumberForCharacter)hireCompatibleEmployee(NumberForCharacter.class, stateClass, "Value to calculate for characters (for Character Values chart)");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module was obtained.");


		ntC = makeCommand("setCalculator",  this);
		numberTask.setHiringCommand(ntC);


		makeMenu("Chart");
		addSubmenu(null, "Values", ntC, NumberForCharacter.class);//TODO: checkmark

		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow( cWindow);
		chartWindowTask.setTaxa(taxa);
		chartWindowTask.setNumberTask(numberTask);
		chartWindowTask.setItemsSource( characterSourceTask);
		cWindow.setChartTitle("Characters Chart " + (++numMade));
		if (!MesquiteThread.isScripting()){
			cWindow.setChartVisible();
			cWindow.setVisible(true);
			chartWindowTask.doCounts();
			cWindow.toFront();
		}
		resetContainingMenuBar();
		resetAllWindowsMenus();
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
		if (m==chartWindowTask || m == characterSourceTask)
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
	public void windowGoAway(MesquiteWindow whichWindow) { //obed should only supply window!
		if (whichWindow == null)
			return;
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa)); 
		temp.addLine("setCalculator ", numberTask);
		temp.addLine("getCharacterSource ", characterSourceTask);
		temp.addLine("getCharter", chartWindowTask); 
		temp.addLine("setChartVisible"); 
		temp.addLine("doCounts"); 
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
				return taxa;
			}
		} 
		else if (checker.compare(this.getClass(), "Returns chart drawing module", null, commandName, "getCharter")) {
			return chartWindowTask;
		}
		else if (checker.compare(this.getClass(), "Sets the chart to be visible", null, commandName, "setChartVisible")) {
			if (cWindow!=null)
				cWindow.setChartVisible();

		}
		else if (checker.compare(this.getClass(), "Requests the calculations for the charts be redone", null, commandName, "doCounts")) {
			if (cWindow!=null){
				chartWindowTask.setTaxa(taxa);
				chartWindowTask.setNumberTask(numberTask);
				chartWindowTask.setItemsSource( characterSourceTask);
				chartWindowTask.doCounts();
			}
		}
		/*	else if (checker.compare(this.getClass(), "Highlights a character set", null, commandName, "highlightCharSet")) {
			if (cWindow!=null){
				chartWindowTask.setTaxa(taxa);
				chartWindowTask.setNumberTask(numberTask);
				chartWindowTask.setItemsSource( characterSourceTask);
    	 			chartWindowTask.doCounts();
    	 		}
    	 	}
		 */
		else if (checker.compare(this.getClass(), "Returns the source of characters for the characters chart", "[name of module]", commandName, "getCharacterSource")) {
			return characterSourceTask;
		}
		else if (checker.compare(this.getClass(), "Sets the source of characters for the characters chart", "[name of module]", commandName, "setCharacterSource")) { //Temporary
			return characterSourceTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Sets the module to calculate numbers for the characters", "[name of module]", commandName, "setCalculator")) {
			NumberForCharacter temp = (NumberForCharacter)replaceEmployee(NumberForCharacter.class, arguments, "Value to calculate for characters", numberTask);
			if (temp !=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				if (cWindow!=null) {
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource( characterSourceTask);
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
		if (cWindow!=null)
			if (Notification.getCode(notification) == MesquiteListener.PARTS_ADDED && source == characterSourceTask) {
				int[] notifParam = notification.getParameters();
				if (notifParam!=null && notifParam.length==2)
					chartWindowTask.doCounts( notifParam[0]+1, notifParam[0]+notifParam[1], false);
				else
					chartWindowTask.doCounts();
			}
		chartWindowTask.doCounts();
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Characters";
	}

}



