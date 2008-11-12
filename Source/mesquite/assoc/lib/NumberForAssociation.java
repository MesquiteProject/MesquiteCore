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
