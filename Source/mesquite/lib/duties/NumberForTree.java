/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;




/* ======================================================================== */
/**Supplies a number for a tree.*/

public abstract class NumberForTree extends NumberForTreeWContext implements NumberForItem  {

   	 public Class getDutyClass() {
   	 	return NumberForTree.class;
   	 }
 	public String getDutyName() {
 		return "Number for Tree";
   	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#NumberOfTaxa", "#TreeValueUsingMatrix", "#NumForTreeWithChar", "#NumForTreeWith2Chars"};
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree){}

	
	public abstract void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString); 
	
	public  void calculateNumberInContext(Tree tree, TreeSource source, int whichTree, MesquiteNumber result, MesquiteString resultString){
	   	clearResultAndLastResult(result);
		calculateNumber(tree, result, resultString);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
 	public boolean returnsMultipleValues(){
  		return false;
  	}
	/*===== For NumberForItem interface ======*/
   	public void initialize(Object object1, Object object2){
		if (object1 instanceof Tree) 
   			initialize((Tree)object1);
   	}
	public  void calculateNumberInContext(Object object1, Object object2, ItemsSource source, int whichItem, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (object1 instanceof Tree) {
			calculateNumberInContext((Tree)object1, (TreeSource)source, whichItem, result, resultString);
			saveLastResult(result);
			saveLastResultString(resultString);
		}
	}
	public  void calculateNumber(Object object1, Object object2, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (object1 instanceof Tree) {
			calculateNumber((Tree)object1, result, resultString);
			saveLastResult(result);
			saveLastResultString(resultString);
		}
	}
	/** indicates default optimization, e.g. for tree searchers.  If true, tree search will maximize,
	otherwise minimize.  If number has obvious optimum direction, this should be overridden to indicate
	optimum*/
	public boolean biggerIsBetter() {
		return true;
	}
  	public String getNameOfValueCalculated(){ 
		return getNameAndParameters();
   	}
}

