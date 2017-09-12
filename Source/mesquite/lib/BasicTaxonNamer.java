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

public class BasicTaxonNamer extends TaxonNamer {
	
	public BasicTaxonNamer() {
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

	public String getNameToUse(Taxon taxon){
		return taxon.getName();  
	}
	public String getNameToUse(Taxa taxa, int it){
		return taxa.getTaxonName(it);
	}
	

	public int whichTaxonNumber(Taxa taxa, String name){
		if (StringUtil.blank(name))
			return -1;
		int number=-1;
		if (translationTable!=null && translationTable.length==taxa.getNumTaxa())
			number = whichTaxonNumberFromTranslationTable(taxa, name);
		if (number<0 || !MesquiteInteger.isCombinable(number)) {
			Taxon taxon = taxa.getTaxon(name);
			if (taxon!=null)
				number = taxon.getNumber();
		}
		return number;

	}
	public String getName(){
		return "Basic Taxon Namer";
	}

}
