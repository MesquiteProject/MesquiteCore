/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.NumWMatrixForTaxaList;
/*~~  */

import java.awt.Color;
import java.util.Vector;

import mesquite.categ.lib.MolecularState;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTimer;
import mesquite.lib.Notification;
import mesquite.lib.NumberArray;
import mesquite.lib.Pausable;
import mesquite.lib.Snapshot;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.duties.NumberForTaxonAndMatrix;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.MesquiteColorTable;
import mesquite.lists.lib.TaxonListAssistant;

/* ======================================================================== */
public class NumWMatrixForTaxaList extends TaxonListAssistant implements MesquiteListener, Pausable {

	/*.................................................................................................................*/
	public String getName() {
		return "Number for Taxon with Matrix (in List of Taxa window)";
	}
	public String getNameForMenuItem() {
		return "Number for Taxon with Matrix";
	}
	public String getExplanation() {
		return "Supplies numbers for taxa with a matrix for a taxa list window." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of sequences.",
				"The source of characters is arranged initially");
		registerEmployeeNeed(NumberForTaxonAndMatrix.class, getName() + "  needs a calculator of the number for the taxon and matrix.",
				"The calculator is arranged initially");
	}
	/*.................................................................................................................*/
	MatrixSourceCoord matrixSourceTask;
	NumberForTaxonAndMatrix numberTask;
	MesquiteBoolean shadeCells;
	Taxa taxa;
	boolean suppressedByScript = false;
	MesquiteTable table;
	MesquiteCommand mc;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForTaxonAndMatrix)hireNamedEmployee(NumberForTaxonAndMatrix.class, arguments);
			if (numberTask==null) {
				return sorry("Number for taxon with matrix (for list) can't start because the requested calculator module wasn't successfully hired");
			}
		}
		else {
			numberTask = (NumberForTaxonAndMatrix)hireEmployee(NumberForTaxonAndMatrix.class, "Value to calculate for taxa (for taxa list)");
			if (numberTask==null) {
				return sorry("Number for taxon (for list) can't start because no calculator module was successfully hired");
			}
		}
		mc =makeCommand("numberTask",  this);
		numberTask.setHiringCommand(mc);
		addMenuItem("Value to Calculate...", mc);//TODO: checkmark
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, MolecularState.class, "Source of character matrix (for " + getName() + ")"); 
		if (matrixSourceTask==null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
		shadeCells = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Color Cells", makeCommand("toggleShadeCells",  this), shadeCells);
		addMenuItem(null, "Recalculate", makeCommand("recalculate",  this));
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
		return NumberForTaxonAndMatrix.class;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suppress"); 
		temp.addLine("numberTask", numberTask); 
		temp.addLine("getMatrixSource", matrixSourceTask);
		temp.addLine("toggleShadeCells " + shadeCells.toOffOnString());
		temp.addLine("desuppress"); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets calculator module", "[name of module]", commandName, "numberTask")) {
			NumberForTaxonAndMatrix temp = (NumberForTaxonAndMatrix)replaceEmployee(NumberForTaxonAndMatrix.class, arguments, "Number for taxon with matrix", numberTask);
			if (temp !=null){
				numberTask = temp;
				numberTask.setHiringCommand(mc);
				doCalcs();
				outputInvalid();
			parametersChanged();
			return temp;
			}
		}
		else if (checker.compare(this.getClass(), "Returns the matrix source", null, commandName, "getMatrixSource")) {
			return matrixSourceTask;
		}

		else if (checker.compare(this.getClass(), "Requests a recalculation of the numbers (this may be unnecessary)", null, commandName, "recalculate")) {
			doCalcs();
			parametersChanged();
		}


		else if (checker.compare(this.getClass(), "Sets whether or not to color cells", "[on or off]", commandName, "toggleShadeCells")) {
			boolean current = shadeCells.getValue();
			shadeCells.toggleValue(parser.getFirstToken(arguments));
			if (current!=shadeCells.getValue()) {
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Suppresses calculation", null, commandName, "suppress")) {
			suppressedByScript = true;
		}
		else if (checker.compare(this.getClass(), "Releases suppression of calculation", null, commandName, "desuppress")) {
			if (suppressedByScript){
				suppressedByScript = false;
				outputInvalid();
				doCalcs();
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
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
	/*.................................................................................................................*/
	boolean okToCalc() {
		return !suppressedByScript && !paused;
	}
	/*.................................................................................................................*/


	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		this.table = table;
		this.taxa = taxa;
		if (matrixSourceTask != null ) {
			observedStates = matrixSourceTask.getCurrentMatrix(taxa);
		}
		if (okToCalc())
			doCalcs();
	}
	public String getTitle() {
		if (numberTask==null)
			return "";
		String s = numberTask.getVeryShortName();
		if (observedStates != null && getProject().getNumberCharMatricesVisible()>1) {
			if (observedStates.getName()!= null) {
				String n =  observedStates.getName();
				if (n.length()>12)
					n = n.substring(0, 12) + "…"; 
				s += " (" + n + ")";
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == taxa)
			taxa=null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		if (okToCalc()){
			outputInvalid();
			doCalcs();
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (okToCalc()){
			observedStates = null;
			outputInvalid();
			doCalcs();
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	NumberArray na = new NumberArray(0);
	StringArray explArray = new StringArray(0);
	MesquiteNumber min = new MesquiteNumber();
	MesquiteNumber max = new MesquiteNumber();
	MCharactersDistribution observedStates;
	/*.................................................................................................................*/
	public void doCalcs(){
		if (!okToCalc() || numberTask==null || taxa == null)
			return;
		int numTaxa = taxa.getNumTaxa();
		na.resetSize(numTaxa);
		na.deassignArrayToInteger();
		explArray.resetSize(numTaxa);
		MesquiteNumber mn = new MesquiteNumber();
		MesquiteString expl = new MesquiteString();
			observedStates = matrixSourceTask.getCurrentMatrix(taxa);
	
		if (observedStates==null)
			return;

		MesquiteTimer timer = new MesquiteTimer();
		timer.start();

		for (int ic=0; ic<numTaxa; ic++) {
			CommandRecord.tick("Number for taxon in taxon list; examining taxon " + ic);
			Taxon taxon = taxa.getTaxon(ic);
			mn.setToUnassigned();
			numberTask.calculateNumber(taxon, observedStates, mn, expl);
			na.setValue(ic, mn);
			explArray.setValue(ic, expl.getValue());
		}
		CommandRecord.tick("");
		na.placeMinimumValue(min);
		na.placeMaximumValue(max);
	}
	/** Gets background color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getBackgroundColorOfCell(int ic, boolean selected){
		if (!shadeCells.getValue())
			return null;
		if (min.isCombinable() && max.isCombinable() && na != null && na.isCombinable(ic)){
			return MesquiteColorTable.getGreenScale(na.getDouble(ic), min.getDoubleValue(), max.getDoubleValue(), false);
		}
		return null;
	}
	public String getExplanationForRow(int ic){
		if (explArray == null || explArray.getSize() <= ic)
			return null;
		return explArray.getValue(ic);
	}
	public boolean useString(int ic){
		return true;
	}
	public String getStringForTaxon(int ic){
		if (na==null)
			return "";
		return na.toString(ic);
	}
	public String getWidestString(){
		if (numberTask==null)
			return "88888888";
		String name = numberTask.getVeryShortName()+"   ";
		if (StringUtil.blank(name) || name.length()<8)
			return "88888888";
		return name +"8888888888" ;
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

