package mesquite.oliver.nca.MinSpanTreeGenerator;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.oliver.nca.*;

public class MinSpanTreeGenerator extends NumberForMatrix{

		CharacterState csi = null;		//These two variables will hold the data for comparsions
		CharacterState csj = null;
		PrimCellElement[][] primElements;
		PrimSolutionSummary[] primSolution;
		int[][] taxonByTaxon;
		int numSolutionElements = 0;
		                    
		public void initialize(MCharactersDistribution data, CommandRecord commandRec) {
		}

		public boolean startJob(String arguments, Object condition,CommandRecord commandRec, boolean hiredByName) {
			return true;
		}

		public void fillPrimMatrix (MCharactersDistribution data){
			Taxa taxa = data.getTaxa();
			int numTaxa = data.getNumTaxa();
			// Filling the matrix:
			for (int i = 0; i < numTaxa; i++){
				for (int j = 0; j < numTaxa; j++){
					int pDist = 0;
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
		
		public void printPrimMatrix(int numTaxa){
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
		
		public void fillPrimSolution(MCharactersDistribution data){
			int shortestD;
			int shortestL;
			int numChars = data.getNumChars();
			int numTaxa = data.getNumTaxa();
			int comparisonsRemaining = numTaxa;
			for (int comps = 1; comps < comparisonsRemaining; comps++){
				comps--;
				int i = 0;
				shortestD = primElements[i][1].getDistance();
				//Debugg.println("Shortest Distance = "+shortestD+", before looping through taxa.");
				/**Cycles through elements in the first row, finding the shortest distance (may occur in > 1 cell)*/
				for (int j = 1; j < numTaxa; j++){
					//Debugg.print("d("+j+")="+primElements[i][j].getDistance()+" | ");
					if ((shortestD > primElements[i][j].getDistance() || shortestD <= 0) && (primElements[i][j].getDistance() > 0)){
						shortestD = primElements[i][j].getDistance();
						//Debugg.print("("+j+")"+shortestD+" | ");
					}				
				}
				//Debugg.println("");
				//Debugg.println("Shortest Distance = " + shortestD);
				/*This for loop (incrementShortestD) is new, and could be deleted...in the enclosed loops, change
				 * 'shortD' back to 'shortestD'*/
				incrementShortestD: for (int shortD = 1; shortD <= shortestD; shortD++){
					Debugg.println("shortD = "+ shortD+ " shortestD = "+shortestD);
					comparingColumns: for (int j = 1; j< numTaxa; j++){
						// Debugg.println("Looking forwards:  j = "+j+"; shortestD = "+shortestD);
						if (primElements[i][j].getDistance() == shortD && shortD > 0){
							double pValue = getPLinkage(primElements[i][j].getDistance(), (numChars - primElements[i][j].getDistance()));
							Debugg.println("P("+data.getTaxa().getTaxonName(i)+", "+data.getTaxa().getTaxonName(j)+", at "+shortD+") = " + pValue);
							//Debugg.print("Current Distances: ");
							//for (int loopTZero = 0; loopTZero < numTaxa; loopTZero++){
							//	Debugg.print(primElements[i][loopTZero].getDistance()+" ");
							//}
							//Debugg.println("");
							if (getPLinkage(primElements[i][j].getDistance(), (numChars - primElements[i][j].getDistance())) >= 0.94){
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
								
								// TODO: delete? two lines below:
								// primElements[primElements[i][j].getTaxon().getNumber()][j].setDistance(-1);
								// primElements[j][primElements[i][j].getTaxon().getNumber()].setDistance(-1);
								// Debugg.println("Taxon 1 compared to " + data.getTaxa().getTaxonName(j)+ "; i="+i+"  j="+j);
								// Debugg.println("Linking " + primSolution[numSolutionElements-1].getTaxonA().getName() + " and " + primSolution[numSolutionElements-1].getTaxonB().getName());
								comparisonsRemaining = comparisonsRemaining - 1;
								//Debugg.println("Comparisons remaining = "+comparisonsRemaining);
								
								/*Considering the fragment, and adjusting the first row of the of distances accordingly.*/
								for (int jTwo = 0; jTwo < numTaxa; jTwo++){
									if(primElements[i][jTwo].getDistance() != -1 && primElements[j][jTwo].getDistance() != -1){
										if(primElements[i][jTwo].getDistance() > primElements[j][jTwo].getDistance()){
											primElements[i][jTwo].setDistance(primElements[j][jTwo].getDistance());
											primElements[i][jTwo].setTaxon(primElements[j][jTwo].getTaxon());
										}
									}
								}
								// printPrimMatrix(data.getNumTaxa());
							} else {
								Debugg.println("Could not link " + data.getTaxa().getTaxon(i).getName() + " and " + data.getTaxa().getTaxon(j).getName()+"; p < 0.95.");
								comparisonsRemaining--;
							}
							//Debugg.print("Distances after reassiging: ");
							//for (int loopTZero = 0; loopTZero < numTaxa; loopTZero++){
							//	Debugg.print(primElements[i][loopTZero].getDistance()+" ");
							//}
							//Debugg.println("");
							shortD = 1;
							j = 1;
							continue comparingColumns;
						}
					} // incrementShortestD
				} // comparingColumns
			} // the comps loop
		}
		
		public void printPrimSolution(){
			for (int n = 0; n < numSolutionElements; n++){
				Debugg.println("Link " + primSolution[n].getTaxonA().getName() + " and " + primSolution[n].getTaxonB().getName() + " by " + primSolution[n].getDistance() + " steps.");
			}
		}

		public void printTaxonByTaxon(int numTaxa, MCharactersDistribution data){
			for (int i = 0; i < numTaxa; i++){
				Debugg.print(data.getTaxa().getTaxonName(i) + " | ");
				for (int j = 0; j < numTaxa; j++){
					Debugg.print(taxonByTaxon[i][j] + " ");
				}
				Debugg.println("");
			}
		}
		
		public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
			Debugg.println("===========================");
			Taxa taxa = data.getTaxa();
			int numTaxa = data.getNumTaxa();
			int jDiff; // jDiff will equal the number of differences between two sequences
			double pLinkage = 0;
			double pLinkageTwo = 1; // for second derivation, should eventually replace pLinkage
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

			
			if (primElements != null){
				fillPrimMatrix(data);
				//Debugg.println("Initial matrix:");
				//printPrimMatrix(numTaxa);
				fillPrimSolution(data);
				Debugg.println("Prim Solution:");
				printPrimSolution();
				//Debugg.println("Final matrix:");
				//printPrimMatrix(numTaxa);
				Debugg.println("Distance between linked taxa:");
				printTaxonByTaxon(numTaxa, data);
			}
		}

		public String getName() {
			return "Minimum Spanning Tree Calculator (Oliver)";
		}
		public String getExplanation(){
			return "Outputs a Taxa Distance matrix to Mesquite Log to be used to create a minimum spanning tree.  Use's Prim's algorithm for finding minimum spanning tree.";
		}
		public boolean requestPrimaryChoice(){
			return true;
		}

		/**Estimates qHat, the probability of a non-parsimonious linkage between haplotypes 
		(unobserved mutations at any site) (Templeton et al. 1992)*/
		private double getQHat(double j, double m){
			double qHat = 0;
			qHat = j*((2*m+j+1)+(2/3)*(j+1))/((2*m+2*j+2)*((2*m+j+1)+(2/3)*j));
			return qHat;
		}
		
		/**Estimates Pj, the probability that two haplotypes differing at j sites but sharing 
		m have a parsimonious relationship (no unobserved mutations at any site) (Templeton et al. 1992)*/
		private double getPLinkage (double jDiff, double m){
			double pLinkageTwo = 1;
			for(int calcP = 1; calcP <= jDiff; calcP++){
				pLinkageTwo = pLinkageTwo*(1 - getQHat(jDiff, m));
			}
			return pLinkageTwo;
		}
	}
