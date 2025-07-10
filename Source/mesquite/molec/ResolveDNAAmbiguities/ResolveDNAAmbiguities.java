/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.ResolveDNAAmbiguities;
/*~~  */

import java.awt.Checkbox;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNADataAlterer;
import mesquite.categ.lib.DNAState;
import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteThread;
import mesquite.lib.ResultCodes;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererDNACell;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAltererParallelizable;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.molec.lib.MolecUtil;

/* ======================================================================== */
public class ResolveDNAAmbiguities extends DNADataAlterer implements AltererDNACell, DataAltererParallelizable {
	DNAState charState = new DNAState();
	boolean randomlyChooseStateAsFallback = true;
	boolean avoidStopCodons = true;
	boolean preferencesSet=false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference(String tag, String content) {
		if ("randomlyChooseStateAsFallback".equalsIgnoreCase(tag))
			randomlyChooseStateAsFallback = MesquiteBoolean.fromTrueFalseString(content);
		if ("avoidStopCodons".equalsIgnoreCase(tag))
			avoidStopCodons = MesquiteBoolean.fromTrueFalseString(content);

		preferencesSet = true;
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "randomlyChooseStateAsFallback", randomlyChooseStateAsFallback);
		StringUtil.appendXMLTag(buffer, 2, "avoidStopCodons", avoidStopCodons);
		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
 	public Snapshot getSnapshot(MesquiteFile file) { 
 		Snapshot temp = new Snapshot();
 		temp.addLine("randomlyChooseStateAsFallback " + randomlyChooseStateAsFallback);
 		temp.addLine("avoidStopCodons " + avoidStopCodons);
		return temp;
 	}
 	/*.................................................................................................................*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
 		 if (checker.compare(this.getClass(), "Sets whether to choose states randomly as a fallback", "[true or false]", commandName, "randomlyChooseStateAsFallback")) {
 			randomlyChooseStateAsFallback = MesquiteBoolean.fromTrueFalseString(arguments);
 		}
 		else if (checker.compare(this.getClass(), "Sets whether to avoid stop codons", "[true or false]", commandName, "avoidStopCodons")) {  
 			avoidStopCodons = MesquiteBoolean.fromTrueFalseString(arguments);
 		}
		else
 			return  super.doCommand(commandName, arguments, checker);
 		return null;
 	}	

	/*.................................................................................................................*/
	public boolean queryOptions() {
		loadPreferences();

		if (!MesquiteThread.isScripting()){
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "DNA state uncertainty resolution options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			dialog.appendToHelpString("This feature will scan the matrix for any taxa with ambiguity codes (e.g., R, Y, N).  It will then examine "
		+ "the other taxa in the matrix (if you have selected any taxa, it will only do this within the selected taxa) and will look for the commonest"
		+ "nucleotide possessed by the other taxa at that site, and convert the ambiguity to that state. <BR>");
			dialog.appendToHelpString("You can choose to avoid chosing any state that would cause any triplet in which that site participates "
					+ "to have a stop codon. <BR> If this procedure fails to yield a state, you can ask Mesquite to randomly choose one of the"
					+ "states within the uncertainty as the state of that site.");
			Checkbox randomlyChooseStateAsFallbackBox = dialog.addCheckBox("Randomly choose a contained state as fallback", randomlyChooseStateAsFallback);
			Checkbox avoidStopCodonsBox = dialog.addCheckBox("Do not choose a state that would yield a stop codon", avoidStopCodons);
			dialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				randomlyChooseStateAsFallback = randomlyChooseStateAsFallbackBox.getState();
				avoidStopCodons = avoidStopCodonsBox.getState();
				storePreferences();
			}
			dialog.dispose();
			return (buttonPressed.getValue()==0);

		}
		return true;
	}	
	
	
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
		if (!(data instanceof DNAData)){
			MesquiteMessage.warnProgrammer("Can use " + getName() + " only on nucleotide data");
			return ResultCodes.INCOMPATIBLE_DATA;
		}
		if (!queryOptions())
			return ResultCodes.USER_STOPPED;
		return alterContentOfCells(data,table, undoReference);
	}



	/*.................................................................................................................*/
	public void alterCell(CharacterData ddata, int ic, int it){
		if (MolecUtil.resolveAndAssignDNAAmbiguity(this, ddata, ic, it, charState, avoidStopCodons, randomlyChooseStateAsFallback, true)) {
			if (!MesquiteLong.isCombinable(numCellsAltered))
				numCellsAltered=0;
			numCellsAltered++;
		}
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
			return false;
		}
	/*.................................................................................................................*/
	public String getName() {
		return "Resolve DNA Ambiguities";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Resolve DNA Ambiguities...";
	}
		/*.................................................................................................................*/
		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
		public int getVersionOfFirstRelease(){
			return 380;  
		}
		/*.................................................................................................................*/
		/** returns an explanation of what the module does.*/
		public String getExplanation() {
			return "Resolves uncertainties in DNA data by converting it to the most common state among chosen taxa." ;
		}

	}

