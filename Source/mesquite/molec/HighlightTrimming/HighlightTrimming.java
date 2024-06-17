/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.HighlightTrimming; 

import java.util.*;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.molec.lib.SiteFlagger;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class HighlightTrimming extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
	MesquiteTable table;
	CharacterData data;
	long A = CategoricalState.makeSet(0);
	long C = CategoricalState.makeSet(1);
	long G = CategoricalState.makeSet(2);
	long T = CategoricalState.makeSet(3);
	Vector flaggers = new Vector();

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		//	flaggerTask = (SiteFlagger)hireEmployee(SiteFlagger.class, "Trimming or flagging method to highlight");
		//	if (flaggerTask == null)
		//		return false;
		return true;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("cleanFlaggers"); 
		for (int i = 0; i<flaggers.size(); i++) {
			temp.addLine("addFlaggerViaScript ", (SiteFlagger)flaggers.elementAt(i)); 
		}
		return temp;
	}

	void cleanFlaggers() {
		for (int i = 0; i<flaggers.size(); i++) {
			SiteFlagger f = (SiteFlagger)flaggers.elementAt(i); 
			fireEmployee(f);
		}
		flaggers.removeAllElements();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Asks to set what flagger or trimmer to highlight", "[name of module]", commandName, "cleanFlaggers")) {
			cleanFlaggers();
		}
		else if (checker.compare(this.getClass(), "Reset trimmers to highlight", null, commandName, "resetFlaggers")) {
			cleanFlaggers();
			SiteFlagger flaggerTask = (SiteFlagger)hireEmployee(SiteFlagger.class, "Trimming or flagging method to highlight");
			if (flaggerTask !=null){
				flaggers.addElement(flaggerTask);
				calculateNums();
				parametersChanged();
				return flaggerTask;
			}
		}
		else if (checker.compare(this.getClass(), "Sets additional flagger or trimmer to highlight", "[name of module]", commandName, "addFlaggerViaScript")) {
			SiteFlagger temp = (SiteFlagger)hireNamedEmployee(SiteFlagger.class, arguments);
			if (temp !=null){
				flaggers.addElement(temp);
				calculateNums();
				return temp;
			}
		}
		else if (checker.compare(this.getClass(), "Asks to set what flagger or trimmer to highlight", "[name of module]", commandName, "addFlagger")) {
			SiteFlagger temp = (SiteFlagger)hireEmployee(SiteFlagger.class, "Additional flagger or trimmer to highlight");
			if (temp !=null){
				flaggers.addElement(temp);
				calculateNums();
				parametersChanged();
				return temp;
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	MesquiteMenuItemSpec mmis;
	boolean flaggerInitialized = false;
	/*.................................................................................................................*/
	public boolean setActiveColors(boolean active){
		boolean wasActive = isActive();
		setActive(active);
		if (isActive() && !wasActive){
			if (!flaggerInitialized) {
				SiteFlagger flaggerTask = (SiteFlagger)hireEmployee(SiteFlagger.class, "Trimming or flagging method to highlight");
				if (flaggerTask == null)
					return false;
				flaggerInitialized = true;
				flaggers.addElement(flaggerTask);
			}
			mmis = addMenuItem("Reset Trimmers to be Highlighted...", makeCommand("resetFlaggers", this));
			mmis = addMenuItem("Add Trimmer to be Highlighted...", makeCommand("addFlagger", this));
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

	Bits flags, tempFlags;

	public void calculateNums(){
		if (!isActive())
			return;
		if (data == null || !(data instanceof DNAData)) {
			return;
		}
		if (flags == null)
			flags = new Bits(data.getNumChars());
		for (int i = 0; i<flaggers.size(); i++) {
			SiteFlagger flaggerTask = (SiteFlagger)flaggers.elementAt(i);
			tempFlags = flaggerTask.flagSites(data, tempFlags);
			if (i == 0) {
				flags.resetSize(tempFlags.getSize()); 
				flags.setBits(tempFlags);
			}
			else
				flags.orBits(tempFlags);
		}

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
		return "Highlight by Trimming Methods";
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
		return "Colors aligned sequences to darken sections that might be trimmed.";
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
		if (ic<0 || it<0 || flags == null)
			return null;

		if (data == null)
			return null;
		else {
			if (!(data instanceof DNAData))
				return Color.white;
			DNAData	dData = (DNAData)data;
			long state = dData.getState(ic, it);
			if (ic < flags.getSize() && flags.isBitOn(ic)) {
				if (A == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStateVeryDark(0);
				else if (C == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStateVeryDark(1);
				else if (G == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStateVeryDark(2);
				else if (T == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStateVeryDark(3);
				return Color.black;
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
				return ColorDistribution.veryLightGray;
			}
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
		if (ic<0 || it<0)
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


