/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.
Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.SharedProportionalDistance;

import mesquite.lib.CommandRecord;
import mesquite.lib.Incrementable;
import mesquite.lib.Taxa;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.distance.lib.*;

public class SharedProportionalDistance extends IncTaxaDistanceSource implements Incrementable {
	TreeSource treeSourceTask;
	Taxa taxa;
	long currentTree = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
	 	treeSourceTask = (TreeSource)hireEmployee(commandRec, TreeSource.class, "Source of trees (for Proportion Shared distance for " + getEmployer().getName()  + ")");
	 	if (treeSourceTask == null) {
	 		return sorry(commandRec, getName() + " couldn't start because no source of a tree was obtained.");
	 	}
	 	return true;
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification, CommandRecord commandRec) {
	 	if (employee != treeSourceTask || Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED)
	 		super.employeeParametersChanged(employee, source, notification, commandRec);
	}
	  	 
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	  happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa, CommandRecord commandRec){
		this.taxa = taxa;
	   		treeSourceTask.initialize(taxa, commandRec);
	}

	public TaxaDistance getTaxaDistance(Taxa taxa, CommandRecord commandRec){
		Tree tree = treeSourceTask.getTree(taxa, (int)currentTree, commandRec);
		if (tree == null) {
			MesquiteMessage.warnProgrammer("No tree in Proportion Shared Distance " + currentTree + "  source " + treeSourceTask.getName());
			return null;
		}
		return new SharedProportionTD( taxa, tree);
	}

	/*.................................................................................................................*/
	public void setCurrent(long i, CommandRecord commandRec){  //SHOULD NOT notify (e.g., parametersChanged)
	 	currentTree = i;
	}
	public long getCurrent(CommandRecord commandRec){
	 	return currentTree;
	}
	public String getItemTypeName(){
	 	return "Tree";
	}
	public long getMin(CommandRecord commandRec){
		return 0;
	}
	public long getMax(CommandRecord commandRec){
	 	long n = treeSourceTask.getNumberOfTrees(taxa, commandRec)-1;
		return n;
	}
	public long toInternal(long i){
	 	return i-1;
	}
	public long toExternal(long i){ //return whether 0 based or 1 based counting
		return i+1;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Shared Proportion Distances implied by Tree";  
	}
	   	 
	public String getParameters() {
	 	return "Distances among taxa implied by tree from : " + treeSourceTask.getName();
	}
	/*.................................................................................................................*/
	   	 
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Distances among taxa implied by proportion of total branchlength (root to tip) that is shared (Shared Proportion distances).  Unassigned branches are treated as of length 1.0." ;
	}
	   	 
	public boolean isPrerelease(){
		return true;
	}
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	
    public String getAuthors(){
		return "Peter Midford and Wayne Maddison";
    }


	/** This calculates the proportion of the history of two nodes is shared.  It is calculated as the shared history
	 * distance divided by the average distance of the two nodes from the root.  This works fine when comparing tips,
	 * but could produce numbers greater than one if one node is ancestral to the other.
	 */ 
	class SharedProportionTD extends TaxaDistance {
		double[][] distances;
		int numTaxa;
		public SharedProportionTD(Taxa taxa, Tree tree){
			super(taxa);
			numTaxa = taxa.getNumTaxa();
			distances= new double[numTaxa][numTaxa];
			Double2DArray.deassignArray(distances);
			int root = tree.getRoot();
			for (int taxon1=0; taxon1<numTaxa; taxon1++) {
				int node1 = tree.nodeOfTaxonNumber(taxon1);
				if (tree.nodeExists(node1)){
					int node = node1;
					double node1Height = 0;
					while (node != root && tree.nodeExists(node)){
						node1Height += tree.getBranchLength(node, 1.0);
						node = tree.motherOfNode(node);
					}
					for (int taxon2=0; taxon2<numTaxa; taxon2++) {
						int node2 = tree.nodeOfTaxonNumber(taxon2);
						if (tree.nodeExists(node2)){
							node = node2;
							double node2Height = 0;
							while (node != root && tree.nodeExists(node)){
								node2Height += tree.getBranchLength(node, 1.0);
								node = tree.motherOfNode(node);
							}
							int mrca = tree.mrca(node1, node2);
							node = mrca;
							double ancestorHeight = 0;
							while (node != root && tree.nodeExists(node)){
								ancestorHeight += tree.getBranchLength(node, 1.0);
								node = tree.motherOfNode(node);
							}
							distances[taxon1][taxon2]=(2*ancestorHeight)/(node1Height+node2Height);
						}
					}
				}
			}
		}
		public double getDistance(int taxon1, int taxon2){
			if (taxon1>=0 && taxon1<numTaxa && taxon2>=0 && taxon2<numTaxa)
				return distances[taxon1][taxon2];
			else
				return MesquiteDouble.unassigned;
		}

		public double[][] getMatrix(){
			return distances;
		}

		public boolean isSymmetrical(){
			return true;
		}
	

	}

}
