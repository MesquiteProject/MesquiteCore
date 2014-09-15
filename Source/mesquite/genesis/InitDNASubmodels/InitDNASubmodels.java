/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.InitDNASubmodels;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;

/* ======================================================================== */
public class InitDNASubmodels extends FileInit {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	
	/*.................................................................................................................*/
  	 public void projectEstablished(){
  	 	super.projectEstablished();
		StateFreqDNAEqual model = new StateFreqDNAEqual(null);
    		model.addToFile(null, getProject(), null);
    		//model.setName("equal frequencies");
    		model.setBuiltIn(true);
		CategoricalData.registerDefaultModel(model.getParadigm(), model.getName());
		
		StateFreqDNAEmpir empiricalModel = new StateFreqDNAEmpir(null);
    		empiricalModel.addToFile(null, getProject(), null);
    		//empiricalModel.setName("empirical frequencies");
    		empiricalModel.setBuiltIn(true);
		
		RateMatrixEqualDNAModel rModel = new RateMatrixEqualDNAModel(null);
    		rModel.addToFile(null, getProject(), null);
    		//model.setName("one parameter");
    		rModel.setBuiltIn(true);
		CategoricalData.registerDefaultModel(rModel.getParadigm(), rModel.getName());
		CharRatesModel charRates = new CharRatesEqual();
    		charRates.addToFile(null, getProject(), null);
    		//charRates.setName("equal rates");
		CategoricalData.registerDefaultModel("CharRates", charRates.getName());
  	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Initialize DNA Submodels";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Initializes default DNA submodels." ;
   	 }
	/*.................................................................................................................*/
   	 
}

