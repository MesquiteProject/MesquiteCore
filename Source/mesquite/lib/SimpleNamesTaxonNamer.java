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

public class SimpleNamesTaxonNamer extends TaxonNamer {
	int[] numberTranslationTable= null;
	
	public SimpleNamesTaxonNamer() {
	}
	public String getNameToUse(Taxon taxon){
		return "t" + taxon.getNumber();  
	}
	public String getNameToUse(Taxa taxa, int it){
		return "t"+it;
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
		if (!MesquiteInteger.isCombinable(number))
			return -1;
		return number;

	}
	public String getName(){
		return "Simple Taxon Namer";
	}

}
