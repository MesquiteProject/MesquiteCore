package mesquite.lib;

public class SimpleTaxonNamer extends TaxonNamer {
	public String getNameToUse(Taxon taxon){
		return "t" + taxon.getNumber();
	}
	public String getNameToUse(Taxa taxa, int it){
		return "t"+it;
	}
	public int whichTaxonNumber(Taxa taxa, String name){
		if (StringUtil.notEmpty(name) && name.length()>=2) {
			if (name.charAt(0)=='t' || name.charAt(0)=='T') {
				String s = name.substring(1);
				return MesquiteInteger.fromString(s);
			}
		}
		return -1;
	}

}
