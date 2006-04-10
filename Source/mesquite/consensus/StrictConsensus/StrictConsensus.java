/*
 * This software is part of the Strict Consensus Tree module for Mesquite,
 * written by Jeff Klingner and Silvio Neris.
 *
 * Copyright (c) 2002 by the University of Texas
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted under the GNU Lesser General
 * Public License, as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version,
 * provided that this entire notice is included in all copies of any
 * software which are or include a copy or modification of this software
 * and in all copies of the supporting documentation for such software.
 *
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR THE UNIVERSITY OF TEXAS
 * AT AUSTIN MAKE ANY REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE
 * MERCHANTABILITY OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 * IN NO CASE WILL THESE PARTIES BE LIABLE FOR ANY SPECIAL, INCIDENTAL,
 * CONSEQUENTIAL, OR OTHER DAMAGES THAT MAY RESULT FROM USE OF THIS SOFTWARE.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package mesquite.consensus.StrictConsensus;

import java.applet.*;
import java.util.*;
import java.awt.*;

import mesquite.consensus.common.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.util.HashMap;

public class StrictConsensus extends Consenser {

	HashMap rememberedPSWs;
//	WPM 05: make sure hashmaps aren't causing same bugs as in RFDistance.  My guess is that they are
	static final int initialHashTableCapacity = 2500;
	private LrnwStack stack;


	public String getName() { return "Strict Consensus"; }
	public String getVersion() { return "1.1"; }
	public String getYearReleased() { return "2002"; }
	public boolean showCitation() {	return true; }
	public String getPackageName() { return "Tree Comparison Package"; }
	public boolean getUserChoosable() { return true; } //WPM 06 set to true

	public boolean isPrerelease() { return false; }
	public boolean isSubstantive() { return true; }
	public String getCitation() { return "\n" + getYearReleased() + ". " + getAuthors() + "\n"; }
	public String getAuthors() { return "Jeff Klingner, The University of Texas at Austin, with Silvio Neris, City University of New York"; }

	public String getExplanation() {
		return	"The strict consensus tree contains only\n" +
				"those edges which are present in all of\n" +
				"the input trees.";
	}

	public boolean requestPrimaryChoice() { return true; } //pi - not quite what we want.



    public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		rememberedPSWs = new HashMap(initialHashTableCapacity);
		return true;
    }

    public Tree consense(TreeVector list, CommandRecord commandRec) {

		// The first step is to get PSW (postorder sequence with weights) representations for all of the trees
		Tree tempTree;
		PSWTree tempPSW;
		boolean allTreesHaveTwoChildRoots = true;
		for(int i = 0; i < list.getNumberOfTrees(); ++i) {
			tempTree = list.getTree(i);
			commandRec.tick("Majority Rules Consensus: putting tree " + (i+1));
			if (!rememberedPSWs.containsKey(tempTree)) {
				/* We have never seen this tree before.  Compute and save its PSV */
				tempPSW = new PSWTree(tempTree);
				rememberedPSWs.put(tempTree,tempPSW);
			}
			allTreesHaveTwoChildRoots &= checkChildrenOfRoot(tempTree);
		}

		/* compute a bipartition table for the first tree only */
		tempPSW = (PSWTree) rememberedPSWs.get(list.getTree(0));
		MarkedBipartitionTable bipTable = new MarkedBipartitionTable(tempPSW);

		/* mark edges in the bipartition table for removal if they are not found in all other trees */
		stack = new LrnwStack(tempPSW.getN() * 2);
		for (int i = 1; i< list.getNumberOfTrees(); ++i) {
			tempTree = list.getTree(i);
			tempPSW = (PSWTree) rememberedPSWs.get(tempTree);
			commandRec.tick("Majority Rules Consensus: checking partitions in tree " + (i+1));
			checkAgainstTable(tempPSW,bipTable);
			bipTable.clearUntouchedMarks();
		}

		/* finally, take any tree in the set, and contract all of its edges that are not found in the table */
		tempTree = list.getTree(0);
		MesquiteTree answer = tempTree.cloneTree();
		//System.out.println("\nbefore adjustment: " + answer.writeTree(MesquiteTree.BY_NAMES));

		answer.reroot(answer.nodeOfTaxonNumber(0),answer.getRoot(),true);
		//System.out.println("after rerooting: " + answer.writeTree(MesquiteTree.BY_NAMES));

		tempPSW = new PSWTree(answer);
		contractAgainstTable(answer,tempPSW,bipTable);
		//System.out.println("after table contraction: " + answer.writeTree(MesquiteTree.BY_NAMES));

		sortTree(answer);
		//System.out.println("after sorting: " + answer.writeTree(MesquiteTree.BY_NAMES));

		// remove the apparent node-zero based root if necessary
		if (!allTreesHaveTwoChildRoots) {
			answer.collapseBranch(answer.nextAroundUR(answer.getRoot(),answer.nodeOfTaxonNumber(0)),false);
		}
		//System.out.println("after collapsing pseudo-root: " + answer.writeTree(MesquiteTree.BY_NAMES));

		return answer;
    }

	private boolean checkChildrenOfRoot(Tree t) {
		return (t.daughtersOfNode(t.getRoot()).length == 2) && (t.firstDaughterOfNode(t.getRoot()) == t.nodeOfTaxonNumber(0));
	}

	private void checkAgainstTable(PSWTree tree, MarkedBipartitionTable table) {
		stack.empty();
		int[] lrnw,poppedLRNW; /* see description below at initialization */
		int[] vw; /* the current vertex,weight pair */
		int w; /* total weight unaccounted for beneath the current vertex */

		//System.out.println("Traversing " + rememberedTrees[tree2Index].getName() + " against table for " + rememberedTrees[tree1Index].getName());

		for (tree.prepareForEnumeration(), vw = tree.nextVertex(); vw != null; vw = tree.nextVertex()) {
			lrnw = new int[4];
			if (vw[1] == 0) { /* leaf vertex */
				lrnw[0] = table.encoding[vw[0]]; /* leftmost is this leaf */
				lrnw[1] = table.encoding[vw[0]]; /* rightmost is this leaf */
				lrnw[2] = 1; /* one leaf in this subtree */
				lrnw[3] = 1; /* subtree's total weight is one */
				stack.push(lrnw);
			} else { /* interior vertex; represents a bipartition */
				lrnw[0] = tree.getN() + 1; /* we want the smallest leaf among this vertex's children
									  with min(). This is bigger than any leaf */
				lrnw[1] = 0; /* likewise, smaller than ever leaf among my children */
				lrnw[2] = 0; /* start with a leaf count of zero */
				lrnw[3] = 1; /* will be total weight; start with one for this vertex */

				w = vw[1]; /* total weight below this vertex */
				while (w > 0) { /* while there are still children unaccounted for */
					poppedLRNW = stack.pop();
					lrnw[0] = Math.min(lrnw[0],poppedLRNW[0]); /* smallest #ed leaf below me */
					lrnw[1] = Math.max(lrnw[1],poppedLRNW[1]); /* largest #ed leaf below me */
					lrnw[2] = lrnw[2] + poppedLRNW[2]; /* total up the leaves below me */
					lrnw[3] = lrnw[3] + poppedLRNW[3]; /* total up the weight below me */
					w = w - poppedLRNW[3];
				}
				stack.push(lrnw);

				/* Now, for the subtree below this interior vertex, we have the total number
				   of leaves (N), and the smallest (L) and largest (R) #ed leaves (according
				   to the encoding used for tree1's bipartition table).  If the leaves below
				   this vertex form a contiguous sequence (i.e. if N=R-L+1) then we have a
				   candidate bipartiton that could be in tree 1.  If this is the case, we
				   check tree1's bipartition talbe for it and keep score accordingly. */
				if	(!(  // two of the clusters we get in the traversal are not bipartitions
						 (lrnw[0]==1 && lrnw[1] == tree.getN())   // cluster of everything but taxon zero - not a bipartition
					  || (lrnw[0]==1 && lrnw[1] == tree.getN()-1) // cluster that includes the whole tree - not a bipartition
					  )
					)
				{
					if ( lrnw[2] == lrnw[1] - lrnw[0] + 1 ) {/* if (N = R - L + 1) */
						table.touchBipartition(lrnw[0],lrnw[1]); // if bipartition is in the table, it is marked as touched
					}
				} // real cluster
			} // interior vertex
		} // tree traversal
	} // checkAgainstTable

	private void contractAgainstTable(MesquiteTree answer, PSWTree tree, MarkedBipartitionTable table) {
		stack.empty();
		int[] lrnw,poppedLRNW; /* see description below at initialization */
		int[] vw; /* the current vertex,weight pair */
		int w; /* total weight unaccounted for beneath the current vertex */

		int nodeNumber;

		//System.out.println("Traversing " + rememberedTrees[tree2Index].getName() + " against table for " + rememberedTrees[tree1Index].getName());

		for (tree.prepareForEnumeration(), vw = tree.nextVertex(); vw != null; vw = tree.nextVertex()) {
			lrnw = new int[4];
			if (vw[1] == 0) { /* leaf vertex */
				lrnw[0] = table.encoding[vw[0]]; /* leftmost is this leaf */
				lrnw[1] = table.encoding[vw[0]]; /* rightmost is this leaf */
				lrnw[2] = 1; /* one leaf in this subtree */
				lrnw[3] = 1; /* subtree's total weight is one */
				stack.push(lrnw);
			} else { /* interior vertex; represents a bipartition */
				lrnw[0] = tree.getN() + 1; /* we want the smallest leaf among this vertex's children
									  with min(). This is bigger than any leaf */
				lrnw[1] = 0; /* likewise, smaller than ever leaf among my children */
				lrnw[2] = 0; /* start with a leaf count of zero */
				lrnw[3] = 1; /* will be total weight; start with one for this vertex */

				w = vw[1]; /* total weight below this vertex */
				while (w > 0) { /* while there are still children unaccounted for */
					poppedLRNW = stack.pop();
					lrnw[0] = Math.min(lrnw[0],poppedLRNW[0]); /* smallest #ed leaf below me */
					lrnw[1] = Math.max(lrnw[1],poppedLRNW[1]); /* largest #ed leaf below me */
					lrnw[2] = lrnw[2] + poppedLRNW[2]; /* total up the leaves below me */
					lrnw[3] = lrnw[3] + poppedLRNW[3]; /* total up the weight below me */
					w = w - poppedLRNW[3];
				}
				stack.push(lrnw);

				/* Now, for the subtree below this interior vertex, we have the total number
				   of leaves (N), and the smallest (L) and largest (R) #ed leaves (according
				   to the encoding used for tree1's bipartition table).  If the leaves below
				   this vertex form a contiguous sequence (i.e. if N=R-L+1) then we have a
				   candidate bipartiton that could be in tree 1.  If this is the case, we
				   check tree1's bipartition talbe for it and keep score accordingly. */
				if	(!(  // two of the clusters we get in the traversal are not bipartitions
						 (lrnw[0]==1 && lrnw[1] == tree.getN())   // cluster of everything but taxon zero - not a bipartition
					  || (lrnw[0]==1 && lrnw[1] == tree.getN()-1) // cluster that includes the whole tree - not a bipartition
					  )
					)
				{
					if ( lrnw[2] == lrnw[1] - lrnw[0] + 1 ) {/* if (N = R - L + 1)  -- candidate bipartition */
						if (! table.containsMarkedBipartition(lrnw[0],lrnw[1])) {
							// this bipartition was not it all the trees.  contract its associated edge
							nodeNumber = vw[0] - (answer.getNumTaxa() + 1);
							answer.collapseBranch(nodeNumber, true);
						}
					}
				} // real cluster
			} // interior vertex
		} // tree traversal
	} // contractAgainstTable

	/* rearranges branches (by spinning nodes only) so that nodes in every subtree are sorted by node number */
	public void sortTree(Tree t) {
		recursiveSorter(t, t.getRoot());
	}

	private static int recursiveSorter(Tree t, int currentNode) {
		if (t.nodeIsTerminal(currentNode)) {
			return currentNode;
		} else {
			int numDaughters = t.numberOfDaughtersOfNode(currentNode);
			int[] daughterScores = new int[numDaughters];
			int daughterCounter = 0;
			int temp;
			for (int d = t.firstDaughterOfNode(currentNode); t.nodeExists(d); d = t.nextSisterOfNode(d)) {
				daughterScores[daughterCounter++] = recursiveSorter(t,d);
			}
			for (int i=1; i<numDaughters; i++) {
				for (int j= i-1; j>=0 && daughterScores[j] > daughterScores[j+1]; j--) {
					((MesquiteTree) t).interchangeBranches(t.indexedDaughterOfNode(currentNode, j),
					                                       t.indexedDaughterOfNode(currentNode, j+1), false);
					temp = daughterScores[j];
					daughterScores[j] = daughterScores[j+1];
					daughterScores[j+1] = temp;
				}
			}
			return daughterScores[0];
		}
	}

}


