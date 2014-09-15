/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.categ.lib;

import mesquite.lib.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;

public abstract class CategStateForCharacter extends MesquiteModule {
	boolean selectedOnly=true;

	public Class getDutyClass() {
		return CategStateForCharacter.class;
	}

	public String getDutyName() {
 		return "Assigns a single character state for a categorical character; e.g., for consensus sequences.";
   	 }

	public abstract void calculateState(CategoricalData data, int ic,  MesquiteTable table, CategoricalState resultState, MesquiteString resultString, MesquiteDouble fractionMatching);

	/*.................................................................................................................*/
	 public String getShortParameters() {
		 return "";
	 }

	public boolean getSelectedOnly() {
		return selectedOnly;
	}

	public void setSelectedOnly(boolean selectedOnly) {
		this.selectedOnly = selectedOnly;
	}

	
}
