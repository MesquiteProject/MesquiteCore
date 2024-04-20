/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.GBLOCKSHighlighter; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.molec.lib.GBLOCKSCalculator;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class GBLOCKSHighlighter extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
	MesquiteTable table;
	CharacterData data;
	MesquiteString xmlPrefs= new MesquiteString();
	String xmlPrefsString = null;
	GBLOCKSCalculator gblocksCalculator;
	long A = CategoricalState.makeSet(0);
	long C = CategoricalState.makeSet(1);
	long G = CategoricalState.makeSet(2);
	long T = CategoricalState.makeSet(3);
	static boolean askedOnceThisRun = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences(xmlPrefs);
		xmlPrefsString = xmlPrefs.getValue();
		gblocksCalculator = new GBLOCKSCalculator(this, xmlPrefsString);
		Debugg.printStackTrace("Remember options");
		return true;
	}
	/*.................................................................................................................*/
	boolean optionsSet = false;
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		if (gblocksCalculator!=null){
			String s = gblocksCalculator.preparePreferencesForXML();
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Asks to set GBLOCKS options", "[]", commandName, "setGBOptions")) {
			if (gblocksCalculator.queryOptions(this, "Select")) {
				askedOnceThisRun = true;
				optionsSet = true;
				calculateNums();
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	MesquiteMenuItemSpec mmis;
	public boolean setActiveColors(boolean active){
		boolean wasActive = isActive();
		setActive(active);
		if (isActive() && !wasActive){
			mmis = addMenuItem("GBLOCKS options...", makeCommand("setGBOptions", this));
			calculateNums();

		}
		else {
			deleteMenuItem(mmis);
		}
		resetContainingMenuBar();
		return true; //TODO: check success
	}
	public void endJob(){
		if (data!=null)
			data.removeListener(this);
		super.endJob();
	}
	public String getColorsExplanation(){
		return null;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		if (this.data!=data && this.data!=null)
			this.data.removeListener(this);
		this.data = data;
		data.addListener(this);
		calculateNums();
	}

	boolean notCalculated = true;
	boolean[] setToSelect = null;
	public void calculateNums(){
		notCalculated = true;
		if (!isActive())
			return;
		if (data == null || !(data instanceof DNAData)) {
			return;
		}
		if (!optionsSet && !askedOnceThisRun) {
			if (!gblocksCalculator.queryOptions(this, "Select"))
			return;
			else
				askedOnceThisRun = true;
		}
		optionsSet = true;
		setToSelect = new boolean[data.getNumChars()];
		if (!gblocksCalculator.markCharacters(data, this, setToSelect, null))
			return;

		notCalculated = false;
		table.repaintAll();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		calculateNums();
		if (table !=null)
			table.repaintAll();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Highlight by GBLOCKS Acceptance";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Colors aligned sequences to emphasize sections that seem discordant with those of the other taxa.";
	}
	/*.................................................................................................................*/
	ColorRecord[] legend;
	public ColorRecord[] getLegendColors(){
		return null;
		/*
		if (legend == null) {
			legend = new ColorRecord[6];
			legend[0] = new ColorRecord(Color.white, "Apparently aligned");
			legend[1] = new ColorRecord(Color.yellow, "Better shifted to right 1 site");
			legend[2] = new ColorRecord(Color.red, "Better shifted to right 2 sites");
			legend[3] = new ColorRecord(Color.green, "Better shifted to left 1 site");
			legend[4] = new ColorRecord(Color.blue, "Better shifted to left 2 sited");
			legend[5] = new ColorRecord(ColorDistribution.straw, "Inapplicable");
		}
		return legend;
		 */
	}
	/*.................................................................................................................*/
	public Color getCellColor(int ic, int it){
		if (ic<0 || it<0 || notCalculated)
			return null;

		if (data == null || setToSelect == null)
			return null;
		else if (data.isInapplicable(ic, it)){
			return ColorDistribution.straw;
		}
		else {
			if (!(data instanceof DNAData))
				return Color.white;
			DNAData	dData = (DNAData)data;
			long state = dData.getState(ic, it);
			if (ic < setToSelect.length && setToSelect[ic]) {
				if (A == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStatePale(0);
				else if (C == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStatePale(1);
				else if (G == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStatePale(2);
				else if (T == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStatePale(3);
			}
			else {
				if (A == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfState(0);
				else if (C == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfState(1);
				else if (G == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfState(2);
				else if (T == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfState(3);
			}
			return ColorDistribution.veryLightGray;
			/*
			if (offsets[ic][it] == 0)
				return Color.white;
			if (offsets[ic][it] ==1)
				return MesquiteColorTable.getYellowScale(scores[ic][it], 0, 5, false);
			if (offsets[ic][it] >1)
				return MesquiteColorTable.getRedScale(scores[ic][it], 0, 5, false);
			if (offsets[ic][it] ==-1)
				return MesquiteColorTable.getGreenScale(scores[ic][it], 0, 5, false);
			if (offsets[ic][it] <-1)
				return MesquiteColorTable.getBlueScale(scores[ic][it], 0, 5, false);
			 */
		}
		//		return Color.black;
	}
	/*.................................................................................................................*/
	public String getCellString(int ic, int it){
		if (ic<0 || it<0 || notCalculated)
			return null;

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

	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
}


