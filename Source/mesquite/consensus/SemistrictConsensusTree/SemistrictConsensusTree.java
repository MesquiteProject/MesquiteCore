/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.consensus.SemistrictConsensusTree;

import mesquite.consensus.lib.*;
import mesquite.lib.*;

	/* ======================================================================== */
	/** Does semistrict consensus .*/

	public class SemistrictConsensusTree extends BasicTreeConsenser   {
		public String getName() {
			return "Semistrict Consensus";
		}
		
		public String getExplanation() {
			return "Calculates the semistrict consensus tree." ;
		}
	  	public void addTree(Tree t){
	  		if (t==null)
	  			return;
			bipartitions.addTree(t);
		}
	  	
		/*.................................................................................................................*/
	 	public void initialize() {
			bipartitions.initialize();
	 		if (bipartitions!=null)
	 			bipartitions.setMode(BipartitionVector.SEMISTRICTMODE);
	 	}

	 	public Tree getConsensus(){
			Tree t = bipartitions.makeTree();
			return t;
		}
		/*.................................................................................................................*/
		public boolean requestPrimaryChoice(){
			return true;  
		}

		/*.................................................................................................................*/
	   	public boolean isPrerelease(){
	   		return false;  
	   	}
		/*.................................................................................................................*/
		public boolean isSubstantive(){
			return true;
		}   	 
		/*.................................................................................................................*/
		public int getVersionOfFirstRelease(){
			return 250;  
		}

	}
