/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.NumberOfTaxa;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/** this is a silly little module that can be used as a demonstration for NumberForTree modules */
public class NumberOfTaxa extends NumberForTree {
	MesquiteNumber nt;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		nt= new MesquiteNumber();
 		return true;
  	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 if (result==null || tree==null)
    	 		return;
    	clearResultAndLastResult(result);
		nt.setValue(tree.numberOfTerminalsInClade(tree.getRoot()));
		result.setValue(nt);
		if (resultString!=null)
			resultString.setValue("Taxa: "+ nt.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/**Returns true if the module is to appear in menus and other places in which users can choose, and if can be selected in any way other than by direct request*/
	public boolean getUserChooseable(){
		return false;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
    	 public String getName() {
		return "Number of Taxa";
   	 }
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return "Taxa";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Counts the number of taxa in a tree." ;
   	 }
   	 
}

