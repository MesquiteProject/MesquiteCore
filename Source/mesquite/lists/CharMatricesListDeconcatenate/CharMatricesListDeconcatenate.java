/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharMatricesListDeconcatenate;

import java.awt.Checkbox;
import java.util.Vector;

import mesquite.lib.CommandRecord;
import mesquite.lib.Debugg;
import mesquite.lib.FileElement;
import mesquite.lib.ListableVector;
import mesquite.lib.Listened;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteTimer;
import mesquite.lib.Notification;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterPartition;
import mesquite.lib.characters.CharactersGroup;
import mesquite.lib.characters.CharactersGroupVector;
import mesquite.lib.duties.ElementManager;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lists.lib.CharMatricesListUtility;

/* ======================================================================== */
public class CharMatricesListDeconcatenate extends CharMatricesListUtility {
	/*.................................................................................................................*/
	public String getName() {
		return "Deconcatenate Partitions as Separate Matrices...";
	}

	public String getExplanation() {
		return "Splits selected matrices into separate matrices, one for each partition within the matrix." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference(String tag, String content) {
		if ("deleteOriginalMatrices".equalsIgnoreCase(tag))
			deleteOriginalMatrices.setValue(MesquiteBoolean.fromTrueFalseString(content));
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "deleteOriginalMatrices", deleteOriginalMatrices);
		return buffer.toString();
	}

	MesquiteBoolean deleteOriginalMatrices = new MesquiteBoolean(true);
	/*.................................................................................................................*/
	public boolean queryOptions(ListableVector datas) {

		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Deconcatenate matrix")){
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Deconcatenation Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			String m = "original matrices";
			if (datas.size() == 1) {
				m = 	"original matrix \"" + StringUtil.shrinkInMiddle(((CharacterData)datas.elementAt(0)).getName(), 50) + "\"";
				dialog.addLabel("Deconcatenating partitions of matrix");
			}
			else
				dialog.addLabel("Deconcatenating partitions of matrices");

			Checkbox deleteMatricesBox = dialog.addCheckBox("Delete "  + m + " after deconcatenation", deleteOriginalMatrices.getValue());
			dialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				deleteOriginalMatrices.setValue(deleteMatricesBox.getState());
				storePreferences();
			}
			dialog.dispose();
			return (buttonPressed.getValue()==0);

		}
		return true;
	}
	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table){
		if (datas == null)
			return false;
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();
		if (!queryOptions(datas))
			return false;
		incrementMenuResetSuppression();
		incrementNEXUSBlockSortSuppression();
		Vector v = pauseAllPausables();
		ProgressIndicator progIndicator = null;
		CharactersGroupVector groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
		progIndicator = new ProgressIndicator(getProject(),"Deconcatenating partitions", datas.size()*groups.size());
		progIndicator.setStopButtonName("Stop");
		int progI = 0;
		int countParts = 0;
		getProject().setNotificationsOnOff(false);
		getProject().getCentralModelListener().setNotificationsOnOff(false);
		boolean abort = false;
		for (int im = 0; im < datas.size() && !abort; im++){
			CharacterData data = (CharacterData)datas.elementAt(im);
			data.setNotificationsOnOff(false);
			CharacterPartition partition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
			/* Multiple timings.
			 * 
			 * Example
			 * MesquiteTimer[] timers = MesquiteTimer.makeTimers(4);
			 * int currentTimer = MesquiteTimer.startMultipleTimings(timers, "First part");
			 * //first part of code to be timed
			 * currentTimer = MesquiteTimer.nextTimingPart(timers, currentTimer, "Second part");
			 * //second part of code to be timed
			 * currentTimer = MesquiteTimer.nextTimingPart(timers, currentTimer, "Third part");
			 * //third part of code to be timed
			 * currentTimer = MesquiteTimer.nextTimingPart(timers, currentTimer, "Fourth part");
			 * //third part of code to be timed
			 * MesquiteTimer.finishTimingParts(timers, currentTimer);
			 * if (iterations % 100 == 0)
			 * 		MesquiteTimer.summarize(timers);
			 */

		//	MesquiteTimer[] timers = MesquiteTimer.makeTimers(6);
			boolean partitonFound = false;
			boolean deleteLast = false;
			ElementManager charManager = findElementManager(CharacterData.class);
			if (partition != null){
				if (groups != null){
					progIndicator.start();
					for (int i=0; i< groups.size(); i++){
						CharactersGroup group = (CharactersGroup)groups.elementAt(i);
						int numCharsInGroup = countInGroup(partition, group, data.getNumChars());
						if (numCharsInGroup>0){
							partitonFound = true;
							String name = group.getName();
							CharacterData partData = data.makeCharacterData(data.getMatrixManager(), data.getTaxa());
							partData.setNotificationsOnOff(false);

							partData.addCharacters(0, numCharsInGroup, false);  //will trim later						}
							partData.addToFile(getProject().getHomeFile(), getProject(),  charManager); 
							partData.setName(datas.getUniqueName(group.getName()), false);
							int icPart = 0;
							for (int icOrig = 0; icOrig<data.getNumChars() && icPart<numCharsInGroup; icOrig++) {
								if (partition.getProperty(icOrig) == group){
									partData.equalizeCharacter(data, icOrig, icPart++);
									progI = (i+1)*(im+1);
								}
							}
							partData.setNotificationsOnOff(true);
							countParts++;
							deleteLast = true;
							CommandRecord.tick(Integer.toString(countParts) + " partitions deconcatenated (e.g., " + name+ ")");
							if (progIndicator!=null){
								progIndicator.setCurrentValue(progI);
								progIndicator.setText("Partition deconcatenated: " + name);
								if (progIndicator.isAborted())
									abort = true;
							}
							if (abort)
								break;
							

						}
					}

					if (!abort && progIndicator != null) {
						progIndicator.spin();
					}
					if (!abort && partitonFound){  //some were found, therefore OK to write leftovers
						int numCharsWithoutGroup = countInGroup(partition, null, data.getNumChars());
						if (numCharsWithoutGroup>0){
							CharacterData partData = data.makeCharacterData(data.getMatrixManager(), data.getTaxa());
							partData.setNotificationsOnOff(false);

							partData.addCharacters(0, numCharsWithoutGroup, false);  //will trim later						}
							partData.addToFile(getProject().getHomeFile(), getProject(),  findElementManager(CharacterData.class)); 
							partData.setName(datas.getUniqueName("Not assigned to group"), false);
							int icPart = 0;
							for (int icOrig = 0; icOrig<data.getNumChars() && icPart<numCharsWithoutGroup; icOrig++) {
								if (partition.getProperty(icOrig) == null)
									partData.equalizeCharacter(data, icOrig, icPart++);
							}

							partData.setNotificationsOnOff(true);
						}

					}
					if (progIndicator!=null)
						progIndicator.goAway();
					if (!partitonFound && !abort)
						discreetAlert("The matrix being deconcatenated is not partitioned or has no data in those partitions, and so it was not deconcatenated");

				}
			}
			data.setNotificationsOnOff(true);
			if (partitonFound && deleteOriginalMatrices.getValue())
				data.deleteMe(false);

		}
		getProject().setNotificationsOnOff(true);
		getProject().getCentralModelListener().setNotificationsOnOff(true);
		getProject().notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
		getProject().notifyListenersAllVectors();

		unpauseAllPausables(v);
		if (getProject() != null)
			getProject().decrementProjectWindowSuppression();
		decrementNEXUSBlockSortSuppression();
		decrementMenuResetSuppression();
		resetAllMenuBars();
		return true;
	}
	/*-----------------------------*/
	int countInGroup(CharacterPartition partition, CharactersGroup group, int numChars){
		int count = 0; 
		for (int icOrig = 0; icOrig<numChars; icOrig++) {
			if (partition.getProperty(icOrig) == group){
				count++;
			}
		}
		return count;

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
		return 400;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	public void endJob() {
		super.endJob();
	}

}

