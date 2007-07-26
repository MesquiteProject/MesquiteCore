package mesquite.lib.duties;

import mesquite.lib.*;
import mesquite.lib.characters.*;


public abstract class BooleanForCharacter extends MesquiteModule implements BooleanForItem {

	 public Class getDutyClass() {
	   	 	return BooleanForCharacter.class;
	   	 }
	 	public String getDutyName() {
	 		return "Boolean for Character";
	   	 }

	   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	   	public void initialize(CharacterData data){}

		public abstract void calculateBoolean(CharacterData data, int ic, MesquiteBoolean result, MesquiteString resultString); 
		
		/*===== For BooleanForItem interface ======*/
	   	public void initialize(Object object1, Object object2){
			if (object1 instanceof CharacterData) 
	   			initialize((CharacterData)object1);
	   	}
		public  void calculateBoolean(Object object1, Object object2, int i, MesquiteBoolean result, MesquiteString resultString){
			if (result==null)
				return;
			if (object1 instanceof CharacterData && MesquiteInteger.isCombinable(i)){
				calculateBoolean((CharacterData)object1, i, result, resultString);
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
