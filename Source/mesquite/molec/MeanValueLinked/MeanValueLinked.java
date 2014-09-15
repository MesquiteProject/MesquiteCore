/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.MeanValueLinked; 
/*~~  */
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;


public class MeanValueLinked extends NumberForCharacter implements NumberForCharacterIncr, NumForCharTreeIndep {   //CHANGE CLASS NAME TO WHAT YOU WANT and use same name for package (see above)
	boolean warnedOnce = false;
	int item = 0;
	ContinuousData linkedData;
	/*.................................................................................................................*/
	/** a method called when the module is started up.  You can put initialization stuff here*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/*if (condition !=null && condition!= ContinuousData.class && condition!=ContinuousState.class) {
  	 		if (!MesquiteThread.isScripting()) alert("Mean Value of Character (Linked) could not start because it can be used only for continuous-valued characters");
			return false;
 		}*/
 		return true;
  	 }
  	 
   	 public void endJob() {
   		if (linkedData!=null) linkedData.removeListener(this);
   	 	super.endJob();
   	 }
  	 
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		if (obj == linkedData)
			parametersChanged(notification);
	}
 	public void setCurrent(long i){ //SHOULD NOT notify (e.g., parametersChanged)
 		item = (int)i;
 	}
 	public long getCurrent(){
 		return item;
 	}
 	public String getItemTypeName(){
 		return "Item";
 	}
 	public long getMin(){
 		return 0;
 	}
 	public long getMax(){
 		if (linkedData !=null)
 			return linkedData.getNumItems();
 		return 0;
 	}
 	public long toInternal(long i){ //return whether 0 based or 1 based counting
 		return i-1;
 	}
 	public long toExternal(long i){ //return whether 0 based or 1 based counting
 		return i+1;
 	}

   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(CharacterDistribution charStates){
   	}
   	boolean first = true;
   	ContinuousDistribution getLinkedContinuousDistribution(Vector linked, int ic){
   		linkedData = null;
   		for (int i=0; i<linked.size(); i++){
   			//TODO: some means to choose which linked if more than one
   			CharacterData data = (CharacterData)linked.elementAt(i);
   			if (data instanceof ContinuousData){
   				if (linkedData!=null && linkedData!=data)
					linkedData.removeListener(this);
     				if (linkedData!=data)
					data.addListener(this);
 				linkedData = (ContinuousData)data;
   				return (ContinuousDistribution)data.getCharacterDistribution(ic);
   			}
   		}
   		if (first)
   			alert("No linked matrix was found.  The calculations could not be completed");
   		first = false;
   		return null;
   	}
	/*.................................................................................................................*/
	/** the method that other modules call to tell this one to calculate the number for the character.  Note that the result is not immediately
	returned, but rather is remembered and returned when getNumber is called (this requires care in use!; perhaps should be redesigned)*/
	public  void calculateNumber(CharacterDistribution origStates, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		if (origStates==null ||  origStates.getParentData()==null || !MesquiteInteger.isNonNegative(origStates.getParentCharacter())){
			linkedData = null;
			result.setToUnassigned();
		}
		else {
			CharacterData origData = origStates.getParentData();
			int ic = origStates.getParentCharacter();
			Vector linked = origData.getDataLinkages();
			ContinuousDistribution linkedStates = getLinkedContinuousDistribution(linked, ic);
			if (linkedStates==null)
				result.setToUnassigned();
			else if  (!(linkedStates instanceof ContinuousDistribution)){
				String s = "Mean value of character can be calculated only for Continuous characters";
				if (!warnedOnce)
					discreetAlert(s);
				warnedOnce = true;
				result.setToUnassigned();
				if (resultString != null)
					resultString.setValue(s);
			}
			else {
				warnedOnce = false;
				ContinuousDistribution cStates = (ContinuousDistribution)linkedStates;
				int numtaxa = cStates.getNumTaxa();
				double sum= 0.0;
				int n=0;
				for (int i=0; i<numtaxa; i++){
					double s = cStates.getState (i, item);
					if (MesquiteDouble.isCombinable(s)) {
						sum += s;
						n++;
					}
				}
				if (n==0)
					result.setToUnassigned();
				else
					result.setValue(sum/n);
			}
		}
		if (resultString!=null)
			resultString.setValue("Mean value of linked character: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
		if (linkedData==null) {
			discreetAlert("Sorry, no linked matrix was found.  The module \"" + getName() + "\" will quit.");
			iQuit();
		}
	}
	/*.................................................................................................................*/
	/** this method is called for a bit of an explanation of the number calculated*/
    	 public String getParameters() {
    	 	String s = "Linked matrix: ";
    	 	if (linkedData!=null && linkedData.getItemName(item) !=null){
			s+= linkedData.getName();
    	 		if (linkedData!=null && linkedData.getItemName(item) !=null)
				s+= "; item " + linkedData.getItemName(item);

		}
		return s;

   	 }
   	 
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** name of this module as it appears in menus, etc.*/
    	 public String getName() {
		return "Mean value of character (linked matrix)";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates the mean observed trait for continuous character in a linked matrix." ;
   	 }
   	 
	/*.................................................................................................................*/
}

