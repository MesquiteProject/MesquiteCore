/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison. 
 This module copyright 2006 P. Midford and W. Maddison

Version 1.1, May 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.correl.QuadratsRealizationCounter;



import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class QuadratsRealizationCounter extends NumFor2CharHistAndTree {



	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		return true;
	} 

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		//temp.addLine("setSeed " + originalSeed); 
		
		return temp;
	}
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(this.getClass(), "XXXXXXXXXX", "[long integer seed]", commandName, "XXXXXXXXXX")) {
			
		}
		else if (checker.compare(this.getClass(), "XXXXXXXXXX", "[number]", commandName, "XXXXXXXXXX")) {
			
		}
		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}


    


	public  void calculateNumber(Tree tree, CharacterHistory history1, CharacterHistory history2, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec){
		if (result == null)
			return;
		result.setToUnassigned();
		if (tree == null || history1 == null || history2 == null)
			return;
		if (!(history1 instanceof CategoricalHistory) || !(history2 instanceof CategoricalHistory)){
			if (resultString != null)
				resultString.setValue("Quadrats counting can't be done because one or both of the character are not categorical");
			return;
		}
	    
		//examine histories here
		
		
		
		result.setValue(2006);
		if (resultString!=null)
			resultString.setValue("Testing 1..2..3");
	}

	/*.................................................................................................................*/




	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getName() {
		return "Quadrats Correlation";
	}

	public String getExplanation(){
		return "Counts correlation in two realizations using the quadrats method";
	}

	public boolean isPrerelease(){
		return true;
	}

}


