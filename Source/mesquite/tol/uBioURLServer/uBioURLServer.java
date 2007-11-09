package mesquite.tol.uBioURLServer;

import mesquite.lib.*;
import mesquite.tol.lib.TaxonOnWebServer;

public class uBioURLServer extends TaxonOnWebServer {

	public String getURL(String taxonName) {
		String openName = StringUtil.encodeForURL(StringUtil.replace(taxonName,' ', '+'));
		return "http://www.ubio.org/browser/search.php?search_all=" + openName;
	
	}

	public String getName() {
		return "uBio";
	}

}
