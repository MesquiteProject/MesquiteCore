/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.IncludeFlippedFastaFiles;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.genomic.ImportByTaxonFastas.ImportByTaxonFastas;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteFrame;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class IncludeFlippedFastaFiles extends FileAssistantFM {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		String directoryPath = MesquiteFile.chooseDirectory("Choose folder containing FASTA files, one per taxon:", null); 
		if (StringUtil.blank(directoryPath))
			return false;
		ImportByTaxonFastas importerTask = (ImportByTaxonFastas)hireNamedEmployee(ImportByTaxonFastas.class, "#ImportByTaxonFastas");
		if (importerTask == null)
			return false;
		importerTask.processDirectory(directoryPath, getProject());
		return true;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return true;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Include Data from Flipped FASTAs (One per Taxon)...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Include Data from Flipped FASTAs";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports a folder series of FASTA files each containing all of a taxon's sequences in each of a series of loci, merging them into the current project, combining the taxa and matrices for each of those loci." ;
	}

}


