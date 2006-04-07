package mesquite.oliver.ThetaPi;
 
import mesquite.assoc.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;

/**Calculates the estimate of Theta for a set of containing Taxa using pairwise nucleotide differences.*/
public class ThetaPi extends NumberForTaxon {
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

	/** Returns the nucleotide diversity of a single containing taxon */
	public double calcThetaPi(MCharactersDistribution data, int nContained) {
		CharacterState csj = null; // These two CharacterState variables will hold data for comparisons
		CharacterState csi = null;
		double k = 0; // This variable keeps a running tally of the number of differences between selected sequences.
		double thetaPiHolder = 0;
		if (data != null){
			// int numTaxa = data.getNumTaxa();
			
			/* This checks to see if a particular character is variable.  If not, it will not 
			 be used in comparisons (within charactersLoop below).*/ 
			boolean[] isVariable = new boolean[data.getNumChars()]; 
			for (int initChars = 0; initChars < data.getNumChars(); initChars++)
				isVariable[initChars] = false;
			for (int i = 0; i < nContained; i++){
				for (int j = 0; j < i; j++){
					// Debugg.println("Comparing "+containedTaxons[i].getName() + " to "+containedTaxons[j].getName()+" for isVariable method.");
					charLoop: for (int chars = 0; chars < data.getNumChars(); chars++) {
						csi = data.getCharacterState(csi, chars, containedTaxons[i].getNumber());
						csj = data.getCharacterState(csj, chars, containedTaxons[j].getNumber());
						if (!csi.equals(csj))
							isVariable[chars] = true;
					} // inner (char) loop - over all characters
				}
			}
			
			referenceLoop: for (int it = 0; it < nContained; it++){
				comparisonLoop: for (int jt = 0; jt < it; jt++){
					// Debugg.println("Comparing "+containedTaxons[it].getName() + " to "+containedTaxons[jt].getName()+" for calculations.");
					charactersLoop: for(int ic=0; ic < data.getNumChars(); ic++){
						if(isVariable[ic]){
							csi = data.getCharacterState(csi, ic, containedTaxons[it].getNumber());
							csj = data.getCharacterState(csj, ic, containedTaxons[jt].getNumber());
							if(!csi.equals(csj))
								k++;
						}
					}
				}
			}
				
			thetaPiHolder = (k/(double)(nContained*(nContained - 1)/2));
		}
		return thetaPiHolder;
				
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
				result.setValue(calcThetaPi(data, association.getNumAssociates(taxon)));
				if (resultString != null)
					resultString.setValue("Theta(Pi) "+taxon.getName()+" = "+result.toString());
			} else {
				result.setValue(0);
				if (resultString != null)
					resultString.setValue("Theta(Pi) "+taxon.getName()+" is N/A (more than one contained taxon needed for calculations)");
			}
		}		
	}
	
	public String getName() {
		return "Theta(Pi) by Taxon";
	}
	public String getExplanation(){
		return "Calculates the estimate of Theta based on pairwise nucleotide differences (Theta[Pi]) for each containing taxon.";
	}
}
