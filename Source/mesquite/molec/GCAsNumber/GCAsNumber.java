/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.GCAsNumber; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;


/* ======================================================================== */
public class GCAsNumber extends NumberForCharAndTaxon {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	long A = CategoricalState.makeSet(0);
	long C = CategoricalState.makeSet(1);
	long G = CategoricalState.makeSet(2);
	long T = CategoricalState.makeSet(3);
	long AT = A | T;
	long GC = G | C;
	public void initialize(CharacterData data){
	}
	public void calculateNumber(CharacterData data, int ic, int it, MesquiteNumber result, MesquiteString resultString){
   	 	if (result == null)
   	 		return;
	   	clearResultAndLastResult(result);
  	 	if (data == null || !(data instanceof DNAData))
   	 		return;
   	 	DNAData dData = (DNAData)data;
   	 	long s = CategoricalState.statesBitsMask & dData.getState(ic, it);
   	 	if (A == s || T == s || AT == s)
   	 		result.setValue(0);
   	 	else if (C == s || G == s || GC == s)
   	 		result.setValue(1);
		saveLastResult(result);
		saveLastResultString(resultString);
  	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "GC";
   	 }
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Returns 1 for G or C, 0 for A or T.";
   	 }
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
}


	


