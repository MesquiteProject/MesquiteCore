/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAState;


/* ======================================================================== */
public abstract class TaxonNamer  {
	protected String[] translationTable= null;
	
	public abstract String getNameToUse(Taxon taxon);
	public abstract String getNameToUse(Taxa taxa, int it);

	public boolean initialize(Taxa taxa){
		getTranslationTable(taxa);
		return true;
	}

	/** Given a taxon name "name", this method returns the taxon number this name represents. */
	public abstract int whichTaxonNumber(Taxa taxa, String name);	

	public String getName(){
		return "";
	}

	/** Given a taxon name "name", this method returns the taxon number this name represents. 
	This one is based upon the contents of the translation table created from the translation file only. The translation table 
	is an array of the names, and the taxon number is then the index into that array for a particular name. */
	public int whichTaxonNumberFromTranslationTable(Taxa taxa, String name){
		if (StringUtil.blank(name) || translationTable==null || translationTable.length!=taxa.getNumTaxa())
			return -1;
		for (int it=0; it<translationTable.length; it++){
			if (name.equalsIgnoreCase(translationTable[it])) {  //we've found the one we want
				return it;
			}
		}
		return -1;
	}

	public String getTranslationTable(Taxa taxa){
		StringBuffer sb = new StringBuffer();
		if (translationTable==null || translationTable.length!=taxa.getNumTaxa()){
			translationTable = new String[taxa.getNumTaxa()];
			for (int it=0; it<taxa.getNumTaxa(); it++) {
				translationTable[it]=taxa.getTaxonName(it);
			}
		} 
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			sb.append(translationTable[it]+"\t" + taxa.getTaxonName(it) + StringUtil.lineEnding());
		}

		return sb.toString();
	}

	/** This loads in a translation table contained in a file. */
	public boolean loadTranslationTable(Taxa taxa, String translationFile){
		if (taxa==null || StringUtil.blank(translationFile))
			return false;
		if (translationTable==null || translationTable.length!=taxa.getNumTaxa())
			translationTable = new String[taxa.getNumTaxa()];
		Parser parser = new Parser(translationFile);
		Parser subParser = new Parser();
		String line = parser.getRawNextDarkLine();

		boolean abort = false;
		subParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
		subParser.setWhitespaceString("\t");
		subParser.setPunctuationString("");


		while (!StringUtil.blank(line) && !abort) {
			String taxonNameInUse = subParser.getFirstToken();  //taxon Name
			String originalTaxonName = subParser.getNextToken();
			int taxonNumber = taxa.whichTaxonNumber(originalTaxonName);
			if (taxonNumber>=0 && taxonNumber<translationTable.length && MesquiteInteger.isCombinable(taxonNumber))
				translationTable[taxonNumber] = taxonNameInUse;

			line = parser.getRawNextDarkLine();
			subParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
		}	
		return true;
	}

}


