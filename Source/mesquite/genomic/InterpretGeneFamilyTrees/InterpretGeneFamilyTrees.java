/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.InterpretGeneFamilyTrees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.BasicTaxonNamer;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.taxa.TaxonNamer;
import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.categ.lib.*;
import mesquite.io.lib.*;


/* ============  a file interpreter for gene family trees in standard Newick ============*/

public class InterpretGeneFamilyTrees extends InterpretPhylipTrees {
	GeneFamilyTaxonNamer geneFamilyTaxonNamer = new GeneFamilyTaxonNamer(this);
	/*.................................................................................................................*/
	public void readTreeFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		loadPreferences();
		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying about options")){
			if (geneFamilyTaxonNamer.nameParser.queryOptions("Finding species name in gene name", 
					"Species names will be found by removing the gene copy-specific parts of gene names.", 
					"To extract the species name from the gene name,",null)){
				storePreferences();
			}
			else
				return;
		}
		setTaxonNamer(geneFamilyTaxonNamer);
		super.readTreeFile(mf, file, arguments);
	}
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		if (geneFamilyTaxonNamer.nameParser!=null){
			String s = geneFamilyTaxonNamer.nameParser.preparePreferencesForXML(); 
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if (geneFamilyTaxonNamer.nameParser!=null)
			geneFamilyTaxonNamer.nameParser.processSingleXMLPreference(tag,content);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Gene Family Trees";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports Newick trees for gene families, with unique gene copy names appended to the species/taxon names." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*........................../*.................................................................................................................*/
	public boolean canExportEver() {  
		return false;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return false;  //
	}


}


class GeneFamilyTaxonNamer extends BasicTaxonNamer {
	InterpretGeneFamilyTrees ownerModule;
	NameParser nameParser;
	DisplayableBranchProperty geneCopyNameDBP;
	boolean verbose = false;

	public GeneFamilyTaxonNamer(InterpretGeneFamilyTrees ownerModule) {
		nameParser = new NameParser(ownerModule, "Taxon");
		geneCopyNameDBP = new DisplayableBranchProperty("Gene copy name", Associable.STRINGS);
		geneCopyNameDBP.showing = true;
		geneCopyNameDBP.showName = false;
		geneCopyNameDBP.showIfUnassigned = false;
		geneCopyNameDBP.fontSize = 10;
		DisplayableBranchProperty.addToPreferences(geneCopyNameDBP);
	}

	NameReference geneNameRef = NameReference.getNameReference("Gene copy name");

	// this is the name for the a new taxon
	public void setNameOfNewTaxon(Taxa taxa, int it, Tree tree, String original){
		if (StringUtil.blank(original))
			return;
		String baseName = nameParser.extractPart(original);
		int count = 2;
		String suffix = ".1";
		do {
			Taxon taxon = taxa.getTaxon(baseName+suffix);

			if (taxon ==null){ ///new!
				if (verbose) 
				System.err.println(" --->" + baseName+suffix + " for taxon " + it + " at node " + tree.nodeOfTaxonNumber(it));
				taxa.setTaxonName(it, baseName+suffix);
				taxonNameSet(tree, it, original);
				return;
			}
			suffix = "." + count++;
		}
		while (true);
	}

	public void taxonNameSet(Tree tree, int it, String original){
		((MesquiteTree)tree).setAssociatedString(geneNameRef, tree.nodeOfTaxonNumber(it), original);
	}
	int findDerivativeInTaxaButNotInTree(Tree tree, String baseName){
		Taxa taxa = tree.getTaxa();
		int count = 2;
		String suffix = ".1";
		while (count<taxa.getNumTaxa()+1){
			Taxon taxon = taxa.getTaxon(baseName+suffix);
			if (taxon != null){
				int taxonNumber =  taxon.getNumber();
				if (verbose) System.err.println("  " + baseName+suffix + " in taxa");
				if (!tree.nodeExists(tree.nodeOfTaxonNumber(taxonNumber))){//taxon not in tree; return its number so it can be added
					if (verbose) System.err.println("  and not in tree!");
					return taxonNumber;
				}
			}
			suffix = "." + count++;
		}
		if (verbose) System.err.println("  " + baseName + " & derivaties NOT in taxa");
		return -1;
	}

	public int whichTaxonNumber(Tree tree, String name){
		if (StringUtil.blank(name))
			return -1;
		String baseName = nameParser.extractPart(name);
		if (verbose) System.err.println("  " + baseName);
		int found =  findDerivativeInTaxaButNotInTree(tree,baseName);
		if (found>=0){
			if (verbose) System.err.println(" --- " + found + " " + tree.nodeOfTaxonNumber(found));
			((MesquiteTree)tree).setAssociatedString(geneNameRef, tree.nodeOfTaxonNumber(found), name);
		}
		return found;
		/* Situations
		 -- baseName not in taxa block. Therefore new. Return -1
		 -- baseName in taxa block. 
		 	-- baseName not in tree. Therefore return that taxon's number and proceed in standard way for taxon to be added to tree
		 	-- baseName in tree. Therefore return -1 so that later request for name on expansion can return unique new name
		 */

	}
	public String getName(){
		return "Gene Family Taxon Namer";
	}

}
