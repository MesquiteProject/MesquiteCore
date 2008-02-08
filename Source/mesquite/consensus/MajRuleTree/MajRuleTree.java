/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison.
Version 2.01, December 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.consensus.MajRuleTree;


import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.consensus.lib.*;


/* ======================================================================== */
/** Does majority rule consensus .*/

public class MajRuleTree extends BasicTreeConsenser   {
	double frequencyLimit = 0.5;
	MesquiteBoolean useWeights = new MesquiteBoolean(true);
	
	public String getName() {
		return "Majority Rules Consensus";
	}
	public String getExplanation() {
		return "Calculates the majority rules consensus tree." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		bipartitions = new BipartitionVector();
		return true;
	}
	public void addTree(Tree t){
		if (useWeights.getValue()) {
			MesquiteDouble md = (MesquiteDouble)((Attachable)t).getAttachment(TreesManager.WEIGHT);
			if (md != null) {
				if (md.isCombinable())
					bipartitions.setWeight(md.getValue());
				else
					bipartitions.setWeight(1.0);
			} else
				bipartitions.setWeight(1.0);

		}
		bipartitions.addTree(t);
	}
	/*.................................................................................................................*/
	public void initialize() {
		if (bipartitions!=null) {
			bipartitions.setMode(BipartitionVector.MAJRULEMODE);
		}
	}

	public Tree getConsensus(){
		Tree t = bipartitions.makeTree(consensusFrequencyLimit());
		return t;
	}
	/*.................................................................................................................*/
	public boolean useWeights() {
		return useWeights.getValue();
	}

	/*.................................................................................................................*/
	public double consensusFrequencyLimit() {
		return frequencyLimit;
	}

}