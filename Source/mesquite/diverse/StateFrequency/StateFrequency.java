/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.
Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

This module originally by P. Midford, based on MeanValue.
 */
package mesquite.diverse.StateFrequency;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMenuItemSpec;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Snapshot;
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

    public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
        if (condition !=null && condition!= CategoricalData.class && condition!=CategoricalState.class) {
            return sorry(commandRec, "State Frequency of Character could not start because it can be used only for categorical characters");
        }
        if (!(NumberForCharacterIncr.class.isAssignableFrom(getHiredAs()))){ //not hired as obedient
            stateChoiceCommand = MesquiteModule.makeCommand("setState",  this);
            stateItem = addMenuItem("State for State Frequency...", stateChoiceCommand);
        }
        return true;
    }
    /*.................................................................................................................*/
    public boolean isPrerelease(){
        return true;
    }
    /*.................................................................................................................*/
    public boolean showCitation(){
        return false;
    }
     
    public void setCurrent(long i, CommandRecord commandRec){ //SHOULD NOT notify (e.g., parametersChanged)
        selectedState = (int)i;
    }
    public long getCurrent(CommandRecord commandRec){
        return selectedState;
    }
    public String getItemTypeName(){
        return "Item";
    }
    
    public long getMin(CommandRecord commandRec){
        return 0;
    }
    public long getMax(CommandRecord commandRec){
        if (cStates !=null)
            return cStates.getMaxState();
        return 0;
    }
    /*.................................................................................................................*/
     public Snapshot getSnapshot(MesquiteFile file) {
        Snapshot temp = new Snapshot();
        temp.addLine("setItem " + (selectedState));
        return temp;
     }
    /*.................................................................................................................*/
     public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
         if (checker.compare(this.getClass(), "Sets the state to use (in a multi-item continuous data matrix)", "[item number]", commandName, "setItem")) {
             int ic = MesquiteInteger.fromString(arguments);
             if (!MesquiteInteger.isCombinable(ic) && cStates!=null){
                 ic = MesquiteInteger.queryInteger(null, "Select State", "Enter the state for finding the frequency", "", ic, (int)getMin(commandRec), (int)getMax(commandRec), false);
             }
             if (!MesquiteInteger.isCombinable(ic))
                 return null;
             if (cStates==null) {
                 selectedState = ic;
             }
             else if (cStates !=null && cStates instanceof CategoricalDistribution) {
                 if ((ic>=0) && (ic<=getMax(commandRec))) {
                     selectedState = ic;
                     parametersChanged(null, commandRec);
                 }
             }
         }
         else
             return super.doCommand(commandName, arguments, commandRec, checker);
         return null;
     }
 
     
     /*.................................................................................................................*/
//     public CompatibilityTest getCompatibilityTest() {
//        return new CategoricalStateTest();
//     }
 

     public void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
         if (result==null)
             return;
         result.setToUnassigned();
         String itemName = Integer.toString(selectedState+1);
         if (charStates==null)
             result.setToUnassigned();
         else if  (!(charStates instanceof CategoricalDistribution)){
             if (!warnedOnce)
                 discreetAlert(commandRec, "Frequency of character state can be calculated only for Categorical characters");
             warnedOnce = true;
             result.setToUnassigned();
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
                     //Debugg.println("CharState = " + s + "selected State = " + selectedState);
                     if (CategoricalState.isElement(s, selectedState))
                             count++;
                     n++;
                 }
             }
             if (n==0)
                 result.setToUnassigned();
             else
                 result.setValue((1.0*count)/n);
             if (resultString!=null)
                 resultString.setValue("State Frequency: "+ result.toString() + " for State: " + itemName);
         }
     }

    public void initialize(CharacterDistribution charStates, CommandRecord commandRec) {
    }

    public String getName() {
        return "Frequency of State";
    }

    public String getExplanation() {
        return "Calculates the frequency of a state for a categorical character";
    }
    

}
