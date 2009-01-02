/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.lib.duties;

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
