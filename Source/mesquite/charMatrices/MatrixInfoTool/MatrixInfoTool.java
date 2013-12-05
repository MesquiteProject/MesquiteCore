/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.MatrixInfoTool; 

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* TODO: 
 * 	- emphasize rows and columns on mousedowns
 * 	- convert to tool tip
 * 	- have info for things other than data cells; e.g., for whole taxa
 */
/* ======================================================================== */
public class MatrixInfoTool extends DataWindowAssistantI {
	CMTable table;
	CharacterData  data;
	Taxa taxa;
	protected TableTool matrixInfoTool;
	MesquiteWindow window;
	MesquitePopup popup;
	MesquiteCommand respondCommand;

	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (containerOfModule() instanceof MesquiteWindow) {
			matrixInfoTool = new TableTool(this, "matrixInfo", getPath(), "matrixInfo.gif", 8, 8,"Data information", "This tool shows information about the data in each taxon", MesquiteModule.makeCommand("matrixInfo",  this) , null, null);
			matrixInfoTool.setWorksOnColumnNames(false);
			matrixInfoTool.setWorksOnRowNames(false);
			matrixInfoTool.setWorksOnMatrixPanel(true);
			matrixInfoTool.setWorksOnCornerPanel(false);
			//matrixInfoTool.setEmphasizeRowsOnMouseDown(true);
			window = (MesquiteWindow)containerOfModule();
			window.addTool(matrixInfoTool);
			respondCommand = makeCommand("respond", this);
		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = (CMTable)table;
		this.data = data;
		taxa = data.getTaxa();
	}
	/*.................................................................................................................*/
	public int getApplicableNonMissing(int ic, int it, boolean before){
		int count = 0;
		if (before) {
			for (int i = 0; i<ic; i++)
				if (!data.isUnassigned(i, it) && !data.isInapplicable(i,it))
					count++;
		} else
			for (int i = ic+1; i<data.getNumChars(); i++)
				if (!data.isUnassigned(i, it) && !data.isInapplicable(i, it))
					count++;

		return count;
	}

	/*.................................................................................................................*/
	void addToPopup(String s,int response){
		if (popup==null)
			return;
		popup.addItem(s, this, respondCommand, Integer.toString(response));
	}
 	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "move touched cell or selected cells", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "matrixInfo")) {
 	 		if (table!=null && data !=null && taxa!=null){
 	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			int column= MesquiteInteger.fromString(arguments, io);
	   			int row= MesquiteInteger.fromString(arguments, io);
				int responseNumber = 0;
				if (popup==null)
					popup = new MesquitePopup(window.getGraphicsArea());
				popup.removeAll();
				addToPopup("Taxon: " + taxa.getTaxonName(row)+", character: " + (column+1), responseNumber++);
				addToPopup("-", responseNumber++);

				int applicableNonMissingBefore = getApplicableNonMissing(column, row,true);
				int applicableNonMissingAfter = getApplicableNonMissing(column, row,false);
				int totalApplicableNonMissing = applicableNonMissingBefore + applicableNonMissingAfter;
				if (!data.isUnassigned(column, row) && !data.isInapplicable(column, row))
					totalApplicableNonMissing++;
				
				addToPopup("Total number of " + data.getNameOfCellEntry(2)+ ": " + totalApplicableNonMissing, responseNumber++);
				addToPopup("Number to left: " + applicableNonMissingBefore + " " + data.getNameOfCellEntry(applicableNonMissingBefore), responseNumber++);
				addToPopup("Number to right: " + applicableNonMissingAfter + " " + data.getNameOfCellEntry(applicableNonMissingAfter), responseNumber++);
				if (data instanceof DNAData){
					addToPopup("-", responseNumber++);
					addToPopup("Frequencies within character: ", responseNumber++);
					addToPopup("   "+ ((DNAData)data).getStateFrequencyString(column), responseNumber++);
					
				}
				popup.showPopup(table.getColumnX(column), table.getRowY(row));
	   		}
   	 	}
    	 	else if (checker.compare(this.getClass(), "Responds to choice of popup menu", "[choice number]", commandName, "respond")) {
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Matrix Info";
   	 }
    		/*.................................................................................................................*/
    		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
    		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
    		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    		public int getVersionOfFirstRelease(){
    			return 200;  
    		}
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Shows Information about the data in each taxon." ;
   	 }
   	 
}



