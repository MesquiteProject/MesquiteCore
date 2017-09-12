/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.search.OtherTreeSearch;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.duties.CharStatesForNodes;
import mesquite.lib.duties.TreeSearcher;
import mesquite.search.lib.*;

/* ======================================================================== */
public class OtherTreeSearch extends TreeSearch  {
	public String getName() {
		return "Other Tree Inference";
	}
	public String getNameForMenuItem() {
			return "Other Tree Inference";
	}
	public String getExplanation() {
		return "Supplies trees resulting from other criteria.";
	}
	/*.................................................................................................................*/
	public boolean isReconnectable(){
		return false;
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return TreeSearcher.class;  
	}
	/*.................................................................................................................*/
	public Class[] getDontHireSubchoice(){
		return new Class[]{LikelihoodAnalysis.class, DistanceAnalysis.class, ParsimonyAnalysis.class, BayesianAnalysis.class};  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
			return false;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new OTSCompatibilityTest();
		}
		
	//This is treated as compatible only with installations in which there ARE Zephyr 2 style modules, in which case this menu behaves in the new style
	 class OTSCompatibilityTest extends CompatibilityTest {
		public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
			return (numModulesAvailable(LikelihoodAnalysis.class) + numModulesAvailable(DistanceAnalysis.class) + numModulesAvailable(ParsimonyAnalysis.class) + numModulesAvailable(BayesianAnalysis.class)>1);
		}
	}

}

