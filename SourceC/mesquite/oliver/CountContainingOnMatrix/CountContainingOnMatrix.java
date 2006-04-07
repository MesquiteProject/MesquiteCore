package mesquite.oliver.CountContainingOnMatrix;

import mesquite.assoc.lib.*;
import mesquite.oliver.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;
import mesquite.parsimony.lib.CharacterSteps;

/** Counts the number of containing associates are associated with a data matrix
    uses the NumberForMatrixAndAssociation class, which provides a data matrix
    and association*/
public class CountContainingOnMatrix extends NumberForMatrixAndAssociation{
	
	public void initialize(MCharactersDistribution matrix, TaxaAssociation association, CommandRecord commandRec) {
	}
	
	public void calculateNumber(MCharactersDistribution matrix, TaxaAssociation association, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {

		if(matrix != null){
			int numberContaining = 0;
			Taxa containedTaxa = matrix.getTaxa();
			Taxa containingTaxa = association.getOtherTaxa(containedTaxa);
			ING: for (int i = 0; i < containingTaxa.getNumTaxa(); i++){
				ED: for (int j = 0; j < containedTaxa.getNumTaxa(); j++){
					if (association.getAssociation(containingTaxa.getTaxon(i), containedTaxa.getTaxon(j))){
						numberContaining++;
						continue ING;
					}
				}
			}
			result.setValue((double)numberContaining);
			if(resultString!=null)
				resultString.setValue("Number of containing taxa associated with current matrix = " + result.toString());
		}
	}
	public String getName() {
		return "Count Number of Containing Taxa";
	}
	public boolean requestPrimaryChoice(){
		return true;
	}
	public String getExplanation(){
		return "Counts the number of containing taxa associated with a character matrix.  An example module of the abstract NumberForMatrixAndAssociation class.";
	}

	/*===== For NumberForMatrix interface ======*/
	// public void initialize(MCharactersDistribution data, CommandRecord commandRec) {}

	// public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {}



}
