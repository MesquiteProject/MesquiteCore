/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharMatricesListConvertDNAToProtein;
/* created May 02 */

import java.awt.Checkbox;
import java.util.Vector;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.ProteinData;
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
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lists.lib.CharMatricesListProcessorUtility;
import mesquite.lists.lib.ListModule;

/* ======================================================================== */
public class CharMatricesListConvertDNAToProtein extends CharMatricesListProcessorUtility {
	boolean anyExcluded = true;  //default true so provokes question if no data matrix
	boolean preferencesSet=false;
	MesquiteBoolean removeDNAMatrix = new MesquiteBoolean(false); //(-e)
	MesquiteBoolean translateExcludedCharacters = new MesquiteBoolean(false);
	boolean alreadyQueried = false;
	/*.................................................................................................................*/
	public String getName() {
		return "Convert DNA Matrices to Protein";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Convert DNA Matrices to Protein...";
	}
	/*.................................................................................................................*/

	public String getExplanation() {
		return "Converts selected DNA matrices in List of Character Matrices window into protein matrices.  Codon positions must be assigned to a matrix before it can be converted." ;
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
		if ("translateExcludedCharacters".equalsIgnoreCase(tag))
			translateExcludedCharacters.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("removeDNAMatrix".equalsIgnoreCase(tag))
			removeDNAMatrix.setValue(MesquiteBoolean.fromTrueFalseString(content));

		preferencesSet = true;
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "translateExcludedCharacters", translateExcludedCharacters);
		StringUtil.appendXMLTag(buffer, 2, "removeDNAMatrix", removeDNAMatrix);

		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("removeDNAMatrix "+ removeDNAMatrix.toOffOnString());
		temp.addLine("translateExcludedCharacters "+ translateExcludedCharacters.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not to remove original DNA matrices.", "[on or off]", commandName, "removeDNAMatrix")) {
			boolean current = removeDNAMatrix.getValue();
			removeDNAMatrix.toggleValue(parser.getFirstToken(arguments));
			if (current!=removeDNAMatrix.getValue()) {
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to remove excluded characters.", "[on or off]", commandName, "translateExcludedCharacters")) {
			boolean current = translateExcludedCharacters.getValue();
			translateExcludedCharacters.toggleValue(parser.getFirstToken(arguments));
			if (current!=translateExcludedCharacters.getValue()) {
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
			Checkbox deleteMatricesBox = dialog.addCheckBox("Delete original matrices", removeDNAMatrix.getValue());
			Checkbox deleteExcludedBox=null;
			if (anyExcluded)
				deleteExcludedBox = dialog.addCheckBox("Translate excluded characters", !translateExcludedCharacters.getValue());
			dialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				removeDNAMatrix.setValue(deleteMatricesBox.getState());
				if (anyExcluded)
					translateExcludedCharacters.setValue(!deleteExcludedBox.getState());
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
		int deleted = 0;
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();
		
		
		Vector v = pauseAllPausables();
		MesquiteBoolean userAborted = new MesquiteBoolean(false);
		int numMatrices = datas.size();
		boolean success = false;
		ProgressIndicator progIndicator = new ProgressIndicator(getProject(),getName(), "Converting matrices...", datas.size(), true);
		if (progIndicator!=null){
			progIndicator.start();
		}
		
		for (int im = 0; im < datas.size(); im++){
			progIndicator.setCurrentValue(im);
			if (progIndicator.isAborted()) {
				if (userAborted !=null)
					userAborted.setValue(true);
				break;
			}
			found = true;
			CharacterData data = (CharacterData)datas.elementAt(im);
			progIndicator.setText("Converting matrix " + im + ": " +data.getName());
			if (data instanceof DNAData){
				DNAData dData = (DNAData)data;
				ProteinData pData = dData.getProteinData(null, true);
				if (pData!= null) {
					pData.setName(dData.getName()+".aa");
					pData.addToFile(getProject().getHomeFile(), getProject(),  findElementManager(ProteinData.class));  
					count++;
					success=true;
					if (success){
						if (removeDNAMatrix.getValue()){
							data.deleteMe(false);
							deleted++;
						}
					}
				} else countFailed++;
			} else countFailed++;
		}
		if (progIndicator!=null) progIndicator.goAway();

		logln("Total matrices converted: " + count);
		for (int im = datas.size()-1; im>=0; im--){
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (data.isDisposed())
				datas.removeElement(data, false); 
		}
		if (countFailed>0)
			discreetAlert("Some matrices could not be converted because they are of incompatible type or do not have codon positions specified");
		unpauseAllPausables(v);
		getProject().getCharacterMatrices().decrementNotifySuppress(); 
		//table.setNumRows(table.getNumRows()-deleted+1);
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

