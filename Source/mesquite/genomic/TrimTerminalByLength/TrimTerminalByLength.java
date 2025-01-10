package mesquite.genomic.TrimTerminalByLength;

import java.awt.Checkbox;
import java.awt.TextArea;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.DataAlterer;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ExtensibleDialog;

public class TrimTerminalByLength extends DataAlterer implements AltererSimpleCell{
	int numToTrim = 10;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("numToTrim".equalsIgnoreCase(tag))
			numToTrim= MesquiteInteger.fromString(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "numToTrim",numToTrim);

		return buffer.toString();
	}

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Trim Specified Length From Ends",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		IntegerField numField = dialog.addIntegerField("Number of cells containing data to trim from each end", numToTrim, 8, 0, MesquiteInteger.unassigned);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			numToTrim = numField.getValue();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alterData(CharacterData dData, MesquiteTable table,  UndoReference undoReference){
		if (dData==null)
			return -10;
		if (!(dData instanceof CategoricalData))
			return INCOMPATIBLE_DATA;
		CategoricalData data= (CategoricalData)dData;
		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying about options")){ //need to check if can proceed
			if (!queryOptions())
				return USER_STOPPED;
		}

   		UndoInstructions undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});
		boolean changed = false;
		
		for (int it = 0; it<data.getNumTaxa(); it++)
			if (table == null || !table.anyRowSelected()||table.wholeRowSelectedAnyWay(it)) {
				int count = 0;
				for (int ic = 0; ic<data.getNumChars(); ic++){
					if (!data.isInapplicable(ic, it)) {
						count++;
						if (count<=numToTrim) {
							data.setState(ic, it, CategoricalState.inapplicable);
							changed=true;
						}
						else
							break;
					}
				}
				count = 0;
				for (int ic = data.getNumChars()-1; ic>=0; ic--){
					if (!data.isInapplicable(ic, it)) {
						count++;
						if (count<=numToTrim) {
							data.setState(ic, it, CategoricalState.inapplicable);
							changed = true;
						}
						else
							break;
					}
				}
			}
		if (undoInstructions!=null) {
			undoInstructions.setNewData(data);
			if (undoReference!=null){
				undoReference.setUndoer(undoInstructions);
				undoReference.setResponsibleModule(this);
			}
		}
		if ( changed)
		return SUCCEEDED;
		return MEH;
	}	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}

	public String getName() {
		return "Trim Specified Length From Ends";
	}

	public String getNameForMenuItem() {
		return "Trim Specified Length From Ends...";
	}

}
