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
 
package mesquite.treecomp.WeightedRFtreeDifference;
/*~~  */

import java.util.*;

import mesquite.consensus.common.*;
import mesquite.lib.*;
import mesquite.lib.duties.NumberFor2Trees;

/**
 * This is a weighted version of RFtreeDifference.  In addition to considering which
 * edges are present/absent in each of the two trees, it also uses their weights.
 *
 *TODO: insert definition of weighted RF
 *	Jeff Klingner
	Center for Computational Biology and Bioinformatics
	The University of Texas at Austin
	April 2002
*/

public class WeightedRFtreeDifference extends NumberFor2Trees {
	private static final int INITIAL_HASHMAP_CAPACITY = 500;

	/* Memoization variables */
	private HashMap PSWs;
	private HashMap bipTables;
//	WPM 05: make sure hashmaps aren't causing same bugs as in RFDistance.  My guess is that they are
	
	public String getName() { return "Weighted Robinson-Foulds Tree Difference"; }
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
		return	"Calculates the Weighted Robinson-Foulds distance\n" +
				"between two trees.  This is just like the\n" +
				"Robinson-Foulds distance, except the difference\n" +
				"is weighted by the branch lengths of the tree.\n" +
				"If a very long branch is present in one tree but\n" +
				"missing from the other, it will contribute more to\n" +
				"the total difference than similarly placed short\n" +
				"branch would.  Missing branch lengths in the input\n" +
				"are treated as having unit length.";
	}
	


	/** Mesquite modules do their initialization here, instead of in a constructor */
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		PSWs = new HashMap(INITIAL_HASHMAP_CAPACITY);
		bipTables = new HashMap(INITIAL_HASHMAP_CAPACITY);
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}

	/** The main event.  Defining this method makes this a NumberFor2Trees module*/
	public void calculateNumber(Tree tree1, Tree tree2, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {

		/* Start by checking a couple preconditions. */
		if (result==null) {
			System.out.println("You passed an uninitialized result holder to calculateNumber().");
			return;
		}

		if (tree1.getTaxa() != tree2.getTaxa()) {
			System.out.println("WeightedRFtreeDifference only works for trees over the same Taxa.");
			return;
		}

/* First, we check to see if we have seen either of the trees before. 
		 * If we haven't we compute a PSW tree and a bipartition table for them 
		 * For the calculation to come, we need a table for tree1 and a PSW for
		 * tree2
		 */
		BipartitionTable table; // actual table to be used in calculation
		PSWTree psw1,psw2;		// actual trees to be used in calculation
		 
		PSWTree tempPSW;
		BipartitionTable tempTable;
		
		/* Check to see if we have seen the first tree or not */
		if (!PSWs.containsKey(tree1)) {
			tempPSW = new PSWTree(tree1);
			tempTable = new BipartitionTable(tempPSW);
			PSWs.put(tree1, tempPSW);
			bipTables.put(tree1, tempTable);
			table = tempTable;
			psw1 = tempPSW;
		} else { // we have already seen this tree; retrieve its table from memory
			table = (BipartitionTable) bipTables.get(tree1);
			psw1 = (PSWTree) PSWs.get(tree1);
		}
		
		/* Check to see if we have seen the second tree or not */
		if (!PSWs.containsKey(tree2)) {
			tempPSW = new PSWTree(tree2);
			PSWs.put(tree2,tempPSW);
			bipTables.put(tree2, new BipartitionTable(tempPSW));
			psw2 = tempPSW;
		} else {
			psw2 = (PSWTree) PSWs.get(tree2); //
		}


		LrnwStack stack = new LrnwStack(psw1.getN() * 2);
		stack.empty();
		int[] lrnw,poppedLRNW; /* see description below at initialization */
		int[] vw; /* the current vertex,weight pair */
		int w; /* total weight unaccounted for beneath the current vertex */
		double hitTotal = 0.0; /* total branch legths in tree 1 of bipartitons in both trees */
		double hitDiffTotal = 0.0; /* total of the branch length differences of bipartitions in both trees */
		double missTotal = 0.0; /* total branch lengths of bipartitions in tree 2 only */
		double leafDiffTotal = 0.0; /* total branch length differences of edges incident on leaves (not counted in hits) */

		//System.out.println("Traversing " + tree2.getName() + " against table for " + tree1.getName());

		for (psw2.prepareForEnumeration(), vw = psw2.nextVertex();
			 vw != null;
			 vw = psw2.nextVertex()) {
			lrnw = new int[4];
			if (vw[1] == 0) { /* leaf vertex */
				lrnw[0] = table.encoding[vw[0]]; /* leftmost is this leaf */
				lrnw[1] = table.encoding[vw[0]]; /* rightmost is this leaf */
				lrnw[2] = 1; /* one leaf in this subtree */
				lrnw[3] = 1; /* subtree's total weight is one */
				stack.push(lrnw);
				leafDiffTotal += Math.abs(psw1.getLength(vw[0]) - psw2.getLength(vw[0]));
			} else { /* interior vertex; represents a bipartition */
				lrnw[0] = psw2.getN() + 1; /* we want the smallest leaf among this vertex's children
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
						 (lrnw[0]==1 && lrnw[1] == psw2.getN())   // cluster of everything but taxon zero - not a bipartition
					  //|| (lrnw[0]==1 && lrnw[1] == psw2.getN()-1) // cluster that includes the whole tree - not a bipartition
					  ) 
					)
				{
					if (   (lrnw[2] == lrnw[1] - lrnw[0] + 1) /* if (N = R - L + 1) */
						&& table.containsBipartition(lrnw[0],lrnw[1]))
					{
						hitTotal += table.getLength(lrnw[0],lrnw[1]);
						hitDiffTotal+= Math.abs(psw2.getLength(vw[0]) - table.getLength(lrnw[0],lrnw[1]));
						// System.out.println("Hit: <" + lrnw[0] + "," + lrnw[1] + ">");
					} else {
						missTotal+= psw2.getLength(vw[0]);
						// System.out.println("Miss: <" + lrnw[0] + "," + lrnw[1] + ">");
					}
				}
			}
		}

		double answer = leafDiffTotal + hitDiffTotal + missTotal + (table.getTotalLength() - hitTotal);
		//System.out.println("leaf differences total = " + leafDiffTotal);
		//System.out.println("hit differences total = " + hitDiffTotal);
		//System.out.println("miss total = " + missTotal);
		//System.out.println("total length in table = " + table.getTotalLength());
		//System.out.println("hit total = " + hitTotal);
		//System.out.println("Weighted RF difference = " + answer);

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

/** This class implements the postorder sequence with weights representation of trees.  This
	representation is the starting point for computing Day's "cluster tables"  The main advantage
	is in being able to look up the next node visited after a given node in a postorder
	traversal of the tree in constant time. */
class PSWTree {
	int n; /* number of leaves */
	int enumerator; /* used in calls to enumerate vertices */
	int j; /* total number of verticies (leaves+internal) in the tree */
	int[][] vw; /* array of vertex/weight pairs; indexed by the order encountered in a post-order traversal*/
	double[] branchLengths; /* indexed by vertex */
	Tree t; /* The Mesquite tree this PSWtree represents */
	public static final int VERTEX = 0;
	public static final int WEIGHT = 1;
	public static final double defaultBranchLength = 1.0;

	public PSWTree(Tree t) {
		this.t = t;
		n = t.getNumTaxa();
		enumerator = 0;
		vw = new int[n*2][2];
		branchLengths = new double[n*3 + 1];
		for (int i=0; i<branchLengths.length; ++i) {branchLengths[i] = -1;}
		j = 0; /* index for building the array */
		int firstNode = t.nodeOfTaxonNumber(0);
		constructionRecursor(firstNode,t.motherOfNode(firstNode));
		/* insert taxon zero in list at second-to-last entry */
		vw[j][VERTEX] = 0; // taxon zero
		vw[j][WEIGHT] = 0; // leaves hove weight zero
		branchLengths[0] = t.getBranchLength(firstNode, defaultBranchLength);
		j++;
		/* insert pseudo-root (node on the branch incident on firstNode) at last entry */
		vw[j][VERTEX] = n+1; //actual value here doesn't matter
		vw[j][WEIGHT] = vw[j-2][WEIGHT]+2; /* This one does. */
		/* This new root has no parent and so has no branch length */
		j++;
		/* barfPSW(); */
	}

	private int constructionRecursor(int a, int v) {
		if (t.nodeIsTerminal(v)) {
			vw[j][VERTEX] = t.taxonNumberOfNode(v);
			vw[j][WEIGHT] = 0;
			branchLengths[vw[j][VERTEX]] = t.getBranchLength(v, defaultBranchLength);
			j++;
			return 1;
		} else {
			int w = 0;
			int numChildren = 0;
			for (int d=t.firstDaughterOfNodeUR(a,v); d > 0; d=t.nextSisterOfNodeUR(a,v,d)) { /* recurse, baby! */
				w += constructionRecursor(v,d);
				numChildren++;
			}
			if (numChildren > 1) {
				vw[j][VERTEX] = j + n + 1; /* to ensure that interior labels are > n */
				vw[j][WEIGHT] = w;
				branchLengths[vw[j][VERTEX]] = t.getBranchLength(t.nodeOfBranchUR(a,v), defaultBranchLength);
				j++;
				return w+1;
			} else {
				/* interior node of degree 2. Treat as if non-existent */
				return w;
			}
		}
	}

	public int getN() {
		return n;
	}

	public void prepareForEnumeration() {
		enumerator = 0;
	}

	public int[] nextVertex() {
		if (enumerator < j) {
			return vw[enumerator++];
		} else {
			return null;
		}
	}

	/** Returns the leftmost leaf of the vertex that returned by the last call of nextVertex */
	public int leftmostLeaf() {
		return vw[ (enumerator - 1 - vw[enumerator-1][1]) ][0];
	}

	/** returns the length of the branch between vertex and its parent */
	public double getLength(int vertex) {
		if (branchLengths[vertex] == -1) {
			System.out.println("Error: unassigned branch length requested");
		}
		return branchLengths[vertex];
	}

	/** dumps a textual copy of the PSW table to the screen for debugging */
	private void barfPSW() {
		System.out.println("PSW for " + t.getName());
		for (int i=0; i<j; i++) {
			System.out.println(i + ": " + vw[i][VERTEX] + "," + vw[i][WEIGHT]);
		}
		System.out.println();
	}
}


class BipartitionTable {
	public int[] encoding; /* function mapping leaf labels to internal labels */
	private int[][] table;  /* the table of bipartitions */
	private double[] branchLengths; /* branch lengths associated with each bipartition */
	private int numberOfBipartitions; /* in the sense of the Day paper, after pseudo-rooting */
	private double totalBranchLength; /* sum of all branch lengths in the tree */

	public BipartitionTable(PSWTree t) {
		encoding = new int[t.getN() + 1];
		table = new int[t.getN() + 1][2];
		branchLengths = new double[table.length];
		numberOfBipartitions = 0;
		totalBranchLength = 0.0;

		/* First, zero out the table */
		for (int i=0; i<table.length; i++) {
			table[i][0] = 0;
			table[i][1] = 0;
			branchLengths[i] = 0.0;
		}

		int L,R,leafcode;
		int[] vw;
		t.prepareForEnumeration();
		leafcode = 0;
		R = 0;
		vw = t.nextVertex();
		while (vw != null) {
			if (vw[1] == 0) { /* v is a leaf */
				leafcode++;
				encoding[vw[0]] = leafcode;
				R = leafcode;
				vw = t.nextVertex();
			} else { /* v is an internal vertex */
				L = encoding[t.leftmostLeaf()];
				vw = t.nextVertex();
				if	(!(  // two of the clusters we get in the traversal are not bipartitions and shouldn't go in the table
						 (L==1 && R == t.getN())   // cluster of everything but taxon zero - not a bipartition
					  //|| (L==1 && R == t.getN()-1) // cluster that includes the whole tree - not a bipartition
					  ) 
					)
				{
					if (vw == null || vw[1] == 0) {
						table[R][0] = L;
						table[R][1] = R;
						if(vw!=null)
						branchLengths[R] = t.getLength(vw[0]);
						else
							branchLengths[R] = 1.0;
					} else {
						table[L][0] = L;
						table[L][1] = R;
						branchLengths[L] = t.getLength(vw[0]);
					}
					numberOfBipartitions++;
					if(vw!=null)
					totalBranchLength += t.getLength(vw[0]);
					else
						totalBranchLength += 1.0;
				}
			}
		}
		/* barfTable(); */
	}

	private void barfTable() {
		for (int i = 0; i<table.length; i++) {
			System.out.print("e(" + i + ") = " + encoding[i] + "  ");
			System.out.println("<" + table[i][0] + "," + table[i][1] + ">");
		}
		System.out.println(numberOfBipartitions + " bipartitions");
		System.out.println(totalBranchLength + " total branch length");
		System.out.println();
	}

	public boolean containsBipartition(int L, int R) {
		if (table[L][0] == L && table[L][1] == R) {
			return true;
		}
		if (table[R][0] == L && table[R][1] == R) {
			return true;
		}
		return false;
	}

	public double getLength(int L, int R) {
		if (table[L][0] == L && table[L][1] == R) {
			return branchLengths[L];
		}
		if (table[R][0] == L && table[R][1] == R) {
			return branchLengths[R];
		}
		System.out.println("Warning: bipartion table asked for branch length of non-existent bipartiton: <" + L + "," + R + ">");
		return 0.0;
	}

	public int getNumBipartitions() {
		return numberOfBipartitions;
	}

	public double getTotalLength() {
		return totalBranchLength;
	}
}


