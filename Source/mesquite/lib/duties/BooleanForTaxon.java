/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.72, December 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.lib.duties;

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
	   	public String getNameOfValueCalculated(){ 
			return getNameAndParameters();
	   	}
		/*.................................................................................................................*/
		public boolean isPrerelease(){
			return false;
		}
	}
