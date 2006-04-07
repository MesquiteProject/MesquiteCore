package mesquite.oliver.nca.MinimumSpanningTree;

import mesquite.oliver.nca.TreeDistance;
import mesquite.lib.*;
import mesquite.distance.*;
import mesquite.distance.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;

public class MinimumSpanningTree extends TreeDistance {
DNATaxaDistFromMatrix distanceTask;
TaxaDistance taxaDistance;
CharMatrixSource characterSourceTask;

	public boolean startJob(String arguments, Object condition,	CommandRecord commandRec, boolean hiredByName) {
		distanceTask = (DNATaxaDistFromMatrix)hireEmployee(commandRec, DNATaxaDistFromMatrix.class, "Distance Metric");
		if (distanceTask == null)
			return sorry(commandRec, getName() + " couldn't start because no distance metric was found.");
		characterSourceTask = (CharMatrixSource)hireEmployee(commandRec, CharMatrixSource.class, "Source of Characters");
		if (characterSourceTask == null)
			return sorry(commandRec, getName() + " couldn't start because no source of character matrices was found.");
		return true;
	}

	public int getNumberOfTrees(Taxa taxa, CommandRecord commandRec) {
		return 1;
	}

	public void initialize(Taxa taxa, CommandRecord commandRec) {
	}

	public boolean allInTree(Taxa taxa, Tree tree, CommandRecord commandRec){
		for (int i = 0; i < taxa.getNumTaxa(); i++){
			if (!tree.taxonInTree(i))
				return false;
		}
		return true;
	}
	
	/** connects the connectingTaxon to the tree, at the mother node of connectedTaxon.*/
	public MesquiteTree connectTaxon (int connectedTaxon, int connectingTaxon, Taxa taxa, MesquiteTree alteredTree){
		int newPlaceholder = alteredTree.sproutDaughter(alteredTree.motherOfNode(alteredTree.nodeOfTaxonNumber(connectedTaxon)), false);
		alteredTree.setNodeLabel(taxa.getTaxonName(connectingTaxon)+"PH", newPlaceholder);
		int newNode2 = alteredTree.sproutDaughter(newPlaceholder, false);
		alteredTree.setTaxonNumber(newNode2, connectingTaxon, false);
		alteredTree.setBranchLength(newPlaceholder, taxaDistance.getDistance(connectedTaxon, connectingTaxon), false);
		alteredTree.setBranchLength(newNode2, 0, false);
		return alteredTree;
	}
	
	public Tree getDistanceTree(Taxa taxa, Tree tree, int treeNumber, ObjectContainer extra, CommandRecord commandRec) {
		if (tree == null || !(tree instanceof MesquiteTree))
			tree = new MesquiteTree(taxa);
		MesquiteTree mTree = ((MesquiteTree)tree);
		if (characterSourceTask.getCurrentMatrix(taxa, commandRec) != null){
			MCharactersDistribution data = (MCharactersDistribution)characterSourceTask.getCurrentMatrix(taxa, commandRec);
			taxaDistance = distanceTask.getTaxaDistance(taxa, data, commandRec);
			if (taxaDistance == null)
				return null;
			int rootNode = mTree.getRoot();
			int comparisonsMade = 0;
			int allPossibleComparisons = (taxa.getNumTaxa())*(taxa.getNumTaxa());
			
			/**Joins the first taxon to the tree, essentially initializing the tree*/
			firstTaxon: for (int firstSister = 0; firstSister < taxa.getNumTaxa(); firstSister++){
				if (taxaDistance.getDistance(0, firstSister) != MesquiteDouble.unassigned){
					mTree.setNodeLabel(taxa.getTaxonName(0)+"PH", rootNode);
					int newNode = mTree.sproutDaughter(rootNode, false);
					mTree.setTaxonNumber(newNode, 0, false);
					mTree.setBranchLength(newNode, 0, false);
					mTree = connectTaxon(0, firstSister, taxa, mTree);
					break firstTaxon;
				}
			}
			/** adds taxa to tree, using connectTaxon method, until all taxa are in tree, or all 
			 pairwise comparisons are made.  If all taxa are joined in a single minimum spanning 
			 tree, and the taxaDistance matrix is complete, comparisonsMade will never >= totalPossibleComparisons.*/
			while (!allInTree(taxa, mTree, commandRec) && (comparisonsMade < allPossibleComparisons)){
				for (int it = 1; it < taxa.getNumTaxa(); it++){
					for (int jt = 0; jt < it; jt++){
						if(taxaDistance.getDistance(it, jt) != MesquiteDouble.unassigned){
							if(mTree.taxonInTree(it) && !mTree.taxonInTree(jt)){
								mTree = connectTaxon(it, jt, taxa, mTree);
							}
							else if (mTree.taxonInTree(jt) && !mTree.taxonInTree(it)){
								mTree = connectTaxon(jt, it, taxa, mTree);
							}
						}
					}
					comparisonsMade++;
				}
			} 
		}
		return mTree;
	}

	public String getName() {
		return "Minimum Spanning Tree";
	}
	public String getExplanation(){
		return "Minimum Spannning Tree based on distances between taxon pairs.";
	}
	public boolean isPrerelease(){
		return true;
	}
	public boolean requestPrimaryChoice(){
		return true;
	}
}
