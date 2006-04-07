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
	Last change:  DE   16 Apr 2003   11:18 am
 */
 
 
package mesquite.treecomp.SelectDistMatrix;
/*~~  */

import java.applet.*;
import java.util.*;
import java.awt.*;

import mesquite.consensus.common.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class SelectDistMatrix extends NumberFor2Trees {
	private static final int INITIAL_HASHMAP_CAPACITY = 500;

	/* Memoization variables */
	private HashMap PSWs;
	private HashMap bipTables;
	//WPM 05: make sure hashmaps aren't causing same bugs as in RFDistance.  My guess is that they are
	
	public String getName() { return "Select Distance Matrix"; }
	public String getVersion() { return "1.0"; }
	public String getYearReleased() { return "2003"; }
	public boolean showCitation() {	return true; }
	public String getPackageName() { return "Select Distance Matrix Package"; }
	public boolean getUserChoosable() { return false; }
	public boolean isPrerelease() { return false; }
	public boolean isSubstantive() { return true; }
	public String getCitation() { return "\n" + getYearReleased() + ". " + getAuthors() + "\n"; }
	public String getAuthors() { return "Denise Edwards, Lehman College, City University of NY"; }
	
	public String getExplanation() {
		return	"Reads in an already computed Distance Matrix\n" +
				"The Matrix is contained in a nexus file.\n";
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
	}	
	
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree t1, Tree t2, CommandRecord commandRec) {
	}
}






