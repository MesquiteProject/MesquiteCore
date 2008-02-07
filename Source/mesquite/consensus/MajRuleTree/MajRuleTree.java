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
/** Does tree consensus .*/

public class MajRuleTree extends IncrementalConsenser   {
	BipartitionVector bipartitions;
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
	/*.................................................................................................................*/
  	public void reset(Taxa taxa){
		bipartitions.removeAllElements();
		bipartitions.setTaxa(taxa);
	}
  	public void addTree(Tree t){
		bipartitions.addTree(t);
	}
 	public Tree getConsensus(){
		Tree t = bipartitions.makeTree(consensusFrequencyLimit());
		return t;
	}
	/*.................................................................................................................*/
 	public double consensusFrequencyLimit() {
 		return 0.5;
 	}

	/*.................................................................................................................*/
	//ASSUMES TREES HAVE ALL THE SAME TAXA
	/*.................................................................................................................*/
	public Tree consense(Trees list){
		MesquiteTimer timer = new MesquiteTimer();
		timer.start();
		Taxa taxa = list.getTaxa();
		// clean bipartition table
		bipartitions.removeAllElements();
		bipartitions.setTaxa(taxa);
		for (int iTree = 0; iTree < list.size(); iTree++){
			bipartitions.addTree(list.getTree(iTree));
		}
		bipartitions.dump();
		
		//First, go through the trees accumulating the bipartition table
		Tree t = bipartitions.makeTree(consensusFrequencyLimit());
		double time = 1.0*timer.timeSinceLast()/1000.0;
		logln("" + list.size() + " trees processed in " + time + " seconds");
		return t;
	}

}

