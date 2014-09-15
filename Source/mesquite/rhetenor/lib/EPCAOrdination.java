/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.rhetenor.lib; 

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.*;


/* ======================================================================== */
	public class EPCAOrdination extends Ordination   {
	     	double[] sqLengths, transformedSqLengths;
		double sumSqLengths, transformedSumSqLengths;
		public EPCAOrdination (double[][] original, Tree tree, boolean weightByBranchLengths){
			if (original!=null && tree != null){
				//First, reconstruct ancestral states for all characters
				SquaredReconstructor reconstructor = new SquaredReconstructor();
				reconstructor.reconstruct(tree, original, weightByBranchLengths, true, null);
				double[][] reconstructed =reconstructor.getReconstructedStates(0);   //todo: uses only first item
				sqLengths = reconstructor.getSumSquaredLengths();
				sumSqLengths =0;
				for (int i=0; i<sqLengths.length; i++)
					sumSqLengths+= sqLengths[i];

				//Second, construct a new matrix implicitly for a set of taxa, one for each node, assigning change as character states
				double[][] changeMatrix = new double[original.length][ tree.numberOfNodesInClade(tree.getRoot())-1];  //not including root
				Double2DArray.zeroArray(changeMatrix);
				nodeNumber = 0;
				fillMatrix(tree, tree.getRoot(), changeMatrix, reconstructed);
				
				//Third, do PCA on the new matrix of changes.
		  		doEigenAnalysis(MatrixUtil.covariance(changeMatrix), original);
								
				//Fourth, calculate how much of squared change is accounted for by each new axis
				if (eigenValues!=null){
					reconstructor.reconstruct(tree, getScores(), weightByBranchLengths, true, null);
					transformedSqLengths = reconstructor.getSumSquaredLengths();
					transformedSumSqLengths =0;
					for (int i=0; i<transformedSqLengths.length; i++)
						transformedSumSqLengths+= transformedSqLengths[i];
					percentExplained = MatrixUtil.percentage(transformedSqLengths);
				}
			}
		}
		/*...............................................................................................................*/
		/** This method takes the reconstructed matrix, whose elements are numbered as the nodes of the tree
			(which could have unused elements), and compacts it into a matrix consisting of just as many elements
			as there are nodes in the tree (excluding the root).*/
		int nodeNumber;
		private void fillMatrix(Tree tree, int node, double[][] compacted, double[][] reconstructed ){
			if (node!=tree.getRoot()) {
				int mother = tree.motherOfNode(node);
				for (int ic = 0; ic<reconstructed.length; ic++) {
					compacted[ic][nodeNumber]= reconstructed[ic][node] - reconstructed[ic][mother];  //item = 0
				}
				nodeNumber++;
			}
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				fillMatrix(tree, daughter, compacted, reconstructed);
		}
		/*...............................................................................................................*/
		public String report(){
			if (sqLengths==null) return null;
			String s ="Original square lengths\n" + MatrixUtil.toString(sqLengths) + "\nSum: " + sumSqLengths;
			if (transformedSqLengths==null)
				return s+"\n Error: no transformed matrix calculated";
			s+="\nTransformed square lengths\n" + MatrixUtil.toString(transformedSqLengths) + "\nSum: " + transformedSumSqLengths;
			return s;
		}
		/*.................................................................................................................*/
	 	public String getAxisName(int i){
	 		if (sqLengths==null || i>=sqLengths.length || i<0)
	 			return "EPC" + CharacterStates.toExternal(i) ;  
	 		else
	 			return "EPC" + CharacterStates.toExternal(i) + " (" +MesquiteDouble.toStringDigitsSpecified(100.0*(percentExplained[i]), 3) +"%)";
	 	}
}


