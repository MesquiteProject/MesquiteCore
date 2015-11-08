/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

public class SimpleTaxonNamer extends TaxonNamer {
	String[] translationTable= null;
	int[] numberTranslationTable= null;
	
	public SimpleTaxonNamer() {
	}
	public boolean initialize(Taxa taxa){
		getTranslationTable(taxa);
		return true;
	}
	public String getNameToUse(Taxon taxon){
		return "t" + taxon.getNumber();  
	}
	public String getNameToUse(Taxa taxa, int it){
		return "t"+it;
	}
	/** Given a taxon name "name", this method returns the taxon number this name represents. 
	This one is based upon the contents of the translation file only. */
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

	/** Given a taxon number in another numbering system system, this method returns the taxon number in the main taxon numbering system. 
	This one is based upon the contents of the number translation table. This is a reverse of the standard translation table; 
	it is an array of taxon numbers, and the index into that array is the taxon number in the other system. */
	public int whichTaxonNumberFromNumberTranslationTable(Taxa taxa, int otherNumber){
		if (!MesquiteInteger.isCombinable(otherNumber) || numberTranslationTable==null)
			return -1;
		if (otherNumber>=0 && otherNumber<numberTranslationTable.length)
			return numberTranslationTable[otherNumber];
		return -1;
	}

	
	public String getTranslationTable(Taxa taxa){
		StringBuffer sb = new StringBuffer();
		if (translationTable==null || translationTable.length!=taxa.getNumTaxa())
			translationTable = new String[taxa.getNumTaxa()];
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			sb.append(getNameToUse(taxa,it)+"\t" + taxa.getTaxonName(it) + StringUtil.lineEnding());
			translationTable[it]=getNameToUse(taxa,it);
		}
		return sb.toString();
	}
	
	public void setNumberTranslationTable(int[] translation){
		if (translation==null)
			return;
		if (numberTranslationTable==null || numberTranslationTable.length!=translation.length)
			numberTranslationTable = new int[translation.length];
		for (int it=0; it<numberTranslationTable.length; it++){
			numberTranslationTable[it] = translation[it];
		}
	}

	
	public int whichTaxonNumberDefault(Taxa taxa, String name){
		if (StringUtil.notEmpty(name) && name.length()>=2) {
			if (name.charAt(0)=='t' || name.charAt(0)=='T') {
				String s = name.substring(1);
				return MesquiteInteger.fromString(s);
			}
		}
		return -1;
	}
	public int whichTaxonNumber(Taxa taxa, String name){
		if (StringUtil.blank(name))
			return -1;
		int number=-1;
		if (translationTable!=null && translationTable.length==taxa.getNumTaxa())
			number = whichTaxonNumberFromTranslationTable(taxa, name);
		if (numberTranslationTable!=null)
			number = whichTaxonNumberFromNumberTranslationTable(taxa, MesquiteInteger.fromString(name));
		if (number<0 || !MesquiteInteger.isCombinable(number))
			number = whichTaxonNumberDefault(taxa, name);
		return number;

	}

}
