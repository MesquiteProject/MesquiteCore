/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.search.LikelihoodTreeSearch;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.search.lib.*;

/* ======================================================================== */
public class LikelihoodTreeSearch extends TreeSearch  {
	public String getName() {
		return "Likelihood Tree Search";
	}
	public String getExplanation() {
		return "Supplies trees resulting from a search for maximum likelihood trees.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSearcher.class, getName() + "  needs a method to search for trees.",
		"The method to search for trees can be selected initially");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			searchTask = (TreeSearcher)hireNamedEmployee(TreeSearcher.class,arguments);
			if (searchTask==null) {
				return sorry(getName() + " couldn't start because the requested tree searching module not obtained");
			}
		}
		else {
			searchTask= (TreeSearcher)hireEmployee(TreeSearcher.class, "Tree Searcher");
			if (searchTask==null) return sorry(getName() + " couldn't start because tree searching module not obtained.");
		}
		return true;
	}


  

	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return TreeSearcher.class;  
	}

}

