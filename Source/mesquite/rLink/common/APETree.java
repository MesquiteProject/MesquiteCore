/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.rLink.common;
/*~~  */

import mesquite.lib.*;

/* ======================================================================== */
public class APETree  {
	public int[][] edge;
	public double[] edgeLength;
	public String[] tipLabel;
	public int numNodes = 0;
	public int numTerminals = 0;
	int mesquiteRoot;
	public APETree(Tree tree){
		numNodes = tree.numberOfNodesInClade(tree.getRoot());
		numTerminals = tree.numberOfTerminalsInClade(tree.getRoot());
		// 0 for ancestral node of each branch
		// 1 for descendant node of each branch
		// 2 for original node number in Mesquite's tree
		// 3 for original taxon number in Mesquite's tree
		edge = new int[4][numNodes-1];
		edgeLength = new double[numNodes-1];
		tipLabel = new String[numTerminals];
		counter= -1;
		counterInternal = tree.numberOfTerminalsInClade(tree.getRoot())+1;
		counterTerminal = 0;
		mesquiteRoot = tree.getRoot();
		packAPETree(tree);
	}
	public String dump(){
		String s = "ape anc node " + IntegerArray.toString(edge[0]);
		s += "\nape desc node " + IntegerArray.toString(edge[1]); 
		s += "\nmesq original node " + IntegerArray.toString(edge[2]); 
		s += "\nmesq  taxon " + IntegerArray.toString(edge[3]); 
		return s;
	}
	int counterInternal = -1;
	int counterTerminal = -1;
	int counter;
	void inPackAPE(Tree tree, int node, int ancestor){
		int thisNode = -1;
		if (tree.getRoot() != node){
			counter++;
			if (tree.nodeIsTerminal(node)){
				counterTerminal++;
				thisNode = counterTerminal;
				tipLabel[counterTerminal-1] = tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(node));
				edge[3][counter] = tree.taxonNumberOfNode(node);
			}
			else {
				counterInternal++;
				thisNode = counterInternal;
				edge[3][counter] = -1;
			}
			edge[0][counter] = ancestor;  //node at ancestor
			edge[1][counter] = thisNode;  //this node
			edge[2][counter] = node; //original node number
			edgeLength[counter] = tree.getBranchLength(node, 1.0);
		}
		else
			thisNode = ancestor;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			inPackAPE(tree, d, thisNode);
		}
	}
	void packAPETree(Tree tree){
		for (int k=0; k<3; k++)
			for(int it = 0; it<edge[0].length; it++)
				edge[k][it] = -1;
		inPackAPE(tree, tree.getRoot(), tree.numberOfTerminalsInClade(tree.getRoot())+1);
	}
	public int[] getNodeTable(int i){
		return edge[i];
	}
	public int getMesquiteRoot(){
		return mesquiteRoot;
	}
	public int getAPEAncestor(int apeNode){
		if (edge != null && edge.length>2)
			for (int i=0; i< edge[1].length; i++)
				if (edge[1][i] == apeNode)
					return edge[0][i];
		return -1;
	}
	public int getMesquiteNode(int apeNode){
		if (edge != null && edge.length>2){
			if (apeNode == numTerminals + 1) //isRoot
				return mesquiteRoot;
			for (int i=0; i< edge[1].length; i++)
				if (edge[1][i] == apeNode){
					return edge[2][i];
				}
		}

		return -1;
	}
	public int getAPENode(int mesquiteNode){

		if (edge != null && edge.length>2)
			for (int i=0; i< edge[2].length; i++)
				if (edge[2][i] == mesquiteNode)
					return edge[1][i];
		return -1;
	}
	public int getMesquiteTaxon(int apeTaxon){
		int countAPE = 0;
		if (edge != null && edge.length>2)
			for (int i=0; i< edge[3].length; i++){
				if (edge[3][i] >= 0) { //is terminal in original tree
					if (edge[1][i] == apeTaxon)
						return edge[3][i];
					countAPE++;
				}
			}
		return -1;
	}
	public int getAPETaxon(int mesquiteTaxon){

		int countAPE = 0;
		if (edge != null && edge.length>2)
			for (int i=0; i< edge[3].length; i++){
				if (edge[3][i] == mesquiteTaxon)
					return edge[1][i];
				countAPE++;
			}
		return -1;
	}

	public int[][] getEdgeMatrix() {
		int[][] edgeMatrix = new int[2][edge[0].length];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < edge[0].length; j++) {
				edgeMatrix[i][j] = edge[i][j];
			}
		}
		return edgeMatrix;
	}

	public double[] getEdgeLengths() {
		return edgeLength;
	}

	public String[] getTipLabels() {
		return tipLabel;
	}

	public String toRCommand(){
		return toRCommand(false);
	}

/** Converts the tree to an R command.  If adjustZeroLengthBranches is true, then any branch with exactly zero
 *  length is changed to the smallest possible non-zero positive double, so that the tree can be succesfully used by
 *  those R commands that won't work with zero-length branches.
 */
	/*--------------------------------*/
	public String toRCommand(boolean adjustZeroLengthBranches){
		String s = "list(edge = cbind(c(";
		boolean first = true;
		for (int i= 0; i<edge[0].length; i++){
			if (!first)
				s += ", ";
			s += edge[0][i];
			first = false;
		}
		s += "), c(";
		first = true;
		for (int i= 0; i<edge[1].length; i++){
			if (!first)
				s += ", ";
			s += edge[1][i];
			first = false;
		}
		s += ")), edge.length = c(";
		first = true;
		for (int i= 0; i<edgeLength.length; i++){
			if (!first)
				s += ", ";
			if (adjustZeroLengthBranches && edgeLength[i]==0) 
				s += MesquiteDouble.toStringNoNegExponential(Double.MIN_VALUE);
			else
				s += MesquiteDouble.toStringNoNegExponential(edgeLength[i]);
			first = false;
		}
		s += "), tip.label = c(";
		first = true;
		for (int i= 0; i<tipLabel.length; i++){
			if (!first)
				s += ", ";
			s += "\"" + tipLabel[i] + "\"";
			first = false;
		}
		s += "), Nnode = " + (numNodes-numTerminals) + ");";
		return s;
	}

}
