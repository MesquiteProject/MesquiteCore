/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/** A class that provides root states for probabilistic models, using frequencies for the root states. 
This class must be subclassed, with the method setStateProbabilities specified. */
/* ======================================================================== */
public abstract class RateMatrixDNAModel extends RateMatrixCatModel {

	public RateMatrixDNAModel (CompositProbCategModel probabilityModel) {
		super(probabilityModel,4, DNAState.class);
 	}
 	/*.................................................................................................................*/
 	/* Archiving system:  To speed calculations, an archive of up to 6 probability matrices can be stored at each
 	node.  When a calculation is requested, these matrices are perused to see if one is applicable at the node.  It is judged
 	applicable if the matrix had been calculated using the same branch length as before, as long as something hasn't invalidated
 	the matrices (this happens if the eigenvalues etc. are recalculated, or if the tree changes size).  Typically a node will
 	have different branch lengths in different calculations because different characters have different rates of change.  6 was chosen
 	as a reasonable number of recent matrices, because a discrete gamma will usually have 4 categories of character rates and
 	a codon position specific rates model will have 5 categories (N1243-).
 	
 	This full archiving system is built as an addition to the service provided by  RateMatrixCatModel to store probability matrices at each node.
 	*/
	static final int maxArchivePlaces = 7;
	
	/* the archive of probability matrices.  Size is numNodes X maxArchivePlaces X num elements in prob matrix + 1.
	Thus, stores at each node, maxArchivePlaces matrices, each of which has enough spaces for the probabilty matrix plus an extra space.
	The extra space (element 0) is reserved for the branch length under which the matrix was calculated. */
	double[][][] archivedProbabilities;
	double[] previousBranchLengths;
	
	/** check to see that archives and prevoius branch length records still big enough for tree */
	protected void checkMatrixSizes(Tree tree){
		if (previousBranchLengths==null || previousBranchLengths.length<tree.getNumNodeSpaces()) {
	 		previousBranchLengths = new double[tree.getNumNodeSpaces()];
	 		archivedProbabilities = new double[tree.getNumNodeSpaces()][maxArchivePlaces][getNumStates()*getNumStates()+1]; //+1 to store length
	 		invalidateProbabilitiesAtNodes();
		}
	}
	/** Mark archives as invalid. */
 	public void invalidateProbabilitiesAtNodes(){
 		if (previousBranchLengths != null)
 			for (int i=0; i<previousBranchLengths.length; i++){
 				previousBranchLengths[i] = -1; //no previous branch length
 				for (int j=0; j< maxArchivePlaces; j++)
 					archivedProbabilities[i][j][0] = MesquiteDouble.unassigned;  //archive space is open
 			}
 	}
	/** Find an archive spot that is open at the node.  Recall from above that element 0 of the prob matrix is actually the branch length.
	Find a branch length marked as unassigned; this is the signal that the matrix is open for use.*/
 	protected int findOpenArchive(int node){
		if (archivedProbabilities==null)
			return -1;
		for (int i=0; i<maxArchivePlaces; i++)
			if (archivedProbabilities[node][i][0] == MesquiteDouble.unassigned)
				return i;
		return 0;
 	}
	/** Find an archive spot with given branch length at the node.  Recall from above that element 0 of the prob matrix is actually the branch length.
	Find a branch length same as that passed; this will indicate that matrix can be re-used.*/
 	protected int findMatchingArchive(int node, double branchLength){
		if (archivedProbabilities==null || branchLength == MesquiteDouble.unassigned)
			return -1;
		for (int i=0; i<maxArchivePlaces; i++)
			if (archivedProbabilities[node][i][0] == branchLength)
				return i;
		return -1;
 	}
 	/*.................................................................................................................*/
	public void initAvailableStates() {
		availableStates = new int[4];
		for (int i=0; i<4; i++)
			availableStates[i]=i;
	}
}

