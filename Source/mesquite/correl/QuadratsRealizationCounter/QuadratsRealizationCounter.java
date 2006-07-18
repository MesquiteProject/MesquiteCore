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

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class QuadratsRealizationCounter extends NumFor2CharHistAndTree {

	int dependentStart;
	int independentStart;
	private Vector [] quadrats;


	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		return true;
	} 

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		//temp.addLine("setSeed " + originalSeed); 
		
		return temp;
	}

	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(this.getClass(), "XXXXXXXXXX", "[long integer seed]", commandName, "XXXXXXXXXX")) {
			
		}
		else if (checker.compare(this.getClass(), "XXXXXXXXXX", "[number]", commandName, "XXXXXXXXXX")) {
			
		}
		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}


	public  void calculateNumber(Tree tree, CharacterHistory history1, CharacterHistory history2, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec){
		if (result == null)
			return;
		result.setToUnassigned();
		if (tree == null || history1 == null || history2 == null)
			return;
		if (!(history1 instanceof CategoricalHistory) || !(history2 instanceof CategoricalHistory)){
			if (resultString != null)
				resultString.setValue("Quadrats counting can't be done because one or both of the character are not categorical");
			return;
		}
	    
		//examine histories here
		countChanges(tree,(CategoricalHistory)history1,(CategoricalHistory)history2,result,resultString);
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

	
	private void countChanges(Tree tree, CategoricalHistory history1, CategoricalHistory history2,MesquiteNumber result, MesquiteString resultString){
		int independentRoot =  CategoricalState.minimum(history1.getState(tree.getRoot()));
		int dependentRoot =  CategoricalState.minimum(history2.getState(tree.getRoot()));
		
		quadrats = new Vector[tree.getNumNodeSpaces()];
		quadrats[tree.getRoot()] = new Vector(1);

		Quadrat rootExtension = new Quadrat(independentRoot,0.0);  // need to initialize this with root States
		quadrats[tree.getRoot()].add(upPass(tree,tree.getRoot(),history1, history2, rootExtension));
		//do something to add the histories
		BranchAccumulator total = new BranchAccumulator();
		total.sumBranches(tree,tree.getRoot());
		total.getResults(result,resultString);
	}
	
	Quadrat upPass(Tree tree,int node,CategoricalHistory history1, CategoricalHistory history2,Quadrat q){
		Quadrat myQuad;
		if (node != tree.getRoot()){
			myQuad = calculateOneBranch(tree, node, history1, history2,q);
		}
		else
			myQuad = q;
		if (tree.nodeIsInternal(node))
			for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d)){
				upPass(tree, d, history1, history2, myQuad);
			}
		return myQuad;
	}
		
	
	private Quadrat calculateOneBranch(Tree tree,int endNode,CategoricalHistory history1,CategoricalHistory history2,Quadrat q){
		//counters
		Vector independentEvents = history1.getInternodeHistoryVector(endNode);
		Vector dependentEvents = history2.getInternodeHistoryVector(endNode);
		int independentCount = independentEvents.size();
		int dependentCount = dependentEvents.size();
		//counting process
		if (independentCount == 1){   // no changes, this branch just extends the quadrat
			//CategInternodeEvent checkIndepEvent = (CategInternodeEvent)independentEvents.get(0);
			//if (checkIndepEvent.getChangeVersusSample())
			//	Debugg.println("No changes on this branch (true)");
			//else
			//	Debugg.println("No changes on this branch (false)");
			quadrats[endNode]= null;  // no new quadrats for this branch
			q.length++;  // so update the current one - note that distances are proportional to branch length
			Debugg.println("Triggered an addition, q.length is now: " + q.length); 
			int dependentIndex = 0;
			while(dependentIndex<dependentCount-1){
				dependentIndex++;
			}
			return q;                 // and return it
		}  // otherwise keep counting until the first shift in the independent history
		quadrats[endNode] = new Vector(independentCount);
		Quadrat currentQuad = q;
		int dependentIndex = 0;
		int independentIndex = 0;
        CategInternodeEvent curDependentEvent = (CategInternodeEvent)dependentEvents.get(dependentIndex);
		CategInternodeEvent curIndependentEvent = (CategInternodeEvent)independentEvents.get(independentIndex);
		while(true){
			while(curDependentEvent.getPosition() < curIndependentEvent.getPosition()){
				if (curDependentEvent.getChangeVersusSample()){  //check that it's a real change
					if (CategoricalState.minimum(curDependentEvent.getState()) == 0)
						currentQuad.dependent10Changes++;
					else
						currentQuad.dependent01Changes++;
				}
				if (dependentIndex == dependentCount){ // out of dependent events
					double lastPosition = ((CategInternodeEvent)independentEvents.lastElement()).getPosition();
					for(int i=independentIndex+1;i<independentEvents.size();i++){ //zero any additional quadrats
						lastPosition = ((CategInternodeEvent)independentEvents.get(i-1)).getPosition();
						CategInternodeEvent nextEvent = (CategInternodeEvent)independentEvents.get(i);
						if (nextEvent.getChangeVersusSample()) {   // make sure it's real
							//Debugg.println("New quadrat because out of dependent events: " + i );
							currentQuad = new Quadrat(CategoricalState.minimum(nextEvent.getState()),nextEvent.getPosition()-lastPosition);
							quadrats[endNode].add(currentQuad);
							lastPosition = nextEvent.getPosition();   // in case we bail
							//Debugg.println("(1)Added quadrat of length " + ((Quadrat)quadrats[endNode].lastElement()).length +
							//		" at " + (quadrats[endNode].size()-1));
						}
					}
					//Debugg.println("Spot check; emptyQ length is now " + currentQuad.length + " lastPosition = " + lastPosition);
					currentQuad.length += 1-lastPosition;
					//Debugg.println("Out of independent events (1); emptyQ length is now " + currentQuad.length);
					return currentQuad;
				}
				else
					curDependentEvent = (CategInternodeEvent)dependentEvents.get(dependentIndex++);
			}
			independentIndex++;
			if (independentIndex == independentCount){ // finish up
				// do we need to adjust length here?
				currentQuad.length += 1-((CategInternodeEvent)independentEvents.lastElement()).getPosition();
				//Debugg.println("Out of independent events (2); currentQuad length is now " + currentQuad.length);
				return currentQuad;
			}
			else{  // new quadrat
				double lastPosition = curIndependentEvent.getPosition();
				curIndependentEvent = (CategInternodeEvent)independentEvents.get(independentIndex);
				//Debugg.println("New quadrat because independentIndex was bumped" + independentIndex);
				//Debugg.println("current position is " +curIndependentEvent.getPosition() + "last position is " + lastPosition + " diff is " + (curIndependentEvent.getPosition()-lastPosition));
				currentQuad = new Quadrat(CategoricalState.minimum(curIndependentEvent.getState()),curIndependentEvent.getPosition()-lastPosition);
				quadrats[endNode].add(currentQuad);
				//Debugg.println("(2)Added quadrat of length " + ((Quadrat)quadrats[endNode].lastElement()).length +
			    //			" at " + (quadrats[endNode].size()-1));
			}
		}
	}
	
	
	// This holds overrun as states continue up past nodes
	private class Quadrat{
		int independentState;
		double length;
		int dependent10Changes = 0;
		int dependent01Changes = 0;
		
		Quadrat(int iState,double length){
			independentState = iState;
			this.length = length;
			dependent10Changes = 0;
			dependent01Changes = 0;
		}
	}
	
	
	private class BranchAccumulator{
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
			
		void addBranchSum(Vector quadrats, double myLength){
			for(int i=0;i<quadrats.size();i++){
				if (quadrats.get(i) instanceof Quadrat){
					Quadrat q = (Quadrat) quadrats.get(i);
					if (q != null){
						if (q.independentState == 0){  // check the colors
							totalWhiteChange10 += q.dependent10Changes;
							totalWhiteChange01 += q.dependent01Changes;
							totalWhiteLength += q.length;
							weightSumWhiteChange10 += q.dependent10Changes/(q.length*myLength);
							weightSumWhiteChange01 += q.dependent01Changes/(q.length*myLength);
							//Debugg.println("Quadrat length is " + q.length + " index is " + i);
						}
						else {
							totalBlackChange10 += q.dependent10Changes;
							totalBlackChange01 += q.dependent01Changes;
							totalBlackLength += q.length;
							weightSumBlackChange10 += q.dependent10Changes/(q.length*myLength);
							weightSumBlackChange01 += q.dependent01Changes/(q.length*myLength);
							//Debugg.println("Quadrat length is " + q.length + " index is " + i);
						}
					}
				}
			}
		}

		private void sumBranches(Tree tree,int node){
			if (quadrats[node] != null){  //might legitimately be null if no changes occurred in branch leading to node
				addBranchSum(quadrats[node],tree.getBranchLength(node,1.0));
			}
			if (tree.nodeIsInternal(node))
				for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d)){
					sumBranches(tree,d);
				}
		}

		
		void getResults(MesquiteNumber result,MesquiteString resultString) {
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
				Debugg.println(tmpBuffer.toString());
				resultString.append(tmpBuffer.toString());
			}
		}
	
	}
	



}


