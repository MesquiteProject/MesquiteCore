package mesquite.tol.TreeBaseURLServer;

	

import mesquite.lib.*;
import mesquite.tol.lib.*;

public class TreeBaseURLServer extends TaxonOnWebServer {

	public String getURL(String taxonName) {
		String openName = StringUtil.encodeForURL(StringUtil.replace(taxonName,' ', '+'));
		return "http://www.treebase.org/cgi-bin/treebase.pl?Name=" + openName + "&Submit=Taxon+Name";
	
	}

	public String getName() {
		return "TreeBase";
	}

}
