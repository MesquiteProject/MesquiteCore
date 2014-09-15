/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.MeanValue; 
/*~~  */
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;


public class MeanValue extends NumberForCharacter implements NumberForCharacterIncr,  NumForCharTreeIndep{ 
	boolean warnedOnce = false;
	ContinuousDistribution cStates;

	//choice of what item to show
	int currentItem=0;
	MesquiteMenuItemSpec itemItem;
	MesquiteCommand itemChoiceCommand;
	String itemName=null;
	/*.................................................................................................................*/
	/** a method called when the module is started up.  You can put initialization stuff here*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition !=null && condition!= ContinuousData.class && condition!=ContinuousState.class) {
  	 		return sorry("Mean Value of Character could not start because it can be used only for continuous-valued characters");
 		}
		if (!(NumberForCharacterIncr.class.isAssignableFrom(getHiredAs()))){ //not hired as obedient
			itemChoiceCommand = MesquiteModule.makeCommand("setItem",  this);
			itemItem = addMenuItem("Item for Mean Value...", itemChoiceCommand);
		}
 		return true;
  	 }
  	 
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
  	 
 	public void setCurrent(long i){ //SHOULD NOT notify (e.g., parametersChanged)
 		currentItem = (int)i;
 	}
 	public long getCurrent(){
 		return currentItem;
 	}
 	public String getItemTypeName(){
 		return "Item";
 	}
 	public long getMin(){
 		return 0;
 	}
 	public long getMax(){
 		if (cStates !=null)
 			return cStates.getNumItems();
 		return 0;
 	}
 	public long toInternal(long i){ //return whether 0 based or 1 based counting
 		return i-1;
 	}
 	public long toExternal(long i){ //return whether 0 based or 1 based counting
 		return i+1;
 	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setItem " + (currentItem));
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the item to use (in a multi-item continuous data matrix)", "[item number]", commandName, "setItem")) {
    	 		int ic = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
    	 		if (!MesquiteInteger.isCombinable(ic) && cStates!=null){
				ic = cStates.userQueryItem("Select item for Mean Value calculation", this);
    	 		}
   			if (!MesquiteInteger.isCombinable(ic))
   				return null;
   			if (cStates==null) {
    	 			currentItem = ic;
   			}
 			else if (cStates !=null && cStates instanceof ContinuousDistribution) {
	   	 		if ((ic>=0) && (ic<=cStates.getNumItems()-1)) {
	    	 			currentItem = ic;
					parametersChanged();
	 			}
 			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }

	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new ContinuousStateTest();
  	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(CharacterDistribution charStates){
   	}
	/*.................................................................................................................*/
	/** the method that other modules call to tell this one to calculate the number for the character.  Note that the result is not immediately
	returned, but rather is remembered and returned when getNumber is called (this requires care in use!; perhaps should be redesigned)*/
	public  void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		String itemName = Integer.toString(currentItem+1);
		if (charStates==null)
			result.setToUnassigned();
		else if  (!(charStates instanceof ContinuousDistribution)){
			if (!warnedOnce)
				discreetAlert( "Mean value of character can be calculated only for Continuous characters");
			warnedOnce = true;
			result.setToUnassigned();
			if (resultString!=null)
				resultString.setValue("No mean value calculated; can be calculated only for Continuous characters");
		}
		else {
			warnedOnce = false;
			cStates = (ContinuousDistribution)charStates;
			int numtaxa = cStates.getNumNodes();
			if (cStates.getNumItems() > 1){
				itemName = cStates.getItemName(currentItem);
				if (StringUtil.blank(itemName))
					itemName = Integer.toString(currentItem+1);
			}
			double sum= 0.0;
			int n=0;
			for (int i=0; i<numtaxa; i++){
				double s = cStates.getState (i, currentItem);
				if (MesquiteDouble.isCombinable(s)) {
					sum += s;
					n++;
				}
			}
			if (n==0)
				result.setToUnassigned();
			else
				result.setValue(sum/n);
			if (resultString!=null)
				resultString.setValue("Mean value of character: "+ result.toString() + " for item " + itemName);
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/** name of this module as it appears in menus, etc.*/
    	 public String getName() {
		return "Mean value of character";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates the mean observed trait for continuous character." ;
   	 }
   	 
}

