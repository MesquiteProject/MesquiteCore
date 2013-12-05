package mesquite.parsimony.lib;

import mesquite.categ.lib.CategoricalHistory;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.Long2DArray;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Tree;
import mesquite.lib.characters.*;

public class MPRProcessor {
	CharacterHistory history = null;
	long[][] numMPRsForStates = null;
	boolean overflow = false;

	public MPRProcessor(CharacterHistory history) {
		this.history = history;
		overflow=false;
	}

	/*.................................................................................................................*/
	private void assignMPRNumberArray(Tree tree, int node) {
		checkArrayIntegrity(tree);

		long nodeSet = ((CategoricalHistory)history).getState(node);
		for (int ist = 0; ist<= CategoricalState.maxCategoricalState; ist++){
			if (CategoricalState.isElement(nodeSet,ist)) {   // ist is in the MPR set
				numMPRsForStates[node][ist]=1;
			}
		}
		if (!tree.nodeIsTerminal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				assignMPRNumberArray(tree,d);
				for (int ist = 0; ist<= CategoricalState.maxCategoricalState; ist++){
					if (CategoricalState.isElement(nodeSet,ist)) {   // ist is in the MPR set
						long daughterMPR = ((CategoricalHistory)history).getConditionalStateSet(d,ist);
						long fixedNum = 0;
						for (int e = 0; e<= CategoricalState.maxCategoricalState; e++){
							if (CategoricalState.isElement(daughterMPR,e)) {   // e is in the MPR set
								fixedNum+=numMPRsForStates[d][e];
							}
						}
						
						numMPRsForStates[node][ist]=numMPRsForStates[node][ist]*fixedNum;
						if (numMPRsForStates[node][ist]<0)
							overflow=true;
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	private long numMPRsForNode(Tree tree, int node) {
		assignMPRNumberArray(tree,node);
		long nodeMPRs=0;
		for (int e = 0; e<= CategoricalState.maxCategoricalState; e++){
			nodeMPRs+=numMPRsForStates[node][e];
		}
		if (nodeMPRs<0)
			overflow=true;
		if (overflow) {
			MesquiteMessage.println("Number of MPRs exceeds that allowed.");
			return Long.MAX_VALUE;
		}

		return nodeMPRs;
	}
	private void checkArrayIntegrity (Tree tree) {
		if (numMPRsForStates==null || numMPRsForStates.length!= tree.getNumNodeSpaces())
			numMPRsForStates = new long[tree.getNumNodeSpaces()][CategoricalState.maxCategoricalState+1];
	}
	/*.................................................................................................................*/
	public long getNumResolutions(Tree tree) {
		return getNumResolutions(tree, tree.getRoot());
	}

	/*.................................................................................................................*/
	public long getNumResolutions(Tree tree, int node) {
		overflow=false;
		checkArrayIntegrity(tree);
		Long2DArray.zeroArray(numMPRsForStates);
		if (((CategoricalHistory)history).hasConditionalStateSets()) {
			return numMPRsForNode(tree,node);
		}
		return MesquiteInteger.unassigned;
	}

	/*.................................................................................................................*/

	long numMPRs = 0;
	/** Note:  whichMPR is 1-based! */

	private long totalMPRsAtNodeGivenMPRSet(int node, long nodeSet) {
		long mprTotalInNode = 0;
		for (int ist = 0; ist<= CategoricalState.maxCategoricalState; ist++){
			if (CategoricalState.isElement(nodeSet,ist)) {   // ist is in the MPR set
				mprTotalInNode +=numMPRsForStates[node][ist];
			}
		}
		if (mprTotalInNode<0)
			overflow=true;
		if (overflow) {
			MesquiteMessage.println("Number of MPRs exceeds that allowed.");
			return Long.MAX_VALUE;
		}
		return mprTotalInNode;
	}

	/*.................................................................................................................*/
	public void setMPR(Tree tree, CategoricalHistory results, long whichMPR, long totalRemaining, int node) {
//		first, let's figure out the MPRset for this node given what it's mother is fixed to
		int stateOfNode = MesquiteInteger.unassigned;
		int lastState = MesquiteInteger.unassigned;
		long nodeSet;
		if (node!=tree.getRoot()) {
			long motherNodeSet = results.getState(tree.motherOfNode(node));
			int motherNodeState = CategoricalState.getOnlyElement(motherNodeSet);
			nodeSet = ((CategoricalHistory)history).getConditionalStateSet(node,motherNodeState);
		} else
			nodeSet = results.getState(node);

//		now let's figure out in which of the states of the MPRset our chosen MPR resides
		long totalMPRsInCladeIfFixed = 0;
		long numMPRsInStatesCumulative = 0;
		long whichMPROnceFixed = 0;
		for (int ist = 0; ist<= CategoricalState.maxCategoricalState; ist++){
			if (CategoricalState.isElement(nodeSet,ist)) {   // ist is in the MPR set
				lastState=ist;
				numMPRsInStatesCumulative +=numMPRsForStates[node][ist];

				if (numMPRsInStatesCumulative>=whichMPR) {
					numMPRsInStatesCumulative = numMPRsInStatesCumulative - numMPRsForStates[node][ist];
					totalMPRsInCladeIfFixed = numMPRsForStates[node][ist];
					whichMPROnceFixed = whichMPR-numMPRsInStatesCumulative;
					results.setState(node, CategoricalState.makeSet(ist));
					stateOfNode = ist;
					break;
				}
			}
		}

//		if our MPR is in the last bin, then we need to calculate the values for it		
		if (!MesquiteInteger.isCombinable(stateOfNode) && MesquiteInteger.isCombinable(lastState)) {
			numMPRsInStatesCumulative = numMPRsInStatesCumulative - numMPRsForStates[node][lastState];
			totalMPRsInCladeIfFixed = numMPRsForStates[node][lastState];
			whichMPROnceFixed = whichMPR-numMPRsInStatesCumulative;
			results.setState(node, CategoricalState.makeSet(lastState));
			stateOfNode = lastState;
		}

		// now let's cycle through the daughters, and figure out how much each daughter gets out of the pie, and which MPR to use within each daughter
		long totalMPRsPerMPRInNode = totalMPRsInCladeIfFixed;
		if (!tree.nodeIsTerminal(node)){
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				long daughterNodeSet = ((CategoricalHistory)history).getConditionalStateSet(d,stateOfNode);
				long mprsInDaughter = totalMPRsAtNodeGivenMPRSet(d,daughterNodeSet);

				if (mprsInDaughter==0) {
					totalMPRsPerMPRInNode = Long.MAX_VALUE;
					MesquiteTrunk.mesquiteTrunk.discreetAlert("Number of MPRs exceeds that allowed - calculations can not be completed.");
					return;
				}
				else 
					totalMPRsPerMPRInNode = totalMPRsPerMPRInNode/mprsInDaughter;

				long whichMPRInDaughter;
				if (totalMPRsPerMPRInNode==0) {
					whichMPRInDaughter = Long.MAX_VALUE;
					MesquiteTrunk.mesquiteTrunk.discreetAlert("Number of MPRs exceeds that allowed - calculations can not be completed.");
					return;
				}
				else{
					whichMPRInDaughter = whichMPROnceFixed / totalMPRsPerMPRInNode;  
					if (whichMPROnceFixed % totalMPRsPerMPRInNode!=0) 
						whichMPRInDaughter++;
				}
				setMPR(tree,results, whichMPRInDaughter, mprsInDaughter, d);
				if (totalMPRsPerMPRInNode==0) {
					whichMPROnceFixed = Long.MAX_VALUE;
					MesquiteTrunk.mesquiteTrunk.discreetAlert("Number of MPRs exceeds that allowed - calculations can not be completed.");
					return;
				}
				whichMPROnceFixed=whichMPROnceFixed % totalMPRsPerMPRInNode+1;   // have to take the remainder and convert it to 1-based; this is for the next daughter
			}
		} 

	}
	

	/** Returns in results the character history that is the resolution "resolutionNumber" */
	/*.................................................................................................................*/
	public CharacterHistory getResolution(Tree tree, int node, CharacterHistory results, long resolutionNumber) {
		if (tree==null || resolutionNumber<0) 
			return null;
		long numMPRs = getNumResolutions(tree, node);
		if (resolutionNumber>=numMPRs || overflow)
			return null;
		results=((CategoricalHistory)history).adjustHistorySize(tree,results);
		history.clone(results);
		setMPR(tree, (CategoricalHistory)results,resolutionNumber+1, numMPRs, node);
		return results;

	}
	/** Returns in results the character history that is the resolution "resolutionNumber" */
	/*.................................................................................................................*/
	public CharacterHistory getResolution(Tree tree, CharacterHistory results, long resolutionNumber) {
	 return getResolution(tree,tree.getRoot(), results, resolutionNumber);
	}
	

}
