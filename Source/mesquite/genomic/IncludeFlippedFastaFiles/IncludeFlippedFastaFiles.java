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

import mesquite.genomic.CombineFlippedFastas.CombineFlippedFastas;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class IncludeFlippedFastaFiles extends FileAssistantFM {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		String directoryPath = MesquiteFile.chooseDirectory("Choose folder containing FASTA files, one per taxon:", null); 
		if (StringUtil.blank(directoryPath))
			return false;
		CombineFlippedFastas importerTask = (CombineFlippedFastas)hireNamedEmployee(CombineFlippedFastas.class, "#ImportByTaxonFastas");
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
		return "Imports all \"flipped\" FASTA files in a folder, each containing the sequences of many loci for a single taxon, to merge them into the current project, matching the taxa and sequences by name to current taxa and matrices."
				+" Each input file should be named by the taxon name, and each sequence should be named for its locus. "
				+" Tuned for phylogenomics workflows that maintain a library of flipped fasta files that can be combined for varied studies with different taxon sampling. "
				+" Flipped FASTA files can be produced using File, Export, Flipped FASTA files (One per taxon). (Note: To establish a new project rather than merge into an existing, use Combine Flipped FASTA Files in the Open Special submenu.)" ;
	}

}


