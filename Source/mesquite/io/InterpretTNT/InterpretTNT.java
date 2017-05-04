/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretTNT;

import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.ProteinState;
import mesquite.cont.lib.ContinuousState;
import mesquite.io.lib.*;

public class InterpretTNT extends InterpretHennig86Base {


	/*.................................................................................................................*/
	public String getName() {
		return "TNT";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports and exports TNT files." ;
	}
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "tnt";
	}
	/*.................................................................................................................*/
	public boolean isTNT() {
		return true;
	}
	/*.................................................................................................................*/
	public Class[] getAcceptedClasses() {
		return new Class[] {CategoricalState.class,ProteinState.class, DNAState.class, ContinuousState.class};
	}

	/*.................................................................................................................*/
	public boolean additiveIsDefault() {  
		return false;
	}


}




