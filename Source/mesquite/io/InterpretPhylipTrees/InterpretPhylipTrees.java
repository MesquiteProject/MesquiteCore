/* Mesquite (package mesquite.io).  Copyright 2000-2011 D. Maddison and W. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretPhylipTrees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.io.lib.*;


/* ============  a file interpreter for protein Phylip files ============*/

public class InterpretPhylipTrees extends InterpretPhylip {
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
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, StringBuffer outputBuffer){
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
	public void readTreeFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		boolean enlargeTaxaBlock = false;
		Taxa taxa = getProject().chooseTaxa(containerOfModule(), "From what taxa are these trees composed?");
		if (taxa== null) {
			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
			taxa = taxaTask.makeNewTaxa("Taxa", 0, false);
			taxa.addToFile(file, getProject(), taxaTask);
			enlargeTaxaBlock = true;
		}
		incrementMenuResetSuppression();
		if (file.openReading()) {
			readPhylipTrees(mf, file, null, null, taxa, enlargeTaxaBlock);
			finishImport(null, file, false );
		}
		decrementMenuResetSuppression();
	}
	/*.................................................................................................................*/
	protected void exportTrees(Taxa taxa, TreeVector treeVector, StringBuffer outputBuffer) { 
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
/*.................................................................................................................*/
    	 public String getName() {
		return "Phylip (trees)";
   	 }
/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports Phylip trees." ;
   	 }
	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    	public int getVersionOfFirstRelease(){
    		return 110;  
    	}
    	/*.................................................................................................................*/
    	public boolean isPrerelease(){
    		return false;
    	}

 	 
}
	

