/*
 * This software is part of the Tree Set Visualization module for Mesquite,
 * written by Jeff Klingner and Silvio Neris
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

package mesquite.consensus.common;


import java.applet.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


public class MarkedBipartitionTable 
{
    public int[] encoding; /* function mapping leaf labels to internal labels */
    private int[][] table;  /* the table of bipartitions */
	public BitSet marks; /* a flag for each bipartition */
	private BitSet touches;
	private int tableSize;
    private int numberOfBipartitions; /* in the sense of the Day paper, after pseudo-rooting */
    
    public MarkedBipartitionTable(PSWTree t) {
		tableSize = t.getN() + 1;
		encoding = new int[tableSize];
		table = new int[tableSize][2];
		marks = new BitSet(tableSize);
		touches = new BitSet(tableSize);
		numberOfBipartitions = 0;
		
		/* First, zero out the table */
		for (int i=0; i<table.length; i++) {
			table[i][0] = 0;
			table[i][1] = 0;
			marks.set(i);
			touches.clear(i);
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
			//System.out.println("encoding[ " + vw[0] + " ] = " + leafcode + " = R");
			R = leafcode;
			vw = t.nextVertex();
			} else { /* v is an internal vertex */
			L = encoding[t.leftmostLeaf()];
			//System.out.println("encoding[ " + t.leftmostLeaf() + " ] = " + L + " = L");
			vw = t.nextVertex();
			if	(!(  // two of the clusters we get in the traversal are not bipartitions and shouldn't go in the table
					 (L==1 && R == t.getN())   // cluster of everything but taxon zero - not a bipartition
				  //|| (L==1 && R == t.getN()-1) // cluster that includes the whole tree - not a bipartition
				  ) 
				)
			{
				
				if (vw == null || vw[1] == 0) {
				//System.out.println("ifffffffff");
				table[R][0] = L;
				table[R][1] = R;
				} else {
				//System.out.println("elseeeeeeee");
				table[L][0] = L;
				table[L][1] = R;
				}
				numberOfBipartitions++;
			}
			}
		}
		//barfTable(); 
    }
    
    private void barfTable() {
		for (int i = 0; i<table.length; i++) {
			System.out.print("e(" + i + ") = " + encoding[i] + "  ");
			System.out.println("<" + table[i][0] + "," + table[i][1] + ">");
		}
		System.out.print(numberOfBipartitions + " bipartitions\n\n");
    }
    
    public void touchBipartition(int L, int R) {
		if (table[L][0] == L && table[L][1] == R) {
			touches.set(L);
		} else if (table[R][0] == L && table[R][1] == R) {
			touches.set(R);
		}
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
	
	public boolean containsMarkedBipartition(int L, int R) {
		if (table[L][0] == L && table[L][1] == R) {
			return marks.get(L);
		}
		if (table[R][0] == L && table[R][1] == R) {
			return marks.get(R);
		}
		return false;
    }
	
	public void clearUntouchedMarks() {
		for (int i=0; i < tableSize; ++i) {
			if (!touches.get(i)) { // if this bipartition was never checked for
				marks.clear(i);    // remove its mark
			}
			touches.clear(i); // reset this touch for next time around
		}
	}
		
    
	public int getNumBipartitions() {
	    return numberOfBipartitions;
	}
}
















