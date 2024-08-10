/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.AlterMatrixAsUtility;

import mesquite.lists.lib.*;

import java.util.Vector;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class AlterMatrixAsUtility extends DatasetsListProcessorUtility {
	/*.................................................................................................................*/
	public String getName() {
		return "Alter/Transform Matrices";  
	}
	public String getNameForMenuItem() {
		return "Alter/Transform Matrices...";
	}

	public String getExplanation() {
		return "Alters selected matrices in List of Character Matrices window." ;
	}
	DataAlterer alterTask = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			alterTask = (DataAlterer)hireNamedEmployee(DataAlterer.class, arguments);
			if (alterTask == null)
				return sorry(getName() + " couldn't start because the requested data alterer wasn't successfully hired.");
		}
		else if (!MesquiteThread.isScripting()) {
			alterTask = (DataAlterer)hireEmployee(DataAlterer.class, "Alterer/Transformer of matrices");
			if (alterTask == null)
				return sorry(getName() + " couldn't start because no tranformer module obtained.");
		}
		return true;
	}
 	public String getNameForProcessorList() {
 		if (alterTask != null)
 			return getName() + "(" + alterTask.getName() + ")";
 		return getName();
   	}
	/*.................................................................................................................*/
 public String getNameAndParameters() {
	 if (alterTask==null)
		 return "Alter Matrices";
	 else
		 return alterTask.getNameAndParameters();
 }
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setDataAlterer ", alterTask);  
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that alters data", "[name of module]", commandName, "setDataAlterer")) {
			DataAlterer temp =  (DataAlterer)replaceEmployee(DataAlterer.class, arguments, "Data alterer", alterTask);
			if (temp!=null) {
				alterTask = temp;
				return alterTask;
			}

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		if (!MesquiteThread.isScripting()){
		}
		return true;
	}
	boolean firstTime = true;
	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table){
		CompatibilityTest test = alterTask.getCompatibilityTest();
		firstTime = true;
		getProject().getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(this);
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();
		Vector v = pauseAllPausables();
		int count = 0;
		
		ProgressIndicator progIndicator = new ProgressIndicator(getProject(),"Altering matrices", "", datas.size(), true);
		progIndicator.start();
		for (int im = 0; im < datas.size(); im++){
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (test.isCompatible(data, getProject(), this)){
				if (datas.size()<=50)
					logln("Altering matrix \"" + data.getName() + "\"");
				AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
				progIndicator.setText("Altering matrix " +data.getName());
				MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
				boolean a = alterTask.alterData(data, null, null, alteredDataParameters);
				MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);
				progIndicator.increment();
				if (im < 2)
					progIndicator.toFront();
				if (datas.size()>50 && im != 0 && im % 50 == 0)
					logln("" + (im) +  " matrices altered.");
				if (a){
					Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), null);
					if (alteredDataParameters.getSubcodes()!=null)
						notification.setSubcodes(alteredDataParameters.getSubcodes());
					data.notifyListeners(this, notification);
					count++;
				}
				firstTime = false;
			}
		}
		progIndicator.goAway();
		logln("Altered: " + (count) +  " matrices.");
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
		return true;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	public void endJob() {
		super.endJob();
	}

}

