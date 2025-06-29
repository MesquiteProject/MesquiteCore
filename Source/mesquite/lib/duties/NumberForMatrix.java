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

import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.MCharactersDistribution;


/* ======================================================================== */
/**Supplies a number for a data set (a CharacterData object)*/

public abstract class NumberForMatrix extends MesquiteModule implements NumberForItem  {

   	 public Class getDutyClass() {
   	 	return NumberForMatrix.class;
   	 }
 	public String getDutyName() {
 		return "Number for Matrix";
   	 }
 	 public String[] getDefaultModule() {
 	 	return new String[] {"#NumTaxaWithDataInMatrix", "#NumSelTaxaWithDataInMatrix"};
 	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/matrixNumber.gif";
   	 }
  	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(MCharactersDistribution data);
   	
	public abstract void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString); //TODO: pass result
	/*===== For NumberForItem interface ======*/
 	public boolean returnsMultipleValues(){
  		return false;
  	}
  	public void initialize(Object object1, Object object2){
		if (object1 instanceof MCharactersDistribution) 
   			initialize((MCharactersDistribution)object1);
   	}
	public  void calculateNumber(Object object1, Object object2, MesquiteNumber result, MesquiteString resultString){
		clearResultAndLastResult(result);
		if (result==null)
			return;
		if (object1 instanceof MCharactersDistribution) {
			calculateNumber((MCharactersDistribution)object1, result, resultString);
			saveLastResult(result);
			saveLastResultString(resultString);
		}
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


