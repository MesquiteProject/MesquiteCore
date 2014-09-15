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


/* ======================================================================== */
/** Manages spec sets.*/

public abstract class SpecsSetManager extends FileInit implements ElementManager   {
	/*.................................................................................................................*/
	public boolean getSearchableAsModule(){
		return false;
	}

   	 public Class getDutyClass() {
   	 	return SpecsSetManager.class;
   	 }
 	public String getDutyName() {
 		return "Manager of specification sets";
   	 }
	/*.................................................................................................................*/
	public MesquiteModule showElement(FileElement e){
		if (e != null)
			alert("Sorry, the " + e.getTypeName() + "  cannot be shown by this means yet.");
		return null;
	}
	/*.................................................................................................................*/
	public void deleteElement(FileElement e){
		if (e instanceof SpecsSet){
			if (getElementClass()!=null && getElementClass().isAssignableFrom(e.getClass())) {
				SpecsSet t = (SpecsSet)e;
				AssociableWithSpecs aws = t.getParent();
				if (aws !=null)
					aws.removeSpecsSet(t,getElementClass());
			}
		}
	}
   	public boolean isSubstantive(){
   		return false;  
   	}
  	 public ManagerAssistant showSpecsSets(Object obj, String listerName){
    	 		//Check to see if already has lister for this
    	 		boolean found = false;
			for (int i = 0; i<getNumberOfEmployees(); i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ManagerAssistant)
					if (((ManagerAssistant)e).showing(obj)) {
						((ManagerAssistant)e).getModuleWindow().setVisible(true);
						return ((ManagerAssistant)e);
					}
			}
			ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize(listerName));
	 			if (lister!=null) {
	 				lister.showListWindow(obj);
    	 			if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
    	 				lister.getModuleWindow().setVisible(true);
    	 		}
	 		return lister;
    	 		
  	 }
}


