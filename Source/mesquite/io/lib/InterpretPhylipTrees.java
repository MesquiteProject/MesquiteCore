/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.lib;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.categ.lib.*;
import mesquite.io.lib.*;


/* ============  a file interpreter for phylip trees ============*/

public abstract class InterpretPhylipTrees extends InterpretPhylip {
/*.................................................................................................................*/
	public void setPhylipState(CharacterData data, int ic, int it, char c){
		//only deals with trees
	}
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		readTreeFile( mf,  file,  arguments);
	}
/*........................../*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true;  //
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		 return project.getNumberOfFileElements(TreeVector.class) > 0;  //
	}
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return false;
	}
/*.................................................................................................................*/
	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  
		 return null;  //
	}
/*.................................................................................................................*/
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, MesquiteStringBuffer outputBuffer){
		//
	}
/*.................................................................................................................*/
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		return true;
	}	
/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		return null;
	}
	/*.................................................................................................................*/
	public boolean importExtraFiles(MesquiteFile file, Taxa taxa, TreeVector trees) {  
		 return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}

/*.................................................................................................................*/
	public void readTreeFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		boolean enlargeTaxaBlock = false;
		Taxa taxa = getProject().chooseTaxa(containerOfModule(), "From what taxa are these trees composed?");
		if (taxa== null) {
			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
			if (taxaTask==null) 
				return;
			taxa = taxaTask.makeNewTaxaBlock("Taxa", 0, false);
			taxa.addToFile(file, getProject(), taxaTask);
			enlargeTaxaBlock = true;
		}
		incrementMenuResetSuppression();
		if (file.openReading()) {
			initializeTreeImport(file, taxa);
			if (StringUtil.notEmpty(arguments) && arguments.indexOf("useStandardizedTaxonNames")>=0)
				taxonNamer = new SimpleNamesTaxonNamer();
				
			TreeVector trees = IOUtil.readPhylipTrees(this,mf, file, null, null, taxa, enlargeTaxaBlock, taxonNamer,getTreeNameBase(), true);
			importExtraFiles(file,taxa, trees);
			finishImport(null, file, false );
		}
		decrementMenuResetSuppression();
	}
	/*.................................................................................................................*/
	protected void exportTrees(Taxa taxa, TreeVector treeVector, MesquiteStringBuffer outputBuffer) { 
		Tree tree;
		if (treeVector !=null && treeVector.size()>0) {
			for (int iTree = 0; iTree < treeVector.size(); iTree++) {
				tree = (Tree)treeVector.elementAt(iTree);
				outputBuffer.append(tree.writeTreeSimpleByNames());  //or Tree.BY_NUMBERS  or Tree.BY_NAMES
				// if do it BY_NAMES, make sure you truncate the taxon names to 10 characters!!
				outputBuffer.append(getLineEnding());
			}
		}
	}

 	 
}
	

