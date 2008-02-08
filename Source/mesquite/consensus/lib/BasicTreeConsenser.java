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
package mesquite.consensus.lib;


import mesquite.lib.duties.*;
import mesquite.lib.*;


public abstract class BasicTreeConsenser extends IncrementalConsenser   {
	protected BipartitionVector bipartitions=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		bipartitions = new BipartitionVector();
		return true;
	}
	/*.................................................................................................................*/
  	public void reset(Taxa taxa){
  		if (bipartitions==null)
  			bipartitions = new BipartitionVector();
  		else
  			bipartitions.removeAllElements();		// clean bipartition table
		bipartitions.setTaxa(taxa);
		initialize();
	}
  	public abstract void addTree(Tree t);
  	
 	public abstract Tree getConsensus();
	/*.................................................................................................................*/
 	public void initialize() {
 	}

	/*.................................................................................................................*/
	//ASSUMES TREES HAVE ALL THE SAME TAXA
	/*.................................................................................................................*/
	public Tree consense(Trees list){
		MesquiteTimer timer = new MesquiteTimer();
		timer.start();
		Taxa taxa = list.getTaxa();
		
		reset(taxa);
		for (int iTree = 0; iTree < list.size(); iTree++){
			addTree(list.getTree(iTree));
		}
		Tree t = getConsensus();
		double time = 1.0*timer.timeSinceLast()/1000.0;
		bipartitions.dump();
		logln("" + list.size() + " trees processed in " + time + " seconds");
		return t;
	}

}

