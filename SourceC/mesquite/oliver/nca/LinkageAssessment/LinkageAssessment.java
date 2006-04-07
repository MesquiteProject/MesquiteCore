package mesquite.oliver.nca.LinkageAssessment;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.NumberForMatrix;
import mesquite.oliver.nca.*;

public class LinkageAssessment extends NumberForMatrix {
	// PitmanEstimator qHat;
	CharacterState csi = null;		//These two variables will hold the data for comparsions
	CharacterState csj = null;
	PrimCellElement[][] primElements;
	PrimSolutionSummary[] primSolution;
	int numSolutionElements = 0;
	                    
	public void initialize(MCharactersDistribution data, CommandRecord commandRec) {
	}

	public boolean startJob(String arguments, Object condition,CommandRecord commandRec, boolean hiredByName) {
		// if (qHat == null)
		//	return false;
		//else 
		return true;
	}

	private int numDifferences(MCharactersDistribution data, int it, int jt){

		int k = 0;
		charLoop: for (int chars = 0; chars < data.getNumChars(); chars++) {
			csi = data.getCharacterState(csi, chars, it);
			csj = data.getCharacterState(csj, chars, jt);
			if (!csi.equals(csj))
				k++;
		} // inner (char) loop - over all characters
		return k;
	}

	private int numVariable (MCharactersDistribution data){
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
		
		for (int sumVariable = 0; sumVariable < data.getNumChars(); sumVariable++){
			if (isVariable[sumVariable] == true)
				numVar++;
		}
		return numVar;
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
		int comparisonsRemaining = numTaxa+2;
		for (int comps = -1; comps < comparisonsRemaining; comps++){
			// comps;  // this was comps--
			int i = 0;
			shortestD = primElements[i][1].getDistance();
			Debugg.println("Shortest Distance = "+shortestD+", before looping through taxa.");
			/**Cycles through elements in the first row, finding the shortest distance (may occur in > 1 cell)*/
			for (int j = 1; j < numTaxa; j++){
				//Debugg.print("d("+j+")="+primElements[i][j].getDistance()+" | ");
				if ((shortestD > primElements[i][j].getDistance() || shortestD <= 0) && (primElements[i][j].getDistance() > 0)){
					shortestD = primElements[i][j].getDistance();
				}				
			}
			// Debugg.println("");
			Debugg.println("Shortest Distance = " + shortestD);
			for (int j = 1; j< numTaxa; j++){
				// Debugg.println("Looking forwards:  j = "+j+"; shortestD = "+shortestD);
				if (primElements[i][j].getDistance() == shortestD && shortestD > 0){
					double pValue = getPLinkage(primElements[i][j].getDistance(), (numChars - primElements[i][j].getDistance()));
					Debugg.println("P("+(i+1)+", "+(j+1)+") = " + pValue);
					if (getPLinkage(primElements[i][j].getDistance(), (numChars - primElements[i][j].getDistance())) >= 0.94){
						numSolutionElements++;
						primSolution[numSolutionElements-1] = new PrimSolutionSummary();
						primSolution[numSolutionElements-1].setDistance(primElements[i][j].getDistance());
						primSolution[numSolutionElements-1].setTaxonA(data.getTaxa().getTaxon(j));
						primSolution[numSolutionElements-1].setTaxonB(primElements[i][j].getTaxon());
						primElements[i][j].setDistance(-1);
						primElements[j][i].setDistance(-1);
						primElements[primSolution[numSolutionElements-1].getTaxonA().getNumber()][primSolution[numSolutionElements-1].getTaxonB().getNumber()].setDistance(-1);
						primElements[primSolution[numSolutionElements-1].getTaxonB().getNumber()][primSolution[numSolutionElements-1].getTaxonA().getNumber()].setDistance(-1);
						// TODO: delete? two lines below:
						// primElements[primElements[i][j].getTaxon().getNumber()][j].setDistance(-1);
						// primElements[j][primElements[i][j].getTaxon().getNumber()].setDistance(-1);
						// Debugg.println("Taxon 1 compared to " + data.getTaxa().getTaxonName(j)+ "; i="+i+"  j="+j);
						// Debugg.println("Linking " + primSolution[numSolutionElements-1].getTaxonA().getName() + " and " + primSolution[numSolutionElements-1].getTaxonB().getName());
						comparisonsRemaining = comparisonsRemaining - 1;
						Debugg.println("Comparisons remaining = "+comparisonsRemaining);
					
						/*Considering the fragment, and adjusting the first row of the of distances accordingly.*/
						for (int jTwo = 0; jTwo < numTaxa; jTwo++){
							if(primElements[i][jTwo].getDistance() != -1 && primElements[j][jTwo].getDistance() != -1){
								if(primElements[i][jTwo].getDistance() > primElements[j][jTwo].getDistance()){
									primElements[i][jTwo].setDistance(primElements[j][jTwo].getDistance());
									primElements[i][jTwo].setTaxon(primElements[j][jTwo].getTaxon());
								}
							}
						}
						//printPrimMatrix(data.getNumTaxa());
						
						/* Looking at other possible connections of Taxon j of length shortestD - allows reticulation.*/
						Debugg.println("Looking backwards, shortest distance = "+shortestD);
						for (int jTwo = 0; jTwo < numTaxa; jTwo++){
							if(primElements[j][jTwo].getDistance() == shortestD){
								double pValue2 = getPLinkage(primElements[j][jTwo].getDistance(), (numChars - primElements[j][jTwo].getDistance()));
								Debugg.println("P("+(j+1)+", "+(jTwo+1)+") = " + pValue2);
								if (getPLinkage(primElements[j][jTwo].getDistance(), (numChars - primElements[j][jTwo].getDistance())) >= 0.94){
									numSolutionElements++;
									primSolution[numSolutionElements-1] = new PrimSolutionSummary();
									primSolution[numSolutionElements-1].setDistance(primElements[j][jTwo].getDistance());
									primSolution[numSolutionElements-1].setTaxonA(data.getTaxa().getTaxon(jTwo));
									primSolution[numSolutionElements-1].setTaxonB(primElements[j][jTwo].getTaxon());
									primElements[j][jTwo].setDistance(-1);
									primElements[jTwo][j].setDistance(-1);
									primElements[i][jTwo].setDistance(-1);
									primElements[jTwo][i].setDistance(-1);
									// primElements[primSolution[numSolutionElements-1].getTaxonA().getNumber()][primSolution[numSolutionElements-1].getTaxonB().getNumber()].setDistance(-1);
									// primElements[primSolution[numSolutionElements-1].getTaxonB().getNumber()][primSolution[numSolutionElements-1].getTaxonA().getNumber()].setDistance(-1);
									// Debugg.println("Linking " + primSolution[numSolutionElements-1].getTaxonA().getName() + " and " + primSolution[numSolutionElements-1].getTaxonB().getName()+" (j = "+data.getTaxa().getTaxonName(j)+"  jTwo = "+data.getTaxa().getTaxonName(jTwo)+")");
									
									/*Considering the fragment, and adjusting the first row of the of distances accordingly.*/
									for (int jThree = 0; jThree < numTaxa; jThree++){
										if(primElements[i][jThree].getDistance() != -1 && primElements[jTwo][jThree].getDistance() != -1){
											if(primElements[i][jThree].getDistance() > primElements[jTwo][jThree].getDistance()){
												primElements[i][jThree].setDistance(primElements[jTwo][jThree].getDistance());
												primElements[i][jThree].setTaxon(primElements[jTwo][jThree].getTaxon());
											}
										}
									}
									
									//printPrimMatrix(data.getNumTaxa());
									comparisonsRemaining = comparisonsRemaining - 1;
									Debugg.println("Comparisons remaining = "+comparisonsRemaining);
								} else Debugg.println("Could not link " + data.getTaxa().getTaxon(j).getName() + " and " + data.getTaxa().getTaxon(jTwo).getName()+"; p < 0.95.");
							}
						}
						
						// printPrimMatrix(data.getNumTaxa());
					} else Debugg.println("Could not link " + data.getTaxa().getTaxon(i).getName() + " and " + data.getTaxa().getTaxon(j).getName()+"; p < 0.95.");
				}
			}
		} // the comps loop
	}
	
	public void printPrimSolution(){
		for (int n = 0; n < numSolutionElements; n++){
			Debugg.println("Link " + primSolution[n].getTaxonA().getName() + " and " + primSolution[n].getTaxonB().getName() + " by " + primSolution[n].getDistance() + " steps.");
		}
	}
	
	public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		// int m = data.getNumChars();
		Debugg.println("===========================");
		Taxa taxa = data.getTaxa();
		int numTaxa = data.getNumTaxa();
		int jDiff; // jDiff will equal the number of differences between two sequences
		double pLinkage = 0;
		double pLinkageTwo = 1; // for second derivation, should eventually replace pLinkage
		PrimCellElement[][] primTempElements = new PrimCellElement[numTaxa][numTaxa];
		primElements = primTempElements;
		/**Initializing the Prim Matrix: */
		for (int i = 0; i < numTaxa; i++){
			for (int j = 0; j < numTaxa; j++){
				primElements[i][j] = new PrimCellElement(); 
			}
		}
		int totalPossLinks = (numTaxa*(numTaxa - 1))/2;
		PrimSolutionSummary[] primTempSolution = new PrimSolutionSummary[totalPossLinks];
		primSolution = primTempSolution;
		/*		for (int i = 0; i < totalPossLinks; i++){
			primSolution[]
		}*/
		
		if (primElements != null){
			fillPrimMatrix(data);
			Debugg.println("Initial matrix:");
			printPrimMatrix(numTaxa);
			fillPrimSolution(data);
			Debugg.println("Prim Solution:");
			printPrimSolution();
			Debugg.println("Final matrix:");
			printPrimMatrix(numTaxa);
		}
		
		// TODO: the method to assess linkage support has been commented out temporarily (below)
		/*		for (int i = 0; i < numTaxa; i++){
			for (int j = 0;j < i; j++){
				pLinkageTwo = 1; // for second derivation, should eventually replace pLinkage
				jDiff = numDifferences(data, i, j);
				// int m = data.getNumChars() - jDiff;
				int m = numVariable(data) - jDiff;
				// Debugg.println(numDifferences(data, i, j) + " differences between " + taxa.getTaxonName(i) + " and " + taxa.getTaxonName(j)+" j = "+jDiff+", m = "+m);
				for(int calcP = 1; calcP <= jDiff; calcP++){
					//Debugg.println("Pre-calculation: "+taxa.getTaxonName(i) + " vs. " + taxa.getTaxonName(j) + " P("+ calcP+") = "+pLinkageTwo +", j = "+jDiff+" m = "+m);
					pLinkageTwo = pLinkageTwo*(1 - getQHat(jDiff, m));
					//Debugg.println("Post-calculation: "+taxa.getTaxonName(i) + " vs. " + taxa.getTaxonName(j) + " P("+ calcP+") = "+pLinkageTwo +", j = "+jDiff+" m = "+m);
				}
				//if (jDiff < 3){
					//if (jDiff == 1)
						//pLinkage = (1 - getEstimate(m, 1));
					//else if (jDiff == 2)
						//pLinkage = (1 - getEstimate(m, 1))*(1 - getEstimate(m, 2));
					// Debugg.println("P of linkage of " + jDiff + " steps between " + taxa.getTaxonName(i) + " and " + taxa.getTaxonName(j) + " = " + pLinkage);
					//Debugg.println("pLinkageTwo of " + jDiff + " steps between " + taxa.getTaxonName(i) + " and " + taxa.getTaxonName(j) + " = " + pLinkageTwo);
				//} else {
					// Debugg.println("The sequences are too different to link at this time with first derivation.");
					Debugg.println("P(m= "+m+") " + jDiff + " steps between " + taxa.getTaxonName(i) + " and " + taxa.getTaxonName(j) + " = " + pLinkageTwo);
				//}
			}
		} */
	}

	public String getName() {
		return "SPN Calculator";
	}
	public String getExplanation(){
		return "Calculates probability of linkage between two haplotypes for NCA cladogram estimation.";
	}
	public boolean requestPrimaryChoice(){
		return false;
	}

/*==========The methods below will ideally end up in their own class...============*/
/*................Second derivation, allows j to vary..............................*/

	private double getQHat(double j, double m){
		double qHat = 0;
		qHat = j*((2*m+j+1)+(2/3)*(j+1))/((2*m+2*j+2)*((2*m+j+1)+(2/3)*j));
		return qHat;
	}
	
	private double getPLinkage (double jDiff, double m){
		double pLinkageTwo = 1;
		for(int calcP = 1; calcP <= jDiff; calcP++){
			//Debugg.println("Pre-calculation: "+taxa.getTaxonName(i) + " vs. " + taxa.getTaxonName(j) + " P("+ calcP+") = "+pLinkageTwo +", j = "+jDiff+" m = "+m);
			pLinkageTwo = pLinkageTwo*(1 - getQHat(jDiff, m));
			//Debugg.println("Post-calculation: "+taxa.getTaxonName(i) + " vs. " + taxa.getTaxonName(j) + " P("+ calcP+") = "+pLinkageTwo +", j = "+jDiff+" m = "+m);
		}
		return pLinkageTwo;
	}
	
/*................First derivation, j must be known................................*/
	
 	private double jOneDenomFunc(double q, double a){
		return (Math.pow((1-q), a)*(q-1)*(4-2*q+4*(Math.pow(q,2))+(Math.pow(a,2))*(1-2*q+2*(Math.pow(q, 2))) + a*(3-4*q+6*(Math.pow(q,2)))))/(6+11*a+6*(Math.pow(a,2))+Math.pow(a,3));
	}
	
	private double jOneDenominator(double m){
		double jOneD = -1.0;
		double a = 2*(m)+2;
		jOneD = jOneDenomFunc(1,a) - jOneDenomFunc(0,a);
		return jOneD;
	}

	private double jOneNumFunc(double q, double a){
		double qSquared = Math.pow(q,2);
		double qCubed = Math.pow(q,3);
		double aSquared = Math.pow(a,2);
		double aCubed = Math.pow(a,3);
		return ((Math.pow((1-q),a))*(q-1)*(8+8*q-4*qSquared+12*qCubed+aCubed*q*(1-2*q-2*qSquared))+aSquared*(1+4*q-8*qSquared+12*qCubed)+a*(3+11*q-10*qSquared+22*qCubed))/(24+50*a+35*aSquared+10*aCubed+a*aCubed);
	}

	private double jOneNumerator(double m){
		double jOneN = -1.0;
		double a = 2*(m)+2;
		jOneN = jOneNumFunc(1,a) - jOneNumFunc(0,a);
		return jOneN;
	}

	private double jTwoDenomFunc(double q, double b){
		double qSquared = Math.pow(q,2);
		double qCubed = Math.pow(q,3);
		double bSquared = Math.pow(b,2);
		double bCubed = Math.pow(b,3);
		return (2*Math.pow((1-q),b)*(8+8*q-4*qSquared+12*qCubed+bCubed*q*(1-2*q+2*qSquared))+bSquared*(1+4*q-8*qSquared+12*qCubed)+b*(3+11*q-10*qSquared+22*qCubed))/(24+50*b+35*bSquared+10*bCubed+b*bCubed);
	}
	
	private double jTwoDenominator(double m){
		double jTwoD = 0;
		double b = 2*m+3;
		jTwoD = jTwoDenomFunc(1,b) - jTwoDenomFunc(0,b);
		return jTwoD;
	}
	private double jTwoNumFunc(double q, double b){
		double qSquared = Math.pow(q,2);
		double qCubed = Math.pow(q,3);
		double qFourth = Math.pow(q,4);
		double bSquared = Math.pow(b,2);
		double bCubed = Math.pow(b,3);
		double bFourth = Math.pow(b,4);
		return (2*(Math.pow((1-q),b))*(q-1)*((bFourth*qSquared*(1-2*q+2*qSquared))+2*bCubed*q*(1+3*q-7*qSquared+10*qCubed)+4*(7+7*q+7*qSquared-3*qCubed+12*qFourth)+2*b*(3+17*q+24*qSquared-17*qCubed+50*qFourth)+bSquared*(2+8*q+25*qSquared-34*qCubed+70*qFourth)))/(120+274*b+225*bSquared+85*bCubed+15*bFourth+b*bFourth);
	}

	private double jTwoNumerator(double m){
		double jTwoN = 0;
		double b = 2*m+3;
		jTwoN = jTwoNumFunc(1,b) - jTwoNumFunc(0,b);
		return jTwoN;
	}
	
	public double getEstimate(double m, double j){
		double qHat = 0;
		if (j == 1)
			qHat = jOneNumerator(m)/jOneDenominator(m);
		else if (j == 2)
			qHat = jTwoNumerator(m)/jTwoDenominator(m);
		else qHat = -1.0;
		return qHat;
	}

}
