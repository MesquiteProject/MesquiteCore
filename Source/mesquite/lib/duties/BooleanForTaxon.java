package mesquite.lib.duties;

import mesquite.lib.*;

public abstract class BooleanForTaxon extends MesquiteModule implements BooleanForItem {

	 public Class getDutyClass() {
	   	 	return BooleanForTaxon.class;
	   	 }
	 	public String getDutyName() {
	 		return "Boolean for Taxon";
	   	 }

	   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	   	public void initialize(Taxa taxa){}

		public abstract void calculateBoolean(Taxa taxa, int i, MesquiteBoolean result, MesquiteString resultString); 
		
		/*===== For BooleanForItem interface ======*/
	   	public void initialize(Object object1, Object object2){
			if (object1 instanceof Taxa) 
	   			initialize((Taxa)object1);
	   	}
		public  void calculateBoolean(Object object1, Object object2, int i, MesquiteBoolean result, MesquiteString resultString){
			if (result==null)
				return;
			if (object1 instanceof Taxa && MesquiteInteger.isCombinable(i)) {
				calculateBoolean((Taxa)object1, i, result, resultString);
			}
		}
	   	public String getNameOfValueCalculated(){ 
			return getNameAndParameters();
	   	}
		/*.................................................................................................................*/
		public boolean isPrerelease(){
			return false;
		}
	}
