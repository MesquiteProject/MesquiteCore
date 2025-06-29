/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.lib;

/* ======================================================================== */
/**Reconstructs the ancestral states for a character using likelihood, for a specific character model.
Also calculates likelihood by virtue of extending MargLikelihoodForModel.  Can be set to report only clade local values*/

public abstract class MargLikeAncStCLForModel extends MargLikeAncStForModel  {

   	 public Class getDutyClass() {
   	 	return MargLikeAncStCLForModel.class;
   	 }
 	public String getDutyName() {
 		return "Maximum Likelihood ancestral states (marginal reconstruction) with clade-local ability";
   	 }

	public abstract void setReportCladeLocalValues(boolean reportCladeValues);
	public abstract boolean getReportCladeLocalValues();
	//public abstract void setRootMode(int rootMode);
	//public abstract int getRootMode();
	

}



