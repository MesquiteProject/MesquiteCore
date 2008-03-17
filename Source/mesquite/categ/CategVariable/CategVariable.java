package mesquite.categ.CategVariable;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.ParsimonyModelSet;
import mesquite.categ.lib.*;

public class CategVariable extends BooleanForCharacter {

	public String getName() {
		return "Variable (Mult. States)";
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public int charIsVariable(CharacterData data, int ic) {
		CategoricalData cData = (CategoricalData)data;
		long intersection = CategoricalState.statesBitsMask;
		boolean anySel = cData.getTaxa().anySelected();
		for (int it=0; it<cData.getNumTaxa(); it++){
			if (!anySel || cData.getTaxa().getSelected(it)){
				long state = cData.getState(ic, it);
				if (CategoricalState.isCombinable(state)){
					if (CategoricalState.cardinality(state)>1){ //polymorphic or uncertain
						if (CategoricalState.isUncertain(state)){ //uncertain; ok if overlaps
							intersection &= state;
							if (intersection == 0L)
								return 1;
						}
						else
							return 1;
					}
					else {
						intersection &= state;
						if (intersection == 0L)
							return 1;
					}
				}
			}
		}
		return 0;
	}


	public void calculateBoolean(CharacterData data, int ic, MesquiteBoolean result, MesquiteString resultString) {
		if (data==null || result==null)
			return;
		if (!(data instanceof CategoricalData))
			return;
		Taxa taxa = data.getTaxa();
		int inform = charIsVariable(data,ic);

		if (inform==0)
			result.setValue(false);
		else if (inform==1)
			result.setValue(true);
		else
			result.setToUnassigned();
	}

	public boolean isPrerelease() {
		return true;
	}


}
