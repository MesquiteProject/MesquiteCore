/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.NumDataCellsStrip;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.charMatrices.ColorByState.ColorByState;
import mesquite.molec.ColorByAA.ColorByAA;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;



public class NumDataCellsStrip extends DataColumnNamesAssistant {
	int[] calculatedValues = null;
	MesquiteMenuItemSpec menuItem1, menuItem2, closeMenuItem, lineMenuItem;

	boolean suspend = false;

	MesquiteBoolean selectedOnly = new MesquiteBoolean(true);

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		setUseMenubar(false);


		return true;
	}
	/*.................................................................................................................*/
	public void getSubfunctions(){
		String  explanationString = "(An Info Strip of a Categorical Matrix Window) Displays a consensus sequence for categorical data, as indicated by the two arrows in the figure below. <br> <img src=\"" + MesquiteFile.massageFilePathToURL(getPath() + "consensus.gif");
		explanationString += "\"><br>To create a consensus sequence, choose Matrix>Add Char Info Strip>Consensus Sequence.  To adjust options, use the drop-down menu that appears when you touch on the consensus sequence.<br>";
		registerSubfunction(new FunctionExplanation("Consensus Sequence", explanationString, null, null));
		super.getSubfunctions();
	}
	/*.................................................................................................................*/
	public void deleteMenuItems() {
		deleteMenuItem(menuItem1);
		deleteMenuItem(menuItem2);
	}
	public void deleteRemoveMenuItem() {
		deleteMenuItem(lineMenuItem);
		deleteMenuItem(closeMenuItem);
	}
	public void addRemoveMenuItem() {
		closeMenuItem= addMenuItem(null,"Remove Number of Data Cells Strip", makeCommand("remove", this));
		lineMenuItem = addMenuSeparator();
	}

	public void setTableAndData(MesquiteTable table, CharacterData data) {
		deleteMenuItems();
		deleteRemoveMenuItem();
		addRemoveMenuItem();
		menuItem1= addCheckMenuItem(null,"Selected Taxa Only", makeCommand("toggleSelectedOnly", this), selectedOnly);


		if (data != null)
			data.removeListener(this);
		this.data = data;
		this.table = table;
		data.addListener(this);

		calculateValues();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

		temp.addLine("suspend");
		temp.addLine("toggleSelectedOnly " + selectedOnly.toOffOnString());
		temp.addLine("resume");

		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not only selected taxa are included are all taxa.", "[on or off]", commandName, "toggleSelectedOnly")) {
			boolean current = selectedOnly.getValue();
			selectedOnly.toggleValue(parser.getFirstToken(arguments));
			if (current!=selectedOnly.getValue() && !suspend) {
				parametersChanged();
				calculateValues();
				if (table !=null) {
					table.repaintAll();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Removes the Consensus Strip", null, commandName, "remove")) {
			iQuit();
		}
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspend = true;
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspend = false;
			calculateValues();
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		calculateValues();
		if (table !=null)
			table.repaintAll();
	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}
	/*.................................................................................................................*/
	public void endJob() {
		if (table!=null) {
			((ColumnNamesPanel)table.getColumnNamesPanel()).decrementInfoStrips();
			table.resetTableSize(false);
		}
		super.endJob();
	}


	/*.................................................................................................................*/
	public void calculateValues() {
		int[] values = new int[data.getNumChars()];
		boolean noRowsSelected =  !table.anyRowSelected();
		for (int ic = 0; ic<data.getNumChars(); ic++) {
			values[ic] = 0;
			for (int it=0; it<data.getNumTaxa(); it++)
				if (!selectedOnly.getValue() || table.isRowSelected(it) || noRowsSelected)
					if (!data.isInapplicable(ic, it))
						values[ic]++;
		}
		calculatedValues = values;
	}
	/*.................................................................................................................*/
	public void calculateValue(int ic) {
		boolean noRowsSelected =  !table.anyRowSelected();
		calculatedValues[ic] = 0;
		for (int it=0; it<data.getNumTaxa(); it++)
			if (!selectedOnly.getValue() || table.isRowSelected(it) || noRowsSelected)
				if (!data.isInapplicable(ic, it))
					calculatedValues[ic]++;

	}

	/*.................................................................................................................*/
	public static Color getColorOfScore(int num, int total){
		if (num<=10)  
			return MesquiteColorTable.getDefaultColor(30, 30-num*2, MesquiteColorTable.BLUESCALE);
		else
			return MesquiteColorTable.getDefaultColor(total, total-num, MesquiteColorTable.GREENSCALE);
	}

	/*.................................................................................................................*/
	public void drawInCell(int ic, Graphics g, int x, int y, int w, int h, boolean selected) {
		if (data == null || calculatedValues==null) 
			return;


		if (ic<calculatedValues.length) {
			Color cellColor = null;
			String cellString = "";
			Color stringColor = Color.black;
			calculateValue(ic);
			if (!MesquiteInteger.isCombinable(calculatedValues[ic])) {
				cellColor=Color.white;
			} if (calculatedValues[ic]==0) {
				cellColor=Color.black;
				stringColor = Color.white;
				cellString="0";
			} else {
				int numT = data.getNumTaxa();
				if (table.numRowsSelected()>0)
					numT = table.numRowsSelected();
				cellColor = getColorOfScore(calculatedValues[ic], numT);
				if (calculatedValues[ic]<10)
					cellString = ""+calculatedValues[ic];
				if (calculatedValues[ic]<6)
					stringColor=Color.white;
			}
			g.setColor(cellColor);
			g.fillRect(x,y,w,h);
			g.setColor(stringColor);

			StringBuffer sb = new StringBuffer();
			FontMetrics fm = g.getFontMetrics(g.getFont());
			int svp = StringUtil.getStringVertPosition(fm, y, h, null);

			int length = fm.stringWidth(cellString);
			int useX = x + (w - length) / 2;
			g.drawString(cellString, useX, svp);

		}
		else {
			g.setColor(Color.white);
			g.fillRect(x,y,w,h);
		}
	}

	/*.................................................................................................................*/
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj instanceof Taxa &&  (Taxa)obj ==data.getTaxa()) {
			if (code==MesquiteListener.SELECTION_CHANGED && selectedOnly.getValue()) {
				calculateValues();
			}
			else if (code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_DELETED) {
				calculateValues();
			}
		}
		else if (obj instanceof CharacterData && (CharacterData)obj ==data) {
			if (code==MesquiteListener.PARTS_DELETED || code==AssociableWithSpecs.SPECSSET_CHANGED || code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_MOVED || code==MesquiteListener.DATA_CHANGED) {
				calculateValues();
			}
			else{
				calculateValues();
			}
		}
		super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	public String getTitle() {
		return "Number of Data Cells Strip";
	}


	public String getName() {
		return "Number of Data Cells Strip";
	}	


	public String getExplanation() {
		return "Displays the number of cells in a character containing data as an info strip in a character matrix editor.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
	}

}
