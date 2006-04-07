package mesquite.oliver.nca.PrimMSTDistance;

import mesquite.distance.*;
import mesquite.distance.lib.*;
import mesquite.oliver.nca.PrimCellElement;
import mesquite.oliver.nca.PrimSolutionSummary;
import mesquite.lib.*;
import mesquite.lib.characters.*;

/** This class uses Prim's (1957) algorithm for finding a Minimum Spanning Tree 
 given distances among a set of taxa.  It currently uses uncorrected pairwise 
 differences for calculations.  For details on the algorithm, see Prim, R.C. 
 1957. Shortest connection networks and some generalizations. Bell. Syst. 
 Tech. J. 36:1389-1401.*/

/**NOTICE: This class finds a single Minimum Spanning Tree; however, there may be multiple Minimum Spanning Trees.*/
public class PrimMSTDistance extends DNATaxaDistFromMatrix {

	public TaxaDistance getTaxaDistance(Taxa taxa, MCharactersDistribution observedStates, CommandRecord commandRec) {
		if (observedStates==null) {
			MesquiteMessage.warnProgrammer("Observed states null in "+ getName());
			return null;
		}
		PrimMSTTD simpleTD = new PrimMSTTD(this, taxa, observedStates, getEstimateAmbiguityDifferences());
		if(taxa != null && observedStates != null){
			if (simpleTD.primElements != null && simpleTD.primSolution != null){
				return simpleTD;
			}else return null;
		} else return null;
	}

	public String getName() {
		return "Distance for MST (Prim algorithm)";
	}
	public String getExplanation(){
		return "Distance for minimum spanning tree based on Prim's algorithm.";
	}
	 public boolean requestPrimaryChoice(){
	   	 	return true;
	   	 }
	 public boolean isPrerelease(){
	 	return true;
	 }
	/*.................................................................................................................*/
	 public boolean showCitation(){
	 	return true;
	 }
}

// =============

class PrimMSTTD extends DNATaxaDistance{
	CharacterState csi = null;		//These two variables will hold the data for comparsions
	CharacterState csj = null;
	PrimCellElement[][] primElements;
	PrimSolutionSummary[] primSolution;
	int[][] taxonByTaxon;
	int numSolutionElements = 0;

	public PrimMSTTD(MesquiteModule ownerModule, Taxa taxa, MCharactersDistribution observedStates, boolean estimateAmbiguityDifferences){
		super(ownerModule, taxa, observedStates, estimateAmbiguityDifferences);
		setEstimateAmbiguityDifferences(((DNATaxaDistFromMatrix)ownerModule).getEstimateAmbiguityDifferences());

		initialize(taxa, observedStates);
		if (primElements != null && primSolution != null){
			fillPrimMatrix(observedStates);
			Debugg.println("==============Prim Matrix============");
			printPrimMatrix(taxa.getNumTaxa());
			fillPrimSolution(observedStates);
			Debugg.println("=============Prim Solution===========");
			printPrimSolution();
			Debugg.println("=========Taxon By Taxon Matrix=======");
			printTaxonByTaxon(taxa.getNumTaxa(), observedStates);
		}

		
		if (taxonByTaxon != null){
			for (int taxoni = 0; taxoni < taxa.getNumTaxa(); taxoni++){
				for (int taxonj = 0; taxonj < taxa.getNumTaxa(); taxonj++){
					if (taxonByTaxon[taxoni][taxonj] < 1)
						distances[taxoni][taxonj]= MesquiteDouble.unassigned;
					else
						distances[taxoni][taxonj]=taxonByTaxon[taxoni][taxonj];
				}
			}
		}
	}
/*Methods below have been taken from MinSpanTreeGenerator module.*/
		
/** Initializes the various objects that will be used for calculations.  Here to avoid the spectre of NullPointerExceptions.*/
	public void initialize(Taxa taxa, MCharactersDistribution observedStates){
		int numTaxa = taxa.getNumTaxa();
		int[][] tempMatrix = new int[numTaxa][numTaxa];
		taxonByTaxon = tempMatrix;
		PrimCellElement[][] primTempElements = new PrimCellElement[numTaxa][numTaxa];
		primElements = primTempElements;
		/**Initializing the Prim Matrix and the taxonByTaxon matrix: */
		for (int i = 0; i < numTaxa; i++){
			for (int j = 0; j < numTaxa; j++){
				primElements[i][j] = new PrimCellElement(); 
				taxonByTaxon[i][j] = 0;
			}
		}
		int totalPossLinks = (numTaxa*(numTaxa - 1))/2;
		PrimSolutionSummary[] primTempSolution = new PrimSolutionSummary[totalPossLinks];
		primSolution = primTempSolution;
	}
	
/** Fills the Prim Matrix, which in this case, is represented by a taxa X taxa array of PrimCellElements*/
	public void fillPrimMatrix(MCharactersDistribution data){
		if (primElements != null){
			Taxa taxa = data.getTaxa();
			int numTaxa = data.getNumTaxa();
			int pDist = 0;
			// Filling the matrix:
			for (int i = 0; i < numTaxa; i++){
				for (int j = 0; j < numTaxa; j++){
					pDist = 0;
					charLoop: for (int chars = 0; chars < data.getNumChars(); chars++) {
						csi = data.getCharacterState(csi, chars, i);
						csj = data.getCharacterState(csj, chars, j);
						if (!csi.equals(csj))
							pDist++;
					} // inner (char) loop - over all characters
					primElements[i][j].setDistance(pDist);
					primElements[i][j].setTaxon(taxa.getTaxon(i));
				}
			}
		}
	}
/** Prints the Prim Matrix to the Mesquite Log; used for quality control.*/
	public void printPrimMatrix (int numTaxa){
		if(primElements != null){
			for (int i = 0; i < numTaxa; i++){
				for (int j = 0; j< numTaxa; j++){
					if (j == 0){
						Debugg.print(primElements[i][j].getTaxon().getName()+" | ");
					}
					Debugg.print(primElements[i][j].getDistance()+" ");
				}
				Debugg.println("");
			}
		}
	}

/** Fills the solution matrix using Prim's algorithm for finding a minimum spanning tree (Prim 1957).*/	
	public void fillPrimSolution(MCharactersDistribution data){
		if(primElements != null){
			int shortestD;
			int shortestL;
			int numChars = data.getNumChars();
			int numTaxa = data.getNumTaxa();
			int comparisonsRemaining = numTaxa;
			for (int comps = 1; comps < comparisonsRemaining; comps++){
				comps--;
				int i = 0;
				shortestD = primElements[i][1].getDistance();
				/**Cycles through elements in the first row, finding the shortest distance (may occur in > 1 cell)*/
				for (int j = 1; j < numTaxa; j++){
					if ((shortestD > primElements[i][j].getDistance() || shortestD <= 0) && (primElements[i][j].getDistance() > 0)){
						shortestD = primElements[i][j].getDistance();
					}				
				}
				incrementShortestD: for (int shortD = 1; shortD <= shortestD; shortD++){
					comparingColumns: for (int j = 1; j< numTaxa; j++){
						if (primElements[i][j].getDistance() == shortD && shortD > 0){
							numSolutionElements++;
							primSolution[numSolutionElements-1] = new PrimSolutionSummary();
							primSolution[numSolutionElements-1].setDistance(primElements[i][j].getDistance());
							primSolution[numSolutionElements-1].setTaxonA(data.getTaxa().getTaxon(j));
							primSolution[numSolutionElements-1].setTaxonB(primElements[i][j].getTaxon());
							taxonByTaxon[primSolution[numSolutionElements-1].getTaxonA().getNumber()][primSolution[numSolutionElements-1].getTaxonB().getNumber()] = primSolution[numSolutionElements-1].getDistance();
							taxonByTaxon[primSolution[numSolutionElements-1].getTaxonB().getNumber()][primSolution[numSolutionElements-1].getTaxonA().getNumber()] = primSolution[numSolutionElements-1].getDistance();
							primElements[i][j].setDistance(-1);
							primElements[j][i].setDistance(-1);
							primElements[primSolution[numSolutionElements-1].getTaxonA().getNumber()][primSolution[numSolutionElements-1].getTaxonB().getNumber()].setDistance(-1);
							primElements[primSolution[numSolutionElements-1].getTaxonB().getNumber()][primSolution[numSolutionElements-1].getTaxonA().getNumber()].setDistance(-1);
							taxonByTaxon[primSolution[numSolutionElements-1].getTaxonA().getNumber()][primSolution[numSolutionElements-1].getTaxonB().getNumber()] = primSolution[numSolutionElements-1].getDistance();
							
							comparisonsRemaining = comparisonsRemaining - 1;
							
							/*Considering the fragment, and adjusting the first row of the of distances accordingly.  See Prim (1957) for details.*/
							for (int jTwo = 0; jTwo < numTaxa; jTwo++){
								if(primElements[i][jTwo].getDistance() != -1 && primElements[j][jTwo].getDistance() != -1){
									if(primElements[i][jTwo].getDistance() > primElements[j][jTwo].getDistance()){
										primElements[i][jTwo].setDistance(primElements[j][jTwo].getDistance());
										primElements[i][jTwo].setTaxon(primElements[j][jTwo].getTaxon());
									}
								}
							}
							
						shortD = 1;
						j = 1;
						continue comparingColumns;
						}
					} // incrementShortestD
				} // comparingColumns
			} // the comps loop
		}
	}		

/** Prints the Prim Solution to the Mesquite Log; used for quality control.  
 The solution lists the links to be made to create a Minimum Spanning Tree.*/
	public void printPrimSolution(){
		if(primElements != null){
			for (int n = 0; n < numSolutionElements; n++){
				Debugg.println("Link " + primSolution[n].getTaxonA().getName() + " and " + primSolution[n].getTaxonB().getName() + " by " + primSolution[n].getDistance() + " steps.");
			}
		}
	}

/** Prints the Prim Solution Matrix to the Mesquite Log as a matrix; used for quality control.  
 The solution printed on the screen shows the distance between taxa that are to be linked for 
 Minimum Spanning Tree; for those taxa that do not have a direct connection, the distance is 
 listed as a zero (0).*/
	public void printTaxonByTaxon(int numTaxa, MCharactersDistribution data){
		if(primElements != null){
			for (int i = 0; i < numTaxa; i++){
				Debugg.print(data.getTaxa().getTaxonName(i) + " | ");
				for (int j = 0; j < numTaxa; j++){
					Debugg.print(taxonByTaxon[i][j] + " ");
				}
				Debugg.println("");
			}
		}
	}
/*========End of MinSpanTreeGenerator============*/

}
