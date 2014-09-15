/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.lib;

import mesquite.lib.*;
import mesquite.assoc.lib.*;
import mesquite.lib.duties.ItemsSource;
import mesquite.lib.duties.NumberForItem;

public abstract class NumberForAssociation extends MesquiteModule implements NumberForItem {
	TaxaAssociation currentAssociation=null;

	public TaxaAssociation getCurrentAssociation() {
		return currentAssociation;
	}

	public void setCurrentAssociation(TaxaAssociation currentAssociation) {
		this.currentAssociation = currentAssociation;
	}

	public Class getDutyClass() {
		return NumberForAssociation.class;
	}

	public String getDutyName() {
		return "Number for Association";
	}

	public abstract void calculateNumber(TaxaAssociation association, MesquiteNumber result, MesquiteString resultString); 
	
  	public boolean returnsMultipleValues(){
  		return false;
  	}
  	
  	public void initialize(){
  	}

	/*===== For NumberForItem interface ======*/
   	public void initialize(Object object1, Object object2){
   	}
   	
	public  void calculateNumber(Object object1, Object object2, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (object1 instanceof TaxaAssociation) {
			calculateNumber((TaxaAssociation)object1,result, resultString);
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
	/** indicates default optimization, e.g. for association searchers.  If true, association search will maximize,
	otherwise minimize.  If number has obvious optimum direction, this should be overridden to indicate
	optimum*/
	public boolean biggerIsBetter() {
		return true;
	}

}
