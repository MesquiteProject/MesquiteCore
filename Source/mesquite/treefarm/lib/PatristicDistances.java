/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;


public class PatristicDistances {
	double[][] heights;

	/*-----------------------------------------*/
	public double[][] calculatePatristic(Tree tree, int numTaxa, double[][] distances){
		if (distances == null || distances.length != numTaxa)
			distances = new double[numTaxa][numTaxa];
		if (heights == null || heights.length != tree.getNumNodeSpaces() || heights[0].length != numTaxa)
			heights = new double[tree.getNumNodeSpaces()][numTaxa];
		for (int i = 0; i < numTaxa; i++){
			for (int j = 0; j < numTaxa; j++)
				if (i!=j)
					distances[i][j] = -1;
				else
					distances[i][j] = 0;
		}
		for (int i = 0; i < tree.getNumNodeSpaces(); i++){
			for (int j = 0; j < numTaxa; j++)
				heights[i][j] = -1;
		}
		getHeights(tree, tree.getRoot(), heights);
		getDistances(tree, tree.getRoot(), numTaxa, heights, distances);
		return distances;
	}
	/*-----------------------------------------*/
	/** Finds heights to taxa for each node.  Beforehand initialize heights to -1*/
	void getHeights(Tree tree, int node, double[][] heights) { 
		if (tree.nodeIsTerminal(node)){
			int taxon = tree.taxonNumberOfNode(node);
			if (node < heights.length && taxon >= 0 && taxon< heights[node].length)
				heights[node][taxon]=0;
		}
		else {
			boolean firstDaughter = true;
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				getHeights(tree, daughter, heights);
				if (daughter<heights.length)
					for (int i=0; i<heights[daughter].length; i++)
						if (heights[daughter][i] >= 0){ //taxon i is in daughter clade
							if (node < heights.length && i< heights[node].length){
								if (heights[node][i]<0)
									heights[node][i] = 0;
								if (tree.getRoot()==node && !firstDaughter && !tree.nodeIsPolytomous(node))
									heights[node][i] += heights[daughter][i];
								else
									heights[node][i] += heights[daughter][i] + 1;
							}
							firstDaughter = false;
						}
			}
		}
	}
	/** Finds distances among taxa.  Beforehand initialize distances to -1*/
	void getDistances(Tree tree, int node,  int numTaxa, double[][] heights, double[][] distances) { 
		if (tree.nodeIsInternal(node)){
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				getDistances(tree, daughter, numTaxa, heights, distances);
			}
			for (int i= 0; i< numTaxa; i++)
				for (int j= 0; j< i; j++)
					if (distances[i][j]<=0) { //not yet assigned
						if (heights[node][i]>= 0 && heights[node][j] >=0){ //first common ancestor found
							distances[i][j] = heights[node][i] + heights[node][j];
							distances[j][i] = distances[i][j];
						}
					}
		}
	}
	
	public double[][] getDistancesWithLengths(Tree tree, int numTaxa, double[][] distances){
		if (distances == null || distances.length != numTaxa)
			distances = new double[numTaxa][numTaxa];
		Double2DArray.deassignArray(distances);
		if (tree == null)
			return null;
		int root = tree.getRoot();
		for (int taxon1=0; taxon1<numTaxa; taxon1++) {
			int node1 = tree.nodeOfTaxonNumber(taxon1);
			if (tree.nodeExists(node1)){
				for (int taxon2=0; taxon2<numTaxa; taxon2++) {
					double sumPath =0;
					int node2 = tree.nodeOfTaxonNumber(taxon2);
					if (tree.nodeExists(node2)){
						int mrca = tree.mrca(node1, node2);
						int node = node1;
						while (node!=mrca && (node != root) && tree.nodeExists(node)){
							sumPath += tree.getBranchLength(node, 1.0);
							node = tree.motherOfNode(node);
						}
						node = node2;
						while (node!=mrca && node != root && tree.nodeExists(node)){
							sumPath += tree.getBranchLength(node, 1.0);
							node = tree.motherOfNode(node);
						}
						distances[taxon1][taxon2]=sumPath;
					}
				}
			}
		}
		return distances;
	}


}


