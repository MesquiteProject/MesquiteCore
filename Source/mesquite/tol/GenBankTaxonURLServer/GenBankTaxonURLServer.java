package mesquite.tol.GenBankTaxonURLServer;

import mesquite.lib.*;
import mesquite.tol.lib.*;

public class GenBankTaxonURLServer extends TaxonOnWebServer {

	public String getURL(String taxonName) {
		String openName = StringUtil.encodeForURL(StringUtil.replace(taxonName,' ', '+'));
		return "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Undef&name=" + openName + "&lvl=0&srchmode=1";

	}

	public String getName() {
		return "GenBank Taxon";
	}

}
