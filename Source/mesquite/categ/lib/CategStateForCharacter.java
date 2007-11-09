package mesquite.categ.lib;

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

	public abstract void calculateState(CategoricalData data, int ic,  MesquiteTable table, CategoricalState resultState, MesquiteString resultString);

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
