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

import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.tree.Tree;


/* ======================================================================== */
/**Calculates a number for a character and a tree.*/

public abstract class NumberForCharAndTree extends MesquiteModule implements NumberForItem  {

   	 public Class getDutyClass() {
   	 	return NumberForCharAndTree.class;
   	 }
 	public String getDutyName() {
 		return "Number for Character and Tree";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeCharNumber.gif";
   	 }
  	 public String[] getDefaultModule() {
 		String[] s= {"#ParsCharSteps", "#BiSSELikelihood"};
 		return s;
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Tree tree, CharacterDistribution charStates);

	public  abstract void calculateNumber(Tree tree, CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString);

 	public boolean returnsMultipleValues(){
  		return false;
  	}
	/*===== For NumberForItem interface ======*/
   	public void initialize(Object object1, Object object2){
		if (object1 instanceof Tree && object2 instanceof CharacterDistribution)
   			initialize((Tree)object1, (CharacterDistribution)object2);
   	}
	public  void calculateNumberInContext(Object object1, Object object2, ItemsSource source, int whichItem, MesquiteNumber result, MesquiteString resultString){
	   	clearResultAndLastResult(result);
		calculateNumber(object1, object2, result, resultString);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	public  void calculateNumber(Object object1, Object object2, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (object1 instanceof Tree && object2 instanceof CharacterDistribution) {
			calculateNumber((Tree)object1, (CharacterDistribution)object2, result, resultString);
			saveLastResult(result);
			saveLastResultString(resultString);
		}
		else {
			MesquiteMessage.warnProgrammer("Error: passing wrong class of objects to ItemSource calculateNumber in " + getName());
		}
	}

	
	
	public String getNameOfValueCalculated(){ 
		return getNameAndParameters();
   	}
}


