/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import java.awt.*;
import mesquite.lib.*;

/*Last documented:  May 2000 */
/* ======================================================================== */
public class ModelCompatibilityInfo implements CompatibilityChecker {
	public Class targetStateClass; //so model can check that it can handle this type of data
	public Class targetModelSubclass;  //so model can check that it is correct subclass
	public ModelCompatibilityInfo(Class targetModelSubclass, Class targetStateClass){
		this.targetModelSubclass=targetModelSubclass;
		this.targetStateClass = targetStateClass;
	}
	 //obj to be passed here is model, so that requester of model can check for compatibility as well as vice versa; added Apr 02
 	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
 		return true;
 	}
	 //obj to be passed here is model, so that requester of model can check for compatibility as well as vice versa; added Apr 02
 	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report){
 		return true;
 	}

}



