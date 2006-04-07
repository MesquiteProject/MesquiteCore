package mesquite.oliver.tests.PstPairwise;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/**This class calculates the statistic Pst for a pair of containing taxa.*/
public class PstPairwise extends NumberForMatrix{
	AssociationSource associationTask;
	TaxaAssociation association;
	Taxa containedTaxa, containingTaxa;
	Taxon containingOne, containingTwo;
	
	/*The startJob method will select the association to use for calcualtions.*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		associationTask = (AssociationSource)hireEmployee(commandRec, AssociationSource.class, "Source of taxon associations");
		if (associationTask ==null)
			return sorry(commandRec, getName() + " couldn't start because no source of taxon associations obtained.");

		return true;
	} // end of startJob method

/*--------------------------------------------------------------------------------------------------------------------------------------------------*/	
	/**Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from happening at inopportune times (e.g., while a long chart calculation is in mid-progress).*/
	public void initialize(MCharactersDistribution data, CommandRecord commandRec) {
		if (data == null)
			return;
		containedTaxa = data.getTaxa();
		if (association == null || (association.getTaxa(0) != containedTaxa && association.getTaxa(1) != containedTaxa)) {
			association = associationTask.getCurrentAssociation(containedTaxa, commandRec);
			if (association.getTaxa(0) == containedTaxa){
				containingTaxa = association.getTaxa(1);
				logln("containing taxa corresponds to 1.");
			}
			else containingTaxa = association.getTaxa(0);

		}
	} // end of initialize method


/*--------------------------------------------------------------------------------------------------------------------------------------------------*/	

/** Returns the nucleotide diversity of a single containing taxon */
	public double meanPi(MCharactersDistribution data, int iContaining) {
		CharacterState csj = null; // These two CharacterState variables will hold data for comparisons
		CharacterState csi = null;
		int nContained = 0;
		int numTaxa = data.getNumTaxa();
		double k = 0; // This variable keeps a running tally of the number of differences between selected sequences.
		Taxon containingTaxon = containingTaxa.getTaxon(iContaining);
		
		/* The following conditional checks to see if there are multiple taxa associated with the 
		   current containing Taxon (i.e. it checks to make sure that there are multiple genes 
		   within a species).  If there are not, it moves to the next taxon in the containing group.*/
		if (association.getNumAssociates(containingTaxon)>1){
			
			boolean[] isVariable = new boolean[data.getNumChars()]; 
			for (int initChars = 0; initChars < data.getNumChars(); initChars++){
				isVariable[initChars] = false;
			}
			/* This loop checks to see if a particular character is variable.  If not, it will not 
			   be used in comparisons (within charactersLoop below).*/ 
			for (int i = 0; i < numTaxa; i++){
				for (int j = 0; j < i; j++){
					charLoop: for (int chars = 0; chars < data.getNumChars(); chars++) {
						csi = data.getCharacterState(csi, chars, i);
						csj = data.getCharacterState(csj, chars, j);
						if (!csi.equals(csj))
							isVariable[chars] = true;
					} // inner (char) loop - over all characters
				}
			}
			
			
			// referenceLoop will choose the reference sequence to use
			referenceLoop: for(int it=0; it<containedTaxa.getNumTaxa() - 1; it++){
				Taxon refContainedTaxon = containedTaxa.getTaxon(it);
				
				// This conditional checks to see if the reference sequence of the contained taxon is associated with the current containing taxon.  If not, it moves to the next sequence in the matrix.
				if (!association.getAssociation(refContainedTaxon, containingTaxon))
					continue referenceLoop;
				else {
					// Debugg.println("Comparing sequences to " + containedTaxa.getName(it));
					nContained ++;
					//comparisonLoop will choose the sequence to compare to the reference sequence
					
					comparisonLoop: for(int jt=(it+1); jt<containedTaxa.getNumTaxa(); jt++){
						Taxon compContainedTaxon = containedTaxa.getTaxon(jt);
						
						// This conditional checks to see if the comparison sequence of the contained taxon is associated with the current containing taxon (and thus, is associated with the same containing Taxon as the reference sequence).  If not, it moves to the next sequence in the matrix.
						if (!association.getAssociation(compContainedTaxon, containingTaxon))
							continue comparisonLoop;
						else {
							charactersLoop: for(int ic=0; ic < data.getNumChars(); ic++){
								csi = data.getCharacterState(csi, ic, it);
								csj = data.getCharacterState(csj, ic, jt);
								if(!csi.equals(csj))
									k++;
							} // end of charactersLoop
						} // end of conditional checking to see if the comparison sequence is associated with the containing taxon
					} // end of comparisonLoop
				} // end of conditional checking to see if the reference sequence is associated with the current containing taxon.
			} // end of referenceLoop
		} 
		
		// Debugg.println("nContained = " + nContained);
		double piHolder = (k/(double)(nContained*(nContained - 1)/2))/(double)data.getNumChars();
		
		return piHolder;
		
	} // end of meanPi method
	
/*--------------------------------------------------------------------------------------------------------------------------------------------------*/	
	/** Returns the total nucleotide diversity of the sample; in this case, the sample consists of all taxa contained within the selected pair of containing taxa.*/
	public double nucleotideDiversity(MCharactersDistribution data) {
		Taxa taxa = data.getTaxa();
		CharacterState csi = null;	//These two variables will hold the data for comparisons
		CharacterState csj = null;
		int nContaining = 0;
		int numTaxa = data.getNumTaxa();
		int k = 0; // This variable will represent the running sum of Kij
		
		if(taxa != null && taxa.getNumTaxa() > 0){

			boolean[] isVariable = new boolean[data.getNumChars()]; 
			for (int initChars = 0; initChars < data.getNumChars(); initChars++){
				isVariable[initChars] = false;
			}
			/* This loop checks to see if a particular character is variable.  If not, it will not 
			   be used in comparisons (within charactersLoop below).*/ 
			for (int i = 0; i < numTaxa; i++){
				for (int j = 0; j < i; j++){
					charLoop: for (int chars = 0; chars < data.getNumChars(); chars++) {
						csi = data.getCharacterState(csi, chars, i);
						csj = data.getCharacterState(csj, chars, j);
						if (!csi.equals(csj))
							isVariable[chars] = true;
					} // inner (char) loop - over all characters
				}
			}
			
			// outerLoop selects reference taxon, it:
			outerLoop: for(int it = 0; it < (containedTaxa.getNumTaxa() - 1); it++){
				if ((association.getAssociation(containedTaxa.getTaxon(it), containingOne)) || (association.getAssociation(containedTaxa.getTaxon(it), containingTwo))){
					// Debugg.println("Comparing sequences to " + containedTaxa.getName(it));
					nContaining++;
					
					/*middleLoop selects the taxon to compare, jt */
					middleLoop: for(int jt = (it + 1); jt < taxa.getNumTaxa(); jt++){
						
						if ((association.getAssociation(containedTaxa.getTaxon(jt), containingOne)) || (association.getAssociation(containedTaxa.getTaxon(jt), containingTwo))){
							// innerLoop loops through each character, comparing the state of character ic between taxa it and jt
							innerLoop: for(int ic = 0; ic < data.getNumChars(); ic++){
								csi = data.getCharacterState(csi, ic, it);
								csj = data.getCharacterState(csj, ic, jt);
								if (!csi.equals(csj)){
									k++;
								}// end of conditional comparing the two sequences at the ith site (sitei)
							}// end of innerLoop
						}// end of middleLoop
					}// end of outerLoop
				}
			}
		// Debugg.println("nContaining (overall Pi)= " + nContaining);
		double L = (double)data.getNumChars();
		double pi = ((double)k / (((double)(nContaining * (nContaining - 1)))/2))/L;
		// DEBUGGER: logln("Nucleotide diversity = " + pi);
		return pi;
		}// end of conditional checking for non-empty matrix
		
		else return -1; // returns a negative value for nucleotide diversity; actual values are between 0 and 1.
		
	} // end of nuceotideDiversity method
		
/*--------------------------------------------------------------------------------------------------------------------------------------------------*/		

	/**The calculateNumber method checks for a character matrix and association, then, if both are present, calls the partitionedPi method to calculate the partitioned nucleotide diversity.*/	
	public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		if (result == null)
			return;
		if(data != null){
			
			/* The following lines also appear in the initialize method, but as it is never called, 
			 I have placed them here as well...*/
			containedTaxa = data.getTaxa();
			if (association == null || (association.getTaxa(0) != containedTaxa && association.getTaxa(1) != containedTaxa)) {
				association = associationTask.getCurrentAssociation(containedTaxa, commandRec);
				Debugg.println("Choosing association");
				if (association.getTaxa(0) == containedTaxa){
					containingTaxa = association.getTaxa(1);
					logln("containing taxa corresponds to 1.");
				}
				else containingTaxa = association.getTaxa(0);
			} 
			
			if(containingTaxa != null && containedTaxa !=null && association != null){
				if(containingOne == null || containingTwo == null){
					
					containingOne = containingTaxa.userChooseTaxon(containerOfModule(), "Choose the first containing taxon:");
					containingTwo = containingTaxa.userChooseTaxon(containerOfModule(), "Choose containing taxon to compare with " + containingOne.getName() + ":");
					if(containingTwo.getNumber() == containingOne.getNumber())
						while (containingTwo.getNumber() == containingOne.getNumber()){
							containingTwo = containingTaxa.userChooseTaxon(containerOfModule(), "Comparison must be made between different containing taxa.  Choose containing taxon to compare with " + containingOne.getName() + ":");
						}
				}
				
				if(containingOne != null && containingTwo != null){
					double piOne = meanPi(data, containingOne.getNumber());
					double piTwo = meanPi(data, containingTwo.getNumber());
					double avePi = (piOne + piTwo)/2;
					double totalPi = nucleotideDiversity(data); 
					
					
					if (piOne<0 || piTwo<0 || totalPi == 0)
						result.setValue(-1);
					else result.setValue((totalPi - avePi)/totalPi);
					Debugg.println("The nucleotide diversity within  " + containingOne.getName() + " is " + piOne);
					Debugg.println("The nucleotide diversity within  " + containingTwo.getName() + " is " + piTwo);
					Debugg.println("The average nucleotide diversity is " + avePi);
					Debugg.println("The overall nucleotide diversity is " + totalPi);
				}	
			}
			// else result.setValue(-1); // TODO Throw some sort of alert to log about requiring a data matrix
			if (resultString != null)
				resultString.setValue("Pairwise Pst = " + result.toString());
		}
	}

/*--------------------------------------------------------------------------------------------------------------------------------------------------*/	
	public boolean isPrerelease(){
		return true;
	}
		
/*--------------------------------------------------------------------------------------------------------------------------------------------------*/	
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}

/*--------------------------------------------------------------------------------------------------------------------------------------------------*/	
	public String getName() {
		return "Pairwise Pst";
	}

/*--------------------------------------------------------------------------------------------------------------------------------------------------*/	
	public String getVersion() {
		return "1.0b";
	}

/*--------------------------------------------------------------------------------------------------------------------------------------------------*/	
	public String getExplanation(){
		return "Calculates the statistic PiP = (PiT - PiX)/PiT, where PiT is the total nucleotide diversity for the matrix, and PiX is the average nucleotide diversity within a population/species/association.";
	}
} // end of PartitionedPi class
