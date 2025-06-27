/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.meristic.InitMeristicParsimony;
/*~~  */

import mesquite.lib.duties.FileInit;
import mesquite.meristic.lib.MeristicData;
import mesquite.meristic.lib.MeristicLinearParsModel;

/**/
public class InitMeristicParsimony extends FileInit {
	public String getName() {
		return "Initialize predefined meristic parsimony models";
	}
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Initializes the predefined meristic parsimony models." ;
	}
	/*.................................................................................................................*/
	MeristicLinearParsModel linearModel;

	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		linearModel = new MeristicLinearParsModel("Linear Meristic");
		MeristicData.registerDefaultModel("Parsimony", "Linear Meristic");
		return true;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been established but not yet read in.*/
	public void projectEstablished() {
		linearModel.addToFile(null, getProject(), null);
		super.projectEstablished();
	}

	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return false;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 272;  
	}
}



