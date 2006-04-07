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


/**
 * This class implements the postorder sequence with weights representation of trees.  This
 * representation is the starting point for computing Day's "cluster tables"  The main advantage
 * is in being able to look up the next node visited after a given node in a postorder
 * traversal of the tree in constant time.
 *
 *@author Jeff Klinger
 */
public class PSWTree {

    int n; /* number of leaves */

    int enumerator; /* used in calls to enumerate vertices */

    int j; /* total number of verticies (leaves+internal) in the tree */

    int[][] vw; /* array of vertex/weight pairs */
   
    Tree t; /* The Mesquite tree this PSWtree represents */

    

   public PSWTree(Tree t) {

	this.t = t;

	n = t.getNumTaxa();

	enumerator = 0;

	vw = new int[n*2][2];

	j = 0; /* index for building the array */

	int firstNode = t.nodeOfTaxonNumber(0);	

	constructionRecursor( firstNode, t.motherOfNode(firstNode) );

	/* insert taxon zero in list at second-to-last entry */
	vw[j][0] = t.taxonNumberOfNode(firstNode);
	vw[j][1] = 0;

	j++;

	/* insert pseudo-root (node on the branch incident on firstNode) at last entry */
	vw[j][0] = n+1; //actual value here doesn't matter
	vw[j][1] = vw[j-2][1]+2; /* This one does. */

	j++;
       	
	//barfPSW(); 
	
    }

    

    private int constructionRecursor(int a, int v) {

	if (t.nodeIsTerminal(v)) {

	    vw[j][0] = t.taxonNumberOfNode(v);

	    vw[j][1] = 0;	    
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
	
		//System.out.println("Interior: " + v + " v+n+1: " + (v+n+1));
		
		vw[j][0] = v + n + 1; /* to ensure that interior labels are > n */
		vw[j][1] = w;
	
		j++;		

		return w+1;

	    } else {		
		
		return w; /* interior node of degree 2. Treat as if non-existent */
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

    

    private void barfPSW() {

	System.out.println("PSW for " + t.getName());

	for (int i=0; i<j; i++) {

	    System.out.println(i + ": " + vw[i][0] + "," + vw[i][1] + " label: " + t.getNodeLabel(t.nodeOfTaxonNumber(vw[i][0])));
	}
	System.out.println();
    }

}























