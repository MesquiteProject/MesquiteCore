package mesquite.oliver.ThetaS;

import mesquite.assoc.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;

/**Calculates the estimate of Theta for a set of containing Taxa using pairwise nucleotide differences.*/
public class ThetaS extends NumberForTaxon {
	AssociationSource associationTask;
	TaxaAssociation association;
	Taxa containedTaxa, containingTaxa;
	CharMatrixSource matrixTask;
	MCharactersDistribution data;
	Taxon[] containedTaxons;

	public boolean startJob(String arguments, Object condition,	CommandRecord commandRec, boolean hiredByName) {
		associationTask = (AssociationSource)hireEmployee(commandRec, AssociationSource.class, "Source of taxon associations");
		if (associationTask ==null)
			return sorry(commandRec, getName() + " couldn't start because no source of taxon associations obtained.");
		matrixTask = (CharMatrixSource)hireEmployee(commandRec, CharMatrixSource.class, "Source of Character Matricies (Must be DNA data)");
		if (matrixTask == null)
			return sorry(commandRec, getName() + " couldn't start because no source of character matrices obtained.");
		return true;
	}

	public void initialize(Taxa taxa, CommandRecord commandRec) {
		if(taxa != null){
			containingTaxa = taxa;
			if (association == null || (association.getTaxa(1) != containedTaxa && association.getTaxa(0) != containedTaxa)) {
				association = associationTask.getCurrentAssociation(containedTaxa, commandRec);
				if (association.getTaxa(0) == containedTaxa){
					containingTaxa = association.getTaxa(1);
					Debugg.println("Containing taxa corresponds to "+containingTaxa.getName());
				}
				else {
					containingTaxa = association.getTaxa(0);
					Debugg.println("Containing taxa corresponds to "+containingTaxa.getName());
				}
			}
			if (data == null){
				data = matrixTask.getCurrentMatrix(taxa, commandRec);
			}
		}		
	}

	/* elements of numVariable method could be used in calculating theta(S)*/
	private int numVariable (MCharactersDistribution data){
		CharacterState csi = null;	//These two variables will hold the data for comparisons
		CharacterState csj = null;
		int numVar=0;
		int numTaxa = data.getNumTaxa();
		boolean[] isVariable = new boolean[data.getNumChars()]; 
		for (int initChars = 0; initChars < data.getNumChars(); initChars++){
			isVariable[initChars] = false;
		}
		for (int i = 0; i < numTaxa; i++){
			for (int j = 0; j < i; j++){
				charLoop: for (int chars = 0; chars < data.getNumChars(); chars++) {
					csi = data.getCharacterState(csi, chars, containedTaxons[i].getNumber());
					csj = data.getCharacterState(csj, chars, containedTaxons[j].getNumber());
					if (!csi.equals(csj))
						isVariable[chars] = true;
				} // inner (char) loop - over all characters
			}
		}
		
		for (int sumVariable = 0; sumVariable < data.getNumChars(); sumVariable++){
			if (isVariable[sumVariable] == true)
				numVar++;
		}
		return numVar;
	}

	
	/** Returns the nucleotide diversity of a single containing taxon */
	public double calcThetaS(MCharactersDistribution data, int nContained) {
		double thetaSHolder = 0;
		double unbiasCorrector = 0;
		if (data != null){

			for (int i = 1; i <= nContained; i++){
				unbiasCorrector += 1/i;
			}
			
			thetaSHolder = numVariable(data) / unbiasCorrector;
		}
		return thetaSHolder;
				
	} // end of meanPi method
	
	
	public void calculateNumber(Taxon taxon, MesquiteNumber result,	MesquiteString resultString, CommandRecord commandRec) {
		Taxa taxa = taxon.getTaxa();
		if (result == null)
			return;
		// The following lines also appear in the initialize method, but as it is never called, I have placed them here as well...
		if(taxa != null){
			containingTaxa = taxa;
			if (association == null || (association.getTaxa(1) != containingTaxa && association.getTaxa(0) != containingTaxa)) {
				association = associationTask.getCurrentAssociation(containingTaxa, commandRec);
				if (association.getTaxa(0) == containingTaxa){
					containedTaxa = association.getTaxa(1);
					Debugg.println("Containing taxa corresponds to "+containingTaxa.getName());
				}
				else {
					containedTaxa = association.getTaxa(0);
					Debugg.println("Containing taxa corresponds to "+containingTaxa.getName());
				}
			}
			if (data == null){
				data = matrixTask.getCurrentMatrix(containedTaxa, commandRec);
				Debugg.println("Calculations will use "+data.getName());
			}
			Debugg.println("Calculating for "+taxon.getName()+" ("+association.getNumAssociates(taxon)+" taxa)");
			if(association.getNumAssociates(taxon) > 1){
				Taxon[] tempContained = new Taxon[association.getNumAssociates(taxon)];
				containedTaxons = tempContained;
				containedTaxons = association.getAssociates(taxon);
				// Debugg.println(association.getNumAssociates(taxon)+" taxa in "+taxon.getName());
				result.setValue(calcThetaS(data, association.getNumAssociates(taxon)));
				if (resultString != null)
					resultString.setValue("Theta(S) "+taxon.getName()+" = "+result.toString());
			} else {
				result.setValue(0);
				if (resultString != null)
					resultString.setValue("Theta(S) "+taxon.getName()+" is N/A (more than one contained taxon needed for calculations)");
			}
		}		
	}
	
	public String getName() {
		return "Theta(S) by Taxon";
	}
	public String getExplanation(){
		return "Calculates the estimate of Theta based on number of segregating sites (Theta[S]) for each containing taxon.";
	}
}
