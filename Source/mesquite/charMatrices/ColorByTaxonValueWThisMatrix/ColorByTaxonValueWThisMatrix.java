/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ColorByTaxonValueWThisMatrix; 

import java.awt.Color;

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CellColorer;
import mesquite.lib.duties.CellColorerMatrix;
import mesquite.lib.duties.CellColorerTaxa;
import mesquite.lib.duties.DataWindowAssistantID;
import mesquite.lib.duties.NumberForTaxonAndMatrix;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ColorRecord;
import mesquite.lib.ui.MesquiteColorTable;
import mesquite.lib.ui.MesquiteSubmenuSpec;

/* ======================================================================== */
public class ColorByTaxonValueWThisMatrix extends DataWindowAssistantID implements CellColorer, CellColorerTaxa {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTaxonAndMatrix.class, getName() + " colors the rows (taxa) of the matrix to reflect some value for the taxa.",
		"You can request a value to calculate initially, or later under the Values for Colors submenu.");
	}
	MesquiteTable table;
	Taxa taxa;
	CharacterData data;
	NumberForTaxonAndMatrix numberTask;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	double[] values;
	int windowWidth = 1;
	MesquiteSubmenuSpec mss;
	String ntName = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		ntC =makeCommand("setNumberTask",  this);
		numberTaskName = new MesquiteString();
		return true;
	}
	public boolean setActiveColors(boolean active){
		setActive(active);
		if (active){
			if (numModulesAvailable(NumberForTaxonAndMatrix.class)>0) {
				mss = addSubmenu(null, "Values for Colors", ntC, NumberForTaxonAndMatrix.class);
				mss.setSelected(numberTaskName);
				mss.setEnabled(false);
			}
			if (!StringUtil.blank(ntName)) //remember last one used
				numberTask = (NumberForTaxonAndMatrix)hireNamedEmployee(NumberForTaxonAndMatrix.class, ntName);//shouldn't ask as this is an init and might not be needed.  "Value to calculate for character state in taxon"

			if (numberTask == null)
				numberTask = (NumberForTaxonAndMatrix)hireEmployee(NumberForTaxonAndMatrix.class, "Value for coloring taxa");//shouldn't ask as this is an init and might not be needed.  "Value to calculate for character state in taxon"

			if (numberTask != null){
				ntName = "#"+numberTask.getShortClassName();
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
			} else
				return false;
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
		if (taxa!=null)
			taxa.removeListener(this);
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
		if (taxa == null || data == null || !isActive())
			return;
		int numTaxa = taxa.getNumTaxa();
		if (values == null || numTaxa != values.length)
			values = new double[numTaxa];
		if (numberTask ==null)
			return;
		MesquiteNumber result = new MesquiteNumber();
		MesquiteNumber min = new MesquiteNumber();
		MesquiteNumber max = new MesquiteNumber();

		for (int ic= 0; ic < numTaxa; ic++) {
			result.setToUnassigned();
			numberTask.calculateNumber(taxa.getTaxon(ic), data.getMCharactersDistribution(), result, null);
			values[ic]= result.getDoubleValue();
			min.setMeIfIAmMoreThan(result);
			max.setMeIfIAmLessThan(result);
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
		this.data = data;
		Taxa dTaxa = null;
		if (data != null)
			dTaxa = data.getTaxa();
		if (this.taxa!=dTaxa && this.taxa!=null)
			this.taxa.removeListener(this);
		this.taxa = dTaxa;
		if (dTaxa != null)
			taxa.addListener(this);
		if (this.data!=data && this.data!=null)
			this.data.removeListener(this);
		this.data = data;
		if (data != null)
			data.addListener(this);
	}
	public void disposing(Object obj){
		if (obj == taxa)
			taxa = null;
		else if (obj == data)
			data = null;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (numberTask ==null)
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("setNumberTask ", numberTask);  
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that calculates numbers by which to color the cells", "[name of module]", commandName, "setNumberTask")) {
			NumberForTaxonAndMatrix temp =  (NumberForTaxonAndMatrix)replaceEmployee(NumberForTaxonAndMatrix.class, arguments, "Module to calculate numbers by which to color cells", numberTask);
			if (temp!=null) {
				numberTask = temp;
				ntName = "#"+numberTask.getShortClassName();
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				calculateNums();
				table.repaintAll();
				parametersChanged();
				return numberTask;
			}
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
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Color By Taxon Value with Matrix";
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Taxon Value with This Matrix";
	}
	public String getExplanation() {
		return "Colors taxon background according to a value for the Taxon with this matrix.";
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	ColorRecord[] legend;

	public ColorRecord[] getLegendColors(){
		if (legend == null) 
			legend = new ColorRecord[11];
		if (minValue >= maxValue) {
			return null;
		}
		double interval = (maxValue-minValue)/10;
		for (int i = 0; i< 10; i++){
			double startInterval = interval*i + minValue;
			legend[i] = new ColorRecord(MesquiteColorTable.getGreenScale(startInterval, minValue, maxValue, false), MesquiteDouble.toString(startInterval));
		}
		legend[10] = new ColorRecord(MesquiteColorTable.getGreenScale(maxValue, minValue, maxValue, false), MesquiteDouble.toString(maxValue));
		return legend;
	}

	public String getColorsExplanation(){
		if (numberTask!= null)
			return "Value used for coloring: " + numberTask.getNameAndParameters();
		return null;
	}
	public Color getCellColor(int ic, int it){
		if (ic<-1 || it<0)
			return null;
		if (!numsCalculated)
			calculateNums();

		//data version has changed!?
		if (taxa == null || values == null)
			return null;
		else {

			Color c = MesquiteColorTable.getGreenScale(values[it], minValue, maxValue, false);
			return c;
		}
	}
	public String getCellString(int ic, int it){
		if (ic<-1 || it<0 || !isActive())
			return null;
		if (!numsCalculated)
			calculateNums();

		//data version has changed!?
		if (taxa == null || values == null || numberTask == null)
			return "Number not calculated";
		else

			return numberTask.getName() + ": " + values[it];
	}
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (!isActive()) return;
		if (obj instanceof Taxa){
			if (Notification.appearsCosmetic(notification))
				return;
			calculateNums();
			if (table!=null)
				table.repaintAll();
			parametersChanged(notification);
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
		return true;
	}
	/*.................................................................................................................*/
}



