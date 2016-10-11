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
import mesquite.lib.characters.*;


/* ======================================================================== */
/**Supplies a number for a character matrix and a tree.*/

public abstract class NumberForMatrixAndTree extends MesquiteModule implements NumberForItem  {

   	 public Class getDutyClass() {
   	 	return NumberForMatrixAndTree.class;
   	 }
 	public String getDutyName() {
 		return "Number for Matrix And Tree";
   	 }
  	 public String[] getDefaultModule() {
    	 	return new String[] {"#TreelengthForMatrix"};
    	 }

	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeCharNumber.gif";
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Tree tree, MCharactersDistribution matrix);


	public abstract  void calculateNumber(Tree tree, MCharactersDistribution matrix, MesquiteNumber result, MesquiteString resultString);
	/*===== For NumberForItem interface ======*/
	
 	public boolean returnsMultipleValues(){
  		return false;
  	}
   	public void initialize(Object object1, Object object2){
		if (object1 instanceof Tree && object2 instanceof MCharactersDistribution)
   			initialize((Tree)object1, (MCharactersDistribution)object2);
   	}
	public  void calculateNumber(Object object1, Object object2, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (object1 instanceof Tree && object2 instanceof MCharactersDistribution) {
			calculateNumber((Tree)object1, (MCharactersDistribution)object2, result, resultString);
			saveLastResult(result);
			saveLastResultString(resultString);
		}
		else
			MesquiteMessage.warnProgrammer("Error: passing wrong class of objects to ItemSource calculateNumber in " + getName());
	}
 	public  void calculateNumberInContext(Object object1, Object object2, ItemsSource source, int whichItem, MesquiteNumber result, MesquiteString resultString){
	   	clearResultAndLastResult(result);
		calculateNumber(object1, object2, result, resultString);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
  	public String getNameOfValueCalculated(){ 
		return getNameAndParameters();
   	}
}


