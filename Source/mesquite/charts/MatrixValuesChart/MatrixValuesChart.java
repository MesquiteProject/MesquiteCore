/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.MatrixValuesChart;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**=== Class MatrixValuesChart.  ===*/

public class MatrixValuesChart extends FileAssistantCH  {
	/*.................................................................................................................*/
	public String getName() {
		return "Bar & Line Chart for Character Matrices";
	}
	public String getExplanation() {
		return "Shows chart displaying values (such as treelength, etc.) for a series of character matrices." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForMatrix.class, getName() + " needs methods to calculate the two values for the character matrices.",
		"You can select this either when the chart starts up, or using the Values submenu of the Chart menu.  (You may request the chart itself by selecting the Character Matrices item under Bar & Line Charts in the Analysis menu)");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(CharMatrixSource.class, getName() + " needs a source of character matrices.",
		"You can select the character matrices to show either when the chart starts up, or later using the Matrix Source submenu of the Chart menu.  (You may request the chart itself by selecting the Character Matrices item under Bar & Line Charts in the Analysis menu)");
		EmployeeNeed e3 = registerEmployeeNeed(ItemsCharter.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");
	}
	/*.................................................................................................................*/
	NumberForMatrix numberTask;
	CharMatrixSource matrixSourceTask;  
	
//	MatrixSourceCoordObed matrixSourceTask;   
	ItemsCharter chartWindowTask;
	private Taxa oldTaxa, taxa;
	ChartWindow cWindow;
	MesquiteCommand ntC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a chart of values for characters?");
		if (taxa ==null)
			return sorry(getName() + " couldn't start because no block of taxa available.");
		taxa.addListener(this);
		chartWindowTask = (ItemsCharter)hireEmployee(ItemsCharter.class, null);
		if (chartWindowTask == null)
			return sorry(getName() + " couldn't start because no chart window module was obtained");

		//Matrix source %%%%%%
		matrixSourceTask = (CharMatrixSource)hireEmployee(CharMatrixSource.class, "Source of character matrices (for Character Matrix Values chart)");
		if (matrixSourceTask == null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
		MCharactersDistribution item = (MCharactersDistribution)matrixSourceTask.getItem(taxa, 0);
		Class stateClass = null;
		if (item != null)
			stateClass = item.getStateClass();

		//values etc. %%%%%%
		numberTask=(NumberForMatrix)hireCompatibleEmployee(NumberForMatrix.class, stateClass,"Value to calculate for character matrices (for Character Matrix Values chart)");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module was obtained.");
		ntC = makeCommand("setCalculator",  this);
		numberTask.setHiringCommand(ntC);


		makeMenu("Chart");
		addSubmenu(null, "Values for matrices", ntC, NumberForMatrix.class);//TODO: checkmark

		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow( cWindow);
		chartWindowTask.setTaxa(taxa);
		chartWindowTask.setNumberTask(numberTask);
		chartWindowTask.setItemsSource( matrixSourceTask);
		cWindow.setChartTitle("Character Matrix Values");
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
		temp.addLine("suspendCalculations"); 
		temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa)); 
		temp.addLine("setCalculator ", numberTask);
		temp.addLine("getMatrixSource ", matrixSourceTask);
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
				chartWindowTask.setItemsSource( matrixSourceTask);
				chartWindowTask.doCounts();
			}
		}
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspendCalculations")){
			chartWindowTask.incrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resumeCalculations")){
			chartWindowTask.decrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Returns the source of matrices for the character matrices chart", null, commandName, "getMatrixSource")) {

			return matrixSourceTask;

		}
		else if (checker.compare(this.getClass(), "Returns the source of matrices for the character matrices chart", null, commandName, "setMatrixSource")) { //temporary, for data files using old system without coordinators

			return matrixSourceTask.doCommand(commandName, arguments, checker);

		}
		else if (checker.compare(this.getClass(), "Sets the module to calculate numbers for the character matrices", "[name of module]", commandName, "setCalculator")) {
			NumberForMatrix temp = (NumberForMatrix)replaceEmployee(NumberForMatrix.class, arguments, "Value to calculate for character matrices", numberTask);
			if (temp !=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				if (cWindow!=null) {
					chartWindowTask.setTaxa(taxa);
					chartWindowTask.setNumberTask(numberTask);
					chartWindowTask.setItemsSource( matrixSourceTask);
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
			if (Notification.getCode(notification) == MesquiteListener.PARTS_ADDED && source == matrixSourceTask) {
				int[] notifParam = notification.getParameters();
				if (notifParam!=null && notifParam.length==2)
					chartWindowTask.doCounts( notifParam[0]+1, notifParam[0]+notifParam[1], false);
				else
					chartWindowTask.doCounts();
			}
			else chartWindowTask.doCounts();
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Character Matrices";
	}

}



