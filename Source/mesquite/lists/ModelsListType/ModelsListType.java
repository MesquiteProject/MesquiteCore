/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.ModelsListType;
/*~~  */

import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.table.MesquiteTable;
import mesquite.lists.lib.ModelsListAssistant;

/* ======================================================================== */
public class ModelsListType extends ModelsListAssistant implements MesquiteListener {
	/*.................................................................................................................*/
	 public String getName() {
	return "Type of Model";
	 }
	 
	public String getExplanation() {
		return "Indicates type of model (e.g., Mk1, step-matrix) in list window." ;
  	 }
	ListableVector models=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		return true;
  	 }
  	 
	public void setTableAndObject(MesquiteTable table, Object obj){
		if (models !=null)
			models.removeListener(this);
		if (obj instanceof ListableVector)
			this.models = (ListableVector)obj;
		models.addListener(this);
		//table would be used if selection needed
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		//TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Type";
	}
	public String getStringForRow(int ic){
		try {
		if (models ==null || ic<0 || ic> models.size())
			return "";
		return ((CharacterModel)models.elementAt(ic)).getModelTypeName();
		}
		catch (NullPointerException e){}
		return "";
	}
	public String getWidestString(){
		return " Generalized Generalized model ";  //nonsense string to be wide
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;  
   	}
	/*.................................................................................................................*/
   	 public void endJob() {
		if (models !=null)
			models.removeListener(this);
		super.endJob();
   	 }
   	 
}

