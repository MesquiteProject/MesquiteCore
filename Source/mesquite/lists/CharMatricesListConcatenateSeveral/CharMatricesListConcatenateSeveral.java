/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharMatricesListConcatenateSeveral;
/* created May 02 */

import java.awt.Checkbox;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lists.lib.CharMatricesListProcessorUtility;
import mesquite.lists.lib.ListModule;
import mesquite.lists.CharMatricesListConcatenate.CharMatricesListConcatenate;

/* ======================================================================== */
public class CharMatricesListConcatenateSeveral extends mesquite.lists.CharMatricesListConcatenate.CharMatricesListConcatenate {
	int numParts = 5;
	int numMatricesPerPart = 15;
	/*.................................................................................................................*/
	public String getName() {
		return "Concatenate Matrices into Several Matrices";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Concatenate Matrices into Several Matrices...";
	}

	public String getExplanation() {
		return "Concatenates  matrices in List of Character Matrices window into several matrices.  Only those compatible with first selected are concatenated into it." ;
	}

	/*.................................................................................................................*
	public void processSingleXMLPreference(String tag, String content) {
		if ("prefixGroupLabelNames".equalsIgnoreCase(tag))
			prefixGroupLabelNames.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("concatExcludedCharacters".equalsIgnoreCase(tag))
			concatExcludedCharacters.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("removeConcatenated".equalsIgnoreCase(tag))
			removeConcatenated.setValue(MesquiteBoolean.fromTrueFalseString(content));

		preferencesSet = true;
	}

	/*.................................................................................................................*
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "concatExcludedCharacters", concatExcludedCharacters);
		StringUtil.appendXMLTag(buffer, 2, "prefixGroupLabelNames", prefixGroupLabelNames);
		StringUtil.appendXMLTag(buffer, 2, "removeConcatenated", removeConcatenated);

		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setRemoveConcatenated "+ removeConcatenated.toOffOnString());
		temp.addLine("setPrefixGroupLabelNames "+ prefixGroupLabelNames.toOffOnString());
		temp.addLine("setConcatExcludedCharacters "+ concatExcludedCharacters.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not to remove concatenated matrices.", "[on or off]", commandName, "setRemoveConcatenated")) {
			boolean current = removeConcatenated.getValue();
			removeConcatenated.toggleValue(parser.getFirstToken(arguments));
			if (current!=removeConcatenated.getValue()) {
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to previx group label names.", "[on or off]", commandName, "setPrefixGroupLabelNames")) {
			boolean current = prefixGroupLabelNames.getValue();
			prefixGroupLabelNames.toggleValue(parser.getFirstToken(arguments));
			if (current!=prefixGroupLabelNames.getValue()) {
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to remove excluded characters.", "[on or off]", commandName, "setConcatExcludedCharacters")) {
			boolean current = concatExcludedCharacters.getValue();
			concatExcludedCharacters.toggleValue(parser.getFirstToken(arguments));
			if (current!=concatExcludedCharacters.getValue()) {
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/** Re-query the user about all options and setting OTHER THAN most cases of setting employees.
	 * For use by ProcessDataFiles and ProcessMatrices. Used to review current parameter setting while leaving (most) employee relationships in place.*/
	public void queryLocalOptions () {
		queryOptions(true);
	}

	/*.................................................................................................................*
	public boolean queryOptions(boolean overrideScripting) {

		if ((overrideScripting || !MesquiteThread.isScripting()) && okToInteractWithUser(CAN_PROCEED_ANYWAY, "Concatenated matrices")){
			alreadyQueried = true;
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Concatenation Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			if (anyExcluded) {
				dialog.appendToHelpString("During concatenation, characters that are currently excluded will be omitted entirely from the concatenated matrix if \"Remove excluded characters\" is checked.<BR>");
			}
			dialog.appendToHelpString("You may choose to have the original matrices deleted after concatenation. <BR>");
			dialog.appendToHelpString("Any characters not currently assigned to groups will be assigned to a group whose name is the name of the matrix from which they came. ");
			dialog.appendToHelpString("Those characters that where previously assigned to a group will still be assigned to that group, or, optionally, to a new group whose name is its existing group name with the matrix name prefixed to it. ");
			Checkbox deleteMatricesBox = dialog.addCheckBox("Delete original matrices", removeConcatenated.getValue());
			Checkbox prefixGroupLabelNamesBox = dialog.addCheckBox("Prefix existing character group labels with matrix name", prefixGroupLabelNames.getValue());
			Checkbox deleteExcludedBox=null;
			if (anyExcluded)
				deleteExcludedBox = dialog.addCheckBox("Remove excluded characters", !concatExcludedCharacters.getValue());
			dialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				removeConcatenated.setValue(deleteMatricesBox.getState());
				prefixGroupLabelNames.setValue(prefixGroupLabelNamesBox.getState());
				if (anyExcluded)
					concatExcludedCharacters.setValue(!deleteExcludedBox.getState());
				storePreferences();
			}
			dialog.dispose();
			return (buttonPressed.getValue()==0);

		}
		return true;
	}

	
	/*.................................................................................................................*/
	
	public boolean concatenate (ListableVector datas, MesquiteInteger count, MesquiteInteger countFailed, MesquiteInteger deleted) {
		int numMatrices = datas.size();
		boolean found = false;
		int part=1;
		for (int i=0; i<numMatrices; i+= numMatricesPerPart) {
			if (concatenateSome(datas, numMatrices, i, i+numMatricesPerPart-1, true, part, count, countFailed, deleted))
				found = true;
			part++;
		}
		return found;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	public void endJob() {
		super.endJob();
	}

}

