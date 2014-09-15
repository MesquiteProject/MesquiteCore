/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.ItemValueInTaxon; 
/*~~  */
import java.util.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.ancstates.TraceCharacterHistory.TraceCharacterOperator;
import mesquite.cont.lib.*;


public class ItemValueInTaxon extends NumberForCharacter implements NumberForCharacterIncr, NumForCharTreeIndep {   //CHANGE CLASS NAME TO WHAT YOU WANT and use same name for package (see above)
	int item = 0;
	int iTaxon = 0;
	ContinuousData data = null;
	/*.................................................................................................................*/
	/** a method called when the module is started up.  You can put initialization stuff here*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/*if (condition !=null && condition!= ContinuousData.class && condition!=ContinuousState.class) {
  	 		if (!MesquiteThread.isScripting()) alert("Mean Value of Character (Linked) could not start because it can be used only for continuous-valued characters");
			return false;
 		}*/
		MesquiteMenuItemSpec mm = addMenuItem( "Next Taxon", makeCommand("nextTaxon",  this));
		mm.setShortcut(KeyEvent.VK_RIGHT); //right
		mm = addMenuItem( "Previous Taxon", makeCommand("previousTaxon",  this));
		mm.setShortcut(KeyEvent.VK_LEFT); //right
		addMenuItem( "Choose Taxon", makeCommand("chooseTaxon",  this));
		return true;
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
 		if (data !=null)
 			return data.getNumItems();
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
	/*.................................................................................................................*/
	/** the method that other modules call to tell this one to calculate the number for the character.  Note that the result is not immediately
	returned, but rather is remembered and returned when getNumber is called (this requires care in use!; perhaps should be redesigned)*/
	public  void calculateNumber(CharacterDistribution origStates, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		if (origStates==null ||  !(origStates instanceof ContinuousDistribution) || origStates.getParentData()==null || !MesquiteInteger.isNonNegative(origStates.getParentCharacter())){
			data = null;
			result.setToUnassigned();
		}
		else {
			data = (ContinuousData) origStates.getParentData();
			int ic = origStates.getParentCharacter();
			
				ContinuousDistribution cStates = (ContinuousDistribution)origStates;
				double state = cStates.getState(iTaxon, item);
					result.setValue(state);
	
		
		if (resultString!=null)
			resultString.setValue("Value of character in taxon " + (iTaxon+1) + ": "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setTaxon " + Taxon.toExternal(iTaxon));
		temp.addLine("setItem " + CharacterStates.toExternal(item));
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Goes to next character history", null, commandName, "nextTaxon")) {
			if (data == null)
				return null;
			if (iTaxon>= data.getNumTaxa())
				iTaxon=0;
			else
				iTaxon++;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Goes to previous character history", null, commandName, "previousTaxon")) {
			if (data == null)
				return null;
			if (iTaxon<=0)
				iTaxon= (int)data.getNumTaxa()-1;
			else
				iTaxon--;
			parametersChanged();
		}

		else if (checker.compare(this.getClass(), "Queries user about which character history to use", null, commandName, "chooseTaxon")) {
			if (data == null)
				return null;
			Taxa taxa = data.getTaxa();
			Taxon taxon = taxa.userChooseTaxon(containerOfModule(), "Taxon whose values to show");
			if (taxon == null)
				return null;
			iTaxon = taxa.whichTaxonNumber(taxon);
			parametersChanged();
		}

		else if (checker.compare(this.getClass(), "Sets which character history to use", "[taxon number]", commandName, "setTaxon")) {
			pos.setValue(0);

			int itNum = MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteInteger.isCombinable(itNum))
				return null;
			int it = Taxon.toInternal(itNum);
				iTaxon = it;
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets which character history to use", "[item number]", commandName, "setItem")) {
			pos.setValue(0);

			int itemNum = MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteInteger.isCombinable(itemNum))
				return null;
			int it = CharacterStates.toInternal(itemNum);
			if ((it>=0) && (it<=(int)getMax())) {
				iTaxon = it;
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** this method is called for a bit of an explanation of the number calculated*/
    	 public String getParameters() {
    		 if (data == null)
    	 	return "Values for item " + (item+1) + " for taxon " + (iTaxon+1);
    		 else
    			 return "Values for item " + (item+1) + " for " + data.getTaxa().getTaxonName(iTaxon);


   	 }
   	 
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** name of this module as it appears in menus, etc.*/
    	 public String getName() {
		return "Value of item in taxon";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Gives the value of an item for in a continuous character, for a specific taxon." ;
   	 }
   	 
	/*.................................................................................................................*/
}

