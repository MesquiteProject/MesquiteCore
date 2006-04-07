/*
 * This software is part of the Tree Set Visualization module for Mesquite,
 * written by Jeff Klingner and Silvio Neris.
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


public class LrnwStack
{
    private int[][] s;
    private int i;
    
    public LrnwStack(int size) {
	s = new int[size][];
	i = 0;
    }
    
    public void push(int[] lrnw) {
	s[i++] = lrnw;
	}
    
    public int[] pop() {
	if (i > 0) {
	    return s[--i];
	} else {
	    return null;
	}
    }
    
    public void empty() {
	    i = 0;
    }
}
