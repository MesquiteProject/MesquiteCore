/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.SlatkinMaddisonS;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.assoc.lib.*;
import mesquite.parsimony.lib.*;


/* ======================================================================== */
/** This evaluates a gene tree by calculating how much lineage sorting is implied by a containing species tree. */
public class SlatkinMaddisonS extends NumberForTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e3 = registerEmployeeNeed(AssociationSource.class, getName() + " needs a taxa association to indicate how contained taxa fit within containing taxa.",
		"The source of associations may be arranged automatically or is chosen initially.");
		EmployeeNeed e4 = registerEmployeeNeed(ParsAncStatesForModel.class, getName() + " uses a parsimony module to count s.",
		"This is arranged automatically.");
		e4.setSuppressListing(true);
	}
	AssociationSource associationTask; //better to use a partitionSource!
	MesquiteString treeSourceName;
	TaxaAssociation association;
	Taxa containingTaxa;
	Tree lastTree;
	ParsAncStatesForModel countTask;
	public String getKeywords(){
		return "gene_tree species_tree coalescence coalescent";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon associations");
		if (associationTask == null)
			return sorry(getName() + " couldn't start because no source of taxon associations was found.");
 		countTask = (ParsAncStatesForModel)hireNamedEmployee(ParsAncStatesForModel.class, StringUtil.tokenize("Parsimony Unordered"));
 		if (countTask ==null)
 			return sorry(getName() + " couldn't start because the S counting module (Parsimony Unordered) was not obtained.");
 		return true;
  	 }
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
  	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree){
   	}
	/*.................................................................................................................*/
	CategoricalAdjustable locations;
	boolean warned = false;
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		if (result==null)
			return;
		clearResultAndLastResult(result);
		lastTree = tree;
        	if (association == null || (association.getTaxa(0)!= tree.getTaxa() && association.getTaxa(1)!= tree.getTaxa())) {
        		association = associationTask.getCurrentAssociation(tree.getTaxa());
        		if (association == null){
        			if (resultString != null)
        				resultString.setValue("Slatkin & Maddison s not calculated; taxa association not found");
        			return;
        		}
        		if (association.getTaxa(0)== tree.getTaxa())
        			containingTaxa = association.getTaxa(1);
        		else
        			containingTaxa = association.getTaxa(0);
        	}

	        Taxa genes = tree.getTaxa();
	        
	        if (locations==null || locations.getNumTaxa()!=genes.getNumTaxa())
	        	 locations =  new CategoricalAdjustable(genes, genes.getNumTaxa());
	        	 
	        locations.deassignStates();
	        for (int i = 0; i<genes.getNumTaxa(); i++){
	        	Taxon[] species = association.getAssociates(genes.getTaxon(i));
	        	if (species!=null && species.length>0){
	        		long accumulative = 0L;  //accumulate a state set of all species in which gene i occurs, and set the state of the locations characters accordingly
	        		int card = 0;
	        		for (int j = 0; j<species.length; j++){
	        			int cont = containingTaxa.whichTaxonNumber(species[j]);
	        			if (cont> CategoricalState.maxCategoricalState) {
	        				if (!warned)
	        					discreetAlert( "The calculation of Slatkin & Maddison's s will be inaccurate because there are more than the maximum allowed (" + (CategoricalState.maxCategoricalState+1) + ") number of containing taxa.");
	        				warned = true;
	        			}
	        			else {
		        			long set = CategoricalState.makeSet(cont);
		        			accumulative |= set;
		        			card++;
	        			}
	        		}
	        		if (card>1)
	        			accumulative |= CategoricalState.makeSet(CategoricalState.uncertainBit);
	        		locations.setState(i, accumulative);
	        	}
	        }
	        
	        countTask.calculateSteps(tree, locations, null, null, result);
		if (resultString!=null)
			resultString.setValue("s of Slatkin & Maddison: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	public boolean biggerIsBetter() {
		return false;
	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Calculates 's' of Slaktin & Maddison 1989 for tree of genes associated with given populations";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "s of Slatkin & Maddison";
   	 }
}

