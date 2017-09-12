/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

This module originally by P. Midford, based on MeanValue.
 */
package mesquite.categ.NumberOfStates;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.NumberForCharacter;
import mesquite.lib.duties.NumberForCharacterIncr;

public class NumberOfStates extends NumberForCharacter {
    boolean warnedOnce = false;
    CategoricalDistribution cStates;

    public NumberOfStates() {
    }

    public boolean startJob(String arguments, Object condition, boolean hiredByName) {
        if (condition !=null && condition!= CategoricalData.class && condition!=CategoricalState.class) {
            return sorry("Number of States could not start because it can be used only for categorical characters");
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
     
    public String getItemTypeName(){
        return "Item";
    }
    
     /*.................................................................................................................*/
     public CompatibilityTest getCompatibilityTest() {
       return new RequiresAnyCategoricalData();
   }
 

     public void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
         if (result==null)
             return;
         clearResultAndLastResult(result);
         if (charStates!=null)
             if  (!(charStates instanceof CategoricalDistribution)){
                 if (!warnedOnce)
                     discreetAlert( "Number of states can be calculated only for Categorical characters");
                 warnedOnce = true;
                 if (resultString!=null)
                     resultString.setValue("No number of states; can be calculated only for Categorical characters");
             }
             else {
                 warnedOnce = false;
                 cStates = (CategoricalDistribution)charStates;
                 int numtaxa = cStates.getNumNodes();
                 long accumulatedStates = 0;
                 for (int i=0; i<numtaxa; i++){
                     long s = cStates.getState(i);
                     if (CategoricalState.isCombinable(s)) {
                         accumulatedStates= CategoricalState.union(accumulatedStates, s);
                     }
                 }
                 int n= CategoricalState.cardinality(accumulatedStates);
                 result.setValue(n);
                 if (resultString!=null)
                     resultString.setValue("Number of states: "+ result.toString());
             }
         saveLastResult(result);
         saveLastResultString(resultString);
     }
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return 330;  
 	}

    public void initialize(CharacterDistribution charStates) {
    }

    public String getName() {
        return "Number of States";
    }

    public String getExplanation() {
        return "Shows the number of distinct states present in a categorical character";
    }
    

}
