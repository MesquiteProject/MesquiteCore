package mesquite.tol.ToLURLServer;

import mesquite.lib.*;
import mesquite.tol.lib.TaxonOnWebServer;

public class ToLURLServer extends TaxonOnWebServer {

	public String getURL(String taxonName) {
		String openName = StringUtil.encodeForURL(taxonName);
		return "http://tolweb.org/" + openName;
	}

	public String getName() {
		return "Tree of Life Web Project";
	}
	
	public String getExplanation() {
		return "Provides a URL to a taxon on the Tree of Life Web Project";
	}


}
