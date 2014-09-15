/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.AddCharacter;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.table.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class AddCharacter extends ContDataAlterer {
	double scalingFactor = 1.0;
	CharSourceCoordObed characterSourceTask;
	int addingIC = 0;
	ContinuousDistribution addingCharacter;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		String exp = "Source of character to add";
		characterSourceTask = (CharSourceCoordObed)hireCompatibleEmployee(CharSourceCoordObed.class, ContinuousState.class, exp);
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");

		return true;
	}
		
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
		if (!(data instanceof ContinuousData))
			return false;
 			Taxa taxa = data.getTaxa();
   			addingIC=characterSourceTask.queryUserChoose(taxa, "from which to add states");
   			addingCharacter = (ContinuousDistribution)characterSourceTask.getCharacter(taxa, addingIC); 
			boolean did=false;
			return alterContentOfCells(data,table, undoReference);
   	}

	/*.................................................................................................................*/
   	public void alterCell(CharacterData ddata, int ic, int it){
   		if (addingCharacter == null)
   			return;
		ContinuousData data = (ContinuousData)ddata;
		
		double factor = addingCharacter.getState(it, 0);
		for (int item = 0; item<data.getNumItems(); item++){
			double state = data.getState(ic,it, item);
			if (MesquiteDouble.isCombinable(state) && MesquiteDouble.isCombinable(factor))
				data.setState(ic,it, item, state+factor);
		}
	}
	
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  //put release version
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Add Character";
   	 }
    		/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Add Character...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Alters continuous data by adding a continuous character from a source of characters.  All items of the matrix are similarly modified." ;
   	 }
   	 
}


