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


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class AlterMatrixAsUtility extends DatasetsListUtility {
	/*.................................................................................................................*/
	public String getName() {
		return "Alter Matrix As Utility";
	}
	public String getNameForMenuItem() {
		return "Alter/Transform Selected Matrices...";
	}

	public String getExplanation() {
		return "Alters selected matrices in List of Character Matrices window." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
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

		DataAlterer tda= (DataAlterer)hireEmployee(DataAlterer.class, "How to alter data");
		if (tda == null)
			return false;
		CompatibilityTest test = tda.getCompatibilityTest();
		firstTime = true;
		getProject().getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(this);
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();
		for (int im = 0; im < datas.size(); im++){
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (test.isCompatible(data, getProject(), this)){
				logln("About to alter matrix \"" + data.getName() + "\"");
				AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
				boolean a = tda.alterData(data, null, null, alteredDataParameters);
				if (a){
					Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), null);
					if (alteredDataParameters.getSubcodes()!=null)
						notification.setSubcodes(alteredDataParameters.getSubcodes());
					data.notifyListeners(this, notification);
				}
				firstTime = false;
			}
		}
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

