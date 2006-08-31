/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison. 
 This module copyright 2006 P. Midford and W. Maddison

Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.correl.QuadratsRealizationCounter;

import java.util.Vector;

import pal.statistics.ChiSquareDistribution;

import JSci.maths.statistics.TDistribution;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class QuadratsRealizationCounter extends NumFor2CharHistAndTree {

	int dependentStart;
	int independentStart;
	private Vector quadrats;


	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		//todo: interface to specify what is counted (and compared) among quadrats.  Gains in the dependent variable? Losses? Both?
		return true;
	} 

	/*----------------------------------------------------*/
	boolean verbose = false;
	void println(String s){
		if (verbose)
			MesquiteMessage.println(s);
	}
	/*.................................................................................................................*/

	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getName() {
		return "Quadrats Correlation";
	}

	public String getExplanation(){
		return "Counts correlation in two realizations using the quadrats method";
	}

	public boolean isPrerelease(){
		return true;
	}


	/*----------------------------------------------------*/
	public  void calculateNumber(Tree tree, CharacterHistory history1, CharacterHistory history2, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec){
		if (result == null)
			return;

		result.setToUnassigned();
		if (tree == null || history1 == null || history2 == null)
			return;
		if (!(history1 instanceof CategoricalHistory) || !(history2 instanceof CategoricalHistory)){
			if (resultString != null)
				resultString.setValue("Quadrats counting can't be done because at least one of the characters is not categorical");
			return;
		}
		//examine histories here
		countChanges(tree,(CategoricalHistory)history1,(CategoricalHistory)history2,result,resultString);
	}
	/*----------------------------------------------------*/
	private void countChanges(Tree tree, CategoricalHistory history1, CategoricalHistory history2,MesquiteNumber result, MesquiteString resultString){
		println("INDEP EVENTS=======");
		showEvents(tree, tree.getRoot(), history1);
		println("DEP EVENTS=======");
		showEvents(tree, tree.getRoot(), history2);
		println("==============");

		Vector independentEvents = history1.getInternodeHistoryVector(tree.getRoot());
		CategInternodeEvent curIndependentEvent = (CategInternodeEvent)independentEvents.elementAt(0);
		int independentRoot =  CategoricalState.minimum(curIndependentEvent.getState());

		if (quadrats == null)
			quadrats = new Vector();
		else 
			quadrats.removeAllElements();
		Quadrat rootQuadrat = new Quadrat(independentRoot,0.0, tree.getRoot());  // need to initialize this with root States
		quadrats.add(rootQuadrat);
		upPass(tree,tree.getRoot(),history1, history2, rootQuadrat);


		//do something to add the histories
		ResultsAccumulator total = new ResultsAccumulator();
		total.getResults(quadrats, result,resultString);
	}

	/*----------------------------------------------------*/
	private void upPass(Tree tree,int node,CategoricalHistory history1, CategoricalHistory history2,Quadrat q){
		Quadrat myQuad;
		if (node != tree.getRoot())
			myQuad = calculateOneBranch(tree, node, history1, history2,q);
		else
			myQuad = q;
		if (tree.nodeIsInternal(node))
			for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d))
				upPass(tree, d, history1, history2, myQuad);
	}
	/*----------------------------------------------------*/
	private Quadrat calculateOneBranch(Tree tree,int endNode,CategoricalHistory history1,CategoricalHistory history2,Quadrat q){
		//counters
		Vector independentEvents = history1.getInternodeHistoryVector(endNode);
		Vector dependentEvents = history2.getInternodeHistoryVector(endNode);
		int independentCount = independentEvents.size();
		int dependentCount = dependentEvents.size();
		double branchLength = tree.getBranchLength(endNode, 1.0);
		//counting process
		if (independentCount == 1){   // no changes, this branch just extends the quadrat
			q.length += branchLength;  //PETER: this had been just q.length++, but it needs to add actual length, correct? // so update the current one - note that distances are proportional to branch length
			println("Triggered an addition, q.length is now: " + q.length); 
			int dependentIndex = 0;
			while(dependentIndex<dependentCount){  //PETER: this had been dependentCount-1, but then some events were not examined; OK?
				CategInternodeEvent curDependentEvent = (CategInternodeEvent)dependentEvents.elementAt(dependentIndex++);
				if (curDependentEvent.getChangeVersusSample()){  //check that it's a real change
					if (CategoricalState.minimum(curDependentEvent.getState()) == 0)
						q.dependent10Changes++;
					else
						q.dependent01Changes++;
				}
				//dependentIndex++; PETER: this was already incremented above when element was obtained; OK?
			}
			return q;                 // and return it
		}  

		// otherwise keep counting until the first shift in the independent history
		Quadrat currentQuad = q;
		int dependentIndex = 0;
		int independentIndex = 0;
		CategInternodeEvent curDependentEvent = (CategInternodeEvent)dependentEvents.elementAt(dependentIndex);
		CategInternodeEvent curIndependentEvent = (CategInternodeEvent)independentEvents.elementAt(independentIndex);
		while(true){ //keep cycling through all the independent events

			//go through all dependent events that are before the next independent event. These belong to the current quadrat
			while(curDependentEvent.getPosition() < curIndependentEvent.getPosition()){
				if (curDependentEvent.getChangeVersusSample()){  //check that it's a real change
					if (CategoricalState.minimum(curDependentEvent.getState()) == 0)
						currentQuad.dependent10Changes++;
					else
						currentQuad.dependent01Changes++;
				}

				if (dependentIndex == dependentCount){ // out of dependent events; thus any subsequent indep events don't contain dependent, and thus are empty quadrats; need to deal with them
					double lastPosition = ((CategInternodeEvent)independentEvents.lastElement()).getPosition();
					for(int i=independentIndex+1;i<independentEvents.size();i++){ //zero any additional quadrats
						lastPosition = ((CategInternodeEvent)independentEvents.elementAt(i-1)).getPosition();
						CategInternodeEvent nextEvent = (CategInternodeEvent)independentEvents.elementAt(i);
						if (nextEvent.getChangeVersusSample()) {   // make sure it's real
							println("New quadrat because out of dependent events: " + i );
							//PETER: note consideration of branch length in quadrat length below; OK?
							currentQuad = new Quadrat(CategoricalState.minimum(nextEvent.getState()),(nextEvent.getPosition()-lastPosition)*branchLength, endNode);
							quadrats.add(currentQuad);
							lastPosition = nextEvent.getPosition();   // in case we bail
							println("(1)Added quadrat of length " + currentQuad.length +
									" at node " + endNode);
						}
					}
					println("Spot check; emptyQ length is now " + currentQuad.length + " lastPosition = " + lastPosition);
					//	PETER: note consideration of branch length in quadrat length below; OK?
					currentQuad.length += (1-lastPosition)*branchLength;
					println("Out of independent events (1); emptyQ length is now " + currentQuad.length);
					return currentQuad;
				}
				else{
					curDependentEvent = (CategInternodeEvent)dependentEvents.elementAt(dependentIndex++);
				}
			} //end cycle of dependent events before next independent event


			independentIndex++;
			if (independentIndex == independentCount){ // finish up
				// do we need to adjust length here?
				//	 PETER: note consideration of branch length in quadrat length below; OK?
				currentQuad.length += (1-((CategInternodeEvent)independentEvents.lastElement()).getPosition())*branchLength;
				println("Out of independent events (2); currentQuad length is now " + currentQuad.length);
				return currentQuad;
			}
			else{  // new quadrat
				double lastPosition = curIndependentEvent.getPosition();
				curIndependentEvent = (CategInternodeEvent)independentEvents.elementAt(independentIndex);
				println("New quadrat because independentIndex was bumped" + independentIndex);
				println("current position is " +curIndependentEvent.getPosition() + "last position is " + lastPosition + " diff is " + (curIndependentEvent.getPosition()-lastPosition));

				//	PETER: first need to add remnant length to previous quadrat; OK?
				currentQuad.length += (curIndependentEvent.getPosition()-lastPosition)*branchLength;
				//	PETER: but the next quadrat, we don't know how long it will be, so we start it at 0; OK?  Its length will be corrected when next indep event found or run out of indep events
				currentQuad = new Quadrat(CategoricalState.minimum(curIndependentEvent.getState()),0.0, endNode);
				quadrats.add(currentQuad);
				println("(2)Added quadrat of length " + currentQuad.length +
						" at node " + endNode);
			}
		}
	}
	/*----------------------------------------------------*/
	private void showEvents(Tree tree,int node,CategoricalHistory history){
		Vector events = history.getInternodeHistoryVector(node);
		for (int i=0; i<events.size(); i++){
			CategInternodeEvent curEvent = (CategInternodeEvent)events.elementAt(i);
			println("at node  " + node + " EVENT " + curEvent);
		}
		if (tree.nodeIsInternal(node))
			for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d))
				showEvents(tree, d, history);
	}

	/*=======================================================*/
	// This holds overrun as states continue up past nodes
	private class Quadrat{
		int independentState;
		double length;
		int dependent10Changes = 0;
		int dependent01Changes = 0;
		int node = 0;

		Quadrat(int iState,double length, int node){
			independentState = iState;
			this.length = length;
			this.node = node;
			dependent10Changes = 0;
			dependent01Changes = 0;
		}
		public String toString(){
			return "Quadrat: indep state: " + independentState + "; length " + MesquiteDouble.toString(length, 4) + "; dep 01 changes " + dependent01Changes  + "; dep 10 changes " + dependent10Changes  + "; starting on node " + node;
		}
	}

	/*=======================================================*/

	private class ResultsAccumulator{
		static final int weightedTTest = 0;
		static final int pooledAreasTest = 1;
		int totalWhiteChange10;
		int totalWhiteChange01;
		int totalBlackChange10;
		int totalBlackChange01;

		double totalWhiteLength;
		double totalBlackLength;

		double weightSumWhiteChange01;		
		double weightSumWhiteChange10;
		double weightSumBlackChange10;
		double weightSumBlackChange01;

		void listQuadrats(Vector quadrats){
			for(int i=0;i<quadrats.size();i++){
				if (quadrats.elementAt(i) instanceof Quadrat){
					Quadrat q = (Quadrat) quadrats.elementAt(i);
					if (q != null){
						println("" + q.toString());
					}
				}
			}	
		}
		
		void sumQuadrats(Vector quadrats){

			for(int i=0;i<quadrats.size();i++){
				if (quadrats.elementAt(i) instanceof Quadrat){
					Quadrat q = (Quadrat) quadrats.elementAt(i);
					if (q != null){
						if (q.independentState == 0){  // check the colors
							totalWhiteChange10 += q.dependent10Changes;
							totalWhiteChange01 += q.dependent01Changes;
							totalWhiteLength += q.length;
							weightSumWhiteChange10 += q.dependent10Changes/(q.length);
							weightSumWhiteChange01 += q.dependent01Changes/(q.length);
							//println("Quadrat length is " + q.length + " index is " + i);
						}
						else {
							totalBlackChange10 += q.dependent10Changes;
							totalBlackChange01 += q.dependent01Changes;
							totalBlackLength += q.length;
							weightSumBlackChange10 += q.dependent10Changes/(q.length);
							weightSumBlackChange01 += q.dependent01Changes/(q.length);
							//println("Quadrat length is " + q.length + " index is " + i);
						}
					}
				}
			}
		}


		double weightedTTest(Vector quadrats){
			int[] count = new int[2];
			double[] weightedMean = new double[2];
			double[] wtdSumSquares = new double[2];
			//calculating weighted mean, weighted standard deviation
			for (int state =0; state<2; state++){
				double sum = 0;
				double sumWeights = 0;
				double sumSquares = 0;
				count[state] = 0;
				for(int i=0;i<quadrats.size();i++){
					if (quadrats.elementAt(i) instanceof Quadrat){
						Quadrat q = (Quadrat) quadrats.elementAt(i);
						if (q != null && q.independentState == state){
							count[state]++;
							double changeDensity = (q.dependent10Changes + q.dependent01Changes)/q.length;
							sum += q.length * changeDensity;
							sumWeights += q.length;
							sumSquares += q.length * changeDensity*changeDensity;
						}
					}
				}	
				if (sumWeights==0)
					return 0;
				weightedMean[state] = sum/sumWeights;
				wtdSumSquares[state] = count[state]*(sumSquares/sumWeights -  (weightedMean[state]*weightedMean[state]));

				double wtdVariance = (wtdSumSquares[state])/(count[state]-1);
				double wtdStandardDeviation =  Math.sqrt(wtdVariance);
			}
			if (count[0] == 0 || count[1] == 0)
				return 0;
			if (count[0]+count[1] == 2)
				return 1;
			double pooledVariance = (wtdSumSquares[0] + wtdSumSquares[1])/(count[0]+count[1]-2);
			double stdErrorDiffMeans = Math.sqrt(pooledVariance/(1.0/count[0] + 1.0/count[1]));
			double diffMeans = weightedMean[0]-weightedMean[1];
			TDistribution t = new TDistribution(count[0]+count[1]-2);
			double p =  (1-(t.cumulative(diffMeans/stdErrorDiffMeans)));
			if (p <0.5)
				p *= 2;  //two tailed?  what if direction switches among replicates versus stays the same?
			return p;
			//difference = diffMeans; error = stdErrorDiffMeans; degrees of freedom = (count0+count1-2);
		}

		void getResults(Vector quadrats, MesquiteNumber result,MesquiteString resultString) {
			/* PETER:
			 * Possibilities for statistics (gains/losses refer to dep variable)
			 * (1) Count number of gains and losses in total black versus white area.  The null hypothesis would have 
			 *      the number of changes in black versus white to be proportional to relative sizes of black versus white areas
			 *      Is this the calculation below?  This is basically a version of the concentrated changes test.
			 *      Its drawback is that it doesn't consider the quadrats separately, and thus would fall for the trap of a single 
			 *      black clade contributing a significant effect.  This trap is what we hope to avoid using the quadrats.
			 *      This is good as a point of comparison, but it is not what we want for our new method.
			 * (2) Consider each quadrat as a sample point, and ask: do black quadrats have on average a higher density of gains/losses 
			 *      than white quadrats?  This could be done by a t test, or a mann whitney u test, or whatever.  This does consider 
			 *      quadrats independently, but it doesn't consider the fact that some quadrats will have a better estimate of density by virtue
			 *      of being bigger.
			 * (3) Find a statistical test that considers the counts in each quadrat as estimates, with a "mean" (the count of gains/losses) and a "variance" or error
			 *      (related to quadrat size).  Leticia uses weighted analysis of variance frequently, where the observations (the "means") are weighted by their "certainty".
			 *      One can weight by sample size, or by inverse of variance (or std. dev.?).  This is what Dolph suggested also.  For a comparison of only two treatments, 
			 *      anova may be overkill, and I found this: http://bmj.bmjjournals.com/cgi/content/full/316/7125/129  which describes a weighted t test for a medical
			 *      case very much like ours.
			 *      What do we weight by?  If in the discrete sample case weighting by sample size makes sense, then so would weighting by inverse of standard deviation.  
			 *      For areas and densities, then, we would want to weight by the inverse of the error of the estimate of the density.  Is the error on the estimate of a density
			 *      is proportional to area sampled  (e.g. estimating poisson mean from a quadrat of a certain area)?  If so then we would just weight by area.
			 *      
			 * What I've done for now is the Bland & Kerry method, weighting by area.   
			 * 
			 **/
			int test = weightedTTest; //make user interface for choosing test


			if (test == weightedTTest){
				//listQuadrats(quadrats);
				double p = weightedTTest(quadrats);
				result.setValue(p);
				resultString.setValue("weighted t test");
			}
			else if (test == pooledAreasTest){
				sumQuadrats(quadrats);
				double[][] ef = new double[2][2];
				double[][] of = new double[2][2];
				double whiteFrac = totalWhiteLength/(totalWhiteLength+totalBlackLength);
				double blackFrac = totalBlackLength/(totalWhiteLength+totalBlackLength);
				int allChange = totalWhiteChange10 + totalWhiteChange01 + totalBlackChange10 + totalBlackChange01;
				ef[0][0] = 0.5*allChange*whiteFrac;
				ef[0][1] = 0.5*allChange*whiteFrac;
				ef[1][0] = 0.5*allChange*blackFrac;
				ef[1][1] = 0.5*allChange*blackFrac;
				of[0][0] = totalWhiteChange10;
				of[0][1] = totalWhiteChange01;
				of[1][0] = totalBlackChange10;
				of[1][1] = totalBlackChange01;

				if (allChange == 0)
					MesquiteMessage.warnProgrammer("Total changes counted = 0");
				if (blackFrac == 0)
					MesquiteMessage.warnProgrammer("No black changes counted");
				if (whiteFrac == 0)
					MesquiteMessage.warnProgrammer("No white changes counted");

				double chisqStat = ((of[0][0]-ef[0][0])*(of[0][0]-ef[0][0]))/ef[0][0];
				chisqStat += ((of[0][1]-ef[0][1])*(of[0][1]-ef[0][1]))/ef[0][1];
				chisqStat += ((of[1][0]-ef[1][0])*(of[1][0]-ef[1][0]))/ef[1][0];
				chisqStat += ((of[1][1]-ef[1][1])*(of[1][1]-ef[1][1]))/ef[1][1];

				result.setValue(1-ChiSquareDistribution.cdf(chisqStat,1.0));

				if (resultString != null){
					StringBuffer tmpBuffer = new StringBuffer(300);
					tmpBuffer.append("total white changes 1->0: " + totalWhiteChange10);
					tmpBuffer.append(" total white changes 0->1: " + totalWhiteChange01);
					tmpBuffer.append(" total white length: " + totalWhiteLength);
					tmpBuffer.append(" total black changes 1->0: " + totalBlackChange10);
					tmpBuffer.append(" total black changes 0->1: " + totalBlackChange01);
					tmpBuffer.append(" total black length: " + totalBlackLength);
					//tmpBuffer.append(" total length (check): " + (totalWhiteLength + totalBlackLength));
					tmpBuffer.append(" Chi-square stat = " + chisqStat);
					tmpBuffer.append(" pvalue (1 df) = " + result.getDoubleValue());
					println(tmpBuffer.toString());
					resultString.append(tmpBuffer.toString());
				}
			}
		}

	}




}


