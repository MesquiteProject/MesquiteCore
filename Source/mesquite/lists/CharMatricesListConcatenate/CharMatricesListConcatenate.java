/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharMatricesListConcatenate;
/* created May 02 */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;

/* ======================================================================== */
public class CharMatricesListConcatenate extends CharMatricesListProcessorUtility {
	boolean anyExcluded = true;  //default true so provokes question if no data matrix
	boolean preferencesSet=false;
	MesquiteBoolean removeConcatenated = new MesquiteBoolean(false); //(-e)
	MesquiteBoolean concatExcludedCharacters = new MesquiteBoolean(false);
	MesquiteBoolean prefixGroupLabelNames = new MesquiteBoolean(false);
	boolean alreadyQueried = false;
	/*.................................................................................................................*/
	public String getName() {
		return "Concatenate Matrices";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Concatenate Matrices...";
	}

	public String getExplanation() {
		return "Concatenates selected matrices in List of Character Matrices window.  Only those compatible with first selected are concatenated into it." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions(false))
				return false;
		}
		return true;
	}
	
	/*.................................................................................................................*/
	public void processSingleXMLPreference(String tag, String content) {
		if ("prefixGroupLabelNames".equalsIgnoreCase(tag))
			prefixGroupLabelNames.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("concatExcludedCharacters".equalsIgnoreCase(tag))
			concatExcludedCharacters.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("removeConcatenated".equalsIgnoreCase(tag))
			removeConcatenated.setValue(MesquiteBoolean.fromTrueFalseString(content));

		preferencesSet = true;
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "concatExcludedCharacters", concatExcludedCharacters);
		StringUtil.appendXMLTag(buffer, 2, "prefixGroupLabelNames", prefixGroupLabelNames);
		StringUtil.appendXMLTag(buffer, 2, "removeConcatenated", removeConcatenated);

		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setRemoveConcatenated "+ removeConcatenated.toOffOnString());
		temp.addLine("setPrefixGroupLabelNames "+ prefixGroupLabelNames.toOffOnString());
		temp.addLine("setConcatExcludedCharacters "+ concatExcludedCharacters.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		 if (checker.compare(this.getClass(), "Sets whether or not to remove concatenated matrices.", "[on or off]", commandName, "setRemoveConcatenated")) {
			boolean current = removeConcatenated.getValue();
			removeConcatenated.toggleValue(parser.getFirstToken(arguments));
			if (current!=removeConcatenated.getValue()) {
				parametersChanged();
			}
		}
		 else if (checker.compare(this.getClass(), "Sets whether or not to remove concatenated matrices.", "[on or off]", commandName, "setPrefixGroupLabelNames")) {
			boolean current = prefixGroupLabelNames.getValue();
			prefixGroupLabelNames.toggleValue(parser.getFirstToken(arguments));
			if (current!=prefixGroupLabelNames.getValue()) {
				parametersChanged();
			}
		}
		 else if (checker.compare(this.getClass(), "Sets whether or not to remove concatenated matrices.", "[on or off]", commandName, "setConcatExcludedCharacters")) {
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

	/*.................................................................................................................*/
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

	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table){
		anyExcluded = false;
		Taxa taxa = null;
		for (int im = 0; im < datas.size(); im++){
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (taxa == null)
				taxa = data.getTaxa();
			else if (taxa != data.getTaxa()) {
				discreetAlert("Sorry, The matrices cannot be concatenated because they pertain to different taxa blocks.");
				return false;
			}
			if (data.numCharsCurrentlyIncluded() < data.getNumChars())
				anyExcluded = true;
		}
		getProject().getCharacterMatrices().incrementNotifySuppress(); 
		if (!alreadyQueried && getHiredAs() != CharMatricesListProcessorUtility.class && !queryOptions(false))
			return false;
		int count = 0;
		int countFailed = 0;
		String name = "";
		boolean found = false;
		CharacterData starter = null;   // this will be the new concatenated matrix
		int deleted = 0;
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();
		Vector v = pauseAllPausables();
		Vector chunks = new Vector();
		int chunksize = 200;
		int numMatrices = datas.size();
		if (numMatrices>(int)(chunksize*1.2)) {
			// if many matrices, do in chunks.
			CharacterData chunk = null;
			for (int im = 0; im < datas.size(); im++){
				found = true;
				CharacterData data = (CharacterData)datas.elementAt(im);
				if (im != 0 && im % chunksize == 0 && datas.size() - im > (int)(chunksize*.2)) //time to start a new chunk!
					chunk = null;
				if (chunk == null){
					chunk = data.makeCharacterData(data.getMatrixManager(), data.getTaxa());  
					chunk.addToFile(getProject().getHomeFile(), getProject(),  findElementManager(CharacterData.class));  
					chunks.addElement(chunk);
					if (starter == null) {
						starter = data.makeCharacterData(data.getMatrixManager(), data.getTaxa());  
						starter.addToFile(getProject().getHomeFile(), getProject(),  findElementManager(CharacterData.class));  
					}

				}
				boolean success = chunk.concatenate(data, false, concatExcludedCharacters.getValue(), true, prefixGroupLabelNames.getValue(), false, false);
				if (success){
					count++;
					if (count % 100== 0)
						logln("Concatenated " + count + " Matrices");
					if (count > 1)
						name = name + "+";
					name = name + "(" + data.getName() + ")";
					if (removeConcatenated.getValue()){
							data.deleteMe(false);
							deleted++;
					}
				}
				else 
					countFailed++;
			}

			for (int im = 0; im < chunks.size(); im++){
				CharacterData ch = (CharacterData)chunks.elementAt(im);
			boolean success = starter.concatenate(ch, false, concatExcludedCharacters.getValue(), true, false, false, false);
			ch.deleteMe(false);
			}
			
		}
		else for (int im = 0; im < datas.size(); im++){
			found = true;
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (starter == null){
				starter = data.makeCharacterData(data.getMatrixManager(), data.getTaxa());  

				starter.addToFile(getProject().getHomeFile(), getProject(),  findElementManager(CharacterData.class));  
			}
			boolean success = starter.concatenate(data, false, concatExcludedCharacters.getValue(), true, prefixGroupLabelNames.getValue(), false, false);
			if (success){
				count++;
				if (count % 100== 0)
					logln("Concatenated " + count + " Matrices");
				if (count > 1)
					name = name + "+";
				name = name + "(" + data.getName() + ")";
				if (removeConcatenated.getValue()){
						data.deleteMe(false);
						deleted++;
				}
			}
			else 
				countFailed++;

		}
		if (numMatrices>10)
				name = "Concat. (" + count +")";
		logln("Total matrices concatenated: " + count);
		if (starter != null)
			starter.setName(name);
		for (int im = datas.size()-1; im>=0; im--){
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (data.isDisposed())
				datas.removeElement(data, false); 
		}
		datas.addElement(starter, false);
		if (! found)
			discreetAlert("Two more more matrices should be selected first in order to concatenate them");
		if (countFailed>0)
			discreetAlert("Some matrices could not be concatenated into the first selected because they are of incompatible type or are linked to the first");
		unpauseAllPausables(v);
		getProject().getCharacterMatrices().decrementNotifySuppress(); 
		table.setNumRows(table.getNumRows()-deleted+1);
		if (employer instanceof ListModule)
			((ListModule)employer).forceRecalculations();
		if (getProject() != null)
			getProject().decrementProjectWindowSuppression();
		resetAllMenuBars();
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	public void endJob() {
		super.endJob();
	}

}

