/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.BooleanForTaxaList;

import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.IntegerArray;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.Pausable;
import mesquite.lib.Snapshot;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.BooleanForTaxon;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lists.lib.TaxonListAssistant;

public class BooleanForTaxaList extends TaxonListAssistant implements MesquiteListener, Pausable {
	Taxa taxa;
	/*.................................................................................................................*/
	public String getName() {
		return "Boolean for Taxon (in List of Taxa window)";
	}

	public String getNameForMenuItem() {
		return "Boolean for Taxon";
	}

	public String getExplanation() {
		return "Supplies booleans for taxa for a taxon list window." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(BooleanForTaxon.class, getName() + " needs a method to calculate a boolean (yes/no) value for each of the characters.",
		"You can select a value to show in the Boolean For Taxon submenu of the Columns menu of the List of Taxa Window. ");
	}
	/*.................................................................................................................*/
	BooleanForTaxon booleanTask;
	boolean suppressed = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			booleanTask = (BooleanForTaxon)hireNamedEmployee(BooleanForTaxon.class, arguments);
			if (booleanTask==null) {
				return sorry("Boolean for taxon (for list) can't start because the requested calculator module wasn't successfully hired");
			}
		}
		else {
			booleanTask = (BooleanForTaxon)hireEmployee(BooleanForTaxon.class, "Value to calculate for character (for character list)");
			if (booleanTask==null) {
				return sorry("Boolean for taxon (for list) can't start because no calculator module was successfully hired");
			}
		}
		return true;
	}
	/** Returns whether or not it's appropriate for an employer to hire more than one instance of this module.  
 	If false then is hired only once; second attempt fails.*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return BooleanForTaxon.class;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suppress"); 
		temp.addLine("setValueTask ", booleanTask); 
		temp.addLine("desuppress"); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module that calculates a boolean for a character", "[name of module]", commandName, "setValueTask")) {
			BooleanForTaxon temp= (BooleanForTaxon)replaceEmployee(BooleanForTaxon.class, arguments, "Boolean for a taxon", booleanTask);
			if (temp!=null) {
				booleanTask = temp;
				if (!suppressed){
					doCalcs();
					parametersChanged();
				}
				return temp;
			}
		}
		else if (checker.compare(this.getClass(), "Suppresses calculation", null, commandName, "suppress")) {
			suppressed = true;
		}
		else if (checker.compare(this.getClass(), "Releases suppression of calculation", null, commandName, "desuppress")) {
			if (suppressed){
				suppressed = false;
				outputInvalid();
				doCalcs();
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		if (taxa != this.taxa){
			if (this.taxa != null){
				this.taxa.removeListener(this);
			}
			taxa.addListener(this);
		}
		this.taxa = taxa;

		if (!suppressed)
			doCalcs();

	}
	/*.................................................................................................................*/
	public String getTitle() {
		if (booleanTask==null)
			return "";
		return booleanTask.getVeryShortName();
	}
	/*.................................................................................................................*/
	/** Indicate what could be paused */
	public void addPausables(Vector pausables) {
		if (pausables != null)
			pausables.addElement(this);
	}
	/** to ask Pausable to pause*/
	public void pause() {
		paused = true;
	}
	/** to ask a Pausable to unpause (i.e. to resume regular activity)*/
	public void unpause() {
		paused = false;
		doCalcs();
		parametersChanged(null);
	}
	/*.................................................................................................................*/
	boolean paused = false;
	boolean okToCalc() {
		return !paused;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		
		if (!suppressed){
			if (!Notification.appearsCosmetic(notification) && !(obj instanceof Taxa && notification.getCode()==MesquiteListener.SELECTION_CHANGED)){
				outputInvalid();
				doCalcs();
				parametersChanged(notification);
			}
		}
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (!suppressed){
			outputInvalid();
			doCalcs();
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	IntegerArray booleanList = new IntegerArray(0);
	StringArray explArray = new StringArray(0);
	int totalYes = 0;
	int totalNo = 0;
	int totalUndetermined = 0;
	/*.................................................................................................................*/
	public void doCalcs(){
		if (!okToCalc() || suppressed || booleanTask==null)
			return;
		int numTaxa = taxa.getNumTaxa();
		booleanList.resetSize(numTaxa);
		explArray.resetSize(numTaxa);
		MesquiteBoolean mb = new MesquiteBoolean();
		MesquiteString expl = new MesquiteString();
		totalYes = 0;
		totalNo = 0;
		for (int ic=0; ic<numTaxa; ic++) {
			CommandRecord.tick("Boolean for taxon in tree list; examining taxon " + ic);
			mb.setToUnassigned();
			booleanTask.calculateBoolean(taxa, ic, mb, expl);
			if (mb.isUnassigned()) {
				booleanList.setValue(ic, -1);
				totalUndetermined++;
			}
			else if (mb.getValue()){
				booleanList.setValue(ic, 1);
				totalYes++;
			}
			else {
				totalNo++;
				booleanList.setValue(ic, 0);
			}
			explArray.setValue(ic, expl.getValue());
		}
	}
	public String getExplanationForRow(int it){
		if (explArray == null || explArray.getSize() <= it)
			return null;
		String s = explArray.getValue(it);
		if (StringUtil.blank(s))
			s = getStringForTaxon(it);
		return s + " [Totals: Yes: " + totalYes + "; No: " + totalNo + "; Undetermined: " + totalUndetermined + "]";
	}
	public String getStringForTaxon(int it){
		if (booleanList==null)
			return "";
		if (booleanList.getValue(it)<0)
			return "-";
		else if (booleanTask!=null)
			return booleanTask.getValueString(booleanList.getValue(it)==1);
		//return na.toString(ic);
		return "";
	}
	public String getWidestString(){
		if (booleanTask==null)
			return "888888";
		return booleanTask.getVeryShortName()+"   ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

}
