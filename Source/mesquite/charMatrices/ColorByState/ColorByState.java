/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ColorByState; 

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.ui.ColorRecord;
import mesquite.lib.ui.MesquiteColorTable;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.categ.lib.*;


/* ======================================================================== */
public class ColorByState extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
	MesquiteTable table;
	CharacterData data;
	int stateLimit = 9;
	MesquiteBoolean uniformMaximum = new MesquiteBoolean(true);
	MesquiteMenuItemSpec mss, mss2;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		mss2 = addCheckMenuItem(null, "All Characters Use Same Maximum Value for Cell Coloring", MesquiteModule.makeCommand("toggleUniformMaximum", this), uniformMaximum);
		mss = addMenuItem("Set Maximum Value for Cell Coloring...", MesquiteModule.makeCommand("setStateLimit", this));
		return true;
	}
	public boolean setActiveColors(boolean active){
		setActive(active);
		return true; 

	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setStateLimit " + stateLimit);  
		temp.addLine("toggleUniformMaximum " + uniformMaximum.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the maximum value for state coloring if uniform state coloring is used", "[state limit]", commandName, "setStateLimit")) {
			int limit = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(limit) && !MesquiteThread.isScripting()){
				limit = MesquiteInteger.queryInteger(containerOfModule(), "Maximum State Number", "Maximum State Number for Cell Coloring", stateLimit, 1, MesquiteColorTable.maxNumStates-1);
			}
			if (!MesquiteInteger.isCombinable(limit))
				return null;
			stateLimit = limit;
			if (table!=null)
				table.repaintAll();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not all characters are to use the same maximum value", "[on off]", commandName, "toggleUniformMaximum")) {
			uniformMaximum.toggleValue(new Parser().getFirstToken(arguments));
			if (table!=null)
				table.repaintAll();
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		deleteMenuItem(mss);
		deleteMenuItem(mss2);
		this.table = table;
		this.data = data;
		if (data instanceof CategoricalData && !(data instanceof MolecularData)) {
			mss2 = addCheckMenuItem(null, "All Characters Use Same Maximum Value for Cell Coloring", MesquiteModule.makeCommand("toggleUniformMaximum", this), uniformMaximum);
			mss = addMenuItem("Set Maximum Value for Cell Coloring...", MesquiteModule.makeCommand("setStateLimit", this));
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Color By State";
	}
	public String getNameForMenuItem() {
		return "Character State";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Colors the cells of a character matrix by their contained character states.";
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	public String getCellString(int ic, int it){
		if (!isActive())
			return null;
		return "Colored to show state of character";
	}
	ColorRecord[] legend;
	public ColorRecord[] getLegendColors(){
		if (data == null)
			return null;
		legend = null;
		if (data instanceof DNAData){
			legend = new ColorRecord[DNAState.maxDNAState+1];
			for (int is = 0; is<=DNAState.maxDNAState; is++) {
				legend[is] = new ColorRecord(DNAData.getDNAColorOfState(is), DNAData.getDefaultStateSymbol(is));
			}
		}
		else if (data instanceof ProteinData){
			legend = new ColorRecord[ProteinState.maxProteinState+1];
			for (int is = 0; is<=ProteinState.maxProteinState; is++) {
				legend[is] = new ColorRecord(ProteinData.getProteinColorOfState(is), ProteinData.getDefaultStateSymbol(is) + " (" + ProteinData.getStateLongName(is)+")");
			}
		} else if (data instanceof CategoricalData && uniformMaximum.getValue()) {
			legend = new ColorRecord[stateLimit+1];
			for (int is = 0; is<stateLimit; is++) {
				legend[is] = new ColorRecord(((CategoricalData)data).getColorOfState(0, is, stateLimit), CategoricalData.getDefaultStateSymbol(is));
			}
			legend[stateLimit] = new ColorRecord(((CategoricalData)data).getColorOfState(0, stateLimit, stateLimit), CategoricalData.getDefaultStateSymbol(stateLimit)+" or more");

		}

		return legend;
	}
	public String getColorsExplanation(){
		if (data == null)
			return null;
		if (data.getClass() == CategoricalData.class && !uniformMaximum.getValue()){
			return "Colors of states may vary from character to character";
		}
		return null;
	}
	public Color getCellColor(int ic, int it){
		if (ic < 0 || it < 0)  
			return null;
		if (data == null)
			return null;
		else if (data instanceof CategoricalData && uniformMaximum.getValue() && !(data instanceof MolecularData)) {
			return ((CategoricalData)data).getColorOfStatesUpperLimit(ic,it, stateLimit);
		} else
			return data.getColorOfStates(ic, it);
	}
	public CompatibilityTest getCompatibilityTest(){
		return new CharacterStateTest();
	}
	public String getParameters(){
		if (isActive())
			return getName();
		return null;
	}
}





