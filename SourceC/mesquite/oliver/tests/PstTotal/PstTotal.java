package mesquite.oliver.tests.PstTotal;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/**This class calculates the statistic Pst for a sample of containing taxa.*/
public class PstTotal extends NumberForMatrix{
	AssociationSource associationTask;
	TaxaAssociation association;
	Taxa containedTaxa, containingTaxa;
	
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
		logln("In initialize method.");
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
	public double meanPi(MCharactersDistribution data) {
		CharacterState csj = null; // These two CharacterState variables will hold data for comparisons
		CharacterState csi = null;
		
		if(containingTaxa != null && containedTaxa !=null && association != null) { // tests to make sure that the two taxa blocks exist, and proceeds with calcualtions if so
			double[] n = new double[containingTaxa.getNumTaxa()]; // This variable keeps a running tally of the number of sequences compared (NOT the number of comparisons); this number should probably = association.getNumAssociates(containingTaxon)
			double[] k = new double[containingTaxa.getNumTaxa()]; // This variable keeps a running tally of the number of differences between selected sequences.
			double[] piX = new double[containingTaxa.getNumTaxa()]; // This array holds the values of nucleotide diversity of each containing taxon.
			// associationLoop will loop through each of the containing taxa, calculating the nucleotide diversity for each taxon in that block (e.g. it will calculate the nucleotide diversity for each species, given an association between a block of genes and a block of species).
			// TODO the associationLoop seems unnecessary if there is a way to determine if two contained taxa are associated with the same containing taxa...
			
			/*A method to assay which characters are variable, and thus should be considered in calculations*/
			int numVar=0;
			int numTaxa = data.getNumTaxa();
			boolean[] isVariable = new boolean[data.getNumChars()]; 
			for (int initChars = 0; initChars < data.getNumChars(); initChars++){
				isVariable[initChars] = false;
			}
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
			
			
			associationLoop: for (int iContainingTaxon = 0; iContainingTaxon<containingTaxa.getNumTaxa(); iContainingTaxon++) {
				// TODO the associationLoop will occur getNumTaxa times.  Should this be getNumTaxa - 1 times?
				Taxon containingTaxon = containingTaxa.getTaxon(iContainingTaxon);
				n[iContainingTaxon] = 0; //TODO this variable was misbehaving and has been replaced.  It should probably be deleted.
				k[iContainingTaxon] = 0;
				piX[iContainingTaxon] = 0;
				int nContained = 0;
				
				// The following conditional checks to see if there are multiple taxa associated with the current containing Taxon (i.e. it checks to make sure that there are multiple genes within a species).  If there are not, it moves to the next taxon in the containing group.
				if (association.getNumAssociates(containingTaxon)<=1)
					continue associationLoop;
				// TODO when there is but one contained taxon associated with the containing taxon, should nucleotide diversity not be calculated, or should it be set to zero?  Currently, it is set to zero at the beginning of associationLoop (piX[numContainingTaxa] = 0)
				else {
					// referenceLoop will choose the reference sequence to use
					referenceLoop: for(int it=0; it<containedTaxa.getNumTaxa() - 1; it++){
						nContained++;
						Taxon refContainedTaxon = containedTaxa.getTaxon(it);
						
						// This conditional checks to see if the reference sequence of the contained taxon is associated with the current containing taxon.  If not, it moves to the next sequence in the matrix.
						if (!association.getAssociation(refContainedTaxon, containingTaxon))
							continue referenceLoop;
						else {
							//comparisonLoop will choose the sequence to compare to the reference sequence
							comparisonLoop: for(int jt=(it+1); jt<containedTaxa.getNumTaxa(); jt++){
								Taxon compContainedTaxon = containedTaxa.getTaxon(jt);
								
								// This conditional checks to see if the comparison sequence of the contained taxon is associated with the current containing taxon (and thus, is associated with the same containing Taxon as the reference sequence).  If not, it moves to the next sequence in the matrix.
								if (!association.getAssociation(compContainedTaxon, containingTaxon)){
									continue comparisonLoop;
								}
								else {
									n[iContainingTaxon]++;
									charactersLoop: for(int ic=0; ic < data.getNumChars(); ic++){
										if(isVariable[ic]){
											csi = data.getCharacterState(csi, ic, it);
											csj = data.getCharacterState(csj, ic, jt);
											if(!csi.equals(csj))
												k[iContainingTaxon]++;
										} // end of isVariable conditional
									} // end of charactersLoop
								} // end of conditional checking to see if the comparison sequence is associated with the containing taxon
							} // end of comparisonLoop
						} // end of conditional checking to see if the reference sequence is associated with the current containing taxon.
					} // end of referenceLoop
				// Debugg.println(containingTaxa.getName(iContainingTaxon) + " has " + association.getNumAssociates(containingTaxa.getTaxon(iContainingTaxon))+ " taxa.");
				
				} // end of conditional checking for multiple contained taxa
				
				double tempN = association.getNumAssociates(containingTaxa.getTaxon(iContainingTaxon));
				piX[iContainingTaxon]=k[iContainingTaxon]/((tempN*(tempN-1))/2)/(double)data.getNumChars();
			}	// end of associationLoop
				
				double piSum = 0;
				for (int containingT= 0; containingT<containingTaxa.getNumTaxa(); containingT++){
					piSum+=piX[containingT];
				} // end of loop summing the values of nucleotide diversity
				
				return piSum/(double)containingTaxa.getNumTaxa();
		
		} // end of conditional checking for non-empty matrix

		else return -1; // value of -1 returned to paritionedPi if empty matrix found

} // end of meanPi method

/*--------------------------------------------------------------------------------------------------------------------------------------------------*/	
	public double nucleotideDiversity(MCharactersDistribution data) {
		Taxa taxa = data.getTaxa();
		CharacterState csi = null;	//These two variables will hold the data for comparisons
		CharacterState csj = null;
		int nContaining = 0;

		int k = 0; // This variable will represent the running sum of Kij
		
		if(taxa != null && containingTaxa.getNumTaxa() > 0){

			/*A method to assay which characters are variable, and thus should be considered in calculations*/
			int numVar=0;
			int numTaxa = data.getNumTaxa();
			boolean[] isVariable = new boolean[data.getNumChars()]; 
			for (int initChars = 0; initChars < data.getNumChars(); initChars++){
				isVariable[initChars] = false;
			}
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
				nContaining++;
				/*middleLoop selects the taxon to compare, jt */
				middleLoop: for(int jt = (it + 1); jt < containedTaxa.getNumTaxa(); jt++){
					
					// innerLoop loops through each character, comparing the state of character ic between taxa it and jt
					innerLoop: for(int ic = 0; ic < data.getNumChars(); ic++){
						if (isVariable[ic]){
							csi = data.getCharacterState(csi, ic, it);
							csj = data.getCharacterState(csj, ic, jt);
							if (!csi.equals(csj)){
								k++;
							} // end of conditional comparing the two sequences at the ith site (sitei)
						} // end of isVariable conditional 
					}// end of innerLoop
				}// end of middleLoop
			}// end of outerLoop
		nContaining++;
		double L = (double)data.getNumChars();
		double pi = ((double)k / ((double)((nContaining * (nContaining - 1)))/2))/L;
		// Debugg.println("nContaining (overall) = " + nContaining + " overall nucleotide diversity = " + pi);
		// Debugg.println("Nucleotide diversity = " + pi);
			
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

			// The following lines also appear in the initialize method, but as it is never called, I have placed them here as well...
			containedTaxa = data.getTaxa();
			if (association == null || (association.getTaxa(0) != containedTaxa && association.getTaxa(1) != containedTaxa)) {
				association = associationTask.getCurrentAssociation(containedTaxa, commandRec);
				if (association.getTaxa(0) == containedTaxa){
					containingTaxa = association.getTaxa(1);
					logln("containing taxa corresponds to 1.");
				}
				else containingTaxa = association.getTaxa(0);
			}
			
			double avePi = meanPi(data);
			double totalPi = nucleotideDiversity(data); 
			if (avePi<0 || totalPi == 0){
				result.setValue(-1);
				logln("There was an error calculating the Total Pst.");
			}
			else result.setValue((totalPi - avePi)/totalPi);
			// Debugg.println("The average nucleotide diversity is " + avePi);
			// Debugg.println("The overall nucleotide diversity is " + totalPi);
		
		}
		// else result.setValue(-1); // TODO Throw some sort of alert to log about requiring a data matrix
		if (resultString != null)
			resultString.setValue("Partitioned Nucleotide Diversity = " + result.toString());
		
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
		return "Pst Total";
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
