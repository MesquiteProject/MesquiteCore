/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.RarefyTree;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class RarefyTree extends RndTreeModifier {
	int numExcluded=1;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!MesquiteThread.isScripting()){
			int s = MesquiteInteger.queryInteger(containerOfModule(), "Number of taxa to exclude", "Enter the number of randomly chosen taxa to exclude", numExcluded);
			if (MesquiteInteger.isCombinable(s))
				numExcluded = s;
			else
				return false;
		}
		addMenuItem("Number of Taxa Excluded...", makeCommand("setNumberExcluded",  this));
		return true;
	}
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("setNumberExcluded " + numExcluded);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the number of randomly chosen taxa to exclude", "[number]", commandName, "setNumberExcluded")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(s)){
				s = MesquiteInteger.queryInteger(containerOfModule(), "Number of taxa to exclude", "Enter the number of randomly chosen taxa to exclude", numExcluded);
			}
			if (MesquiteInteger.isCombinable(s)){
				numExcluded = s;
				parametersChanged(); 
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void modifyTree(Tree tree, MesquiteTree modified, RandomBetween rng){
		if (tree == null || modified == null)
			return;
		if (tree.getTaxa().anySelected()){  //error fixed in 1. 12
			int[] terminals =tree.getTerminalTaxa(tree.getRoot());
			if (terminals == null)
				return;
			int numTerminals = 0;
			for (int i= 0; i< terminals.length; i++)
				if (tree.getTaxa().getSelected(terminals[i]))
					numTerminals++;
			if (numTerminals>numExcluded){
				int[] selTerminals =new int[numTerminals];
				int icount = 0;
				for (int i= 0; i< terminals.length; i++)
					if (tree.getTaxa().getSelected(terminals[i])){
						selTerminals[icount] = terminals[i];
						icount++;
					}
				terminals = selTerminals;
				for (int it = 0; it<numExcluded; it++) {
					int taxon = -1;
					int count = 0;
					int ntries = 100000;
					while (terminals[taxon=rng.randomIntBetween(0, numTerminals-1)] <0 && count<ntries){  
						count++;
					}
					if (count>= ntries)
						discreetAlert( "ERROR: Rarefy tree failed to find taxon to delete in " + ntries + " tries.");
					else {
						int nT = modified.nodeOfTaxonNumber(terminals[taxon]);
						modified.deleteClade(nT, false);
						terminals[taxon] = -1;
					}

				}
			}
			else
				MesquiteMessage.warnUser("Sorry, the tree could not be rarefied because more taxa are to be excluded than those available");
		}
		else {
			int numTerminals =tree.numberOfTerminalsInClade(tree.getRoot());
			if (numTerminals>numExcluded){
				for (int it = 0; it<numExcluded; it++) {
					int taxon = rng.randomIntBetween(0, numTerminals-it-1);
					int nT = modified.getTerminalNode(modified.getRoot(), taxon);
					modified.deleteClade(nT, false);
				}
			}
			else
				MesquiteMessage.warnUser("Sorry, the tree could not be rarefied because more taxa are to be excluded than those available");
		}
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return"Number of taxa excluded randomly: " + numExcluded;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Rarefy Tree";
	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Rarefies tree by randomly excluding taxa.  If some taxa are selected, random exclusion is limited to the selected taxa.";
	}

}

