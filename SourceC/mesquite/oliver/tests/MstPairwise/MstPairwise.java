package mesquite.oliver.tests.MstPairwise;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/**Calculates the statistic Mst for selected pair of containing taxa.  
Mst = (Mt - Mave)/Mt, where Mt is the greatest distance between any 
two taxa in the sample, and Mave is the average, over the two selected 
containing taxons, of the maximum distance between two taxa within the 
same containing taxon.*/
public class MstPairwise extends NumberForTree{
AssociationSource associationTask;
TaxaAssociation association;
Taxa containedTaxa, containingTaxa;
Taxon containingOne, containingTwo;

	/* startJob method will select the association to use*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		associationTask = (AssociationSource)hireEmployee(commandRec, AssociationSource.class, "Source of taxon associations");
		if(associationTask == null)
			return sorry(commandRec, getName() + " couldn't start because no source of taxon associations obtained.");
		return true;
	}

	/*----------------------------------------------------------------*/
	/** Returns the distance from descendant (node) to ancestor (anc)*/
	private double distanceToAncestor (int node, int anc, Tree tree){
		if(!tree.descendantOf(node, anc))
			return -1;
		else {
			double runningDistance = 0; // That's sort of funny...
			for (int nodeCounter = node; !(nodeCounter == anc); nodeCounter = tree.branchingAncestor(nodeCounter)){
				runningDistance += tree.getBranchLength(nodeCounter);
			}
			return runningDistance;
		}
	}
	/*----------------------------------------------------------------*/
	/** Returns the shortest distance between two taxa (nodeA and nodeB), through the most recent common ancestor */
	private double distanceBetweenTaxa (int nodeA, int nodeB, Tree tree) {
		if(tree.branchLengthUnassigned(nodeA) || tree.branchLengthUnassigned(nodeB))
			return -1;
		else {
			int mrcaNode = tree.mrca(nodeA, nodeB);
			return distanceToAncestor(nodeA, mrcaNode, tree) + distanceToAncestor(nodeB, mrcaNode, tree);
		}
	}

	/*----------------------------------------------------------------*/
	/** Calculates the maximum distance between any two taxa within the containing Taxon. */
	private double calculateMi (CommandRecord commandRec, int iContaining, Tree tree, Taxa containingTaxa, Taxa containedTaxa){
		double runningMaxMi = 0;
		double candidate = 0;
		
		// This for loop will cycle through the contained taxa and determine the path between a pair of taxa
		referenceTaxon: for (int iContained = 0; iContained < (containedTaxa.getNumTaxa() - 1); iContained++){
			// This conditional checks to see if the reference taxon is contained within taxon iContaining, and proceeds if so
			if (association.getAssociation(containedTaxa.getTaxon(iContained), containingTaxa.getTaxon(iContaining))){
				comparisonTaxon: for(int jContained = iContained +1; jContained <containedTaxa.getNumTaxa(); jContained++){
					// This conditional checks to see if the comparison taxon is contained within the contaning taxon (iContaining), and proceeds with calculations if so.
					if (association.getAssociation(containedTaxa.getTaxon(jContained), containingTaxa.getTaxon(iContaining))){
						int nodeTaxonOne = tree.nodeOfTaxonNumber(iContained);
						int nodeTaxonTwo = tree.nodeOfTaxonNumber(jContained);
						candidate = distanceBetweenTaxa(nodeTaxonOne, nodeTaxonTwo, tree);
						
						if (candidate > runningMaxMi)
							runningMaxMi = candidate;
						
					} // ends conditional confirming that the comparison taxon is contained within taxon iContaining
				} // ends the comparisonTaxon loop
			} // ends conditional confirming that the reference taxon is contained within taxon iContaining
		} // ends the referenceTaxon loop
		
		return runningMaxMi;
		
	}
	/*--------------------------------------------------------------------------------------------------------------------------------------------------*/
	/** Calculates the maximum distance between any two taxa in the sample. */
	private double calculateMt (CommandRecord commandRec, Tree tree, Taxa containingTaxa, Taxa containedTaxa){
		double runningMaxMt = 0;
		double candidate = 0;

		//	This for loop will cycle through the contained taxa and determine the path between a pair of taxa
		referenceTaxon: for (int iContained = 0; iContained < (containedTaxa.getNumTaxa() - 1); iContained++){
			if(association.getAssociation(containedTaxa.getTaxon(iContained), containingOne) || association.getAssociation(containedTaxa.getTaxon(iContained), containingTwo)){
				comparisonTaxon: for(int jContained = iContained +1; jContained <containedTaxa.getNumTaxa(); jContained++){
					if(association.getAssociation(containedTaxa.getTaxon(jContained), containingOne) || association.getAssociation(containedTaxa.getTaxon(jContained), containingTwo)){

						int nodeTaxonOne = tree.nodeOfTaxonNumber(iContained);
						int nodeTaxonTwo = tree.nodeOfTaxonNumber(jContained);
						candidate = distanceBetweenTaxa(nodeTaxonOne, nodeTaxonTwo, tree);
						// Debugg.println("Distance between " + containedTaxa.getName(iContained) + " and " + containedTaxa.getName(jContained) + " is " + candidate);
						if (candidate > runningMaxMt)
							runningMaxMt = candidate;
					}
				} // ends the comparisonTaxon loop
			}
		} // ends the referenceTaxon loop
		
		return runningMaxMt;
	}
	
	/*--------------------------------------------------------------------------------------------------------------------------------------------------*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		if (result == null)
			return;
		
		double miHolder = 0;
		double miRunningTotal = 0;
		double aveMi = 0;
		if (tree != null){
			containedTaxa = tree.getTaxa();
			
			if (association == null || (association.getTaxa(0) != containedTaxa && association.getTaxa(1) != containedTaxa)) {
				association = associationTask.getCurrentAssociation(containedTaxa, commandRec);
				if (association.getTaxa(0) == containedTaxa){
					containingTaxa = association.getTaxa(1);
					// Debugg.println("Containing taxa corresponds to 1.");
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
				
				if(tree.allLengthsAssigned(false)){
					/*This for loop will cycle through the containing taxa and calculate the Mi of each by calling the calculateMi method (above). */
					double miOne = calculateMi(commandRec, containingOne.getNumber(), tree, containingTaxa, containedTaxa);
					double miTwo = calculateMi(commandRec, containingTwo.getNumber(), tree, containingTaxa, containedTaxa);
					aveMi = (miOne + miTwo)/2;
					
					/* if (miHolder >= 0)
					 logln("Mi of " + containingTaxa.getName(iContaining) + " = " + miHolder);
					 else logln("Mi of " + containingTaxa.getName(iContaining) + " could not be calculated.");
					 */
					
					// Debugg.println("The average Mi of all populations (N = " + containingTaxa.getNumTaxa() + ") is " + aveMi);
					double totalMt = calculateMt(commandRec, tree, containingTaxa, containedTaxa); 
					// Debugg.println("Mt = " + totalMt);
					double mst = (totalMt - aveMi)/totalMt;
					// Debugg.println("Mst = " + mst);
					result.setValue(mst);
					if(resultString!= null)
						resultString.setValue("Pairwise Mst = " + mst);
				} // end of conditional checking to see if the tree has branch lengths
				else {
					result.setValue(-1);
					logln("Mst requires a tree with branch lengths.");
				}
			}
		}
	}

/*--------------------------------------------------------------------------------------------------------------------------------------------------*/
	/**returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;
	}
	
/*--------------------------------------------------------------------------------------------------------------------------------------------------*/
	public String getName() {
		return "Mst Pairwise";
	}

/*--------------------------------------------------------------------------------------------------------------------------------------------------*/
	public String getExplanation(){
		return "Calculates the statistic Mst for pair of taxa selected.  Mst = (Mt - Mave)/Mt, where Mt is the greatest distance between any two taxa in the sample, and Mave is the average, over the two selected containing taxons, of the maximum distance between two taxa within the same containing taxon.";
	}
}
