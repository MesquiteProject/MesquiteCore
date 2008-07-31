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
		public boolean displayTrueAsDark(){
			return true;
		}
		
		/*.................................................................................................................*/
		public String getValueString(boolean on){
			if (on)
				return getTrueString();
			else
				return getFalseString();
		}

		/** Returns the text to be used to describe the condition of this boolean being true. */
		/*.................................................................................................................*/
		public String getTrueString(){
			return "True";
		}

		/** Returns the text to be used to describe the condition of this boolean being false. */
/*.................................................................................................................*/
		public String getFalseString(){
			return "False";
		}


	 	/*.................................................................................................................*/
	 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	 	public int getVersionOfFirstRelease(){
			return 201;  
	 	}
}
