/*
 * This software is part of the Tree Set Visualization module for Mesquite,
 * written by Jeff Klingner, Fred Clarke, and Denise Edwards.
 *
 * Copyright (c) 2002 by the University of Texas
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee under the GNU Public License is hereby granted, 
 * provided that this entire notice  is included in all copies of any software 
 * which is or includes a copy or modification of this software and in all copies
 * of the supporting documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR THE UNIVERSITY OF TEXAS
 * AT AUSTIN MAKE ANY REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE 
 * MERCHANTABILITY OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */
 
 
package mesquite.treecomp.RFtreeDifference;
/*~~  */

import java.applet.*;
import java.util.*;
import java.awt.*;

import mesquite.consensus.common.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/**
 * This is a reimplementation of RFtreeDifference, created by Wayne Maddison.  I wrote
 * it because the existing module was too slow for the the large number of calls required
 * by TreeSetVisualization.  I have tried to make this as fast as I could, sacrificing
 * unnecesary abstractions and coherence of style with the rest of Mesqutie for the sake
 * of space and time efficiency.
 *
 *@author Jeff Klingner, September 2001
*/
public class RFtreeDifference extends NumberFor2Trees {
	private static final int INITIAL_HASHMAP_CAPACITY = 500;

	/* Memoization variables */
	private HashMap PSWs;
	private HashMap bipTables;
	
	public String getName() { return "Robinson-Foulds Tree Difference"; }
	public String getVersion() { return "1.1"; }
	public String getYearReleased() { return "2002"; }
	public boolean showCitation() {	return true; }
	public String getPackageName() { return "Tree Comparison Package"; }
	public boolean getUserChoosable() { return false; }
	public boolean isPrerelease() { return false; }
	public boolean isSubstantive() { return true; }
	public String getCitation() { return "\n" + getYearReleased() + ". " + getAuthors() + "\n"; }
	public String getAuthors() { return "Jeff Klingner, The University of Texas at Austin"; }
	
	public String getExplanation() {
		return	"Calculates the Robinson-Foulds (Hamming) distance\n" +
				"between two trees.  This is the number of edges\n" + 
				"that is present in exactly one of the two trees.";
	}
	

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		PSWs = new HashMap(INITIAL_HASHMAP_CAPACITY);
		bipTables = new HashMap(INITIAL_HASHMAP_CAPACITY);
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}

	/*.................................................................................................................*/
	public void calculateNumber(Tree tree1, Tree tree2, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		int answer;

		/* Start by checking a couple preconditions. */
		if (result==null) {
			System.out.println("You passed an uninitialized result holder to calculateNumber().");
			return;
		}

		if (tree1.getTaxa() != tree2.getTaxa()) {
			System.out.println("RFtreeDifference only works for trees over the same Taxa.");
			return;
		}

		/* First, we check to see if we have seen either of the trees before. 
		 * If we haven't we compute a PSW tree and a bipartition table for them 
		 * For the calculation to come, we need a table for tree1 and a PSW for
		 * tree2
		 */
		MarkedBipartitionTable table; // actual table to be used in calculation
		PSWTree tree;			// actual tree to be used in calculation
		 
		PSWTree tempPSW;
		MarkedBipartitionTable tempTable;
		
		/*WPM Oct05  saving of trees in hashmap is disabled because it had two bugs:
		-- if tree seen before but was modified, then it failed to redo the calculations because pointers were the same
		-- all trees seen were saved, not purged (is that right?) leading to huge memory use, especially with simulations
		
		*/
		/* Check to see if we have seen the first tree or not */
		if (true || !PSWs.containsKey(tree1)) {
			tempPSW = new PSWTree(tree1);
			tempTable = new MarkedBipartitionTable(tempPSW);
		//WPM Oct05 disabled because this saved ALL TREES EVER SEEN and thus ran roughshod over memory	PSWs.put(tree1, tempPSW);
		//	WPM Oct05 disabled  bipTables.put(tree1, tempTable);
			table = tempTable;
		} else { // we have already seen this tree; retrieve its table from memory
			table = (MarkedBipartitionTable) bipTables.get(tree1);
		}
		
		/* Check to see if we have seen the second tree or not */
		if (true || !PSWs.containsKey(tree2)) {
			tempPSW = new PSWTree(tree2);
			//WPM Oct05 disabled  PSWs.put(tree2,tempPSW);
			//WPM Oct05 disabled  bipTables.put(tree2, new MarkedBipartitionTable(tempPSW));
			tree = tempPSW;
		} else {
			tree = (PSWTree) PSWs.get(tree2); //
		}


		/* To compute RF difference, we can't compare bipartition tables directly,
		   because the two tables may have been (and probably were) constructed using
		   two different leaf relabeling functions.  Instead we traverse one tree
		   (using it's PSW representation) and check for the bipartitions we find in
		   the other tree's bipartition table.  This is still O(n). */


		LrnwStack stack = new LrnwStack(tree.getN() * 2);
		stack.empty();
		int[] lrnw,poppedLRNW; /* see description below at initialization */
		int[] vw; /* the current vertex,weight pair */
		int w; /* total weight unaccounted for beneath the current vertex */
		int hits = 0; /* number of bipartitions in tree2 found in tree1 */
		int misses = 0; /* number of bipartitions in tree 2 not found in tree 1 */

		//System.out.println("Traversing " + tree2.getName() + " against table for " + tree1.getName());

		for (tree.prepareForEnumeration(), vw = tree.nextVertex();
			 vw != null;
			 vw = tree.nextVertex()) {
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
					  //|| (lrnw[0]==1 && lrnw[1] == tree.getN()-1) // cluster that includes the whole tree - not a bipartition
					  ) 
					)
				{
					if (   (lrnw[2] == lrnw[1] - lrnw[0] + 1) /* if (N = R - L + 1) */
						&& table.containsBipartition(lrnw[0],lrnw[1])) {
						hits++;
						 //System.out.println("Hit: <" + lrnw[0] + "," + lrnw[1] + ">");
					} else {
						misses++;
						 //System.out.println("Miss: <" + lrnw[0] + "," + lrnw[1] + ">");
					}
				}
			}
		}

		//System.out.println(hits + " hits, " + misses + " misses, " + table.getNumBipartitions() + " bipartitions in tabled tree");
		answer = misses + table.getNumBipartitions() - hits;

		/* Now fill the caller-supplied result holders. */
		result.setValue(answer);
		if (resultString!=null) {
			resultString.setValue("RF tree difference: " + result.toString() );
		}
		return;
	}


	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree t1, Tree t2, CommandRecord commandRec) {
	}
}






