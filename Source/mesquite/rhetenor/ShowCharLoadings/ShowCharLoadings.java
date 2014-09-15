/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.rhetenor.ShowCharLoadings;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.rhetenor.lib.*;

/* ======================================================================== */
public class ShowCharLoadings extends OrdinationAssistant {
	public String getName() {
		return "Display Character Loadings";
	}
	public String getExplanation() {
		return "Shows character loadings in ordination." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ItemsCharter.class, getName() + "  needs a module to draw the chart.",
		"The module to draw the chart is arranged automatically");
		EmployeeNeed e2 = registerEmployeeNeed(CharacterLoadings.class, getName() + "  needs a source of character loadings.",
		"The source of character loadings is arranged initially");
		EmployeeNeed e3 = registerEmployeeNeed(CharsFromMatrixSource.class, getName() + "  needs a module to extract characters from a matrix.",
		"The module to extract characters froma matrix is arranged initially");
	}
	/*.................................................................................................................*/
	ChartWindow cWindow;
	ItemsCharter chartWindowTask;
	CharacterLoadings loadingsTask;
	CharsFromMatrixSource charSourceConverter;
	MiniScroll axisScroll;
	boolean firstTime = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("Loadings");
		chartWindowTask = (ItemsCharter)hireEmployee(ItemsCharter.class, null);
		if (chartWindowTask == null) {
			return sorry(getName() + " couldn't start because no charting module obtained.");
		}
		cWindow = chartWindowTask.makeChartWindow(this);
		cWindow.doCommand("valuesByItems", null, CommandChecker.defaultChecker);

		setModuleWindow( cWindow);

		//needs to be same set of characters!!!!
		loadingsTask =  (CharacterLoadings)hireNamedEmployee(CharacterLoadings.class, StringUtil.tokenize("Character Loadings"));
		if (loadingsTask==null) {
			return sorry(getName() + " couldn't start because no character loadings module obtained.");
		}
		charSourceConverter = (CharsFromMatrixSource)hireNamedEmployee(CharsFromMatrixSource.class, StringUtil.tokenize("Characters from Matrix Source"));
		if (charSourceConverter==null) {
			return sorry(getName() + " couldn't start because no character source module obtained.");
		}
		addMenuItem(null, "Next Axis", makeCommand("nextAxis",  this));
		addMenuItem(null, "Previous Axis", makeCommand("prevAxis",  this));
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public boolean canHireMoreThanOnce(){
		return false;
	}
	/*.................................................................................................................*/
	public void setOrdination(Ordination ordination, Taxa taxa, MatrixSourceCoord matrixSourceTask){
		if (ordination!=null) {
			chartWindowTask.setTaxa(taxa);
			charSourceConverter.setMatrixSource(matrixSourceTask);
			chartWindowTask.setItemsSource(charSourceConverter);

			loadingsTask.setOrdination(ordination, taxa);
			int curr = loadingsTask.getCurrentAxis();
			if (curr <0 || curr >= loadingsTask.getNumberOfAxes())
				loadingsTask.setCurrentAxis(0);
			chartWindowTask.setNumberTask(loadingsTask);
			if (firstTime){
				cWindow.setScroller(axisScroll = new MiniScroll(makeCommand("setAxis",  this), true, true, 1, 1, loadingsTask.getNumberOfAxes(),"axis"));
				cWindow.setChartTitle("Character Loadings");
				cWindow.resetTitle();
				if (!MesquiteThread.isScripting()) {
					cWindow.setChartVisible();
					if (!cWindow.isVisible())
						cWindow.setVisible(true);
				}
				firstTime = false;
			}
			else {
				if (axisScroll != null && loadingsTask != null){
					axisScroll.setMaximumValue(loadingsTask.getNumberOfAxes());
				}
			}
			chartWindowTask.doCounts();
			resetContainingMenuBar();
			resetAllWindowsMenus();
		}

	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getCharter", chartWindowTask); 
		temp.addLine("setChartVisible"); 
		temp.addLine("doCounts"); 
		temp.addLine("showWindow"); 
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Goes to next axis from ordination", null, commandName, "nextAxis")) {
			int curr = loadingsTask.getCurrentAxis();
			int max = loadingsTask.getNumberOfAxes();
			if (curr+1<max) {
				loadingsTask.setCurrentAxis(curr+1);
				chartWindowTask.doCounts();
			}
		}
		else if (checker.compare(this.getClass(), "Goes to previous axis from ordination", null, commandName, "prevAxis")) {
			int curr = loadingsTask.getCurrentAxis();
			if (curr>0) {
				loadingsTask.setCurrentAxis(curr-1);
				chartWindowTask.doCounts();
			}
		}
		else if (checker.compare(this.getClass(), "Sets which axis from ordination is shown", "[axis number]", commandName, "setAxis")) {
			int i = MesquiteInteger.fromFirstToken(arguments, pos) -1;
			int max = loadingsTask.getNumberOfAxes();
			if (MesquiteInteger.isCombinable(i) && i<max) {
				loadingsTask.setCurrentAxis(i);
				chartWindowTask.doCounts();
			}
		}
		else if (checker.compare(this.getClass(), "Returns chart drawing module", null, commandName, "getCharter")) {
			return chartWindowTask;
		}
		else if (checker.compare(this.getClass(), "Sets the chart to visible", null, commandName, "setChartVisible")) {
			if (cWindow!=null)
				cWindow.setChartVisible();
		}
		else if (checker.compare(this.getClass(), "Requests counts of chart", null, commandName, "doCounts")) {
			if (chartWindowTask!=null)
				chartWindowTask.doCounts();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/


}


