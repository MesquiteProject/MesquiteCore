/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.pairwise.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/***/

public abstract class PairMaker extends MesquiteModule  {
	protected int limit = MesquiteInteger.unassigned;
	protected boolean limitSet = false;
	static final int QUERYTRIGGER = 5;
	
	public Class getDutyClass(){
		return PairMaker.class;
	}
 	public String getDutyName() {
 		return "Taxa Pair Maker";
   	 }
 	public abstract TaxaPairer getPairer();
 	
	/*.................................................................................................................*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Max. Number of Pairings...", makeCommand("setLimit", this));
 		return true;
 	}
	/*.................................................................................................................*/
  	public void setLimit(int limit){
  		this.limit = limit;
  		limitSet = !MesquiteInteger.isUnassigned(limit);
  	}
	/*.................................................................................................................*/
  	public int limitCheckOK(int count){  
		if (limitSet){
			return limit;
		}
		if (count>=QUERYTRIGGER){
			if (MesquiteThread.isScripting()){
				setLimit(QUERYTRIGGER);
				return QUERYTRIGGER;
			}
			else {
				int L = MesquiteInteger.queryInteger(containerOfModule(), "Number of pairs?", "The calculation has found " + QUERYTRIGGER + " pairings so far.  To set a limit on the number of pairings examined, enter it here; otherwise hit Cancel to continue until all pairings counted.", QUERYTRIGGER, 1, MesquiteInteger.infinite);
				if (MesquiteInteger.isUnassigned(L)) 
					L = MesquiteInteger.infinite;
				setLimit(L);
			}
			return limit;
		}
  		return MesquiteInteger.infinite;
  	}
  	
  	public boolean limitReached(int count){  
			return limitSet && MesquiteInteger.isCombinable(limit) && count>= limit;
  	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
		if (MesquiteInteger.isCombinable(limit))
			temp.addLine("setLimit " + limit); 
  	 	return temp;
  	 }
  	 
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(),  "Sets the limit of number of pairs to examine", "[limit]", commandName, "setLimit")) {
			int L= MesquiteInteger.fromFirstToken(arguments, pos);
			int lim = limit;
			if (MesquiteInteger.isUnassigned(limit))
				lim = QUERYTRIGGER;
			if (!MesquiteInteger.isCombinable(L))
				L = MesquiteInteger.queryInteger(containerOfModule(), "Number of pairs?", "Indicate the maximum number of pairings examined:", lim, 1, MesquiteInteger.infinite);
			
			if (MesquiteInteger.isCombinable(L)) {
				setLimit(L);
				if (!MesquiteThread.isScripting())
					parametersChanged();
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
}



