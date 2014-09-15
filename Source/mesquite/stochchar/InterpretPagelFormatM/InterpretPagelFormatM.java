/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.InterpretPagelFormatM;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.stochchar.lib.*;

/* ============  a file interpreter for Pagel's ppy files ============*/

public class InterpretPagelFormatM extends PagelFormatI {
/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;  //make this depend on taxa reader being found?)
  	 }
  	 
	public boolean readTreeAndCharacters(MesquiteFile file, String line, Vector nodes, MesquiteInteger nC){
		int numChars = 1;
		boolean abort = false;
		//first line gives number of taxa and number of characters
		parser.setString(line);
		parser.getNextToken(); //taxa
		numChars = MesquiteInteger.fromString(parser.getNextToken()); //taxa
		if (!MesquiteInteger.isCombinable(numChars) || numChars == 0)
			return false;
		StringBuffer sb = new StringBuffer(1000);
		file.readLine(sb);
		line = sb.toString();
		
		String token;
		int nt = 0;
		while (!StringUtil.blank(line)&& !abort) {  //first pull in all the nodes
			parser.setString(line);
			if (parser.firstDarkChar() != '#') {
				String node = parser.getNextToken();
				token = parser.getNextToken();  //comma ?
				if (",".equalsIgnoreCase(token))
					token = parser.getNextToken();
				String ancestor =token;  //node number of ancestor
				token = parser.getNextToken();  //comma ?
				if (",".equalsIgnoreCase(token))
					token = parser.getNextToken();
				double branchLength = MesquiteDouble.fromString(token, false);  //branch length
				token = parser.getNextToken();  //comma ?
				if (",".equalsIgnoreCase(token))
					token = parser.getNextToken();
				int[] states = new int[numChars];
				states[0] = MesquiteInteger.fromString(token, false);  //state of character 
				token = parser.getNextToken();  //comma ?
				if (",".equalsIgnoreCase(token))
					token = parser.getNextToken();
				int taxonNumber = -1;
				if (MesquiteInteger.isCombinable(states[0])) {  //appears to be terminal; get other characters
					taxonNumber = nt++;
					for (int ic = 1; ic<numChars; ic++){
						states[ic] = MesquiteInteger.fromString(token, false);  //state of character 
						token = parser.getNextToken();  //comma ?
						if (",".equalsIgnoreCase(token))
							token = parser.getNextToken();
					}
				}
				PagNodeRecord pnr = new PagNodeRecord(taxonNumber, node, ancestor, branchLength, states);
				nodes.addElement(pnr);
			}
			file.readLine(sb);
			line = sb.toString();		
			if (file.getFileAborted()) {
				abort = true;
			}
		}
		nC.setValue(numChars);
		return !abort; 
	}
	
	/*.................................................................................................................*/

	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		CharacterData data = getProject().chooseData(containerOfModule(), null, CategoricalState.class, "Data matrix to export");
		if (data ==null)
			return false;
		Taxa taxa = data.getTaxa();
		OneTreeSource treeTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of tree to be exported to Pagel format (ppy) file");
		Tree tree = null;
		if (treeTask != null) {
   			treeTask.initialize(taxa);
			tree = treeTask.getTree(taxa);
		}
		if (tree == null) {
			Listable[] treeVectors = getProject().getCompatibleFileElements(TreeVector.class, taxa);
			if (treeVectors == null) {
				fireEmployee(treeTask);
				return false; //MESSAGE
			}
			TreeVector trees;
			if (treeVectors.length ==1)
				trees = (TreeVector)treeVectors[0];
			else
				trees = (TreeVector)ListDialog.queryList(containerOfModule(), "Choose Trees Block", "Choose block of trees from which to choose a tree for export to  Pagel format (ppy) file", MesquiteString.helpString, treeVectors, 0);
			if (trees == null) {
				fireEmployee(treeTask);
				return false; //MESSAGE
			}
			tree = (Tree)ListDialog.queryList(containerOfModule(), "Choose Tree", "Choose tree for export to Pagel format (ppy) file", MesquiteString.helpString, trees, 0);
		}
		if (tree == null) {
			fireEmployee(treeTask);
			return false; //MESSAGE
		}
			
			
		// convert polytomies to zeroLengthBranches
		sanitizeTree(tree);
		
		//choose two binary character distributions
		String[] charNames = new String[data.getNumChars()];
		for (int i=0; i<data.getNumChars(); i++)
			charNames[i]= data.getCharacterName(i);
 			
		int numCharacters = 1;
		int nc = MesquiteInteger.queryInteger(containerOfModule(), "How many characters?", "How many characters to put into ppy file?", 1, 1, data.getNumChars(), false);
		if (MesquiteInteger.isCombinable(nc))
			numCharacters = nc;
		
		int numSelected = 0;
		String which = "first";
		CategoricalDistribution[] chosenChars = new CategoricalDistribution[numCharacters];
		while (numSelected<numCharacters){
 			int chosen = ListDialog.queryList(containerOfModule(), "Choose character", "Choose " + which + " character for export to Pagel format (ppy) file", MesquiteString.helpString, charNames, 0);
			if (chosen >=0 ){
				CategoricalDistribution distn = (CategoricalDistribution)data.getCharacterDistribution(chosen);
				long allStates = 0L;
				boolean uncombinableState = false;
				boolean polymorphisms = false;
				for (int it = 0; it < distn.getNumTaxa() && !uncombinableState; it++) {
					long state = distn.getState(it);
					if (!CategoricalState.isCombinable(state))
						uncombinableState = true;
					if (CategoricalState.cardinality(state)>1)
						polymorphisms = true;
					allStates |= state;
				}
				if (uncombinableState)
					alert("Sorry, that character can't be used because it has missing or inapplicable codings");
				else if (polymorphisms)
					alert("Sorry, that character can't be used because it has polymorphisms or uncertainties.");
				else if (CategoricalState.maximum(allStates)>5) //allow multistate output for MultiState!
					alert("Sorry, that character can't be used because it has a state other than 0, 1, 2, 3, 4, or 5");
				else {
					chosenChars[numSelected] = distn;
					numSelected++;
					which = "next";
				}
			}
			else {
				fireEmployee(treeTask);
				return false; //MESSAGE
			}
		}
		
		int numTaxa = tree.numberOfTerminalsInClade(tree.getRoot());
		StringBuffer outputBuffer = new StringBuffer(numTaxa*12);
		outputBuffer.append(Integer.toString(numTaxa) + " " + numCharacters + "\r\n");

		outputBuffer.append("#Translated to Pagel format for Multistate from project with home file " + getProject().getHomeFileName());
		outputBuffer.append("\r\n");
		outputBuffer.append("# Block of taxa: " + taxa.getName());
		outputBuffer.append("\r\n");

		for (int i=0; i<numCharacters; i++){
			outputBuffer.append("# Character " + (i+1) + ": " + chosenChars[i].getName() + " of data matrix " + data.getName());
			outputBuffer.append("\r\n");
		}

		outputBuffer.append("# Tree: " + tree.getName());
		outputBuffer.append("\r\n");
		int[] numbers = new int[tree.getNumNodeSpaces()];
		MesquiteInteger nodeCount = new MesquiteInteger(numTaxa+1);
		numberNodes(tree, tree.getRoot(), numbers, nodeCount);
		for (int it = 0; it<taxa.getNumTaxa(); it++){
				if (tree.taxonInTree(it)) {
					int node = tree.nodeOfTaxonNumber(it);
					outputBuffer.append('t'); //t for taxon
					outputBuffer.append(Integer.toString(it+1)); //taxon number
					outputBuffer.append(','); 
					outputBuffer.append(numbers[tree.motherOfNode(node)]);  //ancestor node
					outputBuffer.append(','); 
					outputBuffer.append(MesquiteDouble.toStringNoNegExponential(tree.getBranchLength(node, 1.0)));
					outputBuffer.append(','); 
					for (int ic = 0; ic<numCharacters; ic++){ //writing out character states
						long cs1 = chosenChars[ic].getState(it);
						if (CategoricalState.isElement(cs1, 0))
							outputBuffer.append('0'); 
						else if (CategoricalState.isElement(cs1, 1))
							outputBuffer.append('1'); 
						else if (CategoricalState.isElement(cs1, 2))
							outputBuffer.append('2'); 
						else if (CategoricalState.isElement(cs1, 3))
							outputBuffer.append('3'); 
						else if (CategoricalState.isElement(cs1, 4))
							outputBuffer.append('4'); 
						else if (CategoricalState.isElement(cs1, 5))
							outputBuffer.append('5'); 

						if (ic<numCharacters-1)
							outputBuffer.append(','); 
					}
					outputBuffer.append("\r\n");
				}
		}

		StringBuffer equivalenceBuffer = new StringBuffer(50);
		equivalenceBuffer.append("#Table of equivalencies of nodes with Mesquite's node numbering system\r\n");
		outputInternals(tree, tree.getRoot(), numbers, outputBuffer, equivalenceBuffer);

//table of equivalencies of nodes
		outputBuffer.append("\r\n");
		outputBuffer.append("\r\n");
		outputBuffer.append(equivalenceBuffer.toString());
		String output = outputBuffer.toString();
		
		String name = getProject().getHomeFileName();
		if (name==null)
			name = "untitled.ppy";
		else 
			name = stripNex(name) + ".ppy";
		saveExportedFile(output, arguments, name);
		fireEmployee(treeTask);
		return true;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Pagel format (ppy) file for Multistate";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports files formatted for Pagel's Multistate program." ;
   	 }
	/*.................................................................................................................*/
   	 
   	 
}
	

