/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.GapsToFromMissing;
/*~~  */

import java.awt.Checkbox;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class GapsToFromMissing extends CategDataAlterer {
	static final int ALLREGIONS=0;
	static final int INTERNALREGION=1;
	static final int TERMINALREGION=2;
	static final int NOREGION=3;
	int mode = INTERNALREGION;
	boolean gapsToMissing = false;
	boolean preferencesSet = false;
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("mode".equalsIgnoreCase(tag)) {
			mode = MesquiteInteger.fromString(content);
		} else  if ("gapsToMissing".equalsIgnoreCase(tag)) {   
			gapsToMissing = MesquiteBoolean.fromOffOnString(content);
		}
		preferencesSet = true;
	}
/*.................................................................................................................*/
public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "mode", mode);  
		StringUtil.appendXMLTag(buffer, 2, "gapsToMissing", gapsToMissing);  
		preferencesSet = true;
		return buffer.toString();
	}

	private synchronized boolean cellToConvert(CategoricalData data, int ic, int it) {
 		if (data.isInapplicable(ic, it) && gapsToMissing)
 			return true;
 		if (data.isUnassigned(ic, it) && !gapsToMissing)
 			return true;
 		return false;
 	}
 	private synchronized long conversionValue() {
 		if (gapsToMissing)
 			return CategoricalState.unassigned;
 		else 
 			return CategoricalState.inapplicable;
 	}

	/*.................................................................................................................*/
	public boolean queryOptions(CategoricalData data) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Gaps <-> Missing",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		queryDialog.addLabel("Converting Gaps to Missing or Missing to Gaps");
		
		int defaultValue = 0;
		if (!gapsToMissing)
			defaultValue=1;
		RadioButtons choices = queryDialog.addRadioButtons (new String[]{"Convert Gaps to Missing", "Convert Missing to Gaps"}, defaultValue);
		
		queryDialog.addHorizontalLine(1);
		
		if (data.getTaxa().anySelected())
			queryDialog.addLabel("Within selected sequences, ");
		else
			queryDialog.addLabel("For all sequences, ");

		RadioButtons whereChoice = queryDialog.addRadioButtons (new String[]{"Convert in all regions of sequence", "Convert only within internal regions of sequence", "Convert only in terminal regions beyond sequence"}, mode);

		queryDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			gapsToMissing= (choices.getValue()==0);
			mode=whereChoice.getValue() ;
			storePreferences();
		}
		queryDialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public synchronized boolean alterData(CharacterData dData, MesquiteTable table,  UndoReference undoReference){
		this.table = table;
		if (!(dData instanceof CategoricalData)){
			MesquiteMessage.warnProgrammer("Can use " + getName() + " only on categorical data");
			return false;
		}
		CategoricalData data = (CategoricalData)dData;
		if (!MesquiteThread.isScripting()){
			if (!queryOptions(data))
				return false;
		}
   		UndoInstructions undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});
		boolean noRowsSelected =  table == null || !table.anyRowSelected() ;
		for (int it = 0; it<data.getNumTaxa(); it++){
			if (table==null || noRowsSelected || table.isRowSelected(it)) {
				int cellsAltered = 0;
				boolean done = false;
				int startOfFirstDataRegion = -1;
				int endOfLastDataRegion = -1;
				for (int ic = 0; ic<data.getNumChars() && !done; ic++){  //the first terminal region
					if (cellToConvert(data, ic, it)) {
						if (mode==ALLREGIONS || mode==TERMINALREGION) {
							data.setState(ic, it, conversionValue());
							if (!MesquiteLong.isCombinable(numCellsAltered))
								numCellsAltered = 0;
							numCellsAltered++;
						}
					}
					else if (!data.isUnassigned(ic, it)){  // we are out of the first terminal region
						done = true;
						startOfFirstDataRegion=ic;
					}
				}
				if (done){  
					done = false;
					for (int ic = data.getNumChars()-1; ic>=0 && !done; ic--){  // the end terminal region
						if (cellToConvert(data, ic, it)) {
							if (mode==ALLREGIONS || mode==TERMINALREGION) {
								data.setState(ic, it, conversionValue());
								if (!MesquiteLong.isCombinable(numCellsAltered))
									numCellsAltered = 0;
								numCellsAltered++;
							}
						}
						else if (!data.isUnassigned(ic, it)) {  // we are out of the end terminal region
							endOfLastDataRegion=ic;
							done = true;
						}
					}
				}
				if (done && (mode==ALLREGIONS || mode==INTERNALREGION)) {
					done = false;
					for (int ic = startOfFirstDataRegion; ic<endOfLastDataRegion; ic++){  // now let's scan the internal regions
						if (cellToConvert(data, ic, it)) {
							data.setState(ic, it, conversionValue());
							if (!MesquiteLong.isCombinable(numCellsAltered))
								numCellsAltered = 0;
							numCellsAltered++;
						}
						else if (!data.isUnassigned(ic, it)) {
							done = true;
						}
					}
				}
				if (MesquiteTrunk.debugMode && cellsAltered>0)
					logln(data.getTaxa().getTaxonName(it) + " cells altered: " + cellsAltered);
			}
		}
		if (undoInstructions!=null){
			undoInstructions.setNewData(data);
			if (undoReference!=null){
				undoReference.setUndoer(undoInstructions);
				undoReference.setResponsibleModule(this);
			}
		}
		return true;
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData ddata, int ic, int it){
		/*CategoricalData data = (CategoricalData)ddata;
		if (data.isUnassigned(ic, it))
			data.setState(ic, it, CategoricalState.inapplicable);
		 */
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Gaps<->Missing";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Gaps<->Missing...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Converts gaps to missing data or vice versa." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}


