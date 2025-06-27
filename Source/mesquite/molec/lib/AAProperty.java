/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.lib; 

import mesquite.categ.lib.ProteinState;
import mesquite.categ.lib.RequiresProteinData;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteModule;


/* ======================================================================== */
public abstract class AAProperty extends MesquiteModule {
   	 public Class getDutyClass() {
   	 	return AAProperty.class;
   	 }
 	public String getDutyName() {
 		return "Amino Acid Property";
   	 }
	/*.................................................................................................................*/
	public double getMinimumValue(){
		double m = MesquiteDouble.unassigned;
		for (int is = 0; is<=ProteinState.maxProteinState; is++) { //-5 for * 1 2 3 4 ?
			m = MesquiteDouble.minimum(m, getProperty(is));
		}
		return m;
	}
	/*.................................................................................................................*/
	public double getMaximumValue(){
		double m = MesquiteDouble.unassigned;
		for (int is = 0; is<=ProteinState.maxProteinState; is++) { //-5 for * 1 2 3 4
			m = MesquiteDouble.maximum(m, getProperty(is));
		}
		return m;
	}
	/*.................................................................................................................*/
	public abstract double getProperty(int aa);
	
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresProteinData();
	}
}


	


