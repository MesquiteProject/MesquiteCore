/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
/*=========================================================================*/
/** This class reconstructs ancestral states by squared change parsimony for a matrix of observed data and a tree.
This operates on a simple double[][] instead of a MContinuousDistribution.*/
public class SquaredReconstructor {
	boolean rootedMode = true;
	boolean useWeights = false;
	double[][] observedStates;
	ContinuousDistribution observedDistribution;
	double[][] reconstructedStates;
	Tree tree;
	double[][] downA;
	double[][] downB;
	double[][] upA;
	double[][] upB;
	double[][][] finalC;
	double[][] finalD;
	int item = 0;
	int alreadyWarnedZeroLength = 0;
	MesquiteDouble temp1, temp2, temp3;
	double[] minSQLength;
	int numChars = 0;
	int numItems = 1;
	boolean reconstructed = false;
	boolean[] deleted;
	public SquaredReconstructor(){
		temp1 = new MesquiteDouble(0);
 		temp2 = new MesquiteDouble(0);
 		temp3 = new MesquiteDouble(0);
	}
	public void reconstruct(Tree tree, double[][] observedStates, boolean weighted, boolean rootedMode, boolean[] deletedTaxa){
		//NOTE: rootedMode == false not yet supported
		if (!rootedMode)
			MesquiteMessage.warnProgrammer("SquaredReconstructor error: rootedMode == false not yet supported");
		if (observedStates!=null && tree!=null && !(tree.nodeIsPolytomous(tree.getRoot()) && !rootedMode)){
			this.deleted = deletedTaxa;
			this.observedStates = observedStates;
			observedDistribution=null;
			useWeights = weighted;
			this.rootedMode = rootedMode;
			this.tree = tree;
			numChars = observedStates.length;
			numItems =1;
			doReconstruct();
 		}
 		else
 			reconstructed = false;
	}
	public void reconstruct(Tree tree, ContinuousDistribution observedDistribution, boolean weighted, boolean rootedMode, boolean[] deletedTaxa){
		if (!rootedMode)
			MesquiteMessage.warnProgrammer("SquaredReconstructor error: rootedMode == false not yet supported");
		if (observedDistribution!=null && tree!=null && !(tree.nodeIsPolytomous(tree.getRoot()) && !rootedMode)){
			this.deleted = deletedTaxa;
			this.observedDistribution = observedDistribution;
			observedStates=null;
			useWeights = weighted;
			this.rootedMode = rootedMode;
			this.tree = tree;
			numChars = 1;
			numItems = observedDistribution.getNumItems();
			doReconstruct();
 		}
 		else
 			reconstructed = false;
	}
	synchronized void doReconstruct(){
		
		
		
		if (downA==null || downA.length!=numChars || downA[0]==null || downA[0].length != tree.getNumNodeSpaces()){
			downA=  new double[numChars][tree.getNumNodeSpaces()];
			downB=  new double[numChars][tree.getNumNodeSpaces()];
			upA= new double[numChars][tree.getNumNodeSpaces()];
			upB= new double[numChars][tree.getNumNodeSpaces()];
			finalD=  new double[numChars][tree.getNumNodeSpaces()];
	 		finalC= new double[numItems][numChars][tree.getNumNodeSpaces()];
	 		minSQLength = new double[numChars];
 		}
 		else if (finalC==null || finalC.length!=numItems){
	 		finalC= new double[numItems][numChars][tree.getNumNodeSpaces()];
 		}
 		Double2DArray.zeroArray(downA);
 		Double2DArray.zeroArray(downB);
 		Double2DArray.zeroArray(upA);
 		Double2DArray.zeroArray(upB);
 		Double2DArray.zeroArray(finalD);
 		DoubleArray.zeroArray(minSQLength);
 
		for (int itemT = 0; itemT<numItems; itemT++) { //NOTE THIS relies on the variable item!  reentrancy problems could arise...
	 		item = itemT;
	 		if (statesLegal(tree, tree.getRoot(deleted))){
	 			Double2DArray.zeroArray(finalC[item]);
				minSQReconstruct(tree);
			}
			else {
	 			Double2DArray.deassignArray(finalC[item]);
 			}
		}
		reconstructed = true;
	}
	public double[][] getReconstructedStates(int item){
		
		if (reconstructed) {
			if (finalC !=null && item< finalC.length)
				return Double2DArray.clone(finalC[item]);
			MesquiteMessage.warnProgrammer("Error: item " + item + " requested  in getReconstructedStates of SquaredReconstructor, but that item was not calculated " + finalC);
		}
		return null;
	}
	public void placeReconstructedStates(ContinuousHistory statesAtNodes){
		if (statesAtNodes==null)
			return;
		if (tree == null || !reconstructed){
			statesAtNodes.deassignStates();
			return;
		}
		for (int item = 0; item<  numItems; item++) {
			transferStatesAtNode(tree.getRoot(deleted), tree, statesAtNodes, item);
		}
	}

	public   void transferStatesAtNode(int N, Tree tree, ContinuousHistory statesAtNodes, int item) {
		for (int d = tree.firstDaughterOfNode(N, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted))
			transferStatesAtNode(d, tree, statesAtNodes, item);
		statesAtNodes.setState(N, item, finalC[item][0][N]);
	}
	public boolean statesLegal(Tree tree, int node) {
		if (tree.nodeIsTerminal(node)) {
			for (int ic = 0; ic<numChars; ic++)
				if (!ContinuousState.isCombinable(getObservedState(item, ic, tree.taxonNumberOfNode(node))))
					return false;
			return true;
		}
		for (int d = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted))
			if (!statesLegal(tree, d))
				return false;
		return true;
	}
	 double getObservedState(int item, int ic, int it){
		if (observedStates==null)
			return observedDistribution.getState(it, item);
		else if (ic>=0 && it>= 0 && ic<observedStates.length && it< observedStates[0].length)
			return observedStates[ic][it];
		return 0;
	}
	public double[] getSumSquaredLengths(){
		return DoubleArray.clone(minSQLength);
	}
   	double Sqr(double d) {
   		return d * d;
   	}
	/*_____________________________________________________________________ */
	double branchWeight (Tree tree, int N){ 
		double weight;
		if (useWeights) {
			if (N == tree.getRoot(deleted)) { /*asking for weight across root for unrooted case*/
				if (rootedMode || tree.nodeIsPolytomous(tree.getRoot(deleted)))
					return 0;
				int L = tree.firstDaughterOfNode(N, deleted);
				int R = tree.lastDaughterOfNode(N, deleted);
				if (tree.branchLengthUnassigned(L, deleted))
					weight=branchWeight(tree,R);
				else if (tree.branchLengthUnassigned(R, deleted))
					weight=branchWeight(tree,L);
				else
					weight=tree.getBranchLength(L, deleted) +tree.getBranchLength(R, deleted);
			}
			else if (tree.branchLengthUnassigned(N, deleted)) 
				weight=1.0;
			else
				weight=tree.getBranchLength(N, deleted) ;
		}
		else
			weight=1.0;
			
		if (weight==0) {
			weight=1.0;
			if (alreadyWarnedZeroLength<5)
				MesquiteTrunk.mesquiteTrunk.discreetAlert(MesquiteThread.isScripting() || alreadyWarnedZeroLength>0, "Branch weight of zero found in SquaredReconstructor.  Will be treated as 1");
			alreadyWarnedZeroLength++;
		}
		return(weight);
	}
	/*.................................................................................................................*/
	void addABCdown (Tree tree, MesquiteDouble A,  MesquiteDouble B,  MesquiteDouble C, int N, double WtdN, int ic) {
		if (tree.nodeIsTerminal(N)){   /*terminal, add 1, -2b, c2*/
			double ts = getObservedState(item, ic, tree.taxonNumberOfNode(N));
			if (!MesquiteDouble.isCombinable(ts))
				return;
			A.add(1.0 / WtdN);
			B.subtract( 2.0 * (ts) / WtdN);
			C.add( ts*ts / WtdN);
		}
		else {   /*internal, add a/(a+1), b/(a+1), b2/(4(a+1))-c*/
			if (!MesquiteDouble.isCombinable(downA[ic][N]) || !MesquiteDouble.isCombinable(downB[ic][N]) || !MesquiteDouble.isCombinable(finalC[item][ic][N]))
				return;
			A.add(downA[ic][N] / (downA[ic][N] * WtdN + 1.0));
			B.add(downB[ic][N] / (downA[ic][N] * WtdN + 1.0));
			C.add(finalC[item][ic][N] - (Sqr(downB[ic][N]) / 4.0 / (downA[ic][N] + 1.0 / WtdN)));
		}
	}
	/*_____________________________________________________________________ */
	/*visits all descendants of dN, whether dN is polytomous or not, */
	/*and accumulates three parameters of downpass for dN*/
	void AddABCAllDescendants (Tree tree, MesquiteDouble A, MesquiteDouble B, MesquiteDouble C, int N, int ic) {
		int sisRight, lastDone;
		lastDone = tree.firstDaughterOfNode(N, deleted);
		addABCdown(tree, A, B, C, lastDone, branchWeight(tree,lastDone), ic);
		sisRight = tree.nextSisterOfNode(lastDone, deleted);
		while (tree.nodeExists(sisRight)) {
			addABCdown(tree, A, B, C, sisRight, branchWeight(tree,sisRight), ic);
			lastDone = sisRight;
			sisRight = tree.nextSisterOfNode(sisRight, deleted);
		}
	}
	/*_____________________________________________________________________ */
	/*..................The downpass of squared parsimony...........................*/
	void SquareDown (Tree tree, int N, int ic) {
		downA[ic][N] = MesquiteDouble.unassigned;/*initialize three parameters to 0*/
		downB[ic][N] = MesquiteDouble.unassigned;
		finalC[item][ic][N] = MesquiteDouble.unassigned;
		if (tree.nodeIsInternal(N)) {  
			for (int daughter = tree.firstDaughterOfNode(N, deleted); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter, deleted))
				SquareDown(tree, daughter, ic);
			if (tree.nodeExists(N)) {
				temp1.setToUnassigned();
				temp2.setToUnassigned();
				temp3.setToUnassigned();
				AddABCAllDescendants(tree, temp1, temp2, temp3, N, ic);
				downA[ic][N] = (temp1.getValue());/*initialize three parameters to 0*/
				downB[ic][N] = (temp2.getValue());
				finalC[item][ic][N] = (temp3.getValue());
			}
		}
	}
	/*_____________________________________________________________________ */
	/*This procedure adds downpass parameters of dN to running total of parameters A,B*/
	void AddABDown (Tree tree, MesquiteDouble A, MesquiteDouble B, int N, double wt, int ic) {
		if (tree.nodeIsTerminal(N))  {  /*Terminal, add 1, -2b*/
			double ts= getObservedState(item, ic, tree.taxonNumberOfNode(N));
			if (!MesquiteDouble.isCombinable(ts)) 
				return;
			A.add(1.0 / wt);
			B.add(-2.0 * (ts) / wt);
		}
		else  {/*internal, add a/(a+1), b/(a+1)*/
			double dA = downA[ic][N];
			if (!MesquiteDouble.isCombinable(dA) || !MesquiteDouble.isCombinable(downB[ic][N]))
				return;
			A.add( dA/ (dA * wt + 1));
			B.add( downB[ic][N] / (dA * wt + 1));
		}
	}
	/*_____________________________________________________________________ */
	/*..................The uppass & final pass of squared parsimony...........................*/
	void SquareFinal (Tree tree, int N, int ic) {
		int sister, sisright, lastdone, Nanc;
		if (N == tree.getRoot(deleted))  {
			for (int daughter = tree.firstDaughterOfNode(N, deleted); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter, deleted))
				SquareFinal(tree, daughter, ic);
			if (!rootedMode) 
				finalC[item][ic][N] = (MesquiteDouble.unassigned);
			else if (downA[ic][N] == 0)  {
				finalC[item][ic][N] = (0);
			}
			else if (MesquiteDouble.isCombinable(downA[ic][N]) && MesquiteDouble.isCombinable(downB[ic][N])&& MesquiteDouble.isCombinable(finalC[item][ic][N]))
				finalC[item][ic][N] = (downB[ic][N]/(-2.0* downA[ic][N]));  /*most parsimonious at root; p. 9 of resubmission*/
		}
		else if (tree.nodeIsInternal(N) && MesquiteDouble.isCombinable(downA[ic][N]) && MesquiteDouble.isCombinable(downB[ic][N])){
			/*now we need to poll descendant nodes and nodes below by adding up their contributions*/
			/*to the first and second parameters (final) at N*/
			/*FIRST look to contribution from ABOVE*/

			finalC[item][ic][N] = (downA[ic][N]);  /*additions from above already summed in nb->downA and downB*/
			finalD[ic][N] = (downB[ic][N]);

			/*Now look to contribution from BELOW*/
			 Nanc = tree.motherOfNode(N, deleted);
			if (Nanc == tree.getRoot(deleted)) {  /*ancestor is root*/
				if (rootedMode) { /*----- rooted ----*/
					/***since just above root, uppass states from below are just sum*/
					/*of downpass states from all sisters, which can be found by taking tree.getRoot()'s down and*/
					/*subtracting what had been N's contribution, so as to leave contribution of sisters.*/
					/*Unlike the case with other parsimony algorithms*/
					/*this can be done because everything is additive*/
					upA[ic][N] = (downA[ic][Nanc] - (downA[ic][N] / (downA[ic][N] * branchWeight(tree,N) +  1)));
					upB[ic][N] = (downB[ic][Nanc] - (downB[ic][N] / (downA[ic][N] * branchWeight(tree,N) +  1)));

					/*this calculation adds to finals the contribution from below*/
					/*(other side of root), using a/(a+1) etc to add effect of the root node*/
					finalC[item][ic][N] = (finalC[item][ic][N]+ upA[ic][N] / ( upA[ic][N] * branchWeight(tree,N) +  1));
					finalD[ic][N] = (finalD[ic][N] + upB[ic][N] / ( upA[ic][N] * branchWeight(tree,N) +  1));
				}
				else  {/*----- unrooted -----*/
					/*not rooted, thus use sister's downs directly, without extra calculation to add root*/
					 sister = tree.nextSisterOfNode(N, deleted);
					if (!tree.nodeExists(sister))
						sister = tree.previousSisterOfNode(N, deleted);

					if (tree.nodeIsTerminal(sister)) {
						upA[ic][N] = (-2); /*send signal that sister was terminal*/
						upB[ic][N] = getObservedState(item, ic, tree.taxonNumberOfNode(sister));/*store observed state in sister*/
					}
					else {
						upA[ic][N] = (downA[ic][sister]); 
						upB[ic][N] = (downB[ic][sister]);
					}
					/*now we add to finals the contribution from below*/
					temp1.setValue(finalC[item][ic][N]);
					temp2.setValue(finalD[ic][N]);
					double weight;
					if (tree.nodeIsPolytomous(tree.getRoot(deleted)))
						weight = branchWeight(tree,N);
					else
						weight = branchWeight(tree,tree.getRoot(deleted));
					AddABDown(tree, temp1, temp2, sister, weight, ic);  /*contribution across root*/
					finalC[item][ic][N] = (temp1.getValue());
					finalD[ic][N] = (temp2.getValue());
				}
			}
			else { /*N is not Root; Nanc is not Root; internal node*/
				if (upA[ic][Nanc] < -1.5) { /*receive signal that sister at root was terminal and unrooted*/
					upA[ic][N] = (1 / (branchWeight(tree,tree.getRoot(deleted))));  /*contribution from Anc(N); from terminal sister across unroot*/
					upB[ic][N] = (-2.0 * (upB[ic][Nanc]) / branchWeight(tree,tree.getRoot(deleted))); /*upB contained temporarily state in sister across unroot*/
				}
				else {
					upA[ic][N] = (upA[ic][Nanc] / (upA[ic][Nanc] * branchWeight(tree,Nanc) +  1));  /*contribution from Anc(N)*/
					upB[ic][N] = (upB[ic][Nanc] / (upA[ic][Nanc] * branchWeight(tree,Nanc) +  1) );
				}

				upA[ic][N] = (upA[ic][N] + downA[ic][Nanc] - (downA[ic][N] / (downA[ic][N]  * branchWeight(tree,N) +  1)));/*contribution from sisters*/
				upB[ic][N] = ( upB[ic][N] + downB[ic][Nanc] - (downB[ic][N]  / (downA[ic][N]  * branchWeight(tree,N) +  1)));  /*see comments above ** */
				finalC[item][ic][N] = (finalC[item][ic][N] + upA[ic][N] / (upA[ic][N] * branchWeight(tree,N) +  1));
				finalD[ic][N] = (finalD[ic][N]+ upB[ic][N] / (upA[ic][N] * branchWeight(tree,N) +  1));
			}

			/*NOW we find most parsimonious state*/
			if (finalC[item][ic][N] == 0)
				MesquiteMessage.println("Error in squared change parsimony (downA " + downA[ic][N] + " downB " + downB[ic][N] + " upA " + upA[ic][N] + " N " + N + " branchWeight " + branchWeight(tree,N) +  ")");
			else
				finalC[item][ic][N] = (finalD[ic][N] / (-2.0 * finalC[item][ic][N]));
			/*finalD = finalC[item];*/
			for (int daughter = tree.firstDaughterOfNode(N, deleted); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter, deleted))
				SquareFinal(tree, daughter, ic);
		}
		else  {/*Terminal node, assign observed state*/
			finalC[item][ic][N] = getObservedState(item, ic, tree.taxonNumberOfNode(N));
			/*if (finalC[item][ic][N]==MesquiteDouble.unassigned)  {
				MesquiteMessage.println("Sorry, the squared-change parsimony algorithm can't deal with missing data.  Results will contain errors.");
			}
			*/
		}
	}
	/*_____________________________________________________________________ */
	/*This procedure reconstructs continuous ancestral states using the squared-change*/
	/*parsimony criterion (see Maddison, 1991).  There are two options; either the root is taken as*/
	/*non-existant (unrooted) or as being a node (rooted)*/
	synchronized void minSQReconstruct (Tree tree) {
		
			MesquiteDouble tA = new MesquiteDouble(0);
			MesquiteDouble tB = new MesquiteDouble(0);
			MesquiteDouble tC = new MesquiteDouble(0);
			double SQlengthchecked, SQlengthcheckedWt;
			int root =  tree.getRoot(deleted);
			int rLeft =  tree.firstDaughterOfNode(root, deleted);
			int rRight =  tree.lastDaughterOfNode(root, deleted);
			for (int ic=0; ic<numChars; ic++){
				if (rootedMode) {/*rooted*/
					SquareDown(tree, root, ic);
					/*now need to calculate squared length*/
					if (downA[ic][root] == 0)
						minSQLength[ic] = 0;
					else
						minSQLength[ic] = finalC[item][ic][root]- Sqr(downB[ic][root]) / 4.0 / downA[ic][root];
				}
				else  {/*unrooted*/
					SquareDown(tree, rLeft, ic); /*note don't calculate down for root*/
					SquareDown(tree, rRight, ic);
					
					/*now need to calculate squared length*/
					if (tree.nodeIsInternal(rLeft)){   /*Note: Left(tree.getRoot()) can't be null with unrooted*/
						tA.setToUnassigned();
						tB.setToUnassigned();
						tC.setToUnassigned();
						AddABCAllDescendants(tree, tA, tB, tC,rLeft, ic);
						addABCdown(tree, tA, tB, tC, rRight, branchWeight(tree,root), ic);
						if (tA.getValue() == 0)
							minSQLength[ic] = 0;
						else
							minSQLength[ic] = tC.getValue() - Sqr(tB.getValue()) / 4.0 / tA.getValue();
					}
					else if (tree.nodeIsInternal(rRight)) {  /*Note: Right(tree.getRoot()) can't be null with unrooted*/
						tA.setToUnassigned();
						tB.setToUnassigned();
						tC.setToUnassigned();
						AddABCAllDescendants(tree, tA, tB, tC, rRight, ic);
						addABCdown(tree, tA, tB, tC, rLeft, branchWeight(tree,root), ic);
						if (tA.getValue() == 0)
							minSQLength[ic] = 0;
						else
							minSQLength[ic] = tC.getValue() - Sqr(tB.getValue()) / 4.0 / tA.getValue();
					}
					else /*Only a two taxon tree; squared length for unrooted is just square of difference*/
						minSQLength[ic] = Sqr(finalC[item][ic][rRight] - finalC[item][ic][rLeft]);
				}
				SquareFinal(tree, root, ic);
			}
	}
	
}

