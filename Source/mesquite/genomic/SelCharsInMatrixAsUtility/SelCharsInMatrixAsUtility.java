/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.SelCharsInMatrixAsUtility;

import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Debugg;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharacterSelector;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lists.lib.CharMatricesListProcessorUtility;

/* ======================================================================== */
public class SelCharsInMatrixAsUtility extends CharMatricesListProcessorUtility {
	/*.................................................................................................................*/
	public String getName() {
		return "Select Characters ";  
	}
	public String getNameForMenuItem() {
		return "Select Characters...";
	}

	public String getExplanation() {
		return "Selects characters in matrices in List of Character Matrices window." ;
	}
	CharacterSelector selectTask = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			selectTask = (CharacterSelector)hireNamedEmployee(CharacterSelector.class, arguments);
			if (selectTask == null)
				return sorry(getName() + " couldn't start because the requested data alterer wasn't successfully hired.");
		}
		else if (!MesquiteThread.isScripting()) {
			selectTask = (CharacterSelector)hireEmployee(CharacterSelector.class, "Alterer of matrices");
			if (selectTask == null)
				return sorry(getName() + " couldn't start because no matrix alterer module obtained.");
		}
		return true;
	}
 	public String getNameForProcessorList() {
 		if (selectTask != null)
 			return getName() + "(" + selectTask.getName() + ")";
 		return getName();
   	}
	/*.................................................................................................................*/
 public String getNameAndParameters() {
	 if (selectTask==null)
		 return "Select Characters";
	 else
		 return selectTask.getNameAndParameters();
 }
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setCharacterSelector ", selectTask);  
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that selects characters", "[name of module]", commandName, "setCharacterSelector")) {
			CharacterSelector temp =  (CharacterSelector)replaceEmployee(CharacterSelector.class, arguments, "Character selector", selectTask);
			if (temp!=null) {
				selectTask = temp;
				return selectTask;
			}

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	
	boolean firstTime = true;
	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table){
		CompatibilityTest test = selectTask.getCompatibilityTest();
		Debugg.println("selectTask " + selectTask.getName() + " test ");
		firstTime = true;
		getProject().getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(this);
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();
		Vector v = pauseAllPausables();
		int count = 0;
		
		ProgressIndicator progIndicator = new ProgressIndicator(getProject(),"Selecting characters", "", datas.size(), true);
		progIndicator.start();
		boolean abort = false;
		for (int im = 0; im < datas.size() && !abort; im++){
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (progIndicator.isAborted())
				abort=true;
			if (!abort && test.isCompatible(data, getProject(), this)){
				if (datas.size()<=50)
					logln("Selecting characters of \"" + data.getName() + "\"");
				progIndicator.setText("Selecting characters of " +data.getName());
				MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
				selectTask.selectCharacters(data);
				MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);
				progIndicator.increment();
				if (im < 2)
					progIndicator.toFront();
				if (datas.size()>50 && im != 0 && im % 50 == 0)
					logln("" + (im) +  " matrices altered.");
					Notification notification = new Notification(MesquiteListener.SELECTION_CHANGED);
					data.notifyListeners(this, notification);
					count++;
				} 
				firstTime = false;
			}

		progIndicator.goAway();
		unpauseAllPausables(v);
		if (getProject() != null)
			getProject().decrementProjectWindowSuppression();
		getProject().getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(null);
		resetAllMenuBars();
		return true;
	}
	public boolean okToInteractWithUser(int howImportant, String messageToUser){
		return firstTime;
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
		return false;  
	}
	public void endJob() {
		super.endJob();
	}

}

