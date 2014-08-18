package mesquite.lib;

public class SimpleTaxonNamer extends TaxonNamer {
	String[] translationTable= null;
	
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
	This one is based upon the contents of the translation table created from the translation file only. */
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
		if (translationTable==null || translationTable.length!=taxa.getNumTaxa())
			translationTable = new String[taxa.getNumTaxa()];
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			sb.append(getNameToUse(taxa,it)+"\t" + taxa.getTaxonName(it) + StringUtil.lineEnding());
			translationTable[it]=getNameToUse(taxa,it);
		}
		return sb.toString();
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
		if (translationTable==null || translationTable.length!=taxa.getNumTaxa())
			return whichTaxonNumberDefault(taxa, name);
		
		return whichTaxonNumberFromTranslationTable(taxa, name);
	}

}
