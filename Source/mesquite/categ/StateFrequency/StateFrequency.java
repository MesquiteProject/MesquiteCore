/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison.
Version 2.01, December 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

This module originally by P. Midford, based on MeanValue.
 */
package mesquite.categ.StateFrequency;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.NumberForCharacter;
import mesquite.lib.duties.NumberForCharacterIncr;

public class StateFrequency extends NumberForCharacter {
    boolean warnedOnce = false;
    CategoricalDistribution cStates;

    //choice of what state to check
    int selectedState=0;
    long selectedStates;
    MesquiteMenuItemSpec stateItem;
    MesquiteCommand stateChoiceCommand;

    public StateFrequency() {
    }

    public boolean startJob(String arguments, Object condition, boolean hiredByName) {
        if (condition !=null && condition!= CategoricalData.class && condition!=CategoricalState.class) {
            return sorry("State Frequency of Character could not start because it can be used only for categorical characters");
        }
        if (!(NumberForCharacterIncr.class.isAssignableFrom(getHiredAs()))){ //not hired as obedient
            stateChoiceCommand = MesquiteModule.makeCommand("setState",  this);
            stateItem = addMenuItem("State for State Frequency...", stateChoiceCommand);
        }
        return true;
    }
    /*.................................................................................................................*/
    public boolean isPrerelease(){
        return false;
    }
    /*.................................................................................................................*/
    public boolean showCitation(){
        return false;
    }
     
    public void setCurrent(long i){ //SHOULD NOT notify (e.g., parametersChanged)
        selectedState = (int)i;
    }
    public long getCurrent(){
        return selectedState;
    }
    public String getItemTypeName(){
        return "Item";
    }
    
    public long getMin(){
        return 0;
    }
    public long getMax(){
       long max=0;
    /*  if (cStates !=null)
           	max = cStates.getMaxState();
       if (max<0) max=0;
       */
       CategoricalData data = (CategoricalData)cStates.getParentData();
      return data.getMaxPossibleState();
    }
    /*.................................................................................................................*/
     public Snapshot getSnapshot(MesquiteFile file) {
        Snapshot temp = new Snapshot();
        temp.addLine("setItem " + (selectedState));
        return temp;
     }
    /*.................................................................................................................*/
     public Object doCommand(String commandName, String arguments, CommandChecker checker) {
         if (checker.compare(this.getClass(), "Sets the state whose frequency should be calculated.", "[state number]", commandName, "setState")) {
             int ic = MesquiteInteger.fromString(arguments);
             if (!MesquiteInteger.isCombinable(ic) && cStates!=null){
                 ic = MesquiteInteger.queryInteger(null, "Select State", "State whose frequency will be calculated", "", selectedState, (int)getMin(), (int)getMax(), false);
             }
             if (!MesquiteInteger.isCombinable(ic))
                 return null;
             if (cStates==null) {
                 selectedState = ic;
             }
             else if (cStates !=null && cStates instanceof CategoricalDistribution) {
                 if ((ic>=0) && (ic<=getMax())) {
                     selectedState = ic;
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
       return new RequiresAnyCategoricalData();
   }
 

     public void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
         if (result==null)
             return;
         clearResultAndLastResult(result);
         String itemName = Integer.toString(selectedState+1);
         if (charStates!=null)
             if  (!(charStates instanceof CategoricalDistribution)){
                 if (!warnedOnce)
                     discreetAlert( "Frequency of character state can be calculated only for Categorical characters");
                 warnedOnce = true;
                 if (resultString!=null)
                     resultString.setValue("No frequency of character state; can be calculated only for Categorical characters");
             }
             else {
                 warnedOnce = false;
                 cStates = (CategoricalDistribution)charStates;
                 int numtaxa = cStates.getNumNodes();
                 int count= 0;
                 int n=0;
                 for (int i=0; i<numtaxa; i++){
                     long s = cStates.getState(i);
                     if (CategoricalState.isCombinable(s)) {
                         if (CategoricalState.isElement(s, selectedState))
                             count++;
                         n++;
                     }
                 }
                 if (n != 0)
                     result.setValue((1.0*count)/n);
                 if (resultString!=null)
                     resultString.setValue("State Frequency: "+ result.toString() + " for State: " + itemName);
             }
         saveLastResult(result);
         saveLastResultString(resultString);
     }
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return 200;  
 	}

    public void initialize(CharacterDistribution charStates) {
    }

    public String getName() {
        return "Frequency of State";
    }
    public String getParameters(){
    	return "Frequency of State " + selectedState;
    }

    public String getExplanation() {
        return "Calculates the frequency of a state for a categorical character";
    }
    

}
