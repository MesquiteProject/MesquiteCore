/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.ColorByNumber; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class ColorByNumber extends DataWindowAssistantID implements CellColorer, CellColorerCharacters, CellColorerMatrix {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForCharAndTaxon.class, getName() + "  needs a method to calculate a value for each cell.",
		"The method to calculate values can be selected initially or in the Values for Colors submenu");
	}
	/*.................................................................................................................*/
	MesquiteTable table;
	CharacterData data;
	NumberForCharAndTaxon numberTask;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	double[][] values;
	int windowWidth = 1;
	MesquiteSubmenuSpec mss;
	Class stateClass;
	String ntName = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		ntC =makeCommand("setNumberTask",  this);
 		numberTaskName = new MesquiteString();
		return true;
	}
	public boolean setActiveColors(boolean active){
   	 	setActive(true);
 		if (active){
			if (numCompatibleModulesAvailable(NumberForCharAndTaxon.class, stateClass, this)>0) {
				mss = addSubmenu(null, "Values for Colors", ntC, NumberForCharAndTaxon.class);
	 			mss.setSelected(numberTaskName);
	 			mss.setCompatibilityCheck(stateClass);
				mss.setEnabled(false);
			}
			addMenuItem("Moving Window (for colors)...", makeCommand("setWindowWidth", this));
	 		if (!StringUtil.blank(ntName)) //remember last one used
				numberTask = (NumberForCharAndTaxon)hireNamedEmployee(NumberForCharAndTaxon.class, ntName, data.getStateClass());//shouldn't ask as this is an init and might not be needed.  "Value to calculate for character state in taxon"
			
			if (numberTask == null)
				numberTask = (NumberForCharAndTaxon)hireCompatibleEmployee(NumberForCharAndTaxon.class, data.getStateClass(), "Number for cell of matrix");//shouldn't ask as this is an init and might not be needed.  "Value to calculate for character state in taxon"
			
	 		if (numberTask != null){
		 		ntName = "#"+numberTask.getShortClassName();
		 		numberTask.setHiringCommand(ntC);
		 		numberTaskName.setValue(numberTask.getName());
 			} else
 				return false;
 			mss.setCompatibilityCheck(data.getStateClass());
			mss.setEnabled(active);
			calculateNums();
		}
		else {
			fireEmployee(numberTask);
			numberTask = null;
			deleteMenuItem(mss);
		}
		resetContainingMenuBar();
		return true; //TODO: check success
		
	}
	public void endJob(){
		if (data!=null)
			data.removeListener(this);
		super.endJob();
	}
	double minValue = 0;
	double maxValue = 0;
	boolean numsCalculated = false;
	/*.................................................................................................................*/
	void calculateNums(){
		numsCalculated = false;
   	 	if (data == null || !isActive())
   	 		return;
    	 	if (values == null || data.getNumChars() != values.length || (values.length < 1 || data.getNumTaxa() != values[0].length))
   	 		values = new double[data.getNumChars()][data.getNumTaxa()];
   	 	if (numberTask ==null)
   	 		return;
   	 	MesquiteNumber result = new MesquiteNumber();
   	 	MesquiteNumber min = new MesquiteNumber();
   	 	MesquiteNumber max = new MesquiteNumber();
   	 	
   	 	for (int ic= 0; ic < data.getNumChars(); ic++)
   	 		for (int it= 0; it < data.getNumTaxa(); it++) {
   	 			result.setToUnassigned();
   	 			numberTask.calculateNumber(data, ic, it, result, null);
   	 			values[ic][it] = result.getDoubleValue();
   	 			min.setMeIfIAmMoreThan(result);
   	 			max.setMeIfIAmLessThan(result);
   	 		}
   	 	if (windowWidth>1){
   	 		double[][] win = new double[data.getNumChars()][data.getNumTaxa()];
   	 		for (int it= 0; it < data.getNumTaxa(); it++) {
		   	 	for (int ic= 0; ic < data.getNumChars(); ic++){
		   	 		int left = windowWidth/2;
		   	 		int n = 0;
		   	 		//first go left, skipping gaps!
		   	 		int iw = 0;
		   	 		int is = ic; //start at current character
		   	 		while (iw<left && is>0){
		   	 			is--;
	   	 				if (!data.isInapplicable(is, it)){
	   	 					if (MesquiteDouble.isCombinable(values[is][it])){
			   	 				win[ic][it] += values[is][it];
			   	 				n++;
			   	 				iw++;
		   	 				}
		   	 			}
	   	 			}
	   	 			is = ic-1;
		   	 		while (iw<windowWidth && is<data.getNumChars()){
		   	 			is++;
	   	 				if (!data.isInapplicable(is, it)){
	   	 					if (MesquiteDouble.isCombinable(values[is][it])){
			   	 				win[ic][it] += values[is][it];
			   	 				n++;
			   	 				iw++;
		   	 				}
		   	 			}
	   	 			}
		   	 		/* old; has problems at edges of indels
		   	 		for (int iw = 0; iw<windowWidth; iw++){
	   	 				int is = ic -left + iw;
	   	 				if (is >=0 && is<data.getNumChars() && MesquiteDouble.isCombinable(values[is][it])){
		   	 				win[ic][it] += values[is][it];
		   	 				n++;
		   	 			}
	   	 			}
	   	 			*/
	   	 			if (n!=0)
	   	 				win[ic][it] /= n;
	   	 		}
	   	 	}
   	 		values = win;
 			min.setToUnassigned();
 			max.setToUnassigned();
   	 		for (int it= 0; it < data.getNumTaxa(); it++) {
		   	 	for (int ic= 0; ic < data.getNumChars(); ic++){
	   	 			result.setValue(values[ic][it]);
	   	 			min.setMeIfIAmMoreThan(result);
	   	 			max.setMeIfIAmLessThan(result);
	   	 		}
	   	 	}
 	 	}
   	 	minValue = min.getDoubleValue();
   	 	maxValue = max.getDoubleValue();
		numsCalculated = true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		if (this.data!=data && this.data!=null)
			this.data.removeListener(this);
		this.data = data;
		data.addListener(this);
		stateClass = data.getStateClass();
		if (mss !=null && mss.getCompatibilityCheck() == null) {
 			mss.setCompatibilityCheck(data.getStateClass());
 			resetContainingMenuBar();
 		}
		calculateNums();
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
 	 	if (numberTask ==null)
 	 		return null;
   	 	Snapshot temp = new Snapshot();
 	 	temp.addLine("setNumberTask ", numberTask);  
  	 	temp.addLine("setWindowWidth " + windowWidth);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
   	 	if (checker.compare(this.getClass(), "Sets the module that calculates numbers by which to color the cells", "[name of module]", commandName, "setNumberTask")) {
    	 		NumberForCharAndTaxon temp =  (NumberForCharAndTaxon)replaceCompatibleEmployee(NumberForCharAndTaxon.class, arguments, numberTask, stateClass);
 			if (temp!=null) {
 				numberTask = temp;
		 		ntName = "#"+numberTask.getShortClassName();
		 		numberTask.setHiringCommand(ntC);
		 		numberTaskName.setValue(numberTask.getName());
				calculateNums();
				table.repaintAll();
	 			return numberTask;
	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the width of the moving window", "[width]", commandName, "setWindowWidth")) {
    	 		int w = MesquiteInteger.fromString(parser.getFirstToken(arguments));
    	 		if (!MesquiteInteger.isCombinable(w) && !MesquiteThread.isScripting()){
    	 			w = MesquiteInteger.queryInteger(containerOfModule(), "Moving Window", "Width of Moving Window", windowWidth, 1, 1000);
    	 		}
    	 		if (!MesquiteInteger.isCombinable(w))
    	 			return null;
    	 		windowWidth = w;
			calculateNums();
			if (table!=null)
				table.repaintAll();
    	 	}
      	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
	/*.................................................................................................................*/
   	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
			calculateNums();
			if (table !=null && isActive())
				table.repaintAll();
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Color By Cell Value";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Cell Value";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Colors the cells of a character matrix according to a value for that cell or a moving window of cells.";
   	 }
	/*.................................................................................................................*/
   	public void viewChanged(){
   	}
   	ColorRecord[] legend;
   	
   	public ColorRecord[] getLegendColors(){
   		if (legend == null) 
   			legend = new ColorRecord[12];
   		if (minValue >= maxValue) {
	   		return null;
   		}
   		double interval = (maxValue-minValue)/10;
   		for (int i = 0; i< 10; i++){
   			double startInterval = interval*i + minValue;
   			legend[i] = new ColorRecord(MesquiteColorTable.getBlueScale(startInterval, minValue, maxValue, false), MesquiteDouble.toString(startInterval));
   		}
   		legend[10] = new ColorRecord(MesquiteColorTable.getBlueScale(maxValue, minValue, maxValue, false), MesquiteDouble.toString(maxValue));
   		legend[11] = new ColorRecord(ColorDistribution.straw, "Gap/Inapplicable");
   		return legend;
   	}
   	public String getColorsExplanation(){
   		return null;
   	}
	public Color getCellColor(int ic, int it){
		if (ic<0 || it<0)
			return null;
		if (!numsCalculated)
			calculateNums();

		//data version has changed!?
		if (data == null || values == null)
			return null;
		else if (data.isInapplicable(ic, it)){
			return ColorDistribution.straw;
		}
		else {
			
			Color c = MesquiteColorTable.getBlueScale(values[ic][it], minValue, maxValue, false);
			return c;
		}
	}
   	public String getCellString(int ic, int it){
		if (ic<0 || it<0 || !isActive())
			return null;
		if (!numsCalculated)
			calculateNums();

		//data version has changed!?
		if (data == null || values == null)
			return "No number calculated";
		else if (data.isInapplicable(ic, it)){
			return "Character is inapplicable for this taxon (e.g., gap)";
		} else if (numberTask!=null)
			return numberTask.getName() + ": " + values[ic][it];
		return null;
   	}
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj instanceof CharacterData){
			if (Notification.appearsCosmetic(notification))
				return;
			calculateNums();
			if (table!=null)
				table.repaintAll();
		}
	}
	
	public String getParameters(){
		if (numberTask!= null){
			return "Value used for coloring: " + numberTask.getNameAndParameters();
		}
		return null;
	}
	/*.................................................................................................................*/
    	 public boolean isPrerelease() {
		return false;
   	 }
	public CompatibilityTest getCompatibilityTest(){
		return new CbNCompatibilityTest();
	}
}

class CbNCompatibilityTest extends CharacterStateTest{
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		if (obj == null)
			return true;
		if (!(super.isCompatible(obj, project, prospectiveEmployer)))
			return false;
		return (((MesquiteModule)prospectiveEmployer).numCompatibleModulesAvailable(NumberForCharAndTaxon.class, obj, (MesquiteModule)prospectiveEmployer)>0); 
	}
}



	


