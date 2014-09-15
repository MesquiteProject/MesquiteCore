/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.ReshuffleTaxa;
/*~~ */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;


/* This module shuffles (permutes) the terminal taxa of the tree.  Because it doesn't
   need any values from the user, its interface is pretty simple. */
/* ======================================================================== */

public class ReshuffleTaxa extends RndTreeModifier {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public void modifyTree(Tree tree, MesquiteTree modified, RandomBetween rng){
   		if (tree == null || modified == null)
   			return;
		int[] terminals = tree.getTerminalTaxa(tree.getRoot());
		int numTerminals =tree.numberOfTerminalsInClade(tree.getRoot());
		int taxon  = 0;
		for (int fT = 0; fT < numTerminals-1; fT++) {
			int firstTerminal = terminals[fT];
			int secondTerminal = terminals[rng.randomIntBetween(fT,numTerminals-1)];
			
		    	int firstTaxonNode = modified.nodeOfTaxonNumber(firstTerminal);
			int secondTaxonNode = modified.nodeOfTaxonNumber(secondTerminal);
			modified.setTaxonNumber(secondTaxonNode,firstTerminal,false);
			modified.setTaxonNumber(firstTaxonNode,secondTerminal,false);
		
		}
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Reshuffle Terminal Taxa";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Shuffles (permutes) the taxa among the terminal nodes.";
   	 }
   	 
}





